package eu.su.mas.dedale.mas.agents.observerAgent.guiComponents;

import java.io.Serializable;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Object used by the gui to store the data
 * @author hc
 *
 */
public class ObservedComponent implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5199406501382796061L;
	private SimpleStringProperty name;
	private SimpleIntegerProperty goldValue;
	private SimpleIntegerProperty diamondValue;

	public ObservedComponent(String name,Integer goldValue,Integer diamondValue){
		setName(name);
		setGoldValue(goldValue);
		setDiamondValue(diamondValue);
	}

	public ObservedComponent(String name){
		setName(name);
		setGoldValue(0);
		setDiamondValue(0);
	}

	public final void setName(String name){
		if (this.name == null) {
			this.name = new SimpleStringProperty();
		}
		this.name.set(name);
	}

	public final void setGoldValue(Integer goldValue){
		if (this.goldValue == null) {
			this.goldValue = new SimpleIntegerProperty();
		}
		this.goldValue.set(goldValue);
	}

	public final void setDiamondValue(Integer diamondValue){
		if (this.diamondValue == null) {
			this.diamondValue = new SimpleIntegerProperty();
		}
		this.diamondValue.set(diamondValue);
	}

	public String getName(){
		return this.name.get();
	}

	public Integer getGold(){
		return this.goldValue.get();
	}

	public Integer getDiamond(){
		return this.diamondValue.get();
	}

	@Override
	public String toString(){
		return this.getName()+" - Gold: "+this.getGold()+"; Diamond: "+this.getDiamond()+"\n";
	}

//	public SimpleStringProperty  nameProperty() {
//        return this.name;
//    }
//
//    public SimpleIntegerProperty goldValueProperty() {
//        return goldValue;
//    }
//
//    public SimpleIntegerProperty diamondValueProperty() {
//        return diamondValue;
//    }
    
	/**
	 * An Observed agent is equal to another if the name is identical (Case not considered);
	 */
	@Override
	public boolean equals(Object a){
		return (a!=null && (a instanceof ObservedComponent) && ((ObservedComponent)a).getName().equalsIgnoreCase(this.getName()));
	}

	@Override
	public int hashCode(){
		return this.getName().length();

	}

}
