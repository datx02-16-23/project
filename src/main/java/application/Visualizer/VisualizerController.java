package application.Visualizer;

import application.model.iModel;
import assets.Strings;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import manager.LogStreamManager;
import java.io.File;

/**
 * This is the Controller of MVC for the visualizer GUI.
 */
public class VisualizerController {

    private Stage window;
    private final LogStreamManager lsm;
    private final iModel model;

    // Controls
    private boolean isPlaying = false;
    private int speed = 1;

    public VisualizerController(Stage window, iModel model, LogStreamManager lsm) {
        this.window = window;
        this.model = model;
        this.lsm = lsm;
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
    }
}
