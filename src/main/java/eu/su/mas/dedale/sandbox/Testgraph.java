package eu.su.mas.dedale.sandbox;
import eu.su.mas.dedale.env.gs.gui.*;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.GraphicGraph;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.gs.gsEnvironmentBlob;
import eu.su.mas.dedale.env.gs.gui.JavaFxmlGui;

public class Testgraph {

	public static void main(String[] args) {
		Couple<String, String> c,d;
		c = new Couple<String,String>("A","B");
		d = new Couple<String,String>("A","B");
		System.out.println(c.equals(d));
		HashMap<Couple<String, String>, String> m = new HashMap<Couple<String, String>, String>();
		m.put(c, "ue");
		System.out.println(m.containsKey(d));

	}
}
