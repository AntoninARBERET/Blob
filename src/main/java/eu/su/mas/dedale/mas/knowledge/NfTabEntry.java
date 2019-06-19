package eu.su.mas.dedale.mas.knowledge;

public class NfTabEntry {
	private String id;
	private float foodVal;
	private float dx, dy;
	private int dHop;
	private String nextHop;
	
	public NfTabEntry(String id, float foodVal, float dx, float dy, int dHop, String nextHop) {
		super();
		this.id = id;
		this.foodVal = foodVal;
		this.dx = dx;
		this.dy = dy;
		this.dHop = dHop;
		this.nextHop = nextHop;
	}

	public float getFoodVal() {
		return foodVal;
	}

	public void setFoodVal(float foodVal) {
		this.foodVal = foodVal;
	}

	public float getDx() {
		return dx;
	}

	public void setDx(float dx) {
		this.dx = dx;
	}

	public float getDy() {
		return dy;
	}

	public void setDy(float dy) {
		this.dy = dy;
	}

	public int getdHop() {
		return dHop;
	}

	public void setdHop(int dHop) {
		this.dHop = dHop;
	}

	public String getNextHop() {
		return nextHop;
	}

	public void setNextHop(String nextHop) {
		this.nextHop = nextHop;
	}

	public String getId() {
		return id;
	}
	
	
}
