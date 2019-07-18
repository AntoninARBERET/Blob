package eu.su.mas.dedale.mas.agent.behaviours.blob;

import java.time.Clock;
import java.util.Random;
import java.util.Date;
import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.mas.msgcontent.AdMsgContent;
import eu.su.mas.dedale.mas.msgcontent.FoodMsgContent;
import eu.su.mas.dedale.mas.msgcontent.ResultsMsgContent;
import eu.su.mas.dedale.mas.msgcontent.StateMsgContent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveMessageBehaviour extends AbstractBlobBehaviour{
	/**
	 * 
	 */
	private static final long serialVersionUID = 798562981577225116L;

	public ReceiveMessageBehaviour(AbstractBlobAgent myBlobAgent){
		super(myBlobAgent);
	}
	
	public void action() {
		//1) receive the message
				final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);	

				final ACLMessage msg = this.myBlobAgent.receive(msgTemplate);
				if (msg != null) {
					try {
						//TODO switch on the protocol field
						switch(msg.getProtocol()) {
						//reception ad
						case "AD":
							AdMsgContent ad =(AdMsgContent) msg.getContentObject();
							myBlobAgent.addBehaviour(new AdProcessingBehaviour(myBlobAgent,ad));
							break;
						case "RESULTS":
							ResultsMsgContent res =(ResultsMsgContent) msg.getContentObject();
							myBlobAgent.addBehaviour(new ResultsProcessingBehaviour(myBlobAgent,res));
							break;
						case "FOOD":
							FoodMsgContent food =(FoodMsgContent) msg.getContentObject();
							myBlobAgent.addBehaviour(new FoodProcessingBehaviour(myBlobAgent,food));
							break;
						case "STATE":
							StateMsgContent state =(StateMsgContent) msg.getContentObject();
							myBlobAgent.addBehaviour(new StateProcessingBehaviour(myBlobAgent,state));
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else{
					block();
				}
	}

	@Override
	public boolean done() {
		return false;
	}
}
