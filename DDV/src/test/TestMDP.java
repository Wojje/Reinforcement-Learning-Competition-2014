package test;

import java.util.List;

import utils.ActionStep;
import utils.State;
import utils.StateActionState;
import mdp.GridWorldMDP;
import mdp.MDP;

public class TestMDP {

	public static void main(String[] args) {
		MDP gw = new GridWorldMDP(1.0, -1.0, -0.01, 0.8);
		List<ActionStep> actions = gw.getActions();
		List<State> states = gw.getStates();
		for (State s : states) {
			printState(s);
		}
		for (State s1 : states) {
			for (State s2 : states) {
				for (ActionStep a : actions) {
					double prob = gw.probTransition(new StateActionState(s1, a, s2));
					if (prob != 0.0) {
						System.out.println(prob);
						
					}
				}

			}
		}

	}

	private static void printState(State s) {
		System.out.println("Place " + s.getInt(0) + " Status: " + s.getInt(1));
	}

}
