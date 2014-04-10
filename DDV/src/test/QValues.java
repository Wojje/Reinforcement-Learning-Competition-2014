package test;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mdp.GridWorldMDP;
import mdp.MDP;
import utils.ActionStep;
import utils.State;
import utils.StateActionState;

public class QValues {

	private Map<State, Double> values = new HashMap<State, Double>();
	private Map<State, Qs> qvalues = new HashMap<State, Qs>();
	private int h = 200;
	private MDP mdp;
	private double gamma = 1;
	private double EPS = 0.00000001;
	
	
	QValues(MDP mdp){
		this.mdp = mdp;
		for(State s : mdp.getStates()){
			values.put(s, 0.0);
			qvalues.put(s, new Qs());
		}
		values.put(mdp.getStates().get(3), 1.00);
		values.put(mdp.getStates().get(7), -1.00);
	}
	
	public void valueIteration(int i){
		double delta;
		double v;
		List<State> states = mdp.getStates();
		State win = states.get(3);
		State death = states.get(7);
		State blocked = states.get(5);
		states.remove(win);
		states.remove(death);
		states.remove(blocked);
		while(true){
			delta = 0;
			for(State s : states){
				v = values.get(s);
				double vdelta = maxa(s);
				values.put(s, vdelta);
//				System.out.println("vdelta: "+vdelta);
				delta = Math.max(delta, Math.abs(v-vdelta));
//				System.out.println("delta:" + delta);
				
				
			}
			if(delta < EPS || i == 0){
				values.put(mdp.getStates().get(3), 0.00);
				values.put(mdp.getStates().get(7), 0.00);
				return;
			}
			i--;
		}
	
	}
	
	
	private double maxa(State s){
		double max = Double.NEGATIVE_INFINITY;
		for(ActionStep as : mdp.getActions()){
			double sum = 0;
			for(State sprime : mdp.getStates()){
				StateActionState sas = new StateActionState(s, as, sprime);
				double res = mdp.probTransition(sas) * (mdp.rewardSingleState(s) + gamma * values.get(sprime));
				if(s.getInt(0) == 7){
					System.out.println("prob: "+mdp.probTransition(sas));
					System.out.println("R(s,a, s'): "+(mdp.rewardSingleState(s)));
					System.out.println("future value: "+values.get(sprime));
				}
				sum += res;
			
			}
			if(s.getInt(0) == 7){
				System.out.println(sum);
			}
		
			if(sum > max){
				max = sum;
			}
		}
		return max;
	}
	
	private void calcQValues(){
		List<State> states = mdp.getStates();
		State win = states.get(3);
		State death = states.get(7);
		State blocked = states.get(5);
		states.remove(win);
		states.remove(death);
		states.remove(blocked);
		for(State s : states){
			for(ActionStep a : mdp.getActions()){
				double qval = calcQ(s, a);
				Qs curr = qvalues.get(s);
				curr.setQValue(a, qval);
			}
		}
	}
	
	private Double calcQ(State s, ActionStep a) {
		double sum = 0;
		for(State sprime : mdp.getStates()){
			StateActionState sas = new StateActionState(s, a, sprime);
			sum += mdp.probTransition(sas) * (mdp.rewardSingleState(sprime)+gamma*values.get(sprime));
		}
		return sum;
	}

	public Map<State, Double> getValues() {
		return values;
	}
	
	public Map<State, Qs> getQvalues() {
		return qvalues;
	}
	
	
	public static void main(String[] args) {
		
		MDP mdp = new GridWorldMDP();
		QValues qv = new QValues(mdp);
		
		qv.valueIteration(-1);
		
		for(State s : mdp.getStates()){
			DecimalFormat df = new DecimalFormat("##.##");
			System.out.println("S: "+s.getInt(0)+" : "+df.format(qv.getValues().get(s)));
		}
		
		qv.calcQValues();
		for(State s : mdp.getStates()){
			System.out.println("S: "+s.getInt(0)+" : "+ qv.getQvalues().get(s).toString());
		}
		
	}
	
	private class Qs {
		double[] qvs = new double[5];
		
		public void setQValue(ActionStep a, double value){
			qvs[a.getInt(0)] = value;
		}
		
		public String toString(){
			DecimalFormat df = new DecimalFormat("##.##");
			return "N: "+df.format(qvs[0]) +
					" S: "+df.format(qvs[1]) +
					" W: "+df.format(qvs[2]) +
					" E: "+df.format(qvs[3]) +
					" A: "+df.format(qvs[4]);
		}
	}

}
