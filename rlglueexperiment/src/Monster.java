import java.util.Random;


public class Monster {
	private int position;
	private int movedsince;
	private Random rand;
	private int gridWidth;
	private int maxState;
	private boolean debug = false;
	public Monster(int width) {
		movedsince = 0;
		rand = new Random();
		gridWidth = width;
		position = rand.nextInt(width*width);
		maxState = width * width - 1;
	}
	
	public void nextEpisode() {
		movedsince++;
			//Chance to move grows linearly up to a max of 30% after 35 episodes
		double chanceToMove = (((movedsince > 35) ? 35 : movedsince) / 35.0) * 0.3;
		if (debug) System.out.println("Chance to move is now " + chanceToMove);
		if (rand.nextDouble() < chanceToMove) {
			//move
			int direction = rand.nextInt(4);
			switch (direction) {
			case 0:
				//left
				if (position % gridWidth == 0) {
					//fail to move - don't fall off the edge
				} else {
					position--;
					movedsince = 0;
				}
				break;
			case 1:
				//right
				if (position % gridWidth ==5) {
					//fail to move - don't fall off the edge
				} else {
					position++;
					movedsince = 0;
				}
				break;
			case 2:
				//up
				if (position < gridWidth) {
					//fail to move - don't fall off the edge
				} else {
					position -= gridWidth;
					movedsince = 0;
				}
				break;
			case 3: 
				//down
				if (position > maxState - gridWidth) {
					//fail to move - don't fall off the edge					
				} else {
					position += gridWidth;
					movedsince = 0;
				}
				break;
			}
			movedsince = 0;
		} else {
			//don't move
			
		}
		
	}
	public int getState() { 
		return position;
	}
}
