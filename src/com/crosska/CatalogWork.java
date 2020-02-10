package com.crosska;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.GregorianCalendar;

public class CatalogWork
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
            long cursor = CM.MoveToPartition(file, 0, 4);
            file.seek(cursor);

            for (int j = 0; j < 30; j++)
            {
                byte[] temp = new byte[25];
                for (int i = 0; i < 25; i++)
                {
                    temp[i] = file.readByte();
                }
                String s = new String(temp, StandardCharsets.US_ASCII);
                System.out.print("\n\nИмя файла: " + s);

                temp = new byte[5];
                for (int i = 0; i < 5; i++)
                {
                    temp[i] = file.readByte();
                }
                s = new String(temp, StandardCharsets.US_ASCII);
                System.out.print("\nРасширение: " + s);

                //System.out.printf("\n_%d_\n",file.getFilePointer());
                short inode_num = file.readShort();

                System.out.print("\nНомер инода: " + inode_num);
                //System.out.printf("\n__%d__\n",file.getFilePointer());
                file.skipBytes(30);
            }

        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
    }

    void ShowFiles(String user_global)
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            String[] filenames = new String[30];
            String[] extensions = new String[30];
            Short[] inodes = new Short[30];
            long cursor = CM.MoveToPartition(file, 0, 4);
            file.seek(cursor);

            for (int j = 0; j < 30; j++)
            {
                byte[] temp = new byte[25];
                for (int i = 0; i < 25; i++)
                {
                    temp[i] = file.readByte();
                }
                String s = new String(temp, StandardCharsets.US_ASCII);
                filenames[j] = s;

                temp = new byte[5];
                for (int i = 0; i < 5; i++)
                {
                    temp[i] = file.readByte();
                }
                s = new String(temp, StandardCharsets.US_ASCII);
                extensions[j] = s;

                short inode_num = file.readShort();
                inodes[j] = inode_num;

                file.skipBytes(30);
            }
            System.out.print("\nRoot catalog consist of:\n|_________Filename________|__Ext__|");
            for (int i = 0; i < filenames.length; i++)
            {
                System.out.printf("\n|%15s|.%6s|", filenames[i], extensions[i]);
            }
            System.out.println("\n|-------------------------|-------|");
        } catch (
                IOException ex)

        {
            System.out.println(ex.toString());
        }
    }

    int FindFreeRootCatalogRecord(RandomAccessFile file)
    {
        try
        {
            int skip_count = 0;
            long cursor_cycle = CM.MoveToPartition(file, 0, 4);
            for (int j = 0; j < 30; j++)
            {
                byte[] temp = new byte[25];
                for (int i = 0; i < 25; i++)
                {
                    temp[i] = file.readByte();
                }

                temp = new byte[5];
                for (int i = 0; i < 5; i++)
                {
                    temp[i] = file.readByte();
                }

                short inode_num = file.readShort();
                if (inode_num > 10240 && inode_num < 32767)
                {
                    skip_count++;
                }
                file.skipBytes(30);
            }
            return skip_count;
        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
        return -1;
    }
}
