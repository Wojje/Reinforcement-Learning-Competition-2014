package utils;

import java.util.Arrays;

import org.rlcommunity.rlglue.codec.types.Observation;

public class State extends Observation {
	
	public State(Observation o){
		super(o);
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
	
	public String toString(){
		//Make cleaner
		return Arrays.toString(this.intArray);
	}
	
	

}
