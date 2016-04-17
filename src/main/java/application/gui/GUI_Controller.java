package application.gui;

import application.assets.*;
import application.gui.panels.*;
import application.gui.views.*;
import application.model.iModel;
import application.visualization.Visualization;
import io.*;
import io.Communicator.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;

/**
 * This is the Controller of MVC for the visualizer GUI.
 */
public class GUI_Controller implements CommunicatorListener {

    private Visualization              visualization;
    private Stage                      window;
    private final LogStreamManager     lsm;
    private final iModel               model;
    private final SourcePanel          sourceViewer;
    // Connection dialog stuff.
    private final SimpleStringProperty currentlyConnected       = new SimpleStringProperty();
    private final SimpleStringProperty allConnected             = new SimpleStringProperty();
    private FXMLLoader                 connectedLoader;
    private Stage                      connectedView;
    // Settings dialog stuff
    private Stage                      settingsView;
    // Controls
    private boolean                    isPlaying                = false;
    private int                        stepDelaySpeedupFactor   = 1;
    private long                       stepDelayBase            = 1500;
    private long                       stepDelay                = stepDelayBase / stepDelaySpeedupFactor;
    private boolean                    autoPlayOnIncomingStream = true;
    private InterpreterView            interpreterView;
    private OperationPanel             operationPanel;
    private MenuButton                 streamBehaviourMenuButton;

    public GUI_Controller (Visualization visualization, Stage window, iModel model, LogStreamManager lsm, SourcePanel sourceViewer){
        this.visualization = visualization;
        this.window = window;
        this.model = model;
        this.lsm = lsm;
        this.lsm.PRETTY_PRINTING = true;
        this.lsm.setListener(this);
        this.sourceViewer = sourceViewer;
        this.operationPanel = new OperationPanel(this);
        initConnectedPane();
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
        toggleAutorunStream.setSelected(autoPlayOnIncomingStream);
        //Size and show
        settingsView.setWidth(this.window.getWidth() * 0.75);
        settingsView.setHeight(this.window.getHeight() * 0.75);
        settingsView.show();
    }

    private CheckBox toggleAutorunStream;

    public void toggleAutorunStream (){
        autoPlayOnIncomingStream = toggleAutorunStream.isSelected();
        unsavedChanged();
    }

    public void jumpToEndClicked (Event e){
        streamBehaviourMenuButton.setText(">>");
        Main.console.info("Model will always display the latest operation streamed operation.");
        //TODO: Logic
    }

    public void continueClicked (Event e){
        streamBehaviourMenuButton.setText(">");
        Main.console.info("Autoplay will start when a streamed operation has been received.");
        //TODO: Logic
    }

    public void doNothingClicked (Event e){
        streamBehaviourMenuButton.setText("=");
        Main.console.info("Streaming will not force model progression.");
        //TODO: Logic
    }

    public void clearButtonClicked (){
        operationPanel.getItems().clear();
        model.getOperations().clear();
        model.getStructures().clear();
        sourceViewer.setSources(null);
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
        visualization.render();
    }

    /**
     * Step the animation forward
     */
    public boolean stepForwardButtonClicked (){
        if (model.stepForward()) {
            visualization.render();
            updatePanels();
            return true;
        }
        return false;
    }

    /**
     * Step the animation backward
     */
    public void stepBackwardButtonClicked (){
        stopAutoPlay();
        model.stepBackward();
        visualization.render();
        updatePanels();
    }

    private Button speedButton;

    /**
     * Change the animation speed
     */
    public void changeSpeedButtonClicked (){
        stepDelaySpeedupFactor = stepDelaySpeedupFactor * 2 % 255;
        speedButton.setText(stepDelaySpeedupFactor + "x");
        stepDelay = stepDelayBase / stepDelaySpeedupFactor;
        playPauseButtonClicked();
        playPauseButtonClicked();
    }

    public void aboutProgram (){
        Main.console.info("Placeholder: A project by ");
        for (String name : Strings.DEVELOPER_NAMES) {
            Main.console.info(name + ", ");
        }
    }

    public void openInterpreterView (){
        stopAutoPlay(); // Prevent concurrent modification exception.
        interpreterView.show(operationPanel.getItems());
    }

