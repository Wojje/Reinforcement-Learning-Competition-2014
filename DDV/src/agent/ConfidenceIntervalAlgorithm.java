package agent;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

import utils.*;

public class ConfidenceIntervalAlgorithm implements AgentInterface {

	private int obsRangeMin;
	private static int obsRangeMax;
	private int actRangeMin;
	private int actRangeMax;
	private double maxRew, minRew;
	private static double vMax;

	private State stateZero;

	private static StateAction lastStateAction;

	private double accuracy = 0.1; // Proper value?
	private static double conf = 0.05; // Woot?

	private static double gamma = 0.9; // Decay of rewards

	private static double convergenceFactor = 10.0;

//	private static List<State> observedStates;
	private static Map<StateAction, Set<State>> observedStateTrans;
//	private static Map<StateActionState, Integer> stateActionStateCounter;
//	private static Map<StateAction, Integer> stateActionCounter;
//	private static Map<StateAction, Double> observedRewards;

	private static Map<StateAction, Double> qUppers;
	private static Map<StateAction, Double> qLowers;
	private static Map<State, Double> vUppers;
	private static Map<State, Double> vLowers;
	
	private Model model;
	
	private boolean checkedAllStates;
	private boolean createUnknownState;
	
	private int step = 0;

	private Random randGenerator = new Random();
	
//	public ConfidenceIntervalAlgorithm(){
//		
//	}
	
	public ConfidenceIntervalAlgorithm(int minState, int maxState, int minAct, int maxAct, double maxRew){
		actRangeMax = maxAct;
		actRangeMin = minAct;
		obsRangeMax = maxState;
		obsRangeMin = minState;
		this.maxRew = maxRew;
		
		model = new Model(maxState, conf);
		
//		minRew = theRewardRange.getMin();

		vMax = maxRew / (1 - gamma);

//		observedRewards = new HashMap<StateAction, Double>();
		observedStateTrans = new HashMap<StateAction, Set<State>>();
//		observedStates = new LinkedList<State>();

//		stateActionCounter = new HashMap<StateAction, Integer>();
//		stateActionStateCounter = new HashMap<StateActionState, Integer>();
		qUppers = new HashMap<StateAction, Double>();
		qLowers = new HashMap<StateAction, Double>();
		vUppers = new HashMap<State, Double>();
		vLowers = new HashMap<State, Double>();
	}
	
//	public static void main(String[] args){
//     	AgentLoader theLoader=new AgentLoader(new ConfidenceIntervalAlgorithm());
//        theLoader.run();
//	}

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

		model = new Model(obsRangeMax, conf);
		
		actRangeMax = theActRange.getMax();
		actRangeMin = theActRange.getMin();
		obsRangeMax = theObsRange.getMax();
		obsRangeMin = theObsRange.getMin();
		maxRew = theRewardRange.getMax();
//		minRew = theRewardRange.getMin();

		vMax = maxRew / (1 - gamma);

//		observedRewards = new HashMap<StateAction, Double>();
		observedStateTrans = new HashMap<StateAction, Set<State>>();
//		observedStates = new LinkedList<State>();
//		stateActionCounter = new HashMap<StateAction, Integer>();
//		stateActionStateCounter = new HashMap<StateActionState, Integer>();



