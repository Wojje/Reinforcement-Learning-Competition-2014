package agent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;


public class DDV implements AgentInterface {

	private double accuracy = 0.001; //Proper value?
	private double upperConf = 0.001; //Woot?
	private double lowerConf = 0.001; //Woot?
		
	private double gamma = 1.0; //Decay of rewards
	
	private List<Integer> observedStates;
	private Map<StateAction, Integer> observedStateTrans;
	private Map<StateAction, Double> observedRewards;
	
	private Map<Integer, DoubleTuple> values;
	
	private Map<StateAction, DoubleTuple> qs;

	
	
    private int obsRangeMin;
    private int obsRangeMax;
    private int actRangeMin;
    private int actRangeMax;
    private double maxRew;
    private double minRew;
    private double Rroof;
    
    private StateAction lastStateAction;
	
	
	
	@Override
	public void agent_cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void agent_end(double reward) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void agent_init(String taskSpec) {
		TaskSpec theTaskSpec=new TaskSpec(taskSpec);
		System.out.println("DDV agent parsed the task spec.");
		System.out.println("Observation have "+theTaskSpec.getNumDiscreteObsDims()+" integer dimensions");
		System.out.println("Actions have "+theTaskSpec.getNumDiscreteActionDims()+" integer dimensions");
		IntRange theObsRange = theTaskSpec.getDiscreteObservationRange(0);
		System.out.println("Observation (state) range is: "+theObsRange.getMin()+" to "+theObsRange.getMax());
		IntRange theActRange=theTaskSpec.getDiscreteActionRange(0);
		System.out.println("Action range is: "+theActRange.getMin()+" to "+theActRange.getMax());
		DoubleRange theRewardRange=theTaskSpec.getRewardRange();
		System.out.println("Reward range is: "+theRewardRange.getMin()+" to "+theRewardRange.getMax());
		
		actRangeMax = theActRange.getMax();
		actRangeMin = theActRange.getMin();
		obsRangeMax = theObsRange.getMax();
		obsRangeMin = theObsRange.getMin();
		maxRew = theRewardRange.getMax();
		minRew = theRewardRange.getMin();
		Rroof = maxRew * 0.5;
		
		observedRewards = new HashMap<>();
		observedStateTrans = new HashMap<>();
		observedStates = new LinkedList<>();
		
		values = new HashMap<>();
		
		
	}
	


	@Override //What is this shit?
	public String agent_message(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action agent_start(Observation o) {
		int sprime = o.getInt(0);
		observedStates.add(new Integer(sprime));
		
		
		return null;
	}

	@Override
	public Action agent_step(double r, Observation o) {
		int sprime = o.getInt(0);
		observedStates.add(new Integer(sprime));
		observedRewards.put(lastStateAction, r);
		observedStateTrans.put(lastStateAction, sprime);
		
		//Do update (line 1 in pseudo
		update();
		if(valueDeltaSatisfactory()){
			computePolicy();
		} 
		
		for(Integer i : observedStates){
			for(int a = actRangeMin; a <= actRangeMax; a++){
				StateAction sa = new StateAction(i, a);
				DoubleTuple qprime = computeQPrime(sa);
				double deltaQ = deltaDoubles(qprime);
				
				
				
			}
		}
		
		
		return null;
	}
	
	private DoubleTuple computeQPrime(StateAction sa) {
		if(observedStateTrans.containsKey(sa)){
			//Case 2 i artikeln
			return new DoubleTuple(1337.0, 42.0);
		} else {
			return new DoubleTuple(Rroof + gamma * maxRew/(1 - gamma), Rroof);
		}
	
	}

	private double deltaDoubles(DoubleTuple dt){
		//Abs?
		return dt.getFirst() - dt.getSecond();
	}
	
	//Should this be without params?
	private boolean valueDeltaSatisfactory() {
		
		return true;
	}

	private void computePolicy() {
		// TODO Auto-generated method stub
		
	}

	private void update(){
		
	}
	

	
	
	private class StateAction {
		private int s, a;
		public StateAction(int state, int action){
			s = state;
			a = action;
		}
		public int getAction() {
			return a;
		}
		public int getState() {
			return s;
		}
		@Override
		public boolean equals(Object obj) {
			if(obj.getClass() != StateAction.class){
				return false;
			} else {
				StateAction as = (StateAction) obj;
				return s == as.s && a == as.a;
			}
		}
		@Override
		public int hashCode() {
			return 8011 * s + 9587 * a;
		}
	}
	
	private class DoubleTuple {
		double fst;
		double snd;
		public DoubleTuple(double first, double second){
			fst = first;
			snd = second;
		}
		
		public double getFirst() {
			return fst;
		}
		
		public double getSecond() {
			return snd;
		}
	}
	
	
}
