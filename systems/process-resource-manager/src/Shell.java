// Command Shell
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


public class Shell {
    static ProcessCB[] PCBs;
    static ResourceCB[] RCBs;
    static LinkedList<Integer>[] readylist;

    // For writing to output.txt
    static BufferedWriter writer;

    int pcb_size = 16;
    int rcb_size = 4;

    public Shell(int p, int r, int levels) {
        // A process descriptor array PCB
        PCBs = new ProcessCB[p];

        // A process descriptor array RCB
        RCBs = new ResourceCB[r];

        // A multi-level RL
        ReadyList RL = new ReadyList(levels);
        readylist = RL.readyList;
    }

    public void init_default() {
        // Initial state with default values
        init(3, 1, 1, 2, 3);
    }

    public void init(int levels, int inventory1, int inventory2, int inventory3, int inventory4) {
        // Re-initialize PCBs and RCBs
        PCBs = new ProcessCB[pcb_size];
        RCBs = new ResourceCB[rcb_size];

        // Create a single running process at PCB[0] with priority 0
        PCBs[0] = new ProcessCB(0, 0, 1, -1);

        // Erase and set PCB other PCBs
        for (int i = 1; i < PCBs.length; i++) {
            PCBs[i] = new ProcessCB(i, -1, -1, -1); // Initialize other PCB entries to -1 (free)
        }

        // Initialize RCB resource entries
        RCBs[0] = new ResourceCB(inventory1, inventory1);
        RCBs[1] = new ResourceCB(inventory2, inventory2);
        RCBs[2] = new ResourceCB(inventory3, inventory3);
        RCBs[3] = new ResourceCB(inventory4, inventory4);

        // Enter the process into the RL at the lowest-priority level 0
        ReadyList test = new ReadyList(levels);
        readylist = test.readyList;
        readylist[0].add(0);

        // Call scheduler
        scheduler();
    }

    static void timeout(int running){
        // Get currently running process and remove it
        int rm_process = readylist[running].remove();

        // move process from head of RL to end of RL
        readylist[running].addLast(rm_process);

        // Call Scheduler
        scheduler();
    }

    static void scheduler(){
        // Find process i currently at the head of the ready list
        int head = -1;
        for (int i = readylist.length - 1; i >= 0; i--) {
            if (!readylist[i].isEmpty()) {
                head = i;
                break;}}

        // Display the process running
        try {
            writer.write(Integer.toString(readylist[head].getFirst()));
            writer.write(" ");
        } catch (IOException e) {
            throw new RuntimeException(e);}
    }

    public static void main(String[] args){
        // Initialize
        Shell ExtendedManager = new Shell(16, 4, 3);

        // Input and Output files
        String inputFile = "input.txt";
        String outputFile = "output.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            // Input and Output Declarations
            File file = new File(outputFile);
            FileOutputStream fos = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            String line;

            // Global variable to keep track of the currently running process
            int current_running_process_id = 0;

            // Read file (Shell)
            while ((line = reader.readLine()) != null) {
                String[] cmd = line.split(" ");

                // Switch to handle each case
                switch(cmd[0]){

                    // Init
                    case "in":
                        ExtendedManager.init(
                                Integer.parseInt(cmd[1]),
                                Integer.parseInt(cmd[2]),
                                Integer.parseInt(cmd[3]),
                                Integer.parseInt(cmd[4]),
                                Integer.parseInt(cmd[5]));
                        current_running_process_id++;
                        break;

                    // Init Default
                    case "id":
                        ExtendedManager.init_default();
                        current_running_process_id++;
                        break;

                    // Create
                    case "cr":
                        int cr = Integer.parseInt(cmd[1]);

                        // Getting cr index
                        int ind_cr = -1;
                        for (int i = readylist.length - 1; i >= 0; i--) {
                            if (!readylist[i].isEmpty()) {
                                ind_cr = i;
                                break;
                            }
                        }
                        int ret = current_running_process_id;
                        int first_cr = readylist[ind_cr].getFirst();
                        PCBs[first_cr].create(ret, cr);

                        // Update Global index
                        current_running_process_id++;
                        break;

                    // Destroy
                    case "de":
                        int de = Integer.parseInt(cmd[1]);

                        // Getting de index
                        int ind_de = -1;
                        for (int i = readylist.length - 1; i >= 0; i--) {
                            if (!readylist[i].isEmpty()) {
                                ind_de = i;
                                break;
                            }
                        }

                        int first_de = readylist[ind_de].getFirst();

                        // Destroy if it contains children or is the currently running process
                        if (PCBs[first_de].children.contains(de) || first_de == de){
                            PCBs[first_de].destroy(de, ind_de);

                            // Update Global index
                            current_running_process_id++;

                            // Call scheduler
                            scheduler();

                        } else {writer.write("-1 ");}
                        break;

                    // Request
                    case "rq":
                        // Getting rq index
                        int ind_rq = -1;
                        for (int i = readylist.length - 1; i >= 0; i--) {
                            if (!readylist[i].isEmpty()) {
                                ind_rq = i;
                                break;
                            }
                        }

                        // Resource
                        int r = Integer.parseInt(cmd[1]);

                        // Units Amount
                        int amount = Integer.parseInt(cmd[2]);
                        int first_rq = readylist[ind_rq].getFirst();

                        // Error Handling
                        if (PCBs[first_rq].request(r, amount) == -1){}

                        break;

                    // Release
                    case "rl":
                        // Getting rl index
                        int ind_rl = -1;
                        for (int i = readylist.length - 1; i >= 0; i--) {
                            if (!readylist[i].isEmpty()) {
                                ind_rl = i;
                                break;
                            }
                        }

                        // Resource
                        int r_rl = Integer.parseInt(cmd[1]);

                        // Units Amount
                        int amount_rl = Integer.parseInt(cmd[2]);
                        int j = readylist[ind_rl].getFirst();

                        // Error Handling
                        if (PCBs[j].release(r_rl, amount_rl) == -1){}
                        break;

                    // Timeout
                    case "to":
                        // Getting Timeout index
                        int ind_to = -1;
                        for (int i = readylist.length - 1; i >= 0; i--) {
                            if (!readylist[i].isEmpty()) {
                                ind_to = i;
                                break;
                            }
                        }

                        // Call timeout at the current process
                        timeout(ind_to);
                        break;

                    default:
                        writer.newLine();
                        current_running_process_id = 0;
                        break;
                }
            }
            // Close writing
            writer.close();
        } catch (IOException e) {System.err.println("Error reading input file: " + e.getMessage());}
    }
}