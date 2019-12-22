package com.crosska;

import javax.crypto.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Main
{
    public static void main(String[] Args)
    {
        String Filename = "System.dat";                 // Название файла с ФС
        OS DevOS = new OS();                            // Создание обьекта ОС
        DevOS.Run(Filename);                             // Вызов начальной функции ОС с передачей названия файла
    }
}

class OS
{
    //private short UID = 0;
    //private short GID = 0;
    //private String path = "\\";
    private Scanner scan = new Scanner(System.in);

    private SuperBlock SB = new SuperBlock();
    private BlocksBitMap BBM = new BlocksBitMap();
    private InodesBitMap IBM = new InodesBitMap();
    private InodesList IL = new InodesList();
    private RootCatalog RC = new RootCatalog();
    private Data DT = new Data();
    //private FileWork FW = new FileWork();

    void Run(String Filename)
    {
        SB.SetFilename(Filename);
        BBM.SetFilename(Filename);
        IBM.SetFilename(Filename);
        IL.SetFilename(Filename);
        RC.SetFilename(Filename);
        DT.SetFilename(Filename);

        SB.WriteSB();
        BBM.WriteBitMap();
        IBM.WriteBitMap();
        IL.WriteIList();
        RC.WriteRootCatalog();
        //RC.ReadRootCatalog();

        /*int blocks_byte_to_skip = FindFreeBlock();
        int inodes_byte_to_skip = FindFreeInode();
        inodes_byte_to_skip = (MV_FOMapInodes - inodes_byte_to_skip) * 72;
        CreateCatalog("/", inodes_byte_to_skip, blocks_byte_to_skip);
        ReadCatalog();
        CreateUsersFile();*/

        String username = "";
        String password = "";
        boolean OSRun = true;                           // Переменная работы ОС
        boolean SessionRun;                             // Переменная работы текущей сессии пользователя
        String command;
        while (OSRun)
        {
            System.out.print("\nlogin: ");
            username = scan.nextLine();
            System.out.print("password: ");
            password = scan.nextLine();
            SessionRun = true;
            String[] subStr;
            while (SessionRun)
            {
                System.out.print("\n" + username + ">");
                command = scan.nextLine();
                String delimeter = " ";
                subStr = command.split(delimeter);
                if (subStr.length <= 2 && subStr.length > 0)
                {
                    switch (subStr[0])
                    {
                        case "mkfile":
                            /*int cursor_free_block;
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
                                }
                            }*/
                            break;
                        case "rmfile":
                            break;
                        case "-help":
                            System.out.println("Command line functions:\n" +
                                    "mkfile <filename> - create new file\n" +
                                    "rmfile <filename> - delete existing file\n" +
                                    "-help - show help for the command line application\n" +
                                    "logout - log out from current account\n" +
                                    "shutdown - turn off file system");
                            break;
                        case "logout":
                            SessionRun = false;
                            break;
                        case "shutdown":
                            SessionRun = false;
                            OSRun = false;
                            break;
                        case "-getsb":
                            SB.ReadSB();
                            break;
                        case "-setsb":
                            //SetSB();
                            break;
                        case "-getbmb":
                            BBM.ReadBitMap();
                            break;
                        case "-getbmi":
                            IBM.ReadBitMap();
                            break;
                        case "-getilist":
                            IL.ReadIList();
                            break;
                        case "-tobit":
                            try
                            {
                                byte byte_number = Byte.parseByte(subStr[1]);
                                ConvertToBitView(byte_number);
                            } catch (Exception ex)
                            {
                                System.out.println("Parameter must be byte type.");
                            }
                            break;
                        default:
                            System.out.println("Type \"-help\" if you don't know what to do.");
                            break;
                    }
                } else
                {
                    System.out.println("Type \"-help\" if you don't know what to do.");
                }
            }
        }
    }

    private void ConvertToBitView(Byte number)
    {
        System.out.println(String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(number))).replace(' ', '0'));
    }

}

class SuperBlock
{

    private short SZ_SuperBlock;                 // Размер кластера
    private short SZ_SizeBitMap;                 // Размер битовой карты блоков
    private short SZ_SizeBitMapInodes;             // Размер битовой карты inode
    private short SZ_InodesCount;                 // Количество инодов
    private short SZ_FreeInodesCount;             // Количество свободных inode
    private short SZ_FreeBlocksCount;           // Количество свободных блоков
    private short MV_FOMapBlocks;                // Бкс\з блоков
    private short MV_FOMapInodes;                // Бкс\з inode
    private short MV_IListMass;                  // Массив индексных дескрипторов
    private int MV_RootDirectory;               // Корневой каталог
    private int MV_Data;
    private String Filename;

