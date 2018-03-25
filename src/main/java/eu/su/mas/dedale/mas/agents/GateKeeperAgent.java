package eu.su.mas.dedale.mas.agents;

import org.junit.Assert;
import agent.AbstractDeltaAgent;
import debug.Debug;
import eu.su.mas.dedale.env.EnvironmentType;
import eu.su.mas.dedale.env.IEnvironment;
import eu.su.mas.dedale.env.gs.gsEnvironment;
import eu.su.mas.dedale.env.jme.jmeEnvironment;
import eu.su.mas.dedale.mas.agent.interactions.protocols.P_deployMe;
import eu.su.mas.dedale.princ.ConfigurationFile;
//import env.Environment;
import jade.core.Agent;
//import mas.agents.interactions.protocols.P_deployMe;
//import mas.agents.interactions.protocols.deployMe.R2_envManager;
//import mas.behaviours.RandomWalkBehaviour;
//import mas.behaviours.SayHello;

/**
 * The gate-keeper ensure that 
 *  - there is a possibility to add an agent within the environment
 *  - that the agent is compatible with the types of environment
 *  - the agent receives a reference to the environment
 *  
 * @author hc
 *
 */
public class GateKeeperAgent extends AbstractDeltaAgent {

	private static final long serialVersionUID = 337962546911985502L;


	//TODO Ensure that there is only one gatekeeper  on a given container.
	//Environment ref that all the agents within the container should share.
	private IEnvironment env;

	protected void setup(){

		super.setup();


		//1) get the parameters given into the object[]. In the current case, the type of environment where the agents will evolve
		final Object[] args = getArguments();

		Assert.assertNotNull(args);
		Assert.assertNotNull(args[2]);//type of Env
		//2) create the env
		//Assert.assertNotNull(args[0]);//generated of loaded ?
		//Assert.assertNotNull(args[1]);// config path


		//EnvironmentType e= (En)
		//the GK received the configuration regarding the environment
		//TODO Manage the different loading possibilities
		//		if ((boolean) args[0]){
		//			//the environment should be generated
		//			//the parameters should be X
		//			//{ConfigurationFile.ENVIRONMENTisGENERATED,ConfigurationFile.ENVIRONMENT_TYPE,ConfigurationFile.ENVIRONMENT_SIZE,ConfigurationFile.ACTIVE_DIAMOND,ConfigurationFile.ACTIVE_GOLD,ConfigurationFile.ACTIVE_WELL};//used to give informations to the agent
		//			Assert.assertEquals(args.length,6);
		//			switch ((EnvironmentType) args[1]) {
		//			case GS:
		//				env= new gsEnvironment();
		//				env.
		//				break;
		//			case JME:
		//				env = new jmeEnvironment();
		//				env.CreateEnvironment((String)args[1], (String)args[2]);
		//				break;	
		//			default:
		//				Debug.error("This type of environment is not yet supported");
		//				break;
		//			}
		//		}else{
		//the environment should be loaded
		//the environement should be 
		//{ConfigurationFile.INSTANCE_TOPOLOGY,ConfigurationFile.INSTANCE_CONFIGURATION_ELEMENTS,ConfigurationFile.ENVIRONMENT_TYPE,COnfigurationFile.isGrid,ConfigurationFile.ENVIRONMENT_SIZE,ConfigurationFile.ACTIVE_DIAMOND,ConfigurationFile.ACTIVE_GOLD,ConfigurationFile.ACTIVE_WELL};//used to give informations to the agent
		Assert.assertEquals(args.length, 8);
		switch ((EnvironmentType) args[2]) {
		case GS:
			env= new gsEnvironment();
			env.CreateEnvironment((String)args[0], (String)args[1],(boolean)args[3],(Integer)args[4],(boolean)args[5],(boolean)args[6],(boolean)args[7]);
			break;
		case JME:
			env = new jmeEnvironment();
			env.CreateEnvironment((String)args[0], (String)args[1],(boolean)args[3],(Integer)args[4],(boolean)args[5],(boolean)args[6],(boolean)args[7]);
			break;	
		default:
			Debug.error("This type of environment is not yet supported");
			break;
		}


		//env.CreateEnvironment((String)args[0],(String)args[1]);

		//}else{
		//	System.err.println("Malfunction during parameter's loading of agent"+ this.getClass().getName());
		//	System.exit(-1);
		//}


		//3) Add the behaviour(s) allowing the GK to add/remove agent from the env
		P_deployMe p= new P_deployMe();
		addBehaviour(p.new R2_envManager(this,env));
		
		//4) Add the behaviour(s) to work as a Gui
		
		
		System.out.println("the agent "+this.getLocalName()+ " is started");

	}


}
