package eu.su.mas.dedale.mas.knowledge;

import java.util.Date;
/**
 * Entry in the last contact table of the blobagent
 * @author antoninarberet
 *
 */
public class LastContactTabEntry {
	private String id;
	private Date date;
	private int seqNo, adSeqNo, resSeqNo;
	
	public LastContactTabEntry(String id, Date date, int seqNo, int adSeqNo, int resSeqNo) {
		super();
		this.id = id;
		this.date = date;
		this.seqNo = seqNo;
		this.adSeqNo = adSeqNo;
		this.resSeqNo = resSeqNo;
	}
	
	public String getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}

	public int getAdSeqNo() {
		return adSeqNo;
	}

	public void setAdSeqNo(int adSeqNo) {
		this.adSeqNo = adSeqNo;
	}

	public int getResSeqNo() {
		return resSeqNo;
	}

	public void setResSeqNo(int resSeqNo) {
		this.resSeqNo = resSeqNo;
	}
	
	
	
}