		qUppers = new HashMap<StateAction, Double>();
		qLowers = new HashMap<StateAction, Double>();
		vUppers = new HashMap<State, Double>();
		vLowers = new HashMap<State, Double>();
	}

	// What is this shit?
	public String agent_message(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Action agent_start(Observation o) {
		Observation newObservation = o.duplicate();
		stateZero = new State(newObservation);
//		observedStates.add(stateZero);
		model.addStartState(newObservation);
		
		int theIntAction = randGenerator.nextInt(actRangeMax+1);
		Action bestAction = new Action(1, 0, 0);
		bestAction.setInt(0, (int)(Math.random() * (4))); //Hard-coded for GridWorldMDP
		
		lastStateAction = new StateAction(stateZero, new ActionStep(bestAction));
//		updateStateActionCounter(lastStateAction);
//		System.out.println(bestAction.intArray[0]);
		return bestAction;
	}

	public Action agent_step(double r, Observation o) {
		step++;
		State sprime = new State(o);
//		observedStates.add(new State(o));
//		observedRewards.put(lastStateAction, r);
//		updateObservedStateTrans(lastStateAction, sprime);
//		updateStateActionStateCounter(new StateActionState(lastStateAction,
//				sprime));
		
		model.addObservation(lastStateAction.getState(), lastStateAction.getAction(), sprime, r);
		
		if(step % 900 == 0){
//			doAwesomeStuff();
		}

//		Action bestAction = new Action(1, 0, 0);
//		boolean flags[] = {false, false, false, false, false};
//
//		double bestValue = Double.MIN_VALUE;
//		for (StateAction sa : qUppers.keySet()) {
//			if (sa.getState() == sprime) {
//				double value = qUppers.get(sa);
//				flags[sa.getAction().intArray[0]] = true;
//				if (value > bestValue) {
//					bestAction = sa.getAction();
//					bestValue = value;
//				}
//			}
//		}
//		
//		
//		for (int i = 0; i <= actRangeMax; i++) {
//			if (!flags[i]) {
//				bestAction.intArray[0] = i;
//			}
//		}
//
//		lastStateAction = new StateAction(sprime, new ActionStep(bestAction));
//		
//		System.out.println(bestAction.intArray[0]);
		Action bestAction = new Action(1, 0, 0);
		if(sprime.getInt(0) == 3 || sprime.getInt(0) == 7){
			bestAction.setInt(0, 4);
		} else {
			bestAction.setInt(0, (int)(Math.random() * (4))); //Hard-coded for GridWorldMDP
		}
//		bestAction.setInt(0, (int)(Math.random() * (actRangeMax+1)));
//		System.out.println("Action: "+bestAction.getInt(0));
				
		lastStateAction = new StateAction(sprime, new ActionStep(bestAction));
		
		return bestAction; // return chosen action
	}

public void doAwesomeStuff() {
	for(StateAction sa : model.getObservedTransKeys()){
		updateQ(sa, true);
		updateQ(sa, false);
		updateQUpper();
		updateQLower();
		updateVUpper();
		updateVLower();
	}
//	System.out.println(model.getObservedStates().size());
}

//	private static void updateObservedStateTrans(StateAction lastStateAction,
//			State sprime) {
//		Set<State> sass = observedStateTrans.get(lastStateAction);
//		if (sass == null) {
//			sass = new HashSet<State>();
//			observedStateTrans.put(lastStateAction, sass);
//		}
//		sass.add(sprime);
//	}

//	private void updateStateActionCounter(StateAction sa) {
//		if (stateActionCounter.containsKey(sa)) {
//			stateActionCounter.put(sa, stateActionCounter.get(sa) + 1);
//		} else {
//			stateActionCounter.put(sa, 1);
//		}
//	}

//	private void updateStateActionStateCounter(StateActionState sas) {
//		if (stateActionStateCounter.containsKey(sas)) {
//			stateActionStateCounter.put(sas,
//					stateActionStateCounter.get(sas) + 1);
//		} else {
//			stateActionStateCounter.put(sas, 1);
//		}
//	}

	public void updateQUpper() {
		iterateQ(true);
	}

	private void updateQLower() {
		iterateQ(false);
	}

	private static void updateVUpper() {
		Map<State, Double> v = new HashMap<State, Double>();
		for (StateAction sa : qUppers.keySet()) {
			double value = qUppers.get(sa);
			State state = sa.getState();
			if (v.containsKey(state)) {
				if (v.get(sa.getState()) < value) {
					v.put(state, value);
				}
			} else {
				v.put(state, value);
			}
		}
		vUppers = v;
	}

	private static void updateVLower() {
		Map<State, Double> v = new HashMap<State, Double>();
		for (StateAction sa : qLowers.keySet()) {
			double value = qLowers.get(sa);
			State state = sa.getState();
			if (v.containsKey(state)) {
				if (v.get(sa.getState()) < value) {
					v.put(state, value);
				}
			} else {
				v.put(state, value);
			}
		}
		vLowers = v;
	}

	private void updateMuUpper() {
		// Equation 8
	}

	private void iterateQ(boolean upper) {
		boolean converged = false;
		while (!converged) {
			boolean allConverged = true;
			for (StateAction sa : qUppers.keySet()) {
				double tmp = upper ? qUppers.get(sa) : qLowers.get(sa);
				updateQ(sa, upper);
				double newQ = upper ? qUppers.get(sa) : qLowers.get(sa);
				if (Math.abs(tmp - newQ) > convergenceFactor) {
					allConverged = false;
				}
			}
			if (allConverged) {
				converged = true;
			}
		}
	}

	private void updateQ(StateAction sa, boolean upper) {
		double tmp;
		computePTildes(sa, upper);
		
		model.printPtilde();
		
		Map<State, Double> vals = new HashMap<State, Double>();
		for (StateAction saPrime : model.getObservedTransKeys()) {
			Double d = vals.get(saPrime.getState());
			double val = (d == null ? Double.MIN_VALUE : d);
			if (upper) {
				Double q = qUppers.get(sa);
				tmp = (q == null ? vMax : q);
			} else {
				Double q = qLowers.get(sa);
				tmp = (q == null ? 0.0 : q);
			}

			if (tmp > val) {
				vals.put(saPrime.getState(), tmp);
			}

		}
		double sum = 0;
		for (State s : vals.keySet()) {
			double val = vals.get(s);
			sum += model.pTilde(new StateActionState(sa, s))
					* val;//Math.max(val, vMax);
		}
		if (upper) {
			qUppers.put(sa, model.reward(sa) + gamma * sum);
		} else {
			qLowers.put(sa, model.reward(sa) + gamma * sum);
		}
	}

	private void computePTildes(StateAction sa,
			boolean upper) {
		
		model.initPRoofPTilde(sa);
		System.out.println("-------NEW ciMOute P TILDE----------");
		model.printPtilde();
		
		double deltaOmega = model.omega(sa)/2.0;
		double zeta;
		double sasFloorValue;
		double sasRoofValue;

		
		double temp1;
		double temp2;
		
		//		System.out.println(model.getObservedStates());
		while (deltaOmega > 0) {
			
//			for(StateAction saDerp : model.getObservedTransKeys()){
//				for(State sprime : model.getObservedStates()){
//					StateActionState sas = new StateActionState(saDerp, sprime);
//					String s = "S: " + sas.getState().getInt(0);
//					s += " A: "+sas.getAction().getInt(0);
//					s += " S' "+sas.getSprime().getInt(0);
//				}
//			}
			
			model.createSetOfSprimes(sa);
			

			State min = argmin(sa, upper);
			if(min == null){
				System.out.println("Oj, min var null!");
				return;
			}
			StateActionState sasFloor = new StateActionState(sa, min);
			State max = argmax(sa, upper);
			if(max == null){
				System.out.println("Oj, max var null!");
				System.out.println("S' innehöll: " + model.getSprimes().size() + " värden");
				for(State s:model.getSprimes()){
					System.out.print(" S: "+ s.getInt(0));
					System.out.println();
				}
				
				return;
			}
			//			if(max == null){
//				return;
//			}
			StateActionState sasRoof = new StateActionState(sa, max);
			
//			if(sasFloor.equals(sasRoof)){
//				System.out.println("we are the same!");
//			}
			
			
			temp1 = model.pTilde(sasRoof);			
			temp2 = model.pTilde(sasFloor);
//			System.out.println("pTildes fÃ¶r sasRoof: "+temp1 + " sasfloor: " + temp2);
			
			sasRoofValue = temp1;
			sasFloorValue = temp2;
//			System.out.println("sasRoofValue: "+sasRoofValue + " sasFloorValue: "+sasFloorValue + " deltaOmega "+deltaOmega);
			zeta = Math.min(Math.min(1-temp1, sasFloorValue), deltaOmega);
			
			System.out.println("ZETA: " + zeta);
			
			
			model.updatePTilde(sasFloor, sasFloorValue - zeta);
			model.updatePTilde(sasRoof, sasRoofValue + zeta);
//			System.out.println("DeltaOmega: "+deltaOmega + " Zeta: "+zeta);
			deltaOmega = deltaOmega - zeta;
//			System.out.println("New DeltaOmega "+deltaOmega);
//			System.out.println(deltaOmega);
			model.printPtilde();

			
		}
		System.out.println("TERMINATED PTILDE");
//		System.out.println("Terminated");
	}

	private State argmin(StateAction sa, boolean upper) {
		double value = Double.POSITIVE_INFINITY;
		State min = null;
		Double tmpValue;

//		int i = 0;
		for(State s : model.getObservedStates()){
//			i++;
			StateActionState sas = new StateActionState(sa, s);
			if (model.pTilde(sas) > 0) {
				if (upper) {
					tmpValue = vUppers.get(s);
				} else {		
					tmpValue = vLowers.get(s);
				}			
				if (tmpValue == null) {
					if (upper) {
						tmpValue = vMax;
					} else {
						tmpValue = 0.0; // blir fel vi lowerP?
					}
				}
				if (tmpValue < value) {
					value = tmpValue;
					min = s;
				}
			}
		}
//		checkedAllStates = i == model.getNbrOfStates() || value != 0.0; //In case of not all states checked.
//		createUnknownState = i < model.getNbrOfStates() && value != 0.0;
//		if(createUnknownState){
//			min = new State(new Observation(1, 0));
//			min.setInt(0, -4); // Unique unknown state
//		}
		return min;
	}

	private  State argmax(StateAction sa, boolean upper) {
		double value = Double.NEGATIVE_INFINITY;
		State max = null;
		Double tmpValue;
		int i = 0;
		for (State s : model.getSprimes()) {
			i++;
			StateActionState sas = new StateActionState(sa, s);
			if(model.pTilde(sas) < 1){

				if (upper) {
					tmpValue = vUppers.get(s);
				} else {
					tmpValue = vLowers.get(s);
				}
				if (tmpValue == null) {
					if (upper) {
						tmpValue = vMax;
					} else {
						tmpValue = 0.0;
					}
				}
				if (tmpValue > value) {
					value = tmpValue;
					max = s;
				}
			}
		}
//		createUnknownState = i != model.getNbrOfStates() && value != vMax; //In case of not all states checked.
//		if(createUnknownState){
//			max = new State(new Observation(1, 0));
//			max.setInt(0, -5); // Unique unknown state
//		}
		return max;
	}

//	private static double omega(int nsa) {
//		return Math.sqrt((2 * Math.log(Math.pow(2, obsRangeMax) - 2) - Math
//				.log(conf)) / nsa);
//	}

//	private static Map<StateActionState, Double> createPRoof(StateAction sa) {
//		Map<StateActionState, Double> ret = new HashMap<StateActionState, Double>();
//		double prob;
//		for (State s : observedStates) {
//			StateActionState sas = new StateActionState(sa, s);
//			prob = getCount(sas) / getCount(sa);
//			ret.put(sas, prob);
//		}
//		return ret;
//	}
	
//	private static double getFromProbDist(Map<StateActionState, Double> incompletPD,
//										StateActionState sas){
//		Double d = incompletPD.get(sas);
//		return d == null ? 0.0 : d;
//	}

//	private static Integer getCount(StateAction sa) {
//		Integer i = stateActionCounter.get(sa);
//		return i == null ? 0 : i;
//	}

//	private static Integer getCount(StateActionState sas) {
//		Integer i = stateActionStateCounter.get(sas);
//		return i == null ? 0 : i;
//	}

//	private static double obsRew(StateAction sa) {
//		Double d = observedRewards.get(sa);
//		if (d == null) {
//			return 0;
//		} else {
//			return d;
//		}
//	}

//	private void computePolicy() {
//		// TODO Auto-generated method stubs
//	}
//
//	private double computeQPrimeUpper(StateAction sa) {
//		if (observedStateTrans.containsKey(sa)) {
//			return model.reward(sa) + gamma * maxRew / (1 - gamma);
//		} else {
//			return maxRew / 2 + gamma * maxRew / (1 - gamma);
//		}
//	}
//
//	private double computeQPrimeLower(StateAction sa) {
//		if (observedStateTrans.containsKey(sa)) {
//			return model.reward(sa);
//		} else {
//			return maxRew / 2;
//		}
//	}

	public void printQValues(){
		LinkedList<StateAction> keys = new LinkedList<StateAction>(qUppers.keySet());
		Collections.sort(keys, new StateActionComparator());
		for(StateAction sa : keys){
			String s = "S: "+sa.getState().getInt(0)+
						" A: "+sa.getAction().getInt(0) +
						" QUpper: " + qUppers.get(sa) + 
						" QLower: " + qLowers.get(sa);
			System.out.println(s);
		}
	}
	
	public void printValues(){
		LinkedList<State> keys = new LinkedList<State>(vUppers.keySet());
		Collections.sort(keys, new StateComparator());
		for(State s : keys){
			String str = "S: "+s.getInt(0)+
						" VUpper: " + vUppers.get(s) + 
						" VLower: " + vLowers.get(s);
			System.out.println(str);
		}
	}
	
	private class StateComparator implements Comparator<State> {

		@Override
		public int compare(State s1, State s2) {
			return Integer.compare(s1.getInt(0), s2.getInt(0));
		}
		
	}
	
	private class StateActionComparator implements Comparator<StateAction> {

		@Override
		public int compare(StateAction sa1, StateAction sa2) {
			int val = Integer.compare(sa1.getState().getInt(0), sa2.getState().getInt(0));
			if( val == 0 ){
				return Integer.compare(sa1.getAction().getInt(0), sa2.getAction().getInt(0));
			} else {
				return val;
			}
		}
		
	}
	
}