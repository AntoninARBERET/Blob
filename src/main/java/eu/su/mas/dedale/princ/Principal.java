package eu.su.mas.dedale.princ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agents.GateKeeperAgent;
import eu.su.mas.dedale.mas.agents.dedaleDummyAgents.DummyCollectorAgent;
import eu.su.mas.dedale.mas.agents.dedaleDummyAgents.DummyMovingAgent;
import eu.su.mas.dedale.mas.agents.dedaleDummyAgents.DummyTankerAgent;
import eu.su.mas.dedale.mas.agents.dedaleDummyAgents.DummyWumpusShift;
//import jade.core.AgentContainer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.junit.Assert;
import jade.wrapper.AgentContainer;


public class Principal {


	private static HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();// container's name - container's ref
	private static List<AgentController> agentList;// agents's ref
	private static Runtime rt;	

	public static void main(String[] args){

		//if(!ConfigurationFile.PLATFORMisDISTRIBUTED){
		//No gateKeeper, the environment is created and a reference is given to the agents at creation

		//0) Create the real environment and the observed one
		//env= new Environment(ENVtype.GRID_T,4,null);
		//env= new Environment(ENVtype.DOROGOVTSEV,15,null);
		//env=new Environment("src/main/resources/map2016-2","src/main/resources/map2016-2-config");

		//1), create the platform (Main container (DF+AMS) + containers + monitoring agents : RMA and SNIFFER)
		//rt=emptyPlatform(containerList);

		//2) create agents and add them to the platform.
		//agentList=createAgents(containerList);

		//3) launch agents
		//startAgents(agentList);

		//}else{
		//DistributedVersion of the project
		if(ConfigurationFile.COMPUTERisMAIN){
			//Whe should create the Platform and the GateKeeper, wether the platform is distributed or not 

			//1), create the platform (Main container (DF+AMS) + containers + monitoring agents : RMA and SNIFFER)
			rt=emptyPlatform(containerList);

			//2) create the gatekeeper (in charge of the environment) and add it (them) to the platform.
			agentList=createAgents(containerList);

			//3) launch agents
			startAgents(agentList);
		}else{
			//We only have to create the local container and our agents

			//1') If a distant platform already exist and you want to create and connect your container to it
			containerList.putAll(createAndConnectContainer(ConfigurationFile.LOCAL_CONTAINER_NAME, ConfigurationFile.PLATFORM_HOSTNAME, ConfigurationFile.PLATFORM_ID, ConfigurationFile.PLATFORM_PORT));

			//2) create agents and add them to the platform.
			agentList=createAgents(containerList);

			//3) launch agents
			startAgents(agentList);
		}
	}
	//}



	/**********************************************
	 * 
	 * Methods used to create an empty platform
	 * 
	 **********************************************/

	/**
	 * Create an empty platform composed of 1 main container and 3 containers.
	 * @param containerList 
	 * @return a ref to the platform and update the containerList
	 */
	private static Runtime emptyPlatform(HashMap<String, ContainerController> containerList){

		Runtime rt = Runtime.instance();

		// 1) create a platform (main container+DF+AMS)
		Profile pMain = new ProfileImpl(ConfigurationFile.PLATFORM_HOSTNAME,ConfigurationFile.PLATFORM_PORT,ConfigurationFile.PLATFORM_ID);
		System.out.println("Launching a main-container..."+pMain);
		AgentContainer mainContainerRef = rt.createMainContainer(pMain); //DF and AMS are include

		// 2) create the containers
		containerList.putAll(createContainers(rt));

		// 3) create monitoring agents : rma agent, used to debug and monitor the platform; sniffer agent, to monitor communications; 
		createMonitoringAgents(mainContainerRef);

		System.out.println("Plaform ok");
		return rt;

	}

