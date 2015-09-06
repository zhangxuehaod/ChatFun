package serverAndClient;

import serverAndClient.ChatManager.ChatListener;
import serverAndClient.ClientGroupManager.DialogAdapter;
import serverAndClient.ClientGroupManager.GroupListener;
import serverAndClient.ClientGroupManager.RequestListener;
import serverAndClient.ClientRegister.LoginListener;
import serverAndClient.OprGame.GameAdapter;

public interface ClientListener extends LoginListener,GroupListener,RequestListener,ChatListener,DialogAdapter,GameAdapter{

}
