package eu.su.mas.dedale.mas.agent.behaviours.blob;

import java.time.Clock;
import java.util.Random;

import dataStructures.tuple.Couple;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.mas.knowledge.LastContactTabEntry;
import eu.su.mas.dedale.mas.knowledge.NTabEntry;
import eu.su.mas.dedale.mas.msgcontent.AdMsgContent;
import eu.su.mas.dedale.mas.msgcontent.FoodMsgContent;
import eu.su.mas.dedale.mas.msgcontent.PingMsgContent;
import eu.su.mas.dedale.tools.Debug;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class FoodProcessingBehaviour extends AbstractBlobBehaviour{
	private FoodMsgContent food;
	public FoodProcessingBehaviour(AbstractBlobAgent myBlobAgent, FoodMsgContent food){
		super(myBlobAgent);
		this.food=food;
	}
	
	public void action() {
		Debug.info(myBlobAgent.getPrintPrefix()+"food results "+ food.getSeqNo()+" from "+food.getSender()+" at "+new Date().toString(),1);
		myBlobAgent.setFood(myBlobAgent.getFood()+food.getFood());
		
		
		finished=true;
	}

	@Override
	public boolean done() {
		return finished;
	}
}
