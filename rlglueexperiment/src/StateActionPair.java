import org.rlcommunity.rlglue.codec.types.Action;


public class StateActionPair {
	private int state;
	private Action action;
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
	
	public StateActionPair(int state, Action action) {
		this.state = state;
		this.action = action;
	}
}
