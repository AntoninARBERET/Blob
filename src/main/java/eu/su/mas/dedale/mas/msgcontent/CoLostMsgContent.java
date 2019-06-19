package eu.su.mas.dedale.mas.msgcontent;

import java.io.Serializable;
import java.util.ArrayList;

public class CoLostMsgContent extends AbstractMsgContent {
	private ArrayList<String> forwarders;
	private String lostNode;

	public CoLostMsgContent(String sender, String lostNode, int seqNo) {
		super(sender, null, seqNo);
		this.lostNode = lostNode;
		this.forwarders = new ArrayList<String>();
		this.forwarders.add(sender);
		
	}
	
	public void addForwarder(String id) {
		this.forwarders.add(id);
	}
	
	public ArrayList<String> getForwarders(){
		return forwarders;
	}

	public String getLostNode() {
		return lostNode;
	}
}
