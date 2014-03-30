package mdp;

import java.util.List;

import utils.ActionStep;
import utils.State;

public interface MDP {
	
	public List<State> getStates();
	public List<ActionStep> getActions();
	public double probTransition(State s, State sprime);
	public double reward(State s, State sprime);
	
	

}
