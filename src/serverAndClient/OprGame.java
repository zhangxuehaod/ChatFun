package serverAndClient;

import java.util.Collection;

public class OprGame implements Synchronizer.SynObject  {
	public static final int SetGameState=1,PlayerInput=2,LockAllPlayers=3,Request=4
			,Agree=5,Refuse=6,Cancel=7,OverRequest=8,UpdatePlayer=9;
	private ClientGroupManager manager;
	private GameAdapter adapter;
	private BaseGroup group;
	private String classID;
	private boolean isRequest=false;
	private Synchronizer synchronizer;
	public OprGame(ClientGroupManager manager,Synchronizer synchronizer,GameAdapter adapter){
		this.adapter=adapter;
		this.manager=manager;
		this.classID=State.GameManager;
		this.synchronizer=synchronizer;
	}
	public void setAdapter(GameAdapter adapter){
		this.adapter=adapter;
	}
	public OprGame(BaseGroup group,ClientGroupManager manager,Synchronizer synchronizer,GameAdapter adapter){
		this.adapter=adapter;
		this.manager=manager;
		this.group=group;
		this.classID=State.GameManager+group.getID();
		this.synchronizer=synchronizer;
	}
	
	public OprGame createGame(int methodId, State state){
		BaseClient client;
		switch(state.getMethodId()){
		case PlayerInput:
		case UpdatePlayer:{
			State[] s=State.decodeToArray(state);
			client=manager.getClient(s[0].getString());
			BaseGroup g=manager.getGroup(client);
			return new OprGame(g,manager,synchronizer,adapter);
		}
		case Request:
			State[] s=State.decodeToArray(state);
			BaseClient initor=manager.getClient(s[0].getString(1));
			BaseGroup g=manager.getGroup(initor);
			return new OprGame(g,manager,synchronizer,adapter);
		case SetGameState:
		case LockAllPlayers:
		case Agree:
		case Refuse:
		case Cancel:
		case OverRequest:
			return this;
		}
		return null;
	}
	
	public void toUpdatePlayerState(String id,State state){
		State s=State.code(new State(id),state);
		synchronizer.clientOperateSelf(getID(), UpdatePlayer, s);
	}
	public void toRequest(String req,String initor,String...receivers){
		State state=State.code(new State(req,initor),new State(receivers));
		synchronizer.clientOperateSelf(getID(), Request, state);
	}
	public void toInitGame(){
		State state=adapter.createGameState();
		synchronizer.clientOperateGroup(getID(), SetGameState, state);
	}
	public void toPlayerInput(String id, int code){
		State state=new State(id,code);
		synchronizer.clientOperateGroup(getID(), PlayerInput, state);
	}
	public void toLockAllPlayers(){
		synchronizer.clientOperateGroup(getID(), LockAllPlayers, new State());
	}

	@Override
	public String getID() {
		return classID;
	}

	@Override
	public void operate(State state, BaseClient src) {
		BaseClient client;
		switch(state.getMethodId()){
		case UpdatePlayer:{
			State[] s=State.decodeToArray(state);
			adapter.updatePlayerState(s[0].getString(), s[1]);
		}
			break;
		case SetGameState:
			adapter.setGameState(state);
			break;
		case PlayerInput:
			adapter.playerInput(state.getString(), state.getInt());
			break;
		case LockAllPlayers:
			adapter.lockAllPlayers();
			break;
		case Request:
			State[] s=State.decodeToArray(state);
			String req=s[0].getString(0);
			BaseClient initor=manager.getClient(s[0].getString(1));
			BaseClient[] recvs=new BaseClient[s[1].strs.length];
			for(int i=0;i<recvs.length;i++)
				recvs[i]=manager.getClient(s[1].strs[i]);
			this.request(req, initor, recvs);
			break;
		case Agree:
			client = manager.getClient(state.getString(0));
			BaseClient client2 = manager.getClient(state.getString(1));
			manager.agreeRequest(client, client2);
			break;
		case Refuse:
			client = manager.getClient(state.getString(0));
			BaseClient client3 = manager.getClient(state.getString(1));
			manager.refuseRequest(client, client3);
			break;
		case Cancel:
			client = manager.getClient(state.getString(0));
			manager.cancelRequest(client);
			break;
		case OverRequest:
			break;
		}
	}
	private int transform(int opt){
		switch(opt){
		case ClientGroupManager.Agree:
			return Agree;
		case ClientGroupManager.Refuse:
			return Refuse;
		case ClientGroupManager.Cancel:
			return Cancel;
		case ClientGroupManager.OverRequest:
			return OverRequest;
		}
		return -1;
	}
	
	private void request(final String req,final BaseClient initor,final BaseClient[] receivers){
		new Thread(new Runnable(){
			@Override
			public void run() {
				synchronized(synchronizer){
					if(isRequest)
						return;
					isRequest=true;
					int opt=manager.requestInGroup(initor, receivers,new ClientGroupManager.RequestExcuter() {
						@Override
						public String getReqStr() {
							return req;
						}
						@Override
						public void doRequest() {
							if(initor==manager.getUser()){
								toInitGame();
							}
						}
						@Override
						public void onRequestOver() {
							isRequest=false;
						}
					});
					opt=transform(opt);
					BaseClient user=manager.getUser();
					if(user!=null && !user.notLogined()){
						synchronizer.clientOperateSelf(getID(), opt, new State(initor.getID(),user.getID()));
					}
				}
			}
		}).start();
	}
	@Override
	public Collection<BaseClient> getRelativeClients(BaseClient client,
			State state) {
		if(group==null)
			return manager.getAllClients();
		return group.getClients();
	}

	public interface GameAdapter{
		public State createGameState();
		public void setGameState(State state);
		public void updatePlayerState(String id,State state);
		public void playerInput(String id,int code);
		public void lockAllPlayers();
	}
}
