package application.Visualizer;

import application.model.iModel;
import application.view.Visualization;
import assets.DefaultProperties;
import assets.Strings;
import interpreter.Interpreter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import manager.CommunicatorListener;
import manager.JGroupCommunicator;
import manager.LogStreamManager;
import manager.Communicator.MavserMessage;
import wrapper.Operation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;

/**
 * This is the Controller of MVC for the visualizer GUI.
 */
public class VisualizerController implements CommunicatorListener{

    private Visualization visualization;
    private Stage window;
    private final LogStreamManager lsm;
    private final Interpreter interpreter;
    private final iModel model;

    //Connection dialog stuff.
    private final SimpleStringProperty currentlyConnected = new SimpleStringProperty();
    private final SimpleStringProperty allConnected = new SimpleStringProperty();
    private FXMLLoader connectedLoader;
    private Stage connectedDialog;
    
    //Settings dialog stuff
    private Stage settingsView;
    
    // Controls
    private boolean isPlaying = false;
    private int stepDelaySpeedupFactor = 1;
    private long stepDelayBase = 1500;
    private long stepDelay = stepDelayBase/stepDelaySpeedupFactor;
    private ListView<Operation> operationHistory;

    private boolean autoPlayOnIncomingStream = true;
    
    public VisualizerController(Visualization visualization, Stage window, iModel model, LogStreamManager lsm) {
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

    public void showSettings(){
        settingsView.setWidth(this.window.getWidth()*0.75);
        settingsView.setHeight(this.window.getHeight()*0.75);
        
        //Playback speed
        perSecField.setText(df.format(1000.0/stepDelayBase));
        timeBetweenField.setText(df.format(stepDelayBase));
        
    	toggleAutorunStream.setSelected(autoPlayOnIncomingStream);
    			
        settingsView.show();
    }
    
    private String translateInterpreterRoutine(){
    
		switch(interpreter.getHighOrderRoutine()){
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
    public void toggleAutorunStream(){
    	autoPlayOnIncomingStream = toggleAutorunStream.isSelected();
    	unsavedChanged();
    }
    
    /**
     * Starts playing or pause the AV animation.
     */
	private Button playPauseButton;
	private Thread autoPlayThread;
    public void playPauseButtonClicked(){
        if(!isPlaying) {
        	startAutoPlay();
        }
        else {
        	stopAutoPlay();
        }
    }
    
	public void startAutoPlay(){
        playPauseButton.setText("Pause");
        if(autoPlayThread!=null){
        	autoPlayThread.interrupt();
        }
        isPlaying = true;
        autoPlayThread = new Thread()
		{
		    public void run() {
		    	while(isPlaying){
		    		if(stepForwardButtonClicked() == false){
		    			stopAutoPlay();
		    		}
		    		try {
						sleep(stepDelay);
					} catch (InterruptedException e) {}
		    	}
		    }
		};
		
        autoPlayThread.start();
 
	}
	
	public void stopAutoPlay(){
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				playPauseButton.setText("Play");
			    isPlaying = false;
			    if (autoPlayThread != null){
				    autoPlayThread.interrupt();
			    }
			}
		});
	}
	


    /**
     * Restart the AV animation.
     */
    public void restartButtonClicked(){
    	stopAutoPlay();
        model.reset();
        updateOperationList();
        visualization.render();
    }

    /**
     * Step the animation forward
     */
    public boolean stepForwardButtonClicked(){
        if(model.stepForward()){
            visualization.render();
            updateOperationList();
            return true;
        }
        return false;
    }

    /**
     * Step the animation backward
     */
    public void stepBackwardButtonClicked(){
    	stopAutoPlay();
        model.stepBackward();
        visualization.render();
        updateOperationList();
    }

    /**
     * Change the animation speed
     */
    public void changeSpeedButtonClicked(Event e){
        stepDelaySpeedupFactor = stepDelaySpeedupFactor*2 % 31; // possible values: 1, 2, 4, 8, 16
        ((Button) e.getSource()).setText(stepDelaySpeedupFactor + "x");
        stepDelay = stepDelayBase/stepDelaySpeedupFactor;
    }

