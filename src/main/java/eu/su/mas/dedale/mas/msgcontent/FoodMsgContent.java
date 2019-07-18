package eu.su.mas.dedale.mas.msgcontent;

import java.io.Serializable;
import java.util.ArrayList;

public class FoodMsgContent extends AbstractMsgContent {
	private int food;

	public FoodMsgContent(String sender, int food, int seqNo) {
		super(sender, null, seqNo);
		this.food=food;
	}

	public int getFood() {
		return food;
	}
}
