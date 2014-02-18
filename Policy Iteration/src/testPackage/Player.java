package testPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Player {
	int money; // current state
	double propability;
	boolean playing;
	
	
	Random randgen = new Random();
	int flip, nrOfPlays=0;
	ArrayList<Integer> stateVisited;
	HashMap<Integer, Double> valueFunction = new HashMap<Integer, Double>(); // Corresponds to the average reward recivied in a game where a certain state was visited
	
	// Available actions
	HashMap<Integer, ArrayList<Integer>> actions;
	
	
	public Player (int money, double propability){
		this.money=money;
		this.propability=propability;
		buildActions();
	}
	
	private void buildActions(){
		actions = new HashMap<Integer, ArrayList<Integer>>();
		for(int i = 1; i<100; i++){
			actions.put(i, new ArrayList<Integer>());
			for(int j=0;j < i || j<(100-j); j++){
				ArrayList<Integer> actionList = actions.get(i);
				actionList.add(j);
				actions.put(i, actionList);
			}
		}
	}
	
	public void play(){
		playing = true;
		stateVisited = new ArrayList<Integer>();
		nrOfPlays++;
		
		while(playing){
			System.out.println("The gamler now has " + money + "dollar");
			flip = randgen.nextInt(1);
			
			if (gameOver()){
				handleReward();
				playing=false;
			}
		}
		// 1 corresponds to HEAD	
	}
	
	private void handleReward(){
		for(int state : stateVisited){
			if(!valueFunction.containsKey(state))
				valueFunction.put(state, 1.00);
			else{
				double winningRate = valueFunction.get(state);
				if (money == 100)
					winningRate = ((nrOfPlays-1)*winningRate+1)/(nrOfPlays);
				else
					winningRate = ((nrOfPlays-1)*winningRate)/(nrOfPlays);					
			}
		}
	}
	
	private boolean gameOver(){
		if (money == 0 || money == 100)
			return true;
		else return false;
	}
	
	
	public enum state{
		terminate;
	}
}
