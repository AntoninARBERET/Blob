package eu.su.mas.dedale.gui;





import eu.su.mas.dedale.mas.agents.observerAgent.guiComponents.ObservedComponent;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

//import javafx.scene.Scene;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class JavaFxtest extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		//Button btn = new Button("Say Hello World");
		//btn.setOnAction((e) -> System.out.println("Hello World !"));

		//StackPane root = new StackPane();
		//root.getChildren().add(btn);
		Group root = new Group();
		Scene scene = new Scene(root, 300, 400);
		scene.setFill(Color.SKYBLUE);
		
		final Label label = new Label("State of the agent's backpacks ");
        label.setFont(new Font("Arial", 20));
        
        
		TableView<ObservedComponent> agentTreasureView = new TableView<>();
		agentTreasureView.setEditable(false);
		agentTreasureView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		//agentTreasureView.setMaxSize(600, 600);
		
		ObservableList<ObservedComponent> agentsObserved = FXCollections.observableArrayList();
		agentTreasureView.setItems(agentsObserved);
		
		TableColumn<ObservedComponent, String> agentNameCol = new TableColumn<>("Agent's name");
		agentNameCol.setCellValueFactory(new PropertyValueFactory<ObservedComponent,String>("name"));
		//agentNameCol.setMaxWidth(300);
		//agentNameCol.setMinWidth(00);
		agentNameCol.setMaxWidth(1f*Integer.MAX_VALUE * 50 );
				
		TableColumn<ObservedComponent, Integer> goldCol = new TableColumn<>("Gold");
		goldCol.setCellValueFactory(new PropertyValueFactory<ObservedComponent,Integer>("gold"));
		goldCol.setMaxWidth(1f*Integer.MAX_VALUE * 20);
		
		TableColumn<ObservedComponent, Integer> diamondCol = new TableColumn<>("Diamond");
		diamondCol.setCellValueFactory(new PropertyValueFactory<ObservedComponent,Integer>("diamond"));
		diamondCol.setMaxWidth(1f*Integer.MAX_VALUE * 30);
		
		agentTreasureView.getColumns().add(agentNameCol);
		agentTreasureView.getColumns().add(goldCol);
		agentTreasureView.getColumns().add(diamondCol);
			
		root.getChildren().add(label);
		root.getChildren().add(agentTreasureView);
		
		primaryStage.setTitle("Dedale's Observer");
		primaryStage.setScene(scene);
		
		agentsObserved.add(new ObservedComponent("Agent1", 2, 14));
		agentsObserved.add(new ObservedComponent("Agent2", 5, 20));
		agentsObserved.add(new ObservedComponent("Agent3", 14, 11));
		
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}


