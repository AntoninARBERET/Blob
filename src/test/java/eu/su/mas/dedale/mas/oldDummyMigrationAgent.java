package eu.su.mas.dedale.mas;

import org.junit.Assert;

import env.Environment;
import eu.su.mas.dedale.mas.agent.interactions.protocols.P_deployMe;
import eu.su.mas.dedale.mas.agent.interactions.protocols.deployMe.R1_deployMe;
import eu.su.mas.dedale.mas.agent.interactions.protocols.deployMe.R1_managerAnswer;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

import mas.abstractAgent;
import mas.behaviours.RandomWalkBehaviour;
import mas.behaviours.SayHello;

public class oldDummyMigrationAgent extends AbstractDedaleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2991562876411096907L;
	/**
	 * 
	 */

	String gateKeeperName;

	protected void setup(){

		super.setup();
		gateKeeperName = null;
		//get the parameters given into the object[]. In the current case, the environment where the agent will evolve
		final Object[] args = getArguments();
		Assert.assertNotNull(args);
		if(args!=null && args[0]!=null){
			gateKeeperName=(String)args[0];	
		}else{
			System.err.println("Malfunction during parameter's loading of agent"+ this.getClass().getName());
			System.exit(-1);
		}

		addBehaviour(new R1_deployMe(gateKeeperName,this));
		addBehaviour(new R1_managerAnswer(gateKeeperName,this));
		
		addBehaviour(new RandomWalkBehaviour(this));
		System.out.println("the agent "+this.getLocalName()+ " is started");

	}

	
	protected void beforeMove() {
		super.beforeMove();
				try {
					System.out.println("I'm leaving "+this.getContainerController().getContainerName());
				} catch (ControllerException e) {
					e.printStackTrace();
				}
		System.out.println("I quit");
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		ContainerController c=this.getContainerController();
			
		try {
			System.out.println("I'm arrived at "+c.getContainerName().toString());
		} catch (ControllerException e) {
			e.printStackTrace();
		}
			
		this.addBehaviour(new R1_deployMe(gateKeeperName,this));
	}

}