package eu.su.mas.dedale.gui;


import java.net.URL;

import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.control.*;

public class JavaFxmlLoading extends Application {

	protected String styleSheet = "graph {padding: 60px;}";

	@SuppressWarnings("restriction")
	@Override
	public void start(Stage primaryStage) throws Exception {

		String currentDir = System.getProperty("user.dir");
		System.out.println("Current dir using System:" +currentDir);

		//Parent root = FXMLLoader.load(getClass().getResource("/resources/dedaleui.fxml"));
		//FXMLLoader.load(getClass().getResource("/resources/dedaleui.fxml"));

		//URL fxml = getClass().getResource("/resources/dedaleui.fxml");
		URL fxml = getClass().getResource("/resources/DedaleGuiV0.fxml");
		VBox root= FXMLLoader.load(fxml);
		//Node nd= root.lookup("#MainFrame");//working
		//Node nd= root.lookup("#splitpane");//working
		Node nd= root.lookup("#observeTab");// not working
		System.out.println(nd);
		ObservableList<Node> obsl=root.getChildren();
		System.out.println(obsl);
		AnchorPane n= (AnchorPane) obsl.get(1);
		ObservableList<Node> obsl2=n.getChildren();
		SplitPane p= (SplitPane) obsl2.get(0);
		System.out.println(p);
		ObservableList<Node> obsl3=p.getItems();
		System.out.println(obsl3);
		AnchorPane p2= (AnchorPane) obsl3.get(1);

		FxViewPanel res=truc();
		System.out.println(res.scaleShapeProperty());

		p2.getChildren().add(res);
		res.setScaleShape(true);
		res.prefWidthProperty().bind(p2.widthProperty());
		res.prefHeightProperty().bind(p2.heightProperty());

		//res.setPrefSize(Region.USE_PREF_SIZE,Region.USE_PREF_SIZE);
		//res.setPrefSize(p.getWidth(),p.getHeight());
		//root.setVgrow(res, Priority.ALWAYS);


		//AnchorPane ap=(AnchorPane) root.lookup("splitpane");
		//System.out.println(ap);
		//ap.getChildren().add(truc());
		// System.out.println(obsl2);

		//first is Horizontal parameter
		Scene scene = new Scene(root, 800, 500);

		primaryStage.setTitle("Dedale");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		//launch(args);
		Application.launch(JavaFxmlLoading.class, args);
	}

	private FxViewPanel truc(){
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

