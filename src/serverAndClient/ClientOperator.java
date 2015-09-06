package serverAndClient;

import java.util.Collection;

import serverAndClient.OprGame.GameAdapter;
import serverAndClient.Synchronizer.SynAdapter;
import serverAndClient.Synchronizer.SynObject;

/**
 * Created by apple on 15-7-10.
 */
public class ClientOperator implements SynAdapter {

	private BaseClient user;
	private BaseServer server;
	private Synchronizer synchronizer;
	private ClientGroupManager clientManager;
	@SuppressWarnings("unused")
	private ChatManager chatManager;

	private OprClientManager oprClientManager;
	private OprChatManager oprChatManager;
	private OprGame oprGame;
	public ClientOperator(ClientGroupManager manager,ChatManager chatManager,GameAdapter adapter) {
		this.clientManager = manager;
		this.user = manager.getUser();
		synchronizer = new Synchronizer(this,new Synchronizer.SynClientAdapter() {
			@Override
			public void sendState(State state) {
				user.sendState(state);
			}

		});
		this.chatManager=chatManager;
		chatManager.setUser(user);
		oprClientManager = new OprClientManager(synchronizer,manager);
		oprChatManager = new OprChatManager(chatManager, clientManager);
		oprGame=new OprGame(clientManager,synchronizer,adapter);

		user.setClientMsgHandler(synchronizer);
	}
	public void setGameAdapter(GameAdapter adapter){
		oprGame.setAdapter(adapter);
	}
	public ClientOperator(ClientGroupManager manager, ChatManager chatManager,GameAdapter adapter,BaseServer s) {
		this.clientManager = manager;
		this.server = s;
		synchronizer = new Synchronizer(this,new Synchronizer.SynServerAdapter() {
			@Override
			public void sendState(State state, BaseClient receiver) {
				server.sendState(state, receiver);
			}

			@Override
			public void sendState(State state, Collection<BaseClient> receivers, BaseClient excepter) {
				server.sendState(state, receivers, excepter);
			}
		});
		this.chatManager = chatManager;

		oprClientManager = new OprClientManager(synchronizer,manager);
		oprChatManager = new OprChatManager(chatManager, clientManager);
		oprGame=new OprGame(clientManager,synchronizer,adapter);
		server.setServerMsgHandler(synchronizer);
	}
	public BaseClient getClient(String id){
		return clientManager.getClient(id);
	}
	public BaseGroup getGroup(String id){
		return clientManager.getGroup(id);
	}
	public BaseGroup getGroup(BaseClient client){
		return clientManager.getGroup(client);
	}
	public ClientGroupManager getClientManager(){
		return clientManager;
	}
	@Override
	public SynObject createSynObject(String classID, int methodId, State state) {
		if(State.ClientManager.equals(classID)){
			return oprClientManager;
		}else if(classID.indexOf(State.ChatManager)==0){
			return oprChatManager.createChat(methodId, state);
		}else if(classID.indexOf(State.GameManager)==0){

		}
		return null;
	}

	public void clear(){
		synchronizer.clear();
	}
	public void toOperateClientManager(int type, State state) {
		synchronizer.clientOperateSelf(State.ClientManager, type, state);
	}

	public void toRegist(String id, String password, String name) {
		toOperateClientManager(ClientGroupManager.Regist, new State(user.getID(), id, password, name));
	}

	public void toLogin(String id, String password) {
		toOperateClientManager(ClientGroupManager.Login, new State(user.getID(), id, password));
	}

	public void toVisit() {
		toOperateClientManager(ClientGroupManager.Visit, new State(user.getID()));
	}

	public void toUpdateClient() {
		State s = State.code(new State(user.getID()), user.getState());
		toOperateClientManager(ClientGroupManager.UpdateClient, s);
	}

	public void toLogout() {
		if(user.isRunning())
			toOperateClientManager(ClientGroupManager.Logout, new State(user.getID()));
		else 
			clientManager.logout(user);
	}
	public void toSetPassword(String pass){
		toOperateClientManager(ClientGroupManager.SetPassoword, new State(user.getID(),pass));
	}
	public void toCreateGroup() {
		toOperateClientManager(ClientGroupManager.CreateGroup, new State(user.getID()));
	}

