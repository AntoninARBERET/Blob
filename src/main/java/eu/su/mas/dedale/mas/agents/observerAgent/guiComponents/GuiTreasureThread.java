package eu.su.mas.dedale.mas.agents.observerAgent.guiComponents;

import javafx.application.Application;

public class GuiTreasureThread extends Thread{

	public void run(){
		Application.launch(GuiTreasureObserver.class);
	}
}
