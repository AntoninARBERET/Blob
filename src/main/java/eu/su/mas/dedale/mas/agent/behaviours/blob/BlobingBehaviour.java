package eu.su.mas.dedale.mas.agent.behaviours.blob;
import java.util.Random;

import dataStructures.tuple.Couple;

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
	private boolean start;
	private int roundsDone;
	private Date startDate;
	private boolean updatePhase, decisionPhase, computingPhase ;
	private int availableFood;
	private  Couple<Integer,Map<String,Integer>> decision;
	private boolean allStatesReceived;
	private Date startDateMaj;
	
	
	
	
	public BlobingBehaviour(AbstractBlobAgent myBlobAgent){
		super(myBlobAgent);
		Debug.info(myBlobAgent.getPrintPrefix()+"BlobingBehaviour constructed",4);
		start = true;
		roundsDone=0;
		availableFood=0;
		allStatesReceived=false;
		startDateMaj=null;
		
		updatePhase=false; 
		decisionPhase = false; 
		computingPhase = false;
	}
	
	public void action() {
		super.action();
		if(Math.abs(myBlobAgent.getFood())>3000) {
			Debug.info(myBlobAgent.getPrintPrefix()+" food too high or low : "+myBlobAgent.getFood(),7);
		}
		//executed before a new rounds cycle
		if(start) {
			myBlobAgent.setFood(Math.max(myBlobAgent.getFood()-2, 0));
			myBlobAgent.incLastExplo();
			//myBlobAgent.setFood((myBlobAgent.getFood()-2));
			availableFood=0;
			
			start=false;
			startDate=new Date();
			startDateMaj=null;
			updatePhase=true;
			allStatesReceived=false;
		}
		
		//time allowed to get update of neighbours states
		else if(updatePhase) {
			if(startDateMaj == null) {
				startDateMaj = new Date();
			}
			if(!allStatesReceived) {
				allStatesReceived=true;
				for(NTabEntry entry : myBlobAgent.getnTab().values()) {
					if(entry.isUsed()){
						allStatesReceived=false;
					}
				}
			}
			if(!allStatesReceived || new Date().getTime()-startDateMaj.getTime()<myBlobAgent.getDeltaT()) {
				return;
			}
			
			else{
				
				updatePhase =false;
				decisionPhase = true;
			}
		}
		//making a decision with states and food available
		//TODO change decision, use food owned too
		else if(decisionPhase) {
			if(myBlobAgent.getMode()==AbstractBlobAgent.Modes.RANDOM) {
				float rand = new Random().nextFloat();
				if(rand<myBlobAgent.getProbaSource()) {
					
					availableFood=20;	
					Debug.info(myBlobAgent.getPrintPrefix()+"I am source",2);	
					
				}
				decision = myBlobAgent.getDecision(availableFood);
			}
			else if(myBlobAgent.getMode()==AbstractBlobAgent.Modes.STATIC_FOOD) {
				//TODO
			}
			else if(myBlobAgent.getMode()==AbstractBlobAgent.Modes.FOOD_IN_ENV) {
				decision=myBlobAgent.decideAndPick();
			}
			
			if(myBlobAgent.isExplorationEnabled() && myBlobAgent.isAbleToExplore()) {
				myBlobAgent.explore();
			}
			
			
		
			myBlobAgent.setFood(myBlobAgent.getFood()+decision.getLeft().intValue());
			for(String k : decision.getRight().keySet()) {
				myBlobAgent.sendFoodMsg(k, decision.getRight().get(k).intValue());
			}
			decisionPhase = false;

			computingPhase=true;
		}
		//computing new values
		else if(computingPhase) {
			if(roundsDone<myBlobAgent.getRounds()) {
				//local computation, executed once for each round, then sink and source are reset
				Map<String, NTabEntry> nTab = myBlobAgent.getnTab();

				
				//update Dij and qij, then wait to keep synchronized
				int deltaT=myBlobAgent.getDeltaT();
				float dMax=myBlobAgent.getdMax();
				float r = myBlobAgent.getR();
				float a = myBlobAgent.getA();
				float mu = myBlobAgent.getMu();
				for(Map.Entry<String, NTabEntry> entry : nTab.entrySet()) {
					//Update qij based on 3a
					float newq = (entry.getValue().getDij()/entry.getValue().getLij())*(myBlobAgent.getFood()-entry.getValue().getFood()/*entry.getValue().getSentFood()*/);
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
				
				if(new Date().getTime()-startDate.getTime()<myBlobAgent.getRounds()*myBlobAgent.getSteps()*myBlobAgent.getDeltaT()) {
					return;
				}
				computingPhase=false;
				start=true;
				roundsDone=0;
				
			}
			
		}

		Debug.info(myBlobAgent.getPrintPrefix()+"Food = "+myBlobAgent.getFood(),2);
		myBlobAgent.getRealEnv().updateNodeAndEdgesStyle(myBlobAgent);
		myBlobAgent.getRealEnv().updateConnections(myBlobAgent.getLocalName());
		myBlobAgent.sendStatetMsg();
		
	}
	
	public boolean done() {
		return finished;
	}
}
