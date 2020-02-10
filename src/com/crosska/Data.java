package com.crosska;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class Data
{

    private SuperBlock SB = new SuperBlock();
    private String Filename;
    private BitWork BW = new BitWork();
    private InodesList IL = new InodesList();
    private CursorMovement CM = new CursorMovement();
    private CatalogWork CW = new CatalogWork();
    private BlocksBitMap BlockBM = new BlocksBitMap();
    private Crypter CRYPT = new Crypter();

    void SetFilename(String Filename_received)
    {
        Filename = Filename_received;
    }

    int WriteUsersDataToCluster(Short user_id, Short group_id, String login, String password, String catalog, RandomAccessFile file) throws IOException
    {
        CM.MoveToPartition(file, 49153, 0);
        password = CRYPT.md5Custom(password);
        int cursor_on_cluster = (int) file.getFilePointer();
        login = (String.format("%-15s", login));
        catalog = (String.format("%-15s", catalog));
        file.writeShort(user_id);
        file.writeShort(group_id);
        file.writeBytes(login);
        file.writeBytes(password);
        file.writeBytes(catalog);
        return cursor_on_cluster;
    }

    int WriteGroupDataToCluster(StringBuilder content, RandomAccessFile file) throws IOException
    {
        CM.MoveToPartition(file, 51201, 0);
        String data_to_write = new String(content);
        int cursor_on_cluster = (int) file.getFilePointer();
        //System.out.println(data_to_write);
        file.writeBytes(data_to_write);
        return cursor_on_cluster;
    }

    void AddNewUser(Short user_id, Short group_id, String login, String password, String catalog, int user_count)
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            int bytes_to_skip = 128 * user_count;
            CM.MoveToPartition(file, 0, 5);
            CM.MoveToPartition(file, bytes_to_skip, 0);
            login = (String.format("%-15s", login));
            catalog = (String.format("%-15s", catalog));
            file.writeShort(user_id);
            file.writeShort(group_id);
            file.writeBytes(login);
            file.writeBytes(CRYPT.md5Custom(password));
            file.writeBytes(catalog);

            file.seek(51201);
            int end = (user_count - 1) * 8;
            file.skipBytes(end);
            long point = file.getFilePointer();
            point -= 4;
            file.seek(point);
            file.writeBytes(user_id + "=" + group_id + "|");
        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
    }

    int WriteDataToCluster(StringBuilder content, RandomAccessFile file) throws IOException
    {
        int cursor = BlockBM.FindFreeBlockShift(file);
        file.seek(cursor);
        String data_to_write = new String(content);
        file.writeBytes(data_to_write);
        return cursor;
    }

    void WriteDataToClusterEdited(StringBuilder content, RandomAccessFile file) throws IOException
    {
        String data_to_write = new String(content);
        file.writeBytes(data_to_write);
    }

    void ReadFile(String filename, short UID_Current, short GID_Current)
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

            int id = -1;
            for (int i = 0; i < filenames.length; i++)
            {
                String[] subStr = filenames[i].split(" ");
                if (filename.equals(subStr[0]))
                {
                    id = i;
                    i = filenames.length;
                }
            }

            if (id >= 0)
            {


                file.seek(inodes[id]);
                char file_type = (char) file.readByte();
                if (file_type == '\0') file_type = '-';
                file.readByte();
                file.readByte();
                file.readShort();
                short UID_Creator = file.readShort();
                short GID_Creator = file.readShort();
                file.readInt();
                file.readByte();
                file.readByte();
                file.readShort();
                file.readByte();
                file.readByte();
                file.readShort();
                int[] di_address = new int[12];
                for (int j = 0; j < di_address.length; j++)
                {
                    di_address[j] = file.readInt();
                }
                file.readInt();

                //System.out.printf("\n\nUID_Cur = %s\nGID_Cur = %s\nUID_Cre = %s\nGID_Cre = %s", UID_Current, GID_Current, UID_Creator, GID_Creator);
                if (CheckGroup(UID_Current, GID_Current, UID_Creator, GID_Creator))
                {
                    file.seek((di_address[0]));
                    byte[] temp = new byte[2048];
                    for (int i = 0; i < 2048; i++)
                    {
                        temp[i] = file.readByte();
                    }
                    String s = new String(temp, StandardCharsets.US_ASCII);

                    String[] content = s.split("#");
                    System.out.printf("\nContent in %s:\n%s", filename, content[0]);
                } else
                {
                    System.out.println("You don't have enough rights.");
                }


            } else
            {
                System.out.println("This file don't exist.");
            }
        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
    }

    void CheckFileExist(String filename, short UID_Current, short GID_Current)
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

            int id = -1;
            for (int i = 0; i < filenames.length; i++)
            {
                String[] subStr = filenames[i].split(" ");
                if (filename.equals(subStr[0]))
                {
                    id = i;
                    i = filenames.length;
                }
            }

            if (id >= 0)
            {
                file.seek(inodes[id]);
                char file_type = (char) file.readByte();
                if (file_type == '\0') file_type = '-';
                file.readByte();
                file.readByte();
                file.readShort();
                short UID_Creator = file.readShort();
                short GID_Creator = file.readShort();
                file.readInt();
                file.readByte();
                file.readByte();
                file.readShort();
                file.readByte();
                file.readByte();
                file.readShort();
                int[] di_address = new int[12];
                for (int j = 0; j < di_address.length; j++)
                {
                    di_address[j] = file.readInt();
                }
                file.readInt();

                if (CheckGroup(UID_Current, GID_Current, UID_Creator, GID_Creator))
                {

                    file.seek(inodes[id]);
                    for (int i = 0; i < 73; i++)
                    {
                        file.writeByte(0);
                    }

                    CM.MoveToPartition(file, 0, 4);
                    CM.MoveToPartition(file, (id * 62), 0);
                    for (int i = 0; i < 50; i++)
                    {
                        file.writeByte(0);
                    }
                    for (int i = 0; i < 10; i++)
                    {
                        file.writeByte(0);
                    }
                    file.writeByte(0);
                    file.writeByte(0);

                } else
                {
                    System.out.println("You don't have enough rights.");
                }

            } else
            {
                System.out.println("This file don't exist.");
            }
        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
    }

    boolean CheckGroup(int UID_Current, int GID_Current, int UID_Creator, int GID_Creator)
    {
        if (UID_Current == UID_Creator)
        {
            return true;
        } else return GID_Current == GID_Creator;
    }

    public void CopyExistFile(String filename, short UID_Current, short GID_Current)
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

            int id = -1;
            for (int i = 0; i < filenames.length; i++)
            {
                String[] subStr = filenames[i].split(" ");
                if (filename.equals(subStr[0]))
                {
                    id = i;
                    i = filenames.length;
                }
            }

            if (id >= 0)
            {

                file.seek(inodes[id]);
                char file_type_readed = (char) file.readByte();
                if (file_type_readed == '\0') file_type_readed = '-';
                byte file_rights_UG_readed = file.readByte();
                byte file_rights_OS_readed = file.readByte();
                short link_count_readed = file.readShort();
                short UID_Creator_readed = file.readShort();
                short GID_Creator_readed = file.readShort();
                int file_size_readed = file.readInt();
                byte created_date_readed = file.readByte();
                byte created_month_readed = file.readByte();
                short created_year_readed = file.readShort();
                file.readByte();
                file.readByte();
                file.readShort();
                int[] di_address_readed = new int[12];
                for (int j = 0; j < di_address_readed.length; j++)
                {
                    di_address_readed[j] = file.readInt();
                }
                int addressing_readed = file.readInt();

                //System.out.printf("\n\nUID_Cur = %s\nGID_Cur = %s\nUID_Cre = %s\nGID_Cre = %s", UID_Current, GID_Current, UID_Creator_readed, GID_Creator_readed);
                if (CheckGroup(UID_Current, GID_Current, UID_Creator_readed, GID_Creator_readed))
                {
                    file.seek((di_address_readed[0]));
                    byte[] temp = new byte[2048];
                    for (int i = 0; i < 2048; i++)
                    {
                        temp[i] = file.readByte();
                    }
                    String content_readed = new String(temp, StandardCharsets.US_ASCII);

                    StringBuilder content = new StringBuilder();
                    content.append(content_readed);

                    int cluster_adress = WriteDataToCluster(content, file);
                    //System.out.println(cluster_adress);

                    // Данные инода к записи

                    int cursor_free_inode = IL.FindFreeInode(file);
                    CM.MoveToPartition(file, 0, 3);
                    CM.MoveToPartition(file, cursor_free_inode, 0);

                    // Запись инода

                    //System.out.println("inod" + file.getFilePointer());
                    file.writeByte(file_type_readed);                              // 1
                    file.writeByte(file_rights_UG_readed);                         // 1
                    file.writeByte(file_rights_OS_readed);                         // 1
                    file.writeShort(link_count_readed);                            // 2
                    file.writeShort(UID_Creator_readed);                               // 2
                    file.writeShort(GID_Creator_readed);                              // 2
                    file.writeInt(file_size_readed);                               // 4
                    file.writeByte(created_date_readed);                           // 1
                    file.writeByte(created_month_readed);                          // 1
                    file.writeShort(created_year_readed);                          // 2
                    file.writeByte(created_date_readed);                           // 1
                    file.writeByte(created_month_readed);                          // 1
                    file.writeShort(created_year_readed);                          // 2
                    for (int diAdress : di_address_readed) file.writeInt(diAdress); // 12 * 4 = 48
                    file.writeInt(addressing_readed);                               // 4

                    // Запись данных в корневой каталог

                    int bytes_to_skip = CW.FindFreeRootCatalogRecord(file);
                    CM.MoveToPartition(file, 0, 4);
                    CM.MoveToPartition(file, bytes_to_skip * 62, 0);
                    int i;
                    for (i = 0; i < filenames[id].length(); i++)
                    {
                        if (filenames[id].charAt(i) == ' ')
                        {
                            break;
                        }
                    }

                    filenames[id] = filenames[id].substring(0, i);
                    filename = filenames[id] + "_cp";
                    System.out.println(filename);
                    String name = (String.format("%-25s", filename));
                    System.out.println(name);
                    String ext = extensions[id];
                    String ext_to_write = (String.format("%-5s", ext));
                    file.writeBytes(name);
                    file.writeBytes(ext_to_write);
                    file.writeShort(inodes[id]);

                    // Изменение битовых карт

                    BW.TakeNewBlock(file);
                    BW.TakeNewInode(file);

                } else
                {
                    System.out.println("You don't have enough rights.");
                }
            } else
            {
                System.out.println("This file don't exist.");
            }
        } catch (IOException ex)
        {
        }
    }

    public void EditExistFile(String filename, short UID_Current, short GID_Current, StringBuilder content)
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

            int id = -1;
            for (int i = 0; i < filenames.length; i++)
            {
                String[] subStr = filenames[i].split(" ");
                if (filename.equals(subStr[0]))
                {
                    id = i;
                    i = filenames.length;
                }
            }

            if (id >= 0)
            {

                file.seek(inodes[id]);
                char file_type_readed = (char) file.readByte();
                if (file_type_readed == '\0') file_type_readed = '-';
                byte file_rights_UG_readed = file.readByte();
                byte file_rights_OS_readed = file.readByte();
                short link_count_readed = file.readShort();
                short UID_Creator_readed = file.readShort();
                short GID_Creator_readed = file.readShort();
                int file_size_readed = file.readInt();
                byte created_date_readed = file.readByte();
                byte created_month_readed = file.readByte();
                short created_year_readed = file.readShort();
                file.readByte();
                file.readByte();
                file.readShort();
                int[] di_address_readed = new int[12];
                for (int j = 0; j < di_address_readed.length; j++)
                {
                    di_address_readed[j] = file.readInt();
                }
                int addressing_readed = file.readInt();

                //System.out.printf("\n\nUID_Cur = %s\nGID_Cur = %s\nUID_Cre = %s\nGID_Cre = %s", UID_Current, GID_Current, UID_Creator_readed, GID_Creator_readed);
                if (CheckGroup(UID_Current, GID_Current, UID_Creator_readed, GID_Creator_readed))
                {
                    file.seek((di_address_readed[0]));
                    WriteDataToClusterEdited(content, file);
                } else
                {
                    System.out.println("You don't have enough rights.");
                }
            } else
            {
                System.out.println("This file don't exist.");
            }
        } catch (IOException ex)
        {
        }
    }
}
