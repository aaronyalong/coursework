// Ready List
import java.util.LinkedList;

public class ReadyList {
    LinkedList<Integer>[] readyList;

    // Argument is the number of priorities <levels>
    public ReadyList(int levels) {

        // Initialize ready list
        readyList = new LinkedList[levels];

        // Iterate through the linked list
        for (int i = 0; i < levels; i++) {
            readyList[i] = new LinkedList<>();
        }
    }
}
