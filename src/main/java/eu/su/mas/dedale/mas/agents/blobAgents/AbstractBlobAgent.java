package eu.su.mas.dedale.mas.agents.blobAgents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;
import org.junit.Assert;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.RandomWalkBehaviour;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedale.mas.agent.behaviours.blob.AdBroadcastingBehaviour;
import eu.su.mas.dedale.mas.agent.behaviours.blob.ReceiveMessageBehaviour;
import eu.su.mas.dedale.mas.agent.behaviours.blob.TestBlobGuiBehaviour;
import eu.su.mas.dedale.mas.agent.behaviours.blob.VerySimpleBehaviour;
import eu.su.mas.dedale.mas.knowledge.ConnTabEntry;
import eu.su.mas.dedale.mas.knowledge.LastContactTabEntry;
import eu.su.mas.dedale.mas.knowledge.NTabEntry;
import eu.su.mas.dedale.mas.knowledge.NTabGrowPhaseEntry;
import eu.su.mas.dedale.mas.knowledge.NfTabEntry;
import eu.su.mas.dedale.mas.knowledge.SocTabEntry;
import eu.su.mas.dedale.mas.msgcontent.AdMsgContent;
import eu.su.mas.dedale.mas.msgcontent.CoLostMsgContent;
import eu.su.mas.dedale.mas.msgcontent.PingMsgContent;
import eu.su.mas.dedale.mas.msgcontent.ResultsMsgContent;
import eu.su.mas.dedale.tools.Debug;
import jade.core.AID;
import jade.core.Agent;
import eu.su.mas.dedale.env.IEnvironment;
import eu.su.mas.dedale.env.gs.gsEnvironmentBlob;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public abstract class  AbstractBlobAgent extends Agent{

	protected static final long serialVersionUID = -2991562876411096907L;
	protected Node myNode;
	protected float posX, posY;
	protected String[] agentsIds;
	protected String nodeType;
	protected float foodVal;
	protected int seqNo;
	protected float pressure;
	protected Map<String, NTabEntry> nTab;
	protected HashMap<String, ConnTabEntry> connTabGrowingPhase;
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
	protected ReentrantReadWriteLock mutexX, mutexY, mutexP;
	protected gsEnvironmentBlob realEnv;
	protected Modes mode;
	public static final boolean TEMPO = true;
	public static final int TEMPOTIME = 1000;
	
	
	public enum Modes{
		RANDOM, STATIC_FOOD; 
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
		this.agentsIds=(String[]) args[2];
		this.myNode=(Node) args[3];
		this.probaSink=((Float) args[4]).floatValue();
		this.probaSource=((Float) args[5]).floatValue();
		this.rounds=((Integer) args[6]).intValue();
		this.steps=((Integer) args[7]).intValue();
		this.deltaPressure=((Float) args[8]).floatValue();
		this.deltaT=((Integer) args[9]).intValue();
		this.deltaTSync=((Integer) args[10]).intValue();
		this.dMax=((Float) args[11]).floatValue();
		this.r=((Float) args[12]).floatValue();
		this.mu=((Float) args[13]).floatValue();
		this.a=((Float) args[14]).floatValue();
		this.adTimer=((Integer) args[15]).intValue();
		this.realEnv=(gsEnvironmentBlob) args[16];
		this.mode=(Modes) args[17];
		
		
		//check values
		if(deltaTSync<deltaT*rounds*steps) {
			Debug.error("deltaTSync should be greater than deltaT*rounds*steps"); 
		}
		
		//initializations
		this.seqNo=0;
		this.pressure=0;
		this.nTab=Collections.synchronizedMap(new HashMap<String, NTabEntry>());
		this.lastContact=new HashMap<String, LastContactTabEntry>();
		this.routingTab = new HashMap<String, HashSet<String>>();
		
		this.mutexX = new ReentrantReadWriteLock();
		this.mutexY = new ReentrantReadWriteLock();
		this.mutexP = new ReentrantReadWriteLock();
	
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		//lb.add(new AdBroadcastingBehaviour(this));
		//lb.add(new AdBroadcastingBehaviour(this));
		
		//tests
		//addBehaviour(new TestBlobGuiBehaviour(this));
		
		//addBehaviour(new startMyBehaviours(this,lb));
		Debug.info(getPrintPrefix()+" mode = " + myNode.getAttribute("food"), 2);
		
		if(mode== Modes.STATIC_FOOD && myNode.getAttribute("food")!=null) {
			pressure=(int)myNode.getAttribute("food");
			Debug.info(getPrintPrefix()+" Pressure at construction = " +pressure, 2);
		}
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


	private synchronized int getAndIncSeqNo() {
		seqNo++;
		return seqNo;
	}


	public float getPressure() {
		this.mutexP.readLock().lock();
		float val = this.pressure;
		this.mutexP.readLock().unlock();
		return val;
	}


	public void setPressure(float pressure) {
		this.mutexP.writeLock().lock();
		this.pressure = pressure;
		this.mutexP.writeLock().unlock();
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


	public HashMap<String, ConnTabEntry> getConnTabGrowingPhase() {
		return connTabGrowingPhase;
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

	public void sendPingMsg() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		//Reciever for broadcast
		for(int i =0; i<agentsIds.length; i++) {
			if(!agentsIds[i].equals(this.getLocalName())) {
				msg.addReceiver(new AID(agentsIds[i], AID.ISLOCALNAME));
			}
		}
		msg.setProtocol("PING");
		try {
			msg.setContentObject(new PingMsgContent(this.getLocalName(), (float)myNode.getAttribute("x"),  (float)myNode.getAttribute("y"), getAndIncSeqNo()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sendMessage(msg);
	}
	
	public void sendAdMsg() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		//Reciever for broadcast
		for(int i =0; i<agentsIds.length; i++) {
			if(!agentsIds[i].equals(this.getLocalName())) {
				msg.addReceiver(new AID(agentsIds[i], AID.ISLOCALNAME));
			}
		}
		msg.setProtocol("AD");
		try {
			msg.setContentObject(new AdMsgContent(this.getLocalName(), getPosX(), getPosY(), getPressure(), this.getAndIncSeqNo()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sendMessage(msg);
	}
	
	public void sendResultsMsg() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		//Reciever for broadcast
		for(int i =0; i<agentsIds.length; i++) {
			if(!agentsIds[i].equals(this.getLocalName())) {
				msg.addReceiver(new AID(agentsIds[i], AID.ISLOCALNAME));
			}
		}
		msg.setProtocol("RESULTS");
		try {
			int currSeqNo = getAndIncSeqNo();
			msg.setContentObject(new ResultsMsgContent(this.getLocalName(), getPosX(), getPosY(), getPressure(),  this.nTab,currSeqNo));
			Debug.info(getPrintPrefix()+"Results " +currSeqNo +" sent at : " + new Date().toString(),1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sendMessage(msg);
	}
	
	public void rebroadcastAd(AdMsgContent ad) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		//Reciever for broadcast
		for(int i =0; i<agentsIds.length; i++) {
			if(!agentsIds[i].equals(this.getLocalName())) {
				msg.addReceiver(new AID(agentsIds[i], AID.ISLOCALNAME));
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
	
	public void sendCoLostMsg(String lostNode) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		//Reciever for broadcast
		for(int i =0; i<agentsIds.length; i++) {
			if(!agentsIds[i].equals(this.getLocalName())) {
				msg.addReceiver(new AID(agentsIds[i], AID.ISLOCALNAME));
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
	
	public void reBroadcastCoLostMsg(CoLostMsgContent coLost) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.getAID());
		//Reciever for broadcast
		for(int i =0; i<agentsIds.length; i++) {
			if(!agentsIds[i].equals(this.getLocalName())) {
				msg.addReceiver(new AID(agentsIds[i], AID.ISLOCALNAME));
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
	
	public void print(String s) {
		System.out.println(this.getLocalName()+"\t ----> "+s);
	}
	
	public String getPrintPrefix() {
		return this.getLocalName()+"\t ----> ";
	}
	
	
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

	
}