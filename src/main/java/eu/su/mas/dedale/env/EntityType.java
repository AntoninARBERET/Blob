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

	BLOB_AGENT("BlobAgent");
	
	private String name="";

	EntityType(String name){
		this.name=name;
	}

	@Override
	public String toString(){
		String s=this.name;
		return s;
	}

	public String getName(){
		return this.name;
	}

}
