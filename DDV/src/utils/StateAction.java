package utils;

public class StateAction {
	
	private State state;
	private ActionStep action;
	
	public StateAction(State s, ActionStep a){
		state = s;
		action = a;
	}
	
	public StateAction(StateAction sa){
		this(sa.state, sa.action);
	}

	public State getState() {
		return state;
	}
	
	public ActionStep getAction() {
		return action;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateAction other = (StateAction) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}
	
	

	


}