    short getSZ_SuperBlock()
    {
        return SZ_SuperBlock;
    }

    short getSZ_SizeBitMap()
    {
        return SZ_SizeBitMap;
    }

    short getSZ_SizeBitMapInodes()
    {
        return SZ_SizeBitMapInodes;
    }

    short getSZ_InodesCount()
    {
        return SZ_InodesCount;
    }

    short getSZ_FreeInodesCount()
    {
        return SZ_FreeInodesCount;
    }

    short getSZ_FreeBlocksCount()
    {
        return SZ_FreeBlocksCount;
    }

    short getMV_FOMapBlocks()
    {
        return MV_FOMapBlocks;
    }

    short getMV_FOMapInodes()
    {
        return MV_FOMapInodes;
    }

    short getMV_IListMass()
    {
        return MV_IListMass;
    }

    int getMV_RootDirectory()
    {
        return MV_RootDirectory;
    }

    int getMV_Data()
    {
        return MV_Data;
    }

    void SetFilename(String Filename_received)
    {
        Filename = Filename_received;
    }

    SuperBlock()
    {
        SetSB();
    }

    private void SetSB()                        // Установить стандартные значение Супер блока
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

    void ReadSB()                               // Считать весь Супер блок
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "r"))
        {
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
            System.out.printf(" Размер супер блока: %s%n Размер бит. карты блоков: %s%n Размер бит. карты инодов: %s%n" +
                            " Количество инодов: %s%n Количество свободных инодов: %s%n Количество свободных блоков: %s%n" +
                            " Сдвиг бит. карты блоков: %s%n Сдвиг бит. карты инодов: %s%n Сдвиг списка инодов: %s%n" +
                            " Сдвиг корневого каталога: %s%n Сдвиг раздела данных: %s%n", SZ_SuperBlock, SZ_SizeBitMap,
                    SZ_SizeBitMapInodes, SZ_InodesCount, SZ_FreeInodesCount, SZ_FreeBlocksCount, MV_FOMapBlocks,
                    MV_FOMapInodes, MV_IListMass, MV_RootDirectory, MV_Data);
        } catch (IOException ex)
        {
            System.out.println("");
        }
    }

    void WriteSB()                      // Записать Супер блок
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
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
        } catch (IOException ex)
        {
            System.out.println("");
        }
    }

}

class BlocksBitMap
{

    private CursorMovement CM = new CursorMovement();
    private String Filename;

    void SetFilename(String Filename_received)
    {
        Filename = Filename_received;
    }

    int FindFreeBlock(RandomAccessFile file)                // Найти свободный блок
    {
        try
        {
            file.seek(0);
            long end_cursor = CM.MoveToPartition(file, 0, 2);
            file.seek(0);
            long cursor = CM.MoveToPartition(file, 0, 1);
            file.seek(cursor);
            while (cursor < end_cursor)
            {
                byte block = file.readByte();
                String temp = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(block))).replace(' ', '0');
                for (int i = 0; i < temp.length(); i++)
                {
                    if (temp.charAt(i) == '0')
                    {
                        System.out.println("Свободный бит");
                        long cursor_block_long = file.getFilePointer() - 1;
                        return (int) cursor_block_long;
                    } else
                    {
                        System.out.println("Занятый бит");
                    }
                }
                cursor = file.getFilePointer();
            }
            return -1;
        } catch (IOException ex)
        {
            System.out.println(ex.toString());
            return -1;
        }
    }

    void WriteBitMap()              // Записать битовую карту с\з кластеров
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            long cursor = CM.MoveToPartition(file, 0, 1);
            file.seek(cursor);
            byte start = -8; // 11111000
            byte defolt = 0;
            file.writeByte(start);
            for (int i = 0; i < 4095; i++)
            {
                file.writeByte(defolt);
            }
        } catch (IOException ex)
        {
            System.out.println("");
        }
    }

    void ReadBitMap()              // Записать битовую карту с\з кластеров
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            System.out.println("Битовая карта свободно-занятых блоков");
            long cursor = CM.MoveToPartition(file, 0, 1);
            file.seek(cursor);
            for (int i = 0; i < 4096; i++)
            {
                String temp = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(file.readByte()))).replace(' ', '0');
                System.out.printf("Байт [%d]: %s%n", i + 1, temp);
            }
        } catch (IOException ex)
        {
            System.out.println("");
        }
    }

}

