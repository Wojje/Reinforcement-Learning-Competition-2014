package agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.types.Action;

import utils.ActionStep;
import utils.State;
import utils.StateAction;
import utils.StateActionState;
import utils.Utilities;

public class Logic {
	
	private final int NBR_REACHES = 3;
	private final int HABITATS_PER_REACHES = 3;

	private int nbrOfStates = 100000000; //placeholder
	private double conf = 0.05;
	private double discount = 1.0;
	private double convergenceFactor = 0.1;
	private double vMax;

	private static Map<StateAction, Double> qUppers;
	private static Map<State, Double> vUppers;

	private static Map<State, Set<ActionStep>> observedActions;
	private static Map<StateAction, Set<State>> observedStateTrans;

	private Map<State, Integer> NS;
	private Map<StateAction, Integer> NSA;
	private Map<StateActionState, Integer> NSAS;

	private Map<StateAction, Double> rewards;

	public Logic(TaskSpec theTaskSpec) {
		NSA = new HashMap<StateAction, Integer>();
		NSAS = new HashMap<StateActionState, Integer>();
		rewards = new HashMap<StateAction, Double>();
		observedStateTrans = new HashMap<StateAction, Set<State>>();
		observedActions = new HashMap<State, Set<ActionStep>>();
		
		vMax = theTaskSpec.getRewardRange().getMax() / (1 - discount);

	}

	public void updateNS(State state) {
		Integer ns = NS.get(state);
		NS.put(state, ns == null ? 1 : ns + 1);
	}

	public void updateNSA(State state, ActionStep action) {
		StateAction sa = new StateAction(state, action);
		Integer nsa = NSA.get(sa);
		NSA.put(sa, nsa == null ? 1 : nsa + 1);
	}

	public void updateNSAS(State state, ActionStep action, State sprime) {
		StateActionState sas = new StateActionState(state, action, sprime);
		Integer nsas = NSAS.get(sas);
		NSA.put(sas, nsas == null ? 1 : nsas + 1);
	}

	public void updateReward(State state, ActionStep action, double r) {
		StateAction sa = new StateAction(state, action);
		Double oldReward = rewards.get(sa);
		rewards.put(sa, oldReward == null ? r : oldReward - (oldReward - r)
				/ NSA.get(sa));
	}

	public void updateObservedStateTrans(State state, ActionStep action,
			State sprime) {
		StateAction sa = new StateAction(state, action);
		if (observedStateTrans.containsKey(sa)) {
			observedStateTrans.get(sa).add(sprime);
		} else {
			Set<State> newSet = new HashSet<State>();
			newSet.add(sprime);
			observedStateTrans.put(sa, newSet);
		}
	}

	public void updateObservedActions(State state, ActionStep action) {
		if (observedActions.containsKey(state)) {
			observedActions.get(state).add(action);
		} else {
			Set<ActionStep> newSet = new HashSet<ActionStep>();
			newSet.add(action);
			observedActions.put(state, newSet);
		}
	}

	public void update() {
		iterateQ();
		updateV();
	}

	private void iterateQ() {
		boolean done = false;
		while (!done) {
			boolean allConverged = true;
			for (StateAction sa : qUppers.keySet()) {
				double tmp = qUppers.get(sa);
				if (observedStateTrans.keySet().contains(sa)) {
					updateQ(sa);
				}
				double newQ = qUppers.get(sa);
				if (Math.abs(tmp - newQ) > convergenceFactor) {
					allConverged = false;
				}
			}
			if (allConverged) {
				done = true;
			}
		}
	}

	private void updateQ(StateAction sa) {
		Map<State, Double> pRoof = createPRoof(sa);
		Map<State, Double> pTilde = new HashMap<State, Double>(pRoof);
		double sum = P(sa, pRoof, pTilde);
		for (State sprime : pTilde.keySet()) {
			sum += pTilde.get(sprime) * vUppers.get(sprime);
		}
		qUppers.put(sa, discount * sum + rewards.get(sa));
	}

