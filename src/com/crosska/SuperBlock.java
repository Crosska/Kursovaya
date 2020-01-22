package com.crosska;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SuperBlock
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

    void SetSB()                        // Установить стандартные значение Супер блока
    {
        SZ_SuperBlock = 2048;        // Размер кластера
        SZ_SizeBitMap = 3200;        // Размер битовой карты блоков
        SZ_SizeBitMapInodes = 64;    // Размер битовой карты inode
        SZ_InodesCount = 512;        // Количество инодов
        SZ_FreeInodesCount = 512;    // Количество свободных inode
        SZ_FreeBlocksCount = 25575;  // Количество свободных блоков
        MV_FOMapBlocks = 2049;       // Бкс\з блоков
        MV_FOMapInodes = 6145;       // Бкс\з inode
        MV_IListMass = 10241;         // Массив индексных дескрипторов
        MV_RootDirectory = 47105;    // Корневой каталог
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
