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
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ResultsProcessingBehaviour extends AbstractBlobBehaviour{
	private ResultsMsgContent res;
	public ResultsProcessingBehaviour(AbstractBlobAgent myBlobAgent, ResultsMsgContent res){
		super(myBlobAgent);
		this.res=res;
	}
	
	public void action() {
		myBlobAgent.print("received results from "+res.getSender());
		HashMap<String, LastContactTabEntry> lastCont = myBlobAgent.getLastContact();
		Map<String, NTabEntry> nTab = myBlobAgent.getnTab();
		int seqNo = res.getSeqNo();
		
		//Sender not in nTab yet
		if(!nTab.containsKey(res.getSender())) {
			//Sender not in lastCont yet
			if(!lastCont.containsKey(res.getSender())) {
				lastCont.put(res.getSender(), new LastContactTabEntry(res.getSender(), new Date(), res.getSeqNo(), 0, 0));
			}
			//if results are more recent, update nTab and lastContactTable
			if(lastCont.get(res.getSender()).getResSeqNo()<res.getSeqNo()) {
				float lij = (float) Math.sqrt(Math.pow(myBlobAgent.getPosX()-res.getPosX(), 2)+Math.pow(myBlobAgent.getPosY()-res.getPosY(), 2));
				nTab.put(res.getSender(), new NTabEntry(res.getSender(), res.getPressure(), 0,0,lij));
				myBlobAgent.getRealEnv().addConnection(myBlobAgent.getLocalName(), res.getSender());
				LastContactTabEntry lcEntry = lastCont.get(res.getSender());
				lcEntry.setResSeqNo(seqNo);
				if(lcEntry.getSeqNo()<seqNo) {
					lcEntry.setSeqNo(seqNo);
				}
				lcEntry.setDate(new Date());
			}
			//updating routing table
			HashMap<String, HashSet<String>> rt = myBlobAgent.getRoutingTab();
			if(rt.containsKey(res.getSender())) {
				rt.get(res.getSender()).add(res.getSender());
			}else {
				HashSet<String> hs = new HashSet<String>();
				hs.add(res.getSender());
				rt.put(res.getSender(), hs);
			}
		}
		
		//Sender already in nTab
		else{
			NTabEntry nTabEntry = nTab.get(res.getSender());
			LastContactTabEntry lcEntry = lastCont.get(res.getSender());
			//if results are more recent, update nTab and lastContactTable
			if(lcEntry.getResSeqNo()<seqNo) {
				float lij = (float) Math.sqrt(Math.pow(myBlobAgent.getPosX()-res.getPosX(), 2)+Math.pow(myBlobAgent.getPosY()-res.getPosY(), 2));
				nTabEntry.setLij(lij);
				nTabEntry.setPressure(res.getPressure());
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
