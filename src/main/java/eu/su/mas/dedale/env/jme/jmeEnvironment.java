package eu.su.mas.dedale.env.jme;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.ElementType;
import eu.su.mas.dedale.env.EntityCharacteristics;
import eu.su.mas.dedale.env.EntityType;
import eu.su.mas.dedale.env.IEnvironment;
import eu.su.mas.dedale.env.Observation;

public class jmeEnvironment implements IEnvironment {

	public void CreateEnvironment(String topologyConfigurationFilePath, String instanceConfiguration) {
		// TODO Auto-generated method stub
		
	}

	
	public void deployEntity(String entityName, EntityCharacteristics e, String locationId) {
		// TODO Auto-generated method stub
		
	}

	
	public void removeEntity(String entityName, EntityCharacteristics e) {
		// TODO Auto-generated method stub
		
	}
	
	public String getCurrentPosition(String agentName) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Couple<String, List<Couple<Observation, Integer>>>> observe(String currentPosition, String agentName) {
		// TODO Auto-generated method stub
		return null;
	}


	
	public Integer moveTo(String entityName, EntityCharacteristics ec, String targetedPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public int pick(String entityName, String location, ElementType e, Integer maxQuantity) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	public boolean isReachable(String senderName, String receiverName, int communicationReach) {
		// TODO Auto-generated method stub
		return false;
	}

	public void dropOff(String agentName, ElementType e, Integer quantity) {
		// TODO Auto-generated method stub
		
	}

	public boolean throwGrenade(String agentName, String targetName) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	


	

}
