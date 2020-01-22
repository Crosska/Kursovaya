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
            /*file.seek(inodes[0]);
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
            */

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
                    System.out.println("Попал");
                    skip_count++;
                }
            }
            return skip_count;
        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
        return -1;
    }
}
