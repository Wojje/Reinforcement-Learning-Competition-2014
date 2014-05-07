package agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.rlcommunity.rlglue.codec.types.Observation;

import utils.ActionStep;
import utils.State;
import utils.StateAction;
import utils.StateActionState;

public class Model {

	private int debug=0;
	
	private static final double DISCOUNT = 0.9;
	
	private Map<StateActionState, Double> pTilde;
	private Map<StateActionState, Double> pRoof;
	private List<State> sPrimes;
	private Map<StateAction, Integer> NSA;
	private Map<StateActionState, Integer> NSAS;
	private Map<StateAction, Double> reward;
	private static Map<StateAction, Set<State>> observedStateTrans;
	private Set<State> observedStates;
	private double conf;
	private int nbrOfStates;

	public Model(int nbrOfStates, double conf) {
		// pTilde = new HashMap<>();
		// pRoof = new HashMap<>();
		NSA = new HashMap<StateAction, Integer>();
		NSAS = new HashMap<StateActionState, Integer>();
		reward = new HashMap<StateAction, Double>();
		observedStates = new HashSet<State>();
		observedStateTrans = new HashMap<StateAction, Set<State>>();

		this.nbrOfStates = nbrOfStates;
		this.conf = conf;
	}

	public void addStartState(Observation o) {
		observedStates.add(new State(o));
	}

	public void addObservation(State prev, ActionStep act, Observation o,
			double reward) {

		State sPrime = new State(o);
		observedStates.add(sPrime);

		StateAction sa = new StateAction(prev, act);
		int nsa = NSA(sa);
		NSA.put(sa, nsa + 1);

		StateActionState sas = new StateActionState(sa, sPrime);
		int nsas = NSAS(sas);
		NSAS.put(sas, nsas + 1);		
		
		updateReward(sa, reward);
		
		updateObservedTrans(new StateAction(prev, act), sPrime);

	}

	private void updateReward(StateAction sa, double reward) {
		double d = reward(sa);
		double discountedRew;
		double oldReward = 0;
		int visits = NSA.get(sa);

		if (this.reward.containsKey(sa)) {
			oldReward = this.reward.get(sa);
		}

		if(this.reward.containsKey(sa)){
			discountedRew = d * DISCOUNT;
			discountedRew += reward * (1 - DISCOUNT);
		} else {
			discountedRew = reward;
		}
		this.reward.put(sa, (oldReward*(visits) + discountedRew) / (visits+1));
		
	}

	private void updateObservedTrans(StateAction sa, State sPrime) {
		Set<State> sprimes = observedStateTrans.get(sa);
		if (sprimes == null) {
			sprimes = new HashSet<State>();
			observedStateTrans.put(sa, sprimes);
		}
		sprimes.add(sPrime);

	}

	public double pRoof(StateActionState sas) {
		Double d = pRoof.get(sas);
		return d == null ? 0 : d;
	}

	public double pTilde(StateActionState sas) {
		Double d = pTilde.get(sas);
		double d2 = d == null ? 0 : d;
		if(d2 < 0.0000001){
			d2=0;
		}
		return d2;
	}

	public void updatePTilde(StateActionState sas, double val) {
		pTilde.put(sas, val);
	}

	public double reward(StateAction sa) {
		Double r = reward.get(sa);
		return r == null ? 0 : r;
	}

	public int NSAS(StateActionState sas) {
		Integer times = NSAS.get(sas);
		if (times == null) {
			times = 0;
		}
		return times;
	}

	public int NSA(StateAction sa) {
		Integer times = NSA.get(sa);
		if (times == null) {
			times = 0;
		}
		return times;
	}

	public void initPRoofPTilde(StateAction sa) {
		createPRoof(sa);
		pTilde = new HashMap<StateActionState, Double>(pRoof);
	}

	public double omega(StateAction sa) {
//		return Math.sqrt((2 * Math.log(Math.pow(2, nbrOfStates) - 2) - Math
//				.log(conf)) / NSA.get(sa));
		if(NSA.get(sa)==null){
			System.out.println("bajskorv");
		}
		return Math.sqrt((2 * Math.log(Math.pow(2, observedStates.size()+1) - 2) - Math
				.log(conf)) / NSA.get(sa));
	}

	public Set<Entry<StateActionState, Double>> pRoofEntries() {
		return pRoof.entrySet();
	}

	public Set<State> getObservedStates() {
		return observedStates;
	}
	
	public List<State> getSprimes() {
		return sPrimes;
	}
	
	public Set<State> getPTildeSprimes(){
		Set<State> ret = new HashSet<State>();
		for(StateActionState sas : pTilde.keySet()){
			ret.add(sas.getSprime());
		}
		return ret;
	}

	public int getNbrOfStates() {
		return nbrOfStates;
	}

	public Set<StateAction> getObservedTransKeys() {
		return observedStateTrans.keySet();
	}

	private void createPRoof(StateAction sa) {
		Map<StateActionState, Double> ret = new HashMap<StateActionState, Double>();
		double prob;
		for (State s : observedStates) {
			StateActionState sas = new StateActionState(sa, s);
			double numberOfStateActionStates = NSAS(sas);
			double numberOfStateActions = NSA(sa);
			prob = numberOfStateActionStates / numberOfStateActions;
			ret.put(sas, prob);
		}
		
		
		
/*		
		if(observedStates.size() < nbrOfStates){
			State unknown = new State(new Observation(1, 0));
			unknown.setInt(0, -5);
			ret.put(new StateActionState(sa, unknown), 0.0);
		}
*/	
		pRoof = ret;
/*		if(debug%10000 == 0){
			System.out.println("Antal loper i pRoof: " + debug);
			for(Entry<StateActionState,Double> e : pRoof.entrySet()){
				System.out.println("State: " + e.getKey().getState().getInt(0) + " Action: " + e.getKey().getAction().getInt(0) + " Future state: " +
						e.getKey().getSprime().getInt(0) + " Likelyhood: " +  e.getValue());
			}
		}
		debug++;*/
	}
	
	public void printPtilde(){
		//if(debug%10000 == 0){
			System.out.println("Ptilde: ");
			for(Entry<StateActionState,Double> e : pTilde.entrySet()){
				System.out.print("State: " + e.getKey().getState().getInt(0));
				System.out.print(" Action: " + e.getKey().getAction().getInt(0));
				if(e.getKey().getSprime() != null)
					System.out.print(" Future state: " + e.getKey().getSprime().getInt(0));
				else
					System.out.print(" Future state: " + "NULL");
				System.out.println(" Likelyhood: " +  e.getValue());
			}
		//}
		//debug++;
	}
	

	public void createSetOfSprimes(StateAction sa) {
		sPrimes = new LinkedList<State>();
		for (StateActionState sas : pRoof.keySet()) {
			if (pRoof.get(sas) < 1) {
				sPrimes.add(sas.getSprime());
			}
		}
	}

	public void removeSprime(State min){
		sPrimes.remove(min);
	}
	
}
