package eu.su.mas.dedale.mas.msgcontent;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import eu.su.mas.dedale.mas.knowledge.NTabEntry;

public class ResultsMsgContent extends AbstractMsgContent {
	private float posX,posY;
	private float pressure;
	private int nbEntries;
	private HashMap<String,Float> diameters;

	public ResultsMsgContent(String sender, float posX, float posY, float pressure, Map<String, NTabEntry> nTab, int seqNo) {
		super(sender, null, seqNo);
		this.pressure = pressure;
		this.diameters= new HashMap<String,Float>();
		this.nbEntries=nTab.size();
		for(Map.Entry<String, NTabEntry> entry : nTab.entrySet()) {
			diameters.put(entry.getValue().getId(), entry.getValue().getDij());
		}
		
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

	public int getNbEntries() {
		return nbEntries;
	}

	public HashMap<String, Float> getDiameters() {
		return diameters;
	}
	
}
