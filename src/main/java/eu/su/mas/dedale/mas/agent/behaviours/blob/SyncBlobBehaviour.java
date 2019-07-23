package eu.su.mas.dedale.mas.agent.behaviours.blob;
import java.util.Date;
import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.tools.Debug;

/**
 * SyncBlobBehaviour is used by BlobAgent to broadcast periodically its computing results 
 * @author arberet
 *
 */
public class SyncBlobBehaviour extends AbstractBlobBehaviour{
	private static final long serialVersionUID = -3357762763554435416L;
	private int deltaTSync;
	private boolean sent;
	private Date start ;
	
	public SyncBlobBehaviour(AbstractBlobAgent myBlobAgent){
		super(myBlobAgent);
		//To keep synchronize in temporized mode
		if(AbstractBlobAgent.TEMPO) {
			this.deltaTSync = (int)(myBlobAgent.getDeltaTSync()+myBlobAgent.getRounds()*AbstractBlobAgent.TEMPOTIME);
		}else {
			this.deltaTSync = myBlobAgent.getDeltaT();
		}
		sent =false;
		Debug.info(myBlobAgent.getPrintPrefix()+"SyncBlobBehaviour constructed",4);

	}
	
	public void action() {
		//check if the agent lost some connections
		myBlobAgent.checkContacts();
		
		//when the agent must send a message
		if(sent==false) {
			start = new Date();
			//myBlobAgent.sendStatetMsg();
			sent=true;
		}
		//when it do not
		long remain = (deltaTSync - (new Date().getTime() - start.getTime()));
		if(remain<=0) {
			sent=false;
		}
	}
	
	
	public boolean done() {
		return finished;
	}
}