	/**
	 * Create the containers used to hold the agents 
	 * @param rt The reference to the main container
	 * @return an Hmap associating the name of a container and its object reference.
	 * 
	 * note: there is a smarter way to find a container with its name, but we go fast to the goal here. Cf jade's doc.
	 */
	private static HashMap<String,ContainerController> createContainers(Runtime rt) {
		String containerName;
		ProfileImpl pContainer;
		ContainerController containerRef;
		HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();//bad to do it here.

		System.out.println("Launching containers ...");

		//create the container0	
		containerName=ConfigurationFile.LOCAL_CONTAINER_NAME;
		pContainer = new ProfileImpl(ConfigurationFile.PLATFORM_HOSTNAME, ConfigurationFile.PLATFORM_PORT, ConfigurationFile.PLATFORM_ID);
		pContainer.setParameter(Profile.CONTAINER_NAME,containerName);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.

		containerList.put(containerName, containerRef);

		//create the container0	
		containerName=ConfigurationFile.LOCAL_CONTAINER2_NAME;
		pContainer = new ProfileImpl(ConfigurationFile.PLATFORM_HOSTNAME, ConfigurationFile.PLATFORM_PORT, ConfigurationFile.PLATFORM_ID);
		pContainer.setParameter(Profile.CONTAINER_NAME,containerName);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.

		containerList.put(containerName, containerRef);

		//create the container1	
		containerName=ConfigurationFile.LOCAL_CONTAINER3_NAME;
		pContainer = new ProfileImpl(ConfigurationFile.PLATFORM_HOSTNAME, ConfigurationFile.PLATFORM_PORT, ConfigurationFile.PLATFORM_ID);
		//pContainer = new ProfileImpl(null, 8888, null);
		pContainer.setParameter(Profile.CONTAINER_NAME,containerName);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		containerList.put(containerName, containerRef);

		//create the container2	
		containerName=ConfigurationFile.LOCAL_CONTAINER4_NAME;
		pContainer = new ProfileImpl(ConfigurationFile.PLATFORM_HOSTNAME, ConfigurationFile.PLATFORM_PORT, ConfigurationFile.PLATFORM_ID);
		//pContainer = new ProfileImpl(null, 8888, null);
		pContainer.setParameter(Profile.CONTAINER_NAME,containerName);
		System.out.println("Launching container "+pContainer);
		containerRef = rt.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.
		containerList.put(containerName, containerRef);

		System.out.println("Launching containers done");
		return containerList;

	}

	/**
	 * 
	 * @param containerName
	 * @param host  is the IP of the host where the main-container should be listen to. A null value means use the default (i.e. localhost)
	 * @param platformID is the symbolic name of the platform, if different from default. A null value means use the default (i.e. localhost)
	 * @param port (if null, 8888 by default)
	 * @return
	 */
	private static HashMap<String,ContainerController> createAndConnectContainer(String containerName,String host, String platformID, Integer port){

		ProfileImpl pContainer;
		ContainerController containerRef;
		HashMap<String, ContainerController> containerList=new HashMap<String, ContainerController>();//bad to do it here.
		Runtime rti=Runtime.instance();

		if (port==null){
			port=ConfigurationFile.PLATFORM_PORT;
		}

		System.out.println("Create and Connect container "+containerName+ " to the host : "+host+", platformID: "+platformID+" on port "+port);

		pContainer = new ProfileImpl(host,port, platformID);
		pContainer.setParameter(Profile.CONTAINER_NAME,containerName);
		containerRef = rti.createAgentContainer(pContainer); //ContainerController replace AgentContainer in the new versions of Jade.

		//ContainerID cID= new ContainerID();
		//cID.setName(containerName);
		//cID.setPort(port);
		//cID.setAddress(host);

		containerList.put(containerName, containerRef);
		return containerList;
	}

	/**
	 * create the monitoring agents (rma+sniffer) on the main-container given in parameter and launch them.
	 *  - RMA agent's is used to debug and monitor the platform;
	 *  - Sniffer agent is used to monitor communications
	 * @param mc the main-container's reference
	 */
	private static void createMonitoringAgents(ContainerController mc) {

		Assert.assertNotNull(mc);
		System.out.println("Launching the rma agent on the main container ...");
		AgentController rma;

		try {
			rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
			rma.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("Launching of rma agent failed");
		}

		System.out.println("Launching  Sniffer agent on the main container...");
		AgentController snif=null;

		try {
			snif= mc.createNewAgent("sniffeur", "jade.tools.sniffer.Sniffer",new Object[0]);
			snif.start();

		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("launching of sniffer agent failed");

		}		


	}



