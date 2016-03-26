package application.controller; /**
 * Created by cb on 17/03/16.
 */


import application.model.Model;
import application.view.View;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import manager.LogStreamManager;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;

public class Controller extends Application{
    private final LogStreamManager lsm = new LogStreamManager();
    private final Model model;
    private final View view;

    public Controller(){
        model = new Model();
        view = new View();
    }

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Circle circ = new Circle(40, 40, 30);
        Group root = new Group(circ);
        Scene scene = new Scene(new VBox(), 400, 300);

        //Add menu
        menu(scene);

        stage.setTitle("My JavaFX Application");
        stage.setScene(scene);
        stage.show();
    }

    private void menu(Scene scene){
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
            try {
                lsm.readLog(file);
                model.setOperations(lsm.getOperations());

            } catch (FileNotFoundException e){

            }
        }
    }
}
