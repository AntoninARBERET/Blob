package eu.su.mas.dedale.mas.agents;


import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agents.behaviours.ReceiveTreasureTankerBehaviour;
import eu.su.mas.dedale.mas.agents.behaviours.startMyBehaviours;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;


/**
 * Tanker agents automatically have a @see {@link ReceiveTreasureTankerBehaviour} added
 * 
 * @author hc
 *
 */
public class DummyTankerAgent extends AbstractDedaleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1784844593772918359L;



	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();

		List<Behaviour> lb=new ArrayList<Behaviour>();
		lb.add(new RandomTankerBehaviour(this));
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){

	}
}


/**************************************
 * 
 * 
 * 				BEHAVIOUR
 * 
 * 
 **************************************/

class RandomTankerBehaviour extends TickerBehaviour{
	/**
	 * When an agent choose to move
	 *  
	 */
	private static final long serialVersionUID = 9088209402507795289L;

	public RandomTankerBehaviour (final AbstractDedaleAgent myagent) {
		super(myagent, 10000);
		//super(myagent);
	}

	@Override
	public void onTick() {
		//Example to retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=""){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
			//System.out.println("My current backpack capacity is:"+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
			
//			//list of attribute associated to the currentPosition
//			List<Attribute> lattribute= lobs.get(0).getRight();
//
//			//example related to the use of the backpack for the treasure hunt
//			Boolean b=false;
//			for(Attribute a:lattribute){
//				switch (a) {
//				case TREASURE : case DIAMONDS :
//					System.out.println("My treasure type is :"+((mas.abstractAgent)this.myAgent).getMyTreasureType());
//					System.out.println("My current backpack capacity is:"+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
//					System.out.println("Value of the treasure on the current position: "+a.getName() +": "+ a.getValue());
//					System.out.println("The agent grabbed :"+((mas.abstractAgent)this.myAgent).pick());
//					System.out.println("the remaining backpack capacity is: "+ ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());
//					//System.out.println("The value of treasure on the current position: (unchanged before a new call to observe()): "+a.getValue());
//					b=true;
//					break;
//
//				default:
//					break;
//				}
//			}
//
//			//If the agent picked (part of) the treasure
//			if (b){
//				List<Couple<String,List<Attribute>>> lobs2=((mas.abstractAgent)this.myAgent).observe();//myPosition
//				System.out.println("lobs after picking "+lobs2);
//			}
//
//			//Random move from the current position
//			Random r= new Random();
//			int moveId=r.nextInt(lobs.size());
//
//			//The move action (if any) should be the last action of your behaviour
//			((mas.abstractAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
		}

	}

}