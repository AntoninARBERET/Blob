package eu.su.mas.dedale.mas;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

import org.junit.Assert;
import agent.AbstractDeltaAgent;
import dataStructures.tuple.Couple;
import debug.Debug;

import eu.su.mas.dedale.env.ElementType;
import eu.su.mas.dedale.env.EntityCharacteristics;
import eu.su.mas.dedale.env.EntityType;
import eu.su.mas.dedale.env.IEnvironment;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.agent.behaviours.ReceiveTreasureTankerBehaviour;
import eu.su.mas.dedale.mas.agent.interactions.protocols.P_deployMe;
import eu.su.mas.dedale.mas.agent.interactions.protocols.P_deployMe.R1_ManagerAnswer;
import eu.su.mas.dedale.mas.agent.interactions.protocols.P_deployMe.R1_deployMe;
import eu.su.mas.dedale.princ.ConfigurationFile;


/**
 * Any agent willing to interact with a the DEDALE environment must extend this class
 * it offers :
 * <ul>
 *  <li> the API to move and act in the env, either it is distributed or not</li>
 *  <li> the management of the communication reach</li>
 *  <li> the deployment of the agent</li>
 *  </ul>
 * 
 * @author Cédric Herpson.
 *
 */
public class AbstractDedaleAgent extends AbstractDeltaAgent {

	private static final long serialVersionUID = -7435630598237152610L;
		
	private EntityCharacteristics ec;

	/**
	 * The key is the Observation used as an ObservationType
	 * The value is the Observation object. Currently two elements of the same class, but not in the future 
	 */
	private HashMap<Observation,Integer> myBackPack;


	/**
	 * A private ref to the environment the agent is moving into, should never be directly accessible to the user
	 * It is initiated by the gatekeeper when the agent is deployed 
	 * It should be obtained 
	 */
	private IEnvironment realEnv;

	/**
	 * Used to get the ref to the environment when asking the GK 
	 */
	private String environmentName;


	private String gateKeeperName;


	public AbstractDedaleAgent(){
		super();
	}

	/**
	 * 
	 * @param e the type of agent to create
	 * @param ec its characteristics
	 * @param environmentName name of the environment to deploy the agent
	 */
	public AbstractDedaleAgent(EntityType e,EntityCharacteristics ec, String environmentName){
		super();	
		Assert.assertNotNull(e);
		Assert.assertNotNull(ec);
		Assert.assertNotNull(environmentName);
		this.ec=ec;
		this.myBackPack=new HashMap<Observation, Integer>();
		this.environmentName=environmentName;

		//see AAgent (EnvironmentManager) //registerO2AInterface(EnvironmentManager.class,this);
	}


	/**
	 * 
	 * @return The agent's current position, null if the agent is not in the environment
	 */
	public String getCurrentPosition(){
		if (realEnv==null){
			Debug.warning("The agent is currently not deployed in any environment, you should not call getCurrentPosition()");
			return null;
		}		
		return this.realEnv.getCurrentPosition(this.getLocalName());
	}


	/**
	 * 
	 * @return The available observations from the agent's current position.</br> 
	 * A list of observed position (PositionID), and for each one is associated its list of observations under the form (ObservationType, Value))
	 * . Null if there is a malfunction</br>
	 * Example : {</br> Position1; [(Observation1,Value1);(Obseration2,Value2)],
	 * </br>Position2; [(Observation1,Value1);(Obseration2,Value2)],</br>
	 * ..}  
	 * 
	 * @see Observation for the list of Observation components
	 */
	public synchronized List<Couple<String, List<Couple<Observation,Integer>>>> observe(){
		return this.realEnv.observe(this.getCurrentPosition(),this.getLocalName());
	}


	/**
	 * This method should be the last method called in your behaviour.</br>
	 * The agent can die if he moves too carelessly.
	 * @param myDestination the targeted nodeId
	 * @return true if the move is legit and triggered, false otherwise
	 */
	public synchronized boolean moveTo(String myDestination){
		int consequence=this.realEnv.moveTo(this.getLocalName(),this.ec, myDestination);
		if(consequence==1){
			return true;
		}else{
			if (consequence==-1){
				//if an event in the env killed the agent, destroy the agent
				this.doDelete();
			}
			//Otherwise, just a move refused
			return false;
		}
	}


