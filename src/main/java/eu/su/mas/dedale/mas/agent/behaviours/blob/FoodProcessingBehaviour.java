package eu.su.mas.dedale.mas.agent.behaviours.blob;


import java.util.Date;

import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.mas.msgcontent.FoodMsgContent;

import eu.su.mas.dedale.tools.Debug;

/**
 * Used to process a food message on reception
 * @author antoninarberet
 *
 */
public class FoodProcessingBehaviour extends AbstractBlobBehaviour{

	private static final long serialVersionUID = -552903737105053141L;
	private FoodMsgContent food;
	public FoodProcessingBehaviour(AbstractBlobAgent myBlobAgent, FoodMsgContent food){
		super(myBlobAgent);
		this.food=food;
	}
	
	public void action() {
		Debug.info(myBlobAgent.getPrintPrefix()+"food results "+ food.getSeqNo()+" from "+food.getSender()+" at "+new Date().toString(),1);
		myBlobAgent.setFood(myBlobAgent.getFood()+food.getFood());
		myBlobAgent.addFoodTrade(food.getSender(), food.getFood());
		
		finished=true;
	}

	@Override
	public boolean done() {
		return finished;
	}
}
