package com.crosska;

import java.util.ArrayList;
import java.util.Scanner;

public class EventsPlanner
{
    String User;
    List list = new List();
    Scanner scan = new Scanner(System.in);
    int last_id = 0;

    void StartPlanner(String Username)
    {

        Thread tick = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                int ID_Current;
                while (true)
                {
                    ID_Current = list.GetNewProcessID();
                    for (int i = 0; i < 1; i++) // < количество тиков
                    {
                        ID_Current = list.Tick(ID_Current);
                        try
                        {
                            Thread.sleep(1000);
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });
        tick.start();

        //User = Username;
        User = "root";
        boolean SessionRun = true;
        String command;
        while (SessionRun)
        {
            String[] subStr;
            System.out.print("\nplanner>" + Username + ">");
            command = scan.nextLine();
            String delimeter = " ";
            subStr = command.split(delimeter);
            if (subStr.length <= 3 && subStr.length > 0)
            {
                switch (subStr[0])
                {
                    case "start":
                        if (subStr.length == 3)
                        {
                            int priority = Integer.parseInt(subStr[1]);
                            int duration = Integer.parseInt(subStr[2]);
                            list.AddNewProcessToList(last_id, User, priority, duration);
                            last_id++;
                        } else
                        {
                            System.out.println("Syntax error. Type '-help' to find out right commands.");
                        }
                        break;
                    case "kill":
                        if (subStr.length == 2)
                        {
                            int PID = Integer.parseInt(subStr[1]);
                            list.DeleteProcess(PID);
                        } else
                        {
                            System.out.println("Syntax error. Type '-help' to find out right commands.");
                        }
                        break;
                    case "pause":
                        if (subStr.length == 2)
                        {
                            int PID = Integer.parseInt(subStr[1]);
                            list.StopProcess(PID);
                        } else
                        {
                            System.out.println("Syntax error. Type '-help' to find out right commands.");
                        }
                        break;
                    case "continue":
                        if (subStr.length == 2)
                        {
                            int PID = Integer.parseInt(subStr[1]);
                            list.ContinueProcess(PID);
                        } else
                        {
                            System.out.println("Syntax error. Type '-help' to find out right commands.");
                        }
                        break;
                    case "renice":
                        if (subStr.length == 3)
                        {
                            int pid = Integer.parseInt(subStr[1]);
                            int priority = Integer.parseInt(subStr[2]);
                            list.ChangePriority(pid, priority);
                        } else
                        {
                            System.out.println("Syntax error. Type '-help' to find out right commands.");
                        }
                        break;
                    case "killall":
                        list.KillAllProcesses();
                        last_id = 0;
                        break;
                    case "psnow":
                        list.ShowAllProcess();
                        break;
                    case "-help":
                        System.out.println("");
                        break;
                    case "back":
                        SessionRun = false;
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

class Process
{

    Process(int last, String user, int priority, int duration)
    {
        PID = last + 1;
        USER = user;
        PR = priority;
        DURATION = duration;
    }

    private int PID;
    private String USER;
    private int PR;
    private int STAT = 2;
    private int DURATION;

    public void WorkOneTick()
    {
        DURATION--;
    }

    public int getDURATION()
    {
        return DURATION;
    }

    public void setDURATION(int DURATION)
    {
        this.DURATION = DURATION;
    }

    public int getPID()
    {
        return PID;
    }

    public String getUSER()
    {
        return USER;
    }

    public int getPR()
    {
        return PR;
    }

    public void setPR(int PR)
    {
        this.PR = PR;
    }

    public int getSTAT()
    {
        return STAT;
    }

    public void setSTAT(int STAT)
    {
        this.STAT = STAT;
    }

}

class List
{

    ArrayList<Process> list;

    List()
    {
        list = new ArrayList<Process>();
    }

    void ShowAllProcess()
    {

        ArrayList<Process> list_sorted = list;

        boolean isSorted = false;
        Process buf;
        while (!isSorted)
        {
            isSorted = true;
            for (int i = 0; i < list_sorted.size() - 1; i++)
            {
                if ((list_sorted.get(i)).getPR() > (list_sorted.get(i + 1)).getPR())
                {
                    isSorted = false;

                    buf = list_sorted.get(i);
                    list_sorted.set(i, list_sorted.get(i + 1));
                    list_sorted.set(i + 1, buf);
                }
            }
        }

        System.out.println("| PID |      USER     | PR |  STATUS  | TTL |");
        for (Process process_obj : list_sorted)
        {
            int PID = process_obj.getPID();
            String USER = process_obj.getUSER();
            int PR = process_obj.getPR();
            int STATUS_int = process_obj.getSTAT();
            String STATUS;
            if (STATUS_int == 0)
            {
                STATUS = "paused";
            } else if (STATUS_int == 1)
            {
                STATUS = "working";
            } else if (STATUS_int == 2)
            {
                STATUS = "waiting";
            } else
            {
                STATUS = "completed";
            }
            int TTL = process_obj.getDURATION();
            System.out.printf("|%5d|%15s|%4d|%10s|%5s|\n", PID, USER, PR, STATUS, TTL);
        }
    }

    public int GetNewProcessID()
    {
        int PID = 0;
        int max_pr = 0;
        try
        {
            for (Process process_obj : list)
            {
                int temp_pr = process_obj.getPR();
                if ((temp_pr > max_pr) && (process_obj.getSTAT() != 4) && (process_obj.getSTAT() != 0))
                {
                    max_pr = temp_pr;
                    PID = process_obj.getPID();
                }
                if (process_obj.getSTAT() == 1)
                {
                    process_obj.setSTAT(2);
                }
            }
        } catch (Exception ex)
        {
            //System.out.println(ex.toString());
        }
        return PID;
    }

    void DeleteProcess(int id)
    {
        try
        {
            Process process_obj = list.get(id - 1);
            process_obj.setSTAT(3);
            process_obj.setPR(-1);
        } catch (Exception ex)
        {
            System.out.println("There is no such process");
        }
    }

    public void AddNewProcessToList(int last_id, String user, int priority, int duration)
    {
        Process process_obj = new Process(last_id, user, priority, duration);
        list.add(process_obj);
    }

    public int Tick(int PID_Current)
    {
        try
        {
            Process process_current_obj = list.get(PID_Current - 1);
            if (process_current_obj.getDURATION() == 0)
            {
                DeleteProcess(PID_Current);
                return (GetNewProcessID() + 1);
            } else
            {
                int PID_New = GetNewProcessID();
                if (PID_Current != PID_New)
                {
                    Process process_old_obj = list.get(PID_Current - 1);
                    process_old_obj.setSTAT(2);
                    PID_Current = PID_New;
                } else if (process_current_obj.getSTAT() != 3)
                {
                    process_current_obj = list.get(PID_Current - 1);
                    process_current_obj.setSTAT(1);
                    process_current_obj.WorkOneTick();
                } else if (process_current_obj.getSTAT() == 2)
                {
                    process_current_obj = list.get(PID_Current - 1);
                    process_current_obj.setSTAT(1);
                    process_current_obj.WorkOneTick();
                } else
                {
                    return -1;
                }
            }
        } catch (Exception ex)
        {
            //System.out.println(ex.toString());
        }
        return -1;
    }

    public void StopProcess(int id)
    {
        try
        {
            Process process_obj = list.get(id - 1);
            if (process_obj.getSTAT() == 1 || process_obj.getSTAT() == 2)
            {
                process_obj.setSTAT(0);
            } else if (process_obj.getSTAT() == 3)
            {
                System.out.println("This process is killed");
            } else
            {
                System.out.println("This process is already paused");
            }
        } catch (Exception ex)
        {
            System.out.println("There is no such process");
        }
    }

    public void ContinueProcess(int id)
    {
        try
        {
            Process process_obj = list.get(id - 1);
            if (process_obj.getSTAT() == 0)
            {
                process_obj.setSTAT(2);
            } else
            {
                System.out.println("This process is not paused");
            }
        } catch (Exception ex)
        {
            System.out.println("There is no such process");
        }
    }

    public void KillAllProcesses()
    {
        list.clear();
    }

    public void ChangePriority(int id, int priority)
    {
        try
        {
            Process process_obj = list.get(id - 1);
            if ((priority != process_obj.getPR() && (priority > 0) && process_obj.getSTAT() != 3))
            {
                process_obj.setPR(priority);
            } else
            {
                System.out.println("Syntax error.");
            }
        } catch (Exception ex)
        {
            System.out.println("There is no such process.");
        }
    }

}