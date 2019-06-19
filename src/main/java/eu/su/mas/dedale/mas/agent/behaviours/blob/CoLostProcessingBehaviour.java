package eu.su.mas.dedale.mas.agent.behaviours.blob;

import java.time.Clock;
import java.util.Random;

import dataStructures.tuple.Couple;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.mas.knowledge.LastContactTabEntry;
import eu.su.mas.dedale.mas.knowledge.NTabEntry;
import eu.su.mas.dedale.mas.msgcontent.AdMsgContent;
import eu.su.mas.dedale.mas.msgcontent.CoLostMsgContent;
import eu.su.mas.dedale.mas.msgcontent.PingMsgContent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class CoLostProcessingBehaviour extends AbstractBlobBehaviour{
	private CoLostMsgContent coLost;
	public CoLostProcessingBehaviour(AbstractBlobAgent myBlobAgent, CoLostMsgContent coLost){
		super(myBlobAgent);
		this.coLost=coLost;
	}
	
	public void action() {
		myBlobAgent.print("coLost received : "+coLost.getSender() + " lost "+coLost.getLostNode());
		String lastForwarder = coLost.getForwarders().get(coLost.getForwarders().size()-1);
		HashSet<String> nextToLost= myBlobAgent.getNextTo(coLost.getLostNode());
		//if last forwarder was a potential next hop to the lost node
		if(nextToLost.contains(lastForwarder)) {
			nextToLost.remove(lastForwarder);
			coLost.addForwarder(myBlobAgent.getLocalName());
			if(nextToLost.size()==0) {
				myBlobAgent.reBroadcastCoLostMsg(coLost);
			}
			else if(nextToLost.size()==1) {
				Iterator<String> it = nextToLost.iterator();
				myBlobAgent.reSendCoLostMsgTo(coLost, it.next());
			}
		}
		
		finished=true;
	}
}
