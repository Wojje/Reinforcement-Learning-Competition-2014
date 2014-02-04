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
*  $HeadURL: http://rl-glue-ext.googlecode.com/svn/trunk/projects/codecs/Java/examples/skeleton-sample/SkeletonEnvironment.java $
* 
*/

import java.util.Random;

import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;

/**
 */
public class MonsterGridEnvironment implements EnvironmentInterface {
    private int currentState=10;
    private Monster[] monsters = new Monster[2];
    private Random random = new Random();
    private int successState = 7;
    private boolean failedOnMonster = false;
    private boolean debug = false;
    
    public String env_init() {
	
	//Create a task spec programatically.  This task spec encodes that state, action, and reward space for the problem.
	//You could forgo the task spec if your agent and environment have been created specifically to work with each other
	//ie, there is no need to share this information at run time.  You could also use your own ad-hoc task specification language,
	//or use the official one but just hard code the string instead of constructing it this way.
	    TaskSpecVRLGLUE3 theTaskSpecObject = new TaskSpecVRLGLUE3();
        theTaskSpecObject.setEpisodic();
        theTaskSpecObject.setDiscountFactor(1.0d);
	//Specify that there will be an coordinate system [-7,28] for the state
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, 35));
	//Specify that there will be an integer action [0,3]
        theTaskSpecObject.addDiscreteAction(new IntRange(0, 3));
	//Specify the reward range [-1,1]
        theTaskSpecObject.setRewardRange(new DoubleRange(-1, 1));
        
        System.out.println("Creating monsters");
        //Init monsters
        for (int i = 0; i < monsters.length; i++) {
        	monsters[i] = new Monster(6);        	
        }
        
        String taskSpecString = theTaskSpecObject.toTaskSpec();
        TaskSpec.checkTaskSpec(taskSpecString);

		return taskSpecString;
    }

    public Observation env_start() {
    	
    	//set new random successState
    	do {
    		successState = random.nextInt(30);
    	} while (isFailState(successState));
    	
    	//Set random starting state
    	do {
    		currentState=random.nextInt(30);
    	} while (isFailState(currentState) || isSuccessState(currentState));
        
    	
    	
        Observation returnObservation=new Observation(1,0,0);
        returnObservation.intArray[0]=currentState;
        return returnObservation;
    }

    private boolean isFailState(int state) {
    	if(state>29 || state%6==5 
        		|| state%6==0 || state<6){
    		failedOnMonster = false;
    		return true;
    	}
    	for (Monster m : monsters) {
    		if (state == m.getState()) {
    			failedOnMonster = true;
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean isSuccessState(int state) {    	
    	return (state == successState);
    }
    
    public Reward_observation_terminal env_step(Action thisAction) {
        boolean episodeOver=false;
        double theReward=0.0d;
        int theAction;
        //Only succeed in moving in the requested direction 85% of the time.
        //Otherwise choose random action. 
        if (debug) System.out.print("Now in state: " + currentState+ " ");
        if (random.nextDouble() > 0.85) {
        	theAction = random.nextInt(4);
        	if (debug) System.out.print("Failed to move in requested direction; ");
        } else {
        	theAction = thisAction.intArray[0];
        }
        if (debug) System.out.println("Moving in this direction: " + theAction);
        
        if(theAction==0)
            currentState--;
        if(theAction==1)
            currentState++;
        if(theAction==2)
            currentState+=6;
        if(theAction==3)
            currentState-=6;
        
        if (isSuccessState(currentState)) { 
        	if (debug) System.out.println("Won, currentstate: " + currentState + 
        			" successState: " + successState);
            theReward=1.0d;
            episodeOver=true;
        }
        
        if (isFailState(currentState)) {
        	if (failedOnMonster) {
        		if (debug) System.out.println("Failed on monster on state: " + currentState);
        		
        	} else {
        		if (debug) System.out.println("Failed on edge on state: " + currentState);
        	}
        	theReward = -1.0d;
        	episodeOver = true;
        }
        
        if (episodeOver) {
        	for (Monster m : monsters) {
        		m.nextEpisode();
        	}
        }
        
        theReward -= 0.01;
        
        Observation returnObservation=new Observation(1,0,0);
        returnObservation.intArray[0]=currentState;
        
        Reward_observation_terminal returnRewardObs=new Reward_observation_terminal(theReward,returnObservation,episodeOver);
        return returnRewardObs;
    }

    public void env_cleanup() {
    }

    public String env_message(String message) {
        if(message.equals("what is your name?"))
            return "my name is skeleton_environment, Java edition!";

	return "I don't know how to respond to your message";
    }
    
   /**
     * This is a trick we can use to make the agent easily loadable.
     * @param args
     */
    public static void main(String[] args){
        EnvironmentLoader theLoader=new EnvironmentLoader(new MonsterGridEnvironment());
        theLoader.run();
    }


}
