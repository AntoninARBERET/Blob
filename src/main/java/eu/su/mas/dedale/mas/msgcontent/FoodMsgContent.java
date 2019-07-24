package eu.su.mas.dedale.mas.msgcontent;


public class FoodMsgContent extends AbstractMsgContent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8784788210014944295L;
	private int food;

	public FoodMsgContent(String sender, int food, int seqNo) {
		super(sender, null, seqNo);
		this.food=food;
	}

	public int getFood() {
		return food;
	}
}
