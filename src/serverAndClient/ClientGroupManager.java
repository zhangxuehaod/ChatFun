package serverAndClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import serverAndClient.BaseClient.DisconnectListener;

/**
 * Created by apple on 15-7-12.
 */
public class ClientGroupManager extends ClientRegister {
    public static final int
            SetState = 0, Regist = 1, Login = 2, Visit = 3, UpdateClient = 4,
            CreateGroup = 5, EnterGroup = 6, QuitGroup = 7, Logout = 8,
            InviteIntoGroup = 9, Cancel = 10, Agree = 11, OverRequest = 12,
            Refuse = 13, RequestGame = 14,SetPassoword=15;

    private IdCreator groupID = new IdCreator();
    private Map<String, BaseGroup> groupFinder = new HashMap<String, BaseGroup>();
    private Map<String, BaseGroup> groupClientFinder = new HashMap<String, BaseGroup>();
    private Map<BaseClient, Requestion> requestMap = new HashMap<>();

    private HashSet<GroupListener> groupListeners = new HashSet<>();

    public ClientGroupManager(BaseClient user, boolean isServer) {
        super(user, isServer);
    }

    @Override
    protected void replaceClient(BaseClient newClient, BaseClient oldClient) {
        super.replaceClient(newClient, oldClient);
        BaseGroup g = groupClientFinder.get(oldClient.getID());
        if (g != null) {
            g.replace(newClient, oldClient);
            for (GroupListener l : groupListeners)
                l.onEnterGroup(newClient, g);
        }
        Requestion req = requestMap.get(oldClient);
        if (req != null) {
            req.replace(newClient, oldClient);
        }
    }

    public BaseGroup createGroup(BaseClient... clients) {
        for (BaseClient c : clients)
            if (c == null || hasGroup(c))
                return null;
        BaseGroup g = new BaseGroup(getClientFinder());
        synchronized (groupID) {
            g.setID(groupID.createIdStr());
            groupFinder.put(g.getID(), g);
        }
        synchronized (groupClientFinder) {
            for (BaseClient c : clients) {
                c.addDisconnecter(groupDisconnecter);
                groupClientFinder.put(c.getID(), g);
                g.addClient(c);
            }
        }
        for (GroupListener l : groupListeners)
            l.onAddGroup(g);
        return g;
    }

    public void enterGroup(BaseClient client, BaseGroup group) {
        if (group != null && client != null && !hasGroup(client)) {
            client.addDisconnecter(groupDisconnecter);
            synchronized (groupClientFinder) {
                groupClientFinder.put(client.getID(), group);
            }
            group.addClient(client);
            for (GroupListener l : groupListeners)
                l.onEnterGroup(client, group);
        }
    }

    public BaseGroup quitGroup(BaseClient client) {
        BaseGroup g = getGroup(client);
        if (g != null) {
            g.removeClient(client);
            synchronized (groupClientFinder) {
                groupClientFinder.remove(client.getID());
            }
            for (GroupListener l : groupListeners)
                l.onQuitGroup(client, g);
            if (g.isEmpty())
                this.destroyGroup(g);
            client.removeDisconnecter(groupDisconnecter);
        }
        return g;
    }

    public void destroyGroup(BaseGroup group) {
        String ID = group.getID();
        synchronized (groupFinder) {
            if (groupFinder.containsKey(ID)) {
                groupFinder.remove(ID);
                groupID.recycleID(ID);
                for (BaseClient c : group.getClients()) {
                    groupClientFinder.remove(c.getID());
                    c.removeDisconnecter(groupDisconnecter);
                }
                for (GroupListener l : groupListeners)
                    l.onRemoveGroup(group);
            }
        }
    }

    private void addGroup(BaseGroup g) {
        groupFinder.put(g.getID(), g);
        for (BaseClient c : g.getClients()) {
            c.addDisconnecter(groupDisconnecter);
            groupClientFinder.put(c.getID(), g);
        }
        for (GroupListener l : groupListeners)
            l.onAddGroup(g);
    }

    public int inviteIntoGroup(BaseClient inviter, BaseClient... receiver) {
        return new Invitation(inviter, receiver).toRequest();
    }

