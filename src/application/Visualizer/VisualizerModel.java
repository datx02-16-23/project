package application.Visualizer;

import application.model.Model;
import application.model.iModel;
import application.view.Visualization;
import assets.Strings;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import manager.LogStreamManager;

import java.io.IOException;

public class VisualizerModel extends Application {

    private Stage window;
    private Visualization visualization;
    private final iModel model= new Model();
    private final LogStreamManager lsm = new LogStreamManager();

    @Override
    public void start(Stage primaryStage) throws Exception{

        window = primaryStage;
        window.setTitle(Strings.PROJECT_NAME);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("VisualizerView.fxml"));
        VisualizerController controller = new VisualizerController(window, model, lsm);
        fxmlLoader.setController(controller);

        BorderPane root;
        try {
            root = fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        visualization = new Visualization(model);
        Group datastructs = new Group();
        visualization.render(datastructs);
        root.setCenter(datastructs);

        window.setOnCloseRequest(event -> controller.closeProgram());
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add( getClass().getResource("VisualizerStyle.css").toExternalForm());
        window.setScene(scene);
        window.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
