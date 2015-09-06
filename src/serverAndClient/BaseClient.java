package serverAndClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

public class BaseClient implements StateObject {
	private String id = "", name = "";
	private State state = new State();
	private Set<DisconnectListener> disconnecterSet = new HashSet<>();

	private ArrayBlockingQueue<State> sendQueue = new ArrayBlockingQueue<State>(10, true);
	private StateReceiver stateReceiver;
	private ClientAdapter clientAdapter;
	private ClientMsgHandler handler;
	private Boolean isRunning = false;

	public BaseClient() {
	}

	public void copyDataOf(BaseClient client) {
		this.id=client.id;
		this.name=client.name;
		this.state.loadStrs(client.state.toStrings());
		if(!disconnecterSet.isEmpty())
			disconnecterSet.clear();
		this.disconnecterSet.addAll(client.disconnecterSet);
	}
	public void setClientMsgHandler(ClientMsgHandler h) {
		this.handler=h;
	}
	public boolean notLogined(){
		return "".equals(id);
	}
	private void onMessageReceive(String... strs) {
		State s = new State();
		if (s.loadStrs(strs)) {
			if (handler != null) {
				handler.onStateReceived(s, this);
			}
			if (stateReceiver != null)
				stateReceiver.onStateReceived(s, this);
		}
	}

	private Runnable sender = new Runnable() {
		@Override
		public void run() {
			while (isRunning()) {
				try {
					State s = sendQueue.take();
					if (isRunning()) {
						if (!sendMessage(s.toStrings())){
							break;
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
			disconnect();
		}
	};

	private Runnable receiver = new Runnable() {
		@Override
		public void run() {
			while (isRunning()) {
				String[] strs = clientAdapter.receiveMessage();
				if(strs==null)
					break;
				if (isRunning()) {
					onMessageReceive(strs);
				}
			}
			disconnect();
		}
	};

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public interface StateReceiver {
		public void onStateReceived(State state, BaseClient user);
	}

	public interface DisconnectListener {
		public void onDisconnect(BaseClient client);
	}

	public interface ClientAdapter{
		public boolean connect(BaseClient client);

		public boolean sendMessage(String... strs);

		public String[] receiveMessage();

		public void disconnect(BaseClient client);
	}

	public void addDisconnecter(DisconnectListener listener) {
		synchronized(disconnecterSet){
			disconnecterSet.add(listener);
		}
	}

	public void removeDisconnecter(DisconnectListener listener) {
		synchronized(disconnecterSet){
			disconnecterSet.remove(listener);
		}
	}

	public void setStateReceiver(StateReceiver receiver) {
		this.stateReceiver = receiver;
	}

	public void setClientAdapter(ClientAdapter adapter) {
		this.clientAdapter = adapter;
	}

	public void sendState(State state) {
		try {
			sendQueue.put(state);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	private boolean sendMessage(String... strs) {
		if (clientAdapter != null){
			return clientAdapter.sendMessage(strs);
		}
		return false;
	}

	public boolean connect() {
		synchronized (isRunning) {
			if (isRunning)
				isRunning = false;
		}
		startRun();
		if (clientAdapter != null && !clientAdapter.connect(this)){
			isRunning = false;
		}else{
			new Thread(receiver).start();
			new Thread(sender).start();
		}
		return isRunning;
	}
	public void startRun(){
		if(!sendQueue.isEmpty())
			sendQueue.clear();
		synchronized (isRunning) {
			isRunning = true;
		}
	}
	public boolean isRunning() {
		synchronized (isRunning) {
			return isRunning;
		}
	}
	public void closeButNotDisconnect(){
		HashSet<DisconnectListener> ls=new HashSet<>(disconnecterSet);
		for(DisconnectListener l: ls){
			l.onDisconnect(this);
		}
		clear();
	}
	public void disconnect() {
		synchronized (isRunning) {
			if(!isRunning)
				return;
			isRunning = false;
		}
		if (clientAdapter != null)
			clientAdapter.disconnect(this);
		try {
			sendQueue.put(new State());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		HashSet<DisconnectListener> ls=new HashSet<>(disconnecterSet);
		for(DisconnectListener l: ls){
			l.onDisconnect(this);
		}
		clear();
	}
	public Set<DisconnectListener> getDisconnecter(){
		return this.disconnecterSet;
	}
	@Override
	public void finalize() {
		if (isRunning())
			disconnect();
		try {
			super.finalize();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	public void setID(String id) {
		this.id = id;
	}

	public String getID() {
		return this.id;
	}

	public String toString() {
		return "Client"+id;
	}

	public void clear() {
		id="";
		name = "";
		sendQueue.clear();
		this.disconnecterSet.clear();
	}

	public boolean isSame(BaseClient client) {
		return id.equals(client.id);
	}

	public boolean compareTo(BaseClient oth) {
		return this.id.compareTo(oth.id) > 0;
	}

	public void setMyState(State state) {
		this.state = state;
	}

	public State getMyState() {
		return state;
	}

	public State getState() {
		return State.code(new State(id), state);
	}

	public void setState(State state) {
		clear();
		List<State> list = State.decodeToList(state);
		this.id = list.get(0).strs[0];
		this.state = list.get(1);
	}
}
