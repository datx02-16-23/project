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

public class VisualizerController implements Initializable {

    private Stage window;
    private final LogStreamManager lsm;
    private final iModel model;

    public VisualizerController(Stage window, iModel model, LogStreamManager lsm) {
        this.window = window;
        this.model = model;
        this.lsm = lsm;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void playButtonClicked(){
        System.out.println("What's up player?");
    }

    public void closeProgram(){
        lsm.close();
        window.close();
    }

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

    private void setFile(File file) {
        lsm.readLog(file);
        model.set(lsm.getKnownVariables(), lsm.getOperations());
    }
}