	public void toEnterGroup( BaseGroup group) {
		toOperateClientManager(ClientGroupManager.EnterGroup, new State(user.getID(), group.getID()));
	}

	public void toQuitGroup() {
		if(clientManager.hasGroup(user))
			toOperateClientManager(ClientGroupManager.QuitGroup, new State(user.getID()));
	}

	public void toInvite(BaseClient receiver) {
		if(receiver!=null){
			BaseGroup g=clientManager.getGroup(user);
			if(g!=null && g.getHost()!=user)
				return;
			if(clientManager.hasGroup(receiver))
				return;
			toOperateClientManager(ClientGroupManager.InviteIntoGroup, 
					new State(user.getID(), receiver.getID()));
		}
	}
	public void toDriveAway(BaseClient lever){
		if(lever!=null){
			BaseGroup g=clientManager.getGroup(user);
			if(g==null || g.getHost()!=user)
				return;
			toOperateClientManager(ClientGroupManager.QuitGroup, new State(lever.getID()));
		}
	}
	public void toRequestGame(int gameType){
		State s=new State(user.getID());
		s.setInt(gameType);
		toOperateClientManager(ClientGroupManager.InviteIntoGroup, s);
	}
	public void toSetClientManager(BaseClient client){
		synchronizer.serverAcquireOperate(State.ClientManager, ClientGroupManager.SetState, 
				clientManager.getState(), client,false);
	}
	public void toSendClientLogout(BaseClient client){
		synchronizer.serverAcquireOperate(State.ClientManager, ClientGroupManager.Logout, 
				new State(client.getID()), null, true);
	}

	public void toOperateChatManager(BaseClient client, int method,State state) {
		synchronizer.clientOperateSelf(oprChatManager.transformID(client), method, state);
	}

	public void toOperateChatManager(BaseGroup group,int method,State state) {
		synchronizer.clientOperateSelf(oprChatManager.transformID(group), method, state);
	}
	public void toTalkInGroup(String words){
		toOperateChatManager(clientManager.getGroup(user),ChatManager.TalkInGroup,new State(words,user.getID()));
	}
	public void toTalkInWorld(String words){
		toOperateChatManager(user,ChatManager.TalkInWorld,new State(words,user.getID()));
	}
	public void toTalkToOther(String words,BaseClient other){
		if(other!=null)
			toOperateChatManager(user,ChatManager.TalkToOther,new State(words,user.getID(),other.getID()));
	}

	public void toSendServerMsgInWorld(String words,BaseClient client){
		if(client!=null)
			synchronizer.serverAcquireOperate(State.ChatManager, ChatManager.ServerMsgInWord, 
					new State(words), client,true);
	}

	public void toSendServerMsgInGroup(String words,BaseGroup group){
		if(group!=null)
			synchronizer.serverAcquireOperate(State.ChatManager, ChatManager.ServerMsgInGroup, 
					new State(words,group.getID()),group.getHost(),true);
	}

	public void toSendServerMsgToClient(String words,BaseClient client){ 
		if(client!=null)
			synchronizer.serverAcquireOperate(State.ChatManager, ChatManager.ServerMsgToClient, 
					new State(words,client.getID()), client,true);
	}

	public void toUpdatePlayerState(String id,State state){
		oprGame.toUpdatePlayerState(id, state);
	}
	public void toRequest(String req,String initor,String...receivers){
		oprGame.toRequest(req, initor, receivers);
	}
	public void toInitGame(){
		oprGame.toInitGame();
	}
	public void toPlayerInput(String id, int code){
		oprGame.toPlayerInput(id, code);
	}
	public void toLockAllPlayers(){
		oprGame.toLockAllPlayers();
	}
	public boolean isGroupHost(String id){
		BaseClient client=clientManager.getClient(id);
		BaseGroup g=clientManager.getGroup(client);
		client=g.getHost();
		return (client!=null && client.getID().equals(id));
	}
}
