package eu.su.mas.dedale.gui;


import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class DedaleGui extends Application {

	protected String styleSheet = "graph {padding: 60px;}";

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Parent root = FXMLLoader.load(getClass().getResource("/resources/dedaleui.fxml"));
		//FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/gui/dedaleGuiV0.fxml"));
		//Parent root = loader.load();

		// gets instance from FXMLLoader, must be called after load()
		//FXMLDocumentController fdc = loader.getController(); 
		MultiGraph graph = new MultiGraph("mg");//createGsGraph();
		FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.enableAutoLayout();
		//FxGraphRenderer renderer = new FxGraphRenderer();
		
		//FxViewPanel panel = (FxViewPanel)viewer.addDefaultView(false, renderer);
		
		
		DorogovtsevMendesGenerator gen = new DorogovtsevMendesGenerator();

		graph.setAttribute("ui.antialias");
		graph.setAttribute("ui.quality");
		graph.setAttribute("ui.stylesheet", styleSheet);

		viewer.enableAutoLayout();
		FxViewPanel panel = (FxViewPanel)viewer.addDefaultView(false, new FxGraphRenderer());

		gen.addSink(graph);
		gen.begin();
		for(int i = 0 ; i < 100 ; i++)
			gen.nextEvents();
		gen.end();
		gen.removeSink(graph);
		
		
		
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



}
