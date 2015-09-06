package Test;

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
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import serverAndClient.BaseClient;
import serverAndClient.BaseGroup;
import serverAndClient.ClientListener;
import serverAndClient.State;
import serverAndClient.TcpClient;
import swingDialog.InputDialog;
import swingDialog.OptionDialog;

public class Client extends JFrame implements ClientListener,ActionListener{

	private static final long serialVersionUID = -7764694600645148894L;
	public static void main(String[] arg){
		new Client().setVisible(true);
	}

	JTextPane msgWindow =  new JTextPane();
	JTextField chatInput = new JTextField();
	JScrollPane scrollPane=new JScrollPane(msgWindow);
	JComboBox<BaseClient> onlineClients=new JComboBox<BaseClient>();
	JComboBox<BaseClient> myGroup=new JComboBox<BaseClient>();
	JComboBox<BaseGroup> groups=new JComboBox<BaseGroup>();
	JComboBox<String> chatRange=new JComboBox<String>(new String[]{"世界","群组","私聊"});
	JComboBox<String> gameChoices=new JComboBox<String>(new String[]{"NeverGoBack"});
	JButton send = new JButton("send");
	JButton logout = new JButton("logout");
	JButton clear = new JButton("clear");
	JButton invite = new JButton("invite");
	JButton createGroup = new JButton("createGroup");
	JButton login=new JButton("Login");
	JButton regist=new JButton("Regist");
	JButton enterGroup=new JButton("enterGroup");
	JButton quitGroup=new JButton("quitGroup");
	JButton driveAway=new JButton("driveAway");
	JButton start=new JButton("start");
	JButton quit=new JButton("quit");
	JButton left=new JButton("left");
	JButton top=new JButton("top");
	JButton right=new JButton("right");
	JButton bottom=new JButton("bottom");
	JButton choose=new JButton("chooseGame");

	StyledDocument doc;
	TcpClient client;
	public Client(){
		client=new TcpClient(Server.IP,Server.Port);
		client.setAllListener(this);
		this.setTitle("Client");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		msgWindow.setEditable(false);
		scrollPane.setPreferredSize(new Dimension(400,240));
		this.doc=msgWindow.getStyledDocument();
		Box verBox=Box.createVerticalBox();
		verBox.add(scrollPane);
		verBox.add(chatInput);
		Box horBox=Box.createHorizontalBox();
		horBox.add(driveAway);
		horBox.add(myGroup);
		horBox.add(chatRange);
		horBox.add(send);
		verBox.add(horBox);
		horBox=Box.createHorizontalBox();
		horBox.add(groups);
		horBox.add(onlineClients);
		horBox.add(invite);
		verBox.add(horBox);
		horBox=Box.createHorizontalBox();
		horBox.add(createGroup);
		horBox.add(enterGroup);
		horBox.add(quitGroup);
		verBox.add(horBox);
		horBox=Box.createHorizontalBox();
		horBox.add(start);
		horBox.add(quit);
		horBox.add(gameChoices);
		horBox.add(choose);
		verBox.add(horBox);
		horBox=Box.createHorizontalBox();
		horBox.add(left);
		horBox.add(top);
		horBox.add(right);
		horBox.add(bottom);
		verBox.add(horBox);
		horBox=Box.createHorizontalBox();
		horBox.add(regist);
		horBox.add(login);
		horBox.add(logout);
		horBox.add(clear);
		verBox.add(horBox);
		this.add(verBox);
		this.pack();
		this.setLocationRelativeTo(null);
		this.addWindowListener(new WindowListener(){
			public void windowActivated(WindowEvent arg0) {}
			public void windowClosed(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0) {
				if(client!=null ){
					client.getOperator().toLogout();
				}
			}
			public void windowDeactivated(WindowEvent arg0) {}
			public void windowDeiconified(WindowEvent arg0) {}
			public void windowIconified(WindowEvent arg0) {}
			public void windowOpened(WindowEvent arg0) {}
		});
		driveAway.addActionListener(this);
		logout.addActionListener(this);
		send.addActionListener(this);
		clear.addActionListener(this);
		createGroup.addActionListener(this);
		invite.addActionListener(this);
		login.addActionListener(this);
		regist.addActionListener(this);
		enterGroup.addActionListener(this);
		quitGroup.addActionListener(this);
		start.addActionListener(this);
		quit.addActionListener(this);
		left.addActionListener(this);
		top.addActionListener(this);
		right.addActionListener(this);
		bottom.addActionListener(this);
		choose.addActionListener(this);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Object btn = e.getSource();
		if(!client.isRunning() && btn!=login && btn!=regist){
			JOptionPane.showMessageDialog(this,"not login or regist","Error",JOptionPane.WARNING_MESSAGE);
			return;
		}
		if(btn==logout){
			client.getOperator().toLogout();
		}
		else if(btn==send){
			sendMsg();
		}
		else if(btn==clear)
			this.msgWindow.setText("");
		else if(btn==invite){
			BaseClient c=(BaseClient) this.onlineClients.getSelectedItem();
			client.getOperator().toInvite(c);
		}
		else if(btn==login){
			if(!client.isRunning())
				client.connect();
			InputDialog dialog =
					new InputDialog(this,"Login",new String[]{"ID: ","Paasowrd: "}
					,null,"OK");
			dialog.open();
			String[] in=dialog.getInput();
			client.getOperator().toLogin(in[0], in[1]);
		}
		else if(btn==regist){
			if(!client.isRunning())
				client.connect();
			InputDialog dialog =
					new InputDialog(this,"Regist",new String[]{"ID: ","Paasowrd: ","Nick Name: "}
					,null,"OK");
			dialog.open();
			String[] in=dialog.getInput();
			client.getOperator().toRegist(in[0], in[1], in[2]);
		}	
		else if(btn==createGroup){
			client.getOperator().toCreateGroup();
		}
		else if(btn==enterGroup){
			if(groups.getItemCount()>0){
				BaseGroup g=(BaseGroup) groups.getSelectedItem();
				client.getOperator().toEnterGroup(g);
			}
		}
		else if(btn==quitGroup){
			client.getOperator().toQuitGroup();
		}
		else if(btn==driveAway){
			client.getOperator().toDriveAway((BaseClient) myGroup.getSelectedItem());
		}
		else if(btn==start){

			//handler.getGameHandler().sendReadyToPlay();
		}
		else if(btn==quit){
			client.getOperator().toLogout();
		}
		else if(btn==left){
			//handler.getGameHandler().sendControlOrder(Constants.Left);
		}
		else if(btn==top){
			//handler.getGameHandler().sendControlOrder(Constants.Top);
		}
		else if(btn==right){
			//handler.getGameHandler().sendControlOrder(Constants.Right);
		}
		else if(btn==bottom){
			//handler.getGameHandler().sendControlOrder(Constants.Bottom);
		}
		else if(btn==choose){
			//handler.getGameHandler().sendQuestPlayGame(Message.NeverGoBack);
		}

	}

