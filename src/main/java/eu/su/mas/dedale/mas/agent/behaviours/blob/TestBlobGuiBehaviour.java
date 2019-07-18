package eu.su.mas.dedale.mas.agent.behaviours.blob;
import java.util.Random;
import java.util.Date;
import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.mas.knowledge.NTabEntry;

/**
 * AdProcessingBehaviour is the first Behaviour used by the BlobAgent
 * It send the first ad, and wait to finish approximatively at the same time
 * that the other agents.
 * @author arberet
 *
 */
public class TestBlobGuiBehaviour extends AbstractBlobBehaviour{
	private static final long serialVersionUID = 6931126702428149439L;
	private int adTimer;
	private boolean adSent;
	private Date start, adDate, sendDate;
	private int randomTime;

	public TestBlobGuiBehaviour(AbstractBlobAgent myBlobAgent){
		super(myBlobAgent);

	}
	

	public void action() {
		this.myBlobAgent.doWait(1000);
		myBlobAgent.getRealEnv().updateNodeAndEdgesStyle(myBlobAgent);
		//myBlobAgent.getnTab().put("Blob2", new NTabEntry("Blob2", 1, 3, 5, 7));
	}
	
	public boolean done() {
		return this.finished;
	}
}
