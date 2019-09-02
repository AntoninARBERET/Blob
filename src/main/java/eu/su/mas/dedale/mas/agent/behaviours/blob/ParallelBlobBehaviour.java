package eu.su.mas.dedale.mas.agent.behaviours.blob;

import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.tools.Debug;
import jade.core.behaviours.ParallelBehaviour;

/**
 * Composite behaviour used to create both BlobingBehviour and SyncBlobBehaviour and 
 * execute them in two separate thread
 * @author antoninarberet
 *
 */
public class ParallelBlobBehaviour extends ParallelBehaviour{

	private static final long serialVersionUID = 7404737960641865296L;
	protected AbstractBlobAgent myBlobAgent;
	protected boolean finished;

	public ParallelBlobBehaviour(AbstractBlobAgent myBlobAgent) {
		super();
		this.myBlobAgent=myBlobAgent;
		this.finished=false;
		switch (myBlobAgent.getMode()) {
		case RANDOM:
			this.addSubBehaviour(new BlobingBehaviour(myBlobAgent));
			this.addSubBehaviour(new SyncBlobBehaviour(myBlobAgent));
			Debug.info(myBlobAgent.getPrintPrefix()+"parallelBlobBehaviour constructed in random mode",4);
		case STATIC_FOOD:
			this.addSubBehaviour(new BlobingBehaviour(myBlobAgent));
			this.addSubBehaviour(new SyncBlobBehaviour(myBlobAgent));
		case FOOD_IN_ENV:
			this.addSubBehaviour(new BlobingBehaviour(myBlobAgent));
			this.addSubBehaviour(new SyncBlobBehaviour(myBlobAgent));
		}

	}
	
	
	
	public boolean Done() {
		return finished;
	}

}
