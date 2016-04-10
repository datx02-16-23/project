package application.Visualizer;

import application.model.iModel;
import application.view.Visualization;
import assets.Strings;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import manager.CommunicatorListener;
import manager.JGroupCommunicator;
import manager.LogStreamManager;
import manager.Communicator.MavserMessage;
import wrapper.Operation;

import java.io.File;
import java.util.List;

/**
 * This is the Controller of MVC for the visualizer GUI.
 */
public class VisualizerController implements CommunicatorListener{

    private Visualization visualization;
    private Stage window;
    private final LogStreamManager lsm;
    private final iModel model;
    private final FXMLLoader fxmlLoader;

    // Controls
    private boolean isPlaying = false;
    private int speed = 1;

    public VisualizerController(Visualization visualization, Stage window, iModel model, LogStreamManager lsm, FXMLLoader fxmlLoader) {
        this.visualization = visualization;
        this.window = window;
        this.model = model;
        this.lsm = lsm;
        this.lsm.setListener(this);
        this.fxmlLoader = fxmlLoader;
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
    }

    /**
     * Change the animation speed
     */
    public void changeSpeedButtonClicked(Event e){
        System.out.println("Placeholder: Let's pump it up!");
        speed = speed*2 % 7; // possible values: 1, 2, 4
        ((Button) e.getSource()).setText("" + speed + "x");
    }

    public void aboutProgram(){
        System.out.print("Placeholder: A project by " );
        for (String name : Strings.DEVELOPER_NAMES) {
            System.out.print(name + ", ");
        }
        System.out.println();
        
    }
    
    private final SimpleStringProperty connected = new SimpleStringProperty();
    public void connectedToChannel(Stage parent){
    	JGroupCommunicator jgc = (JGroupCommunicator) lsm.getCommunicator();
    	jgc.listenForMemberInfo(true);
        final Stage dialog = new Stage();
        dialog.getIcons().add(new Image(VisualizerModel.class.getResourceAsStream( "connected_entities_icon.png" )));
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Connected Entities: Channel = \"" + jgc.getChannel() + "\"");
        dialog.initOwner(parent);
        TextArea textArea = new TextArea("If you can see this, something went wrong :(.");
        
        textArea.textProperty().bind(connected);
        
        Scene dialogScene = new Scene(textArea, 600, 200);
        dialog.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            jgc.listenForMemberInfo(false);
        	dialog.close();
        });
        dialog.setScene(dialogScene);
        dialog.show();
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
    private void setFile(File file) {
    	lsm.clearData();
        lsm.readLog(file);
        model.set(lsm.getKnownVariables(), lsm.getOperations());
        @SuppressWarnings("unchecked")
		ListView<Operation> operationHistory = (ListView<Operation>) fxmlLoader.getNamespace().get("operationHistory");
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
	    	connected.set(sb.toString());
			return;
		}
		
        @SuppressWarnings("unchecked")
		ListView<Operation> operationHistory = (ListView<Operation>) fxmlLoader.getNamespace().get("operationHistory");
        Platform.runLater(new Runnable(){

			@Override
			public void run() {
				operationHistory.getItems().addAll(lsm.getOperations());
				lsm.clearData();
			}        	
        });
		
	}

	@Override
	public CommunicatorListener getListener() {
		return null; //VisualizerController doesn't have any listeners.
	}
}
