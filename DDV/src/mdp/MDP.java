package mdp;

import java.util.List;

import utils.ActionStep;
import utils.State;
import utils.StateActionState;

public interface MDP {
	
	public State getStartingState();
	public List<State> getStates();
	public List<ActionStep> getActions();
	public double probTransition(StateActionState sas);
	public double rewardTwoStates(State s, State sprime);
	public double rewardSingleState(State s);
	
	
	
	

}
