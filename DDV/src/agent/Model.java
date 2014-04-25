package agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.rlcommunity.rlglue.codec.types.Observation;

import utils.ActionStep;
import utils.State;
import utils.StateAction;
import utils.StateActionState;

public class Model {

	private Map<StateActionState, Double> pTilde;
	private Map<StateActionState, Double> pRoof;
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
		NSA = new HashMap<>();
		NSAS = new HashMap<>();
		reward = new HashMap<>();
		observedStates = new HashSet<>();
		observedStateTrans = new HashMap<>();
		
		this.nbrOfStates = nbrOfStates;
		this.conf = conf;
	}
	
	public void addStartState(Observation o){
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

		this.reward.put(sa, reward);
		
		updateObservedTrans(new StateAction(prev, act), sPrime);
		

	}

	private void updateObservedTrans(StateAction sa, State sPrime) {
		Set<State> sprimes = observedStateTrans.get(sa);
		if(sprimes == null){
			sprimes = new HashSet<>();
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
//		System.out.println("pTilde: "+d2);
		return d2;
	}
	
	public void updatePTilde(StateActionState sas, double val){
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
		pTilde = new HashMap<>(pRoof);
	}
	
	public double omega(StateAction sa){
		return Math.sqrt( (2 * Math.log(  Math.pow(2, nbrOfStates) - 2 ) - Math.log(conf) ) 
				/ NSA.get(sa));
	}
	
	public Set<Entry<StateActionState, Double>> pRoofEntries(){
		return pRoof.entrySet();
	}
	
	public Set<State> getObservedStates() {
		return observedStates;
	}
	
	public int getNbrOfStates() {
		return nbrOfStates;
	}
	
	public Set<StateAction> getObservedTransKeys(){
		return observedStateTrans.keySet();
	}

	private void createPRoof(StateAction sa) {
		Map<StateActionState, Double> ret = new HashMap<StateActionState, Double>();
		double prob;
		for (State s : observedStates) {
			StateActionState sas = new StateActionState(sa, s);
			double nsasVal = NSAS(sas);
			double nsaVal = NSA(sa);
//			System.out.println("NSAS: "+ nsasVal + " NSA " + nsaVal);
			prob = NSAS(sas) / ((double) NSA(sa));
			ret.put(sas, prob);
		}
		pRoof = ret;
	}

}
