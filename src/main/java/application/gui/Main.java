package application.gui;

import application.assets.Strings;
import application.gui.panels.OperationPanel;
import application.gui.panels.SourcePanel;
import application.model.*;
import application.visualization.Visualization;
import io.LogStreamManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * This is the Model of MVC for the visualizer GUI. All its views comes from VisualizerView.fxml, except for a Group
 * view that is used for the AV. Its controller is VisualizerController.
 */
public class Main extends Application {

    /**
     * Console for printing system and error messages.
     */
    public static MavserConsole    console;
    /**
     * Indicates wether the program is being run for the first time.
     */
    public static boolean          firstRun;
    private Stage                  window;
    private Visualization          visualization;
    private final iModel           model = new Model();
    private final LogStreamManager lsm   = new LogStreamManager();
    private FXMLLoader             fxmlLoader;
    private GUI_Controller         controller;

    @Override
    public void start (Stage primaryStage) throws Exception{
        window = primaryStage;
        window.setTitle(Strings.PROJECT_NAME);
        // Create a Group view for the AV.
        visualization = new Visualization(model);
        fxmlLoader = new FXMLLoader(getClass().getResource("/VisualizerView.fxml"));
        SourcePanel sourceViewer = new SourcePanel();
        controller = new GUI_Controller(visualization, window, model, lsm, sourceViewer);
        OperationPanel operationPanel = controller.getOperationPanel();
        fxmlLoader.setController(controller);
        // Load and get the root layout.
        VBox root;
        try {
            root = fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        //Load console
        Map<String, Object> namespace = fxmlLoader.getNamespace();
        console = new MavserConsole((TextArea) namespace.get("console"));
        //Window size
        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        double windowWidth = (screenSize.getWidth() * .9);
        double windowHeight = (screenSize.getHeight() * .9);
        Scene scene = new Scene(root, windowWidth, windowHeight);
        // Extracting some nodes from the fxml:
        SplitPane sP = (SplitPane) namespace.get("splitPane");
        BorderPane operationPanelContainer = (BorderPane) namespace.get("operationPanelContainer");
        operationPanelContainer.setCenter(operationPanel);
        double leftDivider = (((GridPane) namespace.get("buttonsGrid")).getPrefWidth()+14) / scene.getWidth();
        sP.setDividerPositions(leftDivider, 1 - leftDivider);
        // Add examples
        Menu examples = (Menu) namespace.get("examplesMenu");
        // Get all .json files
        File folder = new File(getClass().getResource("/examples").getFile());
        File[] files = folder.listFiles( (dir, name) -> name.endsWith(".oi"));
        // loop through all files and add menu item
        int i = 0;
        for (; i < files.length; i++) {
            if (files[i].isFile()) {
                File file = files[i];
                MenuItem ex = new MenuItem(stylizeExampleName(file.getName()));
                ex.setOnAction(event1 -> controller.loadFile(file));
                examples.getItems().add(ex);
            }
        }
        Main.console.info("Loaded " + i + " examples from: " + getClass().getResource("/examples").getFile());
        // Add SourceViewer
        AnchorPane sourceViewContainer = (AnchorPane) namespace.get("sourceViewContainer");
        sourceViewContainer.getChildren().add(sourceViewer);
        AnchorPane.setTopAnchor(sourceViewer, 0.0);
        AnchorPane.setBottomAnchor(sourceViewer, 0.0);
        AnchorPane.setLeftAnchor(sourceViewer, 0.0);
        AnchorPane.setRightAnchor(sourceViewer, 0.0);
        // Add AV
        GridPane visualizationPane = (GridPane) namespace.get("visualizationPane");
        visualizationPane.add(visualization, 0, 0);
        // Initialize console
        //Load main window
        scene.getStylesheets().add(getClass().getResource("/VisualizerStyle.css").toExternalForm());
        window.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            controller.closeProgram();
        });
        window.getIcons().add(new Image(Main.class.getResourceAsStream("/assets/icon.png")));
        window.setScene(scene);
        window.show();
        // Load needed components of from main view in Controller.
        controller.loadMainViewFxID(fxmlLoader);
    }

    /**
     * Make a file name a bit more fancy. For example: "bubble_sort.oi" -> "Bubble Sort"
     * 
     * @param original The original file name
     * @return The file name without '_' and '.*' and there is always an upper case after '_'
     */
    private String stylizeExampleName (String original){
        StringBuilder sb = new StringBuilder();
        char currentChar;
        boolean nextUpper = false;
        sb.append(Character.toUpperCase(original.charAt(0)));
        for (int i = 1; i < original.length(); i++) {
            currentChar = original.charAt(i);
            if (currentChar == '_') {
                sb.append(" ");
                nextUpper = true;
            }
            else if (currentChar == '.') {
                return sb.toString();
            }
            else {
                if (nextUpper) {
                    currentChar = Character.toUpperCase(currentChar);
                    nextUpper = false;
                }
                sb.append(currentChar);
            }
        }
        return sb.toString(); // Shouldn't get called.
    }

    @Override
    public void stop (){
        if (controller != null) {
            controller.stopAutoPlay(); // Kill autoplay thread.
        }
    }

    public static void main (String[] args){
        launch(args);
    }

    /**
     * Printout of error messages and warnings from the program. Strings only. Use Object toString to print them.
     * 
     * @author Richard
     *
     */
    public class MavserConsole {

        private static final String force       = "<>\t";
        public boolean              quiet       = false;
        public boolean              information = true;
        public boolean              error       = true;
        public boolean              debug       = false;
        public final TextArea       consoleTextArea;

        public MavserConsole (TextArea consoleTextArea){
            this.consoleTextArea = consoleTextArea;
            consoleTextArea.setEditable(false);
            init();
        }

        /**
         * Print a regular line to the GUI console.
         * 
         * @param info The line to prine.
         */
        public void info (String info){
            if (quiet || !information) {
                return;
            }
            print(info + "\n");
        }

        /**
         * Print an error to the GUI console.
         * 
         * @param err The error to print.
         */
        public void err (String err){
            if (quiet || !error) {
                return;
            }
            print("\t" + err + "\n");
        }

        /**
         * Print a verbose String. Generally DISABLED.
         * 
         * @param out A verbose String to print.
         */
        public void debug (String out){
            if (quiet || !debug) {
                print(out + "\n");
            }
        }

        /**
         * Print a line regardless of settings.
         * 
         * @param str The line to print.
         */
        public void force (String str){
            print(force + str + "\n");
        }

        /**
         * Print the given String. Run on JavaFX Application thread.
         * 
         * @param string The string to print to the console.
         */
        private void print (String string){
            Platform.runLater(new Runnable() {

                @Override
                public void run (){
                    consoleTextArea.appendText(string);
                }
            });
        }

        /**
         * Enable or disable information printouts.
         */
        public void setInfo (boolean value){
            information = value;
            force("Information printouts " + (information ? "ENABLED." : "DISABLED."));
        }

        /**
         * Enable or disable Quiet Mode.
         */
        public void setQuiet (boolean value){
            quiet = value;
            force("Quiet Mode " + (quiet ? "ENABLED." : "DISABLED."));
        }

        /**
         * Enable or disable debug printouts.
         */
        public void setDebug (boolean value){
            debug = value;
            force("Debug printouts " + (debug ? "ENABLED." : "DISABLED."));
        }

        /**
         * Enable or disable error printouts.
         */
        public void setError (boolean value){
            error = value;
            force("Error printouts " + (error ? "ENABLED." : "DISABLED."));
        }

        public void init (){
            StringBuilder sb = new StringBuilder();
            sb.append("Welcome to " + Strings.PROJECT_NAME + "!\n");
            sb.append("Version: " + Strings.VERSION_NUMBER + "\n\n");
            sb.append("AUTHORS: ");
            for (String s : Strings.DEVELOPER_NAMES) {
                sb.append(s + " | ");
            }
            sb.delete(sb.length() - 2, sb.length());
            sb.append("\n\n");
            String initMessage = sb.toString();
            Platform.runLater(new Runnable() {

                @Override
                public void run (){
                    consoleTextArea.setText(initMessage);
                }
            });
        }
    }
}
