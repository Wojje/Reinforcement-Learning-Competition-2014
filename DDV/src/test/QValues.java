package test;

import java.util.HashMap;
import java.util.Map;

import mdp.GridWorldMDP;
import mdp.MDP;

import utils.ActionStep;
import utils.State;
import utils.StateActionState;

public class QValues {

	private Map<State, Double> values = new HashMap<State, Double>();
	private int h = 200;
	private MDP mdp;
	private double gamma = 0.9;
	private double EPS = 0.001;
	
	
	QValues(MDP mdp){
		this.mdp = mdp;
		for(State s : mdp.getStates()){
			values.put(s, 0.0);
		}
	}
	
	public void valueIteration(){
		double delta;
		double v;
		while(true){
			delta = 0;
			for(State s : mdp.getStates()){
				
				v = values.get(s);
				double vdelta = maxa(s);
				values.put(s, vdelta);
				delta = Math.max(delta, Math.abs(v-vdelta));
				
				if(delta < EPS){
					return;
				}
			}
		}
	}
	
	private double maxa(State s){
		double max = 0;
		for(ActionStep as : mdp.getActions()){
			double sum = 0;
			for(State sprime : mdp.getStates()){
				StateActionState sas = new StateActionState(s, as, sprime);
				sum += mdp.probTransition(sas) * 
				(mdp.reward(s, sprime) * gamma * values.get(sprime));
				
				if(sum > max){
					max = sum;
				}
			}
		}
		return max;
	}
	
	public Map<State, Double> getValues() {
		return values;
	}
	
	
	public static void main(String[] args) {
		
		MDP mdp = new GridWorldMDP();
		QValues qv = new QValues(mdp);
		
		qv.valueIteration();
		
		for(State s : qv.getValues().keySet()){
			System.out.println("S: "+s.getInt(0)+" : "+qv.getValues().get(s));
		}
		
		
		
		
		
	}

}
