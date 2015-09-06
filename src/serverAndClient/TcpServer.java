package serverAndClient;

import java.util.Collection;

import serverAndClient.ChatManager.ChatListener;
import serverAndClient.ClientGroupManager.DialogAdapter;
import serverAndClient.ClientGroupManager.GroupListener;
import serverAndClient.ClientGroupManager.RequestListener;
import serverAndClient.ClientRegister.LoginListener;

public class TcpServer extends BaseServer{

	private ClientGroupManager clientManager=new ClientGroupManager(null,true);
	private ChatManager chatManager=new ChatManager(clientManager);
	private ClientOperator clientOperator;
	public TcpServer(int port,ClientListener listener){
		super();
		super.setServerAdapter(new TcpServerAdapter(port));
		clientOperator=new ClientOperator(clientManager,chatManager,listener,this);
		this.setListener(listener);
	}
	public void setListener(ClientListener listener){
		this.addLoginListener(listener);
		this.addGroupListener(listener);
		this.setChatListener(listener);
		this.setDialogAdapter(listener);
		this.setRequestListener(listener);
	}
	@Override
	public void clear(){
		super.clear();
		clientManager.clear();
		clientOperator.clear();
	}
	@Override
	public boolean open(){
		boolean flag=super.open();
		clientManager.loadFromFile();
		return flag;
	}
	@Override
	public void close() {
		super.close();
		clientManager.saveIntoFile();
	}
	
	@Override
	public void onClientConnect(BaseClient client) {
		super.onClientConnect(client);
		clientOperator.toSetClientManager(client);
	}
	@Override
	public void onClientDisconnect(BaseClient client) {
		super.onClientDisconnect(client);
		if(clientManager.isOnline(client)){
			clientOperator.toSendClientLogout(client);
		}
	}
	@Override
	public BaseClient getClient(String id) {
		return clientManager.getClient(id);
	}

	@Override
	public Collection<BaseClient> getAllClient() {
		return clientManager.getAllClients();
	}

	public Collection<BaseGroup> getAllGroups() {
		return clientManager.getAllGroups();
	}
	@Override
	public State getState() {
		return clientManager.getState();
	}

	@Override
	public void setState(State state) {
		clientManager.setState(state);
	}
	
	public ClientOperator getOperator(){
		return clientOperator;
	}
	public ClientGroupManager getClientGroupManager(){
		return clientManager;
	}
    public void addLoginListener(LoginListener listener) {
        clientManager.addLoginListener(listener);
    }

    public void removeLoginListener(LoginListener listener) {
    	clientManager.removeLoginListener(listener);
    }

    public void addGroupListener(GroupListener listener) {
    	clientManager.addGroupListener(listener);
    }

    public void removeGroupListener(GroupListener listener) {
    	clientManager.removeGroupListener(listener);
    }

    public void setDialogAdapter(DialogAdapter adapter) {
    	clientManager.setDialogAdapter(adapter);
    }
    public void setRequestListener(RequestListener listener){
    	clientManager.setRequestListener(listener);
    }
    public void setChatListener(ChatListener listener){
    	chatManager.setChatListener(listener);
    }
}
