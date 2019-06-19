package eu.su.mas.dedale.mas.agent.behaviours.blob;

import java.time.Clock;
import java.util.Random;

import dataStructures.tuple.Couple;

import java.util.Date;
import java.util.HashMap;

import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.mas.msgcontent.PingMsgContent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class PingProcessingBehaviour extends AbstractBlobBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7021924466437120352L;
	private PingMsgContent ping;
	public PingProcessingBehaviour(AbstractBlobAgent myBlobAgent, PingMsgContent ping){
		super(myBlobAgent);
		this.ping=ping;
	}
	
	public void action() {
		/*HashMap<String, Couple<Integer,Date>> map = myBlobAgent.getLastContact();
		if(!map.containsKey(ping.getSender())) {
			//TODO Envoi d'update
			map.put(ping.getSender(), new Couple<Integer,Date>(new Integer(ping.getSeqNo()),new Date()));
		}
		else if(map.get(ping.getSender()).getLeft().intValue()<ping.getSeqNo()) {
			map.put(ping.getSender(), new Couple<Integer,Date>(new Integer(ping.getSeqNo()),new Date()));
		}*/
		
	}
}
