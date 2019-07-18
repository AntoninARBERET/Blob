package eu.su.mas.dedale.mas.agent.behaviours.blob;
import java.util.Random;
import java.util.Date;
import java.util.Map;
import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.mas.knowledge.NTabEntry;

/**
 * The BlobingBehaviour is used by the BlobAgent process local informations and modify its variables
 * to simulate the real SlimeMold local behavior
 * @author arberet
 *
 */
public class BlobingBehaviourOneBlock extends AbstractBlobBehaviour{
	private static final long serialVersionUID = 8957058657171562574L;
	private boolean isSink;
	private boolean isSource;
	
	public BlobingBehaviourOneBlock(AbstractBlobAgent myBlobAgent){
		super(myBlobAgent);
		isSink=false;
		isSource=false;
		myBlobAgent.print("BlobingBehaviour constructed");
	}
	
	public void action() {
//		super.action();
//
//		
//		//reset pressure if this agent was a sink or a source at the last action
//		if(isSink) {
//			myBlobAgent.setPressure(myBlobAgent.getPressure()+myBlobAgent.getDeltaPressure());
//			isSink=false;
//		}
//		else if(isSource) {
//			myBlobAgent.setPressure(myBlobAgent.getPressure()-myBlobAgent.getDeltaPressure());	
//			isSource=false;		
//		}
//		//choose if the agent is a sink or a source this time
//		float rand = new Random().nextFloat();
//		if(rand<myBlobAgent.getProbaSink()) {
//			isSink=true;
//			myBlobAgent.print("I am sink");
//		}else if(rand<myBlobAgent.getProbaSink()+myBlobAgent.getProbaSource()) {
//			isSource=true;
//			myBlobAgent.print("I am source");
//		}
//		//local computation
//		Map<String, NTabEntry> nTab = myBlobAgent.getnTab();
//		for(int round=0; round<myBlobAgent.getRounds(); round++) {
//			float mi = 0;
//			float sum =0;
//			float sumPress =0;
//			//solve pressure based on 5
//			if(nTab.size()>0) {
//				for(Map.Entry<String, NTabEntry> entry : nTab.entrySet()) {
//					NTabEntry j = entry.getValue();
//					myBlobAgent.print(" Value to compute : id "+ j.getId() + " press "+j.getPressure() +" diam " + j.getDij()+ " Long "+j.getLij()+ " q "+j.getQij());
//					mi=mi+j.getQij();
//					sumPress=sumPress +(j.getPressure()*j.getDij()/j.getLij());
//					sum=sum +(j.getDij()/j.getLij());
//				}
//				if(sum!=0) {
//					myBlobAgent.setPressure((mi + sumPress)/sum);
//				}else {
//					myBlobAgent.setPressure(0);
//				}
//			}else {
//				myBlobAgent.setPressure(0);
//
//			}
//
//			
//			//simulate pressure difference
//			if(isSink) {
//				myBlobAgent.setPressure(myBlobAgent.getPressure()-myBlobAgent.getDeltaPressure());
//			}
//			else if(isSource) {
//				myBlobAgent.setPressure(myBlobAgent.getPressure()+myBlobAgent.getDeltaPressure());
//			}
//			//update Dij and qij, then wait to keep synchronized
//			int deltaT=myBlobAgent.getDeltaT();
//			float dMax=myBlobAgent.getdMax();
//			float r = myBlobAgent.getR();
//			float a = myBlobAgent.getA();
//			float mu = myBlobAgent.getMu();
//			for(Map.Entry<String, NTabEntry> entry : nTab.entrySet()) {
//				//Update qij based on 3a
//				float newq = (entry.getValue().getDij()/entry.getValue().getLij())*(myBlobAgent.getPressure()-entry.getValue().getPressure());
//				entry.getValue().setQij(newq);
//				for(int s=0; s<myBlobAgent.getSteps(); s++) {
//					//solve 3c for Dij based on 6
//					long startStep = new Date().getTime();
//					float q = Math.abs(entry.getValue().getQij());
//					float g = (float) (r*dMax*a*Math.pow(q, mu)/(1+a*Math.pow(q, mu)));
//					float newD = (entry.getValue().getDij()+g*deltaT)/(1+r*deltaT);
//					entry.getValue().setDij(newD);
//					long remainingTime = startStep+deltaT-new Date().getTime();
//					if(remainingTime>0) {
//						myBlobAgent.doWait(remainingTime);
//					}
//				}
//			}
//		}
//		myBlobAgent.print("Pressure = "+myBlobAgent.getPressure());
//		myBlobAgent.getRealEnv().updateNodeAndEdgesStyle(myBlobAgent);
//		myBlobAgent.getRealEnv().updateConnections(myBlobAgent.getLocalName());
		
	}
	
	public boolean done() {
		return finished;
	}
}