	/**********************************************
	 * 
	 * Methods used to create the agents and to start them
	 * 
	 **********************************************/


	/**
	 *  Creates the agents and add them to the agentList.  agents are NOT started.
	 *@param containerList :Name and container's ref
	 *@return the agentList
	 */
	private static List<AgentController> createAgents(HashMap<String, ContainerController> containerList) {
		System.out.println("Launching agents...");
		ContainerController c;
		String agentName;
		List<AgentController> agentList=new ArrayList<AgentController>();


		if (ConfigurationFile.COMPUTERisMAIN){

			/*
			 * The main is on this computer, we deploy the GateKeeper 
			 */
			c = containerList.get(ConfigurationFile.LOCAL_CONTAINER_NAME);
			Assert.assertNotNull("This container does not exist",c);
			agentName="GK";
			try {
				Object[] objtab=new Object[]{ConfigurationFile.INSTANCE_TOPOLOGY,ConfigurationFile.INSTANCE_CONFIGURATION_ELEMENTS,ConfigurationFile.ENVIRONMENT_TYPE,ConfigurationFile.ENVIRONMENTisGRID,ConfigurationFile.ENVIRONMENT_SIZE,ConfigurationFile.ACTIVE_DIAMOND,ConfigurationFile.ACTIVE_GOLD,ConfigurationFile.ACTIVE_WELL};//used to give informations to the agent
				//Object[] objtab=new Object[]{null,null,ConfigurationFile.ENVIRONMENT_TYPE};//used to give informations to the agent
				System.out.println("GateKeeperAgent.class.getName(): "+GateKeeperAgent.class.getName());
				AgentController	ag=c.createNewAgent(agentName,GateKeeperAgent.class.getName(),objtab);
				agentList.add(ag);
				System.out.println(agentName+" launched");
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}

		}
		/*the main exist, we deploy the agent(s) on  their local containers
		 *They will have to find the gatekeeper's container to deploy themselves in the environment. This is automatically performed by all agent that extends AbstractDedaleAgent*/

		/*****************************************************
		 * 
		 * 				ADD YOUR AGENTS HERE
		 * 
		 *****************************************************/
		
//		/*********
//		 * GOLEM
//		 *********/
//		//1) Get the container where the agent will appear
//		c = containerList.get(ConfigurationFile.LOCAL_CONTAINER2_NAME);
//		Assert.assertNotNull("This container does not exist",c);
//		
//		//2) Give the name of your agent, MUST be the same as the one given in the entities file.
//		agentName="Golem";
//		
//		//3) If you want to give specific parameters to your agent, add them here
//		Object [] entityParameters={"My parameters"};
//		
//		//4) Give the class name of your agent to let the system instantiate it
//		AgentController	ag=createNewDedaleAgent(c, agentName, DummyWumpusShift.class.getName(), entityParameters);
//		agentList.add(ag);	
		
		
		/*********
		 * AGENT Collect
		 *********/
		//1) Get the container where the agent will appear
		c = containerList.get(ConfigurationFile.LOCAL_CONTAINER2_NAME);
		Assert.assertNotNull("This container does not exist",c);
		
		//2) Give the name of your agent, MUST be the same as the one given in the entities file.
		agentName="Collect";
		
		//3) If you want to give specific parameters to your agent, add them here
		Object [] entityParametersC={"My parameters"};
		
		//4) Give the class name of your agent to let the system instantiate it
		AgentController	ag=createNewDedaleAgent(c, agentName, DummyCollectorAgent.class.getName(), entityParametersC);
		agentList.add(ag);
		
		/*********
		 * AGENT Silo
		 *********/
		//1) Get the container where the agent will appear
		c = containerList.get(ConfigurationFile.LOCAL_CONTAINER2_NAME);
		Assert.assertNotNull("This container does not exist",c);
		
		//2) Give the name of your agent, MUST be the same as the one given in the entities file.
		agentName="Silo";
		
		//3) If you want to give specific parameters to your agent, add them here
		Object [] entityParameters2={"My parameters"};
		
		//4) Give the class name of your agent to let the system instantiate it
		ag=createNewDedaleAgent(c, agentName, DummyTankerAgent.class.getName(), entityParameters2);
		agentList.add(ag);
		
		
		
//		/*********
//		 * AGENT 1
//		 *********/
//		//1) Get the container where the agent will appear
//		c = containerList.get(ConfigurationFile.LOCAL_CONTAINER2_NAME);
//		Assert.assertNotNull("This container does not exist",c);
//		
//		//2) Give the name of your agent, MUST be the same as the one given in the entities file.
//		agentName="Agent1";
//		
//		//3) If you want to give specific parameters to your agent, add them here
//		Object [] entityParameters={"My parameters"};
//		
//		//4) Give the class name of your agent to let the system instantiate it
//		AgentController	ag=createNewDedaleAgent(c, agentName, DummyMigrationAgent.class.getName(), entityParameters);
//		agentList.add(ag);
//		
//		/*********
//		 * AGENT 2
//		 *********/
//		//1) Get the container where the agent will appear
//		c = containerList.get(ConfigurationFile.LOCAL_CONTAINER2_NAME);
//		Assert.assertNotNull("This container does not exist",c);
//		
//		//2) Give the name of your agent, MUST be the same as the one given in the entities file.
//		agentName="Agent2";
//		
//		//3) If you want to give specific parameters to your agent, add them here
//		Object [] entityParameters2={"My parameters"};
//		
//		//4) Give the class name of your agent to let the system instantiate it
//		ag=createNewDedaleAgent(c, agentName, DummyMigrationAgent.class.getName(), entityParameters2);
//		agentList.add(ag);
//		
//		/*********
//		 * AGENT 3
//		 *********/
//		//1) Get the container where the agent will appear
//		c = containerList.get(ConfigurationFile.LOCAL_CONTAINER2_NAME);
//		Assert.assertNotNull("This container does not exist",c);
//		
//		//2) Give the name of your agent, MUST be the same as the one given in the entities file.
//		agentName="Agent3";
//		
//		//3) If you want to give specific parameters to your agent, add them here
//		Object [] entityParameters3={"My parameters"};
//		
//		//4) Give the class name of your agent to let the system instantiate it
//		ag=createNewDedaleAgent(c, agentName, DummyMigrationAgent.class.getName(), entityParameters3);
//		agentList.add(ag);
//		
//		/*********
//		 * AGENT 4
//		 *********/
//		//1) Get the container where the agent will appear
//		c = containerList.get(ConfigurationFile.LOCAL_CONTAINER2_NAME);
//		Assert.assertNotNull("This container does not exist",c);
//		
//		//2) Give the name of your agent, MUST be the same as the one given in the entities file.
//		agentName="Agent4";
//		
//		//3) If you want to give specific parameters to your agent, add them here
//		Object [] entityParameters4={"My parameters"};
//		
//		//4) Give the class name of your agent to let the system instantiate it
//		ag=createNewDedaleAgent(c, agentName, DummyMigrationAgent.class.getName(), entityParameters4);
//		agentList.add(ag);
//		
//		/*********
//		 * AGENT 5
//		 *********/
//		//1) Get the container where the agent will appear
//		c = containerList.get(ConfigurationFile.LOCAL_CONTAINER2_NAME);
//		Assert.assertNotNull("This container does not exist",c);
//		
//		//2) Give the name of your agent, MUST be the same as the one given in the entities file.
//		agentName="Agent5";
//		
//		//3) If you want to give specific parameters to your agent, add them here
//		Object [] entityParameters5={"My parameters"};
//		
//		//4) Give the class name of your agent to let the system instantiate it
//		ag=createNewDedaleAgent(c, agentName, DummyMigrationAgent.class.getName(), entityParameters5);
//		agentList.add(ag);
		
		

		
		//				try {
		//					Object[] objtab=new Object[]{gatekeeperName};//used to give informations to the agent
		//					AgentController	ag=c.createNewAgent(agentName,DummyMigrationAgent.class.getName(),objtab);
		//					agentList.add(ag);
		//					System.out.println(agentName+" launched");
		//				} catch (StaleProxyException e) {
		//					e.printStackTrace();
		//				}

		//if (ConfigurationFile.PLATFORMisDISTRIBUTED)
		/*
		 *Distributed, the main already exist, we deploy the agent(s) on MyLocalContainer
		 *They will have to find the gatekeeper's container to deploy themselves
		 */

		//		c = containerList.get("MyLocalContainer");
		//		Assert.assertNotNull("This container does not exist",c);
		//		agentName="Ulysse1";
		//		String gatekeeperName="GK";
		//		try {
		//			Object[] objtab=new Object[]{gatekeeperName};//used to give informations to the agent
		//			AgentController	ag=c.createNewAgent(agentName,DummyMigrationAgent.class.getName(),objtab);
		//			agentList.add(ag);
		//			System.out.println(agentName+" launched");
		//		} catch (StaleProxyException e) {
		//			e.printStackTrace();
		//		}

		//}

		/*
		 * Local and no GateKeeper
		 */

		//		//wumpus on container0
		//		c = containerList.get("container0");
		//		agentName="Golem";//"Wumpus1"
		//		try {
		//			Object[] objtab=new Object[]{env};//used to give informations to the agent
		//			AgentController	ag=c.createNewAgent(agentName,DummyWumpusAgent.class.getName(),objtab);
		//			agentList.add(ag);
		//			System.out.println(agentName+" launched");
		//		} catch (StaleProxyException e) {
		//			e.printStackTrace();
		//		}

		//		agentName="Wumpus2";
		//		try {
		//
		//
		//			Object[] objtab=new Object[]{env,agentName};//used to give informations to the agent
		//			AgentController	ag=c.createNewAgent(agentName,DummyAgentWumpus.class.getName(),objtab);
		//			agentList.add(ag);
		//			System.out.println(agentName+" launched");
		//		} catch (StaleProxyException e) {
		//			e.printStackTrace();
		//		}
		//
		//		//wumpus on container0
		//		c = containerList.get("container0");
		//		agentName="Wumpus3";
		//		try {
		//
		//
		//			Object[] objtab=new Object[]{env,agentName};//used to give informations to the agent
		//			AgentController	ag=c.createNewAgent(agentName,DummyAgentWumpus.class.getName(),objtab);
		//			agentList.add(ag);
		//			System.out.println(agentName+" launched");
		//		} catch (StaleProxyException e) {
		//			e.printStackTrace();
		//		}
		//
		//
		//Agent0 on container0
		//		c = containerList.get("MyDistantContainer0");
		//		agentName="Explo1";
		//		try {
		//
		//			Object[] objtab=new Object[]{"GK"};//{env}//used to give informations to the agent
		//			AgentController	ag=c.createNewAgent(agentName,DummyMigrationAgent.class.getName(),objtab);
		//			agentList.add(ag);
		//			System.out.println(agentName+" launched");
		//		} catch (StaleProxyException e) {
		//			e.printStackTrace();
		//		}
		//
		//		c = containerList.get("container0");
		//		agentName="Agent1";
		//		try {
		//
		//			Object[] objtab=new Object[]{env};//used to give informations to the agent
		//			AgentController	ag=c.createNewAgent(agentName,DummyExploAgent.class.getName(),objtab);
		//			agentList.add(ag);
		//			System.out.println(agentName+" launched");
		//		} catch (StaleProxyException e) {
		//			e.printStackTrace();
		//		}
		//
		//		c = containerList.get("container0");
		//		agentName="Agent2";
		//		try {
		//
		//			Object[] objtab=new Object[]{env};//used to give informations to the agent
		//			AgentController	ag=c.createNewAgent(agentName,DummyExploAgent.class.getName(),objtab);
		//			agentList.add(ag);
		//			System.out.println(agentName+" launched");
		//		} catch (StaleProxyException e) {
		//			e.printStackTrace();
		//		}
		//		c = containerList.get("container0");
		//		agentName="Agent3";
		//		try {
		//
		//			Object[] objtab=new Object[]{env};//used to give informations to the agent
		//			AgentController	ag=c.createNewAgent(agentName,DummyExploAgent.class.getName(),objtab);
		//			agentList.add(ag);
		//			System.out.println(agentName+" launched");
		//		} catch (StaleProxyException e) {
		//			e.printStackTrace();
		//		}
		//		c = containerList.get("container0");
		//		agentName="Agent4";
		//		try {
		//
		//			Object[] objtab=new Object[]{env};//used to give informations to the agent
		//			AgentController	ag=c.createNewAgent(agentName,DummyExploAgent.class.getName(),objtab);
		//			agentList.add(ag);
		//			System.out.println(agentName+" launched");
		//		} catch (StaleProxyException e) {
		//			e.printStackTrace();
		//		}
		//		
		//		c = containerList.get("container0");
		//		agentName="Agent5";
		//		try {
		//
		//			Object[] objtab=new Object[]{env};//used to give informations to the agent
		//			AgentController	ag=c.createNewAgent(agentName,DummyExploAgent.class.getName(),objtab);
		//			agentList.add(ag);
		//			System.out.println(agentName+" launched");
		//		} catch (StaleProxyException e) {
		//			e.printStackTrace();
		//		}

		//		//Agent0 on container0
		//		c = containerList.get("container0");
		//		agentName="Explo3";
		//		try {
		//
		//
		//			Object[] objtab=new Object[]{env};//used to give informations to the agent
		//			AgentController	ag=c.createNewAgent(agentName,DummyExploAgent.class.getName(),objtab);
		//			agentList.add(ag);
		//			System.out.println(agentName+" launched");
		//		} catch (StaleProxyException e) {
		//			e.printStackTrace();
		//		}






		System.out.println("Agents created...");
		return agentList;
	}

