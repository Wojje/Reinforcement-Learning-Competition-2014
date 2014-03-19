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
	private double lowerConf = 0.001; // Woot?

	private double gamma = 1.0; // Decay of rewards

	private List<Integer> observedStates;
	private Map<StateAction, Integer> observedStateTrans, stateActionCounter;
	private Map<StateAction, Double> observedRewards;
	private Map<StateActionState, Integer> stateActionStateCounter;
	
	
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
		observedStateTrans = new HashMap<StateAction, Integer>();
		observedStates = new LinkedList<Integer>();
		stateActionCounter = new HashMap<StateAction, Integer>();
		stateActionStateCounter = new HashMap<StateActionState, Integer>();
		
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
		int sprime = o.getInt(0);
		observedStates.add(new Integer(sprime));
		
		//do action?

		return null;
	}

	@Override
	public Action agent_step(double r, Observation o) {
		int sprime = o.getInt(0);
		observedStates.add(new Integer(sprime));
		observedRewards.put(lastStateAction, r);
		observedStateTrans.put(lastStateAction, sprime);
		
		//increment N(s,a,s') counter
		updateStateActionStateCounter(lastStateAction, sprime);
		
		//Do update (line 1 in pseudo
		update();
		
		double mu_upper = updateMuUpper();
		
		if(valueDeltaSatisfactory()){
			computePolicy();
		} 
		
		int nextAction = 0;
		double minDeltaV = Double.POSITIVE_INFINITY; 
		
		for(Integer i : observedStates){
			for(int a = actRangeMin; a <= actRangeMax; a++){
				StateAction sa = new StateAction(i, a);
				DoubleTuple qprime = computeQPrime(sa);
				double deltaQ = deltaDoubles(qprime);
				double deltaV = mu_upper*deltaQ;
				if (deltaV < minDeltaV)
					nextAction = a; //(s,a) = argmin_(s,a) deltaV(s_0|s,a)
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
		}
		else stateActionCounter.put(sa, 1);
	}
	
	private void updateStateActionStateCounter(StateAction lastStateAction, int sprime) {
		StateActionState sas = 
				new StateActionState(lastStateAction.s, lastStateAction.a, sprime);
		if (stateActionStateCounter.containsKey(sas)) {
			stateActionStateCounter.put(
					sas, 
					stateActionStateCounter.get(sas) + 1);
		}
		else stateActionStateCounter.put(sas, 1);
	}
	
	private double updateMuUpper() {
		return 1;
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

	}

	private class StateAction {
		private int s, a;

		public StateAction(int state, int action) {
			s = state;
			a = action;
		}

		public int getAction() {
			return a;
		}

		public int getState() {
			return s;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj.getClass() != StateAction.class) {
				return false;
			} else {
				StateAction as = (StateAction) obj;
				return s == as.s && a == as.a;
			}
		}

		@Override
		public int hashCode() {
			return 8011 * s + 9587 * a;
		}
	}
	
	private class StateActionState {
		private int s, a, sprime;

		public StateActionState(int state, int action, int stateprime) {
			s = state;
			a = action;
			sprime = stateprime;
		}

		public int getAction() {
			return a;
		}

		public int getPreviousState() {
			return s;
		}
		
		public int getNewState() {
			return sprime;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj.getClass() != StateAction.class) {
				return false;
			} else {
				StateActionState as = (StateActionState) obj;
				return s == as.s 
						&& a == as.a 
						&& sprime == as.sprime;
			}
		}

		@Override
		public int hashCode() {
			return 8011 * s + 9587 * a + 8651*sprime;
		}
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
