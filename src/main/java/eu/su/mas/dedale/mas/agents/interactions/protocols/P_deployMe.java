package eu.su.mas.dedale.mas.agents.interactions.protocols;



import agent.A2oInterface;
import agent.AbstractDeltaAgent;
import eu.su.mas.dedale.env.EntityCharacteristics;
import eu.su.mas.dedale.env.IEnvironment;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.io.Serializable;

import org.junit.Assert;

/**
 * This class is used to allow an agent to ask an 
 * environmentManager Agent to deploy it in.
 * 
 * A -request(DeployMe) --> GateKeeper
 * A <- Refuse/Agree ref to env -- GateKeeper
 * A -Confirm deployed-> A
 * 
 * @author Cédric Herpson
 */
public class P_deployMe implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8846407488301151348L;
	public final static String PROTOCOLNAME="P_deployMe";

	/**
	 * OneShot behaviour, used by the agent when arriving on a given container to ask for a deployment in the environment, if any
	 */
	public class R1_deployMe extends SimpleBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1990743584808894736L;
		private String environmentManagerLocalName="";

		/**
		 * OneShot behaviour, used when arriving on a given container to deploy the agent in the env
		 * @param environmentManagerName : name of the entity in charge of the environment
		 * @param agent : a ref to the agent, must be a Dedale's agent subclass
		 */
		public R1_deployMe(String environmentManagerName, AbstractDedaleAgent agent) {
			super(agent);
			Assert.assertNotSame("the environment manager must have a name", "",environmentManagerName);
			this.environmentManagerLocalName=environmentManagerName;
		}

		@Override
		public void action() {

			ContainerController c=this.myAgent.getContainerController();
			String temp = null;
			try {
				temp = c.getContainerName().toString();
			} catch (ControllerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println(this.myAgent.getLocalName()+" : I'm currently on container: "+temp);

			ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
			msg.setSender(this.myAgent.getAID());
			msg.addReceiver(new AID(this.environmentManagerLocalName,AID.ISLOCALNAME));
			msg.setProtocol(PROTOCOLNAME);
			msg.setContent(temp);//indicate my current location

			this.myAgent.send(msg);
			//System.out.println("This is not the right P_deployMe");


			//			//TODO Suppress this horrible hardcoded hack useful to simplify things for students and 
			//			//add the behaviour only after arriving on the right container or make the GK refuse if its not the right container
			//			ContainerController c=this.myAgent.getContainerController();
			//			String temp = null;
			//			try {
			//				temp = c.getContainerName().toString();
			//			} catch (ControllerException e1) {
			//				// TODO Auto-generated catch block
			//				e1.printStackTrace();
			//			}
			//			
			//				System.out.println(temp);
			//			
			//			try {
			//				if (!c.getContainerName().equalsIgnoreCase("MyDistantContainer0")){
			//
			//					ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
			//					msg.setSender(this.myAgent.getAID());
			//					msg.addReceiver(new AID(this.environmentManagerLocalName,AID.ISLOCALNAME));
			//					//msg.setProtocol(PROTOCOLNAME);
			//					msg.setContent(temp);
			//					System.out.println("Not here for now");
			//					this.myAgent.send(msg);	
			//				
			//				}else{
			//				
			//				ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
			//				msg.setSender(this.myAgent.getAID());
			//				msg.addReceiver(new AID(this.environmentManagerLocalName,AID.ISLOCALNAME));
			//				msg.setProtocol(PROTOCOLNAME);
			//				System.out.println("Ask for deploy");
			//				this.myAgent.send(msg);
			//				System.out.println(msg.getContent());
			//				
			//				}
			//			} catch (ControllerException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			}

		}

		@Override
		public boolean done() {
			return true;
		}

	}

	/**
	 * 
	 * Behaviour waiting the gatekeeper answer. 
	 * If ok, the agent will be authorized to start its environment's specifics behaviours.
	 * 
	 * @author hc
	 *
	 */
	public class R1_ManagerAnswer extends SimpleBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1990743584808894736L;
		private String environmentManagerLocalName;
		private boolean deployOk=false;

		/**
		 * 
		 * @param environementManagerName : name of the entity in charge of the environment
		 * @param myagent : a ref to the agent
		 */
		public R1_ManagerAnswer(String environementManagerName,AbstractDedaleAgent myagent) {
			super(myagent);
			Assert.assertNotSame("the environment manager must have a name", "",this.environmentManagerLocalName);
			this.environmentManagerLocalName=environementManagerName;
		}


		@Override
		public void action() {


			MessageTemplate template= 
					MessageTemplate.and(
							MessageTemplate.MatchSender(new AID(this.environmentManagerLocalName, AID.ISLOCALNAME)),
							MessageTemplate.and(
									MessageTemplate.MatchProtocol(PROTOCOLNAME),
									MessageTemplate.or(
											MessageTemplate.MatchPerformative(ACLMessage.REFUSE),
											MessageTemplate.MatchPerformative(ACLMessage.AGREE)
											)
									)
							);

			ACLMessage msg=this.myAgent.receive(template);

			if (msg!=null){
				if (msg.getPerformative()==ACLMessage.AGREE){						
					deployOk=true;
					//System.out.println("The gateKeeper agreed");
					((AbstractDedaleAgent)this.myAgent).deployMe();

					//send a message to myself to activate the other behaviours
					ACLMessage deployed= new ACLMessage(ACLMessage.CONFIRM);
					deployed.setProtocol(PROTOCOLNAME);
					deployed.addReceiver(this.myAgent.getAID());
					deployed.setSender(this.myAgent.getAID());
					deployed.setContent("I'm deployed");
					this.myAgent.send(deployed);
				}else{
					try {
						System.out.println("The GakeKeeper refused to add the agent "+this.myAgent.getLocalName()+ " in the environment.\n "
								+ "The agent is currently on the container "+ this.myAgent.getContainerController().getContainerName()+" and the GateKeeper is on "+msg.getContent());
					} catch (ControllerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//send a message to myself to activate the other behaviours
					ACLMessage notDeployed= new ACLMessage(ACLMessage.REFUSE);
					notDeployed.setProtocol(PROTOCOLNAME);
					notDeployed.addReceiver(this.myAgent.getAID());
					notDeployed.setSender(this.myAgent.getAID());
					notDeployed.setContent(msg.getContent());
					this.myAgent.send(notDeployed);
				}
			}else{
				block();
			}



		}

		@Override
		public boolean done() {
			//done in the aftermove()
			//if (!deployOk){
			//	this.myAgent.addBehaviour(new P_deployMe.R1_deployMe(this.environmentManagerLocalName,(AbstractDedaleAgent) this.myAgent));
			//}	
			return deployOk;
		}

	}


	/**********************************************
	 * Set of behaviours associated to the 2nd role of 
	 * this protocol, the one of the environment Manager
	 * @author hc
	 *
	 **********************************************/


	/**
	 * This behaviour is always running on the GK 
	 * @author hc
	 *
	 */
	public class R2_envManager extends SimpleBehaviour {

		private IEnvironment reftoEnv;

		public R2_envManager(final AbstractDeltaAgent a, IEnvironment env) {
			super(a);
			Assert.assertNotNull(env);
			this.reftoEnv=env;
		}

		private static final long serialVersionUID = 4127322826569265361L;

		@Override
		public void action() {

			MessageTemplate template= MessageTemplate.and(
					MessageTemplate.MatchProtocol(PROTOCOLNAME),
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST)						
					);

			ACLMessage msg=this.myAgent.receive(template);

			if (msg!=null){
				//	An new agent is asking for deploy 

				ACLMessage answer=msg.createReply();

				//Check if the sender is on the right container 
				//TODO check if there is some place and if the agent is not already in the env
				ContainerController c=this.myAgent.getContainerController();
				try {
					if (c.getContainerName().equalsIgnoreCase(msg.getContent())){
						//the agent is on the right container	

						AgentController ag = null;
						try {
							ag = c.getAgent(msg.getSender().getLocalName());
						} catch (ControllerException e) {
							e.printStackTrace();
						}
						A2oInterface o2a1 = null;
						try {
							o2a1 = ag.getO2AInterface(A2oInterface.class);
						} catch (StaleProxyException e) {
							e.printStackTrace();
						}
						o2a1.addSharableObject(reftoEnv.getName(),reftoEnv);//o2a1.deployEntity(reftoEnv);
						//If whe want to give specific caracteristics to the agent when he arrives in the env, its here. In The current version of Dedale, in order to allow an open environment, the GK does not know the agents that will come beforehand. Theus, he cannot give them specific things. Only color the map according to their type 
						//o2a1.addSharableObject(msg.getSender().getLocalName(),new EntityCaracteristics(o, diamond, gold, comReach));//these info should be loaded from the configurationFile
						answer.setPerformative(ACLMessage.AGREE);

					}else{
						//answer no, and indicate the targeted container name;
						answer.setPerformative(ACLMessage.REFUSE);
						answer.setContent(c.getContainerName());

					}
				} catch (ControllerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				this.myAgent.send(answer);



				//				A2oInterface o2a1 = null;
				//				try {
				//					o2a1 = ag.getO2AInterface(A2oInterface.class);
				//				} catch (StaleProxyException e) {
				//					// TODO Auto-generated catch block
				//					e.printStackTrace();
				//				}
				//				o2a1.addSharableObject(reftoEnv.getName(),reftoEnv);




			}else {
				block();
			}


		}

		@Override
		public boolean done() {
			return false;
		}
	}//End R2.EnvManager

	//	public void setRole2(Environment e, Agent a){
	//		a.addBehaviour(new R2_envManager(a, e));
	//	}
	//	
	//	public void setRole1(String gateKeeperName, abstractAgent a){
	//		a.addBehaviour(new R1_deployMe(gateKeeperName, a));
	//		a.addBehaviour(new R1_ManagerAnswer(gateKeeperName, a));
	//	}


}
