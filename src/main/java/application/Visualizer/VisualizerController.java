package application.Visualizer;

import application.model.iModel;
import application.view.Visualization;
import assets.Strings;
import interpreter.Interpreter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
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
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * This is the Controller of MVC for the visualizer GUI.
 */
public class VisualizerController implements CommunicatorListener{

    private Visualization visualization;
    private Stage window;
    private final LogStreamManager lsm;
    private final Interpreter interpreter;
    private final iModel model;
    private final FXMLLoader mainViewLoader;

    //Connection dialog stuff.
    private final SimpleStringProperty currentlyConnected = new SimpleStringProperty();
    private final SimpleStringProperty allConnected = new SimpleStringProperty();
    private FXMLLoader connectedLoader;
    private Stage connectedDialog;
    
    //Settings dialog stuff
    private FXMLLoader settingsLoader;
    private Stage settingsDialog;

    // Controls
    private boolean isPlaying = false;
    private int speed = 1;
    private ListView<Operation> operationHistory;

   public VisualizerController(Visualization visualization, Stage window, iModel model, LogStreamManager lsm, FXMLLoader fxmlLoader) {
        this.visualization = visualization;
        this.window = window;
        this.model = model;
        this.lsm = lsm;
        this.lsm.setListener(this);
        this.mainViewLoader = fxmlLoader;
        
        this.interpreter = new Interpreter();
        initConnectedPane();
        initSettingsPane();
    }

    public void showSettings(){
        settingsDialog.setWidth(this.window.getWidth()*0.75);
        settingsDialog.setHeight(this.window.getHeight()*0.75);
        settingsDialog.show();
    }
    
    /**
     * Starts playing or pause the AV animation.
     */
    public void playPauseButtonClicked(Event e){
        if(isPlaying) {
            ((Button) e.getSource()).setText("Play");
            System.out.println("Placeholder: Woah! Hold your horses.");
            isPlaying = false;
        }
        else {
            ((Button) e.getSource()).setText("Pause");
            System.out.println("Placeholder: What's up player?");
            isPlaying = true;
        }
    }

    /**
     * Restart the AV animation.
     */
    public void restartButtonClicked(){
        model.reset();
    }

    /**
     * Step the animation forward
     */
    public void stepForwardButtonClicked(){
        model.stepForward();
        visualization.render();
    }

    /**
     * Step the animation backward
     */
    public void stepBackwardButtonClicked(){
        model.stepBackward();
        visualization.render();
    }

    /**
     * Change the animation speed
     */
    public void changeSpeedButtonClicked(Event e){
        System.out.println("Placeholder: Let's pump it up!");
        speed = speed*2 % 7; // possible values: 1, 2, 4
        ((Button) e.getSource()).setText(speed + "x");
    }

    public void aboutProgram(){
        System.out.print("Placeholder: A project by " );
        for (String name : Strings.DEVELOPER_NAMES) {
            System.out.print(name + ", ");
        }
        System.out.println();
        
    }
    
    public void interpretOperationHistory(){
 		 interpreter.consolidate(operationHistory.getItems());
    }

    private DecimalFormat df;
    private void initSettingsPane(){
    	df = new DecimalFormat("#.##"); 
        settingsLoader = new FXMLLoader(getClass().getResource("/SettingsView.fxml"));
        settingsLoader.setController(this);
        settingsDialog = new Stage();
        settingsDialog.getIcons().add(new Image(VisualizerController.class.getResourceAsStream( "/icon_settings.png" )));
        settingsDialog.initModality(Modality.APPLICATION_MODAL);
        settingsDialog.setTitle(Strings.PROJECT_NAME + ": Settings and Preferences");
        settingsDialog.initOwner(this.window);
        
        TabPane p = null;
		try {
			p = settingsLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        Scene dialogScene = new Scene(p, this.window.getWidth()*0.75, this.window.getHeight()*0.75);
        settingsDialog.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            //Close without saving.
            settingsDialog.close();
        });
        
        timeBetweenField = (TextField) settingsLoader.getNamespace().get("timeBetweenField");
        perSecField = (TextField) settingsLoader.getNamespace().get("perSecField");
        
        settingsDialog.setScene(dialogScene);
    }
    
    private void initConnectedPane(){
    	JGroupCommunicator jgc = (JGroupCommunicator) lsm.getCommunicator();
    	connectedLoader = new FXMLLoader(getClass().getResource("/ConnectedView.fxml"));
    	connectedDialog = new Stage();
    	connectedDialog.getIcons().add(new Image(VisualizerController.class.getResourceAsStream( "/icon_connected.png" )));
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
		
        Scene dialogScene = new Scene(p, this.window.getWidth()*0.75, this.window.getHeight()*0.75);
        connectedDialog.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            jgc.listenForMemberInfo(false);
            connectedDialog.close();
        });
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
				operationHistory.getItems().addAll(lsm.getOperations());
				lsm.clearData();
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
		System.err.println("No way to get operations/variables from model?");
		//TODO: No way to get operations/variables from model?
//		lsm.setOperations(null);
//		lsm.setKnownVariables(null);
//		lsm.printLog(outputPath);
	}
	
    public void propertiesFailed(){
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
	
	private TextField perSecField;
	public void setPlayBackOpsPerSec(Event e){
        double speed;
        
        try{
            perSecField.setStyle("-fx-text-inner-color: black;");
        	speed = Double.parseDouble(perSecField.getText());
        } catch (Exception exc){
            // NaN
            perSecField.setStyle("-fx-text-inner-color: red;");
        	return;
        }
        
        if(speed <= 0){
        	perSecField.setText("invalid");
            perSecField.selectAll();
            return;
        }

        //Valid input. Change other button and speed variable.
        perSecField.setText(df.format(speed));
        timeBetweenField.setText(df.format((1000/speed)));
	}
	
	private TextField timeBetweenField;
	public void setPlaybackTimeBetweenOperations(Event e){
        double speed;
        
        try{
            perSecField.setStyle("-fx-text-inner-color: black;");
        	speed = Double.parseDouble(timeBetweenField.getText());
        } catch (Exception exc){
            // NaN
            perSecField.setStyle("-fx-text-inner-color: red;");
        	return;
        }

        if(speed < 0){
        	timeBetweenField.setText("invalid");
            perSecField.selectAll();
        	return;
        }
        
        //Valid input. Change other button and speed variable.
        perSecField.setText(df.format(1000/speed));
        timeBetweenField.setText(df.format(speed));
	}
	
	@Override
	public CommunicatorListener getListener() {
		return null; //VisualizerController doesn't have any listeners.
	}

	public void setOperationListView(ListView<Operation> listView) {
		operationHistory = listView;
	}
}