    public void aboutProgram(){
        System.out.print("Placeholder: A project by " );
        for (String name : Strings.DEVELOPER_NAMES) {
            System.out.print(name + ", ");
        }
        System.out.println();
        
    }
    
    public void openInterpreterView(){
    	stopAutoPlay();    	//Prevent concurrent modification exception.
    	
    	//Load settings
    	newRoutine = interpreter.getHighOrderRoutine();
    	interpreterRoutineChooser.getSelectionModel().select(translateInterpreterRoutine());
    	
    	//Setup
    	interpreterBefore.getItems().setAll(operationHistory.getItems());
    	beforeCount.setText(""+interpreterBefore.getItems().size());
    	interpreterAfter.getItems().clear();

    	interpreterView.show();
    }
    
    public void interpretOperationHistory(){
    	 stopAutoPlay();
 		 interpreter.consolidate(operationHistory.getItems());
 		 updateOperationList();
    }
    
    private void updateOperationList(){
    	Platform.runLater(new Runnable(){
			@Override
			public void run() {
		    	int index = model.getIndex();
		        operationHistory.getSelectionModel().select(index);
		        operationHistory.getFocusModel().focus(index);
		        operationHistory.scrollTo(index-1);
		        
		        currOpTextField.setText("" + (index));
		 		totNrOfOpLabel.setText("/ " + operationHistory.getItems().size());
			}	
    	});
    }
    
    
    //TODO: Implement detailed inspection of operation
    public void inspectSelection(){
    	System.out.println(operationHistory.getSelectionModel().getSelectedItem());
    }

    public void inputGoToSelecton(){
        int lineNr;

        try{
            currOpTextField.setStyle("-fx-control-inner-background: white;");
            lineNr = Integer.parseInt(currOpTextField.getText());
        } catch (Exception exc){
            // NaN
            currOpTextField.setStyle("-fx-control-inner-background: #C40000;");
            return;
        }

        if(lineNr <= 0){
            currOpTextField.setText("invalid");
            currOpTextField.selectAll();
            return;
        }

        model.goToStep(lineNr-1);
        visualization.render();
        currOpTextField.setText(""+lineNr);
        updateOperationList();
    }

    public void gotoSelection(){
        int lineOffset = operationHistory.getSelectionModel().getSelectedIndex();
    	model.goToStep(lineOffset);
        visualization.render();
        currOpTextField.setText(""+(lineOffset+1));
    }

    private DecimalFormat df;
    @SuppressWarnings("unchecked")
	private void initSettingsPane(){
    	df = new DecimalFormat("#.####"); 
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/SettingsView.fxml"));
        fxmlLoader.setController(this);
        settingsView = new Stage();
        settingsView.getIcons().add(new Image(VisualizerController.class.getResourceAsStream("/assets/icon_settings.png")));
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
        
        //Get namespace items
        	//Save state label
	        settingsSaveState = (Label) fxmlLoader.getNamespace().get("settingsSaveState");
	        
	        //Playpack speed
	        timeBetweenField = (TextField) fxmlLoader.getNamespace().get("timeBetweenField");
	        perSecField = (TextField) fxmlLoader.getNamespace().get("perSecField");
	        
	        toggleAutorunStream = (CheckBox) fxmlLoader.getNamespace().get("toggleAutorunStream");
	        
		p.setPrefWidth(this.window.getWidth()*0.75);
		p.setPrefHeight(this.window.getHeight()*0.75);
	    Scene dialogScene = new Scene(p, this.window.getWidth()*0.75, this.window.getHeight()*0.75);
        settingsView.setScene(dialogScene);
    }
    
    private Stage interpreterView;
    private ListView<Operation>interpreterBefore, interpreterAfter;
    private TextField beforeCount;
    @SuppressWarnings("unchecked")
	private void initInterpreterPane(){
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/InterpreterView.fxml"));
        fxmlLoader.setController(this);
        interpreterView = new Stage();
        interpreterView.getIcons().add(new Image(VisualizerController.class.getResourceAsStream("/assets/icon_interpreter.png")));
        interpreterView.initModality(Modality.APPLICATION_MODAL);
        interpreterView.setTitle(Strings.PROJECT_NAME + ": Interpreter");
        interpreterView.initOwner(this.window);
        
        GridPane p = null;
		try {
			p = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

        
        //Buttons
        interpreterView.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            discardInterpreted();
        });
        
