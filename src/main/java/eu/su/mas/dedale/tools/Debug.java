package eu.su.mas.dedale.tools;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


/**
 * This class allow to activate/deactivate debug messages according to their respective authorization level.
 * 3 modes are available:
 *  -  print messages whose verboseType is equal to the defined level (infegal==0)
 *  -  print messages whose verboseType is inferior or equal to the defined level (infegal==1)
 *  -  print messages whose verboseType belongs to the set of levels in "debugSet" (infegal==2)
 * 
 */

public class Debug {	


	/************************************************
	 * 
	 * Registered Keys
	 * 
	 **********************************************/

	/**
	 * Enum keys that express the meaning of a verbosity number
	 */ 
	//	Application(0), 
	//	/* */
	//	Warning(1), Jade(2), SystemInitialization(3),Unimplemented(4),
	//	/* */
	//	Observation(10), 
	//
	//	None(Integer.MAX_VALUE)
	//	;

	/************************************************
	 * 
	 * Configuration Fields
	 * 
	 **********************************************/

	/**
	 * Holds the different behaviors of the logger 
	 */
	enum DebugConf {
		ExactlyVerboseLevel, EverythingLowerThanVerboseLEvel, ExactlyDebugSet;
	}
	static DebugConf infegal = DebugConf.ExactlyVerboseLevel;

	/**
	 * The reference verbose level used for 
	 * DebugConf.ExactlyVerboseLevel and DebugConf.EverythingLowerThanVerboseLEvel
	 */
	static int verboseLevel=2;
	
	static Collection<Integer> debugSet;
	


	public static boolean printDetails;//allow to express more information for a log
	public static boolean debugSend;//activate automatic printing of send messages
	public static boolean debugReceive;//activate automatic printing of received messages
	public static boolean keepMessagesDescription;

	/************************************************
	 * 
	 * Constructor
	 * 
	 **********************************************/

	private int n;

	/**
	 * @param number
	 */
	private Debug(final int number) {
		this.n = number;
	}

	
	/************************************************
	 * 
	 * PUBLIC METHODS
	 * 
	 **********************************************/

	
	public static void setDebugSet(Collection<Integer> debugSet) {
		Debug.debugSet = debugSet;
	}
	/**
	 * Debug message for static functions
	 * 
	 * @param message
	 *            content of the message
	 * @param verboseType
	 *            type of the message, see the Debug class to create your
	 *            verbose type
	 */
	public static void info(final String message, final int verboseType) {
		Debug.log("\n" + message, System.out, verboseType);
	}

	/**
	 * Debug message
	 * 
	 * @param classe
	 *            Usually this.getClass()
	 * @param message
	 *            content of the message
	 * @param verboseType
	 *            type of the message
	 */
	@SuppressWarnings("rawtypes")
	public static void info(final Class classe, final String message, final int verboseType) {
		Debug.info(classe.getName() + " - " + message, verboseType);
	}



	/**
	 * Debug message for static functions
	 * 
	 * @param message
	 *            content of the message
	 *
	 */
	public static void info(final String message) {
		info(message, 42);
	}

	/*
	 * 
	 */
	/**
	 * Print in red, the throwable trace but do not raise an exception
	 * 
	 * @param message
	 * @param verboseType
	 * @throws Exception
	 */
	public static void warning(final String message, final int verboseType) {
		Debug.log("\n" + message, System.err, verboseType);
	}

	public static void warning(final String message) {
		Debug.warning(message, 42);
	}
	public static void warning(final String message, final Throwable e, final int verboseType) {
		Debug.log("\n" + message,  e,System.err, verboseType);
	}

	public static void warning(final String message, final Throwable e) {
		Debug.warning(message,  e,42);
	}
	
	public static void warning(Class<?> c, final String message){
		Debug.warning(c.getName()+"\n"+ message,42);
	}

	/*
	 * 
	 */

	/**
	 * Critical point that should trigger an error message and raise an exception
	 * 
	 * @param message
	 * @param verboseType
	 * @throws Exception
	 */
	public static RuntimeException error(final String message, final int  verboseType) {
		Debug.warning(message, verboseType);
		throw new RuntimeException(message);
	}

