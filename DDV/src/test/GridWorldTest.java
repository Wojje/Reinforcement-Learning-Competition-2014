package test;

import static org.junit.Assert.*;

import java.util.List;

import mdp.GridWorldMDP;
import mdp.MDP;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utils.ActionStep;
import utils.State;
import utils.StateActionState;

public class GridWorldTest {
	
	private GridWorldMDP mdp;
	private final double EPS = 0.001;

	@Before
	public void setUp() throws Exception {
		mdp = new GridWorldMDP(1, -1, 0.8);
	
	}

	@Test
	public void testGetStates() {
		List<State> l = mdp.getStates();
		assertTrue(l.size() == 12);
	}
	
	@Test
	public void testStateReps(){
		List<State> l = mdp.getStates();
		int i = 0;
		for(State s : l){
			assertTrue(s.getInt(0) == i);
			i++;
		}
	
		assertTrue(l.get(0).getInt(1) == 0);
		assertTrue(l.get(1).getInt(1) == 0);
		assertTrue(l.get(2).getInt(1) == 0);
		assertTrue(l.get(3).getInt(1) == 4);
		assertTrue(l.get(4).getInt(1) == 0);
		assertTrue(l.get(5).getInt(1) == 1);
		assertTrue(l.get(6).getInt(1) == 0);
		assertTrue(l.get(7).getInt(1) == 2);
		assertTrue(l.get(8).getInt(1) == 3);
		assertTrue(l.get(9).getInt(1) == 0);
		assertTrue(l.get(10).getInt(1) == 0);
		assertTrue(l.get(11).getInt(1) == 0);
	}
	
	@Test
	public void testActionReps(){
		List<ActionStep> as = mdp.getActions();
		assertTrue(as.size() == 4);
		
		assertTrue(as.get(0).getInt(0) == 0);
		assertTrue(as.get(1).getInt(0) == 1);
		assertTrue(as.get(2).getInt(0) == 2);
		assertTrue(as.get(3).getInt(0) == 3);
		
		
	}
	
	@Test
	public void testPlaceToRow(){
		for(int p = 0; p < 12; p++){
			if(p < 4){
				System.out.println("p: " +p + " row: " + mdp.placeToRow(p));
				assertTrue(mdp.placeToRow(p) == 0);
			} else if (p < 8){
				assertTrue(mdp.placeToRow(p) == 1);
			} else if (p < 12){
				assertTrue(mdp.placeToRow(p) == 2);
			}
		}
	}
	
	@Test
	public void testPlaceToCol(){
		for(int p = 0; p < 12; p++){
			if(p == 0 || p == 4 || p == 8){
				assertTrue(mdp.placeToCol(p) == 0);
			} else if (p == 1 || p == 5 || p == 9){
				assertTrue(mdp.placeToCol(p) == 1);
			} else if (p == 2 || p == 6 || p == 10){
				System.out.println("p: "+p + " col: " + mdp.placeToCol(p));
				assertTrue(mdp.placeToCol(p) == 2);
				
			} else if (p == 3 || p == 7 || p == 11){
				assertTrue(mdp.placeToCol(p) == 3);
			}
		}
	}
	
	@Test
	public void testRewFunc(){
		List<State> states = mdp.getStates();
		List<ActionStep> as = mdp.getActions();
		for(State s : states){
			for(ActionStep a : as){
				for(State sp : states){
					int place = s.getInt(0);
					int futurePlace = sp.getInt(0);
					
					if(place == futurePlace || place == 3 || place == 7){
						continue;
					}
					
					if(place == 2 && futurePlace == 3){
						double rew = mdp.reward(s, sp);
						assertEquals(1, rew, EPS);
					} else if (place == 6 && futurePlace == 7){
						double rew = mdp.reward(s, sp);
						assertEquals(-1, rew, EPS);
					} else if (place == 11 && futurePlace == 7){
						double rew = mdp.reward(s, sp);
						assertEquals(-1, rew, EPS);
					} else {
						double rew = mdp.reward(s, sp);
						System.out.println("p: " + place +" fp: " + futurePlace + " rew: "+rew);
						assertEquals(0, rew, EPS);
					}
					
					
				}
			}
		}
		
	}

	
	@Test
	public void testProbFunc(){
		List<ActionStep> as = mdp.getActions();
		List<State> states = mdp.getStates();
		
		//EMPTY(0), BLOCKED(1), DEATH(2), START(3), WIN(4);	
		//NORTH = 0, SOUTH = 1,  WEST = 2, EAST = 3;
		/**
		 * E E E W
		 * E B E D
		 * S E E E
		 */
		
		// (0,0)
		testProbCase(states.get(0), as.get(0), states.get(0), 0.0);
		testProbCase(states.get(0), as.get(1), states.get(1), 0.1);
		testProbCase(states.get(0), as.get(1), states.get(0), 0.0);
		testProbCase(states.get(0), as.get(2), states.get(0), 0.0);
		testProbCase(states.get(0), as.get(3), states.get(1), 0.8);
		testProbCase(states.get(0), as.get(3), states.get(4), 0.1);
		
		// (0,1)
		testProbCase(states.get(1), as.get(0), states.get(1), 0.0);
		testProbCase(states.get(1), as.get(0), states.get(0), 0.1);
		testProbCase(states.get(1), as.get(0), states.get(2), 0.1);
		testProbCase(states.get(1), as.get(1), states.get(5), 0.0);
		testProbCase(states.get(1), as.get(1), states.get(0), 0.1);
		testProbCase(states.get(1), as.get(1), states.get(2), 0.1);
		testProbCase(states.get(1), as.get(2), states.get(0), 0.8);
		testProbCase(states.get(1), as.get(2), states.get(5), 0.0);
		testProbCase(states.get(1), as.get(2), states.get(1), 0.0);
		testProbCase(states.get(1), as.get(3), states.get(2), 0.8);
		testProbCase(states.get(1), as.get(3), states.get(5), 0.0);
		testProbCase(states.get(1), as.get(3), states.get(0), 0.0);
		
		// (0,2)
		/**
		 * E E E W
		 * E B E D
		 * S E E E
		 */
		//NORTH = 0, SOUTH = 1,  WEST = 2, EAST = 3;
		testProbCase(states.get(6), as.get(0), states.get(2), 0.8);
		testProbCase(states.get(6), as.get(0), states.get(7), 0.1);
		testProbCase(states.get(6), as.get(0), states.get(5), 0.0);
		testProbCase(states.get(6), as.get(1), states.get(10), 0.8);
		testProbCase(states.get(6), as.get(1), states.get(7), 0.1);
		testProbCase(states.get(6), as.get(1), states.get(10), 0.8);
		testProbCase(states.get(6), as.get(2), states.get(5), 0.0);
		testProbCase(states.get(6), as.get(2), states.get(2), 0.1);
		testProbCase(states.get(6), as.get(2), states.get(10), 0.1);
		testProbCase(states.get(6), as.get(3), states.get(7), 0.8);
		testProbCase(states.get(6), as.get(3), states.get(2), 0.1);
		testProbCase(states.get(6), as.get(3), states.get(10), 0.1);
		testProbCase(states.get(6), as.get(3), states.get(5), 0.0);
		
		
	}
	
	private void testProbCase(State s, ActionStep as, State sprime, double expec){
		StateActionState sas = new StateActionState(s, as, sprime);
//		System.out.println(mdp.probTransition(sas));
//		System.out.println("S:p "+s.getInt(0) + " A: "+ as.getInt(0) + " S':p "+sprime.getInt(0));
		assertEquals(expec, mdp.probTransition(sas), EPS);
//		assertTrue(true);
	}

}
