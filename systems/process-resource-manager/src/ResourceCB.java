// Resource Control Block (RCB) class
import java.util.LinkedList;

public class ResourceCB {
    // Inventory units of the resource
    int inventory;

    // free state of the resource
    int free;

    // Waitlist for the resource
    LinkedList<Resource> waitlist;

    // Initialize
    public ResourceCB(int inventory, int free) {
        // Initialize inventory, free state and the waitlist
        this.inventory = inventory;
        this.free = free;
        this.waitlist = new LinkedList<>();
    }
}
