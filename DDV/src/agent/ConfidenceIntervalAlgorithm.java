package agent;

import java.util.HashMap;
import java.util.HashSet;
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

import utils.ActionStep;
import utils.State;
import utils.StateAction;
import utils.StateActionState;

public class ConfidenceIntervalAlgorithm implements AgentInterface{

	private int obsRangeMin;
	private static int obsRangeMax;
	private int actRangeMin;
	private int actRangeMax;
	private double maxRew, minRew;
	private static double vMax;

	private State stateZero;

	private static StateAction lastStateAction;

	private double accuracy = 0.001; // Proper value?
	private static double conf = 0.001; // Woot?

	private static double gamma = 0.1; // Decay of rewards

	private static double convergenceFactor = 0.001;

	private static List<State> observedStates;
	private static Map<StateAction, Set<State>> observedStateTrans;
	private static Map<StateActionState, Integer> stateActionStateCounter;
	private static Map<StateAction, Integer> stateActionCounter;
	private static Map<StateAction, Double> observedRewards;

	private static Map<StateAction, Double> qUppers;
	private static Map<StateAction, Double> qLowers;
	private static Map<State, Double> vUppers;
	private static Map<State, Double> vLowers;
	private Map<State, Double> muUppers;
	
	private Random randGenerator = new Random();

	
	public void agent_cleanup() {
		// TODO Auto-generated method stub
	}

	
	public void agent_end(double reward) {
		// TODO Auto-generated method stub
	}

	
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

		vMax = maxRew / (1 - gamma);