class InodesBitMap
{

    private CursorMovement CM = new CursorMovement();
    private String Filename;

    void SetFilename(String Filename_received)
    {
        Filename = Filename_received;
    }

    int FindFreeInode(RandomAccessFile file)
    {
        try
        {
            file.seek(0);
            long end_cursor = CM.MoveToPartition(file, 0, 3);
            file.seek(0);
            long cursor = CM.MoveToPartition(file, 0, 2);
            file.seek(cursor);
            while (cursor < end_cursor)
            {
                //System.out.println("Начальный курсор = " + cursor + " | Конечный курсор = " + end_cursor);
                byte inode = file.readByte();
                String temp = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(inode))).replace(' ', '0');
                System.out.println(temp);
                for (int i = 0; i < temp.length(); i++)
                {
                    if (temp.charAt(i) == '0')
                    {
                        System.out.println("Свободный бит");
                        long cursor_inode_long = file.getFilePointer() - 1; // Минус один для того, чтобы каретка вернулась на найденный пустой байт
                        return (int) cursor_inode_long;
                    } else
                    {
                        System.out.println("Занятый бит");
                    }
                }
                cursor = file.getFilePointer();
            }
            return -1;
        } catch (IOException ex)
        {
            System.out.println(ex.toString());
            return -1;
        }
    }

    void WriteBitMap()
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            long cursor = CM.MoveToPartition(file, 0, 2);
            file.seek(cursor);
            byte defolt = 0;
            for (int i = 0; i < 4095; i++)
            {
                file.writeByte(defolt);
            }
        } catch (IOException ex)
        {
            System.out.println("");
        }
    }

    void ReadBitMap()
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            System.out.println("Битовая карта свободно-занятых инодов");
            long cursor = CM.MoveToPartition(file, 0, 2);
            file.seek(cursor);
            for (int i = 0; i < 4096; i++)
            {
                String temp = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(file.readByte()))).replace(' ', '0');
                System.out.printf("Байт [%d]: %s%n", i + 1, temp);
            }
        } catch (IOException ex)
        {
            System.out.println("");
        }
    }

}

class InodesList
{

    private SuperBlock SB = new SuperBlock();
    private CursorMovement CM = new CursorMovement();
    private String Filename;

    void SetFilename(String Filename_received)
    {
        Filename = Filename_received;
    }

    void WriteIList()
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            long cursor = CM.MoveToPartition(file, 0, 3);
            file.seek(cursor);
            byte defolt = 0;
            for (int i = 0; i < 38912; i++)
            {
                file.writeByte(defolt);
            }
        } catch (IOException ex)
        {
            System.out.println("");
        }
    }

    void ReadIList()
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            long cursor = CM.MoveToPartition(file, 0, 3);
            file.seek(cursor);
            System.out.println("Список инодов");
            for (int i = 0; i < 512; i++)
            {
                System.out.printf("%nИнод [%d]%n", i + 1);
                char file_type = (char) file.readByte();
                if (file_type == '\0') file_type = '-';
                byte file_rights_ug = file.readByte();
                byte file_rights_of = file.readByte();
                short link_count = file.readShort();
                short user_id = file.readShort();
                short group_id = file.readShort();
                int file_size = file.readInt();
                byte created_date = file.readByte();
                byte created_month = file.readByte();
                short created_year = file.readShort();
                byte changed_date = file.readByte();
                byte changed_month = file.readByte();
                short changed_year = file.readShort();
                int[] di_address = new int[12];
                for (int j = 0; j < di_address.length; j++)
                {
                    di_address[j] = file.readInt();
                }
                int adressing = file.readInt();

                System.out.println("\nТип файла: " + file_type + "\nПрава доступа: " + String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(file_rights_ug))).replace(' ', '0') + "\nПрава доступа: " + String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(file_rights_of))).replace(' ', '0') + "\nКоличество ссылок: " + link_count + "\nПользователь ID:" + user_id + "\nГруппа ID: " +
                        group_id + "\nРазмер файла: " + file_size + "\nДата создания: " + created_date + "." + created_month + "." + created_year + "\nДата изменения: " +
                        changed_date + "." + changed_month + "." + changed_year + "\nМассив ссылок на данные: " + Arrays.toString(di_address) + "\nАдресация: " + adressing);

            }
        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
    }

    int FindFreeInode(RandomAccessFile file)
    {
        try
        {
            file.seek(0);
            long end_cursor = CM.MoveToPartition(file, 0, 3);
            file.seek(0);
            long cursor = CM.MoveToPartition(file, 0, 2);
            file.seek(cursor);
            while (cursor < end_cursor)
            {
                //System.out.println("Начальный курсор = " + cursor + " | Конечный курсор = " + end_cursor);
                byte inode = file.readByte();
                String temp = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(inode))).replace(' ', '0');
                System.out.println(temp);
                for (int i = 0; i < temp.length(); i++)
                {
                    if (temp.charAt(i) == '0')
                    {
                        System.out.println("Свободный бит");
                        int cursor_inode_long = (int) file.getFilePointer() - 1; // Минус один для того, чтобы каретка вернулась на найденный пустой байт
                        cursor_inode_long = (cursor_inode_long - SB.getMV_FOMapInodes() + i) * 73;
                        return cursor_inode_long;
                    } else
                    {
                        System.out.println("Занятый бит");
                    }
                }
                cursor = file.getFilePointer();
            }
        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
        return -1;
    }

}