	/**
	 * Critical point that should trigger an error message.
	 * 
	 * @param message
	 * @param verboseType
	 * @throws Exception
	 */
	public static RuntimeException error(final String message, final Throwable e, final int  verboseType) {
		Debug.warning(message, e, verboseType);
		throw new RuntimeException(message,e);
	}

	public static RuntimeException error(final String message) {
		return Debug.error(message,42);
	}

	public static RuntimeException error(final String message, final Throwable e) {
		return Debug.error(message,e,42);
	}

	public static RuntimeException error(final Throwable e) {
		return Debug.error("",e,42);
	}
	

	/************************************************
	 * 
	 * PRIMITIVES
	 * 
	 **********************************************/

	public static void log(final String text, final PrintStream printer,  final int verboseType) {
		if (Debug.logIt(verboseType)) {
			printer.println("\n ############## On "+new Date()+" : \n"+text);
		}
	}

	public static void log(final String text,  final Throwable e,final PrintStream printer,  final int verboseType) {
		Debug.log(text, printer, verboseType);
		if (Debug.logIt(verboseType)) {
			e.printStackTrace(printer);
		}
	}
	/**
	 * @param verboseType
	 * @return 
	 */
	private static boolean logIt(final int verboseType) {
		return verboseType == 42 
				|| Debug.infegal.equals(DebugConf.ExactlyVerboseLevel) && verboseType == Debug.verboseLevel
				|| Debug.infegal.equals(DebugConf.EverythingLowerThanVerboseLEvel) && verboseType <= Debug.verboseLevel
				|| Debug.infegal.equals(DebugConf.ExactlyDebugSet) && Debug.debugSet.contains(verboseType);
	}


}





/************************************************
 * 
 * Configuration Fields
 * 
 **********************************************/

/**
 * Holds the different behaviors of the logger 
 */
enum DebugConf {
	ExactlyVerboseLevel, EverythingLowerThanVerboseLEvel, ExactlyDebugSet;



	static DebugConf infegal;

	/**
	 * The reference verbose level used for 
	 * DebugConf.ExactlyVerboseLevel and DebugConf.EverythingLowerThanVerboseLEvel
	 */
	static int verboseLevel;
	static Collection<Integer> debugSet;

	public static boolean printDetails;//allow to express more information for a log
	public static boolean debugSend;//activate automatic printing of send messages
	public static boolean debugReceive;//activate automatic printing of received messages
	public static boolean keepMessagesDescription;

	/************************************************
	 * 
	 * Constructor
	 * 
	 **********************************************/

	private int n;


	

	/************************************************
	 * 
	 * PUBLIC METHODS
	 * 
	 **********************************************/

	/**
	 * Debug message for static functions
	 * 
	 * @param message
	 *            content of the message
	 * @param verboseType
	 *            type of the message, see the Debug class to create your
	 *            verbose type
	 */
	public static void info(final String message, final int verboseType) {
		Debug.log("\n" + message, System.out, verboseType);
	}

	/**
	 * Debug message
	 * 
	 * @param classe
	 *            Usually this.getClass()
	 * @param message
	 *            content of the message
	 * @param verboseType
	 *            type of the message
	 */
	@SuppressWarnings("rawtypes")
	public static void info(final Class classe, final String message, final int verboseType) {
		Debug.info(classe.getName() + " - " + message, verboseType);
	}




	/*
	 * 
	 */
	/**
	 * Print in red, the throwable trace but do not raise an exception
	 * 
	 * @param message
	 * @param verboseType
	 * @throws Exception
	 */
	public static void warning(final String message, final int verboseType) {
		Debug.log("\n" + message, System.err, verboseType);
	}

	public static void warning(final String message) {
		Debug.warning(message, 42);
	}
	public static void warning(final String message, final Throwable e, final int verboseType) {
		Debug.log("\n" + message,  e,System.err, verboseType);
	}

	public static void warning(final String message, final Throwable e) {
		Debug.warning(message,  e,42);
	}

	/*
	 * 
	 */

	/**
	 * Critical point that should trigger an error message and raise an exception
	 * 
	 * @param message
	 * @param verboseType
	 * @throws Exception
	 */
	public static RuntimeException error(final String message, final int  verboseType) {
		Debug.warning(message, verboseType);
		throw new RuntimeException(message);
	}

}




