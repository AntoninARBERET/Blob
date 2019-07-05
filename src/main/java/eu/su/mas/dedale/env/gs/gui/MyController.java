package eu.su.mas.dedale.env.gs.gui;


import javafx.application.*;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.fx_viewer.FxViewPanel;

import javafx.fxml.FXML;
import java.awt.Desktop;

public class MyController {

	// When user click on myButton
	// this method will be called.
	@FXML private MenuItem exit;
	@FXML private MenuItem about;
	@FXML private MenuItem help;
	
	@FXML private AnchorPane right;
	
	@FXML private ListView listNode;
	
	@FXML private Label pressureLabel;
	
	private float[] pressList;

	@FXML
	private void handleExitAction(ActionEvent event) {
		System.out.println("exiting");
		System.exit(0);
		//Platform.exit();
	}

	@FXML
	public void dedaleHelp(ActionEvent event){
		System.out.println("Dedale site");
		openUrl("https://dedale.gitlab.io/");
		System.out.println("Post");
	}

	@FXML
	public void dedaleAbout(ActionEvent event){
		System.out.println("About Dedale");
		openUrl("https://dedale.gitlab.io/page/about/");
	}

	/**
	 * Open a browser tab with the uri given in parameter. Launch a new thread to be javafx compliant.
	 * @param uri
	 */
	private void openUrl(String uri) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			new Thread(() -> {
				try {
					Desktop.getDesktop().browse(new URI(uri));
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				}
			}).start();
		}
	}

	/**
	 * Trying to add the graph ref to the scene
	 * @param truc
	 */
	@SuppressWarnings("restriction")
	public synchronized void setGraph(FxViewPanel truc) {
		System.out.println(truc.scaleShapeProperty());
		right.getChildren().add(truc);
		truc.setScaleShape(true);
		truc.prefWidthProperty().bind(right.widthProperty());
		truc.prefHeightProperty().bind(right.heightProperty());		
	}
	
	//TODO
	public synchronized void setNodeList(Graph g) {
		Iterator<Node> it = g.iterator();
		while(it.hasNext()){
			Node n = it.next();
			listNode.getItems().add("Node "+ n.getId() +"\t Pressure = "+ 0);
		}	
		pressList = new float[listNode.getItems().size()];
		for(int i = 0; i<pressList.length; i++) {
			pressList[i]=0;
		}
		/*System.out.println(truc.scaleShapeProperty());
		right.getChildren().add(truc);
		truc.setScaleShape(true);
		truc.prefWidthProperty().bind(right.widthProperty());
		truc.prefHeightProperty().bind(right.heightProperty());*/
		
	}
	
	public synchronized void setPressure(String id, float pressure) {
		listNode.getItems().set(Integer.valueOf(id)-1, "Node "+ id +"\t Pressure = "+ pressure);
		pressList[Integer.valueOf(id)-1]=pressure;
		float tot = 0;
		for(int i = 0; i<pressList.length; i++) {
			tot=tot+pressList[i];
		}
		pressureLabel.setText("Total pressure = " +tot);
		
	}

}