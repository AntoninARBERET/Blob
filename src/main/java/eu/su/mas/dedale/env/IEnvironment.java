package eu.su.mas.dedale.env;

import java.util.List;


import dataStructures.tuple.Couple;

/**
 * Interface that any environment for Dedale's agent should implement
 * @author hc
 *
 */
public interface IEnvironment {

	
	/**
	 * Create an environment.
	 * @param topologyConfigurationFilePath FullPath to the file describing it. If the environment is autogenerated, null .
	 * @param instanceConfiguration FullPath to the file describing it. If the environment is autogenerated, null.
	 */

	public void CreateEnvironment(String topologyConfigurationFilePath, String instanceConfiguration,boolean isGrid, Integer envSize,boolean diamond,boolean gold,boolean well);
			
	
	/**
	 * Add an entity to the graph
	 *
	 * @param entityName the name of the entity to deploy
	 * @param e the {@link EntityCharacteristics} of the entity
	 * @param locationId the (unique) Id of the position to deploy the agent, null if free
	 */
	public void deployEntity(String entityName, EntityCharacteristics e,String locationId);
	
	public void removeEntity(String entityName,EntityCharacteristics e);
	
	/**
	 * @return The name of the environment
	 */
	public String getName();
	
	/**
	 * 
	 * @return The entity current position, null if the entity is not in the environment
	 */
	public String getCurrentPosition(String entityName);
	
	/**
	 * @param currentPosition the position from which the agent observe
	 * @param agentName the name of the agent
	 * @return The list of observed positions, and for each one the list of observations
	 */
	public List<Couple<String, List<Couple<Observation,Integer>>>> observe(String currentPosition,String agentName);
	
	/**
	 * @param entityName the name of the entity
	 * @param ec characteristics of the entity
	 * @param targetedPosition its expected destination
	 * @return 1 if true, 0 if refused, -1 if the consequence of the move is the agent dying
	 */

	public Integer moveTo(String entityName, EntityCharacteristics ec, String targetedPosition);
	
	
		
	/**
	 * 
	 * @param entityName the name of the entity
	 * @param location the position to pick the resource
	 * @param e ElementType to pick
	 * @param maxQuantity maximum number of e that the agent is able to pick
	 * @return the amount of resources that the agent was able to collect
	 */
	//@param currentPosition its current position
	public int pick(String entityName,String location, ElementType e,Integer maxQuantity);
	
		
//	/**
//	 * 
//	 * @param agentName the name of the agent
//	 * @return A list containing, for each type of resources, the remaining place in the backpack of the agent.
//	 */
//	public List<Observation> getBackPackFreeSpace(String agentName);
//		
	
	/**
	 * @param location id of the node where to drop the element
	 * @param e the element the entity intends to drop
	 * @param quantity the amount of value to drop on the current position
	 */
	public void dropOff(String location, ElementType e,Integer quantity);
	
		
		
	/**
	 * @param agentName The name of the agent that is shooting
	 * @param locationId the id of the room it aims at
	 * @return true if done, false otherwise
	 */
	public boolean throwGrenade(String agentName, String locationId);
	
	/**
	 * 
	 * @param senderName the senderAgent
	 * @param receiverName The agent we are trying to contact
	 * @param communicationReach the sender agent's com capability
	 * @return true if the receiver is within reach
	 */
	public boolean isReachable(String senderName, String receiverName, int communicationReach);

		
}