	private synchronized void append(String msg){
		try {
			doc.insertString(doc.getLength(), msg+"\n", null);
			msgWindow.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	public void sendMsg(){
		int idx=chatRange.getSelectedIndex();
		String txt=chatInput.getText();
		if(idx==1){
			client.getOperator().toTalkInGroup(txt);
		}
		else if(idx==0) {
			client.getOperator().toTalkInWorld(txt);
		}
		else if(idx==2){
			client.getOperator().toTalkToOther(txt, (BaseClient) this.onlineClients.getSelectedItem());
		}
		chatInput.setText("");
	}
	@Override
	public void onClientOnline(BaseClient... clients) {
		for(BaseClient c:clients){
			if(c!=client)
				this.onlineClients.addItem(c);
			else{
				this.setTitle(c.toString());
				this.append(c+" online");
			}
		}
		onlineClients.repaint();
	}
	public void clearAll(){
		onlineClients.removeAllItems();
		groups.removeAllItems();
		myGroup.removeAllItems();
	}
	@Override
	public void onClientOffline(BaseClient client) {
		if(this.client==client){
			this.clearAll();
			this.append(client+" offline");
		}else{
			onlineClients.removeItem(client);
			onlineClients.repaint();
		}
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
		if(group.contains(client)){
			groups.removeItem(group);
			for(BaseClient c:group.getClients())
				myGroup.addItem(c);
		}
		groups.repaint();
	}

	@Override
	public void onRemoveGroup(BaseGroup group) {
		this.groups.removeItem(group);
		groups.repaint();
	}

	@Override
	public void onEnterGroup(BaseClient client, BaseGroup group) {
		if(group.contains(this.client) || this.client==client){
			if(this.client==client){
				for(BaseClient c:group.getClients())
					myGroup.addItem(c);
				groups.removeItem(group);
			}
			else{
				myGroup.addItem(client);
			}
		}
		groups.repaint();
	}

	@Override
	public void onQuitGroup(BaseClient client, BaseGroup group) {
		if(group.contains(this.client)){
			myGroup.removeItem(client);
		}else if(this.client==client){
			myGroup.removeAllItems();
			groups.addItem(group);
		}
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
	public void showChat(String msg, BaseClient speker, int type) {
		this.append(speker+": "+msg);

	}

	@Override
	public void showChat(String msg, int type) {
		this.append("System: "+msg);
	}
	OptionDialog dialog;
	@Override
	public int showDialog(String title, String content, String[] btns,
			int limitTime) {
		dialog=new OptionDialog(this,title, content, btns);
		dialog.open();
		return dialog.getOption();
	}

	@Override
	public void closeDialog() {		
		this.append(client+" closeDialog");
		dialog.close();
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
