package serverAndClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Created by apple on 15-7-10.
 */

public class Synchronizer implements ClientMsgHandler, ServerMsgHandler {

	private Map<String, SynObject> synObjs = new HashMap<>();
	private static final int
	ClientSelfOpr = 1, ClientGroupOpr = 2,DoneGroupOpr = 3;
	private SynClientAdapter clientAdapter;
	private SynServerAdapter serverAdapter;
	private SynAdapter adapter;
	private Map<SynObject,GroupOperate> groupOprs=new HashMap<>();
	public Synchronizer(SynAdapter synAdapter,SynClientAdapter clientAdapter) {
		this.adapter=synAdapter;
		this.clientAdapter = clientAdapter;
	}

	public Synchronizer(SynAdapter synAdapter,SynServerAdapter serAdapter) {
		this.adapter=synAdapter;
		this.serverAdapter = serAdapter;
	}

	public void clear(){
		for(GroupOperate g:groupOprs.values()){
			g.clear();
		}
	}
	public State headerOf(State state, int synType) {
		State s = new State();
		s.setIdType(state.getClassID(), state.getMethodId());
		s.setSyn(synType);
		return s;
	}

	public void code(String classId, int methodId, State state, int synType) {
		state.setIdType(classId, methodId);
		state.setSyn(synType);
	}
	public SynObject getSynObject(State state){
		SynObject obj=synObjs.get(state.getClassID());
		if(obj==null){
			obj=adapter.createSynObject(state.getClassID(), state.getMethodId(), state);
			if(obj!=null)
				synObjs.put(obj.getID(), obj);
		}
		return obj;
	}
	public void clientOperateSelf(String classId, int methodId, State state) {
		code(classId,methodId,state,ClientSelfOpr);
		clientAdapter.sendState(state);
	}

	public void clientOperateGroup(String classId, int methodId, State state) {
		code(classId,methodId,state,ClientGroupOpr);
		clientAdapter.sendState(state);
		getPermit(classId, 0).acquire();
	}
	@Override
	public void onStateReceived(State state, BaseClient user) {
		SynObject obj=getSynObject(state);
		if(obj==null)
			return;
		synchronized(obj){
			obj.operate(state, user);
		}
		switch(state.getSyn()){
		case ClientSelfOpr:
			break;
		case ClientGroupOpr:
			clientAdapter.sendState(headerOf(state,DoneGroupOpr));
			break;
		case DoneGroupOpr:
			getPermit(state.getClassID(), 0).release();;
			break;
		}
	}
	@Override
	public void onStateReceived(State state, BaseClient src, BaseServer server) {
		SynObject obj=getSynObject(state);
		if(obj==null)
			return;
		Collection<BaseClient> related;
		synchronized(obj){
			related=obj.getRelativeClients(src, state);
		}
		switch(state.getSyn()){
		case ClientSelfOpr:
			serverAdapter.sendState(state, src);
			if(related!=null){
				serverAdapter.sendState(state, related, src);
			}
			synchronized(obj){
				obj.operate(state, src);
			}
			break;
		case ClientGroupOpr:
			if(related!=null){
				GroupOperate opr=getGroupOperate(obj);
				opr.acquire();
				opr.startOperate(state, related);
				opr.release();
				synchronized(obj){
					obj.operate(state, src);
				}
				serverAdapter.sendState(headerOf(state,DoneGroupOpr), related,null);
			}
			break;
		case DoneGroupOpr:
			GroupOperate opr=getGroupOperate(obj);
			opr.doneOprate(src);
			break;
		}
	}
	private void serverSendState(SynObject obj,State state,BaseClient client){
		if(client!=null)
			serverAdapter.sendState(state, client);
		Collection<BaseClient> related=obj.getRelativeClients(client, state);
		if(related!=null){
			serverAdapter.sendState(state, related, client);
		}
	}
	public void serverAcquireOperate(String classId, int methodId, State state, BaseClient client,boolean doInServer) {
		code(classId,methodId,state,ClientSelfOpr);
		SynObject obj=getSynObject(state);
		synchronized(obj){
			serverSendState(obj,state,client);
			if(doInServer)
				obj.operate(state, client);
		}
	}
	public void serverAcquireOperateInGroup(String classId, int methodId, State state, BaseClient client,boolean doInServer){
		code(classId,methodId,state,ClientGroupOpr);
		SynObject obj=getSynObject(state);
		synchronized(obj){
			Collection<BaseClient> related=obj.getRelativeClients(client, state);
			GroupOperate opr=getGroupOperate(obj);
			opr.acquire();
			opr.startOperate(state, related);
			opr.release();
			if(doInServer)
				obj.operate(state, client);
		}
	}
	public GroupOperate getGroupOperate(SynObject obj){
		if(!groupOprs.containsKey(obj)){
			groupOprs.put(obj, new GroupOperate());
		}
		return groupOprs.get(obj);
	}
	public class GroupOperate implements BaseClient.DisconnectListener{
		Semaphore sema=new Semaphore(1,true);
		Collection<BaseClient> related;
		Collection<BaseClient> undoSet=new HashSet<>();
		boolean isCancel=false;
		State state;
		public synchronized void startOperate(State state,Collection<BaseClient> related){
			this.related=related;
			if(!undoSet.isEmpty())
				undoSet.clear();
			isCancel=false;
			undoSet.addAll(related);
			for(BaseClient c:undoSet)
				c.addDisconnecter(this);
			serverAdapter.sendState(state, related,null);
			this.state=state;
			acquire();
		}
		public synchronized void doneOprate(BaseClient client){
			if(undoSet.contains(client)){
				undoSet.remove(client);
				if(undoSet.isEmpty()){
					sema.release();
					for(BaseClient c:related)
						c.removeDisconnecter(this);
				}
			}
		}
		public void acquire(){
			try {
				sema.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		public void release(){
			sema.release();
		}
		public boolean isCanceled(){
			return isCancel;
		}

		public void clear() {
			while (sema.hasQueuedThreads())
				sema.release();
			while ((sema.availablePermits() > 1))
				sema.tryAcquire();
		}
		@Override
		public void onDisconnect(BaseClient client) {
			//	isCancel=true;
			if(undoSet.contains(client)){
				undoSet.remove(client);
				client.removeDisconnecter(this);
				if(undoSet.isEmpty())
					sema.release();
			}
		}
	}
	Map<String,Permit> permitMap=new HashMap<>();
	public Permit getPermit(String classId,int permits){
		Permit p=permitMap.get(classId);
		if(p==null)
			p=new Permit(classId,permits);
		return p;
	}
	public class Permit{
		private Semaphore sema;
		private final int permits;
		public Permit(String classId,int permits){
			sema=new Semaphore(permits,true);
			permitMap.put(classId, this);
			this.permits=permits;
		}
		public void acquire(){
			try {
				sema.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		public void release(){
			sema.release();
		}
		public void clear(){
			while(sema.hasQueuedThreads())
				sema.release();
			while(sema.availablePermits()<permits)
				sema.release();
			while(sema.availablePermits()>permits)
				sema.tryAcquire();
		}
	}
	public interface SynObject {

		public String getID();

		public void operate(State state, BaseClient src);

		public Collection<BaseClient> getRelativeClients(BaseClient client, State state);
	}
	public interface SynAdapter{
		public SynObject createSynObject(String classID,int methodId,State state);
	}

	public interface SynClientAdapter{
		public void sendState(State state);
	}

	public interface SynServerAdapter{
		public void sendState(State state, BaseClient receiver);

		public void sendState(State state, Collection<BaseClient> receivers, BaseClient excepter);
	}

}