    public void interpretOperationHistory (){
        Main.console.force("TODO: interpretOperationHistory()");
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
     * Jump to the given index.
     * 
     * @param index
     */
    public void goToStep (int index){
        model.goToStep(index);
        visualization.render();
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

    private void initConnectedPane (){
        JGroupCommunicator jgc = (JGroupCommunicator) lsm.getCommunicator();
        connectedLoader = new FXMLLoader(getClass().getResource("/ConnectedView.fxml"));
        connectedView = new Stage();
        connectedView.getIcons().add(new Image(GUI_Controller.class.getResourceAsStream("/assets/icon_connected.png")));
        connectedView.initModality(Modality.APPLICATION_MODAL);
        connectedView.setTitle("Entities View: Channel = \"" + jgc.getChannel() + "\"");
        connectedView.initOwner(this.window);
        SplitPane p = null;
        try {
            p = connectedLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextArea top = (TextArea) connectedLoader.getNamespace().get("connectedEntities");
        top.textProperty().bind(currentlyConnected);
        TextArea bottom = (TextArea) connectedLoader.getNamespace().get("allEntities");
        bottom.textProperty().bind(allConnected);
        connectedView.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            jgc.listenForMemberInfo(false);
            connectedView.close();
        });
        Scene dialogScene = new Scene(p, this.window.getWidth() * 0.75, this.window.getHeight() * 0.75);
        connectedView.setScene(dialogScene);
    }

    public void connectedToChannel (){
        JGroupCommunicator jgc = (JGroupCommunicator) lsm.getCommunicator();
        jgc.listenForMemberInfo(true);
        StringBuilder sb = new StringBuilder();
        for (String s : jgc.getAllMemberStrings()) {
            sb.append(s + "\n");
        }
        allConnected.set(sb.toString());
        //Set size and show
        connectedView.setWidth(this.window.getWidth() * 0.75);
        connectedView.setHeight(this.window.getHeight() * 0.75);
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
            loadFile(source);
        }
    }

    /**
     * Helper function for {@link #openFileChooser() openFileChooser}
     * 
     * @param file
     */
    void loadFile (File file){
        lsm.clearData();
        if (lsm.readLog(file) == false) {
            Main.console.err("Failed to read log: " + file);
            return;
        }
        //Add operations to model and create Render visuals, then draw them.
        model.set(lsm.getKnownVariables(), lsm.getOperations());
        sourceViewer.setSources(lsm.getSources());
        visualization.createVisuals();
        visualization.render();
        //Update operation list
        operationPanel.getItems().setAll(lsm.getOperations());
        updatePanels();
        //Clean lsm
        lsm.clearData();
    }

    @Override
    public void messageReceived (short messageType){
        if (messageType == MavserMessage.MEMBER_INFO) {
            System.out.println("member info!");
            List<String> memberStrings = ((JGroupCommunicator) lsm.getCommunicator()).getMemberStrings();
            StringBuilder sb = new StringBuilder();
            for (String s : memberStrings) {
                sb.append(s + "\n");
            }
            currentlyConnected.set(sb.toString());
            return;
        }
        if (autoPlayOnIncomingStream) {
            model.goToEnd();
        }
        Platform.runLater(new Runnable() {

            @Override
            public void run (){
                if (lsm.getKnownVariables().isEmpty() == false) {
                    model.getStructures().clear();
                    model.getOperations().clear();
                    model.getStructures().putAll(lsm.getKnownVariables());
                    visualization.createVisuals();
                    Main.console.info("Received new AnnotatedVariable(s): " + lsm.getKnownVariables().values());
                }
                if (lsm.getSources() != null) {
                    sourceViewer.setSources(lsm.getSources());
                    Main.console.info("New source(s) received: " + lsm.getSources().keySet());
                }
                operationPanel.getItems().addAll(lsm.getOperations());
                model.getOperations().addAll(lsm.getOperations());
                operationPanel.update(model.getIndex(), true);
                lsm.clearData();
                if (autoPlayOnIncomingStream) {
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
        fc.setInitialFileName("bla");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("OI-Files", "*.oi"), new FileChooser.ExtensionFilter("All Files", "*.*"));
        File target = fc.showSaveDialog(this.window);
        if (target == null) {
            return;
        }
        lsm.setOperations(model.getOperations());
        lsm.setKnownVariables(model.getStructures());
        lsm.setSources(sourceViewer.getSources());
        lsm.printLog(target);
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

    @Override
    public CommunicatorListener getListener (){
        return null; // VisualizerController doesn't have any listeners.
    }

    @SuppressWarnings("unchecked")
    public void loadMainViewFxID (FXMLLoader mainViewLoader){
        ObservableMap<String, Object> namespace = mainViewLoader.getNamespace();
        //Load from main view namespace
        //@formatter:off
        playPauseButton     = (Button) namespace.get("playPauseButton");
        speedButton         = (Button) namespace.get("speedButton");
        streamBehaviourMenuButton = (MenuButton) namespace.get("streamBehaviourMenuButton");
        //@formatter:on
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
        autoPlayOnIncomingStream = Boolean.parseBoolean(properties.getProperty("autoPlayOnIncomingStream"));
    }

    // Save settings
    public void saveProperties (){
        Properties properties = new Properties();
        properties.setProperty("playbackStepDelay", "" + stepDelayBase);
        properties.setProperty("autoPlayOnIncomingStream", "" + autoPlayOnIncomingStream);
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
    private boolean oooooOOoooOOOooooOOoooed = true;
    public void oooooOOoooOOOooooOOooo (Event e){
        // https://www.youtube.com/watch?v=inli9ukUKIs
        URL resource = getClass().getResource("/assets/oooooOOoooOOOooooOOooo.mp3");
        Media media = new Media(resource.toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
        Main.console.info("GET SPoooooOOoooOOOooooOOoooKED!");
        if(!oooooOOoooOOOooooOOoooed){
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
    /*
     * Console controls end.
     */
}