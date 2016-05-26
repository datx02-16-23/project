package gui;

import java.io.IOException;
import java.util.Map;

import assets.Const;
import assets.examples.Examples;
import assets.examples.Examples.Algorithm;
import contract.io.LogStreamManager;
import gui.panel.ConsolPanel;
import gui.panel.ControlPanel;
import gui.panel.SourcePanel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model2.ExecutionModel;
import render.assets.Visualization2;

/**
 * Entry class for the GUI.
 */
public class Main2 extends Application {

    /**
     * Console for printing system and error messages.
     */
    public static ConsolPanel console;
    /**
     * Indicates whether the program is being run for the first time.
     */
    public static boolean     firstRun;

    private Controller2       Controller2;
    private LogStreamManager  lsm;

    @Override public void start (Stage primaryStage) throws Exception {
        lsm = new LogStreamManager(Const.PROGRAM_NAME + "_GUI");
        primaryStage.setTitle(Const.PROGRAM_NAME);
        // Create a Group view for the AV.
        Visualization2 vis = new Visualization2(ExecutionModel.INSTANCE);
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/Root2.fxml"));
        SourcePanel sourceViewer = new SourcePanel();
        Controller2 = new Controller2(primaryStage, lsm, sourceViewer, vis);
        ControlPanel operationPanel = Controller2.getControlPanel();
        fxmlLoader.setController(Controller2);
        // Load and get the root layout.
        VBox root;
        try {
            root = fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        // Load console
        Map<String, Object> namespace = fxmlLoader.getNamespace();
        console = new ConsolPanel((TextArea) namespace.get("console"));
        // Window size
        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenSize.getWidth() * .9;
        double windowHeight = screenSize.getHeight() * .9;
        Scene scene = new Scene(root, windowWidth, windowHeight);
        // Extracting some nodes from the fxml:
        SplitPane sP = (SplitPane) namespace.get("splitPane");
        BorderPane operationPanelContainer = (BorderPane) namespace.get("operationPanelContainer");
        operationPanelContainer.setCenter(operationPanel);
        sP.setDividerPositions(0, 1);
        // Add examples
        Menu examples = (Menu) namespace.get("examplesMenu");
        for (Algorithm algo : Examples.Algorithm.values()) {
            MenuItem algoButton = new MenuItem(algo.name);
            algoButton.setOnAction(event -> {
                Controller2.loadExample(algo);
            });
            examples.getItems().add(algoButton);
        }
        // Add SourceViewer
        AnchorPane sourceViewContainer = (AnchorPane) namespace.get("sourceViewContainer");
        sourceViewContainer.getChildren().add(sourceViewer);
        AnchorPane.setTopAnchor(sourceViewer, 0.0);
        AnchorPane.setBottomAnchor(sourceViewer, 0.0);
        AnchorPane.setLeftAnchor(sourceViewer, 0.0);
        AnchorPane.setRightAnchor(sourceViewer, 0.0);
        // Add AV
        GridPane visualizationPane = (GridPane) namespace.get("visualizationPane");
        visualizationPane.add(vis, 0, 0);
        // Load needed components of from main view in Controller2.
        Controller2.loadMainViewFxID(fxmlLoader);
        // Load main window
        scene.getStylesheets().add(this.getClass().getResource("/VisualizerStyle.css").toExternalForm());
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            Controller2.closeProgram();
        });
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/icon.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override public void stop () {
        lsm.close();
    }

    public static void main (String[] args) {
        launch(args);
    }
}
