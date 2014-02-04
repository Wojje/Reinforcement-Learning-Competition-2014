/*
 * Copyright 2008 Brian Tanner
 * http://rl-glue-ext.googlecode.com/
 * brian@tannerpages.com
 * http://brian.tannerpages.com
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
* 
*  $Revision: 676 $
*  $Date: 2009-02-08 18:15:04 -0700 (Sun, 08 Feb 2009) $
*  $Author: brian@tannerpages.com $
*  $HeadURL: http://rl-glue-ext.googlecode.com/svn/trunk/projects/codecs/Java/examples/skeleton-sample/SkeletonAgent.java $
* 
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;

/**
 *
 * @author Brian Tanner
 */
public class SkeletonAgent implements AgentInterface {
	boolean debug = false;
    Random randGenerator = new Random();
    Action lastAction;
    Observation lastObservation;
    
    //From state to action
    HashMap<Integer,Action> previousActions;
    //From state to (action to value)
    HashMap <Integer, HashMap<Integer, Double>> actionValues;
    private double alpha = 0.1;
    int obsRangeMin;
    int obsRangeMax;
    int actRangeMin;
    int actRangeMax;

    private void initActionValues() {
		actionValues = new HashMap<Integer, HashMap<Integer, Double>>();
		
		for (int i = obsRangeMin; i <= obsRangeMax; i++) {
			HashMap<Integer, Double> tempHashMap = new HashMap<Integer, Double>();
			for (int j = actRangeMin; j <= actRangeMax; j++) {
				tempHashMap.put(j, 0.0);
			}
			actionValues.put(i, tempHashMap);
		}
    }
    
    public void agent_init(String taskSpecification) {
    	previousActions = new HashMap<Integer, Action>();
		TaskSpec theTaskSpec=new TaskSpec(taskSpecification);
		System.out.println("Skeleton agent parsed the task spec.");
		System.out.println("Observation have "+theTaskSpec.getNumDiscreteObsDims()+" integer dimensions");
		System.out.println("Actions have "+theTaskSpec.getNumDiscreteActionDims()+" integer dimensions");
		IntRange theObsRange=theTaskSpec.getDiscreteObservationRange(0);
		System.out.println("Observation (state) range is: "+theObsRange.getMin()+" to "+theObsRange.getMax());
		IntRange theActRange=theTaskSpec.getDiscreteActionRange(0);
		System.out.println("Action range is: "+theActRange.getMin()+" to "+theActRange.getMax());
		DoubleRange theRewardRange=theTaskSpec.getRewardRange();
		System.out.println("Reward range is: "+theRewardRange.getMin()+" to "+theRewardRange.getMax());
		
		actRangeMax = theActRange.getMax();
		actRangeMin= theActRange.getMin();
		obsRangeMax = theObsRange.getMax();
		obsRangeMin= theObsRange.getMin();
		initActionValues();
		//In more complex agents, you would also check for continuous observations and actions, discount factors, etc.
		//Also, these ranges can have special values like "NEGINF, POSINF, UNSPEC (unspecified)".  There is no guarantee
		//that they are all specified and that they are all nice numbers.
    }

    public Action agent_start(Observation observation) {
        /**
         * Choose a random action (0 or 1)
         */
        int theIntAction = randGenerator.nextInt(4);
        /**
         * Create a structure to hold 1 integer action
         * and set the value
         */
        Action returnAction = new Action(1, 0, 0);
        returnAction.intArray[0] = theIntAction;

        lastAction = returnAction.duplicate();
        lastObservation = observation.duplicate();

        return returnAction;
    }

    public Action agent_step(double reward, Observation observation) {
        int nrActions = actRangeMax - actRangeMin + 1;
    	int currentState = observation.getInt(0);

    	HashMap<Integer,Double> map = actionValues.get(currentState);
    	Map.Entry<Integer, Double> maxEntry = null;
    	if (debug) System.out.println("Finding best action from state: " + currentState + " ");
    	for (Map.Entry<Integer, Double> entry : map.entrySet())
    	{
    	    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
    	    {
    	        maxEntry = entry;
    	    }
	        if (debug) System.out.println("Action: " + entry.getKey() + " Value: " + entry.getValue());
    	}
    	
    	int theIntAction;
    	double rand = randGenerator.nextFloat();
    	
    	if (rand > 0.9) {
    		theIntAction = randGenerator.nextInt(nrActions) + actRangeMin;
    	} else {
    		theIntAction = maxEntry.getKey();
    	}
    		
    	/**
         * Create a structure to hold 1 integer action
         * and set the value (alternate method)
         */
        Action returnAction = new Action();
        returnAction.intArray = new int[]{theIntAction};
        previousActions.put(observation.getInt(0), returnAction);

        lastAction = returnAction.duplicate();
        lastObservation = observation.duplicate();

        return returnAction;
    }

    public void agent_end(double reward) {
    	if (debug) System.out.println("Updating values: (note small \'s\')");
    	for(Integer state : previousActions.keySet()) {
    		int action = previousActions.get(state).getInt(0);
    		HashMap<Integer, Double> theMap = actionValues.get(state);
    		double oldValue = theMap.get(action);
    		double newValue = oldValue + (reward - oldValue) *alpha;
    		if (debug) System.out.println("state: " + state + 
    				 " action: " + action + " oldvalue: " + 
    				 oldValue + " newvalue " + newValue);
    		theMap.put(action, newValue);
    	}
    	previousActions = new HashMap<Integer, Action>();
    }

    public void agent_cleanup() {
        lastAction=null;
        lastObservation=null;
        previousActions = new HashMap<Integer, Action>();
    }

    public String agent_message(String message) {
        if(message.equals("what is your name?"))
            return "my name is skeleton_agent, Java edition!";

	return "I don't know how to respond to your message";
    }
    
    /**
     * This is a trick we can use to make the agent easily loadable.
     * @param args
     */
    
    public static void main(String[] args){
     	AgentLoader theLoader=new AgentLoader(new SkeletonAgent());
        theLoader.run();
	}

}
