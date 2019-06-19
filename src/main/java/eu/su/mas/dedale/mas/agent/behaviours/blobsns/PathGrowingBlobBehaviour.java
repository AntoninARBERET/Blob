package eu.su.mas.dedale.mas.agent.behaviours.blobsns;

import java.util.Random;

import eu.su.mas.dedale.mas.agent.behaviours.blob.AbstractBlobBehaviour;
import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import jade.core.behaviours.SimpleBehaviour;

public abstract class PathGrowingBlobBehaviour extends AbstractBlobBehaviour{
	private int adTimer;
	private boolean adSent;
	
	public PathGrowingBlobBehaviour(AbstractBlobAgent myBlobAgent, int adTimer){
		super(myBlobAgent);
		adSent=false;
		this.adTimer=adTimer;
	}
	
	public void action() {
		//At the first execution, send the ad at a random time before adTimer, then wait until adTimer
		if(!adSent) {
			int temp= new Random().nextInt(adTimer);
			myBlobAgent.doWait(temp);
			//TODO send AD
			myBlobAgent.doWait(adTimer-temp);
			adSent=true;
		}
		
	}
}
