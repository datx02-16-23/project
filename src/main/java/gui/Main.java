package gui;

import java.io.IOException;
import java.util.Map;

import assets.Const;
import assets.example.Examples;
import assets.example.Examples.Algorithm;
import contract.io.LogStreamManager;
import gui.panel.OperationPanel;
import gui.panel.SourcePanel;
import javafx.application.Application;
import javafx.application.Platform;
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
import model.Model;
import render.Visualization;

/**
 * Entry class for the GUI.
 */
public class Main extends Application {

    /**
     * Console for printing system and error messages.
     */
    public static GUIConsole console;
    /**
     * Indicates whether the program is being run for the first time.
     */
    public static boolean    firstRun;

    private Controller       controller;
    private LogStreamManager lsm;

    @Override public void start (Stage primaryStage) throws Exception {
        lsm = new LogStreamManager(Const.PROGRAM_NAME + "_GUI");
        primaryStage.setTitle(Const.PROGRAM_NAME);
        // Create a Group view for the AV.
        Visualization modelRender = new Visualization(Model.instance());
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/Root.fxml"));
        SourcePanel sourceViewer = new SourcePanel();
        controller = new Controller(primaryStage, lsm, sourceViewer, modelRender);
        OperationPanel operationPanel = controller.getOperationPanel();
        fxmlLoader.setController(controller);
        // Load and get the root layout.
        VBox root;
        try {
            root = fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        // Load console
        Map<String, Object> namespace = fxmlLoader.getNamespace();
        console = new GUIConsole((TextArea) namespace.get("console"));
        // Window size
        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenSize.getWidth() * .9;
        double windowHeight = screenSize.getHeight() * .9;
        Scene scene = new Scene(root, windowWidth, windowHeight);
        // Extracting some nodes from the fxml:
        SplitPane sP = (SplitPane) namespace.get("splitPane");
        BorderPane operationPanelContainer = (BorderPane) namespace.get("operationPanelContainer");
        operationPanelContainer.setCenter(operationPanel);
        double leftDivider = (((GridPane) namespace.get("buttonsGrid")).getPrefWidth() + 14) / scene.getWidth();
        sP.setDividerPositions(0, 1 - leftDivider);
        // Add examples
        Menu examples = (Menu) namespace.get("examplesMenu");
        for (Algorithm algo : Examples.Algorithm.values()) {
            MenuItem algoButton = new MenuItem(algo.name);
            algoButton.setOnAction(event -> {
                controller.loadExample(algo);
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
        visualizationPane.add(modelRender, 0, 0);
        // Load needed components of from main view in Controller.
        controller.loadMainViewFxID(fxmlLoader);
        // Load main window
        scene.getStylesheets().add(this.getClass().getResource("/VisualizerStyle.css").toExternalForm());
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            controller.closeProgram();
        });
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/assets/icon.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override public void stop () {
        if (controller != null) {
            controller.stopAutoPlay(); // Kill autoplay thread.
        }
        lsm.close();
    }

    public static void main (String[] args) {
        launch(args);
    }

    /**
     * Printout of error messages and warnings from the program. Strings only. Use Object toString
     * to print them.
     *
     * @author Richard Sundqvist
     *
     */
    public class GUIConsole {

        private static final String prepend_force = "\n<>\t";
        private static final String prepend_err   = "\n>\t";
        private static final String prepend_info  = "\n";
        private static final String prepend_debug = "\n";
        public boolean              quiet         = false;
        public boolean              info          = true;
        public boolean              err           = true;
        public boolean              debug         = false;
        private final TextArea      consoleTextArea;

        public GUIConsole (TextArea consoleTextArea) {
            this.consoleTextArea = consoleTextArea;
            consoleTextArea.setEditable(false);
            init();
        }

        /**
         * Print a regular line to the GUI console.
         *
         * @param info
         *            The line to prine.
         */
        public void info (String info) {
            if (quiet || !this.info) {
                return;
            }
            print(prepend_info + info);
        }

        /**
         * Print an error to the GUI console.
         *
         * @param err
         *            The error to print.
         */
        public void err (String err) {
            if (quiet || !this.err) {
                return;
            }
            print(prepend_err + err);
        }

        /**
         * Print a debug String. Generally DISABLED.
         *
         * @param debug
         *            A debug String to print.
         */
        public void debug (String debug) {
            if (quiet || !this.debug) {
                print(prepend_debug + debug);
            }
        }

        /**
         * Print a line regardless of settings.
         *
         * @param force
         *            The line to print.
         */
        public void force (String force) {
            print((force.equals("") ? "" : prepend_force) + force);
        }

        /**
         * Print the given String. Runs on JavaFX Application thread.
         *
         * @param string
         *            The string to print to the console.
         */
        private void print (String string) {
            Platform.runLater( () -> GUIConsole.this.consoleTextArea.appendText(string));
        }

        /**
         * Enable or disable information printouts.
         *
         * @param value
         *            The setting to apply.
         */
        public void setInfo (boolean value) {
            info = value;
            if (!quiet) {
                print(prepend_info + "Information printouts " + (info ? "ENABLED." : "DISABLED."));
            }
        }

        /**
         * Enable or disable Quiet Mode.
         *
         * @param value
         *            The setting to apply.
         */
        public void setQuiet (boolean value) {
            quiet = value;
            force("Quiet Mode " + (quiet ? "ENABLED." : "DISABLED."));
        }

        /**
         * Enable or disable debug printouts.
         *
         * @param value
         *            The setting to apply.
         */
        public void setDebug (boolean value) {
            debug = value;
            if (!quiet) {
                print(prepend_debug + "Debug printouts " + (debug ? "ENABLED." : "DISABLED."));
            }
        }

        /**
         * Enable or disable error printouts.
         *
         * @param value
         *            The setting to apply.
         */
        public void setError (boolean value) {
            err = value;
            if (!quiet) {
                print(prepend_err + "Error printouts " + (err ? "ENABLED." : "DISABLED."));
            }
        }

        private void init () {
            StringBuilder sb = new StringBuilder();
            sb.append(Const.PROGRAM_NAME + " version " + Const.VERSION_NUMBER);
            sb.append("\nAUTHORS: ");
            for (String s : Const.DEVELOPER_NAMES) {
                sb.append(s + " | ");
            }
            sb.replace(sb.length() - 2, sb.length(), "\n");
            String initMessage = sb.toString();
            Platform.runLater( () -> GUIConsole.this.consoleTextArea.setText(initMessage));
        }

        /**
         * Clear the console.
         */
        public void clear () {
            init();
        }
    }
}
