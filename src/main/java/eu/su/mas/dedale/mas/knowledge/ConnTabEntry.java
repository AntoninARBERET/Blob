package eu.su.mas.dedale.mas.knowledge;

public class ConnTabEntry {
	private String id;
	private Relation relation;
	public ConnTabEntry(String id, Relation relation) {
		super();
		this.id = id;
		this.relation = relation;
	}
	public String getId() {
		return id;
	}
	public Relation getRelation() {
		return relation;
	}
}
