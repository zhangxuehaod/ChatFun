package serverAndClient;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by apple on 15-7-11.
 */
public class IdCreator implements StateObject {

    private Set<Integer> reservedID = new HashSet<Integer>();
    public void addReservedID(int...ids){
        for(int i:ids)
            reservedID.add(i);
    }

    private Integer maxID = 1;
    private Queue<Integer> recycledID = new LinkedList<Integer>();
    private final int inc;
    public IdCreator(){
        inc=1;
    }
    public IdCreator(boolean isPositive){
        inc=isPositive?1:-1;
    }
    public Integer createID() {
        int id;
        synchronized (maxID) {
            do {
                if (recycledID.isEmpty())
                    id = inc * (maxID++);
                else
                    id = recycledID.poll();
            }while (reservedID.contains(id));
        }
        return id;
    }
    public String createIdStr(){
        return createID().toString();
    }
    public void recycleID(Integer id) {
        synchronized (maxID) {
            recycledID.add(id);
        }
    }
    public void recycleID(String id) {
        try {
            Integer in = Integer.valueOf(id);
            synchronized (maxID) {
                recycledID.add(in);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void tryRemoveID(int id){
        recycledID.remove(id);
    }
    public void clear() {
        synchronized (maxID) {
            maxID = 1;
            recycledID.clear();
        }
    }

    @Override
    public State getState() {
        synchronized (maxID) {
            return State.code(new State(maxID),
                    State.codeIntArray(reservedID),
                    State.codeIntArray(recycledID));
        }
    }

    @Override
    public void setState(State state) {
        synchronized (maxID) {
            reservedID.clear();
            recycledID.clear();
            State[] s=State.decodeToArray(state);
            maxID = s[0].getInt();
            for (int i:s[1].ints)
                reservedID.add(i);
            for (int i:s[2].ints)
                recycledID.add(i);
        }
    }
}
