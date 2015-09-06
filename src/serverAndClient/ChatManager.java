package serverAndClient;

/**
 * Created by apple on 15-7-10.
 */
public class ChatManager {
    public static final int
            TalkInGroup=1,TalkInWorld=2,TalkToOther=3,ServerMsgInWord=4,
            ServerMsgInGroup=5,ServerMsgToClient=6;

    private BaseClient user;
    private ChatListener listener;
    private ClientGroupManager manager;
    public ChatManager(ClientGroupManager manager){
    	this.manager=manager;
    }
    public void setUser(BaseClient user){
        this.user=user;
    }
    public void talkInGroup(String words,BaseClient speaker){
        if(listener!=null ){
        	BaseGroup g=manager.getGroup(user);
        	if(g!=null && g.contains(speaker))
        		listener.showChat(words,speaker,TalkInGroup);
        }
    }
    public void talkInWorld(String words,BaseClient speaker){
        if(listener!=null) {
            listener.showChat(words, speaker, TalkInWorld);
        }
    }
    public void talkToOther(String words,BaseClient speaker,BaseClient other){
        if(listener!=null && (speaker==user || other==user)) {
            listener.showChat(words, speaker, TalkToOther);
        }
    }
    public void serverMsgInWorld(String words){
        if(listener!=null) {
            listener.showChat(words, ServerMsgInWord);
        }
    }
    public void serverMsgInGroup(String words,BaseGroup group){
        if(listener!=null) {
            listener.showChat(words, ServerMsgInGroup);
        }
    }
    public void serverMsgToClient(String words,BaseClient client){
        if(listener!=null && (client==user || user==null)) {
            listener.showChat(words, ServerMsgToClient);
        }
    }
    public void setChatListener(ChatListener listener){
        this.listener=listener;
    }
    public interface ChatListener{
        public void showChat(String msg,BaseClient speker,int type);
        public void showChat(String msg,int type);
    }
}
