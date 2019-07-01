package eu.su.mas.dedale.mas.agent.behaviours.blob;

import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.tools.Debug;
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
		Debug.info(myBlobAgent.getPrintPrefix()+"parallelBlobBehaviour constructed",4);

	}
	
	
	
	public boolean Done() {
		return finished;
	}

}
