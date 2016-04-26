package application.gui;

import application.assets.*;
import application.assets.examples.Examples;
import application.assets.examples.Examples.Algorithm;
import application.gui.panels.*;
import application.gui.views.*;
import application.model.Model;
import application.visualization.Visualization;
import io.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import multiset.MultisetAnimation;
import multiset.MultisetController;
import wrapper.datastructures.DataStructure;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;

/**
 * This is the Controller of MVC for the visualizer GUI.
 */
public class GUI_Controller implements CommunicatorListener {

    private Visualization                   visualization;
    private Stage                           window;
    private final LogStreamManager          lsm;
    private final Model                     model;
    // Controls
    private Menu                            visualMenu;
    private MenuButton                      streamBehaviourMenuButton;
    private boolean                         stream_always_show_last_op = true;
    private boolean                         stream_start_autoplay      = false;
    //Autoplay
    private boolean                         isPlaying                  = false;
    private int                             stepDelaySpeedupFactor     = 1;
    private long                            stepDelayBase              = 1500;
    private long                            stepDelay                  = stepDelayBase / stepDelaySpeedupFactor;
    // Settings dialog stuff
    private Stage                           settingsView;
    //Views, panels, dialogs
    private final ConnectedView             connectedView;
    private final InterpreterView           interpreterView;
    private final SourcePanel               sourceViewer;
    private final OperationPanel            operationPanel;
    private final ExamplesDialog            examplesDialog;
    private final VisualDialog              visualDialog;
    private final IdentifierCollisionDialog icd;

    public GUI_Controller (Stage window, LogStreamManager lsm, SourcePanel sourceViewer){
        this.visualization = Visualization.instance();
        Visualization.setAnimationTime(stepDelay);
        this.window = window;
        model = Model.instance();
        this.lsm = lsm;
        this.lsm.PRETTY_PRINTING = true;
        this.lsm.setListener(this);
        this.sourceViewer = sourceViewer;
        this.operationPanel = new OperationPanel(this);
        this.examplesDialog = new ExamplesDialog(window);
        this.visualDialog = new VisualDialog(window);
        this.connectedView = new ConnectedView(window, (JGroupCommunicator) lsm.getCommunicator());
        this.icd = new IdentifierCollisionDialog(window);
        initSettingsPane();
        interpreterView = new InterpreterView(window);
        loadProperties();
    }

    public void showSettings (){
        settingsView.setWidth(this.window.getWidth() * 0.75);
        settingsView.setHeight(this.window.getHeight() * 0.75);
        // Playback speed
        perSecField.setText(df.format(1000.0 / stepDelayBase));
        timeBetweenField.setText(df.format(stepDelayBase));
        toggleAutorunStream.setSelected(stream_always_show_last_op);
        //Size and show
        settingsView.setWidth(this.window.getWidth() * 0.75);
        settingsView.setHeight(this.window.getHeight() * 0.75);
        settingsView.show();
    }

