package com.crosska;

import java.io.IOException;
import java.io.RandomAccessFile;

public class CursorMovement
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
                file.seek(0);
                file.skipBytes(SB.getMV_FOMapBlocks());     // На раздел битовой карты с/з блоков
                cursorPoint = file.getFilePointer();
                break;
            case 2:
                file.seek(0);
                file.skipBytes(SB.getMV_FOMapInodes());     // На раздел битовой карты с/з inodes
                cursorPoint = file.getFilePointer();
                break;
            case 3:
                file.seek(0);
                file.skipBytes(SB.getMV_IListMass());       // На раздел массива индексных дескрипторов
                cursorPoint = file.getFilePointer();
                break;
            case 4:
                file.seek(0);
                file.skipBytes(SB.getMV_RootDirectory());   // На раздел корневого каталога
                cursorPoint = file.getFilePointer();
                break;
            case 5:
                file.seek(0);
                file.skipBytes(SB.getMV_Data());            // На раздел данных
                cursorPoint = file.getFilePointer();
                break;
            default:
                System.out.println("\n\n--- --- --- Ошибка при переходе в раздел диска! --- --- ---\n\n");
        }
        return cursorPoint;
    }

}