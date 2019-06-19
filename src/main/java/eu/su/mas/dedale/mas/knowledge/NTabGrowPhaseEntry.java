package eu.su.mas.dedale.mas.knowledge;

public class NTabGrowPhaseEntry {
	private String id;
	private float angle;
	private float dist;
	
	public NTabGrowPhaseEntry(String id, float angle, float dist) {
		super();
		this.id = id;
		this.angle = angle;
		this.dist = dist;
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getDist() {
		return dist;
	}

	public void setDist(float dist) {
		this.dist = dist;
	}

	public String getId() {
		return id;
	}
	
	
	
}
