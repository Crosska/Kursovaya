package com.crosska;

import java.io.File;
import java.io.IOException;

public class Main
{
    public static void main(String[] Args) throws IOException
    {
        String Filename = "System.dat";                 // Название файла с ФС
        OS DevOS = new OS();                            // Создание обьекта ОС
        File file = new File("System.dat");
        file.delete();
        DevOS.Run(Filename);                             // Вызов начальной функции ОС с передачей названия файла
    }
}
