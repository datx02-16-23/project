package application.gui;

import application.assets.*;
import application.model.iModel;
import application.visualization.Visualization;
import interpreter.Interpreter;
import io.*;
import io.Communicator.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import wrapper.Operation;
import wrapper.operations.*;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is the Controller of MVC for the visualizer GUI.
 */
public class GUI_Controller implements CommunicatorListener {

    private Visualization              visualization;
    private Stage                      window;
    private final LogStreamManager     lsm;
    private final Interpreter          interpreter;
    private final iModel               model;
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

    public GUI_Controller (Visualization visualization, Stage window, iModel model, LogStreamManager lsm){
        this.visualization = visualization;
        this.window = window;
        this.model = model;
        this.lsm = lsm;
        this.lsm.setListener(this);
        this.interpreter = new Interpreter();
        initConnectedPane();
        initSettingsPane();
        initInterpreterPane();
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

    private String translateInterpreterRoutine (){
        switch (interpreter.getHighOrderRoutine()) {
            case Interpreter.DISCARD:
                return "Discard";
            case Interpreter.FLUSH_SET_ADD_HIGH:
                return "Flush Set";
            case Interpreter.KEEP_SET_ADD_HIGH:
                return "Keep Set";
            case Interpreter.DECONSTRUCT:
                return "Deconstruct";
            case Interpreter.ABORT:
                return "Abort";
            default:
                throw new IllegalArgumentException();
        }
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
    private Thread autoPlayThread;

    public void playPauseButtonClicked (){
        if (!isPlaying) {
            startAutoPlay();
        }
        else {
            stopAutoPlay();
        }
    }

    public void startAutoPlay (){
        playPauseButton.setText("Pause");
        if (autoPlayThread != null) {
            autoPlayThread.interrupt();
        }
        isPlaying = true;
        autoPlayThread = new Thread() {

            public void run (){
                while(isPlaying) {
                    if (stepForwardButtonClicked() == false) {
                        stopAutoPlay();
                    }
                    try {
                        sleep(stepDelay);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        autoPlayThread.start();
    }

    public void stopAutoPlay (){
        Platform.runLater(new Runnable() {

            @Override
            public void run (){
                playPauseButton.setText("Play");
                isPlaying = false;
                if (autoPlayThread != null) {
                    autoPlayThread.interrupt();
                }
            }
        });
    }

    /**
     * Restart the AV animation.
     */
    public void restartButtonClicked (){
        stopAutoPlay();
        model.reset();
        updateOperationList();
        visualization.render();
    }

    /**
     * Step the animation forward
     */
    public boolean stepForwardButtonClicked (){
        if (model.stepForward()) {
            visualization.render();
            updateOperationList();
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
        updateOperationList();
    }

    private Button speedButton;

    /**
     * Change the animation speed
     */
    public void changeSpeedButtonClicked (){
        stepDelaySpeedupFactor = stepDelaySpeedupFactor * 2 % 31;
        speedButton.setText(stepDelaySpeedupFactor + "x");
        stepDelay = stepDelayBase / stepDelaySpeedupFactor;
    }

    public void aboutProgram (){
        System.out.print("Placeholder: A project by ");
        for (String name : Strings.DEVELOPER_NAMES) {
            System.out.print(name + ", ");
        }
        System.out.println();
    }

    public void openInterpreterView (){
        stopAutoPlay(); // Prevent concurrent modification exception.
        // Load settings
        newRoutine = interpreter.getHighOrderRoutine();
        interpreterRoutineChooser.getSelectionModel().select(translateInterpreterRoutine());
        // Setup
        interpreterBefore.getItems().setAll(operationHistory.getItems());
        beforeCount.setText("" + interpreterBefore.getItems().size());
        interpreterAfter.getItems().clear();
        afterCount.setText("0");
        setTestCases();
        //Set size and show
        interpreterView.setWidth(this.window.getWidth() * 0.75);
        interpreterView.setHeight(this.window.getHeight() * 0.75);
        interpreterView.show();
    }

    private Map<String, Object> interpreterViewNamespace;

    private void setTestCases (){
        List<OperationType> selectedTypes = interpreter.getTestCases();
        // TODO: Get and set all of them automatically
        // Swap
        CheckBox swap = (CheckBox) interpreterViewNamespace.get("swap");
        swap.setSelected(selectedTypes.contains(OperationType.swap));
    }

    public void interpretOperationHistory (){
        stopAutoPlay();
        interpreter.consolidate(operationHistory.getItems());
        model.setOperations(operationHistory.getItems());
        updateOperationList();
    }

    private void updateOperationList (){
        Platform.runLater(new Runnable() {

            @Override
            public void run (){
                int index = model.getIndex();
                operationHistory.getSelectionModel().select(index);
                operationHistory.getFocusModel().focus(index);
                operationHistory.scrollTo(index - 1);
                currOpTextField.setText("" + (index));
                totNrOfOpLabel.setText("/ " + operationHistory.getItems().size());
            }
        });
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
        updateOperationList();
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

    private Stage               interpreterView;
    private ListView<Operation> interpreterBefore, interpreterAfter;
    private TextField           beforeCount, afterCount;

    @SuppressWarnings("unchecked")
    private void initInterpreterPane (){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/InterpreterView.fxml"));
        fxmlLoader.setController(this);
        interpreterView = new Stage();
        interpreterView.getIcons().add(new Image(GUI_Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
        interpreterView.initModality(Modality.APPLICATION_MODAL);
        interpreterView.setTitle(Strings.PROJECT_NAME + ": Interpreter");
        interpreterView.initOwner(this.window);
        GridPane p = null;
        try {
            p = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Buttons
        interpreterView.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            discardInterpreted();
        });
        // Get namespace items
        // High order routine
        interpreterRoutineChooser = (ChoiceBox<String>) fxmlLoader.getNamespace().get("routineChooser");
        interpreterRoutineChooser.getSelectionModel().selectedItemProperty().addListener(event -> {
            interpreterRoutineChooser();
        });
        interpreterRoutineChooser.setItems(FXCollections.observableArrayList("Discard", "Flush Set", "Keep Set", "Deconstruct", "Abort"));
        interpreterViewNamespace = fxmlLoader.getNamespace();
        // Lists
        interpreterBefore = (ListView<Operation>) interpreterViewNamespace.get("interpreterBefore");
        interpreterAfter = (ListView<Operation>) interpreterViewNamespace.get("interpreterAfter");
        beforeCount = (TextField) interpreterViewNamespace.get("beforeCount");
        afterCount = (TextField) interpreterViewNamespace.get("afterCount");
        List<Operation> afterItems = interpreterAfter.getItems();
        // Interpret button
        Button interpret = (Button) fxmlLoader.getNamespace().get("interpret");
        interpret.setOnAction(event -> {
            afterItems.clear();
            afterItems.addAll(interpreterBefore.getItems());
            interpreter.consolidate(afterItems);
            afterCount.setText("" + afterItems.size());
        });
        // <-- button
        Button moveToBefore = (Button) fxmlLoader.getNamespace().get("moveToBefore");
        moveToBefore.setOnAction(event -> {
            if (afterItems.isEmpty() == false) {
                interpreterBefore.getItems().setAll(afterItems);
                beforeCount.setText("" + interpreterBefore.getItems().size());
                afterItems.clear();
                afterCount.setText("0");
            }
        });
        // Listeners for TestCase buttons
        CheckBox swap = (CheckBox) fxmlLoader.getNamespace().get("swap");
        swap.setOnAction(event -> {
            if (swap.isSelected()) {
                interpreter.addTestCase(OperationType.swap);
            }
            else {
                interpreter.removeTestCase(OperationType.swap, new OP_Swap());
            }
        });
        p.setPrefWidth(this.window.getWidth() * 0.75);
        p.setPrefHeight(this.window.getHeight() * 0.75);
        Scene dialogScene = new Scene(p, this.window.getWidth() * 0.75, this.window.getHeight() * 0.75);
        interpreterView.setScene(dialogScene);
    }

    public void keepInterpreted (){
        operationHistory.getItems().setAll(interpreterAfter.getItems());
        model.setOperations(interpreterAfter.getItems());
        updateOperationList();
        saveProperties();
        interpreterView.close();
    }

    public void discardInterpreted (){
        saveProperties();
        interpreterView.close();
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open OI-File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = fileChooser.showOpenDialog(window);
        if (file != null) { // Null is returned if the users pressed Cancel.
            setFile(file);
        }
    }

    /**
     * Helper function for {@link #openFileChooser() openFileChooser}
     * 
     * @param file
     */
    void setFile (File file){
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
        updateOperationList();
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
                operationHistory.getItems().addAll(lsm.getOperations());
                totNrOfOpLabel.setText("/ " + operationHistory.getItems().size());
                lsm.clearData();
                if (autoPlayOnIncomingStream) {
                    stepForwardButtonClicked();
                }
                else {
                    updateOperationList();
                }
            }
        });
    }

    public void openDestinationChooser (){
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Output Directory");
        File outputPath = dc.showDialog(this.window);
        if (outputPath == null) {
            return;
        }
        lsm.setOperations(model.getOperations());
        lsm.setKnownVariables(model.getStructures());
        lsm.PRETTY_PRINTING = true;
        lsm.printLog(outputPath);
        lsm.PRETTY_PRINTING = false;
    }

    public void propertiesFailed (Exception exception){
        if (exception != null) {
            System.err.println(exception.getMessage());
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

    // Load components from the main view.
    private TextField currOpTextField;
    private Label     totNrOfOpLabel;
    private Label     settingsSaveState;

    @SuppressWarnings("unchecked")
    public void loadMainViewFxID (FXMLLoader mainViewLoader){
        ObservableMap<String, Object> mainViewNameSpace = mainViewLoader.getNamespace();
        operationHistory = (ListView<Operation>) mainViewNameSpace.get("operationHistory");
        playPauseButton = (Button) mainViewNameSpace.get("playPauseButton");
        currOpTextField = (TextField) mainViewNameSpace.get("currOpTextField");
        totNrOfOpLabel = (Label) mainViewNameSpace.get("totNrOfOpLabel");
        speedButton = (Button) mainViewNameSpace.get("speedButton");
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
            System.err.println("Failed to open properties file.");
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
            System.err.println("Property file I/O failed.");
            return DefaultProperties.get();
        }
    }

    // Load settings
    public void loadProperties (){
        Properties properties = tryLoadProperties();
        interpreter.setHighOrderRoutine(Integer.parseInt(properties.getProperty("highOrderRoutine")));
        stepDelayBase = Long.parseLong(properties.getProperty("playbackStepDelay"));
        stepDelay = stepDelayBase; // Speedup factor is 1 at startup.
        autoPlayOnIncomingStream = Boolean.parseBoolean(properties.getProperty("autoPlayOnIncomingStream"));
    }

    // Save settings
    public void saveProperties (){
        Properties properties = new Properties();
        properties.setProperty("playbackStepDelay", "" + stepDelayBase);
        properties.setProperty("autoPlayOnIncomingStream", "" + autoPlayOnIncomingStream);
        properties.setProperty("highOrderRoutine", "" + interpreter.getHighOrderRoutine());
        try {
            URL url = getClass().getClassLoader().getResource(Strings.PROPERTIES_FILE_NAME);
            OutputStream outputStream = new FileOutputStream(new File(url.toURI()));
            properties.store(outputStream, Strings.PROJECT_NAME + " user preferences.");
        } catch (Exception e) {
            propertiesFailed(e);
        }
    }

    private ChoiceBox<String> interpreterRoutineChooser;
    private int               newRoutine = -1;

    public void interpreterRoutineChooser (){
        String choice = interpreterRoutineChooser.getSelectionModel().getSelectedItem();
        switch (choice) {
            case "Discard":
                newRoutine = Interpreter.DISCARD;
                break;
            case "Flush Set":
                newRoutine = Interpreter.FLUSH_SET_ADD_HIGH;
                break;
            case "Keep Set":
                newRoutine = Interpreter.KEEP_SET_ADD_HIGH;
                break;
            case "Deconstruct":
                newRoutine = Interpreter.DECONSTRUCT;
                break;
            case "Abort":
                newRoutine = Interpreter.ABORT;
                break;
        }
        if (newRoutine != interpreter.getHighOrderRoutine()) {
            interpreter.setHighOrderRoutine(newRoutine);
            saveProperties();
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
        System.out.println("GET SPoooooOOoooOOOooooOOoooKED!");
    }
}