package eu.su.mas.dedale.mas.msgcontent;

import java.io.Serializable;
import java.util.ArrayList;

public class AdMsgContent extends AbstractMsgContent {
	private float posX,posY,pressure;
	private ArrayList<String> forwarders;
	private int nbHops;

	public AdMsgContent(String sender, float posX, float posY, float pressure, int seqNo) {
		super(sender, null, seqNo);
		this.posX = posX;
		this.posY = posY;
		this.pressure=pressure;
		this.forwarders = new ArrayList<String>();
		this.forwarders.add(sender);
		this.nbHops=1;
	}


	public float getPosX() {
		return posX;
	}

	public float getPosY() {
		return posY;
	}	
	
	public float getPressure() {
		return pressure;
	}
	
	public void addForwarder(String id) {
		this.forwarders.add(id);
	}
	
	public ArrayList<String> getForwarders(){
		return forwarders;
	}
	
	public void incNbHops() {
		nbHops++;
	}

	public int getNbHops() {
		return nbHops;
	}
}
