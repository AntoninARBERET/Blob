package eu.su.mas.dedale.gui;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;

public class BrowseTest extends Application {
	
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage arg0) throws Exception {
        HostServices host = getHostServices();
        host.showDocument("http://google.com");
    }
}