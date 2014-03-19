package agent;

public class StateActionState extends StateAction {

	private State statePrime;
	
	public StateActionState(State s, ActionStep a, State sprime) {
		super(s, a);
		statePrime = sprime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((statePrime == null) ? 0 : statePrime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateActionState other = (StateActionState) obj;
		if (statePrime == null) {
			if (other.statePrime != null)
				return false;
		} else if (!statePrime.equals(other.statePrime))
			return false;
		return true;
	}
	
	

}
