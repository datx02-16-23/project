package application.Visualizer;

import application.model.iModel;
import application.view.Visualization;
import assets.Strings;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import manager.CommunicatorListener;
import manager.LogStreamManager;
import wrapper.Operation;

import java.io.File;

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
        lsm.readLog(file);
        model.set(lsm.getKnownVariables(), lsm.getOperations());
        ListView<Operation> operationHistory = (ListView<Operation>) fxmlLoader.getNamespace().get("operationHistory");
        operationHistory.getItems().clear();
        operationHistory.getItems().addAll(lsm.getOperations());
    }

	@Override
	public void messageReceived(short messageType) {
        ListView<Operation> operationHistory = (ListView<Operation>) fxmlLoader.getNamespace().get("operationHistory");
        Platform.runLater(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				operationHistory.getItems().addAll(lsm.getOperations());
				lsm.clearData();
			}        	
        });
		
	}
}
