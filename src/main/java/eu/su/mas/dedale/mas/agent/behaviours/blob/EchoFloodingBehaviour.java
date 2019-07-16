package eu.su.mas.dedale.mas.agent.behaviours.blob;
import java.util.Random;
import java.util.Date;
import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.tools.Debug;

/**
 * AdProcessingBehaviour is the first Behaviour used by the BlobAgent
 * It send the first ad, and wait to finish approximatively at the same time
 * that the other agents.
 * @author arberet
 *
 */
public class EchoFloodingBehaviour extends AbstractBlobBehaviour{
	private boolean isTreeExisting;
	private boolean init;
	private int randomTime;
	private Date startRoot;
	private boolean isRoot;
	private String father;
	private boolean roleDecisionMade;
	private boolean waitForAck;


	public EchoFloodingBehaviour(AbstractBlobAgent myBlobAgent){
		super(myBlobAgent);
		isTreeExisting = false;
		init = false;
		randomTime = -1;
		father = null;
		isRoot=false;
		roleDecisionMade = false;
		waitForAck = false;
	}
	

	public void action() {
		super.action();
		
		//initialization : tree construction or adaptation
		if(init) {
			//no tree yet
			if(!isTreeExisting) {
				//Decide if root
				if(roleDecisionMade) {
					//tempo before becoming root
					startRoot=new Date();
					if(randomTime !=-1) {
						randomTime = new Random().nextInt(myBlobAgent.getBoundRoot());
					}
					if(new Date().getTime()-startRoot.getTime()<randomTime && !myBlobAgent.isContacted()) {
						return;
					//contacted by a node
					}else if(myBlobAgent.isContacted()) {
						father=myBlobAgent.getContact().getSender();
						roleDecisionMade=true;
						waitForAck=true;
						//TODO Contacte voisins
					//I'm root
					}else {
						roleDecisionMade=true;
						isRoot=true;
						waitForAck=true;
						//TODO Contacte voisins
					}
				}
				if(waitForAck) {
					//TODO Check ack number
				}
			}
			//tree existing
			else {
				
			}
		}
		//initialization already done
		else {
			
		}
	}
	
	public boolean done() {
		return this.finished;
	}
}
