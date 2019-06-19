package eu.su.mas.dedale.mas.agent.behaviours.blob;
import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import jade.core.behaviours.SimpleBehaviour;

/**
 * Abstract calss of which other behaviors inherit, used to handle the BlobAgent and the temporizing
 * @author arberet
 * 
 */

public abstract class AbstractBlobBehaviour extends SimpleBehaviour{
	
	private static final long serialVersionUID = -4318201296912268535L;
	protected AbstractBlobAgent myBlobAgent;
	protected boolean finished;

	public AbstractBlobBehaviour(AbstractBlobAgent myBlobAgent) {
		super();
		this.myBlobAgent=myBlobAgent;
		this.finished=false;
	}
	
	public void action() {
		
		if(AbstractBlobAgent.TEMPO) {
			myBlobAgent.doWait(AbstractBlobAgent.TEMPOTIME);
		}
	}

}
