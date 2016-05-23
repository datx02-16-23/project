package gui.view;

import java.io.IOException;
import java.util.Collection;

import gui.Controller;
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
    private final SimpleStringProperty allConnected       = new SimpleStringProperty();
    private final Stage                root, parent;
    private final JGroupCommunicator   jgc;

    public ConnectedView (Stage parent, JGroupCommunicator jgc) {
        this.parent = parent;
        this.jgc = jgc;
        FXMLLoader connectedLoader = new FXMLLoader(this.getClass().getResource("/view/ConnectedView.fxml"));
        this.root = new Stage();
        this.root.getIcons().add(new Image(Controller.class.getResourceAsStream("/assets/icon_connected.png")));
        this.root.initModality(Modality.APPLICATION_MODAL);
        this.root.setTitle("Entities View: Channel = \"" + jgc.getChannel() + "\"");
        this.root.initOwner(parent);
        SplitPane p = null;
        try {
            p = connectedLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextArea top = (TextArea) connectedLoader.getNamespace().get("connectedEntities");
        top.textProperty().bind(this.currentlyConnected);
        TextArea bottom = (TextArea) connectedLoader.getNamespace().get("allEntities");
        bottom.textProperty().bind(this.allConnected);
        this.root.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            jgc.listenForMemberInfo(false);
            this.root.close();
        });
        Scene dialogScene = new Scene(p, parent.getWidth() * 0.75, parent.getHeight() * 0.75);
        this.root.setScene(dialogScene);
    }

    public void show () {
        this.jgc.listenForMemberInfo(true);
        StringBuilder sb = new StringBuilder();
        for (String s : this.jgc.allKnownEntities()) {
            sb.append(s + "\n");
        }
        this.allConnected.set(sb.toString());
        // Set size and show
        this.root.setWidth(this.parent.getWidth() * 0.75);
        this.root.setHeight(this.parent.getHeight() * 0.75);
        this.root.show();
    }

    public void update (Collection<String> current, Collection<String> all) {
        StringBuilder sb = new StringBuilder();
        for (String s : current) {
            sb.append(s + "\n");
        }
        this.currentlyConnected.set(sb.toString());
        sb = new StringBuilder();
        for (String s : all) {
            sb.append(s + "\n");
        }
        this.allConnected.set(sb.toString());
    }
}
