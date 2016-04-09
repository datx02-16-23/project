package application.Visualizer;

import application.model.Model;
import application.model.iModel;
import javafx.fxml.Initializable;
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

    public VisualizerController(Stage window, iModel model, LogStreamManager lsm) {
        this.window = window;
        this.model = model;
        this.lsm = lsm;
    }

    /**
     * Starts playing the AV animation.
     */
    public void playButtonClicked(){
        System.out.println("What's up player?");
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
