package serverAndClient;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by apple on 15-7-8.
 */
public class TcpServerAdapter implements BaseServer.ServerAdapter {
	ServerSocket serverSocket;
	private int port;

	public TcpServerAdapter(int port) {
		this.port = port;
	}

	@Override
	public boolean open() {
		try {
			serverSocket = new ServerSocket(port);
			return serverSocket.isBound();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public BaseClient acceptClient(BaseServer server) {
		try {
			Socket socket = serverSocket.accept();
			BaseClient client = new BaseClient();
			TcpServerClientAdapter adapter = new TcpServerClientAdapter(socket, server);
			client.setClientAdapter(adapter);
			client.setStateReceiver(adapter);
			if (client.connect()){
				server.onClientConnect(client);
				return client;
			}
		}catch (SocketException e) {
			//e.printStackTrace();
		}catch (IOException e) {
			//e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	class TcpServerClientAdapter implements BaseClient.ClientAdapter, BaseClient.StateReceiver {
		Socket socket;
		ObjectOutputStream output;
		ObjectInputStream input;
		BaseServer server;

		public TcpServerClientAdapter(Socket socket, BaseServer server) {
			this.socket = socket;
			this.server = server;
		}

		@Override
		public boolean connect(BaseClient client) {
			try {
				output = new ObjectOutputStream(socket.getOutputStream());
				input = new ObjectInputStream(socket.getInputStream());
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}catch (Exception e) {
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
				if(obj instanceof String[]){
					return (String[]) obj;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				//e.printStackTrace();
			} catch (EOFException e) {
				//e.printStackTrace();
			}catch (IOException e) {
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
			}
			server.onClientDisconnect(client);
		}

		@Override
		public void onStateReceived(State state, BaseClient client) {
			server.onClientStateReceive(state, client);
		}
	}
}
