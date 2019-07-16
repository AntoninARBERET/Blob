package eu.su.mas.dedale.mas.knowledge;

public class NTabEntry {
	private String id;
	private float pressure;
	private float Dij;
	private float qij;
	private float Lij;
	private boolean used;
	public NTabEntry(String id, float pressure, float dij, float qij, float lij) {
		super();
		this.id = id;
		this.pressure = pressure;
		Dij = dij;
		this.qij = qij;
		Lij = lij;
		used = false;
	}
	public float getPressure() {
		return pressure;
	}
	public void setPressure(float pressure) {
		this.pressure = pressure;
	}
	public float getDij() {
		return Dij;
	}
	public void setDij(float dij) {
		Dij = dij;
	}
	public float getQij() {
		return qij;
	}
	public void setQij(float qij) {
		this.qij = qij;
	}
	public float getLij() {
		return Lij;
	}
	public void setLij(float lij) {
		Lij = lij;
	}
	public String getId() {
		return id;
	}
	
	public boolean isUsed() {
		return used;
	}
	public void setUsed(boolean used) {
		this.used = used;
	}
	public NTabEntry copy() {
		return new NTabEntry( id,  pressure,  Dij,  qij,  Lij);
	}
}
