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
	private static final int startSample = 4; // IT'S A MAGIC IN ME!!!
	private static int numberOfAlgorithmRuns = startSample;
	private static final double FACTOR = 1.5;

	private static double maxPosReward; // borde varit final
	
	
	public static int NBR_REACHES;
    public static int HABITATS_PER_REACH;
	
    private int actionDims;
    
	boolean optimistic = true;
	
	private int obsRangeMin;
	private static int obsRangeMax;
	private int actRangeMin;
	private int actRangeMax;
	private double maxRew, minRew;
	private static double vMax;

	private State stateZero;

	private static StateAction lastStateAction;

	//private double accuracy = 0.1; // Proper value?
	private static double conf = 0.05; // Woot?

	private static double gamma = 0.9; // Decay of rewards

	private static double convergenceFactor = 0.1;

	private static Map<StateAction, Set<State>> observedStateTrans;

	private static Map<StateAction, Double> qUppers;
	private static Map<State, Double> vUppers;
	
	private Model model;
		
	private int step = 0;

	private Random randGenerator = new Random();
	
	private Map<State, Action> policy = null;
	
	private final boolean DEBUG = false;
	
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
		model = new Model((int) Math.pow(3, theTaskSpec.getNumDiscreteObsDims()), conf);
		
		actionDims = theTaskSpec.getNumDiscreteActionDims(); //reach
		NBR_REACHES = actionDims;  // reach
		HABITATS_PER_REACH = theTaskSpec.getNumDiscreteObsDims()/NBR_REACHES;

		maxPosReward = 11.6*NBR_REACHES + 0.9*NBR_REACHES*HABITATS_PER_REACH;
		
		System.out.println(NBR_REACHES + "---" + HABITATS_PER_REACH);
		actRangeMax = theActRange.getMax();
		actRangeMin = theActRange.getMin();
		obsRangeMax = theObsRange.getMax();
		obsRangeMin = theObsRange.getMin();
		maxRew = /*theRewardRange.getMax() +*/ maxPosReward;
