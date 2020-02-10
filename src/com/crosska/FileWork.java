package com.crosska;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class FileWork
{

    private GregorianCalendar GCalendar = new GregorianCalendar();
    private CursorMovement CM = new CursorMovement();
    private InodesBitMap InodeBM = new InodesBitMap();
    private BlocksBitMap BlockBM = new BlocksBitMap();
    private CatalogWork CW = new CatalogWork();
    private InodesList IL = new InodesList();
    private BitWork BW = new BitWork();
    private Data DT = new Data();
    private String Filename;

    void SetFilename(String Filename_received)
    {
        Filename = Filename_received;
    }

    public void CreateUsersFile()
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {

            // Запись данных в кластер ID USER | ID GROUP | Login | Password | HomeCatalog

            short id = 0;
            int cluster_adress = DT.WriteUsersDataToCluster(id, id, "root", "162d3K", "root", file);

            // Данные инода к записи

            int cursor_free_inode = IL.FindFreeInode(file);    // Возвращает указатель на блок с пустым инодом
            CM.MoveToPartition(file, 0, 3);
            CM.MoveToPartition(file, cursor_free_inode, 0);

            short inode_number = (short) file.getFilePointer();                                  // Сохранение указателя на инод
            byte file_type = 'f';                                                                // Тип файла
            byte file_rights_UG = (byte) -32; // 11100000
            byte file_rights_OS = (byte) -240; // 00010000
            short link_count = 1;                                                                // Количество ссылок
            short user_id = 0;                                                                   // ID пользователя
            short group_id = 0;                                                                  // ID группы
            int file_size = 2048;                                                                // Размер файла
            byte created_date = (byte) GCalendar.get(Calendar.DATE);                             // Число
            byte created_month = (byte) (GCalendar.get(Calendar.MONTH) + 1);                     // Месяц
            short created_year = (short) GCalendar.get(Calendar.YEAR);                           // Год
            int[] di_adress = new int[]{cluster_adress, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};          // Адреса на кластеры в data
            int adressing = 0;                                                                   // Косвенная адресация

            // Запись инода

            file.writeByte(file_type);                              // 1
            file.writeByte(file_rights_UG);                         // 1
            file.writeByte(file_rights_OS);                         // 1
            file.writeShort(link_count);                            // 2
            file.writeShort(user_id);                               // 2
            file.writeShort(group_id);                              // 2
            file.writeInt(file_size);                               // 4
            file.writeByte(created_date);                           // 1
            file.writeByte(created_month);                          // 1
            file.writeShort(created_year);                          // 2
            file.writeByte(created_date);                           // 1
            file.writeByte(created_month);                          // 1
            file.writeShort(created_year);                          // 2
            for (int diAdress : di_adress) file.writeInt(diAdress); // 12 * 4 = 48
            file.writeInt(adressing);                               // 4

            // Запись данных в корневой каталог

            int bytes_to_skip = CW.FindFreeRootCatalogRecord(file);
            CM.MoveToPartition(file, 0, 4);
            CM.MoveToPartition(file, bytes_to_skip * 62, 0);
            String filename = "users";
            String name = (String.format("%-25s", filename)).replace(' ', '\0');
            String extension = "     ";
            file.writeBytes(name);
            file.writeBytes(extension);
            file.writeShort(inode_number);

            // Изменение битовых карт

            BW.TakeNewBlock(file);
            BW.TakeNewInode(file);

        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
    }

    public void CreateGroupsFile()
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            StringBuilder str = new StringBuilder();
            str.append("0=0|"); // UID=GID
            int cluster_adress = DT.WriteGroupDataToCluster(str, file);
            //System.out.println(cluster_adress);

            int cursor_free_inode = IL.FindFreeInode(file);
            CM.MoveToPartition(file, 0, 3);
            CM.MoveToPartition(file, cursor_free_inode, 0);

            short inode_number = (short) file.getFilePointer();                                  // Сохранение указателя на инод
            byte file_type = 'f';                                                                // Тип файла
            byte file_rights_UG = (byte) -16; // 11110000
            byte file_rights_OS = (byte) -112; // 10010000
            short link_count = 1;                                                                // Количество ссылок
            short user_id = 0;                                                                   // ID пользователя
            short group_id = 0;                                                                  // ID группы
            int file_size = 2048;                                                                // Размер файла
            byte created_date = (byte) GCalendar.get(Calendar.DATE);                             // Число
            byte created_month = (byte) (GCalendar.get(Calendar.MONTH) + 1);                     // Месяц
            short created_year = (short) GCalendar.get(Calendar.YEAR);                           // Год
            int[] di_adress = new int[]{cluster_adress, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};// Адреса на кластеры в data
            int adressing = 0;                                                                   // Косвенная адресация

            // Запись инода

            file.writeByte(file_type);                              // 1
            file.writeByte(file_rights_UG);                         // 1
            file.writeByte(file_rights_OS);                         // 1
            file.writeShort(link_count);                            // 2
            file.writeShort(user_id);                               // 2
            file.writeShort(group_id);                              // 2
            file.writeInt(file_size);                               // 4
            file.writeByte(created_date);                           // 1
            file.writeByte(created_month);                          // 1
            file.writeShort(created_year);                          // 2
            file.writeByte(created_date);                           // 1
            file.writeByte(created_month);                          // 1
            file.writeShort(created_year);                          // 2
            for (int diAdress : di_adress) file.writeInt(diAdress); // 12 * 4 = 48
            file.writeInt(adressing);                               // 4

            // Запись данных в корневой каталог

            int bytes_to_skip = CW.FindFreeRootCatalogRecord(file);
            CM.MoveToPartition(file, 0, 4);
            CM.MoveToPartition(file, bytes_to_skip * 62, 0);
            String filename = "groups";
            String name = (String.format("%-25s", filename)).replace(' ', '\0');
            String extension = "     ";
            file.writeBytes(name);
            file.writeBytes(extension);
            file.writeShort(inode_number);

            // Изменение битовых карт

            BW.TakeNewBlock(file);
            BW.TakeNewInode(file);

        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
    }

    public void CreateFile(String Filename_received, String Extension_received, short user_id, short group_id, StringBuilder content)
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            // Запись данных в кластер

            int cluster_adress = DT.WriteDataToCluster(content, file);
            //System.out.println(cluster_adress);

            // Данные инода к записи

            int cursor_free_inode = IL.FindFreeInode(file);
            CM.MoveToPartition(file, 0, 3);
            CM.MoveToPartition(file, cursor_free_inode, 0);

            short inode_number = (short) file.getFilePointer();                                  // Сохранение указателя на инод
            byte file_type = 'f';                                                                // Тип файла
            byte file_rights_UG = (byte) -32; // 11100000
            byte file_rights_OS = (byte) 0; // 00000000
            short link_count = 1;                                                                // Количество ссылок
            int file_size = 2048;                                                                // Размер файла
            byte created_date = (byte) GCalendar.get(Calendar.DATE);                             // Число
            byte created_month = (byte) (GCalendar.get(Calendar.MONTH) + 1);                     // Месяц
            short created_year = (short) GCalendar.get(Calendar.YEAR);                           // Год
            int[] di_adress = new int[]{cluster_adress, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};          // Адреса на кластеры в data
            int adressing = 0;                                                                   // Косвенная адресация

            // Запись инода

            //System.out.println("inod" + file.getFilePointer());
            file.writeByte(file_type);                              // 1
            file.writeByte(file_rights_UG);                         // 1
            file.writeByte(file_rights_OS);                         // 1
            file.writeShort(link_count);                            // 2
            file.writeShort(user_id);                               // 2
            file.writeShort(group_id);                              // 2
            file.writeInt(file_size);                               // 4
            file.writeByte(created_date);                           // 1
            file.writeByte(created_month);                          // 1
            file.writeShort(created_year);                          // 2
            file.writeByte(created_date);                           // 1
            file.writeByte(created_month);                          // 1
            file.writeShort(created_year);                          // 2
            for (int diAdress : di_adress) file.writeInt(diAdress); // 12 * 4 = 48
            file.writeInt(adressing);                               // 4

            // Запись данных в корневой каталог

            int bytes_to_skip = CW.FindFreeRootCatalogRecord(file);
            CM.MoveToPartition(file, 0, 4);
            CM.MoveToPartition(file, bytes_to_skip * 62, 0);
            String filename = Filename_received;
            String name = (String.format("%-25s", filename));
            String ext = Extension_received;
            String ext_to_write = (String.format("%-5s", ext));
            file.writeBytes(name);
            file.writeBytes(ext_to_write);
            file.writeShort(inode_number);

            // Изменение битовых карт

            BW.TakeNewBlock(file);
            BW.TakeNewInode(file);

        } catch (IOException ex)
        {
            System.out.println(ex.toString());
        }
    }
}
