package eu.su.mas.dedale.mas.agent.behaviours;

import java.util.HashMap;

//import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;

import dataStructures.tuple.Couple;
import debug.Debug;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * This behaviour is automatically added to the Tanker agents.
 * PROTOCOL_TANKER="ProtocolTanker";
 * 
 * An agent with the appropriate type call the method EmptyMyBackPack(TankerName)
 * This method trigger a Request (TreasureType,Value) message
 * The current behaviour wait for it and store the result in the agent
 * 
 * @author hc
 *
 */
//TODO Create the Gui based on the the tanker backpack
public class ReceiveTreasureTankerBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6407154712384808742L;
	public static String PROTOCOL_TANKER="ProtocolTanker";
	private HashMap<Observation,Integer> backPack;

	public ReceiveTreasureTankerBehaviour(AbstractDedaleAgent a,HashMap<Observation,Integer> backPack) {
		super(a);
		this.backPack=backPack;
	}

	@Override
	public void action() {

		//Filter the messages
		MessageTemplate template= 
				MessageTemplate.and(
						MessageTemplate.MatchProtocol(PROTOCOL_TANKER),
						MessageTemplate.MatchPerformative(ACLMessage.REQUEST)		
						);

		//I'm waiting for a message from a collector
		ACLMessage msg=this.myAgent.receive(template);
		Couple<Observation,Integer> c=null;

		if (msg!=null){
			//Debug.warning("Tanker agent - Message received: "+msg.toString());
			try {
				c=(Couple<Observation, Integer>) msg.getContentObject();
			} catch (UnreadableException e) {
				Debug.error("Tanker receiving non Deserializable value");
				e.printStackTrace();
			}
			Integer i=this.backPack.get(c.getLeft());
			if (i!=null){
				//	add the received value in the agentTanker backpack
				this.backPack.put(c.getLeft(), i+c.getRight());
				//Debug.warning(c.getLeft()+" - There is now "+this.backPack.get(c.getLeft()) +" in the backPack");
			}else{
				this.backPack.put(c.getLeft(),c.getRight());
			}
			ACLMessage resp=msg.createReply();
			resp.setPerformative(ACLMessage.AGREE);
			this.myAgent.send(resp);
		}else{
			block();
		}
	}
	

	@Override
	public boolean done() {
		return false;
	}

}
