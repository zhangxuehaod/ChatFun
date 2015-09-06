package Test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import serverAndClient.BaseClient;
import serverAndClient.BaseGroup;
import serverAndClient.BaseServer;
import serverAndClient.ClientListener;
import serverAndClient.State;
import serverAndClient.TcpServer;
import serverAndClient.BaseServer.ServerListener;

public class Server extends JFrame implements ClientListener,ActionListener{

	private static final long serialVersionUID = -5202162543473739816L;
	public static String IP="127.0.0.1";
	public static int Port=8888;
	public static void main(String[] arg){
		new Server().setVisible(true);
	}
	private JTextArea msgWindow = new JTextArea();

	JComboBox<BaseClient> onlineClients=new JComboBox<BaseClient>();
	JComboBox<BaseGroup> groups=new JComboBox<BaseGroup>();
	JComboBox<String> chatRange=new JComboBox<String>(new String[]{"世界","群组","私聊"});

	JTextField chatInput = new JTextField();
	JScrollPane scrollPane=new JScrollPane(msgWindow);
	JButton send = new JButton("send");
	JButton clear = new JButton("clear");
	TcpServer server=new TcpServer(Port,this);
	public Server(){
		this.setTitle("Server");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		msgWindow.setEditable(false);
		msgWindow.setBackground(Color.DARK_GRAY);
		msgWindow.setForeground(Color.YELLOW);
		scrollPane.setPreferredSize(new Dimension(400,280));
		Box verBox=Box.createVerticalBox();
		verBox.add(scrollPane);
		verBox.add(chatInput);
		Box horBox=Box.createHorizontalBox();
		horBox.add(onlineClients);
		horBox.add(groups);
		verBox.add(horBox);
		horBox=Box.createHorizontalBox();
		horBox.add(chatRange);
		horBox.add(send);
		horBox.add(clear);
		verBox.add(horBox);
		this.add(verBox);
		this.pack();
		this.setLocationRelativeTo(null);
		this.addWindowListener(new WindowListener(){
			public void windowActivated(WindowEvent arg0) {}
			public void windowClosed(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0) {
				if(server!=null)
					server.close();
			}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowOpened(WindowEvent arg0) {}
		});
		send.addActionListener(this);
		clear.addActionListener(this);
		server.setServerListener(new ServerListener(){
			@Override
			public void onClientConnect(BaseClient client, BaseServer server) {
				append(client+" connects");
			}

			@Override
			public void onClientDisconnect(BaseClient client, BaseServer server) {
				append(client+" disconnects");
			}

			@Override
			public void onClose(BaseServer server) {
				append(" server close");
			}

			@Override
			public void onClientMessageReceive(State state, BaseClient src) {
			//	append(" recv "+src+" "+state.getString());
			}
		});
		server.open();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src=e.getSource();
		if(src==send){
			int idx=chatRange.getSelectedIndex();
			String txt=chatInput.getText();
			if(idx==1){
				server.getOperator().toSendServerMsgInGroup(txt,  (BaseGroup) groups.getSelectedItem());
			}else if(idx==0){
				server.getOperator().toSendServerMsgInWorld(txt,onlineClients.getItemAt(0));
			}
			else if(idx==2){
				server.getOperator().toSendServerMsgToClient(txt, (BaseClient) onlineClients.getSelectedItem());
			}
			chatInput.setText("");
		}else if(src==clear){
			msgWindow.setText("");
		}

	}
	private synchronized void append(String msg){
		msgWindow.append(msg+"\n");
		scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
	}
	@Override
	public void onClientOnline(BaseClient... clients) {
		for(BaseClient c:clients){
			this.onlineClients.addItem(c);
		}
		onlineClients.repaint();
	}

	@Override
	public void onClientOffline(BaseClient client) {
		onlineClients.removeItem(client);
		onlineClients.repaint();
	}

	@Override
	public void onUpdateClient(BaseClient client) {
		onlineClients.repaint();
	}

	@Override
	public void onUserOprResult(String tip) {
		this.append(tip);
	}

	@Override
	public void onAddGroup(BaseGroup group) {
		this.groups.addItem(group);
	}

	@Override
	public void onRemoveGroup(BaseGroup group) {
		this.groups.removeItem(group);
	}

	@Override
	public void onEnterGroup(BaseClient client, BaseGroup group) {
		groups.repaint();
	}

	@Override
	public void onQuitGroup(BaseClient client, BaseGroup group) {
		groups.repaint();
	}

	@Override
	public void onRequestCancel(BaseClient initor, int req) {
		this.append("onRequestCancel");
	}

	@Override
	public void onRequestDone(BaseClient initor, int req) {
		this.append("onRequestDone");

	}

	@Override
	public void onAgreeRequest(BaseClient client) {		
		this.append(client+" agree");
		
	}
	@Override
	public void onRefuseRequest(BaseClient client) {
		this.append(client+" refuse");
	}
	
	@Override
	public void showChat(String msg, BaseClient speker, int type) {
		this.append(speker+": "+msg);

	}

	@Override
	public void showChat(String msg, int type) {
		this.append("System: "+msg);
	}

	@Override
	public int showDialog(String title, String content, String[] btns,
			int limitTime) {
		int c=JOptionPane.showConfirmDialog(this, content, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if(c==JOptionPane.YES_OPTION)
			return 0;
		return 1;
	}

	@Override
	public void closeDialog() {

	}
	@Override
	public State createGameState() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setGameState(State state) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void updatePlayerState(String id, State state) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void playerInput(String id, int code) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void lockAllPlayers() {
		// TODO Auto-generated method stub
		
	}

}
