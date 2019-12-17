package com.crosska;

import java.io.*;

public class Main
{

    public static void main(String[] Args) throws IOException
    {
        OS DevOS = new OS();
        DevOS.CreateNewOS("System.dat");
    }

}

class OS
{
    // Название файла с системой
    private String Filename;
    // Структура суперблока
    private short SZ_SuperBlock = 2048;        // Размер кластера
    private short SZ_SizeBitMap = 3200;        // Размер битовой карты блоков
    private short SZ_SizeBitMapInodes = 64;    // Размер битовой карты inode
    private short SZ_InodesCount = 512;        // Количество инодов
    private short SZ_FreeInodesCount = 512;    // Количество свободных inode
    private short SZ_FreeBlocksCount = 25575;  // Количество свободных блоков
    // Смещения по разделам
    private short MV_FOMapBlocks = 2049;       // Бкс\з блоков
    private short MV_FOMapInodes = 6145;       // Бкс\з inode
    private short MV_IListMass = 8193;         // Массив индексных дескрипторов
    private int MV_RootDirectory = 47105;      // Корневой каталог
    private int MV_Data = 49153;               // Данные
    // Битовая карта с\з кластеров
    private byte[] BM = new byte[4096];


    void CreateNewOS(String OSName) throws IOException  // Начальная функция создания ОС
    {
        Filename = OSName;
        WriteSB();
        WriteBM();
    }


    private void GetSBValues()                  // Вывести данные о Супер блоке
    {
        System.out.println(SZ_SuperBlock + "\n" + SZ_SizeBitMap + "\n" + SZ_SizeBitMapInodes + "\n" +
                SZ_InodesCount + "\n" + SZ_FreeInodesCount + "\n" + SZ_FreeBlocksCount + "\n" + MV_FOMapBlocks +
                "\n" + MV_FOMapInodes + "\n" + MV_IListMass + "\n" + MV_RootDirectory + "\n" + MV_Data);
    }

    private void ReadSB() throws IOException    // Считать весь Супер блок
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

    void SetDefaultSB()                         // Установить стандартные значение Супер блока
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

    private void WriteSB() throws IOException   // Записать Супер блок
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

    private void WriteBM() throws IOException   // Записать битовую картку с\з кластеров
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw");
        long cursor = MoveToPartition(file, 0, 1);
        file.seek(cursor);
        byte start = -128;
        byte defolt = 0;
        file.writeByte(start);
        for (int i = 0; i < 4095; i++)
        {
            file.writeByte(defolt);
        }
        file.close();
    }

    private void ReadAllBM() throws IOException // Считать всю битовую карту с\з кластеров
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "r");
        long cursor = MoveToPartition(file, 0, 1);
        file.seek(cursor);
        for (int i = 0; i < 4096; i++)
        {
            BM[i] = file.readByte();
        }
        file.close();
    }

    private void ReadElementBM() throws IOException // Считать один байт из битовой карты с\з кластеров
    {
        RandomAccessFile file = new RandomAccessFile(new File(Filename), "r");
        long cursor = MoveToPartition(file, 0, 1);
        file.seek(cursor);
        cursor = MoveToPartition(file, 1, 0);
        file.seek(cursor);
        byte readed = file.readByte();
        System.out.println(String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(readed))).replace(' ', '0'));
        file.close();
    }

}