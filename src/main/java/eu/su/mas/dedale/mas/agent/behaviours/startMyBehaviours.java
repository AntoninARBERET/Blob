package eu.su.mas.dedale.mas.agent.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;

/**
 * This behaviour must be the only one added directly to any dedale agent.<br/>
 * It allows the agent to wait for its deployment in the environment before triggering its associated behaviours.<br/>
 * Indeed, an agent have to wait for the gatekeeper to deliver a reference to the env before calling the Environment's API
 * 
 *  {@see DummyMovingAgent} source code for an example
 *  
 * @author hc
 *
 */
public class startMyBehaviours extends SimpleBehaviour {

	private static final long serialVersionUID = 1326096402723539425L;

	private boolean behavioursAdded=false;
	private boolean messageFromGK=false;
	private List<Behaviour> lBehav;

/**
 * 
 * @param a
 * @param behaviourList the list of behaviours to be added after the agent a is deployed in the environment
 * e.g.<br/>
 * List<Behaviour> lb=new ArrayList<Behaviour>();<br/>
   lb.add(new RandomWalkBehaviour(this));<br/>
	addBehaviour(new startMyBehaviours(this,lb));
 */
	public startMyBehaviours(AbstractDedaleAgent a,List<Behaviour> behaviourList) {
		super(a);
		lBehav=behaviourList;
	}

	@Override
	public void action() {
		for (Behaviour b:lBehav){
			//this.myAgent.addBehaviour(new RandomShiftBehaviour((AbstractDedaleAgent) this.myAgent));
			this.myAgent.addBehaviour(b);
		}
		
//		//Filter the messages
//		MessageTemplate template= 
//				MessageTemplate.and(
//						MessageTemplate.MatchSender(new AID(this.myAgent.getLocalName(), AID.ISLOCALNAME)),
//						MessageTemplate.and(
//								MessageTemplate.MatchProtocol(P_deployMe.PROTOCOLNAME),
//								MessageTemplate.or(
//										MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
//										MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
//										)
//								)
//						);
//
//		messageFromGK=false;
//		//I'm waiting for a message indicating that I can interact with the environment (or that I'm not on the right location)
//		ACLMessage msg=this.myAgent.receive(template);
//
//		if (msg!=null){
//			messageFromGK=true;
//
//			//a message is received
//			if (msg.getPerformative()==ACLMessage.CONFIRM){
//				behavioursAdded=true;
//				//I'm deployed in the environment, I can add my behaviours
//
//				//this.myAgent.addBehaviour(new RandomWalkBehaviour((AbstractDedaleAgent) this.myAgent));
//				
//				for (Behaviour b:lBehav){
//					//this.myAgent.addBehaviour(new RandomShiftBehaviour((AbstractDedaleAgent) this.myAgent));
//					this.myAgent.addBehaviour(b);
//				}
//				
//			}else{
//				//The Gatekeeper refused, I should move to the same container has him if I really want to deploy myself in the environment
//				ContainerID cID= new ContainerID();
//				cID.setName(msg.getContent());
//
//				//only necessary of the containers are on different computers
//				//cID.setPort("8888");
//				//cID.setAddress("132.227.205.25");
//				this.myAgent.doMove(cID);
//
//
//			}
//
//		}else{
//			//if the message is not received, I wait for the next message
//			block();
//		}	
	}


	@Override
	public boolean done() {
		if (behavioursAdded){
			System.out.println("I'm arrived on the right container, I can now explore the environment");
		}else{
			if(messageFromGK) {
				System.out.println("StartMyBehaviours - I'm not on the right container, waiting to be on it to explore");
			}
		}
		return behavioursAdded;
	}


}
