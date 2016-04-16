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
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import wrapper.Operation;
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
    private ListView<Operation>        operationHistory;
    private boolean                    autoPlayOnIncomingStream = true;
    private TextField                  currOpTextField;
    private Label                      totNrOfOpLabel;
    private Label                      settingsSaveState;
    private ProgressBar                opProgress;
    private InterpreterView            interpreterView;

    public GUI_Controller (Visualization visualization, Stage window, iModel model, LogStreamManager lsm, SourcePanel sourceViewer){
        this.visualization = visualization;
        this.window = window;
        this.model = model;
        this.lsm = lsm;
        this.lsm.PRETTY_PRINTING = true;
        this.lsm.setListener(this);
        this.sourceViewer = sourceViewer;
        this.initConnectedPane();
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
//        speedButton.setDisable(true);
        if (autoplayTimeline != null) {
            autoplayTimeline.stop();
        }
        isPlaying = true;
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
    }

    public void stopAutoPlay (){
//        speedButton.setDisable(false);
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
        updateLists();
        visualization.render();
    }

    /**
     * Step the animation forward
     */
    public boolean stepForwardButtonClicked (){
        if (model.stepForward()) {
            visualization.render();
            updateLists();
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
        updateLists();
    }

    private Button speedButton;

    /**
     * Change the animation speed
     */
    public void changeSpeedButtonClicked (){
        stepDelaySpeedupFactor = stepDelaySpeedupFactor * 2 % 31;
        speedButton.setText(stepDelaySpeedupFactor + "x");
        stepDelay = stepDelayBase / stepDelaySpeedupFactor;
        playPauseButtonClicked();
        playPauseButtonClicked();
    }

    public void aboutProgram (){
        Main.console.out("Placeholder: A project by ");
        for (String name : Strings.DEVELOPER_NAMES) {
            Main.console.out(name + ", ");
        }
    }

    public void openInterpreterView (){
        stopAutoPlay(); // Prevent concurrent modification exception.
        interpreterView.show(this.operationHistory.getItems());
    }

    public void interpretOperationHistory (){
        System.out.println("TODO: interpretOperationHistory ()");
    }

    private void updateLists (){
        Platform.runLater(new Runnable() {

            @Override
            public void run (){
                int index = model.getIndex();
                model.getCurrentStep().getLastOp();
                sourceViewer.show(model.getCurrentStep().getLastOp());
                updateOperationList(index);
            }
        });
    }

    private void updateOperationList (int index){
        //List selection and position
        operationHistory.getSelectionModel().select(index);
        operationHistory.getFocusModel().focus(index);
        operationHistory.scrollTo(index - 1);
        currOpTextField.setText("" + (index));
        //Text
        totNrOfOpLabel.setText("/ " + operationHistory.getItems().size());
        //TODO: Progress bar   
        opProgress.setProgress(operationHistory.getItems().size() == 0 ? 0 : index / operationHistory.getItems().size());
    }

    // TODO: Implement detailed inspection of operation
    public void inspectSelection (){
        Main.console.err("Not implemented.");
    }

    public void inputGoToSelecton (){
        int lineOffset;
        try {
            currOpTextField.setStyle("-fx-control-inner-background: white;");
            lineOffset = Integer.parseInt(currOpTextField.getText());
        } catch (Exception exc) {
            // NaN
            currOpTextField.setStyle("-fx-control-inner-background: #C40000;");
            return;
        }
        if (lineOffset < 0) {
            currOpTextField.setText("invalid");
            currOpTextField.selectAll();
            return;
        }
        goToOpIndex(lineOffset);
    }

    public void gotoSelection (){
        int lineOffset = operationHistory.getSelectionModel().getSelectedIndex();
        goToOpIndex(lineOffset);
    }

    @SuppressWarnings("unchecked")
    public void doubleClickGoTo (MouseEvent click){
        if (click.getClickCount() == 2) {
            goToOpIndex(((ListView<Operation>) click.getSource()).getSelectionModel().getSelectedIndex());
        }
    }

    private void goToOpIndex (int index){
        model.goToStep(index);
        visualization.render();
        currOpTextField.setText("" + index);
        updateLists();
    }

    private DecimalFormat df;

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
            return;
        }
        //Add operations to model and create Render visuals, then draw them.
        model.set(lsm.getKnownVariables(), lsm.getOperations());
        visualization.createVisuals();
        visualization.render();
        //Update operation list
        operationHistory.getItems().clear();
        operationHistory.getItems().addAll(lsm.getOperations());
        sourceViewer.setSources(lsm.getSources());
        updateLists();
        //Clean lsm
        lsm.clearData();
    }

    @Override
    public void messageReceived (short messageType){
        if (messageType == MavserMessage.MEMBER_INFO) {
            List<String> memberStrings = ((JGroupCommunicator) lsm.getCommunicator()).getMemberStrings();
            StringBuilder sb = new StringBuilder();
            for (String s : memberStrings) {
                sb.append(s + "\n");
            }
            currentlyConnected.set(sb.toString());
            return;
        }
        Platform.runLater(new Runnable() {

            @Override
            public void run (){
                if (autoPlayOnIncomingStream) {
                    model.goToEnd();
                }
                if (lsm.getKnownVariables().isEmpty() == false) {
                    model.getStructures().putAll(lsm.getKnownVariables());
                    visualization.createVisuals();
                }
                sourceViewer.setSources(lsm.getSources());
                operationHistory.getItems().addAll(lsm.getOperations());
                totNrOfOpLabel.setText("/ " + operationHistory.getItems().size());
                lsm.clearData();
                if (autoPlayOnIncomingStream) {
                    stepForwardButtonClicked();
                }
                else {
                    updateLists();
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
        operationHistory    = (ListView<Operation>) namespace.get("operationHistory");
        playPauseButton     = (Button) namespace.get("playPauseButton");
        currOpTextField     = (TextField) namespace.get("currOpTextField");
        totNrOfOpLabel      = (Label) namespace.get("totNrOfOpLabel");
        speedButton         = (Button) namespace.get("speedButton");
        opProgress          = (ProgressBar) namespace.get("opProgress");
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
    public void oooooOOoooOOOooooOOooo (){
        // https://www.youtube.com/watch?v=inli9ukUKIs
        URL resource = getClass().getResource("/assets/oooooOOoooOOOooooOOooo.mp3");
        Media media = new Media(resource.toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
        window.setTitle("SpoooooOOoooOOOooooOOoookster!");
        Main.console.out("GET SPoooooOOoooOOOooooOOoooKED!");
    }
}