		observedRewards = new HashMap<StateAction, Double>();
		observedStateTrans = new HashMap<StateAction, Set<State>>();
		observedStates = new LinkedList<State>();

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
		stateZero = new State(o);
		observedStates.add(stateZero);
		int theIntAction = randGenerator.nextInt(actRangeMax-1);
        Action bestAction = new Action(1, 0, 0);
        bestAction.intArray[0] = theIntAction;
		return bestAction;
	}
	
	public Action agent_step(double r, Observation o) {
		State sprime = new State(o);
		observedStates.add(new State(o));
		observedRewards.put(lastStateAction, r);
		updateObservedStateTrans(lastStateAction, sprime);

		updateQUpper();
		updateQLower();
		updateVUpper();
		updateVLower();
		

		int theIntAction = randGenerator.nextInt(actRangeMax-1);
        Action bestAction = new Action(1, 0, 0);
        bestAction.intArray[0] = theIntAction;

		double bestValue = Double.MIN_VALUE;
		// increment N(s,a) counter
        for (StateAction sa : qUppers.keySet()) {
        	if (sa.getState() == sprime) {
        		double value = qUppers.get(sa);
        		if (value > bestValue) {
        			bestAction = sa.getAction();
        			bestValue = value;
        		}
        	}
        }

        
		

		return bestAction; //return chosen action
	}

	private static void updateObservedStateTrans(StateAction lastStateAction,
			State sprime) {
		Set<State> sass = observedStateTrans.get(lastStateAction);
		if (sass == null) {
			sass = new HashSet<State>();
			observedStateTrans.put(lastStateAction, sass);
		}
		sass.add(sprime);
	}

	private void updateStateActionCounter(StateAction sa) {
		if (stateActionCounter.containsKey(sa)) {
			stateActionCounter.put(sa, stateActionCounter.get(sa) + 1);
		} else {
			stateActionCounter.put(sa, 1);
		}
	}

	private void updateStateActionStateCounter(StateActionState sas) {
		if (stateActionStateCounter.containsKey(sas)) {
			stateActionStateCounter.put(sas,
					stateActionStateCounter.get(sas) + 1);
		} else {
			stateActionStateCounter.put(sas, 1);
		}
	}

	public static void updateQUpper() {
		iterateQ(true);
	}

	private static void updateQLower() {
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
			}
			else {
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
			}
			else {
				v.put(state, value);
			}
		}
		vLowers = v;
	}

	private void updateMuUpper() {
		// Equation 8
	}
	
	private static void iterateQ(boolean upper) {
		boolean converged = false;
		while(!converged) {
			for (StateAction sa : observedStateTrans.keySet()) {
				double tmp = upper ? qUppers.get(sa) : qLowers.get(sa);
				if (Math.abs(tmp - updateQ(sa, upper)) < convergenceFactor) {
					converged = true;
				} else {
					converged = false;
				}
			}
		}
	}

	private static double updateQ(StateAction sa, boolean upper) {
		// Equation 3 and 4
		double tmp;
		Map<StateActionState, Double> pTildes = computePTildes(sa, true);
		Map<State, Double> vals = new HashMap<State, Double>();
		for (StateAction saPrime : observedStateTrans.keySet()) {
			Double d = vals.get(saPrime.getState());
			double val = (d == null ? Double.MIN_VALUE : d);
			if (upper) {
				Double q = qUppers.get(sa);
				tmp = (q == null ? vMax : q);
			} else {
				Double q = qLowers.get(sa);
				tmp = (q == null ? vMax : q);
			}

			if (tmp > val) {
				vals.put(saPrime.getState(), tmp);
			}

		}
		double sum = 0;
		for (State s : vals.keySet()) {
			double val = vals.get(s);
			sum += pTildes.get(new StateActionState(sa, s))
					* Math.max(val, vMax);
		}
		if (upper) {
			return qUppers.put(sa, obsRew(sa) + gamma * sum);
		} else {
			return qLowers.put(sa, obsRew(sa) + gamma * sum);
		}
	}

	private static Map<StateActionState, Double> computePTildes(StateAction sa,
			boolean upper) {
		int nsa = stateActionCounter.get(sa);
		double deltaOmega = omega(nsa) / 2;

		Map<StateActionState, Double> pRoof = createPRoof(sa);
		Map<StateActionState, Double> pTilde = new HashMap<StateActionState, Double>(
				pRoof);

		List<State> sPrimes = new LinkedList<State>();
		for (StateActionState sas : pRoof.keySet()) {
			if (pRoof.get(sas) < 1) {
				sPrimes.add(sas.getSprime());
			}
		}

		while (deltaOmega > 0) {

			StateActionState sasFloor = new StateActionState(sa, argmin(pTilde,
					upper));
			StateActionState sasRoof = new StateActionState(sa, argmax(sPrimes,
					pTilde, upper));
			double sasrval = 1 - pTilde.get(sasRoof);
			double sasfval = pTilde.get(sasFloor);
			double zeta = Math.min(Math.min(sasrval, sasfval), deltaOmega);
			pTilde.put(sasFloor, sasfval - zeta);
			pTilde.put(sasRoof, sasrval + zeta);
			deltaOmega = deltaOmega - zeta;
		}

		return pTilde;
	}

	private static State argmin(Map<StateActionState, Double> pTilde, boolean upper) {
		double value = Double.MAX_VALUE;
		State min = null;
		Double tmpValue;

		for (StateActionState sas : pTilde.keySet()) {
			State s = sas.getSprime();
			if (pTilde.get(sas) > 0) {
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
				if (tmpValue < value) {
					value = tmpValue;
					min = s;
				}
			}
		}
		return min;
	}

	private static State argmax(List<State> sPrimes,
			Map<StateActionState, Double> pTilde, boolean upper) {
		double value = Double.MIN_VALUE;
		State max = null;
		Double tmpValue;
		for (StateActionState sas : pTilde.keySet()) {
			State s = sas.getSprime();
			if (sPrimes.contains(s) && pTilde.get(sas) < 1) {
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

	private static double omega(int nsa) {
		return Math
				.sqrt((2 * Math.log(Math.pow(2, obsRangeMax) - 2) - Math
						.log(conf)) / nsa);
	}

	private static Map<StateActionState, Double> createPRoof(StateAction sa) {
		Map<StateActionState, Double> ret = new HashMap<StateActionState, Double>();
		double prob;
		for (State s : observedStates) {
			StateActionState sas = new StateActionState(sa, s);
			prob = getCount(sas) / getCount(sa);
			ret.put(sas, prob);
		}
		return ret;
	}

	private static Integer getCount(StateAction sa) {
		Integer i = stateActionCounter.get(sa);
		return i == null ? 0 : i;
	}

	private static Integer getCount(StateActionState sas) {
		Integer i = stateActionStateCounter.get(sas);
		return i == null ? 0 : i;
	}

	private static double obsRew(StateAction sa) {
		Double d = observedRewards.get(sa);
		if (d == null) {
			return 0;
		} else {
			return d;
		}
	}

	private void computePolicy() {
		// TODO Auto-generated method stubs
	}

	private double computeQPrimeUpper(StateAction sa) {
		if (observedStateTrans.containsKey(sa)) {
			return observedRewards.get(sa) + gamma * maxRew / (1 - gamma);
		} else {
			return maxRew / 2 + gamma * maxRew / (1 - gamma);
		}
	}

	private double computeQPrimeLower(StateAction sa) {
		if (observedStateTrans.containsKey(sa)) {
			return observedRewards.get(sa);
		} else {
			return maxRew / 2;
		}
	}

}