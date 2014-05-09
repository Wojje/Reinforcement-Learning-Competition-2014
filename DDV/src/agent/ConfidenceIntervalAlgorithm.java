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
	private static final int startSample = 1000; // IT'S A MAGIC IN ME!!!
	private static int numberOfAlgorithmRuns = startSample;

	private static double magicPostivConstant; // borde varit final
	
	
	public static int NBR_REACHES;
    public static int HABITATS_PER_REACHES;
	
    private int actionDims;
    
	private double totalReward = 0;
	boolean optimistic = false;
	
	private int obsRangeMin;
	private static int obsRangeMax;
	private int actRangeMin;
	private int actRangeMax;
	private double maxRew, minRew;
	private static double vMax;

	private State stateZero;

	private static StateAction lastStateAction;

	//private double accuracy = 0.1; // Proper value?
	private static double conf = 0.35; // Woot?

	private static double gamma = 0.9; // Decay of rewards

	private static double convergenceFactor = 0.01;

	private static Map<StateAction, Set<State>> observedStateTrans;

	private static Map<StateAction, Double> qUppers;
	private static Map<StateAction, Double> qLowers;
	private static Map<State, Double> vUppers;
	private static Map<State, Double> vLowers;
	
	private Model model;
	
	private boolean checkedAllStates;
	private boolean createUnknownState;
	
	private int step = 0;

	private Random randGenerator = new Random();
	
	private Map<State, Action> policy = null;
	
	private final boolean DEBUG = false;
	private boolean newStateAction = false;
	
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
		if(DEBUG){
			System.out.println("------Q-VALUES------");
			printQValues();
			System.out.println("------V-values------");
			printValues();
			System.out.println("------POLICY------");
			printPolicy();
			System.out.println("------------");
			
		}
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
 * �ndrade ifr�n obsRangeMax till observationsDimension
 */
		model = new Model((int) Math.pow(3, theTaskSpec.getNumDiscreteObsDims()), conf);
		
		actionDims = theTaskSpec.getNumDiscreteActionDims(); //reach
		NBR_REACHES = actionDims;  // reach
		HABITATS_PER_REACHES = theTaskSpec.getNumDiscreteObsDims()/NBR_REACHES;

		magicPostivConstant = 11.6*NBR_REACHES + 0.9*NBR_REACHES*HABITATS_PER_REACHES;
		
		System.out.println(NBR_REACHES + "---" + HABITATS_PER_REACHES);
		actRangeMax = theActRange.getMax();
		actRangeMin = theActRange.getMin();
		obsRangeMax = theObsRange.getMax();
		obsRangeMin = theObsRange.getMin();
		maxRew = theRewardRange.getMax() + magicPostivConstant;
//		minRew = theRewardRange.getMin();

		vMax = maxRew / (1 - gamma);

		observedStateTrans = new HashMap<StateAction, Set<State>>();

		policy = new HashMap<State, Action>();


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
//		Observation newObservation = o.duplicate();
		stateZero = new State(o);
