// Process Control Block (PCB) class
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class ProcessCB {
    int process_id;                            // ID
    int priority;                       // Priority
    int state;                          // State (1: ready, 0: blocked)
    int parent;                         // Parent

    LinkedList<Integer> children;           //  Children
    LinkedList<Resource> resources;         //

    public ProcessCB(int id, int priority, int state, int parent) {
        this.process_id = id;
        this.priority = priority;

        this.state = state;
        this.parent = parent;
        this.children = new LinkedList<>();
        this.resources = new LinkedList<>();
    }

    // Main functions

    // create(), which creates a new process at the priority level <p>;
    public void create(int current_process_index, int p){
        // Check if priority is higher than the limit when initialized in the ready list
        if (p >= Shell.readylist.length){
            try { Shell.writer.write("-1 ");}
            catch (IOException e) {throw new RuntimeException(e);}
            return;
        }

        // Creating child
        ProcessCB c_PCB = new ProcessCB(current_process_index, p, 1, process_id);

        // Initializing child
        c_PCB.state = 1;                                    // Ready state
        children.add(current_process_index);                // Insert j into the children of i
        c_PCB.parent = process_id;                          // Creating parent i

        Shell.readylist[p].add(current_process_index);      // inserting child into the ready list
        Shell.PCBs[current_process_index] = c_PCB;          // Adding into current array of Processes

        // Calling scheduler
        Shell.scheduler();
    }

    // destroy(), which destroy the process identified by the PCB index <i>, and all of its descendants
    public void destroy(int current_process_index, int i){
        // for all k in children of j destroy(k) recursively
        for (Integer cd : Shell.PCBs[current_process_index].children) {
            if (Shell.PCBs[cd] != null) {

                // Recursive call
                destroy(cd, Shell.PCBs[cd].priority);
            }
        }

        // free PCB of j with release
        for (Resource resource : Shell.PCBs[current_process_index].resources) {
            int r = resource.rid;
            int k = resource.units;
            releaseHelper(r, k);
        }

        // Remove j from parent's list
        Integer j = current_process_index;
        Shell.PCBs[process_id].children.remove(j);
        if (Shell.readylist[i].contains(current_process_index)){
            Shell.readylist[i].remove(Integer.valueOf(current_process_index));
        }

        // Release all resources of j
        for (ResourceCB rcb : Shell.RCBs) {
            Iterator<Resource> iterator = rcb.waitlist.iterator();
            while (iterator.hasNext()) {
                Resource resource = iterator.next();
                if (resource.rid == current_process_index) {
                    iterator.remove();
                    break;
                }
            }
        }
        // Allocate new PCB
        Shell.PCBs[current_process_index] = new ProcessCB(current_process_index, -1, -1, -1);
    }

    // request(), which requests <k> units of resource <r>; <r> can be 0, 1, 2, or 3.
    public int request(int r, int k){
        // Error handles if either r or k is greater than the inventory
        if (r >= Shell.RCBs.length || k > Shell.RCBs[r].inventory){
            try {Shell.writer.write("-1 ");}
            catch (IOException e) {throw new RuntimeException(e);}
            return -1;
        }

        // If sufficient resources are available, allocate them
        // if (r.state >= k)
        if (Shell.RCBs[r].free >= k) {

            // r.state = r.state - k
            Shell.RCBs[r].free -= k;

            // flag
            boolean update = false;

            // Attempt to update the existing resource entry
            for (Resource res : Shell.PCBs[process_id].resources) {
                if (res.rid == r) {
                    res.units += k;
                    update = true;
                    break;
                }
            }

            // If the resource doesn't exist, add a new one
            if (!update) {
                // insert (r, k) into i.resources
                Shell.PCBs[process_id].resources.addFirst(new Resource(r, k));
            }

        } else {
            // i.state = blocked
            Shell.PCBs[process_id].state = 0;

            // Remove the process from the current ready list
            for (int i = Shell.readylist.length - 1; i >= 0; i--) {
                if (Shell.readylist[i].contains(process_id)) {
                    Shell.readylist[i].remove(Integer.valueOf(process_id));
                    break;
                }
            }
            // Insert process in the resource waitlist
            boolean found = false;
            for (Resource wl_resource : Shell.RCBs[r].waitlist) {
                if (wl_resource.rid == process_id) {
                    wl_resource.units += k;

                    // flag
                    found = true;
                    break;
                }
            }
            if (!found) {
                Shell.RCBs[r].waitlist.addLast(new Resource(process_id, k));
            }
        }
        // Call the scheduler to possibly switch the current process
        Shell.scheduler();
        return 1;
    }

    // release(), which release the resource <r>, <r> can be 0, 1, 2, or 3; <k> is the number of units to be released
    public int release(int r, int k){
        // flag
        boolean r_found = false;

        // Iterator for the resources
        Iterator<Resource> iterator = Shell.PCBs[process_id].resources.iterator();

        while (iterator.hasNext()) {
            Resource resource = iterator.next();
            if (resource.rid == r && resource.units >= k) {
                resource.units -= k;
                if (resource.units == 0) {
                    // Remove the resource if no units are left
                    iterator.remove();
                }
                r_found = true;
                break;
            }
        }
        if (!r_found) {
            try {Shell.writer.write("-1 ");}
            catch (IOException e) {throw new RuntimeException(e);}
            return -1;
        }

        // If resources were successfully removed, invoke helper
        releaseHelper(r, k);
        Shell.scheduler();
        return 1;
    }

    // Release Helper
    public void releaseHelper(int r, int k) {
        // Increment the free count of the resource 'r' by 'k' units after they're released.
        Shell.RCBs[r].free += k;

        while (!Shell.RCBs[r].waitlist.isEmpty() && Shell.RCBs[r].free > 0) {
            // Get the first process in the waitlist for resource 'r'
            Resource next = Shell.RCBs[r].waitlist.getFirst();

            // Check if enough resources available  else subtract it
            if (Shell.RCBs[r].free >= next.units) {
                Shell.RCBs[r].free -= next.units;

                // add to waiting process, update resource list, remove from waitlist
                Shell.PCBs[next.rid].resources.addLast(new Resource(r, next.units));
                Shell.PCBs[next.rid].state = 1;
                Shell.RCBs[r].waitlist.remove(next);

                // find priority queue of unblocked p and reinsert it into ready list
                int j_priority = Shell.PCBs[next.rid].priority;
                Shell.readylist[j_priority].addLast(next.rid);

            } else {
                break;
            }
        }
    }


}


