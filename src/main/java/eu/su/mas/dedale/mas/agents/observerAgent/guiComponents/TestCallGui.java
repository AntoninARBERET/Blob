package eu.su.mas.dedale.mas.agents.observerAgent.guiComponents;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Application;

public class TestCallGui {

	public static void main(String[] args) {

		//Application.launch(GuiTreasureObserver.class, args);
		//Constructor<? extends Application> c = GuiTreasureObserver.getConstructor(); 
		//app = c.newInstance();
		GuiTreasureObserver	a;
		
		Integer l=new Integer(1);
		synchronized(l){
		
		//Lock l= new ReentrantLock();
		a=GuiTreasureObserver.getInstance();
		//try {
			System.out.println("b");
			//l.wait();
		//} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		}
			
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(a==null);
		//Little pause to allow you to follow what is going on
//				try {
//					System.out.println("Press a key to allow to execute its next move");
//					System.in.read();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}

		//System.out.println("yo");
		//System.out.println(a==null);
		//GuiTreasureObserver a=new GuiTreasureObserver();
		// GuiTreasureObserver a=GuiTreasureObserver.getInstance();
		a.updateAgentDiamondValue("Agent3", 104);
		a.addObservedAgent("Agent5");
		a.updateAgentGoldValue("Agent1", 15);
		a.updateAgentTreasure("Agent5", 1, 11);
	}
}
