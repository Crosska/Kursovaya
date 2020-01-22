package com.crosska;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

public class BitWork
{


    private InodesBitMap IBM = new InodesBitMap();
    private BlocksBitMap BMB = new BlocksBitMap();
    private CursorMovement CM = new CursorMovement();
    private String Filename;

    void SetFilename(String Filename_received)
    {
        Filename = Filename_received;
    }

    int GetPositionOfZeroByte(long filePointer, RandomAccessFile file)
    {
        try
        {
            file.seek(filePointer);
            byte byte_view = file.readByte();
            String bit_view = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(byte_view))).replace(' ', '0');
            for (int i = 0; i < bit_view.length(); i++)
            {
                if (bit_view.charAt(i) == '0')
                {
                    return ++i;
                }
            }
            return -10;
        } catch (IOException ex)
        {
            return -10;
        }
    }

    void TakeNewInode(RandomAccessFile file)
    {
        try
        {
            int cursor_free_inode = IBM.FindFreeInode(file);
            file.seek(cursor_free_inode);
            byte my_byte = file.readByte();
            file.seek(cursor_free_inode);
            int position = GetPositionOfZeroByte(file.getFilePointer(), file);
            String byte_string = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(my_byte))).replace(' ', '0');
            byte final_byte = SetBitToOne(byte_string, position);
            file.seek(cursor_free_inode);
            file.writeByte(final_byte);
        } catch (IOException ex)
        {
            System.out.println();
        }
    }

    void TakeNewBlock(RandomAccessFile file)
    {
        try
        {
            int cursor_free_block = BMB.FindFreeBlock(file);
            file.seek(cursor_free_block);
            byte my_byte = file.readByte();
            file.seek(cursor_free_block);
            int position = GetPositionOfZeroByte(cursor_free_block, file);
            String byte_string = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(my_byte))).replace(' ', '0');
            byte final_byte = SetBitToOne(byte_string, position);
            file.seek(cursor_free_block);
            file.writeByte(final_byte);
        } catch (Exception ex)
        {
            System.out.println();
        }
    }

    byte SetBitToOne(String my_byte, int pos)
    {
        pos = 8 - pos;
        BitSet bits1 = fromString(my_byte);
        bits1.set(pos, true);
        return bits1.toByteArray()[0];
    }

    byte SetBitToZero(String my_byte, int pos)
    {
        pos = 8 - pos;
        BitSet bits1 = fromString(my_byte);
        bits1.set(pos, false);
        return bits1.toByteArray()[0];
    }

    private BitSet fromString(final String s)
    {
        return BitSet.valueOf(new long[]{Long.parseLong(s, 2)});
    }

    private String toString(BitSet bs)
    {
        return Long.toString(bs.toLongArray()[0], 2);
    }


}
