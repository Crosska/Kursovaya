package com.crosska;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class InodesList
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
                System.out.println(file.getFilePointer());
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
            long end_cursor = CM.MoveToPartition(file, 0, 3);
            long cursor = CM.MoveToPartition(file, 0, 2);
            file.seek(cursor);
            while (cursor < end_cursor)
            {
                byte inode = file.readByte();
                String temp = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(inode))).replace(' ', '0');
                for (int i = 0; i < temp.length(); i++)
                {
                    if (temp.charAt(i) == '0')
                    {
                        int cursor_inode_long = (int) file.getFilePointer() - 1; // Минус один для того, чтобы каретка вернулась на найденный пустой байт
                        cursor_inode_long = ((cursor_inode_long - SB.getMV_FOMapInodes()) * 8 + i) * 73;
                        return cursor_inode_long;
                    }
                }
                cursor = file.getFilePointer();
            }
        } catch (IOException ex)
        {
        }
        return -1;
    }

}
