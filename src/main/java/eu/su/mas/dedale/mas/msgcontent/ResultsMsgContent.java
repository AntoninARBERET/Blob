package eu.su.mas.dedale.mas.msgcontent;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import eu.su.mas.dedale.mas.knowledge.NTabEntry;

public class ResultsMsgContent extends AbstractMsgContent {
	private float posX,posY;
	private int food;
	private int nbEntries;
	private HashMap<String,Float> diameters;

	public ResultsMsgContent(String sender, float posX, float posY, int food, int seqNo) {
		super(sender, null, seqNo);
		this.food = food;
		
		
	}

	public float getPosX() {
		return posX;
	}

	public float getPosY() {
		return posY;
	}

	public int getFood() {
		return food;
	}

	public int getNbEntries() {
		return nbEntries;
	}

	public HashMap<String, Float> getDiameters() {
		return diameters;
	}
	
}
