package com.crosska;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BlocksBitMap
{

    private CursorMovement CM = new CursorMovement();
    private SuperBlock SB = new SuperBlock();
    private String Filename;

    void SetFilename(String Filename_received)
    {
        Filename = Filename_received;
    }

    int FindFreeBlock(RandomAccessFile file)                // Найти свободный блок
    {
        try
        {
            long end_cursor = CM.MoveToPartition(file, 0, 2);
            long cursor = CM.MoveToPartition(file, 0, 1);
            cursor = cursor + 3;
            file.seek(cursor);
            while (cursor < end_cursor)
            {
                byte block = file.readByte();
                String temp = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(block))).replace(' ', '0');
                for (int i = 0; i < temp.length(); i++)
                {
                    if (temp.charAt(i) == '0')
                    {
                        //System.out.println("Свободный бит");
                        long cursor_block_long = file.getFilePointer() - 1;
                        //System.out.println("Курсор куда писать: " + cursor_block_long);
                        return (int) cursor_block_long;
                    } else
                    {
                        //System.out.println("Занятый бит");
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

    int FindFreeBlockShift(RandomAccessFile file)                // Найти свободный блок со сдвигом по 2048
    {
        try
        {
            long end_cursor = CM.MoveToPartition(file, 0, 2);
            long cursor = CM.MoveToPartition(file, 0, 1);
            cursor = cursor + 3;
            file.seek(cursor);
            while (cursor < end_cursor)
            {
                byte block = file.readByte();
                String temp = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(block))).replace(' ', '0');
                for (int i = 0; i < temp.length(); i++)
                {
                    if (temp.charAt(i) == '0')
                    {
                        //System.out.println("Свободный бит");
                        long cursor_block_long = file.getFilePointer() - 1;
                        //System.out.println("Курсор на байт с нулем: " + cursor_block_long);
                        cursor_block_long = ((cursor_block_long - SB.getMV_FOMapBlocks()) * 8 + i) * 2048;
                        //System.out.println("Курсор куда писать: " + cursor_block_long);
                        return (int) cursor_block_long;
                    } else
                    {
                        //System.out.println("Занятый бит");
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
            byte start = -1; // 11111111
            byte defolt = 0;
            file.writeByte(start);
            file.writeByte(start);
            file.writeByte(start);
            for (int i = 0; i < 4093; i++)
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
