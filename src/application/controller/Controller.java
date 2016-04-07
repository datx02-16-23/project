package application.controller; /**
 * Created by cb on 17/03/16.
 */


import application.model.Model;
import application.model.iModel;
import application.view.View;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import manager.LogStreamManager;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;

public class Controller extends Application{
    private final LogStreamManager lsm = new LogStreamManager();
    private final iModel model;
    private final View view;

    public Controller(){
        model = new Model();
        view = new View(model);

    }

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        VBox vbox = new VBox();
        Scene scene = new Scene(vbox, 800, 600);

        //Add menu
        buildMenu(scene);

        Group datastructs = new Group();
        view.render(datastructs);
        vbox.getChildren().add(datastructs);


        stage.setTitle("MAVSER");
        stage.setScene(scene);
        stage.show();
    }

    private void buildMenu(Scene scene){
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem openFile = new MenuItem("Open File");
        openFile.setOnAction(event -> {
            fileChooser();
        });
        menuFile.getItems().addAll(openFile);

        menuBar.getMenus().addAll(menuFile);
        ((VBox)scene.getRoot()).getChildren().addAll(menuBar);

    }

    private void fileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        //Should possibly belong to the scene
        fileChooser.showOpenDialog(null);
        File file = fileChooser.getSelectedFile();

        if (file != null){
            setFile(file);
        } else {
            System.err.println("Unable to find file");
        }

    }

    private void setFile(File file) {
    	lsm.readLog(file);
    	model.set(lsm.getKnownVariables(), lsm.getOperations());
    }
}
