package eu.su.mas.dedale.env.gs;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.graphstream.graph.Graph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.algorithm.Dijkstra.Element;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.GridGenerator;
import org.graphstream.graph.Node;
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
public class gsEnvironment implements IEnvironment {


	/***********************************
	 * 
	 *  		ATTRIBUTES
	 * 
	 ************************************/
	//GUI parameters
	private final static String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under; text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
	private final static String nodeStyle_wumpus= "node.wumpus {"+"fill-color: red;"+"}";
	private final static String nodeStyle_agentCollect= "node.agentcollect {"+"fill-color: blue;"+"}";
	private final static String nodeStyle_agentExplo= "node.agentexplo {"+"fill-color: forestgreen ;"+"}";
	private final static String nodeStyle_agentTanker= "node.agenttanker {"+"fill-color: violet;"+"}";
	private final static String nodeStyle_gold= "node.gold {"+"fill-color: yellow;"+"}";
	private final static String nodeStyle_diamonds= "node.diamonds {"+"fill-color: green;"+"}"; //!! diamond without s seems to be a reserved keyword in gs
	private final static String nodeStyle_EntryExit= "node.exit {"+"fill-color: gray;"+"}";
	private final static String nodeStyle_Well= "node.well {"+"fill-color: cyan;"+"}";
	private final static String nodeStyle_Wind= "node.wind {"+"fill-color: pink;"+"}";
	private final static String nodeStyle_Stench= "node.stench {"+"fill-color: orange;"+"}";
	private final static String nodeStyle=defaultNodeStyle+nodeStyle_wumpus+nodeStyle_agentExplo+nodeStyle_agentTanker+nodeStyle_agentCollect+nodeStyle_gold+nodeStyle_diamonds+nodeStyle_EntryExit+nodeStyle_Well+nodeStyle_Wind+nodeStyle_Stench;

	private Graph graph;
	private Viewer viewer;

