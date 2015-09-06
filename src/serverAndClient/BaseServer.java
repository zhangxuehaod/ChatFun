package serverAndClient;

import java.util.Collection;

import serverAndClient.Synchronizer.SynServerAdapter;

/**
 * Created by apple on 15-7-8.
 */
public class BaseServer implements StateObject,SynServerAdapter {

	private boolean isRunning = false;
	private ServerAdapter serverAdapter;
	private ServerListener serverListener;
	private ServerMsgHandler handler;

	public BaseServer() {
	}
	public void onClientConnect(BaseClient client) {
		if (serverListener != null) 
			serverListener.onClientConnect(client, this);
	}

	public void onClientDisconnect(BaseClient client) {
		if (serverListener != null)
			serverListener.onClientDisconnect(client, this);
	}

	public interface ServerListener {

		public void onClientConnect(BaseClient client, BaseServer server);

		public void onClientDisconnect(BaseClient client, BaseServer server);

		public void onClientMessageReceive(State state, BaseClient src);
		public void onClose(BaseServer server);
	}

	public interface ServerAdapter {
		public boolean open();

		public BaseClient acceptClient(BaseServer server);

		public void close();
	}

	public void setServerListener(ServerListener listener) {
		this.serverListener = listener;
	}

	public void setServerAdapter(ServerAdapter adapter) {
		this.serverAdapter = adapter;
	}

	private Runnable clientAcceper = new Runnable() {
		@Override
		public void run() {
			isRunning = true;
			while (isRunning()) {
				serverAdapter.acceptClient(BaseServer.this);
			}
		}
	};

	public boolean open() {
		if (serverAdapter == null || !serverAdapter.open())
			return false;
		new Thread(clientAcceper).start();
		return true;
	}

	public void setServerMsgHandler(ServerMsgHandler h) {
		this.handler=h;
	}


	public void onClientStateReceive(State state, BaseClient src) {
		if (handler != null) {
			handler.onStateReceived(state, src, this);
		}
		if(serverListener!=null){
			serverListener.onClientMessageReceive(state, src);
		}
	}

	public void close() {
		isRunning = false;
		if (serverListener != null)
			serverListener.onClose(this);
		if (serverAdapter != null)
			serverAdapter.close();
		clear();
	}

	@Override
	public void finalize() {
		if (isRunning())
			close();
		try {
			super.finalize();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}
	public boolean isRunning() {
		return isRunning;
	}

	public void clear() {
	}

	public BaseClient getClient(String id) {
		return null;
	}


	public Collection<BaseClient> getAllClient() {
		return null;
	}

	@Override
	public State getState() {
		return null;
	}

	@Override
	public void setState(State state) {
	}

	@Override
	public void sendState(State state, BaseClient receiver) {
		receiver.sendState(state);
	}

	public void sendState(State state, Collection<BaseClient> group) {
		for (BaseClient c : group)
			if(c!=null)
				c.sendState(state);
	}

	@Override
	public void sendState(State state, Collection<BaseClient> receivers,
			BaseClient excepter) {
		for(BaseClient c:receivers)
			if(c!=null && c!=excepter)
				c.sendState(state);
	}
}