	private double P(StateAction sa, Map<State, Double> pRoof,
			Map<State, Double> pTilde) {
		double deltaOmega = omega(sa) / 2;
		double zeta;
		double sum = 0;

		List<State> sPrimes = createSPrimes(pRoof);
		while (deltaOmega > 0) {
			// List<State> sPrimes = createSPrimes(pRoof);
			State sUnder = argmin(sa, pTilde);
			Double sUnderValue = pTilde.get(sUnder);
			State sOver = argmax(sPrimes);
			Double sOverValue = pTilde.get(sOver);

			if (vUppers.get(sOver) > vMax) {
				zeta = Math.min(Math.min(1 - sOverValue, sUnderValue),
						deltaOmega);
				pTilde.put(sOver, sOverValue + zeta);
				pTilde.put(sUnder, sUnderValue - zeta);
				deltaOmega -= zeta;
			} else {
				zeta = Math.min(sUnderValue, deltaOmega);
				pTilde.put(sUnder, sUnderValue - zeta);
				sum += vMax * zeta;
				deltaOmega -= zeta;
			}
		}
		return sum;
	}

	private static void updateV() {
		double max = Double.NEGATIVE_INFINITY;
		for (State s : observedActions.keySet()) {
			for (ActionStep a : observedActions.get(s)) {
				StateAction sa = new StateAction(s, a);
				double temp = qUppers.get(sa);
				if (temp > max) {
					max = temp;
				}
			}
			vUppers.put(s, max);
		}
	}

	private Map<State, Double> createPRoof(StateAction sa) {
		Map<State, Double> pRoof = new HashMap<State, Double>();
		for (State sprime : observedStateTrans.get(sa)) {
			StateActionState sas = new StateActionState(sa, sprime);
			pRoof.put(sprime, (double)NSAS.get(sas) / NSA.get(sa));
		}
		return pRoof;
	}

	private List<State> createSPrimes(Map<State, Double> pRoof) {
		List<State> sPrimes = new LinkedList<State>();
		for (State s : pRoof.keySet()) {
			if (pRoof.get(s) < 1) {
				sPrimes.add(s);
			}
		}
		return sPrimes;
	}

	private double omega(StateAction sa) {
		return Math.sqrt((2 * Math.log(Math.pow(2, nbrOfStates) - 2) - Math
				.log(conf)) / NSA.get(sa));
	}

	private State argmin(StateAction sa, Map<State, Double> pTilde) {
		double value = Double.POSITIVE_INFINITY;
		State min = null;
		Double tmpValue;

		for (State sprime : observedStateTrans.get(sa)) {
			if (pTilde.get(sprime) > 0) {
				tmpValue = vUppers.get(sprime);
				if (tmpValue < value) {
					value = tmpValue;
					min = sprime;
				}
			}
		}
		return min;
	}

	private State argmax(List<State> sPrimes) {
		double value = Double.NEGATIVE_INFINITY;
		State max = null;
		Double tmpValue;
		for (State sprime : sPrimes) {
			tmpValue = vUppers.get(sprime);
			if (tmpValue > value) {
				value = tmpValue;
				max = sprime;
			}
		}
		return max;
	}
	
	public ActionStep computeBestAction(State obs){
		double biggest = Double.NEGATIVE_INFINITY;
		ActionStep chosenAction=null;
		for(List<Integer> a: Utilities.getActions(obs.intArray, NBR_REACHES, HABITATS_PER_REACHES)){
			ActionStep action = new ActionStep(new Action(NBR_REACHES,0));
			for(int i = 0; i < a.size(); i++) {
				action.setInt(i, a.get(i));
			}
			StateAction sa = new StateAction(obs, new ActionStep(action));
			Double lookUp = qUppers.get(sa);

			if(lookUp == null){
				lookUp=vMax;
			}
			if(lookUp > biggest){
				biggest = lookUp;
				chosenAction = action;
			}
		}


		return chosenAction;
	}

}
