package serverAndClient;

import java.util.Collection;

/**
 * Created by apple on 15-7-10.
 */
public class OprClientManager implements Synchronizer.SynObject {

    private ClientGroupManager clientManager;
    private Synchronizer synchronizer;

    public OprClientManager(Synchronizer synchronizer, ClientGroupManager manager) {
        this.clientManager = manager;
        this.synchronizer = synchronizer;
    }

    @Override
    public String getID() {
        return State.ClientManager;
    }

    public BaseClient getClient(State state, BaseClient src) {
        BaseClient client = clientManager.getClient(state.getString(0));
        if (client == null) {
            if (clientManager.isServer() || (src == clientManager.getUser() && src.notLogined()))
                client = src;
            else
                client = new BaseClient();
        }
        return client;
    }

    @Override
    public void operate(State state, BaseClient src) {
        BaseClient client;
        BaseGroup group;
        switch (state.getMethodId()) {
            case ClientGroupManager.Regist:
                client = getClient(state, src);
                clientManager.regist(client, state.getString(1), state.getString(2), state.getString(3));
                break;
            case ClientGroupManager.Login:
                client = getClient(state, src);
                clientManager.login(client, state.getString(1), state.getString(2));
                break;
            case ClientGroupManager.Visit:
                client = getClient(state, src);
                clientManager.visit(client);
                break;
            case ClientGroupManager.SetState:
                clientManager.setState(state);
                break;
            case ClientGroupManager.UpdateClient:
                State[] s = State.decodeToArray(state);
                client = clientManager.getClient(s[0].getString());
                client.setState(s[1]);
                clientManager.updateClient(client);
                break;
            case ClientGroupManager.Logout:
                client = clientManager.getClient(state.getString());
                clientManager.logout(client);
                break;
            case ClientGroupManager.SetPassoword:
                client = clientManager.getClient( state.getString(1));
                clientManager.setPassword(client,state.getString(1));
                break;
            case ClientGroupManager.CreateGroup:
                String id = state.getString();
                client = clientManager.getClient(id);
                group = clientManager.createGroup(client);
                break;
            case ClientGroupManager.EnterGroup:
                client = clientManager.getClient(state.getString(0));
                group = clientManager.getGroup(state.getString(1));
                clientManager.enterGroup(client, group);
                break;
            case ClientGroupManager.QuitGroup:
                client = clientManager.getClient(state.getString(0));
                group = clientManager.quitGroup(client);
                break;
            case ClientGroupManager.InviteIntoGroup:
                client = clientManager.getClient(state.getString(0));
                BaseClient client1 = clientManager.getClient(state.getString(1));
                if (client != null && client1 != null && !clientManager.hasGroup(client1))
                    this.inviteIntoGroup(client, client1, state);
                break;
            case ClientGroupManager.Agree:
                client = clientManager.getClient(state.getString(0));
                BaseClient client2 = clientManager.getClient(state.getString(1));
                clientManager.agreeRequest(client, client2);
                break;
            case ClientGroupManager.Refuse:
                client = clientManager.getClient(state.getString(0));
                BaseClient client3 = clientManager.getClient(state.getString(1));
                clientManager.refuseRequest(client, client3);
                break;
            case ClientGroupManager.Cancel:
                client = clientManager.getClient(state.getString(0));
                clientManager.cancelRequest(client);
                break;
            case ClientGroupManager.OverRequest:

                break;
            case ClientGroupManager.RequestGame:
                client = clientManager.getClient(state.getString(0));
                requestGame(client, state.getInt(), state);
                break;
        }
    }

    private void inviteIntoGroup(final BaseClient inviter, final BaseClient receiver, final State state) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (synchronizer) {
                    int opt = clientManager.inviteIntoGroup(inviter, receiver);
                    BaseClient user = clientManager.getUser();
                    if (user != null && !user.notLogined()) {
                        synchronizer.clientOperateSelf(getID(), opt, state);
                    }
                }
            }
        }).start();
    }

    private void requestGame(final BaseClient initor, final int gameType, final State state) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (synchronizer) {
                    int opt = clientManager.requestInGroup(initor, new ClientGroupManager.RequestExcuter() {
                        @Override
                        public String getReqStr() {
                            return "选择游戏";
                        }

                        @Override
                        public void doRequest() {
                            BaseGroup g = clientManager.getGroup(initor);
                            if (g != null)
                                g.setGameType(gameType);
                        }

                        @Override
                        public void onRequestOver() {

                        }
                    });

                    BaseClient user = clientManager.getUser();
                    if (user != null && !user.notLogined()) {
                        synchronizer.clientOperateSelf(getID(), opt, state);
                    }
                }
            }
        }).start();
    }

    @Override
    public Collection<BaseClient> getRelativeClients(BaseClient client, State state) {
        switch (state.getMethodId()) {
            case ClientGroupManager.SetState:
                return null;
        }
        return clientManager.getAllClients();
    }
}
