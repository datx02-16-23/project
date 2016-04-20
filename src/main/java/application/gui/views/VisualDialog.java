package application.gui.views;

import java.io.IOException;

import application.assets.Strings;
import application.gui.GUI_Controller;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import wrapper.datastructures.DataStructure;

@SuppressWarnings({"rawtypes", "unchecked"})
public class VisualDialog {

    private final Stage     parent, root;
    private final ChoiceBox choice;
    private final Spinner   children;
    private final Label     name;
    private DataStructure   struct;
    private boolean         changed;

    public VisualDialog (Stage parent){
        this.parent = parent;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/DataStructureDialog.fxml"));
        fxmlLoader.setController(this);
        root = new Stage();
        root.getIcons().add(new Image(GUI_Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
        root.initModality(Modality.APPLICATION_MODAL);
        root.setTitle(Strings.PROJECT_NAME + ": Choose Visualisation");
        root.initOwner(this.parent);
        GridPane p = null;
        try {
            p = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        root.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            closeButton();
        });
        fxmlLoader.getNamespace();
        choice = (ChoiceBox) fxmlLoader.getNamespace().get("choice");
        choice.setItems(FXCollections.observableArrayList("Boxes", "Bar Chart", "KTree"));
        choice.setOnAction(event -> {
            chooseVisual();
        });
        children = (Spinner) fxmlLoader.getNamespace().get("children");
//        children.ge
        name = (Label) fxmlLoader.getNamespace().get("name");
        Scene dialogScene = new Scene(p, p.getPrefWidth() - 5, p.getPrefHeight());
        root.setScene(dialogScene);
        root.setResizable(false);
    }

    private void chooseVisual (){
        if (choice.getSelectionModel().getSelectedItem().equals("KTree")) {
            children.setDisable(false);
        }
        else {
            children.setDisable(true);
        }
    }

    public void closeButton (){
        changed = false;
        root.close();
    }

    public void okButton (){
        if (choice.getSelectionModel().getSelectedItem() == null) {
            changed = false;
            return;
        }
        String selectedItem = getShortName((String) choice.getSelectionModel().getSelectedItem());
        if (selectedItem.equals(struct.visual) == false || selectedItem.equals("tree")) {
            changed = true;
            struct.visual = getShortName(selectedItem);
            if (selectedItem.equals("tree") || struct.visualOptions != ((Integer) children.getValue()).intValue()) {
                struct.visualOptions = ((Integer) children.getValue()).intValue();
                struct.visual = "tree";
            }
            root.close();
        }
        else {
            changed = false;
            root.close();
        }
    }

    public boolean show (DataStructure struct){
        this.struct = struct;
        name.setText(struct.toString());
        String visual = struct.visual;
        if (visual == null) {
            visual = struct.getAbstractVisual();
        }
        choice.getSelectionModel().select(getLongName(visual));
        if (visual.equals("tree")) {
            children.setDisable(false);
        }
        else {
            children.setDisable(true);
        }
        root.showAndWait();
        return changed;
    }

    public String getShortName (String longName){
        String name = null;
        switch (longName) {
            case "Boxes":
                name = "box";
                break;
            case "Bar Chart":
                name = "bar";
                break;
            case "KTree":
                name = "tree";
                break;
            default:
                name = longName;
                break;
        }
        return name;
    }

    private String getLongName (String shortName){
        String name = null;
        switch (shortName) {
            case "box":
                name = "Boxes";
                break;
            case "bar":
                name = "Bar Chart";
                break;
            case "tree":
                name = "KTree";
                break;
            default:
                name = shortName;
                break;
        }
        return name;
    }
}
