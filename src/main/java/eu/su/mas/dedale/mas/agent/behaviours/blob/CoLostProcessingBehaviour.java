package eu.su.mas.dedale.mas.agent.behaviours.blob;

import java.util.HashSet;
import java.util.Iterator;

import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.mas.msgcontent.CoLostMsgContent;

/**
 * Used on reception of a CoLost message to forward it
 * @author antoninarberet
 *
 */
public abstract class CoLostProcessingBehaviour extends AbstractBlobBehaviour{

	private static final long serialVersionUID = 1184763466861677073L;
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
