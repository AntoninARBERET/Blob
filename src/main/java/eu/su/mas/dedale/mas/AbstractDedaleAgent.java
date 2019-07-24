package eu.su.mas.dedale.mas;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

import org.junit.Assert;
import agent.AbstractDeltaAgent;
import debug.Debug;

import eu.su.mas.dedale.env.EntityCharacteristics;
import eu.su.mas.dedale.env.EntityType;
import eu.su.mas.dedale.env.IEnvironment;
import eu.su.mas.dedale.mas.agent.interactions.protocols.P_deployMe;
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


	/**
	 * A private ref to the environment the agent is moving into, should never be directly accessible to the user
	 * It is initiated by the gatekeeper when the agent is deployed 
	 * It should be obtained 
	 */
	protected IEnvironment realEnv;

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
		this.environmentName=environmentName;

		//see AAgent (EnvironmentManager) //registerO2AInterface(EnvironmentManager.class,this);
	}



	/**
	 * 
	 * @param agentSiloName name of the Tanker agent to receive the backpack's content
	 * @return true if you're agent was allowed to realize the transfer, false otherwise 
	 * 
	 */


	/**
	 * Throw a grenade to a given location.
	 * @param locationId the Id of the room the entity is aiming at
	 * @return true if done, false otherwise (the location as to far,..) 
	 * The consequences of this action, if any, will be seen in the environment
	 */


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
			System.out.println("AID" + receiverAID.toString());
			receiverNumber++;
			receiverAID.getLocalName();
			ec.getCommunicationReach();
			if (!this.realEnv.isReachable(senderLocalName,receiverAID.getLocalName())){
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
					case "blobAgent":	
						et=EntityType.BLOB_AGENT;
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

		/*if (!found){
			Debug.error("The agent "+agentName +" whas not found in the configuration file: "+instanceConfigurationEntitiesFullPath+".\n Its mandatory to give its caracteristics. See https://dedale.gitlab.io/page/tutorial/deployAgents/");
		}*/
		Object[] result={ec,ConfigurationFile.GATEKEEPER_NAME};

		return result;
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

