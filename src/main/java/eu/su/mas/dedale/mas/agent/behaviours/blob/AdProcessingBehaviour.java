package eu.su.mas.dedale.mas.agent.behaviours.blob;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.mas.knowledge.LastContactTabEntry;
import eu.su.mas.dedale.mas.knowledge.NTabEntry;
import eu.su.mas.dedale.mas.msgcontent.AdMsgContent;

/**
 * AdProcessingBehaviour is the first Behaviour used by the BlobAgent is used when an Ad is received by the BlobAgent
 * @author arberet
 *
 */
public class AdProcessingBehaviour extends AbstractBlobBehaviour{
	private static final long serialVersionUID = 1L;
	private AdMsgContent ad;
	
	public AdProcessingBehaviour(AbstractBlobAgent myBlobAgent, AdMsgContent ad){
		super(myBlobAgent);
		this.ad=ad;
	}
	
	public void action() {
		HashMap<String, LastContactTabEntry> lactContacts = myBlobAgent.getLastContact();
		Map<String, NTabEntry> nTab = myBlobAgent.getnTab();
		if(ad.getSender().equals(myBlobAgent.getLocalName())) {
			return;
		}
		myBlobAgent.print("Ad recieved from "+ad.getSender());
		//if the current agent is not in forwarders, add last forwarder to routing table
		if(!ad.getForwarders().contains(myBlobAgent.getLocalName())) {
			myBlobAgent.addToRoutingTab(ad.getSender(), ad.getForwarders().get(ad.getForwarders().size()-1));
		}
		//New contact : add to lastContacts
		if(!lactContacts.containsKey(ad.getSender())) {
			lactContacts.put(ad.getSender(), new LastContactTabEntry(ad.getSender(), new Date(), ad.getSeqNo(), ad.getSeqNo(), 0));
			//float lij = (float) Math.sqrt(Math.pow(myBlobAgent.getPosX()-ad.getPosX(), 2)+Math.pow(myBlobAgent.getPosY()-ad.getPosY(),2));
			//nTab.put(ad.getSender(), new NTabEntry(ad.getSender(), ad.getPressure(), 0, 0, lij) );
			ad.addForwarder(myBlobAgent.getLocalName());
			ad.incNbHops();
			myBlobAgent.rebroadcastAd(ad);
		}
		//Contact already made : update last contacts and nTab if needed
		else{
			LastContactTabEntry entry = lactContacts.get(ad.getSender());
			int seqNo = ad.getSeqNo();
			//if ad bring new information
			if(entry.getAdSeqNo()<seqNo) {
				entry.setAdSeqNo(seqNo);
				if(entry.getSeqNo()<seqNo) {
					entry.setSeqNo(seqNo);
					float lij = (float) Math.sqrt(Math.pow(myBlobAgent.getPosX()-ad.getPosX(), 2)+Math.pow(myBlobAgent.getPosY()-ad.getPosY(),2));
					if(nTab.containsKey(ad.getSender())) {
						nTab.get(ad.getSender()).setLij(lij);
						nTab.get(ad.getSender()).setPressure(ad.getPressure());					
					}
					entry.setDate(new Date());
				}
				ad.addForwarder(myBlobAgent.getLocalName());
				ad.incNbHops();
				myBlobAgent.rebroadcastAd(ad);
			}
		}
		finished=true;
	}


	public boolean done() {
		return finished;
	}
}
