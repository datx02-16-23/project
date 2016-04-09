package application.Visualizer;

import application.model.Model;
import application.model.iModel;
import assets.Strings;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import manager.LogStreamManager;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

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
        System.out.println("Placeholder: If only there was a button for second chances.");
    }

    /**
     * Step the animation forward
     */
    public void stepForwardButtonClicked(){
        System.out.println("Placeholder: So it was you.");
    }

    /**
     * Step the animation backward
     */
    public void stepBackwardButtonClicked(){
        System.out.println("Placeholder: aaaand you fell of a cliff");
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

        File file = fileChooser.showOpenDialog(window);

        if (file != null){
            setFile(file);
        } else {
            System.err.println("Unable to find file");
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
