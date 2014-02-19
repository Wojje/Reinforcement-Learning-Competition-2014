package testPackage;

import java.util.Random;

public class CoinFlipping {

	/**
	 * This is an ugly test class for testing the propertys of value iteration
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Player gambler = new Player(50, 0.55);
		for(int i = 0; i<50; i++){
			
			gambler.play();
		}
	}

}