    public int agreeRequest(BaseClient inviter, BaseClient accepter) {
        Requestion iv = requestMap.get(inviter);
        if (iv == null)
            return Cancel;
        iv.agree(accepter);
        return OverRequest;
    }

    public int refuseRequest(BaseClient inviter, BaseClient refuser) {
        Requestion iv = requestMap.get(inviter);
        if (iv != null)
            iv.refuse(refuser);
        return Refuse;
    }

    public int cancelRequest(BaseClient canceler) {
        Requestion iv = requestMap.get(canceler);
        if (iv != null) {
            iv.cancel(canceler);
        }
        return Cancel;
    }

    public int overRequest(BaseClient client) {
        Requestion iv = requestMap.get(client);
        if (iv != null) {
            iv.overRequest(client);
        }
        return Cancel;
    }

    public int requestInGroup(BaseClient client, RequestExcuter excuter) {
        BaseGroup g = getGroup(client);
        if (g != null)
            return new Requestion(excuter, client, g.getClients()).toRequest();
        return -1;
    }

    public int requestInGroup(BaseClient client, BaseClient[] other, RequestExcuter excuter) {
        return new Requestion(excuter, client, other).toRequest();
    }

    public int requestToOther(BaseClient client, BaseClient other, RequestExcuter excuter) {
        BaseGroup g = getGroup(client);
        if (g != null)
            return new Requestion(excuter, client, g.getClients()).toRequest();
        return -1;
    }

    public BaseGroup getGroup(String groupID) {
        return groupFinder.get(groupID);
    }

    public BaseGroup getGroup(BaseClient client) {
        if (client == null)
            return null;
        String id = client.getID();
        synchronized (groupClientFinder) {
            return groupClientFinder.get(id);
        }
    }

    public boolean hasGroup(BaseClient client) {
        synchronized (groupClientFinder) {
            return groupClientFinder.containsKey(client.getID());
        }
    }

    public Collection<BaseGroup> getAllGroups() {
        synchronized (groupClientFinder) {
            return new HashSet<BaseGroup>(groupFinder.values());
        }
    }

    public void clear() {
        super.clearOnlineClients();
        groupID.clear();
        groupFinder.clear();
        groupClientFinder.clear();
        requestMap.clear();
    }

    @Override
    public State getState() {
        List<State> list = new ArrayList<>();
        list.add(super.getState());

        List<State> tmp = new ArrayList<>();
        for (BaseGroup g : groupFinder.values())
            tmp.add(g.getState());
        list.add(State.code(tmp));
        list.add(groupID.getState());

        tmp.clear();
        for (Requestion inv : requestMap.values()) {
            tmp.add(inv.getState());
        }
        list.add(State.code(tmp));
        return State.code(list);
    }

    @Override
    public void setState(State s) {
        clear();
        List<State> list = State.decodeToList(s);
        int i = 0;
        if (list.isEmpty())
            return;
        super.setState(list.get(i++));

        State t = list.get(i++);
        List<State> tmp = State.decodeToList(t);
        for (State st : tmp) {
            BaseGroup g = new BaseGroup(getClientFinder());
            g.setState(st);
            this.addGroup(g);
        }
        groupID.setState(list.get(i++));
        t = list.get(i++);
        tmp = State.decodeToList(t);
        for (State st : tmp) {
            Requestion iv = new Requestion();
            iv.setState(st);
            if (iv.getReqType() == InviteIntoGroup)
                iv = new Invitation(iv);
            iv.init();
        }
    }

    private DisconnectListener groupDisconnecter = new DisconnectListener() {
        @Override
        public void onDisconnect(BaseClient client) {
            quitGroup(client);
        }
    };

    public interface GroupListener {
        public void onAddGroup(BaseGroup group);

        public void onRemoveGroup(BaseGroup group);

        public void onEnterGroup(BaseClient client, BaseGroup group);

        public void onQuitGroup(BaseClient client, BaseGroup group);
    }


    public void addGroupListener(GroupListener listener) {
        if (!groupListeners.contains(listener))
            groupListeners.add(listener);
    }

