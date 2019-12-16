package com.crosska;

import java.io.*;

public class Main
{

    public static void main(String[] Args) throws IOException
    {
        //RandomAccessFile file = new RandomAccessFile(new File("ss.dat"), "rw");
        /*for (int i = 0; i < 52428800; i++)
        {
            file.write('\0');
        }*/
        //file.close();

        RandomAccessFile file = new RandomAccessFile(new File("ss.dat"), "rw");
        short cluster = 2048;
        file.writeShort(cluster);
        file.close();

        file = new RandomAccessFile(new File("ss.dat"), "rw");
        short temp = file.readShort();
        file.close();
        System.out.println(temp);

    }

}

class FileData
{

    // Структура суперблока
    final private short SuperBlock = 2048;        // Размер кластера
    final private short SizeBitMap = 3200;        // Размер битовой карты блоков
    final private short SizeBitMapInodes = 64;    // Размер битовой карты inode
    final private short InodesCount = 512;        // Количество инодов
    final private short FreeInodesCount = 512;    // Количество свободных inode
    final private short FreeBlocksCount = 25576;  // Количество свободных блоков
    // Смещения
    final private short FOMapBlocks = 2049;       // Бкс\з блоков
    final private short FOMapInodes = 6145;       // Бкс\з inode
    final private short IListMass = 8193;         // Массив индексных дескрипторов
    final private int RootDirectory = 47105;      // Корневой каталог
    final private int Data = 49153;               // Данные

}