//		minRew = theRewardRange.getMin();

		vMax = maxRew / (1 - gamma);
		
		observedStateTrans = new HashMap<StateAction, Set<State>>();

		policy = new HashMap<State, Action>();

		qUppers = new HashMap<StateAction, Double>();
		vUppers = new HashMap<State, Double>();
	}

	public String agent_message(String arg0) {
		return null;
	}

	public Action agent_start(Observation o) {
		State start = new State(o);
		if (!model.getObservedStates().contains(start)) {
			addActionsToQ(start);
			vUppers.put(start,0.0); //This should really be vMax, but nope!
		}
		model.addStartState(start);

		Action bestAction;
		
		bestAction = new Action(actionDims, 0, 0);
		
		for(int i = 0; i < actionDims; i++) {
			bestAction.setInt(i, 1);
		}
		
		
		
		lastStateAction = new StateAction(start, new ActionStep(bestAction));

		return bestAction;
	}
	

	public Action agent_step(double r, Observation o) {
		r+=maxPosReward;
		step++;
		State nextS = new State(o);
		if (!model.getObservedStates().contains(nextS)) {
			addActionsToQ(nextS);
			vUppers.put(nextS, vMax);
		}
		model.addObservation(lastStateAction.getState(), lastStateAction.getAction(), nextS, r);
		
		ActionStep bestAction = new ActionStep(new Action(actionDims, 0, 0));
		
		if(NBR_REACHES * HABITATS_PER_REACH < 10 && model.NSAhasDoubled(lastStateAction)){
			findQuppers();
//			System.out.println("New policy");
//			System.out.println(lastStateAction + " NSA "+model.NSA(lastStateAction));
		} else {
			if (step > numberOfAlgorithmRuns){
				findQuppers();
//				System.out.println("New policy: "+numberOfAlgorithmRuns);
				numberOfAlgorithmRuns = (int) (numberOfAlgorithmRuns*FACTOR); 
//				System.out.println(numberOfAlgorithmRuns);
//				numberOfAlgorithmRuns += 100;
			}
		}
		
		
		try {
			if (step % 100 == 0) {
				System.out.print(step + ") " );
				System.out.println(totalMassToMove(lastStateAction) + 
						" " + lastStateAction.getState());
			}
//			System.out.println(vUppers);
			
		} catch (ArithmeticException e) {
			
		}		
		bestAction = findBestActionFromQ(nextS);
		
	/*	if(step >= numberOfAlgorithmRuns){
			System.out.println("Antal samples: " + step);
			numberOfAlgorithmRuns=numberOfAlgorithmRuns+100;
		}*/
		lastStateAction = new StateAction(nextS, bestAction);		
		return bestAction; // return chosen action
	}
	
	private void addActionsToQ(State nextS) {
		for(List<Integer> l : 
				Utilities.getActions(nextS.intArray, NBR_REACHES, HABITATS_PER_REACH)) {
			ActionStep a = new ActionStep(new Action(NBR_REACHES, 0));
			for(int i = 0; i < l.size(); i++) {
				a.intArray[i] = l.get(i);
			}
			qUppers.put(new StateAction(nextS,a),vMax);
		}
		
	}

	private void findQuppers() {
		double diff = Double.MAX_VALUE;
		while (diff > convergenceFactor) {
			diff = 0;
			for (State s : model.getObservedStates()) {
				for(List<Integer> al : Utilities.getActions(s.intArray, NBR_REACHES, 
						HABITATS_PER_REACH)) {
					ActionStep a = new ActionStep(new Action(NBR_REACHES,0));
					for(int i = 0; i < al.size(); i++) {
						a.intArray[i] = al.get(i);
					}
					double val = model.reward(new StateAction(s,a)) + gamma * 
							valFromOptProbDist(s, a);
					StateAction sa = new StateAction(s,a);
					double newDiff = Math.abs(val - qUppers.get(sa));
					diff = Math.max(diff, newDiff);
					qUppers.put(sa, val);
				}
			}
		}
		for(State s : model.getObservedStates()) {
			vUppers.put(s, qUppers.get(new StateAction(s,findBestActionFromQ(s))));
		}
		
	}
	
	private double valFromOptProbDist(State s, ActionStep a) {
		StateAction sa = new StateAction (s,a);
		//Find adjusted probabilities
		if (model.NSA(sa) == 0) {
			return vMax;
		}
		
		Map<State,Double> probs = findProbs(sa);
		
		double v = 0.0;
		for(Map.Entry<State, Double> e : probs.entrySet()) {
			ActionStep nextA = findBestActionFromQ(e.getKey());
			Double thisV = qUppers.get(new StateAction(e.getKey(), nextA));
			thisV = thisV == null ? maxRew : thisV;
			v += thisV * e.getValue();
		}
		return v;
	}
	
	private ActionStep findBestActionFromQ(State s) {
		double bestValue = Double.NEGATIVE_INFINITY;
		ActionStep bestAction = null;
		for(List<Integer> actionList : 
			
			Utilities.getActions(s.intArray, NBR_REACHES, HABITATS_PER_REACH)) {
			ActionStep a = new ActionStep(new Action(NBR_REACHES, 0));
			for(int i = 0; i < actionList.size(); i++) {
				a.intArray[i] = actionList.get(i);
			}
			Double newValue = qUppers.get(new StateAction(s,a));
			if (newValue == null) {
				return a;
			}
			if (newValue > bestValue) {
				bestValue = newValue;
				bestAction = a;
			}
		}
		return bestAction;
	}

	private Map<State, Double> findProbs(StateAction sa) {
		//Get real probabilities
		Map<State, Double> realProbs = new HashMap<State, Double>(); 
		int NSA = model.NSA(sa);
		for(State nextS : model.getObservedStates()) {
			int NSAS = model.NSAS(new StateActionState(sa, nextS));
			if (NSAS > 0) {
				realProbs.put(nextS, ((double) NSAS ) / ((double) NSA));
			}
		}
		
		//find optimal adjustment
		return upperP(realProbs, sa);
	}
	
	private double totalMassToMove(StateAction sa) {
		
		Integer NSA = model.NSA(sa);
		if (NSA == 0) {
			throw new ArithmeticException("no nsa counts recorded (massToMove)");
		}
		return Math.max(0.0, 1.0-NSA * 0.05);
//		int stateCount = model.getObservedStates().size();
//		
//		if(stateCount<1000){
//			double den = 2*(Math.log(2*Math.pow(2,stateCount) - 2) - Math.log(conf));
//			return (Math.sqrt((den/((double)NSA)))) / 2.0;
//		}
//		else{
//			return Math.sqrt( 2*(stateCount*Math.log(2)- Math.log(conf))/ (double) NSA);
//		}
//			
		
	}
	
	private Map<State,Double> upperP(Map<State,Double> realProbs, StateAction sa) {
		double totalMassToMove = totalMassToMove(sa);
		Map<State,Double> retProbs = new HashMap<State, Double>(realProbs);
		
		double massToUnvisited = model.missingMass(sa);
		
		while (totalMassToMove > 0.0) {
			State sWorst = findWorstNextState(sa, retProbs);
			State sBest = findBestNextState(sa, retProbs, massToUnvisited > 0.0);
			if (sWorst == sBest) break;
			//Only move as much as "fits"
			if (retProbs.get(sWorst) == null) {
				retProbs.put(sWorst, 0.0);
			}
			double massToMove = Math.min(totalMassToMove, retProbs.get(sWorst));
			Double bestProb = retProbs.get(sBest);
			bestProb = bestProb == null ? 0.0 : bestProb;
			massToMove = Math.min(massToMove, 1-bestProb);
			if (model.NSAS(new StateActionState(sa,sBest)) == 0) 
				massToMove = Math.min(massToMove, massToUnvisited);
			retProbs.put(sWorst, retProbs.get(sWorst) - massToMove);
			retProbs.put(sBest, bestProb + massToMove);
			if (retProbs.get(sBest) == 1.0) break;
			totalMassToMove -= massToMove;
			if (model.NSAS(new StateActionState(sa, sBest)) == 0) {
				massToUnvisited -= massToMove;
			}
		}
		return retProbs;
	}
	
	private State findWorstNextState(StateAction sa, Map<State,Double> probs) {
		State worst = null;
		Double worstValue = Double.POSITIVE_INFINITY;
		for(State nextS : model.getObservedStates()) {
			if (probs.get(nextS) != null && probs.get(nextS) != 0.0) {
				Double v = vUppers.get(nextS);
				
				if (v < worstValue && probs.get(nextS) != 0.0) {
					worst = nextS;
					worstValue = v;
				}
			} else {
				
			}
		}
		if (worst == null) {
			throw new ArithmeticException("Found no worst state");
		}
		return worst;
	}
	
	private State findBestNextState(StateAction sa, Map<State,Double> probs, 
			boolean allowZeroTargets) {
		State best = null;
		Double bestValue = Double.NEGATIVE_INFINITY;
		for(State nextS : model.getObservedStates()) {
			Double v = vUppers.get(nextS);
			if (v > bestValue && (probs.get(sa) == null || probs.get(sa) != 1.0)) {
				if (allowZeroTargets || model.NSAS(new StateActionState(sa,nextS))!= 0) {
					best = nextS;
					bestValue = v;
				}
			} 
		}
		if (best == null) {
			throw new ArithmeticException("Found no best state");
		}
		return best;
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