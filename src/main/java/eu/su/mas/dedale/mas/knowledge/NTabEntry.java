package eu.su.mas.dedale.mas.knowledge;

public class NTabEntry {
	private String id;
	private float Dij;
	private float qij;
	private float Lij;
	private int food;
	private boolean used;
	private float posX;
	private float posY;
	
	public NTabEntry(String id, float dij, float qij, float lij, int food, float posX, float posY) {
		super();
		this.id = id;
		Dij = dij;
		this.qij = qij;
		Lij = lij;
		this.food = food;
		this.used=false;
		this.posX=posX;
		this.posY=posY;
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
	public int getFood() {
		return food;
	}
	public void setFood(int food) {
		this.food = food;
	}
	public boolean isUsed() {
		return used;
	}
	public void setUsed(boolean used) {
		this.used = used;
	}
	public float getPosX() {
		return posX;
	}
	public void setPosX(float posX) {
		this.posX = posX;
	}
	public float getPosY() {
		return posY;
	}
	public void setPosY(float posY) {
		this.posY = posY;
	}
	
	
}
