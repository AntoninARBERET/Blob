package eu.su.mas.dedale.mas.agents.observerAgent.guiComponents;

import java.io.Serializable;

import debug.Debug;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * JavaFx frame used to display the backPack's state of each observed agent. 
 * 
 * Design pattern Singleton. One Unique frame for one platform
 * 
 * @author hc
 *
 */
public class GuiTreasureObserver extends Application implements Serializable{

	private static final long serialVersionUID = 7723618977904498563L;
	private TableView<ObservedComponent> table = new TableView<ObservedComponent>();
	private static GuiTreasureObserver instance=null;


	public static void main(String[] args) {
		//launch(args);
		Application.launch(GuiTreasureObserver.class, args);	
	}

	
	public GuiTreasureObserver() {
		instance=this;
	}

	/**
	 * Design pattern singleton<br/>
	 * Get an instance of the Gui to call the associated update methods. If the instance does not already exist, this method creates it
	 */
	public synchronized static GuiTreasureObserver getInstance(){
		//System.out.println("Hello");
		//l.tryLock();
		if(instance==null) {
			// Have to run in a thread because launch doesn't return
			(new GuiTreasureThread()).start();
		}
		while(instance==null){
			//System.out.println("Ici");
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//l.unlock();
		//l.notifyAll();
		return instance;
	}

	/****************************************************
	 * Automatically called when App.launch() is triggered
	 ***************************************************/
	@Override
	public void start(Stage stage) {
		Scene scene = new Scene(new Group());
		stage.setTitle("Dedale's treasures observer");
		stage.setWidth(400);
		stage.setHeight(500);

		// Title
		final Label label = new Label("State of the agents' backpacks");
		label.setFont(new Font("Arial", 18));
		label.setAlignment(Pos.CENTER);

		//table

		table.setEditable(false);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<ObservedComponent,String> firstNameCol = new TableColumn<ObservedComponent,String>("Agent's name");
		firstNameCol.setMaxWidth(1f*Integer.MAX_VALUE * 40);
		firstNameCol.setCellValueFactory(new PropertyValueFactory<ObservedComponent,String>("name"));

		TableColumn<ObservedComponent,Integer> goldCol = new TableColumn<ObservedComponent,Integer>("Gold");
		goldCol.setMaxWidth(1f*Integer.MAX_VALUE * 30);
		goldCol.setCellValueFactory(new PropertyValueFactory<ObservedComponent,Integer>("gold"));

		TableColumn<ObservedComponent,Integer> diamondCol = new TableColumn<ObservedComponent,Integer>("Diamond");
		diamondCol.setMaxWidth(1f*Integer.MAX_VALUE * 30);
		diamondCol.setCellValueFactory(new PropertyValueFactory<ObservedComponent,Integer>("diamond"));

//		ObservableList<ObservedAgent> data =
//				FXCollections.observableArrayList(
//						new ObservedAgent("Agent1", 2, 14),
//						new ObservedAgent("Agent2", 12, 1),
//						new ObservedAgent("Agent3", 56, 78)
//						);

		ObservableList<ObservedComponent> data =FXCollections.observableArrayList();						

		table.setItems(data);
		table.getColumns().addAll(firstNameCol, goldCol, diamondCol);

		//create a container for the label and the table
		final VBox vbox = new VBox();
		vbox.setMinWidth(350);
		vbox.setSpacing(5);
		vbox.setAlignment(Pos.CENTER);
		vbox.setPadding(new Insets(10, 0, 0, 10));

		vbox.getChildren().addAll(label, table);

		//add the box to the scene
		((Group) scene.getRoot()).getChildren().addAll(vbox);

		//and the scene to the stage
		stage.setScene(scene);

		stage.show();

		//tests
		//updateAgentDiamondValue("Agent3", 3);
		//updateAgentGoldValue("Agent4", -7);
		//data.add(new ObservedAgent("Agent12", 123, 456));
		//updateAgentDiamondValue("Agent12", 1);

	}

	/**
	 * Set the new values for gold and diamonds. If the agent does not exist, it creates it.
	 * @param agentName
	 * @param gold
	 * @param diamond
	 * @return true is the agent has been updated, false if created
	 */
	public boolean updateAgentTreasure(String agentName,Integer gold,Integer diamond){
		int index=this.table.getItems().indexOf(new ObservedComponent(agentName));
		if (index<0){ 
			Debug.warning(this.getClass().getName()+"\n updateAgentTreasure()"+ "\n "+agentName+" is not currently observed, the agent is thus added");
			this.table.getItems().add(new ObservedComponent(agentName, gold, diamond));
			return false;
		}else{
			ObservedComponent a=this.table.getItems().get(index);
			a.setGoldValue(gold);
			a.setDiamondValue(diamond);
			this.table.refresh();	
			return true;
		}
	}

	/**
	 * Set the gold to its new value 
	 * @param agentName
	 * @param goldValue
	 * @return true if the value has been updated, false otherwise
	 */
	public boolean updateAgentGoldValue(String agentName,Integer goldValue){
		int index=this.table.getItems().indexOf(new ObservedComponent(agentName));
		if (index<0){ 
			Debug.warning(this.getClass().getName()+"\n updateAgentTreasure()"+ "\n "+agentName+" is not currently observed");
			//this.table.getItems().add(new ObservedAgent(agentName, goldValue,0));
			return false;

		}else{
			System.out.println("DOlg");
			ObservedComponent a=this.table.getItems().get(index);
			a.setGoldValue(goldValue);
			this.table.refresh();	
			return true;
		}

	}

	/**
	 * Set the diamond to its new value
	 * @param agentName
	 * @param diamondValue
	 * @return true if the value has been updated, false otherwise
	 */
	public boolean updateAgentDiamondValue(String agentName,Integer diamondValue){
		int index=this.table.getItems().indexOf(new ObservedComponent(agentName));
		if (index<0){
			Debug.warning(this.getClass().getName()+"\n updateAgentDiamond()"+ "\n "+agentName+" is not currently observed");
			return false;
		}else{
			System.out.println("Diamant");
			ObservedComponent a=this.table.getItems().get(index);
			a.setDiamondValue(diamondValue);
			this.table.refresh();
			return true;
		}
	}


	/**
	 * Add the agent to the observerGui (do nothing if already present)
	 * @param agentName (unique) name of the agent
	 */
	public void addObservedAgent(String agentName){
		this.table.getItems().add(new ObservedComponent(agentName));
	}

	/**
	 * Remove the agent from the observerGui
	 * @param agentName
	 */
	public void removeObservedAgent(String agentName){
		this.table.getItems().remove(new ObservedComponent(agentName));
	}
	
	public String toString(){
		return this.table.getItems().toString();
	}

}