	/**
	 * Start the agents
	 * @param agentList
	 */
	private static void startAgents(List<AgentController> agentList){

		System.out.println("Starting agents...");


		for(final AgentController ac: agentList){
			try {
				ac.start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}

		}
		System.out.println("Agents started...");
	}

	/**
	 * 
	 * @param initialContainer container where to deploy the agent
	 * @param agentName name of the agent
	 * @param className class of the agent
	 * @param additionnalParameters 
	 */
	private static AgentController createNewDedaleAgent(ContainerController initialContainer, String agentName,String className, Object[] additionnalParameters){
		//Object[] objtab=new Object[]{env,agentName};//used to give informations to the agent
		Object[] objtab=AbstractDedaleAgent.loadEntityCaracteristics(agentName,ConfigurationFile.INSTANCE_CONFIGURATION_ENTITIES);
		Object []res2=merge(objtab,additionnalParameters);

		AgentController ag=null;
		try {
			ag = initialContainer.createNewAgent(agentName,className,res2);
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertNotNull(ag);
		//agentList.add(ag);
		System.out.println(agentName+" launched");
		return ag;
	}

	/**
	 * tab2 is added at the end of tab1
	 * @param tab1
	 * @param tab2
	 */
	private static Object[] merge (Object [] tab1, Object[] tab2){
		Assert.assertNotNull(tab1);
		Object [] res;
		if (tab2!=null){
			res= new Object[tab1.length+tab2.length];
			int i= tab1.length;
			for(i=0;i<tab1.length;i++){
				res[i]=tab1[i];
			}
			for (int ind=0;ind<tab2.length;ind++){
				res[i]=tab2[ind];
				i++;
			}
		}else{
			res=tab1;
		}
		return res;
	}
}






