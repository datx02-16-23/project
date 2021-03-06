package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Properties;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import assets.Const;
import assets.Debug;
import assets.DefaultProperties;
import assets.Tools;
import assets.example.Examples;
import assets.example.Examples.Algorithm;
import contract.datastructure.DataStructure;
import contract.io.CommunicatorListener;
import contract.io.JGroupCommunicator;
import contract.io.LogStreamManager;
import gui.dialog.ExamplesDialog;
import gui.dialog.VisualDialog;
import gui.panel.OperationPanel;
import gui.panel.SourcePanel;
import gui.view.ConnectedView;
import gui.view.HelpView;
import gui.view.InterpreterView;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Model;
import model.Loader;
import multiset.MultisetController;
import render.Visualization;

/**
 * Horrendously bloated controller class.
 */
public class Controller implements CommunicatorListener {

    // ============================================================= //
    /*
     *
     * Field variables
     *
     */
    // ============================================================= //

    private final Visualization    vis;
    private final Stage            window;
    private final LogStreamManager lsm;
    private final Model            model;
    // Controls
    private Menu                   visualMenu;
    private MenuButton             streamBehaviourMenuButton;
    // Stream behaviour
    private boolean                streamAlwaysShowLastOperation = true;
    private boolean                streamStartAutoplay           = false;
    // Autoplay
    private boolean                isPlaying                     = false;
    private int                    stepDelaySpeedupFactor        = 1;
    private long                   stepDelayBase                 = Const.DEFAULT_ANIMATION_TIME;
    private long                   stepDelay                     = stepDelayBase / stepDelaySpeedupFactor;
    // Settings dialog stuff
    private Stage                  settingsView;
    // Views, panels, dialogs
    private final SourcePanel      sourcePanel;
    private final OperationPanel   operationPanel;
    private final ConnectedView    connectedView;
    // Buttons
    private Button                 backwardButton, forwardButton, playPauseButton;
    private ProgressBar            animationProgressBar;
    private Button                 restartButton, clearButton, speedButton;

    private final Loader    modelImporter;

    // ============================================================= //
    /*
     *
     * Constructors
     *
     */
    // ============================================================= //

    public Controller (Stage window, LogStreamManager lsm, SourcePanel sourceViewer, Visualization visualization) {
        vis = visualization;
        vis.setAnimationTime(stepDelay);
        this.window = window;
        model = Model.instance();
        modelImporter = new Loader(model);
        this.lsm = lsm;
        this.lsm.PRETTY_PRINTING = true;
        this.lsm.setListener(this);
        connectedView = new ConnectedView(window, (JGroupCommunicator) lsm.getCommunicator());
        this.sourcePanel = sourceViewer;
        operationPanel = new OperationPanel(this);
        initSettingsPane();
        loadProperties();
    }
    
    
    private DecimalFormat df;
    private Label         settingsSaveState;

    private void initSettingsPane () {
        df = new DecimalFormat("#.####");
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/view/SettingsView.fxml"));
        fxmlLoader.setController(this);
        settingsView = new Stage();
        settingsView.getIcons().add(new Image(Controller.class.getResourceAsStream("/assets/icon_settings.png")));
        settingsView.initModality(Modality.APPLICATION_MODAL);
        settingsView.setTitle(Const.PROGRAM_NAME + ": Settings and Preferences");
        settingsView.initOwner(window);
        GridPane p = null;
        try {
            p = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        settingsView.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            revertSettings();
        });
        // Get namespace items
        // Save state label
        settingsSaveState = (Label) fxmlLoader.getNamespace().get("settingsSaveState");
        // Playpack speed
        timeBetweenField = (TextField) fxmlLoader.getNamespace().get("timeBetweenField");
        perSecField = (TextField) fxmlLoader.getNamespace().get("perSecField");
        toggleAutorunStream = (CheckBox) fxmlLoader.getNamespace().get("toggleAutorunStream");

