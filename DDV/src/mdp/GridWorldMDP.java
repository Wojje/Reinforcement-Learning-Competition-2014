package mdp;

import java.util.LinkedList;
import java.util.List;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import utils.ActionStep;
import utils.State;
import utils.StateActionState;

/**
 * 
 * GridWorld example as seen below
 * E means empty, B is blocked, D is death, W is win, S starting state.
 *  
 * E E E W
 * E B E D
 * S E E E
 *
 */
public class GridWorldMDP implements MDP {
	
	private enum GWorldStates {
		EMPTY(0), BLOCKED(1), DEATH(2), START(3), WIN(4);	
		public final int VALUE;
		private GWorldStates(int i){
			VALUE = i;
		}
	}
	
	private final int NORTH = 0, SOUTH = 1,  WEST = 2, EAST = 3;
	
	
	//private List<State> states;
	private GWorldStates[][] world;
	private double reward;
	private double deathRew;
	private double succesMoveProb = 0.8;
	
	public GridWorldMDP(double reward, double deathRew, double successMove){
		this.reward = reward;
		this.deathRew = deathRew;
		this.succesMoveProb = successMove;
		createStates();
	}
	
	public GridWorldMDP(){
		this(1, -1, 0.8);
	}
	
	
	private void createStates(){
		
		GWorldStates E = GWorldStates.EMPTY;
		GWorldStates W = GWorldStates.WIN;
		GWorldStates B = GWorldStates.BLOCKED;
		GWorldStates S = GWorldStates.START;
		GWorldStates D = GWorldStates.DEATH;
		world = new GWorldStates[][]
				{ { E, E, E, W },
				  { E, B ,E, D },
				  { S, E ,E, E }
				};
	}
	
	private State createState(int place, int status){
		State s = new State(new Observation(10, 10));
		s.setInt(1, status);
		s.setInt(0, place);
		return s;
	}
	
	private ActionStep createActionStep(int action){
		ActionStep as = new ActionStep(new Action(10, 10));
		as.setInt(0, action);
		return as;
	}
	

	@Override
	public List<State> getStates() {
		List<State> states = new LinkedList<State>();
		int place = 0;
		for(int r = 0; r < world.length; r++){
			for( int c = 0; c < world[r].length; c++){
				switch (world[r][c]) {
				case EMPTY:
					states.add(createState(place, GWorldStates.EMPTY.VALUE));
					break;
				case BLOCKED:
					states.add(createState(place, GWorldStates.BLOCKED.VALUE));
					break;
				case DEATH:
					states.add(createState(place, GWorldStates.DEATH.VALUE));
					break;
				case START:
					states.add(createState(place, GWorldStates.START.VALUE));
					break;
				case WIN:
					states.add(createState(place, GWorldStates.WIN.VALUE));
					break;
				default:
					System.out.println("SOMETHING WENT WROOOONG");
					break;
				}
				place++;
			}
		}
		return states;
	}

	@Override
	public List<ActionStep> getActions() {
		
		List<ActionStep> actions = new LinkedList<ActionStep>();
		for(int a=0; a <= 3; a++){
			actions.add(createActionStep(a));
		}
		return actions;
	}

	@Override
	public double reward(State s, State sprime) {
		int place = s.getInt(0);
		int futurePlace = sprime.getInt(0);
		if(validPlace(futurePlace) && neighborDir(place, futurePlace) != -1){
			int val = sprime.getInt(1);
			if(val == GWorldStates.WIN.VALUE){
				return reward;
			} else if (val == GWorldStates.DEATH.VALUE){
				return deathRew;
			}
		}
		return 0;
	}
	
	/*
	@Override
	public double reward(State s) {
		int place = s.getInt(0);
		if(validPlace(place)) {
			int val = s.getInt(1);
			if(val == GWorldStates.WIN.VALUE){
				return reward;
			} else if (val == GWorldStates.DEATH.VALUE){
				return deathRew;
			}
		}
		return 0;
	}*/

	@Override
	public State getStartingState() {
		int place = 0;
		for(int r = 0; r < world.length; r++){
			for( int c = 0; c < world[r].length; c++){
				if(world[r][c] == GWorldStates.START){
					return createState(place, 3);
				}
				place++;
			}
		}
		return null;//Random?
	}


	@Override
	public double probTransition(StateActionState sas) {
		int place = sas.getState().getInt(0);
		int futurePlace = sas.getSprime().getInt(0);
		int futureStatus = sas.getSprime().getInt(1);
		if(futureStatus != 1){	
			if(validPlace(futurePlace)){
				int dir = neighborDir(place, futurePlace);
				if(dir != -1){
					int a = sas.getAction().getInt(0);
					if(a == dir){
						return succesMoveProb;
					} else {
						if( (a == 0 || a == 1) && (dir == 2 || dir == 3)){
							return (1-succesMoveProb)/2.0;
						} else if( (a == 2 || a == 3) && (dir == 0 || dir == 1) ){
							return (1-succesMoveProb)/2.0;
						}
					}
				}
			}		
		}
		
		return 0;
	}
	
	private int neighborDir(int place, int futurePlace) {
		int pr = placeToRow(place);
		int pc = placeToCol(place);
		int fpr = placeToRow(futurePlace);
		int fpc = placeToCol(futurePlace);		
		int rdiff = fpr - pr;
		int cdiff = fpc - pc;
		if(Math.abs(rdiff) == 1){
			if(Math.abs(cdiff) > 0){
				return -1;
			} else {
				if(rdiff > 0){
					return SOUTH;
				} else {
					return NORTH;
				}
			}
		} else if(Math.abs(cdiff) == 1){
			if(Math.abs(rdiff) > 0){
				return -1;
			} else {
				if(cdiff > 0){
					return EAST;
				} else {
					return WEST;
				}
			}	
		} else {
			return -1;
		}
		
	}

	public int placeToRow(int place){
		return place / (world[0].length);
	}
	
	public int placeToCol(int place){
		return place % (world[0].length);
	}
	
	private boolean validPlace(int place){
		int c = placeToCol(place);
		int r = placeToRow(place);
		return r >= 0 && r < world.length && c >= 0 && c < world[r].length;
	}

}