        //Get namespace items
	        interpreterRoutineChooser = (ChoiceBox<String>) fxmlLoader.getNamespace().get("routineChooser");
	        interpreterRoutineChooser.getSelectionModel().selectedItemProperty().addListener(event -> {
	        	interpreterRoutineChooser(); //Cant set onAction i SceneBuilder for some reason.
	        });
	        interpreterRoutineChooser.setItems(FXCollections.observableArrayList("Discard","Flush Set", "Keep Set" 
	        																	,"Deconstruct", "Abort" //TODO: Implement in Interpreter! Comment this line!
	        																	));
	        
	        interpreterBefore = (ListView<Operation>) fxmlLoader.getNamespace().get("interpreterBefore");
	        interpreterAfter = (ListView<Operation>) fxmlLoader.getNamespace().get("interpreterAfter");
	        
	        beforeCount = (TextField) fxmlLoader.getNamespace().get("beforeCount");
	        TextField afterCount = (TextField) fxmlLoader.getNamespace().get("afterCount");

        	List<Operation> afterItems = interpreterAfter.getItems();
	        
	        Button interpret = (Button) fxmlLoader.getNamespace().get("interpret");
	        interpret.setOnAction(event ->{
	        	
	        	afterItems.clear();
	        	afterItems.addAll(interpreterBefore.getItems());
	        	
	        	interpreter.consolidate(afterItems);
	        	afterCount.setText(""+afterItems.size());
	        });
	        
	        Button moveToBefore = (Button) fxmlLoader.getNamespace().get("moveToBefore");
	        moveToBefore.setOnAction(event ->{
	        	interpreterBefore.getItems().setAll(afterItems);
	        	beforeCount.setText(""+interpreterBefore.getItems().size());
	        });
	    
