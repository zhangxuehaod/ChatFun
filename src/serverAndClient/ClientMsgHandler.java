package serverAndClient;

/**
 * Created by apple on 15-7-8.
 */
public interface ClientMsgHandler {
    public void onStateReceived(State state,BaseClient user);
}
