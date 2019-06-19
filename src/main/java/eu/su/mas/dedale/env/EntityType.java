package eu.su.mas.dedale.env;

import java.io.Serializable;

/**
 * All the types of living entities that can be found in the environment
 * Its independent of the environment instantiation and known by the users
 * 
 * @author hc
 *
 */
public enum EntityType implements Serializable{

	AGENT_EXPLORER("AgentExplo"), //can only explore
	AGENT_TANKER("AgentTanker"),// can only store (and move)
	AGENT_COLLECTOR("AgentCollect"), //can do anything 
	WUMPUS("Wumpus"), //can only move
	WUMPUS_MOVER("WumpusMover"), //can move and shift resources
	BLOB_AGENT("BlobAgent");
	
	private String name="";
	
	//private EntityCaracteristics caracs=null;


	EntityType(String name){
		this.name=name;
		//this.caracs=caracteristics;
	}

	@Override
	public String toString(){
		String s=this.name;
		/*if (this.caracs!=null){
			s="("+s+","+this.caracs.toString()+")";
		}*/
		return s;//"("+this.name+ ","+this.value.toString()+")";
	}

	public String getName(){
		return this.name;
	}
	
//	/**
//	 * 
//	 * @return null if nothing is associated
//	 */
//	public EntityCaracteristics getEntityCaracs(){
//		return this.caracs;
//	}

	
	/*public void setValue(Object value){
		this.value=value;
	}*/
}
