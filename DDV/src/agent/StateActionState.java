package agent;

public class StateActionState extends StateAction {

	private State sprime;
	
	public StateActionState(State s, ActionStep a, State sprime) {
		super(s, a);
		this.sprime = sprime;
	}
	
	public StateActionState(StateAction sa, State sprime){
		super(sa);
		this.sprime = sprime;
	}
	
	public State getSprime() {
		return sprime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((sprime == null) ? 0 : sprime.hashCode());
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
		if (sprime == null) {
			if (other.sprime != null)
				return false;
		} else if (!sprime.equals(other.sprime))
			return false;
		return true;
	}
	

}
