package eu.su.mas.dedale.env.gs;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.view.Viewer;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.algorithm.Dijkstra.Element;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.GridGenerator;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

import org.junit.Assert;

import dataStructures.tuple.Couple;
import debug.Debug;

import eu.su.mas.dedale.env.ElementType;
import eu.su.mas.dedale.env.EntityCharacteristics;
import eu.su.mas.dedale.env.EntityType;
import eu.su.mas.dedale.env.IEnvironment;
import eu.su.mas.dedale.env.InGameConfigurationFile;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gui.JavaFxmlGui;
import eu.su.mas.dedale.env.gs.gui.MyController;
import eu.su.mas.dedale.mas.knowledge.NTabEntry;
import jade.wrapper.PlatformController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.application.*;
//import eu.su.mas.dedale.princ.ConfigurationFile;



/**
 * This class is an implementation of the IEnvironment using the graphStream library
 * 
 * It currently supports : 
 *  - 3 types of agents (tanker, explorer and collector), 
 *  - 1 wumpus (which generates stench), 
 *  - Well (that generate wind and kill the agents that come in them),
 *  - and two types of treasures (gold and diamonds) 
 * @author hc
 *
 */
public class gsEnvironmentBlob implements IEnvironment {


	/***********************************
	 * 
	 *  		ATTRIBUTES
	 * 
	 ************************************/
	//GUI parameters
	private final static String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under; text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
	private final static String nodeStyle_blob= "node.blobi {"+"fill-color: rgb(245,180,31);"+"}";
	private final static String nodeStyle_oneside= "node.onesided {"+"fill-color: red;"+"}";
	private final static String nodeStyle_sym= "node.symetric {"+"fill-color: green;"+"}";
	private final static String nodeStyle_oor= "node.outofrange {"+"fill-color: black;"+"}";
	private final static String edgeStyle= "edge {"+"fill-color: black;size-mode: dyn-size;"+"}";
	
	

	private final static String nodeStyle=defaultNodeStyle+nodeStyle_blob+nodeStyle_oneside+nodeStyle_sym+nodeStyle_oor+edgeStyle;
	private final static String styleSheet = nodeStyle+"graph {padding: 60px;}";

	private Graph graph;
	private Viewer viewer;

	private String environmentName;
	
	



