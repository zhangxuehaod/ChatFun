package serverAndClient;

/**
 * Created by apple on 15-7-8.
 */
public interface StateObject {
    public State getState();
    public void setState(State state);
}
