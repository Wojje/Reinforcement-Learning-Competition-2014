package agent;

import java.util.Arrays;

import org.rlcommunity.rlglue.codec.types.Action;

public class ActionStep extends Action {
	
	public ActionStep(Action a){
		super(a);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.charArray);
		result = prime * result + Arrays.hashCode(this.doubleArray);
		result = prime * result + Arrays.hashCode(this.intArray);
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
		State other = (State) obj;
		if (!Arrays.equals(this.charArray, other.charArray))
			return false;
		if (!Arrays.equals(this.doubleArray, other.doubleArray))
			return false;
		if (!Arrays.equals(this.intArray, other.intArray))
			return false;
		return true;
	}
	
	

}
