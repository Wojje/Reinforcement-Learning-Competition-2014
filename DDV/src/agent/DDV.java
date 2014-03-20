package agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

public class DDV implements AgentInterface {

	private double accuracy = 0.001; // Proper value?
	private double conf = 0.001, // Woot?
				   mu_upper = 0.001; // Woot?

	private double gamma = 1.0; // Decay of rewards

	private List<State> observedStates;
	private Map<StateAction, Set<State>> observedStateTrans;
	private Map<StateActionState, Integer> stateActionStateCounter;
	private Map<StateAction, Integer> stateActionCounter;
	private Map<StateAction, Double> observedRewards;
	
	private Map<State, DoubleTuple> values;

	private Map<StateAction, DoubleTuple> qs;

	private int obsRangeMin,obsRangeMax, actRangeMin, actRangeMax;
	private double maxRew, minRew, Rroof, vMax;

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
		vMax = maxRew / (1-gamma);

		observedRewards = new HashMap<StateAction, Double>();
		observedStateTrans = new HashMap<StateAction, Set<State>>();
		observedStates = new LinkedList<State>();
		stateActionCounter = new HashMap<StateAction, Integer>();
		stateActionStateCounter = new HashMap<StateActionState, Integer>();
		
		values = new HashMap<State, DoubleTuple>();

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
		updateObservedStateTrans(lastStateAction, sprime);
		updateStateActionStateCounter(new StateActionState(lastStateAction, sprime));
		
		
		
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
				double deltaV = mu_upper*deltaQ;
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
	
	private void updateObservedStateTrans(StateAction lastStateAction, State sprime) {
		Set<State> sass = observedStateTrans.get(lastStateAction);
		if(sass == null){
			sass = new HashSet<State>();
			observedStateTrans.put(lastStateAction, sass);
		}
		sass.add(sprime);	
	}

	private void updateStateActionStateCounter(StateActionState sas) {
		if (stateActionStateCounter.containsKey(sas)) {
			stateActionStateCounter.put(
					sas, 
					stateActionStateCounter.get(sas) + 1);
		} else {
			stateActionStateCounter.put(sas, 1);
		}
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
	
	
	private Map<StateActionState, Double> upperP(StateAction sa){
		int nsa = stateActionCounter.get(sa);
		double deltaOmega = delta(nsa)/2;
		
		Map<StateActionState, Double> pRoof = createPRoof(sa);
		Map<StateActionState, Double> pTilde = new HashMap<StateActionState, Double>(pRoof);
		
		List<State> sPrimes = new LinkedList<State>();
		for(StateActionState sas : pRoof.keySet()){
			if(pRoof.get(sas) < 1){
				sPrimes.add(sas.getSprime());
			}
		}
		
		
		while( deltaOmega > 0){
			
			StateActionState sasFloor = new StateActionState(sa, argmin(pTilde));
			StateActionState sasRoof = new StateActionState(sa, argmax(sPrimes, pTilde));
			double sasrval = 1-pTilde.get(sasRoof);
			double sasfval = pTilde.get(sasFloor);
			double zeta = least(sasrval, sasfval, deltaOmega);
			pTilde.put(sasFloor, sasfval - zeta);
			pTilde.put(sasRoof, sasrval + zeta);
			deltaOmega = deltaOmega - zeta;
		}
		
		return pTilde;
	}
	
	
	
	private double least(double d, double d2, double d3) {
		return Math.min(Math.min(d,d2), d3);
	}

	private Map<StateActionState, Double> createPRoof(StateAction sa) {
		Map<StateActionState, Double> ret = new HashMap<StateActionState, Double>();
		double prob;
		for(State s : observedStates){
			StateActionState sas = new StateActionState(sa, s);
			prob = stateActionStateCounter.get(sas) / stateActionCounter.get(sa);
			ret.put(sas, prob);
		}
		return ret;
	}

	private State argmin(Map<StateActionState, Double> pTilde) {
		double value = Double.MAX_VALUE;
		State min = null;
		double tmpValue;
		
		for(StateActionState sas : pTilde.keySet()){
			State s = sas.getSprime();
			if(pTilde.get(sas) > 0){
				tmpValue = vUpper(s);
				if(tmpValue < value){
					value = tmpValue;
					min = s;
				}
			}
		}
		return min;
	}
	
	private State argmax(List<State> sPrimes, Map<StateActionState, Double> pTilde) {
		double value = Double.MIN_VALUE;
		State max = null;
		double tmpValue;
		for(StateActionState sas : pTilde.keySet()){
			State s = sas.getSprime();
			if(sPrimes.contains(s) && pTilde.get(sas) < 1){
				tmpValue = vUpper(s);
				if(tmpValue > value){
					value = tmpValue;
					max = s;
				}
			}
		}
		return max;
	}
	
	private double vUpper(State s){
		DoubleTuple vs = values.get(s);
		if(vs == null){
			return vMax;
		} else {
			return vs.getSecond();
		}
	}
	
	private double vLower(State s){
		DoubleTuple vs = values.get(s);
		if(vs == null){
			return 0;
		} else {
			return vs.getFirst();
		}
	}

	private double pRoof(State sprime, StateAction sa){
		return stateActionStateCounter.get(new StateActionState(sa, sprime)) / stateActionCounter.get(sa);
	}

	private double delta(int nda) {
		return Math.sqrt( ( 2 * Math.log(  Math.pow(2, observedStates.size()) -2) - Math.log(conf) ) / nda );
	}

	private class DoubleTuple {
		private double fst;
		private double snd;

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
