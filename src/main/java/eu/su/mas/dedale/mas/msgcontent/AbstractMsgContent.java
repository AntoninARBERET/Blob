package eu.su.mas.dedale.mas.msgcontent;

import java.io.Serializable;

/**
 * Abstract class, contains the content needed in every message, implements serializable interface
 * to be sendable
 * @author antoninarberet
 *
 */
public abstract class AbstractMsgContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3929941353677282184L;
	private String sender, reciever;
	private int seqNo;

	public AbstractMsgContent(String sender, String reciever, int seqNo) {
		super();
		this.sender = sender;
		this.reciever = reciever;
		this.seqNo=seqNo;
	}

	public String getSender() {
		return sender;
	}

	public String getReciever() {
		return reciever;
	}
	
	public int getSeqNo() {
		return seqNo;
	}
	
}
