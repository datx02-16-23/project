package application.Visualizer;

import application.model.Model;
import application.model.iModel;
import application.view.Visualization;
import assets.Strings;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import manager.LogStreamManager;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;


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

        GridPane visualizationPane = (GridPane) fxmlLoader.getNamespace().get("visualizationPane");
        
        visualizationPane.add(visualization, 0, 0);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Scene scene = new Scene(root, (screenSize.getWidth()*0.5), (screenSize.getHeight()*0.5));
        scene.getStylesheets().add( getClass().getResource("/VisualizerStyle.css").toExternalForm());
        window.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            controller.closeProgram();
        });

        window.getIcons().add(new Image(VisualizerModel.class.getResourceAsStream( "/icon.png" )));
	    	
        window.setScene(scene);
        window.show();
    }
    
    public void init(){}

    public static void main(String[] args) {
        launch(args);
    }
}
