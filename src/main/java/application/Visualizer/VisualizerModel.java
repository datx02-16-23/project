package application.Visualizer;

import application.model.Model;
import application.model.iModel;
import application.view.Visualization;
import assets.Strings;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import manager.LogStreamManager;

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
        BorderPane root;

        try {
            root = fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        Pane visualizationPane = (Pane) fxmlLoader.getNamespace().get("visualization");
        visualizationPane.getChildren().add(visualization);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add( getClass().getResource("/VisualizerStyle.css").toExternalForm());
        window.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            controller.closeProgram();
        });
       
        /*
         * Handlers
         */
        	//Connected entities
	        MenuItem connectedEntitiesBtn = (MenuItem) fxmlLoader.getNamespace().get("connectedEntitiesBtn");
	        connectedEntitiesBtn.setOnAction(new EventHandler<ActionEvent>(){
				@Override
				public void handle(ActionEvent event) {
					controller.connectedToChannel(window);
				}
	        });
	        
	        //Operation history
	    	ObservableList<wrapper.Operation> listItems = FXCollections.observableArrayList();
	    	@SuppressWarnings("unchecked")
			ListView<wrapper.Operation> operationHistory = (ListView<wrapper.Operation>) fxmlLoader.getNamespace().get("operationHistory");
	    	operationHistory.setItems(listItems);


        window.getIcons().add(new Image(VisualizerModel.class.getResourceAsStream( "/icon.png" )));
	    	
        window.setScene(scene);
        window.show();
    }
    
    public void init(){}

    public static void main(String[] args) {
        launch(args);
    }
}