        p.setPrefWidth(window.getWidth() * 0.75);
        p.setPrefHeight(window.getHeight() * 0.75);
        Scene dialogScene = new Scene(p, window.getWidth() * 0.75, window.getHeight() * 0.75);
        settingsView.setScene(dialogScene);
    }

    public Properties tryLoadProperties () {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(Const.PROPERTIES_FILE_NAME);
        if (inputStream == null) {
            Main.console.err("Failed to open properties file.");
            propertiesFailed(null);
            return DefaultProperties.get();
        }
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            inputStream.close();
            return properties;
        } catch (IOException e) {
            propertiesFailed(e);
            Main.console.err("Property file I/O failed.");
            return DefaultProperties.get();
        }
    }

    // Load settings
    public void loadProperties () {
        Properties properties = tryLoadProperties();
        stepDelayBase = Long.parseLong(properties.getProperty("playbackStepDelay"));
        stepDelay = stepDelayBase; // Speedup factor is 1 at startup.
        streamAlwaysShowLastOperation = Boolean.parseBoolean(properties.getProperty("autoPlayOnIncomingStream"));
    }

    // Save settings
    public void saveProperties () {
        Properties properties = new Properties();
        properties.setProperty("playbackStepDelay", "" + stepDelayBase);
        properties.setProperty("autoPlayOnIncomingStream", "" + streamAlwaysShowLastOperation);
        try {
            URL url = this.getClass().getClassLoader().getResource(Const.PROPERTIES_FILE_NAME);
            OutputStream outputStream = new FileOutputStream(new File(url.toURI()));
            properties.store(outputStream, Const.PROGRAM_NAME + " user preferences.");
        } catch (Exception e) {
            propertiesFailed(e);
        }
    }

    // ============================================================= //
    /*
     *
     * Control (FXML onAction receivers.
     *
     */
    // ============================================================= //

    public void showSettings () {
        // Playback speed
        perSecField.setText(df.format(1000.0 / stepDelayBase));
        timeBetweenField.setText(df.format(stepDelayBase));
        toggleAutorunStream.setSelected(streamAlwaysShowLastOperation);
        // Size and show
        settingsView.setWidth(window.getWidth() * 0.75);
        settingsView.setHeight(window.getHeight() * 0.75);
        settingsView.show();
    }

    public void showMultiset () {
        new MultisetController(window);
    }

    private CheckBox toggleAutorunStream;

    public void toggleAutorunStream () {
        streamAlwaysShowLastOperation = toggleAutorunStream.isSelected();
        unsavedChanged();
    }

    public void jumpToEndClicked (Event e) {
        streamBehaviourMenuButton.setText(">>");
        Main.console.info("Model will always display the latest operation streamed operation.");
        streamAlwaysShowLastOperation = true;
        streamStartAutoplay = false;
    }

    public void continueClicked (Event e) {
        streamBehaviourMenuButton.setText(">");
        Main.console.info("Autoplay will start when a streamed operation has been received.");
        streamAlwaysShowLastOperation = false;
        streamStartAutoplay = true;
    }

    public void doNothingClicked (Event e) {
        streamBehaviourMenuButton.setText("=");
        Main.console.info("Streaming will not force model progression.");
        streamAlwaysShowLastOperation = false;
        streamStartAutoplay = false;
    }

    /**
     * Clear everything.
     */
    public void clearButtonClicked () {
        visualMenu.getItems().clear();
        visualMenu.setDisable(true);
        model.hardClear();
        vis.clear();
        sourcePanel.clear();
        operationPanel.clear();
        setButtons();
        window.setTitle(Const.PROGRAM_NAME);
    }

    /**
     * Starts playing or pause the AV animation.
     */
    public void playPauseButtonClicked () {
        if (!isPlaying) {
            startAutoPlay();
        } else {
            stopAutoPlay();
        }
    }

    private Timeline autoplayTimeline;

    public void startAutoPlay () {
        playPauseButton.setText("Pause");
        if (autoplayTimeline != null) {
            autoplayTimeline.stop();
        }
        isPlaying = true;
        stepForwardButtonClicked();
        autoplayTimeline = new Timeline();
        autoplayTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(stepDelay), event -> {
            if (stepForwardButtonClicked() == false) {
                stopAutoPlay();
            }
        }));
        autoplayTimeline.setCycleCount(Animation.INDEFINITE);
        autoplayTimeline.play();
    }

    public void stopAutoPlay () {
        if (autoplayTimeline != null) {
            autoplayTimeline.stop();
            playPauseButton.setText("Play");
            isPlaying = false;
        }
    }

    /**
     * Restart the AV animation.
     */
    public void restartButtonClicked () {
        stopAutoPlay();
        model.reset();
        vis.reset();
        updatePanels();
        setButtons();
    }

    /**
     * Listener for the Forward button.
     *
     * @return The value of stepModelForward().
     */
    public boolean stepForwardButtonClicked () {
        return stepModelForward();
    }

    /**
     * Step the animation backward
     */
    public void stepBackwardButtonClicked () {
        stopAutoPlay();
        if (model.stepBackward()) {
            vis.init();
            vis.render(model.getLastOp());
            setButtons();
            updatePanels();
        }
    }

    /**
     * Change the animation speed
     */
    public void changeSpeedButtonClicked () {
        boolean isPlaying = this.isPlaying;
        if (isPlaying) {
            stopAutoPlay();
        }
        stepDelaySpeedupFactor = stepDelaySpeedupFactor * 2 % 255;
        speedButton.setText(stepDelaySpeedupFactor + "x");
        stepDelay = stepDelayBase / stepDelaySpeedupFactor;
        vis.setAnimationTime(stepDelay);
        if (isPlaying) {
            startAutoPlay();
        }
    }

    public void changeSpeedButtonRightClicked () {
        boolean isPlaying = this.isPlaying;
        if (isPlaying) {
            stopAutoPlay();
        }
        for (int i = 0; i < 7; i++) {
            changeSpeedButtonClicked();
        }
        if (isPlaying) {
            startAutoPlay();
        }
    }

    public void aboutProgram () {
        Main.console.info("Placeholder: A project by ");
        for (String name : Const.DEVELOPER_NAMES) {
            Main.console.info(name + ", ");
        }
    }

    public void openInterpreterView () {
        stopAutoPlay();
        InterpreterView interpreterView = new InterpreterView(window);

        if (interpreterView.show(model.getOperations())) {
            model.reset();
            vis.clearAndCreateVisuals();
            operationPanel.getItems().setAll(model.getOperations());
            updatePanels();
        }
    }

    public void interpretOperationHistory () {
        InterpreterView interpreterView = new InterpreterView(window);
        interpreterView.fast(model.getOperations());
        updatePanels();
        vis.clearAndCreateVisuals();
        operationPanel.getItems().setAll(lsm.getOperations());
    }

    /*
     * Operation Panel listeners
     */
    /**
     * Jump to the given index. {@code index} less than 0 jumps to start, {@code index}
     * greater than {@code size} jumps to end.
     *
     * @param index
     *            The index to jump to.
     */
    public void goToStep (int index) {
        if (true) { // TODO
            System.err.println("goToStep() is buggy and has been disabled.");
            return;
        }
        model.goToStep(index);
        vis.init();
        vis.render(model.getLastOp());
        operationPanel.update(model.getIndex(), false);
    }

    public void inspectSelection () {
        Main.console.force("Not implemented.");
    }

    public void gotoSelection () {
        goToStep(operationPanel.getIndex());
    }

    public void doubleClickGoTo () {
        goToStep(operationPanel.getIndex());
    }

    // ============================================================= //
    /*
     *
     * Utility
     *
     */
    // ============================================================= //


    /**
     * Steps the model forward and forces any ongoing animations to cancel.
     *
     * @return True if the model could progress. False otherwise.
     */
    private boolean stepModelForward () {
        boolean result = model.stepForward();

        vis.render(model.getLastOp());
        startAnimationProgressBar();
        updatePanels();
        setButtons();

        return result;
    }
    
    /**
     * Update SourcePanel and OperationPanel.
     */
    private void updatePanels () {
        Platform.runLater( () -> {
            int index = Controller.this.model.getIndex();
            Controller.this.sourcePanel.show(Controller.this.model.getLastOp());
            Controller.this.operationPanel.update(index, true);
        });
    }

    private Timeline animationProgressTimeline;
    private double   animationProgress = 0;

    private void buildAnimationProgressIndicator () {
        animationProgressBar.disableProperty().bind(playPauseButton.disableProperty());
        animationProgressTimeline = new Timeline();
        animationProgressTimeline.setCycleCount(99);
        animationProgressTimeline.setOnFinished(event -> {
            animationProgressBar.setProgress(0);
        });
    }

    private void startAnimationProgressBar () {
        animationProgressTimeline.stop();
        animationProgressTimeline.getKeyFrames().clear();
        animationProgress = 0.1;
        KeyFrame kf = new KeyFrame(Duration.millis(stepDelay / 100), event -> {
            animationProgressBar.setProgress(animationProgress);
            animationProgress = animationProgress + 0.01;
        });
        animationProgressTimeline.getKeyFrames().add(kf);
        animationProgressTimeline.playFromStart();
    }

    public void connectedToChannel () {
        connectedView.show();
    }

    /**
     * Used for closing the GUI properly.
     */
    public void closeProgram () {
        lsm.close();
        window.close();
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
        File source = fc.showOpenDialog(window);
        if (source != null) {
            window.setTitle(Const.PROJECT_NAME + " - " + source);
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

        boolean modelMayHaveChanged = modelImporter.insertIntoLiveModel(lsm.getDataStructures(), lsm.getOperations());
        if (modelMayHaveChanged == false) {
            return;
        }

        sourcePanel.addSources(lsm.getSources());
        vis.clearAndCreateVisuals();
        vis.render(model.getLastOp());

        // Update operation list
        operationPanel.getItems().addAll(lsm.getOperations());
        loadVisualMenu();
        updatePanels();
        setButtons();
    }

    private void loadVisualMenu () {
        if (model.getStructures().isEmpty()) {
            visualMenu.setDisable(true);
        }
        visualMenu.setDisable(false);
        visualMenu.getItems().clear();

        /*
         * Add static items.
         */
        MenuItem reset = new MenuItem("Reset Positions");
        reset.setOnAction(event -> {
            vis.placeVisuals();
        });
        visualMenu.getItems().add(reset);

        MenuItem live = new MenuItem("Show Live Stats");
        live.setOnAction(event -> {
            vis.showLiveStats();
        });
        live.setDisable(true);
        visualMenu.getItems().add(live);

        visualMenu.getItems().add(new SeparatorMenuItem());

        /*
         * Add controls for the individual structures.
         */

        MenuItem struct_mi;
        for (DataStructure struct : model.getStructures().values()) {
            struct_mi = new MenuItem();
            struct_mi.setText(struct.identifier + ": " + struct.rawType.toString().toUpperCase());
            struct_mi.setOnAction(event -> {
                openVisualDialog(struct);
            });
            visualMenu.getItems().add(struct_mi);
        }
    }

    public void openVisualDialog (DataStructure struct) {
        VisualDialog visualDialog = new VisualDialog(window);
        if (visualDialog.show(struct)) {
            vis.init();
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

            if (Controller.this.streamAlwaysShowLastOperation) {
                Controller.this.model.goToEnd();
                Controller.this.stepForwardButtonClicked();
            } else if (Controller.this.streamStartAutoplay) {
                Controller.this.startAutoPlay();
            }

            Controller.this.updatePanels();
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
        File target = fc.showSaveDialog(window);
        if (target == null) {
            return;
        }
        lsm.setOperations(model.getOperations());
        lsm.setDataStructures(model.getStructures());
        lsm.setSources(sourcePanel.getSources());
        boolean old = lsm.PRETTY_PRINTING;
        lsm.PRETTY_PRINTING = model.getOperations().size() > 100;
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
            Main.console.err(exception.getMessage());
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

    public void loadMainViewFxID (FXMLLoader fxmlLoader) {
        ObservableMap<String, Object> namespace = fxmlLoader.getNamespace();
        // Load from main view namespace
        playPauseButton = (Button) namespace.get("playPauseButton");
        restartButton = (Button) namespace.get("restartButton");
        backwardButton = (Button) namespace.get("backwardButton");
        forwardButton = (Button) namespace.get("forwardButton");
        clearButton = (Button) namespace.get("clearButton");
        speedButton = (Button) namespace.get("speedButton");
        streamBehaviourMenuButton = (MenuButton) namespace.get("streamBehaviourMenuButton");
        visualMenu = (Menu) namespace.get("visualMenu");
        visualMenu.setDisable(true);

        CheckMenuItem debugERR = (CheckMenuItem) namespace.get("debugERR");
        debugERR.setSelected(Debug.ERR);
        CheckMenuItem debugOUT = (CheckMenuItem) namespace.get("debugOUT");
        debugOUT.setSelected(Debug.OUT);

        animationProgressBar = (ProgressBar) fxmlLoader.getNamespace().get("animationProgress");
        buildAnimationProgressIndicator();

        setButtons();
    }

    /*
     * SETTINGS PANEL
     */
    private boolean settingsChanged = false;

    // Commit changes to file.
    public void saveSettings () {
        if (settingsChanged) {
            saveProperties();
            noUnsavedChanges();
        }
        settingsView.close();
    }

    // Reload settings from file.
    public void revertSettings () {
        if (settingsChanged) {
            loadProperties();
            noUnsavedChanges();
        }
        settingsView.close();
    }

    private void noUnsavedChanges () {
        settingsChanged = false;
        settingsSaveState.setText("No unsaved changes.");
        settingsSaveState.setTextFill(Color.web("#00c8ff"));
    }

    private void unsavedChanged () {
        settingsChanged = true;
        settingsSaveState.setText("Unsaved changes.");
        settingsSaveState.setTextFill(Color.web("#ff0000"));
    }

    // Playback speed
    private TextField perSecField;

    public void setPlayBackOpsPerSec (Event e) {
        long newSpeed;
        try {
            perSecField.setStyle("-fx-control-inner-background: white;");
            newSpeed = Long.parseLong(perSecField.getText());
        } catch (Exception exc) {
            // NaN
            perSecField.setStyle("-fx-control-inner-background: #C40000;");
            return;
        }
        if (newSpeed <= 0) {
            perSecField.setText("invalid");
            perSecField.selectAll();
            return;
        }
        // Valid input. Change other button and speed variable.
        perSecField.setText(df.format(newSpeed));// BLA
        timeBetweenField.setText(df.format(1000.0 / newSpeed));
        stepDelayBase = 1000L / newSpeed;
        stepDelay = stepDelayBase / stepDelaySpeedupFactor;
        unsavedChanged();
    }

    private TextField timeBetweenField;

    public void setPlaybackTimeBetweenOperations (Event e) {
        long newSpeed;
        try {
            perSecField.setStyle("-fx-control-inner-background: white;");
            newSpeed = Long.parseLong(timeBetweenField.getText());
        } catch (Exception exc) {
            // NaN
            perSecField.setStyle("-fx-control-inner-background: #C40000;");
            return;
        }
        if (newSpeed < 0) {
            timeBetweenField.setText("invalid");
            perSecField.selectAll();
            return;
        }
        // Valid input. Change other button and speed variable.
        perSecField.setText(df.format(1000.0 / newSpeed));
        timeBetweenField.setText(df.format(newSpeed));
        stepDelayBase = newSpeed;
        stepDelay = stepDelayBase / stepDelaySpeedupFactor;
        unsavedChanged();
    }

    /*
     * How to do sound in JavaFX.
     */
    private boolean oooooOOoooOOOooooOOoooed = false;

    public void oooooOOoooOOOooooOOooo (Event e) {
        // https://www.youtube.com/watch?v=inli9ukUKIs
        URL resource = this.getClass().getResource("/assets/oooooOOoooOOOooooOOooo.mp3");
        Media media = new Media(resource.toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
        Main.console.info("GET SPoooooOOoooOOOooooOOoooKED!");
        if (!oooooOOoooOOOooooOOoooed) {
            Button spooky = (Button) e.getSource();
            spooky.setBlendMode(BlendMode.SRC_OVER);
            // https://pixabay.com/en/ghost-white-spooky-scary-ghostly-157985/
            Image img = new Image(this.getClass().getResourceAsStream("/assets/oooooOOoooOOOooooOOooo.png"));
            spooky.setGraphic(new ImageView(img));
            oooooOOoooOOOooooOOoooed = true;
            window.setTitle("SpoooooOOoooOOOooooOOoookster!");
        }
    }

    // Fulhack
    public OperationPanel getOperationPanel () {
        return operationPanel;
    }

    /**
     * Load an example.
     *
     * @param algo
     *            The algorithm to run.
     */
    public void loadExample (Algorithm algo) {
        ExamplesDialog examplesDialog = new ExamplesDialog(window);
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
        new HelpView(window).show();
        ;
    }

    /**
     * Set enable/disable on buttons.
     */
    public void setButtons () {
        // TODO: Use a property in Model instead.

        if (model.isHardCleared()) { // Model clear?
            playPauseButton.setDisable(true);
            animationProgressBar.setProgress(0);
            forwardButton.setDisable(true);
            // backwardButton.setDisable(true);
            restartButton.setDisable(true);
            clearButton.setDisable(true);
            return;
        }

        boolean forward = !model.tryStepForward();
        playPauseButton.setDisable(forward);
        forwardButton.setDisable(forward);
        boolean backward = !model.tryStepBackward();
        // backwardButton.setDisable(backward);
        restartButton.setDisable(backward);
        clearButton.setDisable(false);
    }

    public void debugERR (Event e) {
        Debug.ERR = ((CheckMenuItem) e.getSource()).isSelected();
    }

    public void debugOUT (Event e) {
        Debug.OUT = ((CheckMenuItem) e.getSource()).isSelected();
    }

    public void markElementXY () {
        Tools.markElementXY(vis);
    }
}
