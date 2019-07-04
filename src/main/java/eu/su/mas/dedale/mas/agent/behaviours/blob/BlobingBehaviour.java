package eu.su.mas.dedale.mas.agent.behaviours.blob;
import java.util.Random;
import java.util.Date;
import java.util.Map;
import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.mas.knowledge.NTabEntry;
import eu.su.mas.dedale.tools.Debug;

/**
 * The BlobingBehaviour is used by the BlobAgent process local informations and modify its variables
 * to simulate the real SlimeMold local behavior
 * @author arberet
 *
 */
public class BlobingBehaviour extends AbstractBlobBehaviour{
	private static final long serialVersionUID = 8957058657171562574L;
	private boolean isSink;
	private boolean isSource;
	private boolean start;
	private int roundsDone;
	private Date startDate;
	private float previousPressure;
	
	
	public BlobingBehaviour(AbstractBlobAgent myBlobAgent){
		super(myBlobAgent);
		isSink=false;
		isSource=false;
		Debug.info(myBlobAgent.getPrintPrefix()+"BlobingBehaviour constructed",4);
		start = true;
		roundsDone=0;
	}
	
	public void action() {
		super.action();
		
		//executed before a new rounds cycle
		if(start) {
			//reset pressure if this agent was a sink or a source at the last action
			if(isSink) {
				myBlobAgent.setPressure(previousPressure);
				//myBlobAgent.setPressure(myBlobAgent.getPressure()+myBlobAgent.getDeltaPressure());
				isSink=false;
			}
			else if(isSource) {
				myBlobAgent.setPressure(previousPressure);
				//myBlobAgent.setPressure(myBlobAgent.getPressure()-myBlobAgent.getDeltaPressure());	
				isSource=false;		
			}
			//choose if the agent is a sink or a source this time
			float rand = new Random().nextFloat();
			if(rand<myBlobAgent.getProbaSink()) {
				isSink=true;
				//myBlobAgent.setPressure(myBlobAgent.getPressure()-myBlobAgent.getDeltaPressure());	
				previousPressure = myBlobAgent.getPressure();
				myBlobAgent.setPressure(-1*myBlobAgent.getDeltaPressure());
				Debug.info(myBlobAgent.getPrintPrefix()+"I am sink",2);

			}else if(rand<myBlobAgent.getProbaSink()+myBlobAgent.getProbaSource()) {
				isSource=true;
				//myBlobAgent.setPressure(myBlobAgent.getPressure()+myBlobAgent.getDeltaPressure());	
				previousPressure = myBlobAgent.getPressure();
				myBlobAgent.setPressure(myBlobAgent.getDeltaPressure());	
				Debug.info(myBlobAgent.getPrintPrefix()+"I am source",2);			}
			start=false;
			startDate=new Date();
		}
		
		if(roundsDone<myBlobAgent.getRounds()) {
			//local computation, executed once for each round, then sink and source are reset
			Map<String, NTabEntry> nTab = myBlobAgent.getnTab();
			
			
			float mi = 0;
			float sum =0;
			float sumPress =0;
			
			//solve pressure based on 5
			if(nTab.size()>0&&!isSink&&!isSource) {
				for(Map.Entry<String, NTabEntry> entry : nTab.entrySet()) {
					NTabEntry j = entry.getValue();
					Debug.info(myBlobAgent.getPrintPrefix()+" Value to compute : id "+ j.getId() + " press "+j.getPressure() +" diam " + j.getDij()+ " Long "+j.getLij()+ " q "+j.getQij(),2);
					mi=mi+j.getQij();
					sumPress=sumPress +(j.getPressure()*j.getDij()/j.getLij());
					sum=sum +(j.getDij()/j.getLij());
				}
				if(sum!=0) {
					myBlobAgent.setPressure((mi + sumPress)/sum);
				}else {
					myBlobAgent.setPressure(0);
				}
			}/*else if(nTab.size()==0){
				myBlobAgent.setPressure(0);

			}*/

			
			/*//simulate pressure difference
			if(isSink) {
				//myBlobAgent.setPressure(myBlobAgent.getPressure()-myBlobAgent.getDeltaPressure());
			}
			else if(isSource) {
				//myBlobAgent.setPressure(myBlobAgent.getPressure()+myBlobAgent.getDeltaPressure());
			}*/
			//update Dij and qij, then wait to keep synchronized
			int deltaT=myBlobAgent.getDeltaT();
			float dMax=myBlobAgent.getdMax();
			float r = myBlobAgent.getR();
			float a = myBlobAgent.getA();
			float mu = myBlobAgent.getMu();
			for(Map.Entry<String, NTabEntry> entry : nTab.entrySet()) {
				//Update qij based on 3a
				float newq = (entry.getValue().getDij()/entry.getValue().getLij())*(myBlobAgent.getPressure()-entry.getValue().getPressure());
				entry.getValue().setQij(newq);
				for(int s=0; s<myBlobAgent.getSteps(); s++) {
					//solve 3c for Dij based on 6
					float q = Math.abs(entry.getValue().getQij());
					float g = (float) (r*dMax*a*Math.pow(q, mu)/(1+a*Math.pow(q, mu)));
					float newD = (entry.getValue().getDij()+g*deltaT)/(1+r*deltaT);
					if(newD>myBlobAgent.getdMax()) {
						newD=myBlobAgent.getdMax();
					}
					entry.getValue().setDij(newD);

				}
			}
			
			//checking the round number
			roundsDone++;
		}
		
		
		
		if(roundsDone==myBlobAgent.getRounds()) {
			//TODO with tempo
			if(new Date().getTime()-startDate.getTime()<myBlobAgent.getRounds()*myBlobAgent.getSteps()*myBlobAgent.getDeltaT()) {
				return;
			}
			start=true;
			roundsDone=0;
		}
		
		Debug.info(myBlobAgent.getPrintPrefix()+"Pressure = "+myBlobAgent.getPressure(),2);
		myBlobAgent.getRealEnv().updateNodeAndEdgesStyle(myBlobAgent);
		myBlobAgent.getRealEnv().updateConnections(myBlobAgent.getLocalName());
		
	}
	
	public boolean done() {
		return finished;
	}
}
