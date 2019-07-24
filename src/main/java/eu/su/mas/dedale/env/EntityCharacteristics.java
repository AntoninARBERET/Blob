package eu.su.mas.dedale.env;
import java.io.Serializable;
import debug.Debug;
import eu.su.mas.dedale.princ.ConfigurationFile;


/**
 * This class contain, for each agent, all its (default) characteristics
 * @author hc
 *
 */
public final class EntityCharacteristics implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7070846449444089267L;
	
	private final EntityType myEntityType;
	private final Integer diamondCapacity;
	private final Integer goldCapacity;
	private final Integer communicationReach;
	private final String initialLocation;
	private final Integer detectionRadius;

	public EntityCharacteristics(EntityType e){
		this.initialLocation=null;
		this.myEntityType=e;
		this.diamondCapacity=0;
		this.goldCapacity=0;
		this.communicationReach=ConfigurationFile.DEFAULT_COMMUNICATION_REACH;
		switch (e){
		
		default:
			this.detectionRadius=0;
		}		
	}

/**
 * 
 * @param e
 * @param diamond
 * @param gold
 * @param comReach
 * @param initialLocation
 * @param detectionRadius
 */
	public EntityCharacteristics(EntityType e,int diamond,int gold, int comReach,String initialLocation,int detectionRadius){
		this.diamondCapacity=diamond;
		this.goldCapacity=gold;
		this.communicationReach=comReach;
		this.myEntityType=e;
		this.initialLocation=initialLocation;
		this.detectionRadius=detectionRadius;
		//mytreasure type should be determined from the combination of EntityType and diamond/gold capacities



	}

	/**
	 * 
	 * @return null if no initial location enforced , locationId otherwise
	 */
	public String getInitialLocation() {	
		return this.initialLocation;
	}

	public Integer getGoldCapacity(){
		return this.goldCapacity;
	}

	public Integer getDiamondCapacity(){
		return this.diamondCapacity;
	}

	public Integer getDetectionRadius(){
		return this.detectionRadius;
	}

	public Integer getCommunicationReach() {
		return communicationReach;
	}


	public EntityType getMyEntityType(){
		return myEntityType;
	}

	public String toString(){
		return "Entitytype: "+myEntityType+ " comReach: "+communicationReach;
	}

}
