package eu.su.mas.dedale.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OpenGui {

	public static void main(String args[]){
		System.out.println("Hello");
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI("http://lemonde.fr"));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
}
