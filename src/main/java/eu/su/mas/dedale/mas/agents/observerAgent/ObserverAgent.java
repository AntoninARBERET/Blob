package eu.su.mas.dedale.mas.agents.observerAgent;

import java.util.ArrayList;
import java.util.List;

import agent.AbstractDeltaAgent;
import agent.abilities.protocols.MyOntology;
import agent.abilities.protocols.ObserverProtocol;
import eu.su.mas.dedale.mas.MyDedaleOntology;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import jade.core.behaviours.Behaviour;

/**
 * This agent is a supervision one, it does not need to register to the gatekeeper.<br/>
 * Calling startMyBehaviours is thus unnecessary.<br/>
 * Currently, the GuiAgent observes the treasure state of all the agents on a given Dedale's instance. 
 * @author hc
 *
 */
public class ObserverAgent extends AbstractDeltaAgent {

	
	private static final long serialVersionUID = -4372506153529689224L;
	
	protected void setup(){
		super.setup();
		
		//1) get the parameters given into the object[]. In the current case, the type of environment where the agents will evolve
		//final Object[] args = getArguments();
				
				
		//TODO find a more userfriendly way to define the required parameters	
		List<List <Object>> behavioursParametersList= new ArrayList<List<Object>>();
		
		//first behaviour parameters
		List<Object> parametersForBehaviour1= new ArrayList<Object>();
		
		parametersForBehaviour1.add(MyDedaleOntology.ABILITY_OBSERVE_TREASURE);
		
		//TODO These agents name should be received from the gatekeeper whenever an agent is deployed
		
		List<String> agentsToObserve=new ArrayList<String>();
		agentsToObserve.add("Agent1");
		agentsToObserve.add("Agent2");
		
		//parameters for behaviour 1
		parametersForBehaviour1.add(this);
		parametersForBehaviour1.add(agentsToObserve);
		
		behavioursParametersList.add(parametersForBehaviour1);
		
		//parameters for behaviour 2
		List<Object> parametersForBehaviour2= new ArrayList<Object>();
		parametersForBehaviour2.add(this);
		
		behavioursParametersList.add(parametersForBehaviour2);
		
/**
 * The ability 
 */
		addAbility(new ObserverProtocol(), MyDedaleOntology.ABILITY_OBSERVE_TREASURE,MyOntology.PROTO_OBSERVER_ROLE_OBSERVER, behavioursParametersList, new TreasureObserverK());
		
		
		
	}

}
