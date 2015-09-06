package serverAndClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by apple on 15-7-10.
 */
public class BaseGroup implements StateObject {

    private List<BaseClient> clients = new ArrayList<BaseClient>();
    private String ID;
    private int gameType;
    private Map<String,BaseClient> clientFinder;

    protected BaseGroup(){
    }
    protected BaseGroup(Map<String,BaseClient> clientFinder) {
        this.clientFinder = clientFinder;
    }
    protected void replace(BaseClient newClient,BaseClient oldClient){
    	clients.remove(oldClient);
    	clients.add(newClient);
    }
    protected void addClient(BaseClient client) {
        if (client == null)
            return;
        if(!clients.contains(client))
            clients.add(client);
    }

    protected void removeClient(BaseClient client) {
        if (client == null || !clients.contains(client))
            return;
        clients.remove(client);
    }
    public boolean contains(BaseClient client){
        return clients.contains(client);
    }
    protected void clear() {
        clients.clear();
    }
    public int size(){
        return clients.size();
    }
    public boolean isEmpty(){
        return clients.isEmpty();
    }
    protected void destroy() {
        clear();
        ID = null;
    }
    public BaseClient getHost(){
    	if(clients.isEmpty())
    		return null;
    	return clients.get(0);
    }
    public List<BaseClient> getClients() {
        return clients;
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public String getID() {
        return ID;
    }

    protected void setID(String ID) {
        this.ID = ID;
    }
    public String toString(){
        return "Group"+ID+"("+clients.size()+")";
    }
    @Override
    public State getState() {
        State s = new State();
        List<String> list = new ArrayList<>();
        s.setState(ID);
        s.setState(gameType);
        for (BaseClient c : clients)
            list.add(c.getID());
        return State.code(s, new State(list));
    }

    @Override
    public void setState(State state) {
        List<State> s = State.decodeToList(state);
        State s1 = s.get(0);
        setID(s1.strs[0]);
        setGameType(s1.ints[0]);
        s1 = s.get(1);
        for (String id : s1.strs)
            addClient(clientFinder.get(id));

    }
    public boolean compareTo(BaseGroup oth){
        return ID.compareTo(oth.ID)>=0;
    }
}
