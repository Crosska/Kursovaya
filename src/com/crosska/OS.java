package com.crosska;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class OS
{

    private short user_group_id = 1;
    private int user_count = 1;
    private short UID = 0;
    private short GID = 0;
    private String User_global;
    private String Password_global;
    private Scanner scan = new Scanner(System.in);

    private EventsPlanner EP = new EventsPlanner();
    private SuperBlock SB = new SuperBlock();
    private BlocksBitMap BBM = new BlocksBitMap();
    private InodesBitMap IBM = new InodesBitMap();
    private InodesList IL = new InodesList();
    private CatalogWork CW = new CatalogWork();
    private CursorMovement CM = new CursorMovement();
    private Data DT = new Data();
    private Crypter CRYPT = new Crypter();
    private FileWork FL = new FileWork();

    void Run(String Filename) throws IOException
    {
        SB.SetFilename(Filename);
        BBM.SetFilename(Filename);
        IBM.SetFilename(Filename);
        IL.SetFilename(Filename);
        CW.SetFilename(Filename);
        DT.SetFilename(Filename);
        FL.SetFilename(Filename);

        SB.WriteSB();
        BBM.WriteBitMap();
        IBM.WriteBitMap();
        IL.WriteIList();
        FL.CreateUsersFile();
        FL.CreateGroupsFile();

        String username;
        String password;
        boolean OSRun = true;                           // Переменная работы ОС
        boolean SessionRun;                             // Переменная работы текущей сессии пользователя
        String command;
        while (OSRun)
        {
            boolean AccountWork = true;
            while (AccountWork)
            {
                System.out.println("\nChoose your account work type (1 or 2):\n1. Create new user account\n2. Log in to existing user account\nYour choice: ");
                int choice;
                try
                {
                    choice = scan.nextInt();
                    switch (choice)
                    {
                        case 1:
                            boolean error = true;
                            username = scan.nextLine();
                            while (error)
                            {
                                System.out.println("Enter your new login (without symbol ' '):");
                                username = scan.nextLine();
                                String[] subStr = username.split(" ");
                                if (subStr.length != 1)
                                {
                                    System.out.println("Login can't include space symbol, use instead it symbol: _");
                                    error = true;
                                } else if (username.length() < 4 || username.length() > 15)
                                {
                                    System.out.println("Login must be length from 4 to 15 (included)");
                                    error = true;
                                } else
                                {
                                    User_global = username;
                                    while (error)
                                    {
                                        System.out.print("\nEnter your new password (without symbol ' '): ");
                                        password = scan.nextLine();
                                        subStr = password.split(" ");
                                        if (subStr.length != 1)
                                        {
                                            System.out.println("Login can't include space symbol, use instead it symbol: _");
                                            error = true;
                                        } else if (password.length() < 8 || password.length() > 32)
                                        {
                                            System.out.println("Login must be length from 8 to 32 (included)");
                                            error = true;
                                        } else
                                        {
                                            error = false;
                                            Password_global = password;
                                            user_count++;
                                            UID = user_group_id;
                                            GID = user_group_id;
                                            user_group_id++;
                                            DT.AddNewUser(user_group_id, user_group_id, User_global, Password_global, User_global, user_count);
                                        }
                                    }
                                }
                            }
                            break;
                        case 2:
                            AccountWork = false;
                            break;
                        default:
                            System.out.println("Error. Choose number from the list.");
                            break;
                    }
                } catch (Exception ex)
                {
                    System.out.println("Error. Type only number.");
                }
            }
            scan.nextLine();
            System.out.print("\nlogin: ");
            username = scan.nextLine();
            System.out.print("password: ");
            password = scan.nextLine();
            SessionRun = CheckLoginExist(username, password, Filename);
            if (!SessionRun)
            {
                System.out.println("Unable to log in. Check your username and password.");
            }
            while (SessionRun)
            {
                String[] subStr;
                System.out.printf("\n%s>", User_global);
                command = scan.nextLine();
                String delimeter = " ";
                subStr = command.split(delimeter);
                if (subStr.length <= 2 && subStr.length > 0)
                {
                    switch (subStr[0])
                    {
                        case "planner":
                            EP.StartPlanner(User_global);
                            break;
                        case "edfile":
                            try
                            {
                                String[] file_info = subStr[1].split("\\.");
                                if (file_info.length > 2 || file_info.length < 1)
                                {
                                    System.out.println("Syntax error. Type '-help' to find out right commands.");
                                } else if (file_info[0].length() > 15 || file_info[0].length() < 1)
                                {
                                    System.out.println("Error. File name must be size from 1 to 15 (included).");
                                } else
                                {

                                    StringBuilder content = new StringBuilder();
                                    System.out.println("Enter content of file (If you want to stop entering type '*'):");
                                    boolean cycle = true;
                                    while (cycle)
                                    {
                                        String temp = scan.nextLine();
                                        if (temp.equals("*"))
                                        {
                                            cycle = false;
                                            content.append("#");
                                            DT.EditExistFile(file_info[0], UID, GID, content);
                                        } else
                                        {
                                            temp = temp + "\n";
                                            content.append(temp);
                                        }
                                    }
                                }
                            } catch (Exception ex)
                            {
                                System.out.println("Syntax error. Type '-help' to find out right commands.");
                            }
                            break;
                        case "cpfile":
                            try
                            {
                                String[] file_info = subStr[1].split("\\.");
                                if (file_info.length > 2 || file_info.length < 1)
                                {
                                    System.out.println("Syntax error. Type '-help' to find out right commands.");
                                } else if (file_info[0].length() > 15 || file_info[0].length() < 1)
                                {
                                    System.out.println("Error. File name must be size from 1 to 15 (included).");
                                } else
                                {
                                    String filename_data = "";
                                    String extension_data = "";
                                    try
                                    {
                                        filename_data = file_info[0];
                                        extension_data = file_info[1];
                                        DT.CopyExistFile(filename_data, UID, GID);
                                    } catch (Exception ex)
                                    {
                                        extension_data = "";
                                        DT.CopyExistFile(filename_data, UID, GID);
                                    }

                                }
                            } catch (Exception ex)
                            {
                                System.out.println("Syntax error. Type '-help' to find out right commands.");
                            }
                            break;
                        case "shdir":
                            try
                            {
                                CW.ShowFiles(User_global);
                            } catch (Exception ex)
                            {
                                System.out.println(ex.toString());
                            }
                            break;
                        case "mkfile":
                            try
                            {
                                //System.out.println(subStr[1]);
                                String[] file_info = subStr[1].split("\\.");
                                //System.out.println(file_info.length);
                                if (file_info.length > 2 || file_info.length < 1)
                                {
                                    System.out.println("Syntax error. Type '-help' to find out right commands.");
                                } else if (file_info[0].length() > 15 || file_info[0].length() < 1)
                                {
                                    System.out.println("Error. File name must be size from 1 to 15 (included).");
                                } else
                                {
                                    String filename_data = "";
                                    String extension_data = "";
                                    try
                                    {
                                        filename_data = file_info[0];
                                        extension_data = file_info[1];
                                    } catch (Exception ex)
                                    {
                                        extension_data = "";
                                    }
                                    StringBuilder content = new StringBuilder();
                                    System.out.println("Enter content of file (If you want to stop entering type '*'):");
                                    boolean cycle = true;
                                    while (cycle)
                                    {
                                        String temp = scan.nextLine();
                                        if (temp.equals("*"))
                                        {
                                            cycle = false;
                                            content.append("#");
                                            FL.CreateFile(filename_data, extension_data, UID, GID, content);
                                        } else
                                        {
                                            temp = temp + "\n";
                                            content.append(temp);
                                        }
                                    }
                                }
                            } catch (Exception ex)
                            {
                                System.out.println("Syntax error. Type '-help' to find out right commands.");
                            }
                            break;
                        case "rmfile":
                            try
                            {
                                String[] file_info = subStr[1].split("\\.");
                                if (file_info.length > 2 || file_info.length < 1)
                                {
                                    System.out.println("Syntax error. Type '-help' to find out right commands.");
                                } else if (file_info[0].length() > 15 || file_info[0].length() < 1)
                                {
                                    System.out.println("Error. File name must be size from 1 to 15 (included).");
                                } else
                                {
                                    String filename_data = "";
                                    String extension_data = "";
                                    try
                                    {
                                        filename_data = file_info[0];
                                        extension_data = file_info[1];
                                        DT.CheckFileExist(filename_data, UID, GID);
                                    } catch (Exception ex)
                                    {
                                        extension_data = "";
                                        DT.CheckFileExist(filename_data, UID, GID);
                                    }

                                }
                            } catch (Exception ex)
                            {
                                System.out.println("Syntax error. Type '-help' to find out right commands.");
                            }
                            break;
                        case "-help":
                            System.out.println("Command line functions:\n" +
                                    "mkfile <filename> - create new file\n" +
                                    "rmfile <filename> - delete existing file\n" +
                                    "edfile <filename> - edit existing file\n" +
                                    "cpfile <filename> - copy existing file\n" +
                                    "opfile <filename> - open existing file\n" +
                                    "shdir - show root directory\n" +
                                    "shutdown - turn off file system\n" +
                                    "logout - log out from current account\n" +
                                    "-help - show help for the command line application");
                            break;
                        case "logout":
                            SessionRun = false;
                            break;
                        case "shutdown":
                            SessionRun = false;
                            OSRun = false;
                            break;
                        case "-getsb":
                            SB.ReadSB();
                            break;
                        case "-setsb":
                            SB.SetSB();
                            break;
                        case "-getbmb":
                            BBM.ReadBitMap();
                            break;
                        case "-getbmi":
                            IBM.ReadBitMap();
                            break;
                        case "-getilist":
                            IL.ReadIList();
                            break;
                        case "-getrcat":
                            CW.ReadRootCatalog();
                            break;
                        case "-tobit":
                            try
                            {
                                byte byte_number = Byte.parseByte(subStr[1]);
                                ConvertToBitView(byte_number);
                            } catch (Exception ex)
                            {
                                System.out.println("Syntax error. Type '-help' to find out right commands.");
                            }
                            break;
                        case "-toMD5":
                            try
                            {
                                String pass = subStr[1];
                                String crypted_pass = CRYPT.md5Custom(pass);
                                System.out.print("\n" + pass + " = " + crypted_pass + "\n");
                            } catch (Exception ex)
                            {
                                System.out.println("Syntax error. Type '-help' to find out right commands.");
                            }
                            break;
                        case "opfile":
                            try
                            {
                                String filename = subStr[1];
                                DT.ReadFile(filename, UID, GID);
                            } catch (Exception ex)
                            {
                                System.out.println("Syntax error. Type '-help' to find out right commands.");
                            }
                            break;
                        default:
                            System.out.println("Type \"-help\" if you don't know what to do.");
                            break;
                    }
                } else
                {
                    System.out.println("Type \"-help\" if you don't know what to do.");
                }
            }
        }
    }

    private boolean CheckPasswordMatch(String[] passwords, String password_entered)
    {
        for (String password : passwords)
        {
            if (CRYPT.md5Custom(password_entered).equals(password))
            {
                Password_global = password;
                return true;
            }
            //System.out.println(CRYPT.md5Custom(password_entered) + " = " + password);
        }
        return false;
    }

    private boolean CheckLoginExist(String username_entered, String password_entered, String Filename)
    {
        try (RandomAccessFile file = new RandomAccessFile(new File(Filename), "rw"))
        {
            short UserID = -1;
            short GroupID = -1;
            String[] logins = new String[25];
            String[] passwords = new String[25];
            file.seek(49153);
            for (int i = 0; i < 25; i++)
            {
                String s;
                String[] subStr;
                String delimeter = " ";
                byte[] temp = new byte[15];

                UserID = file.readShort();
                GroupID = file.readShort();

                for (int j = 0; j < 15; j++)
                {
                    temp[j] = file.readByte();
                }
                s = new String(temp, StandardCharsets.US_ASCII);

                subStr = s.split(delimeter);

                logins[i] = subStr[0];

                temp = new byte[32];
                for (int j = 0; j < 32; j++)
                {
                    temp[j] = file.readByte();
                }
                s = new String(temp, StandardCharsets.US_ASCII);

                passwords[i] = s;

                temp = new byte[15];
                for (int j = 0; j < 15; j++)
                {
                    temp[j] = file.readByte();
                }
                //s = new String(temp, StandardCharsets.US_ASCII);

                file.skipBytes(62);
            }
            for (String login : logins)
            {
                if (username_entered.equals(login))
                {
                    User_global = login;
                    if (CheckPasswordMatch(passwords, password_entered))
                    {
                        return true;
                    } else
                    {
                        return false;
                    }
                }
                //System.out.println(username_entered + " = " + login);
            }
            return false;
        } catch (IOException ex)
        {
            System.out.println(ex.toString());
            return false;
        }
    }

    private void ConvertToBitView(Byte number)
    {
        System.out.println(String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(number))).replace(' ', '0'));
    }

}
