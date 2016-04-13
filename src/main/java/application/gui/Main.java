package application.gui;

import application.assets.Strings;
import application.model.Model;
import application.model.iModel;
import application.visualization.Visualization;
import io.LogStreamManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;


/**
 * This is the Model of MVC for the visualizer GUI.
 * All its views comes from VisualizerView.fxml, except for a Group view
 * that is used for the AV.
 * Its controller is VisualizerController.
 */
public class Main extends Application {

    private Stage window;
    private Visualization visualization;
    private final iModel model= new Model();
    private final LogStreamManager lsm = new LogStreamManager();
    private FXMLLoader fxmlLoader;
    private GUI_Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{

        window = primaryStage;
        window.setTitle(Strings.PROJECT_NAME);

        // Create a Group view for the AV.
        visualization = new Visualization(model);

        fxmlLoader = new FXMLLoader(getClass().getResource("/VisualizerView.fxml"));

        controller = new GUI_Controller(visualization, window, model, lsm);

        fxmlLoader.setController(controller);

        // Load and get the root layout.
        VBox root;
        try {
            root = fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Scene scene = new Scene(root, (screenSize.getWidth()*0.5), (screenSize.getHeight()*0.5));

        // Extracting some nodes from the fxml:
        SplitPane sP = (SplitPane) fxmlLoader.getNamespace().get("splitPane");
        VBox sidePanel = (VBox) fxmlLoader.getNamespace().get("rightSidePanel");
        // Hard coding an extra width (-5) to compensate for the width of the divider of splitPane!
        sP.setDividerPositions( 1 - (sidePanel.getPrefWidth() / (scene.getWidth() - 5) ));

        // Add examples
        Menu examples = (Menu) fxmlLoader.getNamespace().get("examplesMenu");
        // Get all .json files
        File folder = new File(getClass().getResource("/examples").getFile());
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        // loop through all files and add menu item
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {

                File file = files[i];
                MenuItem ex = new MenuItem(stylizeExampleName(file.getName()));
                ex.setOnAction(event1 -> controller.setFile(file));
                examples.getItems().add(ex);
            }
        }

        // Add AV
        GridPane visualizationPane = (GridPane) fxmlLoader.getNamespace().get("visualizationPane");
        visualizationPane.add(visualization, 0, 0);

        scene.getStylesheets().add( getClass().getResource("/VisualizerStyle.css").toExternalForm());
        window.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            controller.closeProgram();
        });

        window.getIcons().add(new Image(Main.class.getResourceAsStream("/assets/icon.png")));
        
        window.setScene(scene);
        window.show();

        //Load needed components of from main view in Controller.
        controller.loadMainViewFxID(fxmlLoader);
    }


    /**
     * Make a file name a bit more fancy. For example: "bubble_sort.json" -> "Bubble Sort"
     * @param original The file name
     * @return The file name without '_' and '.json' or .* and there is always an upper case after '_'
     */
    private String stylizeExampleName(String original){
    	StringBuilder sb = new StringBuilder();
    	char currentChar;
    	boolean nextUpper = false;
    	
    	sb.append(Character.toUpperCase(original.charAt(0)));
    	
    	for(int i = 1; i < original.length(); i++){
    		currentChar = original.charAt(i);
    		if(currentChar == '_'){
    			sb.append(" ");
    			nextUpper = true;
    		} else if (currentChar == '.'){
    			return sb.toString();
    		} else {
    			if(nextUpper){
    				currentChar = Character.toUpperCase(currentChar);
    				nextUpper = false;
    			}
    			sb.append(currentChar);
    		}
    	}
    	return sb.toString(); //Shouldn't get called.
    }

    public void stop(){
    	if(controller != null){
    		controller.stopAutoPlay(); //Kill autoplay thread.		
    	}
    }

    public static void main(String[] args) {
        launch(args);
    }
}
