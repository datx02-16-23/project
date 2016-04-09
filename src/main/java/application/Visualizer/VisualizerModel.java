package application.Visualizer;

import application.model.Model;
import application.model.iModel;
import application.view.Visualization;
import assets.Strings;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
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
    FXMLLoader fxmlLoader;

    @Override
    public void start(Stage primaryStage) throws Exception{

        window = primaryStage;
        window.setTitle(Strings.PROJECT_NAME);

        // Create a Group view for the AV.
        Group datastructs = new Group();
        visualization = new Visualization(model, datastructs);

        fxmlLoader = new FXMLLoader(getClass().getResource("/VisualizerView.fxml"));

        VisualizerController controller = new VisualizerController(visualization, window, model, lsm, fxmlLoader);

        fxmlLoader.setController(controller);
        // Load and get the root layout.
        BorderPane root;

        try {
//        	System.out.println(fxmlLoader.load().toString());
            root = fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

//        root.setCenter(datastructs);
        Pane p = (Pane) fxmlLoader.getNamespace().get("visualization");
        p.getChildren().add(datastructs);
        
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add( getClass().getResource("/VisualizerStyle.css").toExternalForm());
        window.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            controller.closeProgram();
        });
        
        initList();
        
        window.setScene(scene);
        window.show();
    }
    
	public void initList(){
    	ObservableList<wrapper.Operation> listItems = FXCollections.observableArrayList();
    	ListView<wrapper.Operation> operationHistory = (ListView<wrapper.Operation>) fxmlLoader.getNamespace().get("operationHistory");
    	operationHistory.setItems(listItems);
    }
    
    public void init(){

    }


    public static void main(String[] args) {
        launch(args);
    }
}
