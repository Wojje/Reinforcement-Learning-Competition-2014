import java.math.BigDecimal;


public class GamblerMain2 {

	static double gamma = 1.0;
	static BigDecimal p = new BigDecimal(0.40);
	final static int maxStates = 101;
	static BigDecimal[] V = new BigDecimal[maxStates];
	static int[] bestActions = new int[maxStates];
	static double theta = 0.000000000000000000001;
	
	public static void main(String[] args) {
		V[maxStates -1] = new BigDecimal(1.0);
		V[0] = new BigDecimal(0.0);
		for(int i = 1; i < maxStates - 1; i++) {
			V[i] = new BigDecimal(0.0);
		}
		double delta;
		BigDecimal oldValue;
		BigDecimal bestNewValue;
		int k = 0;
		do{
			delta = 0.0;
			
			for (int i = 1; i < maxStates -1 ; i++) {
				oldValue = V[i];
				
				bestNewValue = new BigDecimal(0.0);
				for (int j = 1; j <= i /*&& j <= 100 - i*/; j++) {
					BigDecimal valueWin = V[(i + j) > (maxStates - 1) ? 
							(maxStates - 1): i + j].multiply(p);
					BigDecimal valueLoss = V[(i - j)].multiply(new BigDecimal(1).subtract(p));
					BigDecimal valueTotal = valueLoss.add(valueWin);
					if (bestNewValue.compareTo(valueTotal) < 0) {
						bestNewValue = valueTotal;
						bestActions[i] = j;
					}
				}
				V[i] = bestNewValue;
				if (delta < Math.abs(V[i].subtract(oldValue).doubleValue())) {
					delta = Math.abs(V[i].subtract(oldValue).doubleValue());
				}
			}
			System.out.println("One iteration complete");
			k++;
		} while (k < 10);//(delta > theta);
		
		for (int i = 0; i < maxStates; i++) {
			System.out.println(//"i: " + i + " V: " + V[i] + " best action: " +
					"" + V[i].doubleValue());
		}
	}
}
