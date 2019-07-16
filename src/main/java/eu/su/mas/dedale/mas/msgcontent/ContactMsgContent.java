package eu.su.mas.dedale.mas.msgcontent;

import java.io.Serializable;
import java.util.ArrayList;

public class ContactMsgContent extends AbstractMsgContent {
	private int distToRoot;

	public ContactMsgContent(String sender, int distToRoot, int seqNo) {
		super(sender, null, seqNo);
		this.distToRoot=distToRoot;
	}

}
