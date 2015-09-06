package serverAndClient;

/**
 * Created by apple on 15-7-8.
 */
public interface ServerMsgHandler {
    public void onStateReceived(State state, BaseClient src, BaseServer server);
}
