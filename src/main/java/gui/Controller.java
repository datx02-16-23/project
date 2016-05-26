package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import assets.Const;
import assets.Debug;
import assets.Tools;
import assets.examples.Examples;
import assets.examples.Examples.Algorithm;
import contract.datastructure.DataStructure;
import contract.io.ComListener;
import contract.io.JGroupCommunicator;
import contract.io.LogStreamManager;
import gui.dialog.ExamplesDialog;
import gui.dialog.VisualDialog;
import gui.panel.ControlPanel;
import gui.panel.SourcePanel;
import gui.view.ConnectedView;
import gui.view.HelpView;
import gui.view.InterpreterView;
import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model2.ExecutionModel;
import model2.ExecutionModelController;
import model2.ModelLoader;
import render.Visualization;

/**
 * Horrendously bloated controller class.
 */
public class Controller implements ComListener {

    // ============================================================= //
    /*
     *
     * Field variables
     *
     */
    // ============================================================= //

    private final Visualization           visualization;
    private final Stage                    primaryStage;
    private final LogStreamManager         lsm;

    private final ExecutionModel           execModel;
    private final ModelLoader              modelLoader;
    private final ExecutionModelController execModelController;
    // Controls
    private Menu                           visualMenu;
    // Views, panels, dialogs
    private final SourcePanel              sourcePanel;
    private final ControlPanel             controlPanel;
    private final ConnectedView            connectedView;

    // ============================================================= //
    /*
     *
     * Constructors
     *
     */
    // ============================================================= //

    public Controller (Stage primaryStage, SourcePanel sourcePanel, Visualization visualization, ControlPanel controlPanel) {
        this.visualization = visualization;
        visualization.setAnimationTime(render.assets.Const.DEFAULT_ANIMATION_TIME);
        this.primaryStage = primaryStage;
        execModel = ExecutionModel.INSTANCE;
        modelLoader = new ModelLoader(execModel);
        execModelController = new ExecutionModelController(execModel, visualization);
        lsm = new LogStreamManager(Const.PROGRAM_NAME);
        lsm.PRETTY_PRINTING = true;
        lsm.setListener(this);
        connectedView = new ConnectedView(primaryStage, (JGroupCommunicator) lsm.getCommunicator());
        this.sourcePanel = sourcePanel;
        this.controlPanel = controlPanel;
    }

    // ============================================================= //
    /*
     *
     * Control / FXML onAction()
     *
     */
    // ============================================================= //

    public void aboutProgram () {
        Main.console.info("Placeholder: A project by ");
        for (String name : Const.DEVELOPER_NAMES) {
            Main.console.info(name + ", ");
        }
    }

    public void openInterpreterView () {
        execModelController.stopAutoExecution();
        InterpreterView interpreterView = new InterpreterView(primaryStage);

        if (interpreterView.show(execModel.getOperations())) {
            // Loader.stripUnusedNames(model);
            System.out.println("sturcs = " + execModel.getDataStructures().keySet());
            execModel.reset();
            visualization.clearAndCreateVisuals();
        }
    }

    public void interpretOperationHistory () {
        InterpreterView interpreterView = new InterpreterView(primaryStage);
        interpreterView.fast(execModel.getOperations());
        visualization.clearAndCreateVisuals();
    }

    // ============================================================= //
    /*
     *
     * Utility
     *
     */
    // ============================================================= //

    public void connectedToChannel () {
        connectedView.show();
    }

    /**
     * Used for closing the GUI properly.
     */
    public void closeProgram () {
        lsm.close();
        primaryStage.close();
    }

    /**
     * Used for choosing a file to Visualize.
     */
    public void openFileChooser () {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        fc.setTitle("Open Log File");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON-Files", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File source = fc.showOpenDialog(primaryStage);
        if (source != null) {
            primaryStage.setTitle(Const.PROJECT_NAME + " - " + source);
            readLog(source);
        }
    }

