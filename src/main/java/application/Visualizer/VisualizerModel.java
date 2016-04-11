package application.Visualizer;

import application.model.Model;
import application.model.iModel;
import application.view.Visualization;
import assets.Strings;
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
import manager.LogStreamManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * This is the Model of MVC for the visualizer GUI.
 * All its views comes from VisualizerView.fxml, except for a Group view
 * that is used for the AV.
 * Its controller is VisualizerController.
 */
public class VisualizerModel extends Application {

    private Stage window;
    private Visualization visualization;
    private final iModel model= new Model();
    private final LogStreamManager lsm = new LogStreamManager();
    private FXMLLoader fxmlLoader;

    @Override
    public void start(Stage primaryStage) throws Exception{

        window = primaryStage;
        window.setTitle(Strings.PROJECT_NAME);

        // Create a Group view for the AV.
        visualization = new Visualization(model);

        fxmlLoader = new FXMLLoader(getClass().getResource("/VisualizerView.fxml"));

        VisualizerController controller = new VisualizerController(visualization, window, model, lsm, fxmlLoader);

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
        // Hard coding an extra width (+5) to compensate for the width of the divider of splitPane!
        sP.setDividerPositions( 1 - ( (sidePanel.getPrefWidth() + 5) / scene.getWidth() ));

        // Add examples
        Menu examples = (Menu) fxmlLoader.getNamespace().get("examplesMenu");
        // Get all .json files
        File folder = new File(getClass().getResource("/examples").getFile());
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        // loop through all files and add menu item
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {

                File file = files[i];
                System.out.println("File " + file.getName());
                MenuItem ex = new MenuItem(file.getName());
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

        window.getIcons().add(new Image(VisualizerModel.class.getResourceAsStream( "/icon.png" )));
	    	
        window.setScene(scene);
        window.show();
    }
    
    public void init(){
    	
    	InputStream inputStream =getClass().getClassLoader().getResourceAsStream(Strings.PROPERTIES_FILE_NAME);
    	
    	Properties properties = new Properties();
		if (inputStream != null) {
			try {
				properties.load(inputStream);
			} catch (IOException e) {
				System.err.println("Failed to open properties file.");
			}

			try {
				inputStream.close();
			} catch (IOException e) {
				System.err.println("Failed to close properties file.");
			}
		}
		
		System.out.println(properties.getProperty("test"));	
    }

    public static void main(String[] args) {
        launch(args);
    }
}
