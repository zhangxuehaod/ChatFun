package serverAndClient;

import serverAndClient.ChatManager.ChatListener;
import serverAndClient.ClientGroupManager.DialogAdapter;
import serverAndClient.ClientGroupManager.GroupListener;
import serverAndClient.ClientGroupManager.RequestListener;
import serverAndClient.ClientRegister.LoginListener;
import serverAndClient.Synchronizer.SynClientAdapter;

public class TcpClient extends BaseClient implements SynClientAdapter{

	private ClientGroupManager clientManager=new ClientGroupManager(this,false);
	private ChatManager chatManager=new ChatManager(clientManager);
	private ClientOperator clientOperator;
	private TcpClientAdapter adapter;
	public TcpClient(String ip,int port){
		super();
		adapter=new TcpClientAdapter(ip, port);
		super.setClientAdapter(adapter);
		clientOperator=new ClientOperator(clientManager,chatManager,null);
	}
	public String getServerIP(){
		return adapter.getServerIP();
	}
	public int getServerPort(){
		return adapter.getServerPort();
	}
	public void setServerIP_Port(String ip, int port) {
		adapter.setIP_Prot(ip,port);
	}
	public void setAllListener(ClientListener listener){
		clientOperator.setGameAdapter(listener);
		clientManager.addLoginListener(listener);
		clientManager.addGroupListener(listener);
		clientManager.setDialogAdapter(listener);
		clientManager.setRequestListener(listener);
		chatManager.setChatListener(listener);
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
