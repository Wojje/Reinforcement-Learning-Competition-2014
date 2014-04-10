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
	
	private final int NORTH = 0, SOUTH = 1,  WEST = 2, EAST = 3, EXIT = 4;
	
	
	//private List<State> states;
	private GWorldStates[][] world;
	private double reward;
	private double deathRew;
	private double succesMoveProb;
	private double livingReward;
	
	public GridWorldMDP(double reward, double deathRew, double livingRew, double successMove){
		this.reward = reward;
		this.deathRew = deathRew;
		this.livingReward = livingRew;
		this.succesMoveProb = successMove;
		createStates();
	}
	
	public GridWorldMDP(){
		this(1, -1, -0.01, 0.8);
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
		states.add(createState(-1, -1)); //Terminal exiting state
		return states;
	}

	@Override
	public List<ActionStep> getActions() {
		
		List<ActionStep> actions = new LinkedList<ActionStep>();
		for(int a=0; a <= 4; a++){
			actions.add(createActionStep(a));
		}
		return actions;
	}

	@Override
	public double reward(State s, State sprime) {
		int place = s.getInt(0);
		if(place == -1){
			return 0;
		}
	
		int futurePlace = sprime.getInt(0);
		if(validPlace(futurePlace) && neighborDir(place, futurePlace) != -1){
			int val = sprime.getInt(1);
			if(val == GWorldStates.WIN.VALUE){
				return reward;
			} else if (val == GWorldStates.DEATH.VALUE){
				return deathRew;
			}
		}
		return livingReward;
	}
	
	
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
		return livingReward;
	}

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
		int dir = neighborDir(place, futurePlace);
		int action = sas.getAction().getInt(0);
		if(place == -1 || place == 5){
			return 0;
		}
		if(place == 3 || place == 7){
			if(futurePlace == -1 && action == 4){
				return 1;
			} else {
				return 0;
			}
		}
		if(futurePlace == place){
			
			if(place == 0 && (action == 0 || action == 2)){
				return succesMoveProb + (1-succesMoveProb)/2.0;
			} else if (place == 8 && (action == 1 || action == 2)){
				return succesMoveProb + (1-succesMoveProb)/2.0;
			} else if (place == 11 && (action == 1 || action == 3)){
				return succesMoveProb + (1-succesMoveProb)/2.0;
			}
			
			if(placeToRow(place) == 0){
				if(action == 0){
					return succesMoveProb;
				} else if (action == 2 || action == 3){
					return (1-succesMoveProb)/2.0;
				}
			} else if (placeToRow(place) == 2){
				if(action == 1){
					return succesMoveProb;
				} else if (action == 2 || action == 3){
					return (1-succesMoveProb)/2.0;
				}
			} else if (placeToCol(place) == 0){
				if(action == 2){
					return succesMoveProb;
				} else if (action == 0 || action == 1){
					return (1-succesMoveProb)/2.0;
				}
			} else if (placeToCol(place) == 3){
				if(action == 3){
					return succesMoveProb;
				} else if (action == 0 || action == 1){
					return (1-succesMoveProb)/2.0;
				}
			} else if (moveTowardsBlock(place, action)){
				return succesMoveProb;
			}
		}
	
		if(futureStatus != 1){
			if(validPlace(futurePlace)){
				if(dir != -1){
					if(action == dir){
						return succesMoveProb;
					} else {
						if( (action== 0 || action == 1) && (dir == 2 || dir == 3)){
							return (1-succesMoveProb)/2.0;
						} else if( (action == 2 || action == 3) && (dir == 0 || dir == 1) ){
							return (1-succesMoveProb)/2.0;
						}
					}
				} 
			}		
		} 
		
		return 0;
	}
	

	private boolean moveTowardsBlock(int place, int action) {
		return (place == 1 && action == 1) ||
				(place == 4 && action == 3) ||
				(place == 6 && action == 2) ||
				(place == 9 && action == 0) ;
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
