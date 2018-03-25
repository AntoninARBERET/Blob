package eu.su.mas.dedale.gui;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import eu.su.mas.dedale.mas.agents.observerAgent.guiComponents.ObservedComponent;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * This class is an observable list that will store the data displayed by the Observer agent. 
 * Any change in the data object will be reflected on the Gui.
 * This class stands on the JavaFx ObservableList
 * @author hc
 *
 */
public class ObservedAgentsList implements ObservableList<ObservedComponent>{

	private ObservableList<ObservedComponent> data;
		
	@Override
	public int size() {
		return data.size();
	}

	@Override
	public boolean isEmpty() {		
		return data.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return data.contains(o);
	}

	@Override
	public Iterator<ObservedComponent> iterator() {
		return data.iterator();
	}

	@Override
	public Object[] toArray() {
		return data.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return data.toArray(a);
	}

	@Override
	public boolean add(ObservedComponent e) {
		return data.add(e);
	}

	@Override
	public boolean remove(Object o) {
		
		return data.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return data.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends ObservedComponent> c) {
		return data.addAll(c);
	}
	
	public boolean setGoldTo(String agentName,Integer goldValue){
		ObservedComponent a=new ObservedComponent(agentName, goldValue,0);
		int index =this.data.indexOf(a);
		if (index!=-1){
			ObservedComponent temp=this.data.get(index);
			temp.setGoldValue(goldValue);
			//alternative 
			//a.setDiamondValue(temp.getDiamond());
			//data.add(a);
			return true;
		}else{
			return false;
		}
	}
	
	public boolean setDiamondTo(String agentName,Integer diamondValue){
		ObservedComponent a=new ObservedComponent(agentName, 0,diamondValue);
		int index =this.data.indexOf(a);
		if (index!=-1){
			ObservedComponent temp=this.data.get(index);
			temp.setDiamondValue(diamondValue);
			//alternative 
			//a.setGoldValue(temp.getGold());
			//data.add(a);
			return true;
		}else{
			return false;
		}
	}
	

	@Override
	public boolean addAll(int index, Collection<? extends ObservedComponent> c) {
		return data.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return data.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return data.retainAll(c);
	}

	@Override
	public void clear() {
		data.clear();
		
	}

	@Override
	public ObservedComponent get(int index) {
		return data.get(index);
	}

	@Override
	public ObservedComponent set(int index, ObservedComponent element) {
		return data.set(index, element);
	}

	@Override
	public void add(int index, ObservedComponent element) {
		this.data.add(index, element);
	}

	@Override
	public ObservedComponent remove(int index) {
		return data.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return data.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return data.lastIndexOf(o);
	}

	@Override
	public ListIterator<ObservedComponent> listIterator() {
		return data.listIterator();
	}

	@Override
	public ListIterator<ObservedComponent> listIterator(int index) {
		return data.listIterator(index);
	}

	@Override
	public List<ObservedComponent> subList(int fromIndex, int toIndex) {
		return data.subList(fromIndex, toIndex);
	}

	@Override
	public void addListener(InvalidationListener arg0) {
		data.addListener(arg0);	
	}

	@Override
	public void removeListener(InvalidationListener arg0) {
		data.removeListener(arg0);
	}

	@Override
	public boolean addAll(ObservedComponent... arg0) {
		return this.data.addAll(arg0);
	}

	@Override
	public void addListener(ListChangeListener<? super ObservedComponent> arg0) {
		this.data.addListener(arg0);
	}

	@Override
	public void remove(int arg0, int arg1) {
		data.remove(arg0, arg1);
		
	}

	@Override
	public boolean removeAll(ObservedComponent... arg0) {
		return data.removeAll(arg0);
	}

	@Override
	public void removeListener(ListChangeListener<? super ObservedComponent> arg0) {
		data.removeListener(arg0);	
	}

	@Override
	public boolean retainAll(ObservedComponent... arg0) {
		return data.retainAll(arg0);
	}

	@Override
	public boolean setAll(ObservedComponent... arg0) {
		return data.setAll(arg0);
	}

	@Override
	public boolean setAll(Collection<? extends ObservedComponent> arg0) {
		return data.setAll(arg0);
	}

}