class RootCatalog
{
    private SuperBlock SB = new SuperBlock();
    private BitWork BW = new BitWork();
    private BlocksBitMap BMB = new BlocksBitMap();
    private InodesBitMap IBM = new InodesBitMap();
    private GregorianCalendar GCalendar = new GregorianCalendar();
    private CursorMovement CM = new CursorMovement();
    private InodesList IL = new InodesList();
    private String Filename;

    void SetFilename(String Filename_received)
    {
        Filename = Filename_received;
    }

    void ReadRootCatalog()
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            long cursor = CM.MoveToPartition(file, 0, 3);
            file.seek(cursor);
            System.out.println(file.getFilePointer());
            byte file_type = file.readByte();
            byte file_rights_ug = file.readByte();
            byte file_rights_of = file.readByte();
            short link_count = file.readShort();
            short user_id = file.readShort();
            short group_id = file.readShort();
            int file_size = file.readInt();
            byte created_date = file.readByte();
            byte created_month = file.readByte();
            short created_year = file.readShort();
            byte changed_date = file.readByte();
            byte changed_month = file.readByte();
            short changed_year = file.readShort();
            int[] di_adress = new int[12];
            for (int i = 0; i < di_adress.length; i++)
            {
                di_adress[i] = file.readInt();
            }
            int adressing = file.readInt();

            System.out.println("\nТип файла: " + file_type + "\nПрава доступа: " + String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(file_rights_ug))).replace(' ', '0') + "\nПрава доступа: " +
                    String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(file_rights_of))).replace(' ', '0') + "\nКоличество ссылок: " + link_count + "\nПользователь ID: " + user_id + "\nГруппа ID: " +
                    group_id + "\nРазмер файла: " + file_size + "\nДата создания: " + created_date + "." + created_month + "." + created_year + "\nДата изменения: " +
                    changed_date + "." + changed_month + "." + changed_year + "\nМассив ссылок на данные: " + Arrays.toString(di_adress) + "\nАдресация: " + adressing);
        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
    }

    void WriteRootCatalog()
    {
        WriteDirectory("home");
        WriteDirectory("system");
    }

    void WriteDirectory(String file_name)
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            int cursor_free_inode = IL.FindFreeInode(file);    // Возвращает указатель на блок с пустым инодом
            file.seek(0);
            CM.MoveToPartition(file, 0, 3);
            CM.MoveToPartition(file, cursor_free_inode, 0);

            short inode_number = (short) file.getFilePointer();                                  // Сохранение указателя на инод
            byte file_type = 'd';                                                                // Тип файла
            byte file_rights_UG = (byte) -32;                                                    // 11100000
            byte file_rights_OS = (byte) -240;                                                   // 00010000
            short link_count = 1;                                                                // Количество ссылок
            short user_id = 0;                                                                   // ID пользователя
            short group_id = 0;                                                                  // ID группы
            int file_size = 2048;                                                                // Размер файла
            byte created_date = (byte) GCalendar.get(Calendar.DATE);                             // Число
            byte created_month = (byte) (GCalendar.get(Calendar.MONTH) + 1);                     // Месяц
            short created_year = (short) GCalendar.get(Calendar.YEAR);                           // Год
            int[] di_adress = new int[]{0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};          // Адреса на кластеры в data
            int adressing = 0;                                                                   // Косвенная адресация

            file.writeByte(file_type);                              // 1
            file.writeByte(file_rights_UG);                         // 1
            file.writeByte(file_rights_OS);                         // 1
            file.writeShort(link_count);                            // 2
            file.writeShort(user_id);                               // 2
            file.writeShort(group_id);                              // 2
            file.writeInt(file_size);                               // 4
            file.writeByte(created_date);                           // 1
            file.writeByte(created_month);                          // 1
            file.writeShort(created_year);                          // 2
            file.writeByte(created_date);                           // 1
            file.writeByte(created_month);                          // 1
            file.writeShort(created_year);                          // 2
            for (int diAdress : di_adress) file.writeInt(diAdress); // 12 * 4 = 48
            file.writeInt(adressing);                               // 4

            file.seek(0);
            CM.MoveToPartition(file, 0, 4);

            String name = (String.format("%-50s", file_name)).replace(' ', '\0');
            //System.out.println("|" + name + "|");
            file.writeBytes(name);
            //System.out.println(Arrays.toString(name.getBytes()));
            String extension = "\0\0\0\0\0";
            //System.out.println(Arrays.toString(extension.getBytes()));
            file.writeBytes(extension);
            file.writeShort(inode_number);
            file.close();

            BW.TakeNewBlock(Filename);
            BW.TakeNewInode(Filename);
        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
    }

}

