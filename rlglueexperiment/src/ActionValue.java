import org.rlcommunity.rlglue.codec.types.Action;


public class ActionValue {
	private Action action;
	private int value;
	
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}

	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
	
	public ActionValue(Action action, int value) {
		this.action = action;
		this.value = value;
	}
}
