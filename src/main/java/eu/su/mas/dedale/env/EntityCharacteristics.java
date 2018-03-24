package eu.su.mas.dedale.env;

import java.io.Serializable;

import debug.Debug;
import eu.su.mas.dedale.princ.ConfigurationFile;
import scala.util.Random;

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
	
	private final Observation myTreasureType;
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
		Random r;
		switch (e){
		case AGENT_COLLECTOR:
			r= new Random();
			if (r.nextInt(2)==1){
				this.myTreasureType=Observation.DIAMOND;
			}else{
				this.myTreasureType=Observation.GOLD;
			}
			this.detectionRadius=0;
			break;
		case WUMPUS: case WUMPUS_MOVER:
			r= new Random();
			if (r.nextInt(2)==1){
				this.myTreasureType=Observation.DIAMOND;
			}else{
				this.myTreasureType=Observation.GOLD;
			}
			this.detectionRadius=ConfigurationFile.DEFAULT_DETECTION_RADIUS;
			break;
		case AGENT_EXPLORER:
			this.myTreasureType=Observation.NO_TREASURE;
			this.detectionRadius=0;
			break;
		case AGENT_TANKER:
			this.myTreasureType=Observation.ANY_TREASURE;
			this.detectionRadius=0;
			break;	
		default:
			this.myTreasureType=null;
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
		switch (e) {
		case AGENT_EXPLORER:
			this.myTreasureType=Observation.NO_TREASURE;
			break;
		case AGENT_TANKER:
			this.myTreasureType=Observation.ANY_TREASURE;
			break;
		case AGENT_COLLECTOR:case WUMPUS : case WUMPUS_MOVER:
			if (diamond>0){
				this.myTreasureType=Observation.DIAMOND;
			}else{
				this.myTreasureType=Observation.GOLD;
			}
			break;
		default:
			this.myTreasureType=null;
			Debug.error("This Agent type does not currently exist");
			break;
		}

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
	public Observation getMyTreasureType(){
		return this.myTreasureType;
	}

	public Integer getCommunicationReach() {
		return communicationReach;
	}


	public EntityType getMyEntityType(){
		return myEntityType;
	}

	public String toString(){
		return "Entitytype: "+myEntityType+ "TreasureType : "+myTreasureType+"; diamondCapa: "+this.diamondCapacity+"; goldCapa: "+goldCapacity+"; comReach: "+communicationReach;
	}
	//	/**
	//	 * 
	//	 * @param o The carac (Gold)
	//	 * @param i The quantity it can wistand (50)
	//	 */
	//	public void addCaracteristic(Observation o,Integer i){
	//		this.caracs.put(o, i);
	//	}
	//	public Integer getDefaultBackPackCapacity(Observation o){
	//		return this.caracs.get(o);
	//	}




	//	public String toString(){
	//		String s="";
	//		for (Observation o:caracs.keySet()){
	//			s+=o.toString()+"\n";
	//		}
	//		return s;
	//	}

}
