package eu.su.mas.dedale.env.gs;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

import org.junit.Assert;

import dataStructures.tuple.Couple;

import eu.su.mas.dedale.env.EntityCharacteristics;
import eu.su.mas.dedale.env.EntityType;
import eu.su.mas.dedale.env.IEnvironment;
import eu.su.mas.dedale.env.gs.gui.JavaFxmlGui;
import eu.su.mas.dedale.env.gs.gui.MyController;
import eu.su.mas.dedale.mas.agents.blobAgents.AbstractBlobAgent;
import eu.su.mas.dedale.mas.knowledge.NTabEntry;
import eu.su.mas.dedale.tools.Debug;
import jade.wrapper.ContainerController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

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
	private final static String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:dyn-size;text-alignment:under; text-size:14;text-color:black;}";
	private final static String nodeStyle_blob= "node.blobi {"+"fill-mode: dyn-plain; fill-color: rgb(245,180,31), rgb(0,0,0);"+"}";
	private final static String nodeStyle_oneside= "node.onesided {"+"fill-color: red;shape: box;"+"}";
	private final static String nodeStyle_sym= "node.symetric {"+"fill-color: green;shape: box;"+"}";
	private final static String nodeStyle_oor= "node.outofrange {"+"fill-color: black;shape: cross;"+"}";
	private final static String edgeStyle= "edge {"+"fill-mode: dyn-plain;fill-color: rgb(255,248,147), rgb(240,185,0);size-mode: dyn-size;"+"}";
	private final static String nodeStyle_food= "node.food {"+" fill-color: rgba(0,0,0,0); stroke-mode: plain; stroke-color:blue; stroke-width:3;"+"}";

	

	private final static String nodeStyle=defaultNodeStyle+nodeStyle_blob+nodeStyle_oneside+nodeStyle_sym+nodeStyle_oor+edgeStyle+nodeStyle_food;
	private final static String styleSheet = nodeStyle+"graph {padding: 60px;}";

	private Graph graph;
	private FxViewer viewer;

	private String environmentName;
	private ProxyPipe pipe;
	private MyController m;
	
	private BlockingQueue<Couple<Node, ReadWriteLock>> foodList;

	private int nbBlob;
	
	private ArrayList<String> agentsId;
	private ContainerController c;
	/**
	 * 
	 */
	@SuppressWarnings("restriction")
	public void CreateEnvironment(String topologyConfigurationFilePath, String instanceConfiguration,boolean isGrid, Integer envSize,boolean diamond,boolean gold,boolean well) {
		//	TODO allow the generation of elements on a loaded topology
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		nbBlob=0;
		//1)load topology

		if (topologyConfigurationFilePath==null){
			//randomlyGenerated environment
			generateRandomGraph(envSize);



		}else{
			Assert.assertNotNull("The topology configuration should be given",topologyConfigurationFilePath);

			loadGraph(topologyConfigurationFilePath);
			if(instanceConfiguration!=null) {
				//loadingMapConfiguration(instanceConfiguration);
			}

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
					m=loader.getController();
					System.out.println("controller2: "+m);
					Assert.assertNotNull(m);
					m.setGraph(getJavaFxViewer());
					m.setNodeList(graph);
					viewer.enableXYZfeedback(true);
					// Create a pipe coming from the viewer ...
					pipe = viewer.newViewerPipe();
				
					//pipe = viewer.newThreadProxyOnGraphicGraph();

					
					// ... and connect it to the graph
					pipe.addAttributeSink(graph);
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
		foodList=new LinkedBlockingQueue<Couple<Node, ReadWriteLock>>();
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
			
			if(n.getAttribute("type").equals("blobi")) {
				n.setAttribute("ui.class", "blobi");
				n.setAttribute("ui.label",n.getId());

			}
			
			if(n.getAttribute("type").equals("food")) {
				Debug.info("Food node : "+n.getId());
				n.setAttribute("ui.class", "food");
				n.setAttribute("ui.label","Food : "+n.getAttribute("quantity"));
				n.setAttribute("ui.size", /*((int)n.getAttribute("quantity")+10)*/5+" gu");
				foodList.add(new Couple<Node,ReadWriteLock>(n,new ReentrantReadWriteLock()));
			}
			//System.out.println(n.getId()+" "+n.getAttribute("label").toString());
			//n.setAttribute("ui.label", n.getAttribute("label").toString());
			
			graph.setAttribute("ui.styleSheet", styleSheet);
		
			
		}
		
	}


	/**
	 * Randomly add the required components to the environment based on the configurationFile, the InGameConfigurationFile and the ElemenType class
	 * Currently : well, gold, diamond
	 */
	


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
					case "food":
						indicateFoodPresence(this.graph.getNode(l[1]), Integer.valueOf(l[2]));
						break;
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


	/**
	 * 
	 * @param n the node to deploy the element (well,..)
	 * @param elem The element to add on the map
	 * @param value (null if no value related to this element)
	 * 
	 * If the element is already present, the value is added
	 * Examples: indicateElementPresence(n,ElementType.WELL,null);indicateElementPresence(n,ElementType.Gold,42)
	 */


	public synchronized Node getNewBlobNode(int id) {
		Node n = graph.addNode(id+"");
		n.setAttribute("ui.class", "blobi");
		n.setAttribute("ui.label",""+id);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				m.addToPressList(id);
			}
		});
		
		return n;
	}

	public void indicateEntityPresence(Node n,EntityCharacteristics e, String entityName) {
		n.setAttribute("blob", entityName);
		updateNodeRendering(n);
	}

	private void indicateFoodPresence(Node n, int value) {

		Debug.info("Env : food at "+n+" = "+ value,7);
		n.setAttribute("food", value);
		
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
		
		viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
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
	
	float communicationReach = 40;
	int edgeAdded=1;
	
	Map<Couple<String, String>, Node> connections = Collections.synchronizedMap(new HashMap<Couple<String, String>, Node>());
	
	
	public Graph getG() {
		return graph;
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
			float sendX=(float)GraphPosLengthUtils.nodePosition(senderNode)[0];
			float sendY=(float)GraphPosLengthUtils.nodePosition(senderNode)[1];
			float recX=(float)GraphPosLengthUtils.nodePosition(receiverNode)[0];
			float recY=(float)GraphPosLengthUtils.nodePosition(receiverNode)[1];
			
			/*float sendY=(float)senderNode.getAttribute("y");
			float recX=(float)receiverNode.getAttribute("x");
			float recY=(float)receiverNode.getAttribute("y");*/
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
	
	private synchronized int getNewEdgeId() {
		return this.edgeAdded++;
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
			//n.setAttribute("x", x);
			//n.setAttribute("y", y);
			n.setAttribute("xyz",x,y,0);
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
	
	public synchronized boolean addConnection(String ag1, String ag2) {
		Couple<String,String> key = new Couple<String,String>(ag1,ag2);
		Couple<String,String> yek = new Couple<String,String>(ag2,ag1);
		
		if(!getConnections().containsKey(key) && !getConnections().containsKey(yek)) {
			Debug.info("Env update : "+ag1+" and "+ag2+" was not in connections",6);
			if(isReachable(ag1, ag2)){
				Debug.info("Env update : "+ag1+" and "+ag2+" was reachable",6);
				Node n1 = getBlobAgentNode(ag1);
				Node n2 = getBlobAgentNode(ag2);
				Node n12=graph.addNode(n1.getId()+"-"+n2.getId());
				/*float midX = ((float)n1.getAttribute("x")+(float)n2.getAttribute("x"))/2;
				float midY = ((float)n1.getAttribute("y")+(float)n2.getAttribute("y"))/2;*/
				float midX = ((float)GraphPosLengthUtils.nodePosition(n1)[0]+(float)GraphPosLengthUtils.nodePosition(n2)[0])/2;
				float midY = ((float)GraphPosLengthUtils.nodePosition(n1)[1]+(float)GraphPosLengthUtils.nodePosition(n2)[1])/2;
				
				float w = (float) Math.sqrt(Math.pow(midX-(float)GraphPosLengthUtils.nodePosition(n1)[0],2)+Math.pow(midY-(float)GraphPosLengthUtils.nodePosition(n1)[1],2));
				//System.out.println(midX + " "+ midY);
				//n12.setAttribute("x", midX);
				//n12.setAttribute("y", midY);
				n12.setAttribute("xyz", midX, midY, 0);
				Edge e1 = this.graph.addEdge(new Integer(getNewEdgeId()).toString(),n1.getId(),n1.getId()+"-"+n2.getId());
				Edge e2 = this.graph.addEdge(new Integer(getNewEdgeId()).toString(),n2.getId(),n1.getId()+"-"+n2.getId());
				//e1.setAttribute("layout.weight", w);
				//e2.setAttribute("layout.weight", w);
				n12.setAttribute("ui.class", "onesided");
				getConnections().put(key, n12);
				updateConnections(ag1);
				return true;
			}
		}else if (getConnections().containsKey(yek)) {
			Node n21 = getConnections().get(yek);
			n21.setAttribute("ui.class", "symetric");
			getConnections().put(key, n21);
			updateConnections(ag1);
		}else {
			//updateConnections(ag1);
			return true;
		}
		return false;
	}

	public synchronized boolean removeConnection(String n1, String n2) {
		Couple<String,String> key = new Couple<String,String>(n1,n2);
		Couple<String,String> yek = new Couple<String,String>(n2,n1);
		if(!getConnections().containsKey(key)) {
			return false;
		}
		if(!getConnections().containsKey(yek)) {
			Node n = getConnections().get(key);
			graph.removeNode(n);
			getConnections().remove(key);
		}
		else {
			Node n = getConnections().get(yek);
			n.setAttribute("ui.class", "onesided");
			getConnections().remove(key);
		}
		updateConnections(n1);
		return true;
	}
	
	public synchronized Map<Couple<String, String>, Node> getConnections() {
		return connections;
	}
	
	/*public ArrayList<Couple<String, Node>> getConnections(String ag){
		
	}*/
	public float getDist(String ag1, String ag2) {
		Node n1 = getBlobAgentNode(ag1);
		Node n2 = getBlobAgentNode(ag2);
		float d = (float) Math.sqrt(Math.pow((float)GraphPosLengthUtils.nodePosition(n2)[0]-(float)GraphPosLengthUtils.nodePosition(n1)[0],2)+Math.pow((float)GraphPosLengthUtils.nodePosition(n2)[1]-(float)GraphPosLengthUtils.nodePosition(n1)[1],2));
		return d;
	}
	
	public float getDist(Node n1, Node n2) {
		float d = (float) Math.sqrt(Math.pow((float)GraphPosLengthUtils.nodePosition(n2)[0]-(float)GraphPosLengthUtils.nodePosition(n1)[0],2)+Math.pow((float)GraphPosLengthUtils.nodePosition(n2)[1]-(float)GraphPosLengthUtils.nodePosition(n1)[1],2));
		return d;
	}
	
	public Couple<Node, ReadWriteLock> getUsableFoodNode(Node agentNode){
		Couple<Node, ReadWriteLock> min = null;
		float minDist = Float.MAX_VALUE;
		
		for(Couple<Node, ReadWriteLock> c : foodList) {
			float d = getDist(agentNode, c.getLeft());
			
			c.getRight().readLock().lock();
			String rangeStr = ((String)c.getLeft().getAttribute("ui.size")).split(" ")[0];
			c.getRight().readLock().unlock();
			float rangeVal = Float.valueOf(rangeStr)/2;
			if(d<=rangeVal && d<minDist) {
				min = c;
				minDist=d;
			}
		}
		return min;
	}
	
	public void updateFoodNode(Couple<Node, ReadWriteLock> c){
		if((int)c.getLeft().getAttribute("quantity")<=0) {
			foodList.remove(c);
			graph.removeNode(c.getLeft());
		}
		else {
			//c.getLeft().setAttribute("ui.size", (10+(int)c.getLeft().getAttribute("quantity")+" gu"));
			c.getLeft().setAttribute("ui.label","Food : "+c.getLeft().getAttribute("quantity"));
		}
	}
	
	/**
	 * Update connections of the agent, check if neigbours are in communication range
	 * Must be call at each move
	 * @param ag : id of the agent
	 */
	public synchronized void  updateConnections(String ag) {
		Node myNode = getBlobAgentNode(ag);
		for(Couple<String, String> k : getConnections().keySet()) {
			boolean isLeft = k.getLeft().equals(ag);;
			boolean isRight = k.getRight().equals(ag);
			String otherAg=null;
			if(isLeft) {
				otherAg=k.getRight();
			}else if(isRight) {
				otherAg=k.getLeft();
			}
			if(otherAg!=null) {
					
					if(getDist(ag,otherAg)>communicationReach) {
						getConnections().get(k).setAttribute("oldclass",getConnections().get(k).getAttribute("ui.class"));
						getConnections().get(k).setAttribute("ui.class", "outofrange");
					}else if(getConnections().get(k).getAttribute("ui.class").equals("outofrange")){
						if(getConnections().containsKey(new Couple<String,String>(k.getRight(),k.getLeft()))){
							getConnections().get(k).setAttribute("ui.class", "symetric");
						}else {
							getConnections().get(k).setAttribute("ui.class", "onesided");
						}
					}
					Node otherNode=getBlobAgentNode(otherAg);
					float midX = ((float)GraphPosLengthUtils.nodePosition(myNode)[0]+(float)GraphPosLengthUtils.nodePosition(otherNode)[0])/2;
					float midY = ((float)GraphPosLengthUtils.nodePosition(myNode)[1]+(float)GraphPosLengthUtils.nodePosition(otherNode)[1])/2;
					getConnections().get(k).setAttribute("xyz", midX, midY, 0);
			}
		}
	}
	
	public void updateEdgeStyle(Edge e, float d, float dMax){
		float prop=d/dMax;
		if(prop>=1) {
			prop=(float)9.9;
		}
		float sizeMin = 1;
		float sizeMax = 200;
		float size=sizeMin+(sizeMax-sizeMin)*prop;
		e.setAttribute("ui.size", size);
		e.setAttribute("ui.color", /*prop*/(float)1.0);
	}
	
	public synchronized void updateNodeAndEdgesStyle(AbstractBlobAgent ag) {
		Node n = ag.getMyNode();
		
		int food = ag.getFood();
		
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				//TODO change in food
				m.setPressure(n.getId(), food);
			}
		});
		
		
		try {
			if(food>=0) {
				float size = 10 + food/4;
				n.setAttribute("ui.color", (float)0.0);
				n.setAttribute("ui.size", size);
			}else {
				float size = 10-food/4;
				n.setAttribute("ui.color", (float)9.9);
				n.setAttribute("ui.size", size);			
			}
		}catch(Exception e) {
			//TODO remove this disgusting try catch
			e.printStackTrace();
			
		}
		
		
		float dMax = ag.getdMax();
		for(NTabEntry entry : ag.getNTabEntries()) {
			String j = entry.getId();
			Node mid = getConnections().get(new Couple<String,String>(ag.getLocalName(),j));
			if(mid!=null) {
				Edge e = n.getEdgeBetween(mid);
				if(e!=null) {
					updateEdgeStyle(e, entry.getDij(), dMax);
				}
			}
		}
	}
	
	

	public ProxyPipe getPipe() {
		return pipe;
	}
	
	public synchronized int incAndGetNbBlob() {
		nbBlob++;
		return nbBlob;
	}


	public void setAgentsId(ArrayList<String> agentsId) {
		this.agentsId = agentsId;
	}
	
	public synchronized ArrayList<String> getListWithMyId(String id) {
		agentsId.add(id);
		return agentsId;
	}
	
	public void setC(ContainerController c) {
		this.c = c;
	}


	public ContainerController getC() {
		return c;
	}

}





