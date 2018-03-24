package eu.su.mas.dedale.env;

import java.io.Serializable;

/**
 * Known by the user as the type of data the agents can obtain from the environment
 * 
 * @author hc
 *
 */
public enum Observation implements Serializable{

	/** treasure type of the entity */
	
	GOLD("Gold"),
	/** treasure type of the entity */
	DIAMOND("Diamond"),
	
	/** treasure type of the entity */
	ANY_TREASURE("Any"),
	/** treasure type of the entity */
	NO_TREASURE("None"),
	
	/**
	 * A golem releases its stench in its vicinity
	 */
	STENCH("Stench"),
	
	/**
	 * A well generates wind around itself
	 */
	
	WIND("WIND");
	
	private String name;
	//private Integer value;
	
	private Observation(String name) {
		this.name=name;
		//this.value=value;
	}
	
	
	
	@Override
	public String toString(){
		return this.name;
	}
	
	
	public String getName(){
		return this.name;
	}
}
