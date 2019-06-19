package eu.su.mas.dedale.mas.agent.behaviours.blob;
import java.util.Random;
import java.util.Date;
import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;

/**
 * AdProcessingBehaviour is the first Behaviour used by the BlobAgent
 * It send the first ad, and wait to finish approximatively at the same time
 * that the other agents.
 * @author arberet
 *
 */
public class AdBroadcastingBehaviour extends AbstractBlobBehaviour{
	private static final long serialVersionUID = 6931126702428149439L;
	private int adTimer;
	private boolean adSent;
	private Date start, adDate, sendDate;
	private int randomTime;

	public AdBroadcastingBehaviour(AbstractBlobAgent myBlobAgent){
		super(myBlobAgent);
		adSent=false;
		this.adTimer=myBlobAgent.getAdTimer();
		start = new Date();
		sendDate=new Date();
		randomTime = new Random().nextInt(adTimer);
		sendDate.setTime(start.getTime()+randomTime);
		adDate = new Date();
		adDate.setTime(start.getTime()+adTimer);
	}
	

	public void action() {
		super.action();
		if(!adSent && new Date().after(sendDate)) {
			myBlobAgent.sendAdMsg();
			adSent=true;
			myBlobAgent.print("Ad sent");
		}
		else if(adSent && new Date().after(adDate)) {
			this.finished = true;
			myBlobAgent.print("Message sent, end of AdTimer, creating next behaviours");
			myBlobAgent.addBehaviour(new ParallelBlobBehaviour(myBlobAgent));
		}
	}
	
	public boolean done() {
		return this.finished;
	}
}