	    p.setPrefWidth(this.window.getWidth()*0.75);
	    p.setPrefHeight(this.window.getHeight()*0.75);
	    Scene dialogScene = new Scene(p, this.window.getWidth()*0.75, this.window.getHeight()*0.75);
        interpreterView.setScene(dialogScene);
    }
    
    public void keepInterpreted(){
    	operationHistory.getItems().setAll(interpreterAfter.getItems());
    	updateOperationList();
    	saveProperties();
    	interpreterView.close();
    }
    
    public void discardInterpreted(){
    	saveProperties();
    	interpreterView.close();
    }
    
    private void initConnectedPane(){
    	JGroupCommunicator jgc = (JGroupCommunicator) lsm.getCommunicator();
    	connectedLoader = new FXMLLoader(getClass().getResource("/ConnectedView.fxml"));
    	connectedDialog = new Stage();
    	connectedDialog.getIcons().add(new Image(VisualizerController.class.getResourceAsStream("/assets/icon_connected.png")));
        connectedDialog.initModality(Modality.APPLICATION_MODAL);
        connectedDialog.setTitle("Entities View: Channel = \"" + jgc.getChannel() + "\"");
        connectedDialog.initOwner(this.window);
        
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
		
        connectedDialog.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            jgc.listenForMemberInfo(false);
            connectedDialog.close();
        });

        Scene dialogScene = new Scene(p, this.window.getWidth()*0.75, this.window.getHeight()*0.75);
        connectedDialog.setScene(dialogScene);
    }
    public void connectedToChannel(){
    	JGroupCommunicator jgc = (JGroupCommunicator) lsm.getCommunicator();
    	jgc.listenForMemberInfo(true);
        
    	StringBuilder sb = new StringBuilder();
    	for(String s : jgc.getAllMemberStrings()){
    		sb.append(s + "\n");
    	}
    	allConnected.set(sb.toString());
    	
        connectedDialog.setWidth(this.window.getWidth()*0.75);
        connectedDialog.setHeight(this.window.getHeight()*0.75);
        connectedDialog.show();
    }

    /**
     * Used for closing the GUI properly.
     */
    public void closeProgram(){
        lsm.close();
        window.close();
    }

    /**
     * Used for choosing a file to Visualize.
     */
    public void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open OI-File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = fileChooser.showOpenDialog(window);

        if (file != null){ //Null is returned if the users pressed Cancel.
            setFile(file);
        } 

    }

    /**
     * Helper function for {@link #openFileChooser() openFileChooser}
     * @param file
     */
    void setFile(File file) {
    	lsm.clearData();
    	
        if(lsm.readLog(file) == false){
        	return;
        }
        
        model.set(lsm.getKnownVariables(), lsm.getOperations());
        operationHistory.getItems().clear();
        operationHistory.getItems().addAll(lsm.getOperations());
        visualization.render();
        totNrOfOpLabel.setText("/ " + operationHistory.getItems().size());
        updateOperationList();
    }

	@Override
	public void messageReceived(short messageType) {
		if(messageType == MavserMessage.MEMBER_INFO){
	    	List<String> memberStrings = ((JGroupCommunicator) lsm.getCommunicator()).getMemberStrings();
	    	StringBuilder sb = new StringBuilder();
	    	for (String s : memberStrings){
	    		sb.append(s + "\n");
	    	}
	    	currentlyConnected.set(sb.toString());
			return;
		}
		
        Platform.runLater(new Runnable(){

			@Override
			public void run() {
				if (autoPlayOnIncomingStream){
					model.goToEnd();
				}
				operationHistory.getItems().addAll(lsm.getOperations());
                totNrOfOpLabel.setText("/ " + operationHistory.getItems().size());
				lsm.clearData();
				if (autoPlayOnIncomingStream){
					stepForwardButtonClicked();
				} else {
					updateOperationList();
				}
			}        	
        });
		
	}

	public void openDestinationChooser(){
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("Choose Output Directory");
		File outputPath = dc.showDialog(this.window);
		if (outputPath == null){
			return;
		}
		
		lsm.setOperations(model.getOperations());
		lsm.setKnownVariables(model.getStructures());
		lsm.PRETTY_PRINTING = true;
		lsm.printLog(outputPath);
		lsm.PRETTY_PRINTING = false;
	}
	
    public void propertiesFailed(Exception exception){
    	if(exception != null){
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
	public CommunicatorListener getListener() {
		return null; //VisualizerController doesn't have any listeners.
	}

	
	//Load components from the main view.
    private TextField currOpTextField;
	private Label totNrOfOpLabel;
	private Label settingsSaveState;
	@SuppressWarnings("unchecked")
	public void loadMainViewFxID(FXMLLoader mainViewLoader) {
		ObservableMap<String, Object> mainViewNameSpace = mainViewLoader.getNamespace();
		
		operationHistory = (ListView<Operation>) mainViewNameSpace.get("operationHistory");
        playPauseButton = (Button) mainViewNameSpace.get("playPauseButton");
        currOpTextField = (TextField) mainViewNameSpace.get("currOpTextField");
        totNrOfOpLabel = (Label) mainViewNameSpace.get("totNrOfOpLabel");
	}
	
	
		/*				  
		 * SETTINGS PANEL
		 */
		private boolean settingsChanged = false;
		
		//Commit changes to file.
		public void saveSettings(){
			if(settingsChanged){
				saveProperties();
				noUnsavedChanges();
			}
			settingsView.close();
		}
		
		//Reload settings from file.
		public void revertSettings(){
			if(settingsChanged){
				loadProperties();
				noUnsavedChanges();
			}
			settingsView.close();
		}
		
		private void noUnsavedChanges(){
			settingsChanged = false;
			settingsSaveState.setText("No unsaved changes.");
			settingsSaveState.setTextFill(Color.web("#00c8ff"));
		}
		
		private void unsavedChanged(){
			settingsChanged = true;
			settingsSaveState.setText("Unsaved changes.");
			settingsSaveState.setTextFill(Color.web("#ff0000"));
		}
		
		//Playback speed
		private TextField perSecField;
		public void setPlayBackOpsPerSec(Event e){
	        long newSpeed;
	        
	        try{
	            perSecField.setStyle("-fx-control-inner-background: white;");
	        	newSpeed = Long.parseLong(perSecField.getText());
	        } catch (Exception exc){
	            // NaN
	            perSecField.setStyle("-fx-control-inner-background: #C40000;");
	        	return;
	        }
	        
	        if(newSpeed <= 0){
	        	perSecField.setText("invalid");
	            perSecField.selectAll();
	            return;
	        }
	
	        //Valid input. Change other button and speed variable.
	        perSecField.setText(df.format(newSpeed));//BLA
	        timeBetweenField.setText(df.format((1000.0/newSpeed)));
	        stepDelayBase = (1000L/newSpeed);
	        stepDelay = stepDelayBase/stepDelaySpeedupFactor;
	        unsavedChanged();
		}
		
		private TextField timeBetweenField;
		public void setPlaybackTimeBetweenOperations(Event e){
	        long newSpeed;
	        
	        try{
	            perSecField.setStyle("-fx-control-inner-background: white;");
	        	newSpeed = Long.parseLong(timeBetweenField.getText());
	        } catch (Exception exc){
	            // NaN
	            perSecField.setStyle("-fx-control-inner-background: #C40000;");
	        	return;
	        }
	
	        if(newSpeed < 0){
	        	timeBetweenField.setText("invalid");
	            perSecField.selectAll();
	        	return;
	        }
	        
	        //Valid input. Change other button and speed variable.
	        perSecField.setText(df.format(1000.0/newSpeed));
	        timeBetweenField.setText(df.format(newSpeed));
	        stepDelayBase = newSpeed;
	        stepDelay = stepDelayBase/stepDelaySpeedupFactor;
	        unsavedChanged();
		}
	    
	    public Properties tryLoadProperties(){
	    	InputStream inputStream = getClass().getClassLoader().getResourceAsStream(Strings.PROPERTIES_FILE_NAME);

	    	if(inputStream == null){
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
	    
	    //Load settings
	    public void loadProperties(){
			Properties properties = tryLoadProperties();
			
			interpreter.setHighOrderRoutine(Integer.parseInt(properties.getProperty("highOrderRoutine")));
			stepDelayBase = Long.parseLong(properties.getProperty("playbackStepDelay")); stepDelay = stepDelayBase; //Speedup factor is 1 at startup.
			autoPlayOnIncomingStream = Boolean.parseBoolean(properties.getProperty("autoPlayOnIncomingStream"));
	    }
	    
	    //Save settings
	    public void saveProperties(){
			Properties properties = new Properties();
			
			properties.setProperty("playbackStepDelay", ""+stepDelayBase);
			properties.setProperty("autoPlayOnIncomingStream", ""+autoPlayOnIncomingStream);
			
			properties.setProperty("highOrderRoutine", ""+interpreter.getHighOrderRoutine());
			
			try {
				URL url = getClass().getClassLoader().getResource(Strings.PROPERTIES_FILE_NAME);
				OutputStream outputStream = new FileOutputStream(new File(url.toURI()));
				properties.store(outputStream, Strings.PROJECT_NAME + " user preferences.");
			} catch (Exception e) {
				propertiesFailed(e);
			}
	    }
	    
	    private ChoiceBox<String> interpreterRoutineChooser;
	    private int newRoutine = -1;
	    public void interpreterRoutineChooser(){
	    	String choice = interpreterRoutineChooser.getSelectionModel().getSelectedItem();
	    	
	    	switch(choice){
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
	    	
	    	if (newRoutine != interpreter.getHighOrderRoutine()){
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
		//https://www.youtube.com/watch?v=inli9ukUKIs
		
	    URL resource = getClass().getResource("/assets/oooooOOoooOOOooooOOooo.mp3");
	    Media media = new Media(resource.toString());
	    MediaPlayer mediaPlayer = new MediaPlayer(media);
	    mediaPlayer.play();
	    window.setTitle("SpoooooOOoooOOOooooOOoookster!");
	    System.out.println("GET SPoooooOOoooOOOooooOOoooKED!");
		
	}
}