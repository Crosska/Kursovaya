package com.crosska;

import java.io.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;

public class Main
{
    public static void main(String[] Args) throws IOException
    {
        String Filename = "System.dat";                 // Название файла с ФС
        OS DevOS = new OS();                            // Создание обьекта ОС
        DevOS.OS(Filename);                             // Вызов начальной функции ОС с передачей названия файла
    }
}

class OS
{
    GregorianCalendar GCalendar = new GregorianCalendar();
    // Глобальынй сканнер ввода
    private Scanner scan = new Scanner(System.in);
    // Название файла с системой
    private String Filename;
    // Структура суперблока
    private short SZ_SuperBlock = 2048;                 // Размер кластера
    private short SZ_SizeBitMap = 3200;                 // Размер битовой карты блоков
    private short SZ_SizeBitMapInodes = 64;             // Размер битовой карты inode
    private short SZ_InodesCount = 512;                 // Количество инодов
    private short SZ_FreeInodesCount = 512;             // Количество свободных inode
    private short SZ_FreeBlocksCount = 25575;           // Количество свободных блоков
    // Смещения по разделам
    private short MV_FOMapBlocks = 2049;                // Бкс\з блоков
    private short MV_FOMapInodes = 6145;                // Бкс\з inode
    private short MV_IListMass = 8193;                  // Массив индексных дескрипторов
    private int MV_RootDirectory = 47105;               // Корневой каталог
    private int MV_Data = 49153;                        // Данные
    // Битовая карта с\з кластеров
    //private byte[] BM_CLusters = new byte[4096];
    //private BitmapInode[] mass = new BitmapInode[4096];

    void OS(String OSName) throws IOException           // Начальная функция создания ОС
    {
        Filename = OSName;                              // Сохранение имени файла в глобальную переменную
        W_SB();                                         // Запись стандратного Супер блока
        W_BMClusters();                                 // Запись начальной битовой карты с\з блоков
        W_BMInodes();                                   // Запись начальной битовой карты с\з инодов
        W_IList();                                      // Запись начального списка инодов
        //ConvertToBitView((byte)-32);
        InterfaceOS();                                  // Вызов метода работы с пользователем
    }

    void InterfaceOS() throws IOException
    {
        String username = "";
        String password;
        boolean OSRun = true;                           // Переменная работы ОС
        boolean SessionRun;                             // Переменная работы текущей сессии пользователя
        String command;
        while (OSRun)
        {
            System.out.print("\nlogin: ");
            username = scan.nextLine();
            System.out.print("Password: ");
            password = scan.nextLine();
            SessionRun = true;
            String[] subStr;

            while (SessionRun)
            {
                System.out.print("\n" + username + ">");
                command = scan.nextLine();
                String delimeter = " ";
                subStr = command.split(delimeter);
                if (subStr.length == 2)
                {
                    switch (subStr[0])
                    {
                        case "mkfile":
                            int cursor_free_block;
                            cursor_free_block = FindFreeBlock();
                            System.out.println("Указатель пустого блока" + cursor_free_block);
                            if (cursor_free_block == -1)
                            {
                                System.out.println("Error. All blocks are occupied. Delete some files.");
                            } else
                            {
                                int cursor_free_inode;
                                cursor_free_inode = FindFreeInode();
                                cursor_free_inode = (MV_FOMapInodes - cursor_free_inode) * 72; // Получение адреса найденного пустого инода в списке инодов
                                System.out.println("Указатель пустого инода" + cursor_free_inode);
                                if (cursor_free_inode == -1)
                                {
                                    System.out.println("Error. File system have max count files. Delete some files.");
                                } else
                                {
                                    CreateNewFile(cursor_free_block, cursor_free_inode, subStr[1]);
                                }
                            }
                            break;
                        case "rmfile":
                            break;
                        case "help":
                            System.out.println("Command line functions:\n" +
                                    "mkfile <filename> - create new file\n" +
                                    "rmfile <filename> - delete existing file\n" +
                                    "help - show help for the command line application\n" +
                                    "logout - log out from current account\n" +
                                    "shutdown - turn off file system");
                            break;
                        case "logout":
                            SessionRun = false;
                            break;
                        case "shutdown":
                            SessionRun = false;
                            OSRun = false;
                        default:
                            System.out.println("Type \"help\" if you don't know what to do.");
                    }
                } else
                {
                    System.out.println("Type \"help\" if you don't know what to do.");
                }
            }
        }
    }

