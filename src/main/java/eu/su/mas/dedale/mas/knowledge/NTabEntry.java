package eu.su.mas.dedale.mas.knowledge;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.princ.ConfigurationFile;

/**
 * Entry int the neigbour table of the agent
 * @author antoninarberet
 *
 */
public class NTabEntry {
	private String id;
	private float Dij;
	private float qij;
	private float Lij;
	private int food;
	private boolean used;
	private float posX;
	private float posY;
	private ArrayList<Couple<Date,Integer>> foodHist;
	static final int DELTA_T = ConfigurationFile.DELTA_T;
	
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
		foodHist=new ArrayList<Couple<Date,Integer>>();
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
	public void addFoodTrade(int val) {
		foodHist.add(new Couple<Date,Integer>(new Date(), val));
	}
	
	/**
	 * return recently sent food
	 */
	public int getSentFood() {
		Date d = new Date();
		int sum  =0;
		Iterator<Couple<Date, Integer>> it = foodHist.iterator();
		while(it.hasNext()) {
			Couple<Date, Integer> c = it.next();
			if(d.getTime()-c.getLeft().getTime()>10*DELTA_T) {
				it.remove();
			}else {
				sum+=Math.abs(c.getRight());
			}
		}
		return sum;
	}
}
