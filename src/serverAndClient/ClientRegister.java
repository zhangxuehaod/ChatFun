package serverAndClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import serverAndClient.BaseClient.DisconnectListener;


public class ClientRegister implements StateObject {
    public static final int
            Login_Succeed = 1, Login_No_Id = 2, Login_Pass_Wrong = 3,
            Regist_ID_Exist = 4, Pass_Format_Wrong = 5, Name_Format_Wrong = 6, Id_Format_Wrong = 7,
            Regist_Succeed = 8, AlreadyLogined = 9, VisitSuccess = 10;
    private FileSaver inforSaver = new FileSaver("clientRegistInfors.txt");

    private IdCreator visitorID = new IdCreator(false);
    private Map<String, ClientInfor> inforRecoder = new HashMap<String, ClientInfor>();
    private Map<String, BaseClient> onlineClients = new HashMap<String, BaseClient>();
    private BaseClient user = null;
    private final boolean isServer;
    private HashSet<String> avoidLogout = new HashSet<>();

    public ClientRegister(BaseClient user, boolean isServer) {
        this.user = user;
        this.isServer = isServer;
    }

    public boolean isServer() {
        return this.isServer;
    }

    public static boolean isStringFormatWrong(String str) {
        if (str == null || str.length() == 0 || " ".equals(str))
            return true;
        return false;
    }

    public String getTip(int id) {
        switch (id) {
            case Regist_ID_Exist:
                return "用户名已存在";
            case Id_Format_Wrong:
                return "用户名格式错误";
            case Name_Format_Wrong:
                return "昵称格式错误";
            case Pass_Format_Wrong:
                return "密码格式错误";
            case AlreadyLogined:
                return "该账号已登录";
            case Login_No_Id:
                return "用户名不存在";
            case Login_Pass_Wrong:
                return "密码格式错误";
            case VisitSuccess:
                return "游客登录成功";
            case Regist_Succeed:
                return "注册成功";
            case Login_Succeed:
                return "登录成功";
        }
        return "错误";
    }

    public synchronized int regist(BaseClient client, String id, String password, String name) {
        int resCode;
        if (inforRecoder.containsKey(id))
            resCode = Regist_ID_Exist;
        else if (isStringFormatWrong(id))
            resCode = Id_Format_Wrong;
        else if (isStringFormatWrong(name))
            resCode = Name_Format_Wrong;
        else if (isStringFormatWrong(password))
            resCode = Pass_Format_Wrong;
        else if (id.equals(client.getID()))
            resCode = AlreadyLogined;
        else {
            client.setID(id);
            client.setName(name);
            id = client.getID();
            name = client.getName();
            this.inforRecoder.put(id, new ClientInfor(id, password, name));
            online(client);
            resCode = Regist_Succeed;
        }
        if (user == client) {
            for (LoginListener l : listeners)
                l.onUserOprResult(getTip(resCode));
        }
        return resCode;
    }

    private void checkLoginer(BaseClient client) {
        String id = client.getID();
        if (onlineClients.containsKey(id)) {
            this.updateClientInfor(client);
            if (client == user || isServer) {
                this.onlineClients.remove(id);
                client.closeButNotDisconnect();
                for (LoginListener l : listeners)
                    l.onClientOffline(client);
            } else {
                offline(client);
            }
        }
    }

    protected void replaceClient(BaseClient newClient, BaseClient oldClient) {
        newClient.copyDataOf(oldClient);
    }

    public synchronized int login(BaseClient client, String id, String password) {
        int resCode;
        if (isStringFormatWrong(id))
            resCode = Id_Format_Wrong;
        else if (isStringFormatWrong(password))
            resCode = Pass_Format_Wrong;
        else if (id.equals(client.getID()) && isOnline(id))
            resCode = AlreadyLogined;
        else if (inforRecoder.containsKey(id)) {
            ClientInfor inf = inforRecoder.get(id);
            if (inf.isPassRight(password)) {
                checkLoginer(client);
                BaseClient other = onlineClients.get(id);
                if (other != null) {
                    if (other == user) {
                        offline(other);
                        return Login_Succeed;
                    } else {
                        avoidLogout.add(id);
                        //replaceClient(client,other);
                        updateClientInfor(other);
                        offline(other);
                    }
                }
                client.setID(id);
                client.setName(inf.getName());
                client.setMyState(inf.getMyState());
                online(client);
                resCode = Login_Succeed;
            } else {
                resCode = Login_Pass_Wrong;
            }
        } else {
            resCode = Login_No_Id;
        }
        if (user == client) {
            for (LoginListener l : listeners)
                l.onUserOprResult(getTip(resCode));
        }
        return resCode;
    }

    public synchronized BaseClient visit(BaseClient client) {
        String id;
        do {
            id = visitorID.createID().toString();
        } while (inforRecoder.containsKey(id));
        checkLoginer(client);
        client.setID(id);
        client.setName("游客" + id);
        online(client);
        if (user == client) {
            for (LoginListener l : listeners)
                l.onUserOprResult(getTip(VisitSuccess));
        }
        return client;
    }

