package eu.su.mas.dedale.gui;

import java.awt.Component;
import java.util.Iterator;

import javax.swing.JFrame;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.spriteManager.SpriteManager;

import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;


public class oldGsMapEmbedded {

	public static void main (String[] args){
		
		JFrame myJframe =new JFrame("Dedale environment");
		myJframe.setSize(800, 600);
		
		
		Graph graph = createGsGraph();
		//Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.enableAutoLayout();
		// ...
		View view = viewer.addDefaultView(false);   // false indicates "no JFrame".
		
		// ...
		myJframe.add((Component) view);
		myJframe.setVisible(true);
	}

	private static Graph createGsGraph(){

		//color of a node according to its type
		String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under; text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
		String nodeStyle_wumpus= "node.wumpus {"+"fill-color: red;"+"}";
		String nodeStyle_agent= "node.agent {"+"fill-color: forestgreen;"+"}";
		String nodeStyle_treasure="node.treasure {"+"fill-color: yellow;"+"}";
		String nodeStyle_EntryExit="node.exit {"+"fill-color: green;"+"}";
		
		String nodeStyle=defaultNodeStyle+nodeStyle_wumpus+nodeStyle_agent+nodeStyle_treasure+nodeStyle_EntryExit;
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		Graph graph = new SingleGraph("Illustrative example");//generateGraph(true, 30);
		
		Iterator<Node> iter=graph.iterator();
		
		//SingleGraph graph = new SingleGraph("Tutorial 1");
		graph.setAttribute("ui.stylesheet",nodeStyle);
		
		//Viewer viewer = graph.display();
		SpriteManager sman = new SpriteManager(graph);

		// the nodes can be added dynamically.
		graph.addNode("A");
		Node n= graph.getNode("A");
		n.setAttribute("ui.label", "Agent J");	
		n.setAttribute("ui.class", "agent");
		
		Object o=n.getAttribute("ui.label");
		System.out.println("object: "+o.toString());
		
		graph.addNode("B");
		n= graph.getNode("B");
		n.setAttribute("ui.label", "treasure");	
		n.setAttribute("ui.class", "treasure");
		
		graph.addNode("C");	
		n= graph.getNode("C");
		n.setAttribute("ui.label", "wumpus");	
		n.setAttribute("ui.class", "wumpus");
		
		graph.addNode("D");
		n= graph.getNode("D");
		n.setAttribute("ui.label", "The exit");	
		n.setAttribute("ui.class", "exit");
		
		graph.addNode("E");

		//graph structure
		graph.addEdge("AB", "A", "B");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("CA", "C", "A");
		graph.addEdge("DA", "D", "A");
		graph.addEdge("EC", "E", "C");
		
		return graph;
	}
}