    public void removeGroupListener(GroupListener listener) {
        groupListeners.remove(listener);
    }

    public interface DialogAdapter {
        public int showDialog(String title, String content, String[] btns, int limitTime);

        public void closeDialog();
    }

    public DialogAdapter dialogAdapter;

    public void setDialogAdapter(DialogAdapter adapter) {
        this.dialogAdapter = adapter;
    }

    protected class Invitation extends Requestion {

        protected Invitation() {
        }

        public Invitation(BaseClient initor, BaseClient... receiver) {
            super(InviteIntoGroup, initor, receiver);
        }

        protected Invitation(Requestion req) {
            this.isCanceld = req.isCanceld;
            this.initor = req.initor;
            this.voters = req.voters;
            this.req = req.req;
            this.all = req.all;
            this.hasUser = req.hasUser;
        }

        @Override
        public boolean isEnable() {
            if (super.isEnable()) {
                for (BaseClient c : voters)
                    if (hasGroup(c))
                        return false;
            }
            return true;
        }

        @Override
        public String getReqStr(int reqType) {
            return "组群邀请";
        }

        @Override
        public void doRequest() {
            if (isAllAgree()) {
                BaseGroup g = getGroup(initor);
                if (g == null) {
                    BaseClient[] clients = new BaseClient[all.size()];
                    all.toArray(clients);
                    createGroup(clients);
                } else {
                    all.remove(initor);
                    for (BaseClient c : all)
                        enterGroup(c, g);
                }
            }
        }
    }

    public class Requestion implements BaseClient.DisconnectListener, StateObject {
        protected BaseClient initor;
        protected HashSet<BaseClient> all = new HashSet<>();
        protected HashSet<BaseClient> voters = new HashSet<>();
        protected int req;
        protected boolean isCanceld = false;
        protected boolean hasUser = false;
        private RequestExcuter excuter;

        protected Requestion() {
        }

        public Requestion(int reqType, BaseClient initor, BaseClient... voters) {
            this.req = reqType;
            this.initor = initor;
            for (BaseClient c : voters)
                this.voters.add(c);
            all.add(initor);
            all.addAll(this.voters);
        }

        public Requestion(RequestExcuter excuter, BaseClient initor, Collection<BaseClient> voters) {
            this.initor = initor;
            this.voters.addAll(voters);
            this.excuter = excuter;
            all.add(initor);
            all.addAll(this.voters);
        }

        public Requestion(RequestExcuter excuter, BaseClient initor, BaseClient... voters) {
            this.initor = initor;
            for (BaseClient c : voters)
                this.voters.add(c);
            this.excuter = excuter;
            all.add(initor);
            all.addAll(this.voters);
        }

        public void replace(BaseClient newClient, BaseClient oldClient) {
            if (initor == oldClient)
                initor = newClient;
            if (all.contains(oldClient)) {
                all.remove(oldClient);
                all.add(newClient);
            }
            if (voters.contains(oldClient)) {
                voters.remove(oldClient);
                voters.add(newClient);
            }
        }

        public void setExcuter(RequestExcuter excuter) {
            this.excuter = excuter;
        }

        public int getReqType() {
            return req;
        }

        public boolean isEnable() {
            if (voters.isEmpty())
                return false;
            for (BaseClient c : all) {
                if (c == null || !isOnline(c))
                    return false;
                else if (requestMap.containsKey(c))
                    return false;
            }
            return true;
        }

        public int init() {
            if (!isEnable())
                return -1;
            for (BaseClient c : all) {
                c.addDisconnecter(this);
                requestMap.put(c, this);
            }
            return 0;
        }

        public String getReqStr(int reqType) {
            if (excuter != null)
                return excuter.getReqStr();
            return "" + reqType;
        }

        public String getDescription() {
            String des = null;
            for (BaseClient c : voters) {
                if (des == null)
                    des = c.toString();
                else
                    des += " " + c;
            }
            return "请求：" + getReqStr(req) + "\n发起者：" + initor + "\n接收者：" + des + "\n";
        }

