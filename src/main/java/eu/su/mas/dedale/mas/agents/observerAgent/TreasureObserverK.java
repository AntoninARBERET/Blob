package eu.su.mas.dedale.mas.agents.observerAgent;

import debug.Debug;
import eu.su.mas.dedale.mas.agents.observerAgent.guiComponents.GuiTreasureObserver;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import knowledge.Knowledge;

/**
 * This class corresponds to the knowledge component of an agent that is willing to observe the Treasure state of others 
 * through the gui
 * @author hc
 *
 */
public class TreasureObserverK extends Knowledge {
	
	private GuiTreasureObserver guiTreasureObs;

	private static final long serialVersionUID = 671883855087431362L;
	
	
	public TreasureObserverK() {
		guiTreasureObs=GuiTreasureObserver.getInstance();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void update(Agent ag) {
		Debug.info("Do nothing");

	}


	@Override
	public void updateK(ACLMessage msg) {
		//Should update the Gui
		TreasureK data=null;
		try {
			data = (TreasureK) msg.getContentObject();
		} catch (UnreadableException e) {
			e.printStackTrace();
		}
		this.guiTreasureObs.updateAgentTreasure(data.getAgentName(),data.getAgentGoldVaue(),data.getAgentDiamondValue());
	}

	@Override
	public Knowledge getPublicData(Agent ag) {
		Debug.info("There is no public data for this knowledge component: "+this.getClass().getName());
		return null;
	}

	@Override
	public String toString() {
		return guiTreasureObs.toString();
	}

}