    /**
     * Helper function for {@link #openFileChooser() openFileChooser}
     *
     * @param file
     *            The file to load.
     */
    public void readLog (File file) {
        lsm.clearData();
        boolean success = false;
        try {
            success = lsm.readLog(file);
        } catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
            Main.console.err("Failed to read log: " + e.getMessage());
        }
        if (success) {
            loadFromLSM();
            lsm.clearData();
            Main.console.info("Import successful: " + file);
        } else {
            Main.console.err("Import failed: " + file);
        }
    }

    /**
     * Load the current data from LSM. Does not clear any data.
     */
    public void loadFromLSM () {
        // Add operations to model and create Render visuals, then draw them.

        boolean modelMayHaveChanged = modelLoader.insertIntoLiveModel(lsm.getDataStructures(), lsm.getOperations());
        if (modelMayHaveChanged == false) {
            return;
        }

        sourcePanel.addSources(lsm.getSources());
        visualization.clearAndCreateVisuals();
        // vis.render(model.getLastOp()); //TODO

        // Update operation list
        loadVisualMenu();
    }

    private void loadVisualMenu () {
        if (execModel.getDataStructures().isEmpty()) {
            visualMenu.setDisable(true);
        }
        visualMenu.setDisable(false);
        visualMenu.getItems().clear();

        /*
         * Add static items.
         */
        MenuItem reset = new MenuItem("Reset Positions");
        reset.setOnAction(event -> {
            visualization.placeVisuals();
        });
        visualMenu.getItems().add(reset);

        MenuItem live = new MenuItem("Show Live Stats");
        live.setOnAction(event -> {
            visualization.showLiveStats();
        });
        live.setDisable(true);
        visualMenu.getItems().add(live);

        visualMenu.getItems().add(new SeparatorMenuItem());

        /*
         * Add controls for the individual structures.
         */

        MenuItem struct_mi;
        for (DataStructure struct : execModel.getDataStructures().values()) {
            struct_mi = new MenuItem();
            struct_mi.setText(struct.identifier + ": " + struct.rawType.toString().toUpperCase());
            struct_mi.setOnAction(event -> {
                openVisualDialog(struct);
            });
            visualMenu.getItems().add(struct_mi);
        }
    }

    public void openVisualDialog (DataStructure struct) {
        VisualDialog visualDialog = new VisualDialog(primaryStage);
        if (visualDialog.show(struct)) {
            visualization.init();
        }
    }

    /**
     * Method for reception of streamed messages.
     */
    @Override public void messageReceived (short messageType) {
        if (messageType >= 10) {
            JGroupCommunicator jgc = (JGroupCommunicator) lsm.getCommunicator();
            connectedView.update(jgc.getMemberStrings(), jgc.allKnownEntities());
            return;
        }
        Platform.runLater( () -> {
            Controller.this.loadFromLSM();
            Controller.this.lsm.clearData();
            // TODO
        });
    }

    public void openDestinationChooser () {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        fc.setTitle("Save Log File");
        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd_HHmmss");
        Calendar cal = Calendar.getInstance();
        fc.setInitialFileName(dateFormat.format(cal.getTime()));
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON-Files", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File target = fc.showSaveDialog(primaryStage);
        if (target == null) {
            return;
        }
        lsm.setOperations(execModel.getOperations());
        lsm.setDataStructures(execModel.getDataStructures());
        lsm.setSources(sourcePanel.getSources());
        boolean old = lsm.PRETTY_PRINTING;
        lsm.PRETTY_PRINTING = execModel.getOperations().size() > 100;
        try {
            Main.console.info("Printing log: " + target);
            lsm.printLog(target);
        } catch (FileNotFoundException e) {
            Main.console.err("Printing failed: " + e.getMessage());
        }
        lsm.PRETTY_PRINTING = old;
    }

    public void propertiesFailed (Exception exception) {
        if (exception != null) {
            exception.printStackTrace();
        }
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/dialog/PropertiesAlertDialog.fxml"));
        Stage stage = new Stage();
        GridPane p = null;
        try {
            p = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene dialogScene = new Scene(p);
        stage.setOnCloseRequest(event -> {
            event.consume();
            stage.close();
        });
        Button close = (Button) loader.getNamespace().get("closeAlert");
        close.setOnAction(event -> {
            event.consume();
            stage.close();
        });
        stage.setScene(dialogScene);
        stage.toFront();
        stage.show();
    }

    public void loadFXML (FXMLLoader fxmlLoader) {
        ObservableMap<String, Object> namespace = fxmlLoader.getNamespace();
        // Load from main view namespace
        visualMenu = (Menu) namespace.get("visualMenu");
        visualMenu.setDisable(true);

        CheckMenuItem debugERR = (CheckMenuItem) namespace.get("debugERR");
        debugERR.setSelected(Debug.ERR);
        CheckMenuItem debugOUT = (CheckMenuItem) namespace.get("debugOUT");
        debugOUT.setSelected(Debug.OUT);
    }

    public void showSettings () {
        // TODO
    }

    public void play () {
        execModelController.startAutoExecution();
    }

    public void forward () {
        execModelController.executeNext();
    }

    public void back () {
        execModelController.executePrevious();
    }

    public void restart () {
        execModelController.reset();
    }

    public void clear () {
        execModelController.clear();
    }

    /**
     * Load an example.
     *
     * @param algo
     *            The algorithm to run.
     */
    public void loadExample (Algorithm algo) {
        ExamplesDialog examplesDialog = new ExamplesDialog(primaryStage);
        double[] data = examplesDialog.show(algo.name);
        if (data == null) {
            return;
        }
        Main.console.force("Not implemented yet. Sorry :/");
        Main.console.info("Running " + algo.name + " on: " + Arrays.toString(data));
        String json = Examples.getExample(algo, data);
        if (json != null) {
            lsm.clearData();
            lsm.unwrap(json);
            loadFromLSM();
            lsm.clearData();
        }
    }

    /*
     * Console controls.
     */
    public void toggleQuietMode (Event e) {
        CheckBox cb = (CheckBox) e.getSource();
        Main.console.setQuiet(cb.isSelected());
    }

    public void toggleInformation (Event e) {
        CheckBox cb = (CheckBox) e.getSource();
        Main.console.setInfo(cb.isSelected());
    }

    public void toggleError (Event e) {
        CheckBox cb = (CheckBox) e.getSource();
        Main.console.setError(cb.isSelected());
    }

    public void toggleDebug (Event e) {
        CheckBox cb = (CheckBox) e.getSource();
        Main.console.setDebug(cb.isSelected());
    }

    public void clearConsole () {
        Main.console.clear();
    }
    /*
     * Console controls end.
     */

    public void dragDropped (DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean hasFiles = db.hasFiles();
        if (hasFiles) {
            for (File file : db.getFiles()) {
                readLog(file);
            }
        }
        event.setDropCompleted(hasFiles);
        event.consume();
    }

    public void dragOver (DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        } else {
            event.consume();
        }
    }

    public void showHelp () {
        new HelpView(primaryStage).show();
    }

    public void debugERR (Event e) {
        Debug.ERR = ((CheckMenuItem) e.getSource()).isSelected();
    }

    public void debugOUT (Event e) {
        Debug.OUT = ((CheckMenuItem) e.getSource()).isSelected();
    }

    public void markElementXY () {
        Tools.markElementXY(visualization);
    }
}
