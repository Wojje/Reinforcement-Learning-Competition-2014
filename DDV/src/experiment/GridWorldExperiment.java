package experiment;

import java.util.Collections;
import java.util.LinkedList;

import mdp.GridWorldMDP;
import mdp.MDP;
import utils.ActionStep;
import utils.State;
import utils.StateActionState;
import agent.ConfidenceIntervalAlgorithm;

public class GridWorldExperiment {
	private MDP mdp;
	private int maxAct;
//	private static State currentState;
	private static final int sampleLength = 10000;

	private int steps;
	private final boolean optimistic = true;
	public GridWorldExperiment(MDP mdp, int nbrOfSteps){
		this.mdp = mdp;
		this.steps = nbrOfSteps;
	}
	
	public void runExperiment(){
		
		int minState = 0;
		int maxState = mdp.getStates().size()-1;
		int minAct = 0;
		this.maxAct = mdp.getActions().size()-1;
		double maxRew = 1.0;
		ConfidenceIntervalAlgorithm cia = new ConfidenceIntervalAlgorithm(minState, maxState, minAct, maxAct, maxRew, optimistic);
		
		State start = mdp.getStartingState();
		State s = null;
		ActionStep a = new ActionStep(cia.agent_start(start));	
		State sprime = getNextState(start, a);
		double rew;
		if(sprime == null){
			s = start;
			rew = 0;
		} else {
			rew = mdp.reward(sprime);
			s = sprime;
		}
		
		
		for(int step = 1; step < steps; ){
			
			a = new ActionStep(cia.agent_step(rew, s));
			sprime = getNextState(s, a);
			
			//System.out.println("S: "+s.getInt(0) + " A: "+a.getInt(0) + " S' "+sprime.getInt(0));
			
			
			
			if(sprime == null){
//				sprime = s;
				rew = 0;
			} else {
				rew = mdp.reward(sprime);
				s = sprime;
				step++;
			}
			
		}
		
		cia.printReward();
		cia.printPolicy();
		
	}
	
	private State getNextState(State s, ActionStep a) {
		
		if(s.getInt(0) == -1){
			return mdp.getStartingState();
		}
		
		LinkedList<StateProb> possible = new LinkedList<StateProb>();
		for(State sprime : mdp.getStates()){
			StateActionState sas = new StateActionState(s, a, sprime);
			double prob = mdp.probTransition(sas);
			if(prob > 0){
				possible.add(new StateProb(sprime, prob));
			}
		}
		
		double r = Math.random();
		double probLim = 0;
		Collections.sort(possible);
		for(StateProb spos : possible){
			double prob = spos.getProb();
			if(r <= probLim+prob){
				return spos.getS();
			} else {
				probLim += prob;
			}
		}
		
		return s;
	}
	
	private class StateProb implements Comparable<StateProb>{
		private State s;
		private double prob;
		public StateProb(State s, double prob){
			this.s = s;
			this.prob = prob;
		}
		
		public double getProb() {
			return prob;
		}
		
		public State getS() {
			return s;
		}
		@Override
		public int compareTo(StateProb o) {
			return Double.compare(o.prob, prob); //Intentional swap, biggest should be first
		}
	}
	
	
	
	
	
	
	public static void main(String[] args) {
		GridWorldExperiment exp = new GridWorldExperiment(new GridWorldMDP(), sampleLength);
		exp.runExperiment();
		
//		State s = new State(new Observation(1,0));
//		s.setInt(0, 9);
//		ActionStep a = new ActionStep(new Action(1,0));
//		a.setInt(0, 0);
//		for(int i = 0; i < 10000; i++){
//			State sprime = exp.getNextState(s, a);
//			if(sprime.getInt(0) == 4){
//				System.out.println("Haxxed");
//			}
//		}
	
	}
	
	


}