//		observedStates.add(stateZero);
		model.addStartState(o);
		
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
//		updateStateActionCounter(lastStateAction);

		return bestAction;
	}
	

	public Action agent_step(double r, Observation o) {
		r=r+magicPostivConstant;
		step++;
		totalReward+=r;
		State sprime = new State(o);
//		observedStates.add(new State(o));
//		observedRewards.put(lastStateAction, r);
//		updateObservedStateTrans(lastStateAction, sprime);
//		updateStateActionStateCounter(new StateActionState(lastStateAction,
//				sprime));
		if(!model.getObservedStates().contains(sprime)){
			addActions(sprime);
		}
		model.addObservation(lastStateAction.getState(), lastStateAction.getAction(), sprime, r);
//		updateQ(sa, upper)
		
		Action bestAction = new Action(actionDims, 0, 0);
		
		/*
		 * Ta inte bort, allt blir kass
		 */

	/*	if(newStateAction && step > startSample){
			System.out.println("Antal samples: " + step);
			performPlanning();
			computePolicy();				
			newStateAction=false;
		}
		else*/ if(step >= numberOfAlgorithmRuns){
			System.out.println("Antal samples: " + step);
			performPlanning();
			computePolicy();
			numberOfAlgorithmRuns=numberOfAlgorithmRuns+100;
		}
		
		
		if(step<startSample){
			List<List<Integer>> possibleActions =  Utilities.getActions(sprime.intArray, NBR_REACHES, HABITATS_PER_REACHES);
			int randomIndex = (int) (Math.random()*possibleActions.size());
			List<Integer> randomAction = possibleActions.get(randomIndex);
			Action action = new Action(NBR_REACHES,0);
			for(int i = 0; i < randomAction.size(); i++) {
				action.setInt(i,randomAction.get(i));
			}
			bestAction = action;
		} else {
			
			Action tmp = policy.get(sprime);
			if(tmp == null){
				bestAction = new Action(actionDims, 0, 0);
				for(int i = 0; i < actionDims; i++) {
					bestAction.setInt(i, 1);
				}
			} else {
				bestAction = tmp;
			}

			if(DEBUG && step % 1000 == 0){
				System.out.println("------Q-VALUES------");
				printQValues();
				System.out.println("------V-values------");
				printValues();
				System.out.println("------POLICY------");
				printPolicy();
				System.out.println("------------");
				
			}
		}
		
		lastStateAction = new StateAction(sprime, new ActionStep(bestAction));
		newStateAction = !model.getObservedTransKeys().contains(lastStateAction);
		
//		System.out.println(bestAction.intArray);

//		List<Integer> l = Utilities.getActions(o.intArray, NBR_REACHES, HABITATS_PER_REACHES).get(random.nextInt(
//				Utilities.getActions(o.intArray, NBR_REACHES, HABITATS_PER_REACHES).size()));
//		for(int i = 0; i < actionDims; i++) {
//			bestAction.setInt(i, l.get(i));
//		}
		
		return bestAction; // return chosen action
	}
	
	
	private void addActions(State s){
		List<List<Integer>> possibleAction = Utilities.getActions(s.intArray, NBR_REACHES, HABITATS_PER_REACHES);
		Action action = new Action(NBR_REACHES,0);
		for(List<Integer> list : possibleAction){
			for(int i = 0; i < list.size(); i++) {
				action.setInt(i,list.get(i));
			}
			if(optimistic){
				qUppers.put(new StateAction(s,new ActionStep(action)), vMax);
			}else{
				qLowers.put(new StateAction(s, new ActionStep(action)), 0.0);
			}
		}
	}

	public void performPlanning() {
		updateQ(lastStateAction, optimistic);
		if(optimistic){
			updateQUpper();
			updateVUpper();
		}
		else{
			updateQLower();
			updateVLower();
		}
	}

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

	private void iterateQ(boolean upper) {
		boolean converged = false;
		while (!converged) {
			boolean allConverged = true;
			for (StateAction sa : qUppers.keySet()) {
				double tmp = upper ? qUppers.get(sa) : qLowers.get(sa);
				if(model.getObservedTransKeys().contains(sa)){
					updateQ(sa, upper);
				}
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
		double sum = 0.0;
		
		computePTildes(sa, upper);
		
		for(State obs : model.getObservedStates()){
			StateActionState obsobs = new StateActionState(sa, obs);
			if(model.pTilde(obsobs)==0.0){
				continue;
			}
			else{
				sum+= model.pTilde(obsobs)*computeActionMaxQ(obs, upper);
			}
		}
		if(upper){
			qUppers.put(sa, model.reward(sa) + gamma * sum);
		}
		else{
			qLowers.put(sa, model.reward(sa) + gamma * sum);
		}
		
	}
	
	private double computeActionMaxQ(State obs, boolean upper){
		double biggest = Double.NEGATIVE_INFINITY;
//		for(int a = actRangeMin; a <= actRangeMax;a++){
		for(List<Integer> a: Utilities.getActions(obs.intArray, NBR_REACHES, HABITATS_PER_REACHES)){
			Action action = new Action(NBR_REACHES,0);
			for(int i = 0; i < a.size(); i++) {
				action.setInt(i,a.get(i));
			}
			StateAction sa = new StateAction(obs, new ActionStep(action));
			Double lookUp;
			if(upper)
				lookUp = qUppers.get(sa);
			else
				lookUp = qLowers.get(sa);
			if(lookUp == null){
				lookUp=vMax;
			}
			if(lookUp > biggest){
				biggest = lookUp;
			}
		}
		
		
		return biggest;
	}

	private Action computeMaxAction(State obs, boolean upper){
		double biggest = Double.NEGATIVE_INFINITY;
		Action chosedAction=null;
		for(List<Integer> a: Utilities.getActions(obs.intArray, NBR_REACHES, HABITATS_PER_REACHES)){
			Action action = new Action(NBR_REACHES,0);
			for(int i = 0; i < a.size(); i++) {
				action.setInt(i, a.get(i));
			}
			StateAction sa = new StateAction(obs, new ActionStep(action));
			Double lookUp;
			
			if(upper){
				lookUp = qUppers.get(sa);
			} else {
				lookUp = qLowers.get(sa);
			}
			
			if(lookUp == null){
				lookUp=vMax;
			}
			if(lookUp > biggest){
				biggest = lookUp;
				chosedAction = action;
			}
		}
		
		
		return chosedAction;
	}
	
	private void computePTildes(StateAction sa,
			boolean upper) {
		
		model.initPRoofPTilde(sa);
		
//		if(true){
//			return;
//		}
		
		double deltaOmega = model.omega(sa)/2.0;
		double zeta;
		double sasFloorValue;
		double sasRoofValue;

		while (deltaOmega > 0) {

			
			model.createSetOfSprimes(sa);
			

			State min = argmin(sa, upper);
			if(min == null){
//				System.out.println("Oj, min var null!");
				return;
			}
			model.removeSprime(min);
			StateActionState sasFloor = new StateActionState(sa, min);

			State max = argmax(sa, upper);
//			if(max == null){
//				System.out.println("Oj, max var null!");
//				for(State s:model.getSprimes()){
//					System.out.print(" S: "+ s.getInt(0));
//					System.out.println();
//				}
//				
//				return;
//			}
			StateActionState sasRoof = new StateActionState(sa, max);
			
			
			sasRoofValue = model.pTilde(sasRoof);
			sasFloorValue = model.pTilde(sasFloor);
			zeta = Math.min(Math.min(1-sasRoofValue, sasFloorValue), deltaOmega);
			

			
			model.updatePTilde(sasFloor, sasFloorValue - zeta);
			model.updatePTilde(sasRoof, model.pTilde(sasRoof) + zeta);
			deltaOmega = deltaOmega - zeta;
			}
	}

	private State argmin(StateAction sa, boolean upper) {
		double value = Double.POSITIVE_INFINITY;
		State min = null;
		Double tmpValue;

		for(State s : model.getObservedStates()){
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
		return max;
	}

	public void printQValues(){
		LinkedList<StateAction> keys = new LinkedList<StateAction>(qUppers.keySet());
		Collections.sort(keys, new StateActionComparator());
		for(StateAction sa : keys){
			String s = "S: "+sa.getState()+
						" A: "+sa.getAction() +
						" QUpper: " + qUppers.get(sa) + 
						" QLower: " + qLowers.get(sa);
			System.out.println(s);
		}
	}
	
	public void printValues(){
		LinkedList<State> keys = new LinkedList<State>(vUppers.keySet());
//		Collections.sort(keys, new StateComparator());
		for(State s : keys){
			String str = "S: "+s+
						" VUpper: " + vUppers.get(s) + 
						" VLower: " + vLowers.get(s);
			System.out.println(str);
		}
	}
	
	public void printReward(){
		System.out.println("Total Reward: " + totalReward);
	}
	
	
	public void computePolicy(){
		LinkedList<State> keys = new LinkedList<State>(model.getObservedStates());
		 
//		Collections.sort(keys, new StateComparator());
		Action a;
		Map<State, Action> policy = new HashMap<State, Action>();
		for(State s : keys){
			a = computeMaxAction(s, optimistic);
			policy.put(s, a);
		}
		this.policy = policy;
		
	}
	
	public void printPolicy(){
		String str;
		if(policy == null){
			System.out.println("No policy yet");
		}
		
		LinkedList<State> keys = new LinkedList<State>(model.getObservedStates());
		 
//		Collections.sort(keys, new StateComparator());
		for(State s : keys){
			str = "S: "+ s;
			Action a = policy.get(s);
			str += " A: " + a;
			str += " NSA ";
			if(a != null){
			 str += model.NSA(new StateAction(s, new ActionStep(policy.get(s))));
			} else {
				str += " No action ";
			}
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