	/**
	 * 
	 * @return the amount of wealth that the agent was able to pick. 0 if there is no treasure at this place, or if the agent cannot grab it (
	 * backPack full, not authorized,...)
	 */
	public synchronized int pick(){
		//TODO update the agent BackPack and decide on the quantity according to its capability
		int pickedQuantity;
		switch (ec.getMyTreasureType()) {
		case DIAMOND:
			pickedQuantity= this.realEnv.pick(this.getLocalName(),this.getCurrentPosition(),
					ElementType.DIAMOND,
					ec.getDiamondCapacity()-this.myBackPack.get(ec.getMyTreasureType())
					);
			break;
		case GOLD:
			pickedQuantity=this.realEnv.pick(this.getLocalName(),this.getCurrentPosition(),
					ElementType.GOLD,
					ec.getGoldCapacity()-this.myBackPack.get(ec.getMyTreasureType())
					);
			break;
		default:
			Debug.warning("The agent's type does not allow him to pick anything in this configuration of the project");
			pickedQuantity= 0;
			break;
		}

		if (pickedQuantity>0){
			//update the backpack
			this.myBackPack.put(getMyTreasureType(),this.myBackPack.get(getMyTreasureType())+pickedQuantity);
		}
		return pickedQuantity;


	}

	/**
	 * 
	 * @return The available carrying capacity of the agent according to its type. Null if not applicable (Tanker,Explo,..)
	 */
	public Integer getBackPackFreeSpace(){
		switch (ec.getMyTreasureType()) {
		case DIAMOND:
			return ec.getDiamondCapacity()-this.myBackPack.get(ec.getMyTreasureType());
		case GOLD:
			return ec.getGoldCapacity()-this.myBackPack.get(ec.getMyTreasureType());
		default:
			return null;
		}

	}

	/**
	 * 
	 * @return The type of treasure that the agent is able to pick
	 */
	public Observation getMyTreasureType(){
		return this.ec.getMyTreasureType();
	}

	/**
	 * 
	 * @param agentSiloName name of the Tanker agent to receive the backpack's content
	 * @return true if you're agent was allowed to realize the transfer, false otherwise 
	 * 
	 */
	public synchronized boolean emptyMyBackPack(String agentSiloName){
		
		//I cannot be sure that the targetAgent is a silo before sending the message.
		//Protocol SendTreasure:
		// A -(type,quantity)-> B
		//Message arrive only if reacheable.
		// if B ok -(done)->A
		// if B nok -(refused)->A
		//A return false;

		//chose to do it in the critical section with senMessage, but will block the agent
		//or asynchronously
		
		//critical section is preferred to check the agentSiloName type and ensure the boolean answer. Otherwise I cannot give a boolean answer to the agent.
		//At the same time it is normal to block the agent during the realisation of a task.
		//conception decision to explain in the documentation

		if (ec.getMyTreasureType()!=Observation.NO_TREASURE && ec.getMyTreasureType()!=Observation.ANY_TREASURE && this.realEnv.isReachable(this.getLocalName(),agentSiloName,this.ec.getCommunicationReach())){
			//this.addAbility(ability, abilityID, role, behavioursParameters, knowledge);
			//this.addBehaviour(new SendTreasure(this,agentSiloName,this.myTreasureType,this.myBackPack.get(this.myTreasureType)));

			ACLMessage msg= new ACLMessage(ACLMessage.REQUEST);
			msg.setProtocol(ReceiveTreasureTankerBehaviour.PROTOCOL_TANKER);
			msg.setSender(this.getAID());
			msg.addReceiver(new AID(agentSiloName,AID.ISLOCALNAME));
			
			Couple<Observation, Integer> c= new Couple<Observation, Integer>(ec.getMyTreasureType(), this.myBackPack.get(ec.getMyTreasureType()));
			try {
				msg.setContentObject(c);
			} catch (IOException e) {
				Debug.error("EmptyMyBackPack - non serializable");
				e.printStackTrace();
			}
			send(msg);
			
			//Filter the messages
			MessageTemplate template= 
					MessageTemplate.and(
							MessageTemplate.MatchSender(new AID(agentSiloName, AID.ISLOCALNAME)),
							MessageTemplate.and(
									MessageTemplate.MatchProtocol(ReceiveTreasureTankerBehaviour.PROTOCOL_TANKER),
									MessageTemplate.MatchPerformative(ACLMessage.AGREE)		
									)
							);

			//I'm waiting for a message indicating that the silo received the value. If not, then the receiver is not a silo
			ACLMessage msg2=this.blockingReceive(template,4000);

			if (msg2!=null){
				this.myBackPack.put(ec.getMyTreasureType(), 0);
				return true;
			}else{
				return false;
			}			
		}else{
			return false;
		}
	}

	/**
	 * Throw a grenade to a given location.
	 * @param locationId the Id of the room the entity is aiming at
	 * @return true if done, false otherwise (the location as to far,..) 
	 * The consequences of this action, if any, will be seen in the environment
	 */

	public boolean throwGrenade(String locationId){
		return this.realEnv.throwGrenade(this.getLocalName(), locationId);
	}