	private String environmentName;
	


/**
 * 
 */
	public void CreateEnvironment(String topologyConfigurationFilePath, String instanceConfiguration,boolean isGrid, Integer envSize,boolean diamond,boolean gold,boolean well) {
		//	TODO allow the generation of elements on a loaded topology
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		//1)load topology

		if (topologyConfigurationFilePath==null && instanceConfiguration==null){
			//randomlyGenerated environment
			generateGraph(isGrid,envSize);

			//2)addComponents
			addElements(diamond,gold,well);

		}else{
			Assert.assertNotNull("The topology configuration should be given",topologyConfigurationFilePath);
			Assert.assertNotNull("The instance configuration should be given",instanceConfiguration);
		
			loadGraph(topologyConfigurationFilePath);
			loadingMapConfiguration(instanceConfiguration);

		}


		//3) define GUI parameters
		this.graph.setAttribute("ui.stylesheet", nodeStyle);
		this.viewer=this.graph.display();

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
				n.addAttribute(e.getMyEntityType().getName(), entityName);
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
		Iterator<Node> nodeCollection=this.graph.getNodeIterator();
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

		Iterator<Node>iter=n.getNeighborNodeIterator();
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
		Iterator<String>	iter=n.getAttributeKeyIterator();
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
			Integer treasureToPick=n.getAttribute(e.getName());
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

	public boolean isReachable(String senderName, String receiverName, int communicationReach) {
		String senderNodeId = getCurrentPosition(senderName);
		String receiverNodeId = getCurrentPosition(receiverName);

		if (senderNodeId!=null && receiverNodeId!=null){
			Node senderNode=this.graph.getNode(senderNodeId);
			Node receiverNod=this.graph.getNode(receiverNodeId);
			boolean bug=false;
			//System.out.println("Computed Distance from sender:"+senderName+"(node: "+senderNode+") to "+receiverName+" (node: "+receiverNod+"):");

			Dijkstra dijkstra = new Dijkstra(Element.EDGE,null,null);

			// Compute the shortest paths in g from A to all nodes
			dijkstra.init(this.graph);
			dijkstra.setSource(senderNode);
			try{
				dijkstra.compute();
			} catch (Exception e ){
				Debug.info("Bug in GrasphStream's dijkstra compute() : "+e.toString());
				bug=true;
			}
			Double dist;
			if(!bug){
				dist=dijkstra.getPathLength(receiverNod);
			}else{
				//System.err.println("\n Dijkstra");
				//for now if there is a bug, do not communicate
				return false;
			}
			//System.out.println("COmm reach: "+communicationReach);
			//System.out.println("Computed Distance from sender:"+senderName+"(node: "+senderNode+") to "+receiverName+" (node: "+receiverNod+"):"+dist);
			if (dist>communicationReach){					
				return false;
			}
			return true;
		}else{
			return false;
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
	 * 
	 * @param type true creates a grid, Dorogovtsev otherwise
	 * @param size number of iteration, the greater the bigger maze.
	 * @return a new graph
	 */
	private void generateGraph(boolean isGrid,int size){
		graph=new SingleGraph("Randomly generated graph");

		Generator gen;

		if (!isGrid){
			//generate a DorogovtsevMendes environment
			gen= new DorogovtsevMendesGenerator();
		}else{
			//generate a square grid environment
			gen= new GridGenerator();
		}

		gen.addSink(graph);
		gen.begin();
		for(int i=0;i<size;i++){
			gen.nextEvents();
		}
		gen.end();

		//show the node Id on the GUI
		Iterator<Node> iter=graph.getNodeIterator();
		while (iter.hasNext()){
			Node n=iter.next();
			n.addAttribute("ui.label",n.getId());
		}
		//return g;
	}

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

		Iterator<Node> it = graph.getNodeIterator();
		while (it.hasNext()){
			Node n = (Node)it.next();
			//System.out.println(n.getId()+" "+n.getAttribute("label").toString());
			n.addAttribute("ui.label", n.getAttribute("label").toString());
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
			int nbHole=1+(int)Math.round(this.graph.getNodeSet().size()*ElementType.WELL.getOccurrencePercentage());
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
			int nbTreasures=1+(int)Math.round(this.graph.getNodeSet().size()*ElementType.GOLD.getOccurrencePercentage());
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
			int nbTreasures=1+(int)Math.round(this.graph.getNodeSet().size()*ElementType.DIAMOND.getOccurrencePercentage());
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
		Iterator<Node> iter = this.graph.getNodeIterator();
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
			i=n.getAttribute(ElementType.GOLD.getName());
			if (i!=null){
				//if there already is gold, increment
				n.addAttribute(ElementType.GOLD.getName(),value+i);
			}else{
				n.addAttribute(ElementType.GOLD.getName(),value);
			}
			break;
		case DIAMOND:
			i=n.getAttribute(ElementType.DIAMOND.getName());

			if (i!=null){
				//if there already is gold, increment
				n.addAttribute(ElementType.DIAMOND.getName(),value+i);
			}else{
				n.addAttribute(ElementType.DIAMOND.getName(),value);
			}
			break;
		case WELL:
			//for the gui
			//n.addAttribute("ui.label", envComponent.WELL.getName());
			//n.setAttribute("ui.class","well");	
			if (n.hasAttribute(ElementType.DIAMOND.getName()) ||n.hasAttribute(ElementType.GOLD.getName())|| n.hasAttribute(ElementType.WELL.getName())|| n.hasAttribute(EntityType.AGENT_COLLECTOR.getName())|| n.hasAttribute(EntityType.AGENT_EXPLORER.getName())|| n.hasAttribute(EntityType.AGENT_TANKER.getName())|| n.hasAttribute(EntityType.WUMPUS.getName())|| n.hasAttribute(EntityType.WUMPUS_MOVER.getName())){
				Debug.error("Impossible to deploy a Well on a position where another entity or element is. Check your configuration file");
			}else{
				n.addAttribute(ElementType.WELL.getName(),ElementType.WELL.getName());

				//for the other agents (wind,noise,...)
				n2update=findNeighbours(n,elem.getRadius());
				//n.addAttribute(ElementType.WIND.getName(),true);
				//n2update.remove(n);
				for (Node n2:n2update){
					n2.addAttribute(ElementType.WIND.getName(),true);
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

		//1° get the nodes to update
		Set<Node> n2update;
		Integer i;
		//2° update them	
		switch(e.getMyEntityType()){
		case AGENT_COLLECTOR:case AGENT_EXPLORER:case AGENT_TANKER:
			//TODO use indicateElementPresence instead of this line
			n.addAttribute(e.getMyEntityType().getName(), entityName);
			break;
		case  WUMPUS : case WUMPUS_MOVER:
			n.addAttribute(e.getMyEntityType().getName(), entityName);
			n.addAttribute(ElementType.STENCH.getName(),true);
			n2update=findNeighbours(n,e.getDetectionRadius());
			n2update.remove(n);
			for (Node n2:n2update){
				n2.addAttribute(ElementType.STENCH.getName(),true);
				updateNodeRendering(n2);
			}
			break;
		default:
			Debug.error("The entity type does not currently exist; "+entityName+" - Type: "+e.getMyEntityType());
			break;
		}
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
			Iterator<Node> iter=n.getNeighborNodeIterator();
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
		if (n.hasAttribute(EntityType.AGENT_COLLECTOR.getName())){
			n.setAttribute("ui.class", "agentcollect");
			n.setAttribute("ui.label", (String)n.getAttribute(EntityType.AGENT_COLLECTOR.getName()));
		}else {
			if (n.hasAttribute(EntityType.AGENT_EXPLORER.getName())){
				n.setAttribute("ui.class", "agentexplo");
				n.setAttribute("ui.label", (String)n.getAttribute(EntityType.AGENT_EXPLORER.getName()));
			}else {
				if (n.hasAttribute(EntityType.AGENT_TANKER.getName())){
					n.setAttribute("ui.class", "agenttanker");
					n.setAttribute("ui.label", (String)n.getAttribute(EntityType.AGENT_TANKER.getName()));
				} else {
					if (n.hasAttribute(EntityType.WUMPUS.getName())){
						n.setAttribute("ui.label", (String)n.getAttribute(EntityType.WUMPUS.getName()));
						n.setAttribute("ui.class", "wumpus");
					}else{
						//then gold
						if (n.hasAttribute(ElementType.GOLD.getName())){
							n.setAttribute("ui.class","gold");
							n.setAttribute("ui.label", n.getId());
						}else{
							if (n.hasAttribute(ElementType.DIAMOND.getName())){
								n.setAttribute("ui.class","diamonds");
								n.setAttribute("ui.label", n.getId());
							} else{
								//then well
								if (n.hasAttribute(ElementType.WELL.getName())){
									n.setAttribute("ui.class", "well");
									n.setAttribute("ui.label", (String)n.getAttribute(ElementType.WELL.getName()));
								}else {
									//then stench
									if (n.hasAttribute(ElementType.STENCH.getName())){
										n.setAttribute("ui.class", "stench");
										n.setAttribute("ui.label", n.getId());
									}else{
										//then wind
										if (n.hasAttribute(ElementType.WIND.getName())){
											n.setAttribute("ui.class", "wind");
											n.setAttribute("ui.label", n.getId());
										}else{
											//default node
											n.removeAttribute("ui.class");
											n.setAttribute("ui.label", n.getId());
										}
									}
								}
							}	
						}

						//whatever the situation is,as its not an agent, the label become the id of the node
						//n.changeAttribute("ui.label",n.getId());

					}// agent 
					//depending of the remaining attribute, the rendering class changes.
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
		Iterator<Node> iter=this.graph.getNodeIterator();
		System.out.println("Graph content:");
		while (iter.hasNext()){
			Node n=iter.next();
			System.out.println("Node "+n.toString());
			Iterator<String> iter2=n.getAttributeKeyIterator();
			while (iter2.hasNext()){
				String attributeKey=iter2.next();
				System.out.println("Attribute: "+attributeKey+";"+n.getAttribute(attributeKey).toString());
			}
		}
	}
}