class Data
{

    private String Filename;

    void SetFilename(String Filename_received)
    {
        Filename = Filename_received;
    }

}

class CursorMovement
{

    long MoveToPartition(RandomAccessFile file, int bytes, int moveToPartition) throws IOException // Сдвинуть каретку в файле
    {
        SuperBlock SB = new SuperBlock();
        long cursorPoint = -1;
        switch (moveToPartition)
        {
            case 0:
                file.skipBytes(bytes);              // На определнное количество байт
                cursorPoint = file.getFilePointer();
                break;
            case 1:
                file.skipBytes(SB.getMV_FOMapBlocks());     // На раздел битовой карты с/з блоков
                cursorPoint = file.getFilePointer();
                break;
            case 2:
                file.skipBytes(SB.getMV_FOMapInodes());     // На раздел битовой карты с/з inodes
                cursorPoint = file.getFilePointer();
                break;
            case 3:
                file.skipBytes(SB.getMV_IListMass());       // На раздел массива индексных дескрипторов
                cursorPoint = file.getFilePointer();
                break;
            case 4:
                file.skipBytes(SB.getMV_RootDirectory());   // На раздел корневого каталога
                cursorPoint = file.getFilePointer();
                break;
            case 5:
                file.skipBytes(SB.getMV_Data());            // На раздел данных
                cursorPoint = file.getFilePointer();
                break;
            default:
                System.out.println("\n\n--- --- --- Ошибка при переходе в раздел диска! --- --- ---\n\n");
        }
        return cursorPoint;
    }

}

class BitWork
{

    private InodesBitMap IBM = new InodesBitMap();
    private BlocksBitMap BMB = new BlocksBitMap();
    private CursorMovement CM = new CursorMovement();
    private String Filename;

    void SetFilename(String Filename_received)
    {
        Filename = Filename_received;
    }

    int GetPositionOfZeroByte(long filePointer, RandomAccessFile file)
    {
        try
        {
            file.seek(filePointer);
            byte byte_view = file.readByte();
            String bit_view = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(byte_view))).replace(' ', '0');
            for (int i = 0; i < bit_view.length(); i++)
            {
                if (bit_view.charAt(i) == '0')
                {
                    return ++i;
                }
            }
            return -10;
        } catch (IOException ex)
        {
            return -10;
        }
    }

    void TakeNewInode(String Filename)
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            int cursor_free_inode = IBM.FindFreeInode(file);
            file.seek(cursor_free_inode);
            byte my_byte = file.readByte();
            file.seek(cursor_free_inode);
            int position = GetPositionOfZeroByte(file.getFilePointer(), file);
            String byte_string = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(my_byte))).replace(' ', '0');
            byte final_byte = SetBitToOne(byte_string, position);
            file.seek(cursor_free_inode);
            file.writeByte(final_byte);
        } catch (IOException ex)
        {
            System.out.println();
        }
    }

    void TakeNewBlock(String Filename)
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            int cursor_free_block = BMB.FindFreeBlock(file);
            file.seek(cursor_free_block);
            byte my_byte = file.readByte();
            file.seek(cursor_free_block);
            int position = GetPositionOfZeroByte(cursor_free_block, file);
            String byte_string = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(my_byte))).replace(' ', '0');
            byte final_byte = SetBitToOne(byte_string, position);
            file.seek(cursor_free_block);
            file.writeByte(final_byte);
        } catch (IOException ex)
        {
            System.out.println();
        }
    }

    byte SetBitToOne(String my_byte, int pos)
    {
        pos = 8 - pos;
        BitSet bits1 = fromString(my_byte);
        bits1.set(pos, true);
        return bits1.toByteArray()[0];
    }

    byte SetBitToZero(String my_byte, int pos)
    {
        pos = 8 - pos;
        BitSet bits1 = fromString(my_byte);
        bits1.set(pos, false);
        return bits1.toByteArray()[0];
    }

    private BitSet fromString(final String s)
    {
        return BitSet.valueOf(new long[]{Long.parseLong(s, 2)});
    }

    private String toString(BitSet bs)
    {
        return Long.toString(bs.toLongArray()[0], 2);
    }

}