	/**
	 * Drop the treasure on the current position, if allowed (depending of the agent's type)
	 */
	public void dropOff(){
		if(ec.getMyEntityType()==EntityType.WUMPUS_MOVER || ec.getMyEntityType()==EntityType.WUMPUS){
			switch (ec.getMyTreasureType()) {
			case DIAMOND: 
				this.realEnv.dropOff(this.getCurrentPosition(),ElementType.DIAMOND,this.myBackPack.get(ec.getMyTreasureType()));
				this.myBackPack.put(getMyTreasureType(),0);
				break;
			case GOLD:
				this.realEnv.dropOff(this.getCurrentPosition(),ElementType.GOLD,this.myBackPack.get(ec.getMyTreasureType()));
				this.myBackPack.put(getMyTreasureType(),0);
				break;
			default:
				Debug.warning("The agent's treasure type does not allow him to call this method");
				break;
			}
		}else{
			Debug.warning("The agent's type does not allow him to use the dropOff method");
		}
	}

	/**
	 * This method MUST be used instead of the final method JADE.core.Agent.send()  in order for the platform 
	 * to control the reach of the communications.
	 * @param msg the message to send
	 */
	//TODO wait for JadeIterator Update then clear the receiverNumber
	public void sendMessage(ACLMessage msg){
		Assert.assertNotNull("The sender must have been defined",msg.getSender());

		//filter reachable agents
		Iterator<AID> iter=msg.getAllReceiver();
		int receiverNumber=0;
		String senderLocalName =msg.getSender().getLocalName();
		while (iter.hasNext()){
			AID receiverAID= iter.next();
			receiverNumber++;
			if (!this.realEnv.isReachable(senderLocalName,receiverAID.getLocalName(),ec.getCommunicationReach())){
				iter.remove();
				receiverNumber--;
			}
		}

		if (receiverNumber>0){
			super.send(msg);
			
		}//else{
		//	System.out.println(msg.getSender().getLocalName()+"-- No agent within reach --");
		//}
	}

	/**
	 * Used to get a ref of this environment from the genericSharableObject table to realEnv, as well as the agents characteristics obtained through the configurationFile
	 */
	public void deployMe() {
		this.realEnv=(IEnvironment)this.getSharableObject(this.environmentName);
		Assert.assertNotNull(this.realEnv);
		this.realEnv.deployEntity(this.getLocalName(), this.ec,this.ec.getInitialLocation());
		//this.ec=(EntityCaracteristics)this.getSharableObject(this.getLocalName());//assuming that the agent's name is unique
	}

	public static Object[] loadEntityCaracteristics(String agentName, String instanceConfigurationEntitiesFullPath) {

		//explore the file, find the line for agent agentName
		Assert.assertNotNull("The configuration file for the entity " +agentName+" is not given while you require to create it", instanceConfigurationEntitiesFullPath);
		BufferedReader filereader=null;
		try {
			filereader = Files.newBufferedReader(Paths.get(instanceConfigurationEntitiesFullPath),StandardCharsets.UTF_8);
		} catch (IOException e1) {
			Debug.error("The configuration file for the entities does not exist");
			e1.printStackTrace();
		}

		EntityCharacteristics ec=null;
		boolean found=false;
		try {	
			String line = filereader.readLine();
			String[]l=line.split(":");
			System.out.println("Loading entity "+ agentName +" for environment "+ l[1]);
			while (!found && filereader!=null && (line = filereader.readLine()) != null) {
				l=line.split(":");
				EntityType et=null;
				if (l[1].equals(agentName)){
					switch (l[0]) {
					case "wumpus":
						et=EntityType.WUMPUS;
						//deployWumpus(l[1],l[2],Integer.parseInt(l[3]),Integer.parseInt(l[4]));
						break;
					case "agentExplo":	
						et=EntityType.AGENT_EXPLORER;
						//deployAgentFromConfig(l[1], l[2],EntityType.AGENT_EXPLORER, Integer.parseInt(l[3]),Integer.parseInt(l[4]));
						break;	
					case "agentCollect":	
						et=EntityType.AGENT_COLLECTOR;
						//deployAgentFromConfig(l[1], l[2],EntityType.AGENT_COLLECTOR, Integer.parseInt(l[3]),Integer.parseInt(l[4]));
						break;	
					case "agentTanker":	
						et=EntityType.AGENT_TANKER;
						//deployAgentFromConfig(l[1], l[2],EntityType.AGENT_TANKER, Integer.parseInt(l[3]),Integer.parseInt(l[4]));
						break;	
					default :
						Debug.error("This Agent type does not currently exist"+l[0]);
					}
					//case where no location is given
					if (l[3].equalsIgnoreCase("free")){
						ec=new EntityCharacteristics(et,Integer.parseInt(l[5]),Integer.parseInt(l[4]),Integer.parseInt(l[2]),null,Integer.parseInt(l[6]));
					}else{
						ec=new EntityCharacteristics(et,Integer.parseInt(l[5]),Integer.parseInt(l[4]),Integer.parseInt(l[2]),l[3],Integer.parseInt(l[6]));	
					}
					found=true;
				}
			}
		} catch (IOException|ArrayIndexOutOfBoundsException e) {
			System.err.println("configuration file describing entity "+agentName+ "is not reachable or malformed: "+instanceConfigurationEntitiesFullPath);
			e.printStackTrace();
		}

		if (!found){
			Debug.error("The agent "+agentName +" whas not found in the configuration file: "+instanceConfigurationEntitiesFullPath+".\n Its mandatory to give its caracteristics. See https://dedale.gitlab.io/page/tutorial/deployAgents/");
		}
		Object[] result={ec,ConfigurationFile.GATEKEEPER_NAME};

		return result;
	}
	
