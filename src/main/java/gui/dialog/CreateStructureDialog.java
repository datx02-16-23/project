package gui.dialog;

import java.io.IOException;

import assets.Const;
import contract.datastructure.Array;
import contract.datastructure.DataStructure;
import contract.datastructure.IndependentElement;
import contract.datastructure.RawType;
import contract.datastructure.VisualType;
import gui.Controller;
import gui.Main;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CreateStructureDialog {

    private final Stage          parent, root;
    private final ChoiceBox      rawType;
    private final Label          name;
    // Volatile
    private RawType              raw;
    private RawType.AbstractType abs;
    private VisualType           vis;
    private DataStructure        struct;
    private String               identifier;

    public CreateStructureDialog (Stage parent) {
        this.parent = parent;
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/dialog/CreateStructureDialog.fxml"));
        fxmlLoader.setController(this);
        this.root = new Stage();
        this.root.getIcons().add(new Image(Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
        this.root.initModality(Modality.APPLICATION_MODAL);
        this.root.setTitle(Const.PROGRAM_NAME + ": Create Data Structure");
        this.root.initOwner(this.parent);
        GridPane p = null;
        try {
            p = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        this.root.setOnCloseRequest(event -> {
            event.consume();
            this.closeButton();
        });
        /*
         * Raw Type
         */
        this.rawType = (ChoiceBox) fxmlLoader.getNamespace().get("rawType");
        this.rawType.setItems(FXCollections.observableArrayList(RawType.values()));
        this.rawType.getSelectionModel().select(RawType.independentElement);
        this.rawType.setOnAction(event -> {
            this.chooseRawType();
        });
        /*
         * Build.
         */
        this.name = (Label) fxmlLoader.getNamespace().get("name");
        Scene dialogScene = new Scene(p, p.getPrefWidth() - 5, p.getPrefHeight());
        this.root.setScene(dialogScene);
        this.root.setResizable(false);
    }

    private void chooseRawType () {
        this.raw = (RawType) this.rawType.getSelectionModel().getSelectedItem();
    }

    public void closeButton () {
        this.root.close();
    }

    public void okButton () {
        this.createStruct();
        this.root.close();
    }

    private void createStruct () {
        this.raw = (RawType) this.rawType.getSelectionModel().getSelectedItem();
        switch (this.raw) {
        case array:
            this.struct = new Array(this.identifier, this.abs, this.vis, null);
            break;
        case tree:
            this.struct = null;
            Main.console.err("Not supported yet.");
            break;
        case independentElement:
            this.struct = new IndependentElement(this.identifier, this.abs, this.vis, null);
            break;
        }
    }

    /**
     * Show the DataStructure creation dialog.
     *
     * @param identifier
     *            The name of the new structure.
     * @return A new DataStructure. Returns {@code null} if the user cancelled.
     */
    public DataStructure show (String identifier) {
        this.identifier = identifier;
        this.struct = null;
        this.name.setText("Create Variable: \"" + identifier + "\"");
        this.root.showAndWait();
        return this.struct;
    }

    public void allOrphan () {
        // TODO: implement
    }
}
