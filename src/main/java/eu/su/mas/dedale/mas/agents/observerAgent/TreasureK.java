package eu.su.mas.dedale.mas.agents.observerAgent;

import java.util.HashMap;

//import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.MyDedaleOntology;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import knowledge.Knowledge;

/**
 * Knowledge component corresponding to the treasure ability.
 * At each time steps, it contains the agent current resources

 * @author hc
 *
 */
public class TreasureK extends Knowledge {

	private static final long serialVersionUID = -5841615007532085926L;
	
	private String agentName;
	private Integer goldValue;
	private Integer diamondValue;
	
	public TreasureK(String agentName, Integer goldValue, Integer diamondValue) {
		this.agentName=agentName;
		this.goldValue=goldValue;
		this.diamondValue=diamondValue;
	}

	
	
	@Override
	public void update(Agent ag) {	
		
	}

	@Override
	public void updateK(ACLMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Knowledge getPublicData(Agent ag) {
		// We return a copy of the knowledge content. It could posses less or more informations
		//TODO extract the agent name from the default TreasureK
		return new TreasureK(agentName, goldValue, diamondValue);
	}

	@Override
	public String toString() {
		return this.agentName+ "- gold: "+this.goldValue+" - diamond: "+diamondValue; 
	}

	public String getAgentName() {
		return this.agentName;
	}

	public Integer getAgentDiamondValue() {
		return this.diamondValue;
	}


	public Integer getAgentGoldVaue() {
		return this.goldValue;
	}

}