	/********************************
	 * Visible to the project only
	 *******************************/
	/**
	 * Used by the Knowledge components
	 * @return the backpack
	 */
	protected HashMap<Observation,Integer> getBackPack(){
		return this.myBackPack;
	}

	/******************
	 * Agent Creation
	 ******************
	 */

	/**
	 * This method is automatically called when a Dedale agent is created.
	 * It initialize its internal variables and add the mandatory abilities of any dedale's agent : 
	 * <ul>
	 * <li> To deploy itself within the environment and let its internal treasure state be observable :DeployMe</li>
	 * <li> To received treasures if the agent's type is "tanker"</li>
	 * </ul> 
	 */
	protected void setup(){	
		super.setup();
		
		final Object[] args = getArguments();
		Assert.assertNotNull(args);
		EntityCharacteristics ec=(EntityCharacteristics) args[0];
		Assert.assertNotNull(ec);
		this.ec=ec;
		this.myBackPack=new HashMap<Observation, Integer>();
		this.myBackPack.put(Observation.GOLD, 0);
		this.myBackPack.put(Observation.DIAMOND, 0);
	
		this.environmentName="env";
		//TODO move the environment name in the configuration file
		
		//get the parameters given into the object[]. In the current case, the environment where the agent will evolve	
		if(args[1]!=null){
			gateKeeperName=(String)args[1];	
		}else{
			Debug.error("Malfunction during parameter's loading of agent"+ this.getClass().getName());
		}

		P_deployMe p= new P_deployMe();
		addBehaviour(p.new R1_deployMe(gateKeeperName,this));
		addBehaviour(p.new R1_ManagerAnswer(gateKeeperName,this));
		
		if (ec.getMyEntityType()==EntityType.AGENT_TANKER){
			addBehaviour(new ReceiveTreasureTankerBehaviour(this,myBackPack));
		}

		System.out.println("the agent "+this.getLocalName()+ " is started");
	}



	/*
	 * *****************
	 * Agent Destruction
	 * *****************
	 */

	protected void takeDown(){
		Debug.warning(getLocalName()+": I'm diying, that's life; \"Blow, wind! Come, wrack! At least I’ll die with harness on my back.\" (MB)");
		//this.realEnv.removeEntity(this.getLocalName(),this.ec);// already done by the move
		super.takeDown();
		
	}

	/*
	 * ********************
	 * MOBILITY COMPONENTS
	 * ********************
	 */


	protected void beforeMove() {
		super.beforeMove();
		try {
			System.out.println(this.getLocalName()+" : I'm leaving "+this.getContainerController().getContainerName());
		} catch (ControllerException e) {
			e.printStackTrace();
		}
		//System.out.println("I quit");
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		ContainerController c=this.getContainerController();

		try {
			System.out.println(this.getLocalName()+ ": I'm arrived at "+c.getContainerName().toString());
		} catch (ControllerException e) {
			e.printStackTrace();
		}

		P_deployMe p= new P_deployMe();	
		this.addBehaviour(p.new R1_deployMe(gateKeeperName,this));
	}





	// see DELTA
	//	public void addAbility(Ability ability,String abilityID,String role,List<List<Object>> behavioursParameters,Knowledge knowledge){
	//		//Call to the protocol class that will get me all the behaviours necessary to add to the agent regarding the role r
	//
	//		//specific knowledge component required for the protocol
	//		String conversationID=this.setKnowledge(abilityID, knowledge);
	//
	//		//the conversationID must be given to the behaviours. Even if they may not use it
	//		for (List<Object> lo:behavioursParameters){
	//			lo.add(conversationID);
	//		}
	//		
	//		List<Behaviour> lb=ability.getBehaviours(role,behavioursParameters);
	//		for (Behaviour b:lb){
	//			this.addBehaviour(b);
	//		}
	//	}

}

