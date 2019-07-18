package eu.su.mas.dedale.mas.agent.behaviours.blob;

import java.time.Clock;
import java.util.Random;

import dataStructures.tuple.Couple;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.mas.knowledge.LastContactTabEntry;
import eu.su.mas.dedale.mas.knowledge.NTabEntry;
import eu.su.mas.dedale.mas.msgcontent.AdMsgContent;
import eu.su.mas.dedale.mas.msgcontent.PingMsgContent;
import eu.su.mas.dedale.mas.msgcontent.ResultsMsgContent;
import eu.su.mas.dedale.mas.msgcontent.StateMsgContent;
import eu.su.mas.dedale.tools.Debug;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class StateProcessingBehaviour extends AbstractBlobBehaviour{
	private StateMsgContent state;
	public StateProcessingBehaviour(AbstractBlobAgent myBlobAgent, StateMsgContent state){
		super(myBlobAgent);
		this.state=state;
	}
	
	public void action() {
		Debug.info(myBlobAgent.getPrintPrefix()+"Received state "+ state.getSeqNo()+" from "+state.getSender()+" at "+new Date().toString(),1);
		HashMap<String, LastContactTabEntry> lastCont = myBlobAgent.getLastContact();
		Map<String, NTabEntry> nTab = myBlobAgent.getnTab();
		int seqNo = state.getSeqNo();
		
		//Sender not in nTab yet
		if(!nTab.containsKey(state.getSender())) {
			Debug.info(myBlobAgent.getPrintPrefix()+" new contact, state from "+state.getSender(), 5);

			//Sender not in lastCont yet
			if(!lastCont.containsKey(state.getSender())) {
				Debug.info(myBlobAgent.getPrintPrefix()+" create entry in lastcontact for "+state.getSender(), 5);
				lastCont.put(state.getSender(), new LastContactTabEntry(state.getSender(), new Date(), state.getSeqNo(), 0, 0));
			}
			//if results are more recent, update nTab and lastContactTable
			if(lastCont.get(state.getSender()).getResSeqNo()<state.getSeqNo()) {
				Debug.info(myBlobAgent.getPrintPrefix()+" fresh state from unknown "+state.getSender(), 5);
				float lij = (float) Math.sqrt(Math.pow(myBlobAgent.getPosX()-state.getPosX(), 2)+Math.pow(myBlobAgent.getPosY()-state.getPosY(), 2));
				nTab.put(state.getSender(), new NTabEntry(state.getSender(), 1,0,lij, state.getFood()));
				myBlobAgent.getRealEnv().addConnection(myBlobAgent.getLocalName(), state.getSender());
				LastContactTabEntry lcEntry = lastCont.get(state.getSender());
				lcEntry.setResSeqNo(seqNo);
				if(lcEntry.getSeqNo()<seqNo) {
					lcEntry.setSeqNo(seqNo);
				}
				lcEntry.setDate(new Date());
			}
			//updating routing table
			HashMap<String, HashSet<String>> rt = myBlobAgent.getRoutingTab();
			if(rt.containsKey(state.getSender())) {
				rt.get(state.getSender()).add(state.getSender());
			}else {
				HashSet<String> hs = new HashSet<String>();
				hs.add(state.getSender());
				rt.put(state.getSender(), hs);
			}
		}
		
		//Sender already in nTab
		else{
			Debug.info(myBlobAgent.getPrintPrefix()+" knew sender, results from "+state.getSender(), 5);

			NTabEntry nTabEntry = nTab.get(state.getSender());
			LastContactTabEntry lcEntry = lastCont.get(state.getSender());
			//if results are more recent, update nTab and lastContactTable
			if(lcEntry.getResSeqNo()<seqNo) {
				Debug.info(myBlobAgent.getPrintPrefix()+" updating last contact with "+state.getSender()+" after receiving results", 5);
				float lij = (float) Math.sqrt(Math.pow(myBlobAgent.getPosX()-state.getPosX(), 2)+Math.pow(myBlobAgent.getPosY()-state.getPosY(), 2));
				nTabEntry.setLij(lij);
				nTabEntry.setFood(state.getFood());
				nTabEntry.setUsed(false);
				lcEntry.setResSeqNo(seqNo);
				if(lcEntry.getSeqNo()<seqNo) {
					lcEntry.setSeqNo(seqNo);
				}
				lcEntry.setDate(new Date());
			}
		}
		finished=true;
	}

	@Override
	public boolean done() {
		return finished;
	}
}