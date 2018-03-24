package eu.su.mas.dedale.mas;

import java.io.IOException;

import java.util.List;
import java.util.Random;

import env.Attribute;
import env.Couple;
import env.Environment;
import jade.core.behaviours.SimpleBehaviour;

import mas.abstractAgent;

/**
 * 
 * This Dummy Wumpus is harmless. It only forbid the access to the place where it is located.
 * The Wumpus will only move (randomly) if you press Enter on the console
 * 
 * @author hc
 *
 */
public class oldDummyWumpusAgent extends AbstractDedaleAgent{


	/**
	 * 
	 */
	private static final long serialVersionUID = 2703609263614775545L;


	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1 set the agent attributes 
	 *	 		2 add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();

		//get the parameters given into the object[]
		final Object[] args = getArguments();
		if(args[0]!=null){
			deployWumpus((Environment) args[0]);
		}else{
			System.err.println("Malfunction during parameter's loading of agent"+ this.getClass().getName());
			System.exit(-1);
		}

		//Add the behaviours
		addBehaviour(new mas.behaviours.RandomWalkBehaviourWumpus(this));

		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){

	}


	/**************************************
	 * 
	 * 
	 * 				BEHAVIOURS
	 * 
	 * 
	 **************************************/


	public class RandomWalkBehaviour extends SimpleBehaviour{
		/**
		 * When an agent choose to move
		 *  
		 */
		private static final long serialVersionUID = 9088209402507795289L;

		private boolean finished=false;
		private Environment realEnv;

		public RandomWalkBehaviour (final AbstractDedaleAgent myagent) {
			super(myagent);

		}

		@Override
		public void action() {

			String myPosition=getCurrentPosition();
			if (myPosition!=""){
				List<Couple<String,List<Attribute>>> lobs=observe();
				System.out.println("lobs: "+lobs);

				try {
					System.out.println("Press a key to allow the wumpus "+this.myAgent.getLocalName()+" to move to the next step ");
					System.in.read();
				} catch (IOException e) {
					e.printStackTrace();
				}


				Random r= new Random();
				int moveId=r.nextInt(lobs.size());
				moveTo(lobs.get(moveId).getLeft());
				
			}else{
				System.err.println("Empty posit");
				System.exit(D_UNKNOWN);
			}

		}

		@Override
		public boolean done() {
			return false;
		}

	}


}
