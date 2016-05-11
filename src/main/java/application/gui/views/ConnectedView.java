package application.gui.views;

import java.io.IOException;
import java.util.Collection;

import application.gui.GUI_Controller;
import io.JGroupCommunicator;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConnectedView {

	private final SimpleStringProperty currentlyConnected = new SimpleStringProperty();
	private final SimpleStringProperty allConnected = new SimpleStringProperty();
	private final Stage root, parent;
	private final JGroupCommunicator jgc;

	public ConnectedView(Stage parent, JGroupCommunicator jgc) {
		this.parent = parent;
		this.jgc = jgc;
		FXMLLoader connectedLoader = new FXMLLoader(getClass().getResource("/ConnectedView.fxml"));
		root = new Stage();
		root.getIcons().add(new Image(GUI_Controller.class.getResourceAsStream("/assets/icon_connected.png")));
		root.initModality(Modality.APPLICATION_MODAL);
		root.setTitle("Entities View: Channel = \"" + jgc.getChannel() + "\"");
		root.initOwner(parent);
		SplitPane p = null;
		try {
			p = connectedLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		TextArea top = (TextArea) connectedLoader.getNamespace().get("connectedEntities");
		top.textProperty().bind(currentlyConnected);
		TextArea bottom = (TextArea) connectedLoader.getNamespace().get("allEntities");
		bottom.textProperty().bind(allConnected);
		root.setOnCloseRequest(event -> {
			event.consume(); // Better to do this now than missing it later.
			jgc.listenForMemberInfo(false);
			root.close();
		});
		Scene dialogScene = new Scene(p, parent.getWidth() * 0.75, parent.getHeight() * 0.75);
		root.setScene(dialogScene);
	}

	public void show() {
		jgc.listenForMemberInfo(true);
		StringBuilder sb = new StringBuilder();
		for (String s : jgc.allKnownEntities()) {
			sb.append(s + "\n");
		}
		allConnected.set(sb.toString());
		// Set size and show
		root.setWidth(parent.getWidth() * 0.75);
		root.setHeight(parent.getHeight() * 0.75);
		root.show();
	}

	public void update(Collection<String> current, Collection<String> all) {
		StringBuilder sb = new StringBuilder();
		for (String s : current) {
			sb.append(s + "\n");
		}
		currentlyConnected.set(sb.toString());
		sb = new StringBuilder();
		for (String s : all) {
			sb.append(s + "\n");
		}
		allConnected.set(sb.toString());
	}
}