        public int toRequest() {
            if (init() == 0 && !isServer()) {
                BaseClient user = getUser();
                int opt = -1;
                if (initor == user) {
                    hasUser = true;
                    opt = dialogAdapter.showDialog("等待同意", getDescription(), new String[]{"取消请求"}, 30);
                    if (opt == 0)
                        opt = Cancel;
                    else
                        opt = OverRequest;
                } else if (voters.contains(user)) {
                    hasUser = true;
                    opt = dialogAdapter.showDialog("请求", getDescription(), new String[]{"同意", "拒绝"}, 30);
                    if (opt == 0)
                        opt = Agree;
                    else
                        opt = Refuse;
                }
                return opt;
            }
            return -1;
        }

        public synchronized int agree(BaseClient client) {
            if (voters.contains(client)) {
                voters.remove(client);
                if (hasUser && requestListener != null)
                    requestListener.onAgreeRequest(client);
                if (voters.isEmpty()) {
                    doRequest();
                    clear();
                    if (hasUser && requestListener != null)
                        requestListener.onRequestDone(initor, req);
                }
                return Agree;
            }
            return -1;
        }

        public synchronized int refuse(BaseClient client) {
            if (voters.contains(client)) {
                isCanceld = true;
                if (hasUser && requestListener != null) {
                    requestListener.onRefuseRequest(client);
                    requestListener.onRequestCancel(initor, req);
                }
                clear();
                return Refuse;
            }
            return -1;
        }

        public synchronized int overRequest(BaseClient client) {
            if (initor == client) {
                clear();
                return OverRequest;
            }
            return -1;
        }

        public synchronized int cancel(BaseClient client) {
            if (initor == client) {
                isCanceld = true;
                clear();
                if (hasUser && requestListener != null)
                    requestListener.onRequestCancel(initor, req);
                return Cancel;
            }
            return -1;
        }

        public synchronized boolean isAllAgree() {
            return voters.isEmpty() && !isCanceld;
        }

        public boolean isCancel() {
            return isCanceld;
        }


        public void doRequest() {
            if (excuter != null)
                excuter.doRequest();
        }

        private void clear() {
            if (hasUser) {
                hasUser = false;
                dialogAdapter.closeDialog();
            }
            for (BaseClient c : all) {
                requestMap.remove(c);
                c.removeDisconnecter(this);
            }
            voters.clear();
            all.clear();
            initor = null;
            if (excuter != null)
                excuter.onRequestOver();
        }

        @Override
        public void onDisconnect(BaseClient client) {
            if (initor == client || voters.contains(client)) {
                isCanceld = true;
                if (hasUser && requestListener != null)
                    requestListener.onRequestCancel(initor, req);
                clear();
            }
        }

        @Override
        public State getState() {
            List<String> ids = new ArrayList<>();
            if (initor == null)
                ids.add("");
            else
                ids.add(initor.getID());
            for (BaseClient c : voters)
                ids.add(c.getID());
            State s = new State(ids);
            s.setState(isCanceld, hasUser);
            s.setState(req);
            return State.code(s, State.codeClientIdArray(all));
        }

        @Override
        public void setState(State state) {
            State[] s = State.decodeToArray(state);
            state = s[0];
            int i = 0;
            initor = getClient(state.strs[i++]);
            voters.clear();
            for (; i < state.strs.length; i++)
                voters.add(getClient(state.strs[i++]));
            isCanceld = state.getBoolean(0);
            hasUser = state.getBoolean(1);
            req = state.getInt();
            state = s[1];
            BaseClient[] clients = State.decodeClientIdArray(state, getClientFinder());
            for (BaseClient c : clients)
                all.add(c);
        }
    }

    public interface RequestExcuter {
        public String getReqStr();

        public void doRequest();

        public void onRequestOver();

    }

    public interface RequestListener {
        public void onRequestCancel(BaseClient initor, int req);

        public void onRequestDone(BaseClient initor, int req);

        public void onAgreeRequest(BaseClient client);

        public void onRefuseRequest(BaseClient client);
    }

    private RequestListener requestListener;

    public void setRequestListener(RequestListener listener) {
        this.requestListener = listener;
    }
}