	/**
	 * 
	 */
	@SuppressWarnings("restriction")
	public void CreateEnvironment(String topologyConfigurationFilePath, String instanceConfiguration,boolean isGrid, Integer envSize,boolean diamond,boolean gold,boolean well) {
		//	TODO allow the generation of elements on a loaded topology
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		//1)load topology

		if (topologyConfigurationFilePath==null && instanceConfiguration==null){
			//randomlyGenerated environment
			generateRandomGraph(envSize);

			//2)addComponents
			addElements(diamond,gold,well);

		}else{
			Assert.assertNotNull("The topology configuration should be given",topologyConfigurationFilePath);
			Assert.assertNotNull("The instance configuration should be given",instanceConfiguration);

			loadGraph(topologyConfigurationFilePath);
			loadingMapConfiguration(instanceConfiguration);

		}


		//3) define GUI parameters
		this.graph.setAttribute("ui.stylesheet", styleSheet);

		//this.graph.setProperty("org.graphstream.ui")
		/** work before javaFx ***/
		//this.graph.display(true);
		//this.viewer=this.graph.display();
		/** end before javaFx**/
		/** with javaFx**/
		new Thread(() -> {
			Application.launch(JavaFxmlGui.class, null);
		}).start();
		
		JavaFxmlGui startUpTest = JavaFxmlGui.waitForStartUpTest();

		//Test 1 : pb, **  Uncaught Exception for agent GK  ***
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// Update UI here.
					FXMLLoader loader= startUpTest.getLoad();
					System.out.println("Loader2: "+loader);
					MyController m=loader.getController();
					System.out.println("controller2: "+m);
					Assert.assertNotNull(m);
					m.setGraph(getJavaFxViewer());
			}
		});

		//printAllNodes();
		this.environmentName="env";
	}


	/**
	 *@param entityName Should be unique
	 *@param e Type of the agent
	 *@param locationId position where to deploy the agent, null if free
	 *
	 * Add the agent entityName of type e on the position locationId
	 */
	public synchronized void deployEntity(String entityName, EntityCharacteristics e,String locationId) {
		if (locationId==null){
			deployEntityFromVoid(entityName,e);
		}else{
			deployEntityFromConfig(entityName,e,locationId);
		}
	}

	private void deployEntityFromVoid(String entityName, EntityCharacteristics e) {
		//An agent with the same name should not be already on the map
		Assert.assertNotNull("A name is required", entityName);
		String nodeId=getCurrentPosition(entityName);
		Assert.assertNull("An entity with the same name is already deployed", nodeId);

		//not existing, random deployment
		Node n=org.graphstream.algorithm.Toolkit.randomNode(this.graph);

		boolean free = isOkToDeployEntityOn(n.getId());
		//TODO Guarantee non infinite loop
		while(!free){
			n=org.graphstream.algorithm.Toolkit.randomNode(this.graph);
			free=isOkToDeployEntityOn(n.getId());
		}

		if (free){
			Debug.info("Entity " + entityName + " - type : "+e+", deployed in " + n.getId());
			indicateEntityPresence(n, e,entityName);
		}else{// no free node
			Debug.error("Impossible to deploy entity " + entityName + ", no free position remaining (max one agent for each node)");
		}
	}

	private void deployEntityFromConfig(String entityName, EntityCharacteristics e, String locationId) {
		//An agent with the same name should not be already on the map
		String nodeId=getCurrentPosition(entityName);
		Assert.assertNull("An entity with the same name is already deployed", nodeId);
		Assert.assertNotNull(e);
		Assert.assertNotNull("A coordinate to deploy the entity on should be given",locationId);

		Node n = this.graph.getNode(locationId);
		if (n == null)	{
			Debug.error("Impossible to deploy entity " + entityName + ", the indicated position does not exist: "+locationId);
		}else{
			boolean free = isOkToDeployEntityOn(n.getId());
			if (free)	{
				System.out.println("Entity " + entityName + " of type : "+e.toString()+", deployed in " + n.getId());
				n.setAttribute(e.getMyEntityType().getName(), entityName);
				updateNodeRendering(n);
			}else {
				Debug.error("Impossible to deploy entity " + entityName + ", the indicated position is not free");
			}
		}
	}



	public synchronized void removeEntity(String entityName,EntityCharacteristics e) {
		String nodeId=this.getCurrentPosition(entityName);
		Assert.assertNotEquals("The entity "+entityName+ "was not found in the environment", null, nodeId);
		Node n=this.graph.getNode(nodeId);
		clearEntityPresence(n,entityName, e);
	}


	/**
	 * 
	 * @param n
	 * @param entityName the name of the entity to remove (currently not used as two agents cannot be on the same location)
	 * @param e
	 */
	private synchronized void clearEntityPresence(Node n, String entityName, EntityCharacteristics e) {

		//1° get the node to update
		Set<Node> n2update=findNeighbours(n,e.getDetectionRadius());

		//2° update them
		switch (e.getMyEntityType()) {
		case WUMPUS: case WUMPUS_MOVER :
			n.removeAttribute(ElementType.STENCH.getName());
			n.removeAttribute(e.getMyEntityType().getName());
			updateNodeRendering(n);
			n2update.remove(n);
			for (Node n2:n2update){
				n2.removeAttribute(ElementType.STENCH.getName());
				updateNodeRendering(n2);
				//if this is not a well or a wind node, change the rendering
			}
			break;
		case AGENT_COLLECTOR : case AGENT_EXPLORER : case AGENT_TANKER:   
			n.removeAttribute(e.getMyEntityType().getName());
			updateNodeRendering(n);
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @param n node to update
	 * @param e element to remove
	 */
	private synchronized void clearElementPresence(Node n, ElementType e){
		n.removeAttribute(e.getName());
		updateNodeRendering(n);
	}

	public synchronized String getCurrentPosition(String entityName) {
		
		Iterator<Node> nodeCollection=this.graph.iterator();
		boolean found=false;
		Node n=null;

		while (!found && nodeCollection.hasNext()){
			n= nodeCollection.next();
			found=n.hasAttribute("ui.label") && entityName.equals(n.getAttribute("ui.label").toString());
		}
		if (found){
			return n.getId();
		}else{
			//Debug.warning("getCurrentPosition - The entity "+entityName +" was not found in the environment.");
			return null;
		}
	}


	public synchronized List<Couple<String, List<Couple<Observation, Integer>>>> observe(String currentPosition, String agentName) {
		List<Couple<String, List<Couple<Observation, Integer>>>> l= new ArrayList<Couple<String, List<Couple<Observation, Integer>>>>();
		Node n=this.graph.getNode(currentPosition);

		if(!n.getAttribute("ui.label").toString().contains(agentName)){
			Debug.error("You can't observe from this position (cheater..)");
		}

		List<Couple<Observation, Integer>> la= getObservations(n,true);
		Couple<String,List<Couple<Observation, Integer>>> c=new Couple<String,List<Couple<Observation, Integer>>>(n.getId(),la);
		//TODO switch to the Couple class in tools
		l.add(c);

		Iterator<Node>iter=n.neighborNodes().iterator();// getNeighborNodeIterator();
		while(iter.hasNext()){
			Node temp=iter.next();
			la= getObservations(temp,false);
			c=new Couple<String,List<Couple<Observation, Integer>>>(temp.getId(),la);	
			l.add(c);
		}

		return l;
	}

	/**
	 * Synchronized method to 
	 * @param n the node to consider
	 * @param onIt true if the agent is in node n, false otherwise
	 * @return the associated observations (at the moment of the access)
	 */
	private synchronized List<Couple<Observation, Integer>> getObservations(Node n,Boolean onIt) {
		Iterator<String>	iter=n.attributeKeys().iterator();//getAttributeKeyIterator();
		List<Couple<Observation, Integer>> l= new ArrayList<Couple<Observation, Integer>>();

		//Iterator on the attributes of node n
		while(iter!=null && iter.hasNext()){
			String attrib=iter.next();
			if (ElementType.WIND.getName().equalsIgnoreCase(attrib)){
				l.add(new Couple<Observation, Integer>(Observation.WIND,null));
			}
			if(onIt && ElementType.GOLD.getName().equals(attrib)){
				//Attribute a=Attribute.TREASURE;
				//a.setValue(n.getAttribute(attrib));
				l.add(new Couple<Observation, Integer>(Observation.GOLD,(Integer)n.getAttribute(attrib)));
			}

			if(onIt && ElementType.DIAMOND.getName().equals(attrib)){
				//Attribute a=Attribute.DIAMONDS;
				//a.setValue(n.getAttribute(attrib));
				l.add(new Couple<Observation, Integer>(Observation.DIAMOND,(Integer)n.getAttribute(attrib)));
			}

			if (ElementType.STENCH.getName().equals(attrib)){
				l.add(new Couple<Observation,Integer>(Observation.STENCH,null));
			}
		}
		return l;
	}


	public synchronized Integer moveTo(String entityName, EntityCharacteristics e, String targetedPosition) {
		String currentPosition = this.getCurrentPosition(entityName);

		if (currentPosition==targetedPosition){
			return 1;
		}else{
			Node current=this.graph.getNode(currentPosition);
			Node target=this.graph.getNode(targetedPosition);

			//System.out.println("Move function");
			//System.out.println("Agent "+ agentName+ ", current node"+currentNodeId+", target: "+targetedNodeId);

			if(current.hasEdgeBetween(target)){
				//if the target is reachable, the move is legit

				//if the target is a well, kill the agent. Otherwise move
				if (target.hasAttribute(ElementType.WELL.getName())){
					//update the state of the original node
					clearEntityPresence(current,entityName,e);
					return -1;	
				}else{
					boolean targetEmpty=isPossibleToMove(targetedPosition);

					if (targetEmpty){
						//move authorized
						clearEntityPresence(current,entityName,e);
						indicateEntityPresence(target, e, entityName);
						return 1;	
					}else{
						//move forbidden due to the presence of another entity
						return 0;
					}	
				}
			}else{
				//The target is not reachable from the current position
				Debug.error("This target is not reachable from the current position, cheater.");
				return 0;
			}
		}
	}


	public synchronized int pick(String agentName,String currentPosition, ElementType e, Integer maxQuantity) {
		//Only pick if maxQuantity>0 and there exist an element of type (diamond or gold)  on the currentPosition

		Assert.assertNotNull(currentPosition);
		Assert.assertNotNull(e);
		Assert.assertTrue(e.getName()==ElementType.DIAMOND.getName() || e.getName()==ElementType.GOLD.getName());
		Assert.assertNotNull(maxQuantity);

		Node n= this.graph.getNode(currentPosition);
		int pickedQuantity=0;
		if (maxQuantity>0 && n.hasAttribute(e.getName())){
			//the agent can grab some of e and there is e on the current position
			Integer treasureToPick=(Integer) n.getAttribute(e.getName());
			if (maxQuantity>=treasureToPick){
				//the treasure is cleared
				pickedQuantity=treasureToPick;
				clearElementPresence(n, e);
			}else{
				//some of the treasure remains
				pickedQuantity=maxQuantity;	
				//update the env and apply the losses
				n.setAttribute(e.getName(),((Double)((treasureToPick-pickedQuantity)*(1-InGameConfigurationFile.PERCENTAGE_TREASURE_LOSS))).intValue());
			}
		}

		return pickedQuantity;
	}

	public synchronized void dropOff(String location, ElementType e, Integer quantity) {
		//allowed to drop	
		indicateElementPresence(this.graph.getNode(location),e,quantity);

	}


	public boolean throwGrenade(String agentName, String targetName) {
		//TODO To implement	
		Debug.error("This method is not yet implemented");
		return false;
	}




	public String getName() {
		Assert.assertNotNull(this.environmentName);
		return this.environmentName;
	}


	/*************************************
	 * 
	 * Private methods
	 * 
	 * 
	 *************************************/

	

	/**
	 * Load a graph from file
	 * @param topologyConfigurationFilePath fullpath to the topology
	 */
	private void loadGraph(String topologyConfigurationFilePath) {

		graph = new SingleGraph("Loaded environment");

		FileSource fs = null;
		try		{
			fs = FileSourceFactory.sourceFor(topologyConfigurationFilePath);
		} catch (IOException e1){
			e1.printStackTrace();
		}

		fs.addSink(graph);
		try	{
			fs.readAll(topologyConfigurationFilePath);
		}catch (IOException e) {
			e.printStackTrace();
		}

		Iterator<Node> it = graph.iterator();
		while (it.hasNext()){
			Node n = (Node)it.next();
			//System.out.println(n.getId()+" "+n.getAttribute("label").toString());
			n.setAttribute("ui.label", n.getAttribute("label").toString());
		}

	}


	/**
	 * Randomly add the required components to the environment based on the configurationFile, the InGameConfigurationFile and the ElemenType class
	 * Currently : well, gold, diamond
	 */
	private void addElements(boolean diamond,boolean gold,boolean well) {
		Random r;

		if (well){
			//wells added
			int nbHole=1+(int)Math.round(this.graph.getNodeCount() *ElementType.WELL.getOccurrencePercentage());//getNodeSet().size()
			for (int i=0;i<nbHole;i++){
				String nodeID=findFreePlace(ElementType.WELL);
				if (nodeID!="-1"){
					Node n= this.graph.getNode(nodeID);
					indicateElementPresence(n,ElementType.WELL,null);
				}
			}
		}

		if (gold){
			//adding treasures
			int nbTreasures=1+(int)Math.round(this.graph.getNodeCount()*ElementType.GOLD.getOccurrencePercentage());//getNodeSet().size()
			r= new Random();
			for(int i=0;i<nbTreasures;i++){
				String nodeID=findFreePlace(ElementType.GOLD);
				if (nodeID!="-1"){
					//createTreasure(nodeID,ElementType.GOLD);
					Node n= this.graph.getNode(nodeID);
					r= new Random();
					indicateElementPresence(n,ElementType.GOLD,1+r.nextInt((Integer)InGameConfigurationFile.MAX_GOLD_VALUE));

				}
			}
		}

		if (diamond){
			//adding treasures
			int nbTreasures=1+(int)Math.round(this.graph.getNodeCount()*ElementType.DIAMOND.getOccurrencePercentage());//getNodeSet().size()
			r= new Random();
			for(int i=0;i<nbTreasures;i++){
				String nodeID=findFreePlace(ElementType.DIAMOND);
				if (nodeID!="-1"){
					Node n= this.graph.getNode(nodeID);
					r= new Random();
					indicateElementPresence(n,ElementType.DIAMOND,1+r.nextInt((Integer)InGameConfigurationFile.MAX_DIAMOND_VALUE));
				}
			}
		}
	}


	/**
	 * 
	 * @param fullPath Path of the file that contains the position of all the elements that should appear on the graph
	 */
	private void loadingMapConfiguration(String fullPath){
		if (fullPath!=null){
			BufferedReader filereader=null;
			try {
				filereader = Files.newBufferedReader(Paths.get(fullPath),StandardCharsets.UTF_8);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			String line = null;
			try {	
				while (filereader!=null && (line = filereader.readLine()) != null) {
					String[]l=line.split(":");
					switch (l[0]) {
					case "mapname":
						System.out.println("Loading configuration for environment "+ l[1]);
						break;
					case "well":
						//addWell(l[1]);
						indicateElementPresence(this.graph.getNode(l[1]),ElementType.WELL,null);
						break;
					case "gold":
						indicateElementPresence(this.graph.getNode(l[1]),ElementType.GOLD,Integer.parseInt(l[2]));
						break;
					case "diamonds":
						//addTreasure(l[1], Integer.parseInt(l[2]),envComponent.DIAMONDS);
						indicateElementPresence(this.graph.getNode(l[1]),ElementType.DIAMOND,Integer.parseInt(l[2]));
						break;
						//					case "wumpus":
						//						deployWumpus(l[1],l[2],Integer.parseInt(l[3]),Integer.parseInt(l[4]));
						//						break;
						//					case "agentExplo":			
						//						deployAgentFromConfig(l[1], l[2],EntityType.AGENT_EXPLORER, Integer.parseInt(l[3]),Integer.parseInt(l[4]));
						//						break;	
						//					case "agentCollect":			
						//						deployAgentFromConfig(l[1], l[2],EntityType.AGENT_COLLECTOR, Integer.parseInt(l[3]),Integer.parseInt(l[4]));
						//						break;	
						//					case "agentTanker":			
						//						deployAgentFromConfig(l[1], l[2],EntityType.AGENT_TANKER, Integer.parseInt(l[3]),Integer.parseInt(l[4]));
						//						break;	
					default:
						System.err.println("Loading a configuration : This type of entry does not yet exist - "+l[0]);
						System.exit(0);
						break;
					}

				}
			} catch (IOException|ArrayIndexOutOfBoundsException e) {
				System.err.println("configurationFile not reachable or malformed");
				e.printStackTrace();
			}
		}
	}

	/**
	 * A node is free is there is no agent nor well on it
	 * @return the nodeId of a free node, -1 if no node is available
	 **/
	private synchronized String findFreePlace(ElementType e)	{
		Random r = new Random();
		Iterator<Node> iter = this.graph.iterator();
		List<String> emptyNode = new ArrayList<String>();
		while (iter.hasNext()){
			Node n = (Node)iter.next();
			if (isOktoDeployElementOn(e, n.getId()))
				emptyNode.add(n.getId());
		}
		if (!emptyNode.isEmpty())
			return (String)emptyNode.get(r.nextInt(emptyNode.size()));
		return "-1";
	}


	/**
	 * Check is the targeted destination is ok in order to deploy an entity on it (no wumpus, agent or well)
	 * 
	 * @param targetedNodeId
	 * @return true is the target is free
	 */

	private boolean isOkToDeployEntityOn(String targetedNodeId){
		Node n = this.graph.getNode(targetedNodeId);
		boolean free = true;
		EntityType [] iter= EntityType.values();
		int i=0;
		while (i<iter.length && free){
			free=!n.hasAttribute(iter[i].getName());
			i++;
		}
		return free;
	}


	/**
	 * Check is the targeted destination is ok to deploy the element depending of its  type
	 * @param e Type of Element to deploy
	 * @param targetedNodeId
	 * @return true if the target is free
	 */
	private boolean isOktoDeployElementOn(ElementType e,String targetedNodeId){
		Node n = this.graph.getNode(targetedNodeId);
		//Iterator<String> iter = n.getAttributeKeyIterator();
		boolean free = true;
		switch (e) {
		case DIAMOND: case GOLD:
			// ok if there is not already a well or an agent on this position
			free = ! (n.hasAttribute(ElementType.WELL.getName()) || n.hasAttribute(EntityType.AGENT_COLLECTOR.getName())|| n.hasAttribute(EntityType.AGENT_EXPLORER.getName())|| n.hasAttribute(EntityType.AGENT_TANKER.getName()) || n.hasAttribute(EntityType.WUMPUS.getName())|| n.hasAttribute(EntityType.WUMPUS_MOVER.getName())) ;
			break;
		case WELL:
			// ok if there is not already a well or treasures or agents on this position
			free = ! (n.hasAttribute(ElementType.WELL.getName()) || n.hasAttribute(ElementType.DIAMOND.getName()) || n.hasAttribute(ElementType.GOLD.getName()) || n.hasAttribute(EntityType.AGENT_COLLECTOR.getName())|| n.hasAttribute(EntityType.AGENT_EXPLORER.getName())|| n.hasAttribute(EntityType.AGENT_TANKER.getName()) || n.hasAttribute(EntityType.WUMPUS.getName())|| n.hasAttribute(EntityType.WUMPUS_MOVER.getName())) ;
			break;
		default:
			break;
		}
		return free;
	}


	/**
	 * 
	 * @param n the node to deploy the element (well,..)
	 * @param elem The element to add on the map
	 * @param value (null if no value related to this element)
	 * 
	 * If the element is already present, the value is added
	 * Examples: indicateElementPresence(n,ElementType.WELL,null);indicateElementPresence(n,ElementType.Gold,42)
	 */
	private void indicateElementPresence(Node n, ElementType elem, Integer value) {

		//1° get the nodes to update
		Set<Node> n2update;
		Integer i;
		//2° update them
		switch (elem) {
		case GOLD :
			i=(Integer) n.getAttribute(ElementType.GOLD.getName());
			if (i!=null){
				//if there already is gold, increment
				n.setAttribute(ElementType.GOLD.getName(),value+i);
			}else{
				n.setAttribute(ElementType.GOLD.getName(),value);
			}
			break;
		case DIAMOND:
			i=(Integer) n.getAttribute(ElementType.DIAMOND.getName());

			if (i!=null){
				//if there already is gold, increment
				n.setAttribute(ElementType.DIAMOND.getName(),value+i);
			}else{
				n.setAttribute(ElementType.DIAMOND.getName(),value);
			}
			break;
		case WELL:
			//for the eu.su.mas.dedale.env.gs.gui
			//n.addAttribute("ui.label", envComponent.WELL.getName());
			//n.setAttribute("ui.class","well");	
			if (n.hasAttribute(ElementType.DIAMOND.getName()) ||n.hasAttribute(ElementType.GOLD.getName())|| n.hasAttribute(ElementType.WELL.getName())|| n.hasAttribute(EntityType.AGENT_COLLECTOR.getName())|| n.hasAttribute(EntityType.AGENT_EXPLORER.getName())|| n.hasAttribute(EntityType.AGENT_TANKER.getName())|| n.hasAttribute(EntityType.WUMPUS.getName())|| n.hasAttribute(EntityType.WUMPUS_MOVER.getName())){
				Debug.error("Impossible to deploy a Well on a position where another entity or element is. Check your configuration file");
			}else{
				n.setAttribute(ElementType.WELL.getName(),ElementType.WELL.getName());

				//for the other agents (wind,noise,...)
				n2update=findNeighbours(n,elem.getRadius());
				//n.addAttribute(ElementType.WIND.getName(),true);
				//n2update.remove(n);
				for (Node n2:n2update){
					n2.setAttribute(ElementType.WIND.getName(),true);
					updateNodeRendering(n2);
					//if its not a well, change the graphic rendering
					//if (!envComponent.WELL.getName().equalsIgnoreCase(n2.getAttribute("ui.label").toString()))
					//	n2.setAttribute("ui.class", "wind");
				}
			}
			break;
		default:
			System.out.println("This element is not yet defined");
			break;
		}
		updateNodeRendering(n);

	}


	private void indicateEntityPresence(Node n,EntityCharacteristics e, String entityName) {
		n.setAttribute("blob", entityName);
		updateNodeRendering(n);
	}



	/**
	 * 
	 * @param n the source node
	 * @param radius the associated radius to search in
	 * @return the list of node who are reachable from n in a given radius, n included
	 */
	private Set<Node> findNeighbours(Node n, int radius) {
		Set<Node>n2update=new java.util.HashSet<Node>();

		//Non-final recursivity
		if (radius==0){
			n2update.add(n);
		}else{
			int temp=radius-1;
			n2update.add(n);
			Iterator<Node> iter=n.neighborNodes().iterator();// getNeighborNodeIterator();
			while(iter.hasNext()){
				n2update.addAll(findNeighbours(iter.next(), temp));
			}
		}
		return n2update;
	}

	/**
	 * This method is called whenever an attribute is added or removed from a node in order to update its rendering
	 * @param n node to update
	 */
	private synchronized void updateNodeRendering(Node n) {

		//first agent
		//		if (n.hasAttribute(EntityType.AGENT.getName())){
		//			n.setAttribute("ui.class", "agent");
		//			n.setAttribute("ui.label", (String)n.getAttribute(Attribute.AGENT.getName()));
		//		}else {
		System.out.println("updaterender");
		if (n.hasAttribute("blobi")){
			n.setAttribute("ui.class", "blobi");
			n.setAttribute("ui.label", "blobi");
		}else {
			if (n.hasAttribute("outofrange")){
				n.setAttribute("ui.class", "outofrange");
				n.setAttribute("ui.label", "outofrange");
			}else {
				if (n.hasAttribute("onesided")){
					n.setAttribute("ui.class", "onesided");
					n.setAttribute("ui.label", "onesided");
				}else{
					if (n.hasAttribute("symetric")){
						n.setAttribute("ui.class", "symetric");
						n.setAttribute("ui.label", "symetric");
					}
				}
			}
		}
	}

	/**
	 * Only check for free regarding an agent move, do not guarantee the safety of the targeted area (well,...)
	 * 
	 * @param targetedNodeId
	 * @return true is the entity can be moved on the target
	 */
	private boolean isPossibleToMove(String targetedNodeId) {

		Node n=this.graph.getNode(targetedNodeId);
		//Iterator<String> iter=n.getAttributeKeyIterator();
		boolean free=!(n.hasAttribute(EntityType.AGENT_COLLECTOR.getName()) || 
				n.hasAttribute(EntityType.AGENT_EXPLORER.getName()) || 
				n.hasAttribute(EntityType.AGENT_TANKER.getName()) || 
				n.hasAttribute(EntityType.WUMPUS.getName()) || 
				n.hasAttribute(EntityType.WUMPUS_MOVER.getName())
				);
		return free;
	}

	/**
	 * Print the nodes and their respective attributes
	 */
	private void printAllNodes(){
		Iterator<Node> iter=this.graph.iterator();
		System.out.println("Graph content:");
		while (iter.hasNext()){
			Node n=iter.next();
			System.out.println("Node "+n.toString());
			Iterator<String> iter2=n.attributeKeys().iterator();// AttributeKeyIterator();
			while (iter2.hasNext()){
				String attributeKey=iter2.next();
				System.out.println("Attribute: "+attributeKey+";"+n.getAttribute(attributeKey).toString());
			}
		}
	}
	
	

	/**
	 * Test javaFx GS rendering
	 * @return JavaFxViewPanel of a random graph
	 */
	private FxViewPanel truc(){
		MultiGraph graph = new MultiGraph("mg");//createGsGraph();
		FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.disableAutoLayout();
		//FxGraphRenderer renderer = new FxGraphRenderer();

		//FxViewPanel panel = (FxViewPanel)viewer.addDefaultView(false, renderer);


		DorogovtsevMendesGenerator gen = new DorogovtsevMendesGenerator();

		gen.addSink(graph);
		gen.begin();
		for(int i = 0 ; i < 100 ; i++)
			gen.nextEvents();
		gen.end();
		gen.removeSink(graph);


		graph.setAttribute("ui.antialias");
		graph.setAttribute("ui.quality");
		graph.setAttribute("ui.stylesheet", styleSheet);


		FxViewPanel panel = (FxViewPanel)viewer.addDefaultView(false, new FxGraphRenderer());



		return panel;
	}

	/**
	 * Test javaFx GS rendering
	 * @return JavaFxViewPanel of the raph
	 */
	private FxViewPanel getJavaFxViewer(){
		
		FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		viewer.disableAutoLayout();
		
		graph.setAttribute("ui.antialias");
		graph.setAttribute("ui.quality");
		
		FxViewPanel panel = (FxViewPanel)viewer.addDefaultView(false, new FxGraphRenderer());
		
		return panel;
	}
	
	
	/*
	 * 
	 * 
	 * 
	 * 
	 * BLOB MODIFS
	 * 
	 * 
	 * 
	 * 
	 * */
	
	float communicationReach = 100;
	int edgeAdded=1;
	
	Map<Couple<String, String>, Node> connections = Collections.synchronizedMap(new HashMap<Couple<String, String>, Node>());
	
	
	public Graph getG() {
		return graph;
	}


	@Override
	public boolean isReachable(String senderName, String receiverName, int communicationReach) {
		System.out.println("Not used in this version of the env, try with a double for communicationReach");
		return false;
	}
	
	/**
	 * This method must be synchronized due tothe way graphStream computes the shortestPath
	 * @param senderName name of the entity willing to send the message
	 * @param receiverName name of the receiver
	 * @param communicationReach number of hops autorised to reach the targer
	 */
	public synchronized boolean isReachable(String senderName, String receiverName) {
		Node senderNode = getBlobAgentNode(senderName);
		Node receiverNode = getBlobAgentNode(receiverName);
		if (senderNode!=null && receiverNode!=null){			
			float sendX=(float)senderNode.getAttribute("x");
			float sendY=(float)senderNode.getAttribute("y");
			float recX=(float)receiverNode.getAttribute("x");
			float recY=(float)senderNode.getAttribute("y");
			double tmp = Math.sqrt(Math.pow(sendX-recX, 2)+Math.pow(sendY-recY, 2));
			if(tmp<=communicationReach){
				return true;
			}
		}
		return false;
		

	}
	
	private Node getBlobAgentNode(String agentId) {
		String nodeId = agentId.substring(4);
		return this.graph.getNode(nodeId);
	}
	
	/**
	 * 
	 * @return a new graph
	 */
	private void generateRandomGraph(int size){
		this.graph=new SingleGraph("Randomly generated graph");
		Layout layout = new SpringBox();
		layout.setForce(0);
	    graph.addSink(layout);
	    layout.addAttributeSink(graph);
		Random r = new Random();
		for(int i=1; i<=size; i++) {
			String nodeId=new Integer(i).toString();
			graph.addNode(nodeId);
			float x = (float)(r.nextInt(100)+r.nextDouble());
			float y = (float)(r.nextInt(100)+r.nextDouble());
			Node n =graph.getNode(nodeId);
			n.setAttribute("ui.class", "blobi");
			n.setAttribute("blobi", "blobi");
			n.setAttribute("x", x);
			n.setAttribute("y", y);
		}
		//show the node Id on the GUI
		Iterator<Node> iter=graph.iterator();
		while (iter.hasNext()){
			Node n=iter.next();
			n.setAttribute("ui.label",n.getId());
		}
		graph.setAttribute("ui.styleSheet", styleSheet);
		//return g;
	}
	
	public boolean addConnection(String ag1, String ag2) {
		Couple<String,String> key = new Couple<String,String>(ag1,ag2);
		Couple<String,String> yek = new Couple<String,String>(ag2,ag1);
		
		if(!connections.containsKey(key) && !connections.containsKey(yek)) {
			if(isReachable(ag1, ag2)){
				System.out.println(ag1);
				Node n1 = getBlobAgentNode(ag1);
				Node n2 = getBlobAgentNode(ag2);
				Node n12=graph.addNode(n1.getId()+"-"+n2.getId());
				float midX = ((float)n1.getAttribute("x")+(float)n2.getAttribute("x"))/2;
				float midY = ((float)n1.getAttribute("y")+(float)n2.getAttribute("y"))/2;
				float w = (float) Math.sqrt(Math.pow(midX-(float)n1.getAttribute("x"),2)+Math.pow(midY-(float)n1.getAttribute("y"),2));
				System.out.println(midX + " "+ midY);
				n12.setAttribute("x", midX);
				n12.setAttribute("y", midY);
				Edge e1 = this.graph.addEdge(new Integer(this.edgeAdded++).toString(),n1.getId(),n1.getId()+"-"+n2.getId());
				Edge e2 = this.graph.addEdge(new Integer(this.edgeAdded++).toString(),n2.getId(),n1.getId()+"-"+n2.getId());
				//e1.setAttribute("layout.weight", w);
				//e2.setAttribute("layout.weight", w);
				n12.setAttribute("ui.class", "onesided");
				connections.put(key, n12);
				return true;
			}
		}else if (connections.containsKey(yek)) {
			Node n21 = connections.get(yek);
			n21.setAttribute("ui.class", "symetric");
			connections.put(key, n21);
		}else {
			return true;
		}
		return false;
	}

	public boolean removeConnection(String n1, String n2) {
		Couple<String,String> key = new Couple<String,String>(n1,n2);
		Couple<String,String> yek = new Couple<String,String>(n2,n1);
		if(!connections.containsKey(key)) {
			return false;
		}
		if(!connections.containsKey(yek)) {
			Node n = connections.get(key);
			graph.removeNode(n);
		}
		else {
			Node n = connections.get(yek);
			n.setAttribute("ui.class", "onesided");
			connections.remove(key);
		}
		return true;
	}
	
	/*public ArrayList<Couple<String, Node>> getConnections(String ag){
		
	}*/
	public float getDist(String ag1, String ag2) {
		Node n1 = getBlobAgentNode(ag1);
		Node n2 = getBlobAgentNode(ag2);
		float d = (float) Math.sqrt(Math.pow((float)n2.getAttribute("x")-(float)n1.getAttribute("x"),2)+Math.pow((float)n2.getAttribute("y")-(float)n1.getAttribute("y"),2));
		return d;
	}
	
	public void updateConnections(String ag) {
		Node myNode = getBlobAgentNode(ag);
		for(Couple<String, String> k : connections.keySet()) {
			boolean isLeft = k.getLeft().equals(ag);;
			boolean isRight = k.getRight().equals(ag);
			String otherAg=null;
			if(isLeft) {
				otherAg=k.getRight();
			}else if(isRight) {
				otherAg=k.getLeft();
			}
			if(otherAg!=null) {
					System.out.println(getDist(ag,otherAg));
					
					if(getDist(ag,otherAg)>communicationReach) {
						connections.get(k).setAttribute("oldclass",connections.get(k).getAttribute("ui.class"));
						connections.get(k).setAttribute("ui.class", "outofrange");
					}else if(connections.get(k).getAttribute("ui.class").equals("outofrange")){
						if(connections.containsKey(new Couple<String,String>(k.getRight(),k.getLeft()))){
							connections.get(k).setAttribute("ui.class", "symetric");
						}else {
							connections.get(k).setAttribute("ui.class", "onesided");
						}
					}
					Node otherNode=getBlobAgentNode(otherAg);
					float midX = ((float)myNode.getAttribute("x")+(float)otherNode.getAttribute("x"))/2;
					float midY = ((float)myNode.getAttribute("y")+(float)otherNode.getAttribute("y"))/2;
					connections.get(k).setAttribute("x", midX);
					connections.get(k).setAttribute("y", midY);
			}
		}
	}
	
}





