package gui.dialog;

import java.io.IOException;

import assets.Const;
import contract.datastructure.DataStructure;
import contract.datastructure.VisualType;
import gui.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class VisualDialog {

    private final Stage     root;
    private final Label     name;
    private DataStructure   struct;
    private final ChoiceBox visualTypeChoiceBox;
    private boolean         changed;

    public VisualDialog (Stage parent) {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/dialog/VisualDialog.fxml"));
        fxmlLoader.setController(this);
        this.root = new Stage();
        this.root.getIcons().add(new Image(Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
        this.root.initModality(Modality.APPLICATION_MODAL);
        this.root.setTitle(Const.PROGRAM_NAME + ": Choose Visualisation");
        this.root.initOwner(parent);
        GridPane p = null;
        try {
            p = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.root.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            this.closeButton();
        });

        this.visualTypeChoiceBox = (ChoiceBox) fxmlLoader.getNamespace().get("choice");
        for (VisualType vt : VisualType.values()) {
            if (!vt.isClone) {
                this.visualTypeChoiceBox.getItems().add(vt);
            }
        }

        this.name = (Label) fxmlLoader.getNamespace().get("name");
        Scene dialogScene = new Scene(p, p.getPrefWidth(), p.getPrefHeight());
        this.root.setScene(dialogScene);
        this.root.setResizable(false);
    }

    public void closeButton () {
        this.changed = false;
        this.root.close();
    }

    public void okButton () {
        this.struct.setVisual((VisualType) this.visualTypeChoiceBox.getSelectionModel().getSelectedItem());
        this.root.close();
    }

    /**
     * Show the visualisation options dialog for a given structure.
     *
     * @param struct
     *            A DataStructure.
     * @return True if the visualisation options have changed, false otherwise.
     */
    public boolean show (DataStructure struct) {
        this.struct = struct;
        this.name.setText(struct.toString());
        this.root.showAndWait();
        this.visualTypeChoiceBox.getSelectionModel().select(struct.resolveVisual());
        return this.changed;
    }
}