    public void showMultiset(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MultisetView.fxml"));
        fxmlLoader.setController(new MultisetController(window));
        VBox p = null;
        try {
            p = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        window.setScene(new MultisetAnimation(p, this.window.getWidth(), this.window.getHeight(), fxmlLoader));
    }

    private CheckBox toggleAutorunStream;

    public void toggleAutorunStream (){
        stream_always_show_last_op = toggleAutorunStream.isSelected();
        unsavedChanged();
    }

    public void jumpToEndClicked (Event e){
        streamBehaviourMenuButton.setText(">>");
        Main.console.info("Model will always display the latest operation streamed operation.");
        stream_always_show_last_op = true;
        stream_start_autoplay = false;
    }

    public void continueClicked (Event e){
        streamBehaviourMenuButton.setText(">");
        Main.console.info("Autoplay will start when a streamed operation has been received.");
        stream_always_show_last_op = false;
        stream_start_autoplay = true;
    }

    public void doNothingClicked (Event e){
        streamBehaviourMenuButton.setText("=");
        Main.console.info("Streaming will not force model progression.");
        stream_always_show_last_op = false;
        stream_start_autoplay = false;
    }

    /**
     * Clear everything.
     */
    public void clearButtonClicked (){
        visualMenu.getItems().clear();
        visualMenu.setDisable(true);
        model.clear();
        visualization.clear();
        sourceViewer.clear();
        operationPanel.clear();
        visualMenu.getItems().clear();
    }

    /**
     * Starts playing or pause the AV animation.
     */
    private Button playPauseButton;

    public void playPauseButtonClicked (){
        if (!isPlaying) {
            startAutoPlay();
        }
        else {
            stopAutoPlay();
        }
    }

    private Timeline autoplayTimeline;

    public void startAutoPlay (){
        playPauseButton.setText("Pause");
        if (autoplayTimeline != null) {
            autoplayTimeline.stop();
        }
        isPlaying = true;
        stepForwardButtonClicked();
        autoplayTimeline = new Timeline();
        autoplayTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(stepDelay), new EventHandler<ActionEvent>() {

            @Override
            public void handle (ActionEvent actionEvent){
                if (stepForwardButtonClicked() == false) {
                    stopAutoPlay();
                }
            }
        }));
        autoplayTimeline.setCycleCount(Animation.INDEFINITE);
        autoplayTimeline.play();
        autoplayTimeline.setCycleCount(Animation.INDEFINITE);
        autoplayTimeline.play();
    }

    public void stopAutoPlay (){
        if (autoplayTimeline != null) {
            autoplayTimeline.stop();
            playPauseButton.setText("Play");
            isPlaying = false;
        }
    }

    /**
     * Restart the AV animation.
     */
    public void restartButtonClicked (){
        stopAutoPlay();
        model.reset();
        updatePanels();
        visualization.render(null);
    }

    /**
     * Listener for the Forward button.
     * 
     * @return The value of stepModelForward().
     */
    public boolean stepForwardButtonClicked (){
        return stepModelForward();
    }

    /**
     * Steps the model forward and forces any ongoing animations to cancel.
     * 
     * @return True if the model progress. False otherwise.
     */
    private boolean stepModelForward (){
        if (model.stepForward()) {
            visualization.render(model.getCurrentStep().getLastOp());
            updatePanels();
            return true;
        }
        else {
            visualization.render(model.getCurrentStep().getLastOp());
            return false;
        }
    }

    /**
     * Step the animation backward
     */
    public void stepBackwardButtonClicked (){
        stopAutoPlay();
        model.stepBackward();
        visualization.render(model.getCurrentStep().getLastOp());
        updatePanels();
    }

    private Button   speedButton;
    private MenuItem speedMenuItem;

    /**
     * Change the animation speed
     */
    public void changeSpeedButtonClicked (){
        boolean isPlaying = this.isPlaying;
        if (isPlaying) {
            stopAutoPlay();
        }
        stepDelaySpeedupFactor = stepDelaySpeedupFactor * 2 % 255;
        speedButton.setText(stepDelaySpeedupFactor + "x");
        stepDelay = stepDelayBase / stepDelaySpeedupFactor;
        Visualization.setAnimationTime(stepDelay);
        if (isPlaying) {
            startAutoPlay();
        }
    }
    
    public void changeSpeedButtonRightClicked(){
        boolean isPlaying = this.isPlaying;
        if (isPlaying) {
            stopAutoPlay();
        }
        for(int i = 0; i < 7; i++){
            changeSpeedButtonClicked();
        }
        if (isPlaying) {
            startAutoPlay();
        }
    }

    public void aboutProgram (){
        Main.console.info("Placeholder: A project by ");
        for (String name : Strings.DEVELOPER_NAMES) {
            Main.console.info(name + ", ");
        }
    }

    public void openInterpreterView (){
        stopAutoPlay(); // Prevent concurrent modification exception.
        if (interpreterView.show(model.getOperations())) {
            model.reset();
            visualization.clearAndCreateVisuals();
            operationPanel.getItems().setAll(model.getOperations());
//            operationPanel.update(0, true);
            updatePanels();
        }
    }

    public void interpretOperationHistory (){
        interpreterView.fast(model.getOperations());
        updatePanels();
        visualization.clearAndCreateVisuals();
        operationPanel.getItems().addAll(lsm.getOperations());
    }

    /**
     * Update SourcePanel and OperationPanel.
     */
    private void updatePanels (){
        Platform.runLater(new Runnable() {

            @Override
            public void run (){
                int index = model.getIndex();
                sourceViewer.show(model.getCurrentStep().getLastOp());
                operationPanel.update(index, true);
            }
        });
    }

    /*
     * Operation Panel listeners
     */
    /**
     * Jump to the given index. {@code index} less than 0 jumps to start, {@code index} greater than {@code size} jumps
     * to end.
     * 
     * @param index The index to jump to.
     */
    public void goToStep (int index){
        model.goToStep(index);
        visualization.render(model.getCurrentStep().getLastOp());
        operationPanel.update(model.getIndex(), false);
    }

    public void inspectSelection (){
        Main.console.force("Not implemented.");
    }

    public void gotoSelection (){
        goToStep(operationPanel.getIndex());
    }

    public void doubleClickGoTo (){
        goToStep(operationPanel.getIndex());
    }

    /*
     * Operation Panel end.
     */
    private DecimalFormat df;
    private Label         settingsSaveState;

    private void initSettingsPane (){
        df = new DecimalFormat("#.####");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/SettingsView.fxml"));
        fxmlLoader.setController(this);
        settingsView = new Stage();
        settingsView.getIcons().add(new Image(GUI_Controller.class.getResourceAsStream("/assets/icon_settings.png")));
        settingsView.initModality(Modality.APPLICATION_MODAL);
        settingsView.setTitle(Strings.PROJECT_NAME + ": Settings and Preferences");
        settingsView.initOwner(this.window);
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
        p.setPrefWidth(this.window.getWidth() * 0.75);
        p.setPrefHeight(this.window.getHeight() * 0.75);
        Scene dialogScene = new Scene(p, this.window.getWidth() * 0.75, this.window.getHeight() * 0.75);
        settingsView.setScene(dialogScene);
    }

    public void connectedToChannel (){
        connectedView.show();
    }

    /**
     * Used for closing the GUI properly.
     */
    public void closeProgram (){
        lsm.close();
        window.close();
    }

    /**
     * Used for choosing a file to Visualize.
     */
    public void openFileChooser (){
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        fc.setTitle("Open OI-File");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("OI-Files", "*.oi"), new FileChooser.ExtensionFilter("All Files", "*.*"));
        File source = fc.showOpenDialog(window);
        if (source != null) {
            readLog(source);
        }
    }

    /**
     * Helper function for {@link #openFileChooser() openFileChooser}
     * 
     * @param file
     */
    public void readLog (File file){
        lsm.clearData();
        if (lsm.readLog(file) == false) {
            Main.console.err("Failed to read log: " + file);
            return;
        }
        loadFromLSM();
        //Clean lsm
        lsm.clearData();
    }

    private boolean always_clear_old = false;
    private boolean always_keep_old  = false;

    /**
     * Load the current data from LSM. Does not clear any data.
     */
    public void loadFromLSM (){
        //Add operations to model and create Render visuals, then draw them.
        Map<String, DataStructure> oldStructs = model.getStructures();
        Map<String, DataStructure> newStructs = lsm.getDataStructures();
        if (checkCollision(oldStructs, newStructs) == false) {
            return;
        }
        oldStructs.putAll(newStructs);
        visualMenu.getItems().clear();
        visualMenu.setDisable(newStructs.isEmpty());
        model.getOperations().addAll(lsm.getOperations());
        sourceViewer.addSources(lsm.getSources());
        visualization.clearAndCreateVisuals();
        visualization.render(model.getCurrentStep().getLastOp());
        //Update operation list
        operationPanel.getItems().addAll(lsm.getOperations());
        loadVisualMenu();
        updatePanels();
    }

    private boolean checkCollision (Map<String, DataStructure> oldStructs, Map<String, DataStructure> newStructs){
        checkCollison: for (String newKey : newStructs.keySet()) {
            for (String oldKey : oldStructs.keySet()) {
                if (oldKey.equals(newKey)) {
                    Main.console.force("ERROR: Data Structure identifier collision:");
                    Main.console.force("Known structures: " + model.getStructures().values());
                    Main.console.force("New structures: " + lsm.getDataStructures().values());
                    if (always_clear_old) {
                        Main.console.force("Known structures cleared.");
                        clearButtonClicked();
                        break checkCollison;
                    }
                    else if (always_keep_old) {
                        Main.console.force("New structures rejected.");
                        lsm.clearData();
                        return false;
                    }
                    else {
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        short routine = icd.show(oldStructs.values(), oldStructs.values());
                        switch (routine) {
                            //Clear old structures, import new
                            case IdentifierCollisionDialog.ALWAYS_CLEAR_OLD:
                                always_clear_old = true;
                                clearButtonClicked();
                                Main.console.force("Conflicting structures will overrwrite existing for this session.");
                                break checkCollison;
                            case IdentifierCollisionDialog.CLEAR_OLD:
                                clearButtonClicked();
                                Main.console.force("Known structures cleared.");
                                break checkCollison;
                            //Reject new structures
                            case IdentifierCollisionDialog.ALWAYS_KEEP_OLD:
                                always_keep_old = true;
                                Main.console.force("Conflicting structures will be rejected for this session.");
                                lsm.clearData();
                                return false;
                            case IdentifierCollisionDialog.KEEP_OLD:
                                Main.console.force("New structures rejected.");
                                lsm.clearData();
                                return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void loadVisualMenu (){
        for (DataStructure struct : model.getStructures().values()) {
            MenuItem mi = new MenuItem();
            mi.setText(struct.identifier + ": " + struct.rawType.toString().toUpperCase());
            mi.setOnAction(event -> {
                openVisualDialog(struct);
            });
            visualMenu.getItems().add(mi);
        }
    }

    public void openVisualDialog (DataStructure struct){
        if (visualDialog.show(struct)) {
            visualization.clearAndCreateVisuals();
            int step = model.getIndex();
            model.reset();
            goToStep(step);
        }
    }

    @Override
    public void messageReceived (short messageType){
        if (messageType >= 10) {
            JGroupCommunicator jgc = (JGroupCommunicator) lsm.getCommunicator();
            connectedView.update(jgc.getMemberStrings(), jgc.allKnownEntities());
            return;
        }
        Platform.runLater(new Runnable() {

            @Override
            public void run (){
                if (stream_always_show_last_op) {
                    model.goToEnd();
                }
                else if (stream_start_autoplay) {
                    startAutoPlay();
                }
                loadFromLSM();
                lsm.clearData();
                if (stream_always_show_last_op) {
                    stepForwardButtonClicked();
                }
                else {
                    updatePanels();
                }
            }
        });
    }

    public void openDestinationChooser (){
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        fc.setTitle("Save OI-File");
        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd_HHmmss");
        Calendar cal = Calendar.getInstance();
        fc.setInitialFileName(dateFormat.format(cal.getTime()));
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("OI-Files", "*.oi"), new FileChooser.ExtensionFilter("All Files", "*.*"));
        File target = fc.showSaveDialog(this.window);
        if (target == null) {
            return;
        }
        lsm.setOperations(model.getOperations());
        lsm.setDataStructures(model.getStructures());
        lsm.setSources(sourceViewer.getSources());
        boolean old = lsm.PRETTY_PRINTING;
        lsm.PRETTY_PRINTING = model.getOperations().size() > 100;
        lsm.printLog(target);
        lsm.PRETTY_PRINTING = old;
    }

    public void propertiesFailed (Exception exception){
        if (exception != null) {
            Main.console.err(exception.getMessage());
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/PropertiesAlert.fxml"));
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

    public void loadMainViewFxID (FXMLLoader mainViewLoader){
        ObservableMap<String, Object> namespace = mainViewLoader.getNamespace();
        //Load from main view namespace
        playPauseButton = (Button) namespace.get("playPauseButton");
        speedButton = (Button) namespace.get("speedButton");
        speedMenuItem = (MenuItem) namespace.get("speedMenuItem");
        streamBehaviourMenuButton = (MenuButton) namespace.get("streamBehaviourMenuButton");
        visualMenu = (Menu) namespace.get("visualMenu");
        visualMenu.setDisable(true);
    }

    /*
     * SETTINGS PANEL
     */
    private boolean settingsChanged = false;

    // Commit changes to file.
    public void saveSettings (){
        if (settingsChanged) {
            saveProperties();
            noUnsavedChanges();
        }
        settingsView.close();
    }

    // Reload settings from file.
    public void revertSettings (){
        if (settingsChanged) {
            loadProperties();
            noUnsavedChanges();
        }
        settingsView.close();
    }

    private void noUnsavedChanges (){
        settingsChanged = false;
        settingsSaveState.setText("No unsaved changes.");
        settingsSaveState.setTextFill(Color.web("#00c8ff"));
    }

    private void unsavedChanged (){
        settingsChanged = true;
        settingsSaveState.setText("Unsaved changes.");
        settingsSaveState.setTextFill(Color.web("#ff0000"));
    }

    // Playback speed
    private TextField perSecField;

    public void setPlayBackOpsPerSec (Event e){
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
        timeBetweenField.setText(df.format((1000.0 / newSpeed)));
        stepDelayBase = (1000L / newSpeed);
        stepDelay = stepDelayBase / stepDelaySpeedupFactor;
        unsavedChanged();
    }

    private TextField timeBetweenField;

    public void setPlaybackTimeBetweenOperations (Event e){
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

    public Properties tryLoadProperties (){
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(Strings.PROPERTIES_FILE_NAME);
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
    public void loadProperties (){
        Properties properties = tryLoadProperties();
        stepDelayBase = Long.parseLong(properties.getProperty("playbackStepDelay"));
        stepDelay = stepDelayBase; // Speedup factor is 1 at startup.
        stream_always_show_last_op = Boolean.parseBoolean(properties.getProperty("autoPlayOnIncomingStream"));
    }

    // Save settings
    public void saveProperties (){
        Properties properties = new Properties();
        properties.setProperty("playbackStepDelay", "" + stepDelayBase);
        properties.setProperty("autoPlayOnIncomingStream", "" + stream_always_show_last_op);
        try {
            URL url = getClass().getClassLoader().getResource(Strings.PROPERTIES_FILE_NAME);
            OutputStream outputStream = new FileOutputStream(new File(url.toURI()));
            properties.store(outputStream, Strings.PROJECT_NAME + " user preferences.");
        } catch (Exception e) {
            propertiesFailed(e);
        }
    }
    /*
     * End settings
     */

    /*
     * How to do sound in JavaFX.
     */
    private boolean oooooOOoooOOOooooOOoooed = false;

    public void oooooOOoooOOOooooOOooo (Event e){
        // https://www.youtube.com/watch?v=inli9ukUKIs
        URL resource = getClass().getResource("/assets/oooooOOoooOOOooooOOooo.mp3");
        Media media = new Media(resource.toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
        Main.console.info("GET SPoooooOOoooOOOooooOOoooKED!");
        if (!oooooOOoooOOOooooOOoooed) {
            Button spooky = (Button) e.getSource();
            spooky.setBlendMode(BlendMode.SRC_OVER);
            //https://pixabay.com/en/ghost-white-spooky-scary-ghostly-157985/
            Image img = new Image(getClass().getResourceAsStream("/assets/oooooOOoooOOOooooOOooo.png"));
            spooky.setGraphic(new ImageView(img));
            oooooOOoooOOOooooOOoooed = true;
            window.setTitle("SpoooooOOoooOOOooooOOoookster!");
        }
    }

    //Fulhack
    public OperationPanel getOperationPanel (){
        return operationPanel;
    }

    /**
     * Load an example.
     * 
     * @param algo The Wrapper containing the example.
     */
    public void loadExample (Algorithm algo){
        double[] data = examplesDialog.show(algo.name);
        if (data == null) {
            return;
        }
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
    public void toggleQuietMode (Event e){
        CheckBox cb = (CheckBox) e.getSource();
        Main.console.setQuiet(cb.isSelected());
    }

    public void toggleInformation (Event e){
        CheckBox cb = (CheckBox) e.getSource();
        Main.console.setInfo(cb.isSelected());
    }

    public void toggleError (Event e){
        CheckBox cb = (CheckBox) e.getSource();
        Main.console.setError(cb.isSelected());
    }

    public void toggleDebug (Event e){
        CheckBox cb = (CheckBox) e.getSource();
        Main.console.setDebug(cb.isSelected());
    }
    
    public void clearConsole(){
        Main.console.clear();
    }
    /*
     * Console controls end.
     */

    public void helpPython (){
        WebView wv;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/HelpJava.fxml"));
        fxmlLoader.setController(this);
        Stage root = new Stage();
        root.getIcons().add(new Image(GUI_Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
        root.initModality(Modality.NONE);
        root.setTitle(Strings.PROJECT_NAME + ": Java Help");
        root.initOwner(window);
        BorderPane p = null;
        try {
            p = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        root.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            root.close();
        });
        Scene dialogScene = new Scene(p, window.getWidth(), window.getHeight());
        wv = (WebView) p.getChildren().get(0);
        wv.getEngine().load("https://www.google.se/");
//        wv.getEngine().loadContent("hellp!<br>new line?");
        root.setScene(dialogScene);
        root.show();
    }

    public void helpJava (){
        WebView wv;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/HelpJava.fxml"));
        fxmlLoader.setController(this);
        Stage root = new Stage();
        root.getIcons().add(new Image(GUI_Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
        root.initModality(Modality.NONE);
        root.setTitle(Strings.PROJECT_NAME + ": Java Help");
        root.initOwner(window);
        BorderPane p = null;
        try {
            p = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        root.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            root.close();
        });
        Scene dialogScene = new Scene(p, window.getWidth(), window.getHeight());
        wv = (WebView) p.getChildren().get(0);
        wv.getEngine().load("https://docs.google.com/document/d/1W1MdmZLjabvS3eSahuWZayL1TGBceh2JC3JVVjaEntg/pub");
//        wv.getEngine().loadContent("JavaAnnotationsUserGuideGettingstartedwithannotationsforthe�visualizationtool�.InMavenFirstweneedtosetupthemavendependenciesandcompilerplugin.CopypastethefollowingintoyourProject�pom.xml�.&#60;dependencies&#62;&#60;dependency&#62;&#60;groupId&#62;com.dennisjonsson&#60;/groupId&#62;&#60;artifactId&#62;annotation&#60;/artifactId&#62;&#60;version&#62;1.0-SNAPSHOT&#60;/version&#62;&#60;/dependency&#62;&#60;/dependencies&#62;&#60;build&#62;&#60;plugins&#62;&#60;plugin&#62;&#60;groupId&#62;org.apache.maven.plugins&#60;/groupId&#62;&#60;artifactId&#62;maven-compiler-plugin&#60;/artifactId&#62;&#60;version&#62;3.0&#60;/version&#62;&#60;configuration&#62;&#60;annotationProcessors&#62;&#60;annotationProcessor&#62;com.dennisjonsson.annotation.processor.VisualizeProcessor&#60;/annotationProcessor&#62;&#60;/annotationProcessors&#62;&#60;source&#62;1.8&#60;/source&#62;&#60;target&#62;1.8&#60;/target&#62;&#60;/configuration&#62;&#60;/plugin&#62;&#60;/plugins&#62;&#60;/build&#62;Now,tryandbuildingyourproject.Ifthebuildwassuccessfulyoucanstartusingtheannotationstovisualizeyourdatastructures.Therearefourannotationsyouneedtoapplytoyourcodeinordertoenableavisualization.@SourcePath(path=�path/to/your/project�)Markoneofyourclasseswiththisannotation,preferablyyoumainclass.Thepathshouldspecifytherootfolderofyourproject.@VisualClassAllclassesyouwanttoincludeinavisualizationprogramneedstobemarkedwiththisannotation.@Visualize(abstractType=�array�|�binarytree�|�adjacencymatrix�)Usethisannotationtomarkclassfieldsormethodparameteryouwanttovisualize.Provideyourabstractvisualizationtype,i.e.howyouwantyourdatastructuretobevisualized.");
        root.setScene(dialogScene);
        root.show();
    }
}