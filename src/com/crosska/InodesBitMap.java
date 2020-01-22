package com.crosska;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class InodesBitMap
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
            long end_cursor = CM.MoveToPartition(file, 0, 3);
            long cursor = CM.MoveToPartition(file, 0, 2);
            file.seek(cursor);
            while (cursor < end_cursor)
            {
                byte inode = file.readByte();
                String temp = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(inode))).replace(' ', '0');
                //System.out.println(temp);
                for (int i = 0; i < temp.length(); i++)
                {
                    if (temp.charAt(i) == '0')
                    {
                        long cursor_inode_long = file.getFilePointer() - 1;
                        return (int) cursor_inode_long;
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
