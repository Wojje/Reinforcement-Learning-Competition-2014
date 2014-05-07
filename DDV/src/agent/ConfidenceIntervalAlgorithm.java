package agent;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

import utils.ActionStep;
import utils.State;
import utils.StateAction;
import utils.StateActionState;
import utils.Utilities;

public class ConfidenceIntervalAlgorithm implements AgentInterface {
	public static int NBR_REACHES;
    public static int HABITATS_PER_REACHES;
	
    private int actionDims;
    
	private double totalReward = 0;

	private int obsRangeMin;
	private static int obsRangeMax;
	private int actRangeMin;
	private int actRangeMax;
	private double maxRew, minRew;

	private State stateZero;

	private static StateAction lastStateAction;

	private Logic logic;
	
	
	private Map<State, Action> policy = null;
	

	public ConfidenceIntervalAlgorithm(){
		
	}
	
	public static void main(String[] args){
     	AgentLoader theLoader=new AgentLoader(new ConfidenceIntervalAlgorithm());
        theLoader.run();
	}

	public void agent_cleanup() {
		lastStateAction = null;
	}

	public void agent_end(double reward) {
		// TODO Auto-generated method stub
	}

	public void agent_init(String taskSpec) {
		TaskSpec theTaskSpec = new TaskSpec(taskSpec);
		System.out.println("CI agent parsed the task spec.");
		System.out.println("Observation have "
				+ theTaskSpec.getNumDiscreteObsDims() + " integer dimensions");
		System.out.println("Actions have "
				+ theTaskSpec.getNumDiscreteActionDims()
				+ " integer dimensions");
		IntRange theObsRange = theTaskSpec.getDiscreteObservationRange(0);
		System.out.println("Observation (state) range is: "
				+ theObsRange.getMin() + " to " + theObsRange.getMax());
		IntRange theActRange = theTaskSpec.getDiscreteActionRange(0);
		System.out.println("Action range is: " + theActRange.getMin() + " to "
				+ theActRange.getMax());
		DoubleRange theRewardRange = theTaskSpec.getRewardRange();
		System.out.println("Reward range is: " + theRewardRange.getMin()
				+ " to " + theRewardRange.getMax());
/*
 * andrade ifran obsRangeMax till observationsDimension
 */
		logic = new Logic(theTaskSpec);
		
		actionDims = theTaskSpec.getNumDiscreteActionDims(); //reach
		NBR_REACHES = actionDims;  // reach
		HABITATS_PER_REACHES = theTaskSpec.getNumDiscreteObsDims()/NBR_REACHES;
		
		System.out.println(NBR_REACHES + "---" + HABITATS_PER_REACHES);
		actRangeMax = theActRange.getMax();
		actRangeMin = theActRange.getMin();
		obsRangeMax = theObsRange.getMax();
		obsRangeMin = theObsRange.getMin();
		maxRew = theRewardRange.getMax();
//		minRew = theRewardRange.getMin();


		policy = new HashMap<State, Action>();

	}

	// What is this shit?
	public String agent_message(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Action agent_start(Observation o) {
		stateZero = new State(o);
		Action bestAction;
		
		Action tmp = policy.get(stateZero);
		
		if(tmp == null){
			bestAction = new Action(actionDims, 0, 0);
			for(int i = 0; i < actionDims; i++) {
				bestAction.setInt(i, 1);
			}
		} else {
			bestAction = tmp;
		}
		
		lastStateAction = new StateAction(stateZero, new ActionStep(bestAction));

		return bestAction;
	}
	

	public Action agent_step(double r, Observation o) {
		State sprime = new State(o);
		
		logic.updateNS(sprime);
		logic.updateNSAS(lastStateAction.getState(), lastStateAction.getAction(), sprime);
		logic.updateReward(lastStateAction.getState(), lastStateAction.getAction(), r);
		logic.updateObservedStateTrans(lastStateAction.getState(), lastStateAction.getAction(), sprime);
		
		logic.update();
		
		ActionStep bestAction = logic.computeBestAction(sprime);
		
		lastStateAction = new StateAction(sprime, bestAction);
		
		logic.updateNSA(sprime, bestAction);
		logic.updateObservedActions(sprime, bestAction);
		
		return bestAction;
	}	
}