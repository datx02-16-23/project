package gui.dialog;

import java.io.IOException;
import java.util.Collection;

import assets.Const;
import contract.datastructure.DataStructure;
import gui.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class IdentifierCollisionDialog {

    public static final short KEEP_OLD         = 0;
    public static final short ALWAYS_KEEP_OLD  = 1;
    public static final short CLEAR_OLD        = 3;
    public static final short ALWAYS_CLEAR_OLD = 4;
    private short             answer;
    private final TextField   oldStructs, newStructs;
    private final CheckBox    memory;
    private final Stage       parent, root;

    public IdentifierCollisionDialog (Stage parent) {
        this.parent = parent;
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/dialog/IdentifierCollisionDialog.fxml"));
        fxmlLoader.setController(this);
        this.root = new Stage();
        this.root.getIcons().add(new Image(Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
        this.root.initModality(Modality.APPLICATION_MODAL);
        this.root.setTitle(Const.PROGRAM_NAME + ": Identifier Collision");
        this.root.initOwner(this.parent);
        GridPane p = null;
        try {
            p = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.root.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            this.answer = KEEP_OLD;
            this.root.close();
        });
        fxmlLoader.getNamespace();
        this.oldStructs = (TextField) fxmlLoader.getNamespace().get("oldStructs");
        this.newStructs = (TextField) fxmlLoader.getNamespace().get("newStructs");
        this.memory = (CheckBox) fxmlLoader.getNamespace().get("memory");
        Scene dialogScene = new Scene(p, p.getPrefWidth() - 5, p.getPrefHeight());
        this.root.setScene(dialogScene);
        this.root.setResizable(false);
    }

    /**
     *
     * @param oldStructs
     * @param newStructs
     * @return
     */
    public short show (Collection<DataStructure> oldStructs, Collection<DataStructure> newStructs) {
        this.oldStructs.setText(oldStructs.toString());
        this.newStructs.setText(newStructs.toString());
        this.root.showAndWait();
        return this.answer;
    }

    public void reject_old () {
        if (this.memory.isSelected()) {
            this.answer = ALWAYS_CLEAR_OLD;
        } else {
            this.answer = CLEAR_OLD;
        }
        this.root.close();
    }

    public void keep_old () {
        if (this.memory.isSelected()) {
            this.answer = ALWAYS_KEEP_OLD;
        } else {
            this.answer = KEEP_OLD;
        }
        this.root.close();
    }
}