    private int FindFreeInode() throws IOException
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw");
        long end_cursor = MoveToPartition(file, 0, 3);
        file.close();
        file = new RandomAccessFile(new File(Filename), "rw");
        long cursor = MoveToPartition(file, 0, 2);
        file.seek(cursor);
        while (cursor < end_cursor)
        {
            System.out.println("Начальный курсор = " + cursor + " | Конечный курсор = " + end_cursor);
            byte inode = file.readByte();
            String temp = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(inode))).replace(' ', '0');
            System.out.println(temp);
            for (int i = 0; i < temp.length(); i++)
            {
                if (temp.charAt(i) == '0')
                {
                    System.out.println("Свободный бит");
                    long cursor_inode_long = file.getFilePointer() - 1; // Минус один для того, чтобы каретка вернулась на найденный пустой бит
                    Integer cursor_inode_int = (int) (long) cursor_inode_long;
                    file.close();
                    return cursor_inode_int;
                } else
                {
                    System.out.println("Занятый бит");
                }
            }
            cursor = file.getFilePointer();
        }
        file.close();
        return -1;
    }

    private int FindFreeBlock() throws IOException
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw");
        long end_cursor = MoveToPartition(file, 0, 2);
        file.close();
        file = new RandomAccessFile(new File(Filename), "rw");
        long cursor = MoveToPartition(file, 0, 1);
        file.seek(cursor);
        while (cursor < end_cursor)
        {
            System.out.println("Начальный курсор = " + cursor + " | Конечный курсор = " + end_cursor);
            byte block = file.readByte();
            String temp = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(block))).replace(' ', '0');
            //System.out.println(temp);
            for (int i = 0; i < temp.length(); i++)
            {
                if (temp.charAt(i) == '0')
                {
                    //System.out.println("Свободный бит");
                    long cursor_block_long = file.getFilePointer() - 1;
                    Integer cursor_block_int = (int) (long) cursor_block_long;
                    file.close();
                    return cursor_block_int;
                } else
                {
                    //System.out.println("Занятый бит");
                }
            }
            cursor = file.getFilePointer();
        }
        file.close();
        return -1;
    }

    // --- --- --- --- --- --- --- Системные функции

    private void ShowSB()                               // Вывести данные о Супер блоке
    {
        System.out.println(SZ_SuperBlock + "\n" + SZ_SizeBitMap + "\n" + SZ_SizeBitMapInodes + "\n" +
                SZ_InodesCount + "\n" + SZ_FreeInodesCount + "\n" + SZ_FreeBlocksCount + "\n" + MV_FOMapBlocks +
                "\n" + MV_FOMapInodes + "\n" + MV_IListMass + "\n" + MV_RootDirectory + "\n" + MV_Data);
    }

    private void SetSB()                                // Установить стандартные значение Супер блока
    {
        SZ_SuperBlock = 2048;        // Размер кластера
        SZ_SizeBitMap = 3200;        // Размер битовой карты блоков
        SZ_SizeBitMapInodes = 64;    // Размер битовой карты inode
        SZ_InodesCount = 512;        // Количество инодов
        SZ_FreeInodesCount = 512;    // Количество свободных inode
        SZ_FreeBlocksCount = 25575;  // Количество свободных блоков
        MV_FOMapBlocks = 2049;       // Бкс\з блоков
        MV_FOMapInodes = 6145;       // Бкс\з inode
        MV_IListMass = 8193;         // Массив индексных дескрипторов
        MV_RootDirectory = 47105;      // Корневой каталог
        MV_Data = 49153;
    }

    private long MoveToPartition(RandomAccessFile file, int bytes, int moveToPartition) throws IOException // Сдвинуть каретку в файле
    {
        long cursorPoint = -1;
        switch (moveToPartition)
        {
            case 0:
                file.skipBytes(bytes);              // На определнное количество байт
                cursorPoint = file.getFilePointer();
                break;
            case 1:
                file.skipBytes(MV_FOMapBlocks);     // На раздел битовой карты с/з блоков
                cursorPoint = file.getFilePointer();
                break;
            case 2:
                file.skipBytes(MV_FOMapInodes);     // На раздел битовой карты с/з inodes
                cursorPoint = file.getFilePointer();
                break;
            case 3:
                file.skipBytes(MV_IListMass);       // На раздел массива индексных дескрипторов
                cursorPoint = file.getFilePointer();
                break;
            case 4:
                file.skipBytes(MV_RootDirectory);   // На раздел корневого каталога
                cursorPoint = file.getFilePointer();
                break;
            case 5:
                file.skipBytes(MV_Data);            // На раздел данных
                cursorPoint = file.getFilePointer();
                break;
            default:
                System.out.println("\n\n--- --- --- Ошибка при переходе в раздел диска! --- --- ---\n\n");
        }
        return cursorPoint;
    }

    private void ConvertToBitView(Byte number)
    {
        System.out.println(String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(number))).replace(' ', '0'));
    }

    // --- --- --- --- --- --- --- Работа с Супер блоком

    private void R_SB() throws IOException              // Считать весь Супер блок
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "r");
        SZ_SuperBlock = file.readShort();
        SZ_SizeBitMap = file.readShort();
        SZ_SizeBitMapInodes = file.readShort();
        SZ_InodesCount = file.readShort();
        SZ_FreeInodesCount = file.readShort();
        SZ_FreeBlocksCount = file.readShort();
        MV_FOMapBlocks = file.readShort();
        MV_FOMapInodes = file.readShort();
        MV_IListMass = file.readShort();
        MV_RootDirectory = file.readInt();
        MV_Data = file.readInt();
        file.close();
    }

    private void W_SB() throws IOException              // Записать Супер блок
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw");
        file.setLength(52428800);
        file.writeShort(SZ_SuperBlock);
        file.writeShort(SZ_SizeBitMap);
        file.writeShort(SZ_SizeBitMapInodes);
        file.writeShort(SZ_InodesCount);
        file.writeShort(SZ_FreeInodesCount);
        file.writeShort(SZ_FreeBlocksCount);
        file.writeShort(MV_FOMapBlocks);
        file.writeShort(MV_FOMapInodes);
        file.writeShort(MV_IListMass);
        file.writeInt(MV_RootDirectory);
        file.writeInt(MV_Data);
        file.close();
        System.out.println("* Выполнена автоматическая запись суперблока *");
    }

    // --- --- --- --- --- --- --- Работа с битовой картой блоков

    private void W_BMClusters() throws IOException      // Записать битовую картку с\з кластеров
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw");
        long cursor = MoveToPartition(file, 0, 1);
        file.seek(cursor);
        byte start = -8; // 11111000
        byte defolt = 0;
        file.writeByte(start);
        for (int i = 0; i < 4095; i++)
        {
            file.writeByte(defolt);
        }
        file.close();
        System.out.println("* Выполнена автоматическая запись битовой карты с/з блоков *");
    }

    private void R_ElementBMCluster(int shift) throws IOException // Считать один байт из битовой карты с\з кластеров
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "r");
        long cursor = MoveToPartition(file, 0, 1);
        file.seek(cursor);
        cursor = MoveToPartition(file, shift, 0);
        file.seek(cursor);
        byte readed = file.readByte();
        System.out.println(String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(readed))).replace(' ', '0'));
        file.close();
    }

    // --- --- --- --- --- --- --- Работа с битовой картой инодов

    private void W_BMInodes() throws IOException
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw");
        long cursor = MoveToPartition(file, 0, 2);
        file.seek(cursor);
        byte defolt = 0;
        for (int i = 0; i < 4096; i++)
        {
            file.writeByte(defolt);
        }
        file.close();
        System.out.println("* Выполнена автоматическая запись битовой карты с/з инодов *");
    }

    // --- --- --- --- --- --- --- Работа со списком инодов

    private void W_IList() throws IOException
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw");
        long cursor = MoveToPartition(file, 0, 3);
        file.seek(cursor);
        byte defolt = 0;
        for (int i = 0; i < 38912; i++)
        {
            file.writeByte(defolt);
        }
        file.close();
        System.out.println("* Выполнена автоматическая запись массива индексных дескрипторов*");
    }

    // --- --- --- --- --- --- --- Работа с файлами

    private void CreateUsersFile() throws IOException
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw");
        long cursor = MoveToPartition(file, 0, 3);
    }

    private void CreateCatalogFile() throws IOException
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw");
        long cursor = MoveToPartition(file, 0, 3);
    }

    private void CreateNewFile(long cursor_free_block, int byte_shift_inode, String file_name) throws IOException
    {

        RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw");
        long cursor = MoveToPartition(file, 0, 3);
        cursor = MoveToPartition(file, byte_shift_inode, 0);
        file.seek(cursor);
        char file_type = 'f';
        byte file_rights = -32; // 11100000
        short link_count;
        short user_id;
        short group_id;
        long file_size = 0;
        byte created_date = (byte) GCalendar.get(Calendar.DATE);
        byte created_month = (byte) GCalendar.get(Calendar.MONTH);
        short created_year = (short) GCalendar.get(Calendar.YEAR);
        byte changed_date = (byte) GCalendar.get(Calendar.DATE);
        byte changed_month = (byte) GCalendar.get(Calendar.MONTH);
        short changed_year = (short) GCalendar.get(Calendar.YEAR);
        long[] di_adress = new long[12];
        long adressing;
    }

}
