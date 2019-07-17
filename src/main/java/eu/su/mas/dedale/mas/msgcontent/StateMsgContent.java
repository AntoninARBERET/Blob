package eu.su.mas.dedale.mas.msgcontent;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import eu.su.mas.dedale.mas.knowledge.NTabEntry;

public class StateMsgContent extends AbstractMsgContent {
	private float posX,posY;
	private int food;

	public StateMsgContent(String sender, float posX, float posY, int food, int seqNo) {
		super(sender, null, seqNo);
		this.food = food;
		this.posX=posX;
		this.posY=posY;
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

	
}
