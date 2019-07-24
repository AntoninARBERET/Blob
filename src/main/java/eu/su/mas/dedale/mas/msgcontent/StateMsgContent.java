package eu.su.mas.dedale.mas.msgcontent;


public class StateMsgContent extends AbstractMsgContent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2673317524141240199L;
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
