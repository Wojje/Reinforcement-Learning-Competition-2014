package agent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

public class DDV implements AgentInterface {

	private double accuracy = 0.001; // Proper value?
	private double upperConf = 0.001; // Woot?
	private double lowerConf = 0.001, 
				   my_upper = 0.001; // Woot?

	private double gamma = 1.0; // Decay of rewards

	private List<State> observedStates;
	private Map<StateAction, State> observedStateTrans;
	private Map<StateAction, Integer> stateActionCounter;
	private Map<StateAction, Double> observedRewards;
	
	private Map<Integer, DoubleTuple> values;

	private Map<StateAction, DoubleTuple> qs;

	private int obsRangeMin,obsRangeMax, actRangeMin, actRangeMax;
	private double maxRew, minRew, Rroof;

	private StateAction lastStateAction;

	@Override
	public void agent_cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void agent_end(double reward) {
		// TODO Auto-generated method stub

	}

	@Override
	public void agent_init(String taskSpec) {
		TaskSpec theTaskSpec = new TaskSpec(taskSpec);
		System.out.println("DDV agent parsed the task spec.");
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

		actRangeMax = theActRange.getMax();
		actRangeMin = theActRange.getMin();
		obsRangeMax = theObsRange.getMax();
		obsRangeMin = theObsRange.getMin();
		maxRew = theRewardRange.getMax();
		minRew = theRewardRange.getMin();
		Rroof = maxRew * 0.5;

		observedRewards = new HashMap<StateAction, Double>();
		observedStateTrans = new HashMap<StateAction, State>();
		observedStates = new LinkedList<State>();
		stateActionCounter = new HashMap<StateAction, Integer>();
		
		values = new HashMap<Integer, DoubleTuple>();

	}

	@Override
	// What is this shit?
	public String agent_message(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action agent_start(Observation o) {

		observedStates.add(new State(o));
		
		//do action?

		return null;
	}

	@Override
	public Action agent_step(double r, Observation o) {
		State sprime = new State(o);
		observedStates.add(new State(o));
		observedRewards.put(lastStateAction, r);
		observedStateTrans.put(lastStateAction, sprime);
		
		
		
		//Do update (line 1 in pseudo
		update();
		
		if(valueDeltaSatisfactory()){
			computePolicy();
		} 
		
		ActionStep nextAction = null;
		double minDeltaV = Double.POSITIVE_INFINITY; 
		
		for(State s : observedStates){
			for(int a = actRangeMin; a <= actRangeMax; a++){
				ActionStep act = new ActionStep(new Action(1,0,0));
				act.setInt(0, a);
				StateAction sa = new StateAction(s, act);
				DoubleTuple qprime = computeQPrime(sa);
				double deltaQ = deltaDoubles(qprime);
				double deltaV = my_upper*deltaQ;
				if (deltaV < minDeltaV)
					nextAction = act; //(s,a) = argmin_(s,a) deltaV(s_0|s,a)
					minDeltaV = deltaV;
			}
		}
		
		
		
		StateAction lastStateAction = new StateAction(sprime, nextAction);
		
		//increment N(s,a) counter
		updateStateActionCounter(lastStateAction);
		
		return null;
	}
	
	private void updateStateActionCounter(StateAction sa) {
		if (stateActionCounter.containsKey(sa)) {
			stateActionCounter.put(
					sa, 
					stateActionCounter.get(sa) + 1);
		} else {
			stateActionCounter.put(sa, 1);
		}
	}
	

	private DoubleTuple computeQPrime(StateAction sa) {
		if (observedStateTrans.containsKey(sa)) {
			// Case 2 i artikeln
			return new DoubleTuple(1337.0, 42.0);
		} else {
			return new DoubleTuple(Rroof + gamma * maxRew / (1 - gamma), Rroof);
		}

	}

	private double deltaDoubles(DoubleTuple dt) {
		// Abs?
		return dt.getFirst() - dt.getSecond();
	}

	// Should this be without params?
	private boolean valueDeltaSatisfactory() {

		return true;
	}

	private void computePolicy() {
		// TODO Auto-generated method stubs

	}

	private void update() {
		// Eqution 3,4 and 8.
		updateQ_upper();
	}
	
	private void updateQ_upper(){
		// Equation 3
	}
	
	private void updateQ_lower(){
		// Equation 4
	}
	
	private void updateMY_upper(){
		// Equation 8
	}

	private class DoubleTuple {
		double fst;
		double snd;

		public DoubleTuple(double first, double second) {
			fst = first;
			snd = second;
		}

		public double getFirst() {
			return fst;
		}

		public double getSecond() {
			return snd;
		}
	}

}
