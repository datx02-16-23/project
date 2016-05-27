package gui.panel;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import contract.json.Operation;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import model.ExecutionModelController;
import model.ExecutionTickListener;
import render.Visualization;

/**
 * 
 * @author Richard Sundqvist
 *
 */
public class ControlPanel extends Pane implements ExecutionTickListener {

    private static final int               tickCount = 100;

    // ============================================================= //
    /*
     *
     * Field variables
     *
     */
    // ============================================================= //

    private final ExecutionModelController emController;
    private final Visualization            visualization;
    private final ProgressBar              animationProgress;

    // Model progress list + related items.
    private ProgressBar                    modelProgress;
    private ListView<Operation>            operationList;
    private TextField                      listSizeLabel;
    private TextField                      currentOperationLabel;

    // ============================================================= //
    /*
     *
     * Constructors
     *
     */
    // ============================================================= //

    /**
     * Create a new ControlPanel
     * 
     * @param executionModelController
     *            Used to control the model
     * @param visualization
     *            Used to control visualization.
     */
    @SuppressWarnings("unchecked") public ControlPanel (ExecutionModelController executionModelController,
            Visualization visualization) {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/panel/ControlPanel.fxml"));
        fxmlLoader.setController(this);

        Pane root = null;
        try {
            root = (Pane) fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        getChildren().add(root);

        emController = executionModelController;
        emController.setExecutionTickListener(this, tickCount);
        this.visualization = visualization;

        emController.getExecutionModel().indexProperty().addListener(new InvalidationListener() {
            @Override public void invalidated (Observable observable) {
                int index = emController.getExecutionModel().indexProperty().get();
                updateOperationOverview(index);
            }
        });

        // ============================================================= //
        /*
         * Initialise namespace items.
         */
        // ============================================================= //

        Map<String, Object> namespace = fxmlLoader.getNamespace();

        animationProgress = (ProgressBar) namespace.get("animationProgress");

        Slider speedSlider = (Slider) namespace.get("speedSlider");
        speedSlider.setValue(emController.getAutoExecutionSpeed());
        speedSlider.setOnMouseReleased(event -> {
            emController.setAutoExecutionSpeed((long) speedSlider.getValue());
        });

        // Panel sizing.
        root.prefWidthProperty().bind(widthProperty());
        root.prefHeightProperty().bind(heightProperty());

        CheckBox animate = (CheckBox) namespace.get("animate");
        animate.setSelected(visualization.getAnimate());

        // ============================================================= //
        /*
         * Binding
         */
        // ============================================================= //

        Button play = (Button) namespace.get("play");
        play.disableProperty().bind(emController.getExecutionModel().executeNextProperty().not());

        emController.autoExecutingProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed (ObservableValue<? extends Boolean> observable, Boolean oldValue,
                    Boolean newValue) {

                if (newValue) {
                    play.setText("Pause");
                } else {
                    play.setText("Play");
                }
            }

        });

        Button forward = (Button) namespace.get("forward");
        forward.disableProperty().bind(emController.getExecutionModel().executeNextProperty().not());

        Button back = (Button) namespace.get("back");
        back.disableProperty().bind(emController.getExecutionModel().executePreviousProperty().not());

        // Operation progress bar
        operationList = (ListView<Operation>) namespace.get("operationList");
        modelProgress = (ProgressBar) namespace.get("modelProgress");
        listSizeLabel = (TextField) namespace.get("listSizeLabel");
        currentOperationLabel = (TextField) namespace.get("currentOperationLabel");
        ObservableList<Operation> obsList = emController.getExecutionModel().getOperations();
        operationList.setItems(obsList);

    }

    // ============================================================= //
    /*
     *
     * Control / FXML onAction()
     *
     */
    // ============================================================= //

    public void play () {
        emController.toggleAutoExecution();
    }

    public void forward () {
        emController.executeNext();
    }

    public void back () {
        emController.executePrevious();
    }

    public void restart () {
        emController.reset();
    }

    public void clear () {
        emController.clear();
    }

    public void toggleAnimate (Event e) {
        CheckBox cb = (CheckBox) e.getSource();
        visualization.setAnimate(cb.isSelected());
    }

    public void oooooOOoooOOOooooOOooo (Event e) {
        Button b = (Button) e.getSource();
        b.setOpacity(0.05);

        // https://www.youtube.com/watch?v=inli9ukUKIs
        URL resource = this.getClass().getResource("/assets/oooooOOoooOOOooooOOooo.mp3");
        Media media = new Media(resource.toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }

    public void textFieldGoto (Event e) {
        TextField tf = (TextField) e.getSource();

        String input = tf.getText();
        try {
            int index = Integer.parseInt(input) - 1;
            emController.execute(index);
        } catch (Exception exc) {
            tf.setText((emController.getExecutionModel().getIndex()) + "");
            URL resource = this.getClass().getResource("/assets/shortcircuit.mp3");
            Media media = new Media(resource.toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(0.1);
            mediaPlayer.play();
        }

    }

    @SuppressWarnings("rawtypes") public void listViewGoto (Event e) {
        ListView lw = (ListView) e.getSource();

        int index = lw.getSelectionModel().getSelectedIndex() - 1;
        emController.execute(index);
    }

    // ============================================================= //
    /*
     *
     * Interface
     *
     */
    // ============================================================= //

    @Override public void tickUpdate (int tickNumber) {
        animationProgress.setProgress((double) tickNumber / tickCount);
    }

    private void updateOperationOverview (Number index) {
        int currOp = index.intValue() + 1;
        int totOps = operationList.getItems().size();

        operationList.getFocusModel().focus(currOp + 1);
        operationList.scrollTo(currOp + 1);
        operationList.getSelectionModel().select(currOp - 1);

        modelProgress.setProgress((double) currOp / totOps);

        currentOperationLabel.setText(currOp + "");
        listSizeLabel.setText(totOps + "");
    }
}
