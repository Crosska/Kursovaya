package com.crosska;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Data
{

    private SuperBlock SB = new SuperBlock();
    private String Filename;
    private BitWork BT = new BitWork();
    private CursorMovement CM = new CursorMovement();
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


}
