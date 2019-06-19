package eu.su.mas.dedale.mas.msgcontent;

import java.io.Serializable;

public class PingMsgContent extends AbstractMsgContent {
	private float posX,posY;

	public PingMsgContent(String sender, float posX, float posY, int seqNo) {
		super(sender, null, seqNo);
		this.posX = posX;
		this.posY = posY;
	}	
}