    public synchronized void logout(BaseClient client) {
        String id = client.getID();
        if (!onlineClients.containsKey(id))
            return;
        if (avoidLogout.contains(id)) {
            avoidLogout.remove(id);
        } else {
            updateClientInfor(client);
            offline(client);
        }
    }
    public synchronized void setPassword(BaseClient client,String pass){
        if(isStringFormatWrong(pass))
            return;
        ClientInfor infor;
        infor = this.inforRecoder.get(client.getID());
        if (infor != null) {
            infor.setPassword(pass);
        }
    }
    public synchronized void updateClient(BaseClient client) {
        updateClientInfor(client);
    }

    private void updateClientInfor(BaseClient client) {
        ClientInfor infor;
        infor = this.inforRecoder.get(client.getID());
        if (infor != null) {
            infor.setName(client.getName());
            infor.setMyState(client.getMyState());
            for (LoginListener l : listeners)
                l.onUpdateClient(client);
        }
    }

    public BaseClient getUser() {
        return user;
    }

    public Map<String, BaseClient> getClientFinder() {
        return onlineClients;
    }

    public synchronized Collection<BaseClient> getAllClients() {
        return new HashSet<BaseClient>(onlineClients.values());
    }

    public synchronized BaseClient getClient(String id) {
        return onlineClients.get(id);
    }

    public synchronized boolean isOnline(String id) {
        return onlineClients.containsKey(id);
    }

    public synchronized boolean isOnline(BaseClient client) {
        if (client == null)
            return false;
        return onlineClients.containsKey(client.getID());
    }

    private void online(BaseClient... clients) {
        synchronized (onlineClients) {
            for (BaseClient c : clients) {
                c.addDisconnecter(loginDisconnecter);
                onlineClients.put(c.getID(), c);
                c.startRun();
            }
        }
        for (LoginListener l : listeners)
            l.onClientOnline(clients);
    }

    private void offline(BaseClient client) {
        synchronized (onlineClients) {
            onlineClients.remove(client.getID());
        }
        for (LoginListener l : listeners)
            l.onClientOffline(client);
        client.disconnect();
    }

    public synchronized void clearOnlineClients() {
        for (BaseClient c : onlineClients.values()) {
            updateClientInfor(c);
            for (LoginListener l : listeners)
                l.onClientOffline(c);
        }
        onlineClients.clear();
    }

    public boolean saveIntoFile() {
        return inforSaver.saveInfors(this.getState().toStrings());
    }

    public boolean loadFromFile() {
        String[] infs = inforSaver.loadInfors();
        if (infs == null)
            return false;
        State s = new State();
        s.loadStrs(infs);
        this.setState(s);
        return true;
    }

    @Override
    public synchronized State getState() {
        List<State> list = new ArrayList<>();
        list.add(visitorID.getState());
        List<State> list1 = new ArrayList<>();
        for (ClientInfor inf : inforRecoder.values())
            list1.add(inf.getState());
        list.add(State.code(list1));
        list.add(State.codeClientArray(onlineClients.values()));
        list.add(new State(avoidLogout));
        return State.code(list);
    }

    @Override
    public synchronized void setState(State state) {
        List<State> list = State.decodeToList(state);
        State s = list.get(0);
        visitorID.setState(s);
        inforRecoder.clear();
        s = list.get(1);
        List<State> list1 = State.decodeToList(s);
        for (State t : list1) {
            ClientInfor inf = new ClientInfor();
            inf.setState(t);
            inforRecoder.put(inf.getId(), inf);
        }
        onlineClients.clear();
        s = list.get(2);
        BaseClient[] clients = State.decodeClientArray(s);
        online(clients);
        s = list.get(3);
        avoidLogout.clear();
        for (String str : s.strs)
            avoidLogout.add(str);
    }

    public interface LoginListener {
        public void onClientOnline(BaseClient... client);

        public void onClientOffline(BaseClient client);

        public void onUpdateClient(BaseClient client);

        public void onUserOprResult(String tip);
    }

    private Set<LoginListener> listeners = new HashSet<>();

    public void addLoginListener(LoginListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeLoginListener(LoginListener listener) {
        listeners.remove(listener);
    }

    private class ClientInfor implements StateObject {
        private String id;
        private String name;
        private String password;
        private State state = new State();

        protected ClientInfor() {
        }

        public ClientInfor(String id, String password, String name) {
            this.setId(id);
            this.setName(name);
            this.setPassword(password);
        }

        public boolean isPassRight(String password) {
            if (this.password == null)
                return false;
            return this.password.equals(password);
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @SuppressWarnings("unused")
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }


        public void setMyState(State state) {
            this.state = state;
        }

        public State getMyState() {
            return state;
        }

        @Override
        public State getState() {
            return State.code(new State(id, name, password), state);
        }

        @Override
        public void setState(State s) {
            List<State> list = State.decodeToList(s);
            s = list.get(0);
            id = s.strs[0];
            name = s.strs[1];
            password = s.strs[2];
            state = list.get(1);
        }
    }

    private DisconnectListener loginDisconnecter = new DisconnectListener() {
        @Override
        public void onDisconnect(BaseClient client) {
            if (isOnline(client))
                logout(client);
        }
    };

}
