package serverAndClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class State {
    public static final String  ClientManager = "Cli", GameManager = "Gam", ChatManager = "Cha";

    public int syn=-1;
    public String classId = "";
    public int methodId = -1;

    public int[] ints = new int[0];
    public String[] strs = new String[0];
    public boolean[] bools = new boolean[0];

    public State() {
    }

    public void setIdType(String id, int type) {
        this.classId = id;
        this.methodId = type;
    }

    public String getClassID() {
        return classId;
    }

    public int getMethodId() {
        return methodId;
    }

    public void setSyn(int syn){
        this.syn=syn;
    }
    public int getSyn(){
        return syn;
    }
    public State(int[] ints, String[] strs, boolean[] bools) {
        this.ints = ints;
        this.strs = strs;
        this.bools = bools;
    }

    public State(int[] ints, String[] strs) {
        this.ints = ints;
        this.strs = strs;
    }

    public State(int... s) {
        this.ints = s;
    }

    public State(String... s) {
        this.strs = s;
    }
    public State(String id,int code){
        setString(id);
        setInt(code);
    }
    public State(boolean... s) {
        this.bools = s;
    }

    public State(State other) {
        this.classId = other.classId;
        this.methodId = other.methodId;
        this.ints = other.ints;
        this.strs = other.strs;
        this.bools = other.bools;
    }

    public State(Collection<String> s) {
        this.strs = new String[s.size()];
        s.toArray(strs);
    }

    public void setState(int... s) {
        this.ints = s;
    }

    public void setState(String... s) {
        this.strs = s;
    }

    public void setState(boolean... s) {
        this.bools = s;
    }

    public void setState(Collection<String> s) {
        this.strs = new String[s.size()];
        s.toArray(strs);
    }


    public static State codeStateObject(StateObject... stateObjects) {
        List<String> strs = new ArrayList<>();
        strs.add(String.valueOf(stateObjects.length));
        for (StateObject i : stateObjects) {
            List<String> strs1 = i.getState().toStrArray();
            strs.add(String.valueOf(strs1.size()));
            for (String s : strs1)
                strs.add(s);
        }
        String[] res = new String[strs.size()];
        strs.toArray(res);
        return new State(res);
    }

    public static int[] code(Collection<int[]> src, int... oth) {
        List<Integer> list = new ArrayList<>();
        for (int in : oth)
            list.add(in);
        list.add(src.size());
        for (int[] p : src) {
            list.add(p.length);
            for (int i : p)
                list.add(i);
        }
        int[] a = new int[list.size()];
        int i = 0;
        for (Integer in : list)
            a[i++] = in;
        return a;
    }

    public static State codeIntArray(Collection<Integer> integers) {
        int[] a = new int[integers.size()];
        int i = 0;
        for (Integer in : integers) {
            if (i < a.length)
                a[i++] = in;
        }
        return new State(a);
    }

    public static State codeClientIdArray(Collection<BaseClient> clients) {
        String[] a = new String[clients.size()];
        int i = 0;
        for (BaseClient c : clients) {
            if (i < a.length)
                a[i++] = c.getID();
        }
        return new State(a);
    }

    public static State codeClientArray(Collection<BaseClient> clients) {
        State[] a = new State[clients.size()];
        int i = 0;
        for (BaseClient c : clients) {
            if (i < a.length)
                a[i++] = c.getState();
        }
        return State.code(a);
    }

    public static State codeClientIdArray(BaseClient... clients) {
        String[] a = new String[clients.length];
        int i = 0;
        for (BaseClient c : clients) {
            if (i < a.length)
                a[i++] = c.getID();
        }
        return new State(a);
    }

    public static BaseClient[] decodeClientIdArray(State state, Map<String, BaseClient> finder) {
        BaseClient[] a = new BaseClient[state.strs.length];
        int i = 0;
        for (String id : state.strs) {
            a[i++] = finder.get(id);
        }
        return a;
    }

    public static State codeClientArray(BaseClient... clients) {
        State[] a = new State[clients.length];
        int i = 0;
        for (BaseClient c : clients) {
            if (i < a.length)
                a[i++] = c.getState();
        }
        return State.code(a);
    }


    public static BaseClient[] decodeClientArray(State state) {
        State[] states = State.decodeToArray(state);
        BaseClient[] a = new BaseClient[states.length];
        int i = 0;
        for (State s : states) {
            a[i]= new BaseClient();
            a[i++].setState(s);
        }
        return a;
    }

    public static int decode(int[] des, int i, Collection<int[]> src) {
        if (!src.isEmpty())
            src.clear();
        int size = des[i++];
        for (int j = 0; j < size; j++) {
            int len = des[i++];
            int[] p = new int[len];
            for (int k = 0; k < len; k++)
                p[k] = des[i++];
            src.add(p);
        }
        return i;
    }

    public static State code(State... src) {
        List<String> strs = new ArrayList<>();
        strs.add(String.valueOf(src.length));
        for (State i : src) {
            List<String> strs1 = i.toStrArray();
            strs.add(String.valueOf(strs1.size()));
            for (String s : strs1)
                strs.add(s);
        }
        String[] res = new String[strs.size()];
        strs.toArray(res);
        return new State(res);
    }

    public static State code(List<State> src) {
        List<String> strs = new ArrayList<>();
        strs.add(String.valueOf(src.size()));
        for (State i : src) {
            List<String> strs1 = i.toStrArray();
            strs.add(String.valueOf(strs1.size()));
            for (String s : strs1)
                strs.add(s);
        }
        String[] res = new String[strs.size()];
        strs.toArray(res);
        return new State(res);
    }

    public static State[] decodeToArray(State state) {
        int i = 0, j, len, size;
        size = Integer.valueOf(state.strs[i++]);
        State[] src = new State[size];
        for (j = 0; j < size; j++) {
            len = Integer.valueOf(state.strs[i++]);
            String[] strs = new String[len];
            for (int k = 0; k < len; k++) {
                strs[k] = state.strs[i++];
            }
            State s = new State();
            if (s.loadStrs(strs))
                src[j] = s;
            else
                return null;
        }
        return src;
    }

    public static List<State> decodeToList(State state) {
        List<State> src = new ArrayList<State>();
        int i = 0, j, len, size;
        size = Integer.valueOf(state.strs[i++]);
        for (j = 0; j < size; j++) {
            len = Integer.valueOf(state.strs[i++]);
            String[] strs = new String[len];
            for (int k = 0; k < len; k++) {
                strs[k] = state.strs[i++];
            }
            State s = new State();
            if (s.loadStrs(strs))
                src.add(s);
            else
                return null;
        }
        return src;
    }

    public String[] toStrings() {
        List<String> all = toStrArray();
        String[] strs = new String[all.size()];
        all.toArray(strs);
        return strs;
    }

    public List<String> toStrArray() {
        List<String> all = new ArrayList<>();
        all.add(String.valueOf(syn));
        all.add(String.valueOf(classId));
        all.add(String.valueOf(methodId));
        all.add(String.valueOf(ints.length));
        for (int i : ints)
            all.add(String.valueOf(i));
        all.add(String.valueOf(strs.length));
        for (String i : strs)
            all.add(i);
        all.add(String.valueOf(bools.length));
        for (boolean i : bools)
            all.add(String.valueOf(i));
        return all;
    }

    public boolean loadStrs(String[] all) {
        try {
            if (all == null || all.length == 0)
                return false;
            int i = 0, j, len;
            syn = Integer.valueOf(all[i++]);
            classId = String.valueOf(all[i++]);
            methodId = Integer.valueOf(all[i++]);
            len = Integer.valueOf(all[i++]);
            ints = new int[len];
            for (j = 0; j < len; j++) {
                ints[j] = Integer.valueOf(all[i++]);
            }
            len = Integer.valueOf(all[i++]);
            strs = new String[len];
            for (j = 0; j < len; j++) {
                strs[j] = all[i++];
            }
            len = Integer.valueOf(all[i++]);
            bools = new boolean[len];
            for (j = 0; j < len; j++) {
                bools[j] = Boolean.valueOf(all[i++]);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Integer getInt() {
        if (ints.length == 0)
            return null;
        return ints[0];
    }

    public Integer getInt(int idx) {
        if (strs.length <= idx)
            return null;
        return ints[idx];
    }

    public String getString() {
        if (strs.length == 0)
            return null;
        return strs[0];
    }

    public String getString(int idx) {
        if (strs.length <= idx)
            return null;
        return strs[idx];
    }

    public Boolean getBoolean() {
        if (bools.length == 0)
            return null;
        return bools[0];
    }

    public Boolean getBoolean(int idx) {
        if (bools.length<=idx)
            return null;
        return bools[idx];
    }
    public void setInt(int i) {
        ints = new int[]{i};
    }

    public void setString(String s) {
        strs = new String[]{s};
    }

    public void setBoolean(boolean b) {
        bools = new boolean[]{b};
    }
}