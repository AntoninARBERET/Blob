package eu.su.mas.dedale.princ;

import eu.su.mas.dedale.env.EnvironmentType;

/**
 * 
 * @author hc
 *
 */
public final class ConfigurationFile {


	public static boolean PLATFORMisDISTRIBUTED= false;
	public static boolean COMPUTERisMAIN= true;

	public static String PLATFORM_HOSTNAME="127.0.0.1";
	public static String PLATFORM_ID="Ithaq";
	public static Integer PLATFORM_PORT=8888;

	public static String LOCAL_CONTAINER_NAME=PLATFORM_ID+"_"+"container1";
	public static String LOCAL_CONTAINER2_NAME=PLATFORM_ID+"_"+"container2";
	public static String LOCAL_CONTAINER3_NAME=PLATFORM_ID+"_"+"container3";
	public static String LOCAL_CONTAINER4_NAME=PLATFORM_ID+"_"+"container4";

	/**
	 * Required by the environment class to be able to load it
	 */
	public static EnvironmentType ENVIRONMENT_TYPE=EnvironmentType.GS;

	public static Integer DEFAULT_COMMUNICATION_REACH=3;

	public static String GATEKEEPER_NAME="GK";

	/**
	 * When the environment is loaded; should be null if the environment is generated
	 */

	public static String INSTANCE_TOPOLOGY=null;
	//public static String INSTANCE_TOPOLOGY="src/test/java/resources/map2016-topology";

	/**
	 * When the environment is loaded; should be null if the environment is generated
	 */
	public static String INSTANCE_CONFIGURATION_ELEMENTS=null;
	//public static String INSTANCE_CONFIGURATION_ELEMENTS="src/test/java/resources/map2018-elements";

	//public static String INSTANCE_CONFIGURATION_ENTITIES=null;
	//public static String INSTANCE_CONFIGURATION_ENTITIES="src/test/java/resources/map2018-agentExplo";
	//public static String INSTANCE_CONFIGURATION_ENTITIES="src/test/java/resources/map2018-agentTanker";
	//public static String INSTANCE_CONFIGURATION_ENTITIES="src/test/java/resources/map2018-agentCollect";
	//public static String INSTANCE_CONFIGURATION_ENTITIES="src/test/java/resources/map2018-entities";
	//public static String INSTANCE_CONFIGURATION_ENTITIES="src/test/java/resources/map2018-agentGolem";
	public static String INSTANCE_CONFIGURATION_ENTITIES="src/test/java/resources/blob-10agents";
	/************************************
	 * 
	 * 
	 * When the environment is generated (Instance_topology and instance configuration elements are null) 
	 * 
	 * 
	 ***********************************/

	/**
	 * Parameter used to generate the environment 
	 */
	public static Integer ENVIRONMENT_SIZE=7;

	/**
	 * Parameter used to perceive the wumpus
	 */
	public static final Integer DEFAULT_DETECTION_RADIUS = 1;

	/**true if a grid environment should be generated, false otherwise (A dogoronev env is generated)**/
	public static boolean ENVIRONMENTisGRID=true;
	public static boolean ACTIVE_WELL=false;
	public static boolean ACTIVE_GOLD=true;
	public static boolean ACTIVE_DIAMOND=true;
	
	/**Blob part**/
	public static int NB_BLOB_AG= 10;
	public static float PROBA_SINK=(float) 0.1;
	public static float PROBA_SOURCE=(float) 0.1;
	public static int ROUNDS= 3;
	public static int STEPS= 3;
	public static float DELTA_PRESSURE=(float) 1;
	public static int DELTA_T= 300;
	public static int DELTA_T_SYNC= 1000;
	public static float D_MAX=(float) 3;
	public static float R=(float) 0.1;
	public static float MU=(float) 0.5;
	public static float A=(float) 0.1;
	public static int AD_TIMER= 1000;
}