class FileWork
{

    private CursorMovement CM = new CursorMovement();
    private String Filename;

    void SetFilename(String Filename_received)
    {
        Filename = Filename_received;
    }

    private void CreateUsersFile() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException
    {
    }

    /*private void WriteRootCatalog(String file_name, int inodes_bytes_to_skip, int blocks_bytes_to_skip)
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            long cursor = CM.MoveToPartition(file, 0, 3);                      // Сдвиг курсора к битовой карте с\з инодов
            file.seek(cursor);
            file.skipBytes(inodes_bytes_to_skip);                                                // Пропуск в байтах уже занятых инодов

            // Данные инода к записи

            short inode_number = (short) file.getFilePointer();                                  // Сохранение указателя на инод
            byte file_type = 'd';                                                                // Тип файла
            byte file_rights_UG = (byte) -32; // 11111100
            byte file_rights_OS = (byte) -240; // 00010000
            short link_count = 1;                                                                // Количество ссылок
            short user_id = 1;                                                                   // ID пользователя
            short group_id = 0;                                                                  // ID группы
            int file_size = 2048;                                                                    // Размер файла
            byte created_date = (byte) GCalendar.get(Calendar.DATE);                             // Число
            byte created_month = (byte) (GCalendar.get(Calendar.MONTH) + 1);                     // Месяц
            short created_year = (short) GCalendar.get(Calendar.YEAR);                           // Год
            int[] di_adress = new int[]{0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};          // Адреса на кластеры в data
            int adressing = 0;                                                                   // Косвенная адресация

            // Запись инода

            file.writeByte(file_type);                              // 1
            file.writeByte(file_rights_UG);                         // 1
            file.writeByte(file_rights_OS);                         // 1
            file.writeShort(link_count);                            // 2
            file.writeShort(user_id);                               // 2
            file.writeShort(group_id);                              // 2
            file.writeInt(file_size);                               // 4
            file.writeByte(created_date);                           // 1
            file.writeByte(created_month);                          // 1
            file.writeShort(created_year);                          // 2
            file.writeByte(created_date);                           // 1
            file.writeByte(created_month);                          // 1
            file.writeShort(created_year);                          // 2
            for (int diAdress : di_adress) file.writeInt(diAdress); // 12 * 4 = 48
            file.writeInt(adressing);                               // 4
            file.close();

            // Переход к корневому каталогу

            file.seek(0);
            cursor = CM.MoveToPartition(file, 0, 4);
            file.seek(cursor);

            // Данные корневого каталога к записи и запись
            String[] names = new String[]{"home", "system"};
            for (int i = 0; i < names.length; i++)
            {
                String temp = String.format("%50s", names[i]);
                System.out.println("|" + temp + "|");
                byte[] name = new byte[50];
                name = temp.getBytes();
                System.out.println(name.length);
            }
            byte[] extension = new byte[5];
            for (int i = 0; i < extension.length; i++)
            {
                file.writeByte(extension[i]);
            }
            file.writeShort(inode_number);
            file.close();

            // Запись занятого блока в битовую карту

            file.seek(0);
            cursor = CM.MoveToPartition(file, 0, 1);
            file.seek(cursor);
            file.skipBytes(blocks_bytes_to_skip);
            byte my_byte = file.readByte();
            String byte_string = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(my_byte))).replace(' ', '0');
            int position = BW.GetPositionOfByte(file.getFilePointer());
            byte final_byte = BW.SetBitToOne(byte_string, position);
            file.seek(cursor);
            file.writeByte(final_byte);
        } catch (IOException ex)
        {
            System.out.println("");
        }
    }*/

}