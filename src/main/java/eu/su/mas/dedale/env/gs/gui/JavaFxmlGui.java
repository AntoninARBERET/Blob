package eu.su.mas.dedale.env.gs.gui;


import java.net.URL;
import java.util.concurrent.CountDownLatch;

import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.jfree.chart.labels.SymbolicXYItemLabelGenerator;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.control.*;

public class JavaFxmlGui extends Application {

	protected String styleSheet = "graph {padding: 60px;}";
	protected FxViewPanel gs;
	protected FXMLLoader loader;
	
	public static final CountDownLatch latch = new CountDownLatch(1);
    public static JavaFxmlGui startUpTest = null;

    public static JavaFxmlGui waitForStartUpTest() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return startUpTest;
    }

    public static void setStartUpTest(JavaFxmlGui startUpTest0) {
        startUpTest = startUpTest0;
    }	
	

	public JavaFxmlGui() {
		System.out.println("Init called******************");
		setStartUpTest(this);
	}

	public FXMLLoader getLoad(){
		System.out.println("Im In the loader");
		return this.loader;
	}
	
	@SuppressWarnings("restriction")
	@Override
	public void start(Stage primaryStage) throws Exception {

		//System.out.println("HERE IN START of JAVAFXMLGui");
		String currentDir = System.getProperty("user.dir");
		//System.out.println("Current dir using System:" +currentDir);
		
		URL fxml = new URL("file:"+currentDir+"/resources/gui/DedaleGui-v0.fxml");
		System.out.println(fxml);
		
		//VBox root= FXMLLoader.load(fxml);
		loader= new FXMLLoader(fxml);
		VBox root= loader.load();
		
		System.out.println("loaderToString: "+loader.toString());
		System.out.println("loaderResources: "+loader.getResources());
		System.out.println("loaderController: "+loader.getController());
		
		//VBox root= FXMLLoader.load(fxml);
		//System.out.println(root);
				
		//Node nd= root.lookup("#MainFrame");//working
		//Node nd= root.lookup("#splitpane");//working
		//Node nd= root.lookup("#observeTab");// not working
		//System.out.println(nd);
		
		//works perfectly
//		ObservableList<Node> obsl=root.getChildren();
//		System.out.println(obsl);
//		AnchorPane n= (AnchorPane) obsl.get(1);
//		ObservableList<Node> obsl2=n.getChildren();
//		SplitPane p= (SplitPane) obsl2.get(0);
//		System.out.println(p);
//		ObservableList<Node> obsl3=p.getItems();
//		System.out.println(obsl3);
//		AnchorPane p2= (AnchorPane) obsl3.get(1);
//		FxViewPanel res=truc();
//		System.out.println(res.scaleShapeProperty());
//		p2.getChildren().add(res);
//		res.setScaleShape(true);
//		res.prefWidthProperty().bind(p2.widthProperty());
//		res.prefHeightProperty().bind(p2.heightProperty());

		//delegate the setting of the graph to the controller. Equivalent to the above, works perfectly 
		//MyController m=loader.getController();
		//m.setGraph(this.gs);
		
		//first is Horizontal parameter
		Scene scene = new Scene(root, 1200, 800);

		primaryStage.setTitle("Dedale");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		//System.out.println("END IN START of JAVAFXMLGui");
		latch.countDown();
	}

	public static void main(String[] args) {
		//launch(args);
		Application.launch(JavaFxmlGui.class, args);
		
		
		
	}

	public FxViewPanel testDefaultGraphViewer(){
		MultiGraph graph = new MultiGraph("mg");//createGsGraph();
		FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.enableAutoLayout();
		//FxGraphRenderer renderer = new FxGraphRenderer();

		//FxViewPanel panel = (FxViewPanel)viewer.addDefaultView(false, renderer);


		DorogovtsevMendesGenerator gen = new DorogovtsevMendesGenerator();

		graph.setAttribute("ui.antialias");
		graph.setAttribute("ui.quality");
		graph.setAttribute("ui.stylesheet", styleSheet);

		FxViewPanel panel = (FxViewPanel)viewer.addDefaultView(false, new FxGraphRenderer());

		gen.addSink(graph);
		gen.begin();
		for(int i = 0 ; i < 100 ; i++)
			gen.nextEvents();
		gen.end();
		gen.removeSink(graph);

		return panel;
	}
}

