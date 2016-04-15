package application.gui;

import application.assets.Strings;
import application.model.Model;
import application.model.iModel;
import application.visualization.Visualization;
import io.LogStreamManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

/**
 * This is the Model of MVC for the visualizer GUI. All its views comes from VisualizerView.fxml, except for a Group
 * view that is used for the AV. Its controller is VisualizerController.
 */
public class Main extends Application {

    /**
     * Console for printing system and error messages.
     */
    public static MavserConsole    console;
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
        controller = new GUI_Controller(visualization, window, model, lsm);
        fxmlLoader.setController(controller);
        // Load and get the root layout.
        VBox root;
        try {
            root = fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        //Load console
        console = new MavserConsole((TextArea) fxmlLoader.getNamespace().get("console"));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Scene scene = new Scene(root, (screenSize.getWidth() * 0.5), (screenSize.getHeight() * 0.5));
        // Extracting some nodes from the fxml:
        SplitPane sP = (SplitPane) fxmlLoader.getNamespace().get("splitPane");
        VBox sidePanel = (VBox) fxmlLoader.getNamespace().get("rightSidePanel");
        // Hard coding an extra width (-5) to compensate for the width of the divider of splitPane!
        sP.setDividerPositions(1 - (sidePanel.getPrefWidth() / (scene.getWidth() - 5)));
        // Add examples
        Menu examples = (Menu) fxmlLoader.getNamespace().get("examplesMenu");
        // Get all .json files
        File folder = new File(getClass().getResource("/examples").getFile());
        File[] files = folder.listFiles( (dir, name) -> name.endsWith(".json"));
        // loop through all files and add menu item
        int i = 0;
        for (   ; i < files.length; i++) {
            if (files[i].isFile()) {
                File file = files[i];
                MenuItem ex = new MenuItem(stylizeExampleName(file.getName()));
                ex.setOnAction(event1 -> controller.setFile(file));
                examples.getItems().add(ex);
            }
        }
        Main.console.out("Loaded "+ i +" examples from: " + getClass().getResource("/examples").getFile());
        // Add AV
        GridPane visualizationPane = (GridPane) fxmlLoader.getNamespace().get("visualizationPane");
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
     * Make a file name a bit more fancy. For example: "bubble_sort.json" -> "Bubble Sort"
     * 
     * @param original The file name
     * @return The file name without '_' and '.json' or .* and there is always an upper case after '_'
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

    public void stop (){
        if (controller != null) {
            controller.stopAutoPlay(); // Kill autoplay thread.
        }
    }

    public static void main (String[] args){
        launch(args);
    }

    /**
     * Printout of error messages and warnings from the program.
     * 
     * @author Richard
     *
     */
    public class MavserConsole {

        private boolean        allowPrinting = true;
        private boolean        verbose       = false;
        private final TextArea consoleTextArea;

        public MavserConsole (TextArea consoleTextArea){
            this.consoleTextArea = consoleTextArea;
            init();
        }

        /**
         * Print a regular line to the GUI console.
         * 
         * @param out The line to prine.
         */
        public void out (String out){
            if (!allowPrinting) {
                return;
            }
            print(out + "\n");
        }

        /**
         * Print an error to the GUI console.
         * 
         * @param err The error to print.
         */
        public void err (String err){
            if (!allowPrinting) {
                return;
            }
            print("\t" + err + "\n");
        }

        /**
         * Print a verbose String. Generally disabled.
         * 
         * @param verbose A verbose String to print.
         */
        public void verbose (String verbose){
            print(verbose + "\n");
        }

        /**
         * Print a line regardless of settings.
         * 
         * @param str The line to print.
         */
        public void forceOut (String str){
            print(str + "\n");
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
         * Disable console output.
         */
        public void enablePrinting (){
            setPrinting(true);
        }

        /**
         * Enable console output.
         */
        public void disablePrinting (){
            setPrinting(false);
        }

        public void setPrinting (boolean value){
            if (value == allowPrinting) {
                return;
            }
            allowPrinting = !value;
            out("Printing " + (allowPrinting != true ? "enabled." : "disabled."));
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
            Platform.runLater(new Runnable() {

                @Override
                public void run (){
                    consoleTextArea.setText(sb.toString());
                }
            });
        }
    }
}
