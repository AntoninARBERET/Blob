package eu.su.mas.dedale.mas.agent.behaviours.blob;

import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class ParallelBlobBehaviour extends ParallelBehaviour{
	protected AbstractBlobAgent myBlobAgent;
	protected boolean finished;

	public ParallelBlobBehaviour(AbstractBlobAgent myBlobAgent) {
		super();
		this.myBlobAgent=myBlobAgent;
		this.finished=false;
		this.addSubBehaviour(new BlobingBehaviour(myBlobAgent));
		this.addSubBehaviour(new SyncBlobBehaviour(myBlobAgent));
		myBlobAgent.print("parallelBlobBehaviour constructed");
	}
	
	
	
	public boolean Done() {
		return finished;
	}

}
