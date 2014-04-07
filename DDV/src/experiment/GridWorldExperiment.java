package experiment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import utils.ActionStep;
import utils.State;
import utils.StateAction;
import utils.StateActionState;
import agent.ConfidenceIntervalAlgorithm;
import mdp.GridWorldMDP;

public class GridWorldExperiment {
	
	private int obsRangeMin, obsRangeMax, actRangeMin, actRangeMax;
	private double maxRew, minRew, vMax;

	private State stateZero;

	private StateAction lastStateAction;

	private double accuracy = 0.001; // Proper value?
	private double conf = 0.001; // Woot?

	private double gamma = 0.1; // Decay of rewards

	private double convergenceFactor = 0.001;
	
	private List<State> observedStates;
	private Map<StateAction, Set<State>> observedStateTrans;
	private Map<StateAction, Double> observedRewards;

	private Map<StateAction, Double> qUppers;
	private Map<StateAction, Double> qLowers;
	private Map<State, Double> vUppers;
	private Map<State, Double> vLowers;
	
	private static GridWorldMDP mdp;
	private static State currentState;

	public static void main(String[] args) {
		GridWorldMDP mdp = new GridWorldMDP();
		int steps = experiment();		
	}
	
	public static int experiment() {
		currentState = mdp.getStartingState();
		Action action;
		for (int i = 0; i < 100; i++) {
			
			
			action = ConfidenceIntervalAlgorithm.agent_step(mdp.reward(currentState), currentState);
			
			currentState = GenerateNextState(currentState, action);
			
			if (currentState.getInt(1) == 4) {
				return i;
			}
			if (currentState.getInt(1) == 2) {
				return i;
			}
		}
		return 100;
	}
	
	public static State GenerateNextState(State currentState, Action action) {
		return null; //choose next state by weighted randomization.
	}

}
