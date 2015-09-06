package serverAndClient;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by apple on 15-7-8.
 */
public class TcpClientAdapter implements BaseClient.ClientAdapter {
	private String ip;
	private int port;
	private Socket socket;
	private ObjectInputStream input;
	private ObjectOutputStream output;

	public TcpClientAdapter(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	public String getServerIP(){
		return ip;
	}
	public int getServerPort(){
		return port;
	}
	public void setIP_Prot(String ip,int port){
		this.ip = ip;
		this.port = port;
	}
	@Override
	public boolean connect(BaseClient client) {
		try {
			socket = new Socket(ip, port);
			if (socket.isConnected()) {
				input = new ObjectInputStream(socket.getInputStream());
				output = new ObjectOutputStream(socket.getOutputStream());
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean sendMessage(String... strs) {
		try {
			output.writeObject(strs);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String[] receiveMessage() {
		try {
			Object obj = input.readObject();
			if( obj instanceof String[]){
				return (String[]) obj;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch(SocketException e){
			//e.printStackTrace();
		} catch(EOFException e){
			//e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void disconnect(BaseClient client) {
		try {
			if(socket.isConnected())
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
