package eu.su.mas.dedale.mas.agent.behaviours.blob;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class VerySimpleBehaviour extends SimpleBehaviour{
	private boolean finished;
	private int act, don;
	private Agent ag;
	public VerySimpleBehaviour(Agent ag){
		this.finished = false;
		act =0;
		don = 0;
		this.ag=ag;
	}
	
	public void action() {
		System.out.println("Tour de boucle : "+act++);
		ag.doWait(1000);
		if(act>5) {
			this.finished=true;
		}
	}

	@Override
	public boolean done() {
		if(finished) {
			System.out.println("Done : "+don++);

		}
		return finished;
	}
}
