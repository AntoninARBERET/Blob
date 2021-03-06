package eu.su.mas.dedale.mas.agents.blobAgents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;
import org.junit.Assert;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.blob.AdBroadcastingBehaviour;
import eu.su.mas.dedale.mas.agent.behaviours.blob.ReceiveMessageBehaviour;
import eu.su.mas.dedale.mas.knowledge.LastContactTabEntry;
import eu.su.mas.dedale.mas.knowledge.NTabEntry;
import eu.su.mas.dedale.mas.msgcontent.AdMsgContent;
import eu.su.mas.dedale.mas.msgcontent.CoLostMsgContent;
import eu.su.mas.dedale.mas.msgcontent.FoodMsgContent;
import eu.su.mas.dedale.mas.msgcontent.StateMsgContent;
import eu.su.mas.dedale.princ.ConfigurationFile;
import eu.su.mas.dedale.tools.Debug;
import jade.core.AID;
import jade.core.Agent;
import eu.su.mas.dedale.env.gs.gsEnvironmentBlob;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public abstract class  AbstractBlobAgent extends Agent{

	protected static final long serialVersionUID = -2991562876411096907L;
	protected Node myNode;
	protected float posX, posY;
	protected ArrayList<String> agentsIds;
	protected String nodeType;
	protected float foodVal;
	protected int seqNo;
	protected Map<String, NTabEntry> nTab;
	protected float probaSink;
	protected float probaSource;
	protected int rounds;
	protected int steps;
	//pressure difference between sink or source and 0
	protected float deltaPressure;
	protected int deltaT;
	protected int deltaTSync;
	protected float dMax;
	protected float r;
	protected float mu;
	protected float a;
	protected int adTimer;
	protected HashMap<String, LastContactTabEntry> lastContact;
	protected HashMap<String, HashSet<String>> routingTab;
	protected ReentrantReadWriteLock mutexX, mutexY, mutexP, mutexF;
	protected gsEnvironmentBlob realEnv;
	protected Modes mode;
	public static final boolean TEMPO = true;
	public static final int TEMPOTIME = 1000;
	private int food;
	private int foodBound;
	private int originalPickCapacity;
	private int pickCapacity;
	private ArrayList<Integer> meanFoodHistory;
	private ArrayList<Integer> sentFoodHistory;
	private int foodConso;
	private boolean onFood;
	private int upgradedFor;
	private boolean explorationEnabled;
	private int lastExplo;
	private int tempoExplo;
	private float myPropFood;
	private float neighboursPropFood;
	private float probaExplo;
	private int nbDirection;
	private int distMin;
	private int distMax;
	private float probaDeviation;
	private float propKeep;

	
	
	
	public enum Modes{
		RANDOM, STATIC_FOOD,FOOD_IN_ENV; 
	}
	
	

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){
		super.setup();
		
		

		//Parameters
		final Object[] args = getArguments();
		this.agentsIds=(ArrayList<String>) args[2];
		this.myNode=(Node) args[3];
		this.probaSource=((Float) args[4]).floatValue();
		this.rounds=((Integer) args[5]).intValue();
		this.steps=((Integer) args[6]).intValue();
		this.deltaT=((Integer) args[7]).intValue();
		this.dMax=((Float) args[8]).floatValue();
		this.r=((Float) args[9]).floatValue();
		this.mu=((Float) args[10]).floatValue();
		this.a=((Float) args[11]).floatValue();
		this.adTimer=((Integer) args[12]).intValue();
		this.realEnv=(gsEnvironmentBlob) args[13];
		this.mode=(Modes) args[14];
		this.foodBound= ((Integer) args[15]).intValue();
		this.pickCapacity= ((Integer) args[16]).intValue();
		this.foodConso= ((Integer) args[17]).intValue();
		this.explorationEnabled=((Boolean) args[18]).booleanValue();
		this.tempoExplo= ((Integer) args[19]).intValue();
		this.myPropFood=((Float) args[20]).floatValue();
		this.neighboursPropFood=((Float) args[21]).floatValue();
		this.probaExplo=((Float) args[22]).floatValue();
		this.nbDirection= ((Integer) args[23]).intValue();
		this.distMin= ((Integer) args[24]).intValue();
		this.distMax= ((Integer) args[25]).intValue();
		this.probaDeviation=((Float) args[26]).floatValue();
		this.propKeep=((Float) args[26]).floatValue();

		
		
		//initializations
		this.seqNo=0;
		this.nTab=Collections.synchronizedMap(new HashMap<String, NTabEntry>());
		this.lastContact=new HashMap<String, LastContactTabEntry>();
		this.routingTab = new HashMap<String, HashSet<String>>();
		this.food=0;
		this.meanFoodHistory=new ArrayList<Integer>();
		this.sentFoodHistory=new ArrayList<Integer>();
		this.originalPickCapacity=pickCapacity;
		this.onFood=false;
		upgradedFor=0;
		lastExplo=10;
		
		this.mutexX = new ReentrantReadWriteLock();
		this.mutexY = new ReentrantReadWriteLock();
		this.mutexP = new ReentrantReadWriteLock();
		this.mutexF = new ReentrantReadWriteLock();
	
		myNode.setAttribute("agentId", getLocalName());
		
		//lb.add(new AdBroadcastingBehaviour(this));
		//lb.add(new AdBroadcastingBehaviour(this));
		
		//tests
		//addBehaviour(new TestBlobGuiBehaviour(this));
		
		//addBehaviour(new startMyBehaviours(this,lb));
		Debug.info(getPrintPrefix()+" mode = " + myNode.getAttribute("food"), 2);
		
		
		addBehaviour(new AdBroadcastingBehaviour(this));
		addBehaviour(new ReceiveMessageBehaviour(this));

		
			
				
		
		
	}


	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
		super.takeDown();
	}
	
	/**
	 * This method is automatically called before migration. 
	 * You can add here all the saving you need
	 */
	protected void beforeMove(){
		super.beforeMove();
	}
	
	/**
	 * This method is automatically called after migration to reload. 
	 * You can add here all the info regarding the state you want your agent to restart from 
	 * 
	 */
	protected void afterMove(){
		super.afterMove();
	}

	public float getPosX() {
		this.mutexX.readLock().lock();
		float val = (float)GraphPosLengthUtils.nodePosition(myNode)[0];
		this.mutexX.readLock().unlock();
		return val;
	}
	
	public float getPosY() {
		this.mutexY.readLock().lock();
		float val = (float)GraphPosLengthUtils.nodePosition(myNode)[1];
		this.mutexY.readLock().unlock();
		return val;
	}
	
	public float getFoodVal() {
		return foodVal;
	}


	public void setFoodVal(float foodVal) {
		this.foodVal = foodVal;
	}


	/**
	 * get the next seq number
	 * @return
	 */
	private synchronized int getAndIncSeqNo() {
		seqNo++;
		return seqNo;
	}





	public Node getMyNode() {
		return myNode;
	}


	public String getNodeType() {
		return nodeType;
	}




	public Map<String, NTabEntry> getnTab() {
		return nTab;
	}


	
	public float getProbaSink() {
		return probaSink;
	}


	public float getProbaSource() {
		return probaSource;
	}

	public int getRounds() {
		return rounds;
	}

	public int getSteps() {
		return steps;
	}

	public float getDeltaPressure() {
		return deltaPressure;
	}
	
	
	public int getDeltaT() {
		return deltaT;
	}


	public int getDeltaTSync() {
		return deltaTSync;
	}

	public float getdMax() {
		return dMax;
	}


	public float getR() {
		return r;
	}

	public float getMu() {
		return mu;
	}


	public float getA() {
		return a;
	}


	public int getAdTimer() {
		return adTimer;
	}


	public HashMap<String, LastContactTabEntry> getLastContact() {
		return lastContact;
	}
	
	/**
	 * get next node to forward a package for dest
	 */
	public HashSet<String> getNextTo(String dest){
		return routingTab.get(dest);
	}
	
	
	public void addToRoutingTab(String dest, String hop) {
		if(routingTab.containsKey(dest)) {
			routingTab.get(dest).add(hop);
		}else {
			HashSet<String> hs = new HashSet<String>();
			hs.add(hop);
			routingTab.put(dest, hs);
		}
	}
	
	public void removeFromRoutingTab(String hop) {
		for(String k : routingTab.keySet()) {
			routingTab.get(k).remove(hop);
		}
	}
	
	public HashMap<String, HashSet<String>> getRoutingTab() {
		return routingTab;
	}
	
	public Collection<NTabEntry> getNTabEntries() {
		return nTab.values();
	}

	public gsEnvironmentBlob getRealEnv() {
		return realEnv;
	}

	public Modes getMode() {
		return mode;
	}
	
	
	public boolean isOnFood() {
		return onFood;
	}


	public void setOnFood(boolean onFood) {
		this.onFood = onFood;
	}


	public int getFood() {
		this.mutexF.readLock().lock();
		int val = this.food;
		this.mutexF.readLock().unlock();
		return val;
	}


	public void setFood(int food) {
		this.mutexF.writeLock().lock();
		this.food = food;
		this.mutexF.writeLock().unlock();
	}



	public boolean isExplorationEnabled() {
		return explorationEnabled;
	}
	
	public void addIfNeeded(String id) {
		if(!agentsIds.contains(id)) {
			agentsIds.add(id);
		}
	}

	public void incLastExplo() {
		lastExplo++;
	}

	public void addFoodTrade(String id, int val) {
		nTab.get(id).addFoodTrade(val);
	}
	
	public int getSentFood(String id) {
		return nTab.get(id).getSentFood();
	}
	
	/**
	 * Send an Ad message
	 */
	public void sendAdMsg() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		//Reciever for broadcast
		Iterator<String> it = agentsIds.iterator();
		while(it.hasNext()) {
			String id = it.next();
			if(!id.equals(this.getLocalName())) {
				msg.addReceiver(new AID(id, AID.ISLOCALNAME));
			}
		}
		msg.setProtocol("AD");
		try {
			msg.setContentObject(new AdMsgContent(this.getLocalName(), getPosX(), getPosY(), this.getAndIncSeqNo(), getFood()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sendMessage(msg);
	}
	

	/**
	 * Rebrodcast a received Ad message
	 */
	public void rebroadcastAd(AdMsgContent ad) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		//Reciever for broadcast
		Iterator<String> it = agentsIds.iterator();
		while(it.hasNext()) {
			String id = it.next();
			if(!id.equals(this.getLocalName())) {
				msg.addReceiver(new AID(id, AID.ISLOCALNAME));
			}
		}
		msg.setProtocol("AD");
		try {
			msg.setContentObject(ad);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sendMessage(msg);
	}
	
	/**
	 * Send a coLost message
	 */
	public void sendCoLostMsg(String lostNode) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		//Reciever for broadcast
		Iterator<String> it = agentsIds.iterator();
		while(it.hasNext()) {
			String id = it.next();
			if(!id.equals(this.getLocalName())) {
				msg.addReceiver(new AID(id, AID.ISLOCALNAME));
			}
		}
		msg.setProtocol("CO_LOST");
		try {
			msg.setContentObject(new CoLostMsgContent(this.getLocalName(), lostNode, getAndIncSeqNo()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sendMessage(msg);
	}
	
	/**
	 * Rebroadcat a received coLost message message
	 */
	public void reBroadcastCoLostMsg(CoLostMsgContent coLost) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		//Reciever for broadcast
		Iterator<String> it = agentsIds.iterator();
		while(it.hasNext()) {
			String id = it.next();
			if(!id.equals(this.getLocalName())) {
				msg.addReceiver(new AID(id, AID.ISLOCALNAME));
			}
		}
		msg.setProtocol("CO_LOST");
		try {
			msg.setContentObject(coLost);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sendMessage(msg);
	}
	
	/**
	 * Resend a received coLost message to a specific agent
	 */
	public void reSendCoLostMsgTo(CoLostMsgContent coLost, String receiver) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		msg.addReceiver(new AID(receiver, AID.ISLOCALNAME));
		msg.setProtocol("CO_LOST");
		try {
			msg.setContentObject(coLost);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sendMessage(msg);
	}
	
	/**
	 * print the prefix of the agent concatenated with s
	 * @param s
	 */
	public void print(String s) {
		System.out.println(this.getLocalName()+"\t ----> "+s);
	}
	
	/**
	 * Return a prefix used to identify the agent in console displays
	 */
	public String getPrintPrefix() {
		return this.getLocalName()+"\t ----> ";
	}
	
	/**
	 * Send a food message
	 */
	public void sendFoodMsg(String rec, int foodToSend) {
		addFoodTrade(rec, -foodToSend);
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		//Reciever for broadcast
		msg.addReceiver(new AID(rec, AID.ISLOCALNAME));
		msg.setProtocol("FOOD");
		try {
			msg.setContentObject(new FoodMsgContent(this.getLocalName(), foodToSend, getAndIncSeqNo()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Debug.info(this.getPrintPrefix()+" sent " + foodToSend + " food to "+ rec,1);
		setFood(getFood()-foodToSend);
		this.sendMessage(msg);
	}
	
	/**
	 * Send a state message
	 */
	public void sendStatetMsg() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		//Reciever for broadcast
		Iterator<String> it = agentsIds.iterator();
		while(it.hasNext()) {
			String id = it.next();
			if(!id.equals(this.getLocalName())) {
				msg.addReceiver(new AID(id, AID.ISLOCALNAME));
			}
		}
		msg.setProtocol("STATE");
		try {
			msg.setContentObject(new StateMsgContent(this.getLocalName(), getPosX(), getPosY(), getFood(), this.getAndIncSeqNo()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Debug.info(this.getPrintPrefix()+" sent state to "+ rec,1);
		this.sendMessage(msg);
	}
	
	/**
	 * Send a message, used by every message sending function
	 */
	public void sendMessage(ACLMessage msg){
		Assert.assertNotNull("The sender must have been defined",msg.getSender());

		//filter reachable agents
		Iterator<AID> iter=msg.getAllReceiver();
		int receiverNumber=0;
		while (iter.hasNext()){
			AID receiverAID= iter.next();
			receiverNumber++;
			receiverAID.getLocalName();
			if (!this.realEnv.isReachable(this.getLocalName(),receiverAID.getLocalName())){
				//System.out.println(this.getLocalName()+" removed "+receiverAID.getLocalName());
				iter.remove();
				receiverNumber--;
			}
		}
		
		if (receiverNumber>0){
			super.send(msg);
			
		}//else{
		//	System.out.println(msg.getSender().getLocalName()+"-- No agent within reach --");
		//}
	}
	
	/**
	 * check if neighbours are still close to the agent
	 */
	public void checkContacts() {
		ArrayList<String> toRemove = new ArrayList<String>();
		float delay;
		//max time before considering a connection lost : 2.5 deltaTSync to allow one lost package
		if(TEMPO) {
			delay = 3*(deltaTSync+rounds*TEMPOTIME);
		}else {
			delay = 3*deltaTSync;
		}
		for( NTabEntry n : nTab.values()) {
			LastContactTabEntry c = lastContact.get(n.getId());
			if(new Date().getTime() - c.getDate().getTime()>delay) {
				Debug.info(getPrintPrefix()+" connection lost with "+c.getId()+", remove entries",1);
				if(nTab.containsKey(c.getId())) {
					toRemove.add(n.getId());
				}
				if(routingTab.containsKey(c.getId())) {
					routingTab.get(c.getId()).remove(c.getId());
				}
				this.realEnv.removeConnection(this.getLocalName(), n.getId());
				//to let other agents know that I'm not a valid forwarder to this node anymore
				sendCoLostMsg(c.getId());
				//to try to communicate a knew way to reach me to nodes which lost me with this disconnection
				sendAdMsg();
				
			}
		}
		for(String s : toRemove) {
			nTab.remove(s);
		}
	}
	
	
	/**
	 *Decide how to pick and pick it
	 */
	public Couple<Integer,Map<String,Integer>> decideAndPick() {
		Couple<Node, ReadWriteLock> c = realEnv.getUsableFoodNode(myNode);
		if(c == null) {
			//TODO better
			onFood=false;
			return getDecision(0);
		}
		onFood=true;
		Node n=c.getLeft();
		ReadWriteLock l=c.getRight();
		Debug.info(this.getPrintPrefix()+ " try to acquire writeLock on " + n.getId(),7);
		l.writeLock().lock();
		Debug.info(this.getPrintPrefix()+ " writeLock locked on " + n.getId(),7);
		int availableFood = (int)n.getAttribute("quantity");
		Couple<Integer,Map<String,Integer>> decision = getDecision(availableFood);
		int picked = decision.getLeft().intValue();
		if(picked>0) {
			Debug.info(this.getPrintPrefix()+ " picked "+picked+" on " + n.getId(),7);
			n.setAttribute("quantity", availableFood-picked);
			
			realEnv.updateFoodNode(c);
			
		}
		l.writeLock().unlock();
		Debug.info(this.getPrintPrefix()+ " writeLock released on " + n.getId(),7);
		return decision;
	}

	/**
	 * Decide the way to pick and share the available food
	 */
	public Couple<Integer,Map<String,Integer>> getDecision(int availableFood){
		//Counting how much we need food
		int myFood = getFood();
		int needed = Math.max(foodBound-myFood,0);
		int neighbours_needs = 0;
		int meanFood = myFood;
		int partToKeep=0;
		int partToGive=0;
		Map<String,Integer> giveAway = new HashMap<String,Integer>();
		for(NTabEntry entry: nTab.values()) {
			needed+=Math.max(0,foodBound-entry.getFood());
			neighbours_needs+=Math.max(0,foodBound-entry.getFood());
			entry.setUsed(true);
			meanFood += entry.getFood();
		}
		int pickable = Math.min(availableFood, pickCapacity)
;		int pickup = Math.min(pickable, needed);
		meanFood=(meanFood+pickup)/(nTab.size()+1);
		
		//check food mean
//		meanFoodHistory.add(meanFood);
//		if(meanFoodHistory.size()>=4 && onFood) {
//			boolean enoughFood = false;
//			for(int i=0; i<3; i++) {
//				int target = ((meanFoodHistory.get(i)-foodConso)+((sentFoodHistory.get(i))/nTab.size()))*3/4;
//				Debug.info(this.getPrintPrefix()+"check if mean is rising should be at least "+target+" and is" +meanFoodHistory.get(i+1));
//				if(target<=meanFoodHistory.get(i+1)) {
//					enoughFood=true;
//				}
//				else {
//					Debug.info(this.getPrintPrefix()+"not enough : mean at i "+meanFoodHistory.get(i)+ " sent at i "+ sentFoodHistory.get(i)+" and is" +meanFoodHistory.get(i+1));
//				}
//				
//			}
//			if(!enoughFood&&onFood/*&&upgradedFor>=10*/) {
//				
//				Debug.info(this.getPrintPrefix()+"pick up capacity upgraded "+pickCapacity+" ->" +(pickCapacity+originalPickCapacity));
//				pickCapacity = pickCapacity+originalPickCapacity;
//				upgradedFor=0;
//			}
//			upgradedFor++;
//			meanFoodHistory.remove(0);
//		}
		
		//I'm alone
		if(nTab.isEmpty()||myFood+pickup<=0) {
		}
		//I'm not alone
		else {
			partToKeep=(int)Math.min(Math.min(meanFood, myFood+pickup), (myFood+pickup+1)*propKeep); //Keep at least 1/2 food
			//partToKeep=Math.min(meanFood, myFood+pickup);
			partToGive=myFood+pickup-partToKeep;
			
			if(partToGive!=0) {
				int myNeed =foodBound-myFood;
				
				
				int remaining =availableFood;
				if(neighbours_needs!=0) {
					//splitting food
					for(NTabEntry entry: nTab.values()) {
						giveAway.put(entry.getId(), new Integer(((partToGive*(Math.max(0,foodBound-entry.getFood())))/neighbours_needs)));
						remaining -= partToGive/nTab.size();
					}
					//arbitrarily keeping the remaining quantity for me mouahahahah
				}
			}
			
		}
		if(sentFoodHistory.size()>=3) {
			sentFoodHistory.remove(0);
		}
		sentFoodHistory.add(partToGive);
		Debug.info(this.getPrintPrefix()+" made decision : my food "+food+" available "+availableFood+" neighbours need "+neighbours_needs+" will pick "+pickup+ " and send " +partToGive +" : "+giveAway.toString(),7);
		return(new Couple<Integer,Map<String,Integer>> (new Integer(pickup), giveAway));
	}
	
	/**
	 * Check if an agent is in condition to explore
	 * @return
	 */
	public boolean isAbleToExplore() {
		
		if(lastExplo<tempoExplo) {
			return false;
		}
		//TODO Paremeter
		if(getFood()<foodBound*myPropFood) {
			return false;
		}
		for(NTabEntry entry : nTab.values()) {
			if(entry.getFood()<foodBound*neighboursPropFood) {
				return false;
			}
		}
		if(new Random().nextFloat()<probaExplo){
			return false;
		}
		return true;
	}
	
	/**
	 * Explore the environment : create a new agent and a node for it
	 */
	public void explore() {

		float[] scores = new float[nbDirection];
		float myX = getPosX();
		float myY = getPosY();
		for(int i = 0; i<nbDirection; i++) {
			scores[i]=0;
		}
		for(NTabEntry entry : nTab.values()) {
			//neighbour angle
			float alpha = (float)Math.atan(Math.abs(myY-entry.getPosY())/Math.abs(myX-entry.getPosX()));
			for(int i = 0; i<nbDirection; i++) {
				//dir angle
				float beta = (float)(i*2*Math.PI/(nbDirection));
				scores[i]=scores[i]+(float)Math.min(Math.pow(alpha-beta,2), Math.pow(alpha+2*Math.PI-beta,2));
			}
		}
		int dirMax = 0;
		float scoreMax = scores[0];
		Debug.info(this.getPrintPrefix()+ " score explo vers 0 = " +scores[0]);
		for(int i = 1; i<nbDirection; i++) {
			Debug.info(this.getPrintPrefix()+ " score explo vers "+i+"*2pi/"+nbDirection+" = " +scores[i]);
			if(scores[i]>scoreMax) {
				dirMax = i;
				scoreMax = scores[i];
			}
			
		}
		Random random = new Random();
		float randF = random.nextFloat();
//		if(rand<propDeviation/2) {
//			dirMax=dirMax+1;
//			if(dirMax>=nbDirection) {
//				dirMax=0;
//			}
//		}
		if(randF<probaDeviation) {
			int randI = random.nextInt(nbDirection/8)-nbDirection/16;
			dirMax=randI;
			if(dirMax<0) {
				dirMax=nbDirection+dirMax;
			}
			if(dirMax>=nbDirection) {
				dirMax = dirMax-nbDirection;
			}
		}
		
		float dist = distMin + new Random().nextFloat()*(distMax-distMin);
		float newPosX = myX + (float) Math.cos(dirMax*2*Math.PI/nbDirection)*dist;
		float newPosY = myY + (float) Math.sin(dirMax*2*Math.PI/nbDirection)*dist;
		int agentNum =realEnv.incAndGetNbBlob();
		String agentName="Blob"+agentNum;
		Node n = realEnv.getNewBlobNode(agentNum);
		n.setAttribute("xyz", newPosX, newPosY, 0);
		

		Integer nb_blob=ConfigurationFile.NB_BLOB_AG;
		Float p_source=ConfigurationFile.PROBA_SOURCE;
		Integer rounds =ConfigurationFile.ROUNDS;
		Integer steps=ConfigurationFile.STEPS;
		Integer d_t =ConfigurationFile.DELTA_T;
		Float d_max=ConfigurationFile.D_MAX;
		Float r=ConfigurationFile.R;
		Float mu =ConfigurationFile.MU;
		Float a=ConfigurationFile.A;
		Integer ad_timer=ConfigurationFile.AD_TIMER;
		AbstractBlobAgent.Modes mode = ConfigurationFile.MODE;
		Integer foodBound = ConfigurationFile.FOOD_BOUND;
		Integer pickCapacity = ConfigurationFile.PICK_CAPACITY;
		Integer foodConso = ConfigurationFile.FOOD_CONSO;
		Boolean explorationEnabled = ConfigurationFile.EXPLORATION_ENABLED;
		Integer tempoExplo = ConfigurationFile.TEMPO_EXPLO;
		Float myPropFood=ConfigurationFile.MY_PROP_FOOD;
		Float neighboursPropFood=ConfigurationFile.NEIGHBOURS_PROP_FOOD;
		Float probaExplo=ConfigurationFile.PROBA_EXPLO;
		Integer nbDirection = ConfigurationFile.NB_DIRECTION;
		Integer distMin = ConfigurationFile.DIST_MIN;
		Integer distMax = ConfigurationFile.DIST_MAX;
		Float probaDeviation=ConfigurationFile.PROBA_DEVIATION;
		Float propKeep=ConfigurationFile.PROP_KEEP;

		
		ArrayList<String> agentsId = realEnv.getListWithMyId(agentName);
		//3) If you want to give specific parameters to your agent, add them here
		Object [] entityParameters={agentsId, 
				n,
				p_source, rounds, steps, 
				d_t, d_max, r, mu, a, ad_timer, realEnv, 
				mode, foodBound, pickCapacity,foodConso, explorationEnabled,
				tempoExplo, myPropFood, neighboursPropFood, probaExplo, 
				nbDirection, distMin, distMax, probaDeviation, propKeep};
		
		
		//4) Give the class name of your agent to let the system instantiate it
		AgentController ag=createNewDedaleAgent(realEnv.getC(), agentName, BlobAgent.class.getName(), entityParameters);
		try {
			ag.start();
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		realEnv.indicateEntityPresence(n, null, agentName);
		
	}
	
	/**
	 * create a new agent
	 */
	private static AgentController createNewDedaleAgent(ContainerController initialContainer, String agentName,String className, Object[] additionnalParameters){
		//Object[] objtab=new Object[]{env,agentName};//used to give informations to the agent
		Object[] objtab=AbstractDedaleAgent.loadEntityCaracteristics(agentName,ConfigurationFile.INSTANCE_CONFIGURATION_ENTITIES);
		Object []res2=merge(objtab,additionnalParameters);

		AgentController ag=null;
		try {
			ag = initialContainer.createNewAgent(agentName,className,res2);
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertNotNull(ag);
		//agentList.add(ag);
		System.out.println(agentName+" launched");
		return ag;
	}
	
	private static Object[] merge (Object [] tab1, Object[] tab2){
		Assert.assertNotNull(tab1);
		Object [] res;
		if (tab2!=null){
			res= new Object[tab1.length+tab2.length];
			int i= tab1.length;
			for(i=0;i<tab1.length;i++){
				res[i]=tab1[i];
			}
			for (int ind=0;ind<tab2.length;ind++){
				res[i]=tab2[ind];
				i++;
			}
		}else{
			res=tab1;
		}
		return res;
	}
	
}