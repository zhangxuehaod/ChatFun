package serverAndClient;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by apple on 15-7-10.
 */
public class OprChatManager implements Synchronizer.SynObject {

	private ChatManager chatManager;
	private ClientGroupManager clientManager;
	private String classID;
	public OprChatManager(ChatManager chatManager1, ClientGroupManager clientManager1) {
		classID = State.ChatManager;
		this.chatManager = chatManager1;
		this.clientManager = clientManager1;
	}

	private OprChatManager(BaseClient client, ChatManager chatManager1, ClientGroupManager clientManager1) {
		classID = transformID(client);
		this.chatManager = chatManager1;
		this.clientManager = clientManager1;
	}

	private OprChatManager(BaseGroup group, ChatManager chatManager1, ClientGroupManager clientManager1) {
		classID = transformID(group);
		this.chatManager = chatManager1;
		this.clientManager = clientManager1;
	}

	public String transformID(BaseClient client){
		return State.ChatManager + client.getID()+"C";
	}
	public String transformID(BaseGroup group){
		return State.ChatManager + group.getID()+"G";
	}
	public OprChatManager createChat(int methodId, State state){
		switch (methodId) {
		case ChatManager.ServerMsgInWord:
		case ChatManager.TalkInWorld:
			return this;
		case ChatManager.ServerMsgInGroup:{
			BaseGroup g=clientManager.getGroup(state.getString(1));
			if(g!=null)
				return new OprChatManager(g,chatManager,clientManager) ;
			else
				return null;
		}
		case ChatManager.TalkInGroup:{
			BaseClient speker=clientManager.getClient(state.getString(1));
			BaseGroup g=clientManager.getGroup(speker);
			if(g!=null)
				return new OprChatManager(g,chatManager,clientManager) ;
			else
				return null;
		}
		case ChatManager.ServerMsgToClient:
		case ChatManager.TalkToOther:
			BaseClient other=clientManager.getClient(state.getString(2));
			if(other!=null)
				return new OprChatManager(other,chatManager,clientManager) ;
			return null;
		default:
		}
		return this;
	}
	public OprChatManager checkChat(BaseGroup group){
		return new OprChatManager(group,chatManager,clientManager);

	}
	@Override
	public String getID() {
		return classID;
	}

	@Override
	public void operate(State state, BaseClient src) {
		BaseGroup group;
		BaseClient client;
		int id=state.getMethodId();
		String words=state.getString();
		switch (id) {
		case ChatManager.ServerMsgInGroup:
			group = clientManager.getGroup(state.getString(1));
			chatManager.serverMsgInGroup(words, group);
			break;
		case ChatManager.ServerMsgInWord:
			chatManager.serverMsgInWorld(words);
			break;
		case ChatManager.ServerMsgToClient:
			client = clientManager.getClient(state.getString(1));
			chatManager.serverMsgToClient(words, client);
			break;
		case ChatManager.TalkInGroup:
			client = clientManager.getClient(state.getString(1));
			group = clientManager.getGroup(client);
			if (client == null) {
				showTip(-1,id,words);
			} else if (group == null) {
				showTip(-2,id,words);
			} else {
				chatManager.talkInGroup(words, client);
			}
			break;
		case ChatManager.TalkInWorld:
			client = clientManager.getClient(state.getString(1));
			if (client != null)
				chatManager.talkInWorld(words, client);
			break;
		case ChatManager.TalkToOther:
			client = clientManager.getClient(state.getString(1));
			BaseClient other = clientManager.getClient(state.getString(2));
			if (client == null) {
				showTip(-1,id,words);
			} else if (other == null) {
				showTip(-2,id,words);
			} else {
				chatManager.talkToOther(words, client, other);
			}
			break;
		default:
		}
	}

	@Override
	public Collection<BaseClient> getRelativeClients(BaseClient client, State state) {
		BaseGroup group;
		switch (state.getMethodId()) {
		case ChatManager.TalkInGroup:
		case ChatManager.ServerMsgInGroup:
			group = clientManager.getGroup(client);
			if(group!=null)
				return group.getClients();

		case ChatManager.ServerMsgInWord:
		case ChatManager.TalkInWorld:
			return clientManager.getAllClients();

		case ChatManager.TalkToOther:
			BaseClient other = clientManager.getClient(state.getString(2));
			HashSet<BaseClient> clients = new HashSet<>();
			clients.add(client);
			clients.add(other);
			return clients;

		case ChatManager.ServerMsgToClient:
			HashSet<BaseClient> clients1 = new HashSet<>();
			clients1.add(client);
			return clients1;
		default:
		}
		return null;
	}

	public interface OprChatMangerListener {
		public void onCreateClientChat(BaseClient client);

		public void onCreateGroupChat(BaseGroup group);

		public void onChatFailed(String tip, int type, String words);
	}

	private OprChatMangerListener oprChatListener;

	public void setOprChatListener(OprChatMangerListener listener) {
		this.oprChatListener = listener;
	}
	public void showTip(int resCode, int type,String words){
		String tip="发送失败";
		switch (resCode){
		case -1:
			tip+="：本方离线";
			break;
		case -2:
			tip+="：对方离线";
			break;
		}
		if(oprChatListener!=null)
			oprChatListener.onChatFailed(tip,type,words);
	}
}
