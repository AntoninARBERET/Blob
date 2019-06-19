package eu.su.mas.dedale.mas.knowledge;

public class SocTabEntry {
	private String id;
	private float goodness;
	public SocTabEntry(String id, float goodness) {
		super();
		this.id = id;
		this.goodness = goodness;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public float getGoodness() {
		return goodness;
	}
	public void setGoodness(float goodness) {
		this.goodness = goodness;
	}
	
	@Override
	public String toString() {
		return "SocTabEntry [id=" + id + ", goodness=" + goodness + "]";
	}
	
	
}
