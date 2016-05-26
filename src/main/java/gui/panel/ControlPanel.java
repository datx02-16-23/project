package gui.panel;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
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
import model2.ExecutionModelController;
import model2.ExecutionTickListener;
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

    // ============================================================= //
    /*
     *
     * Constructors
     *
     */
    // ============================================================= //

    @SuppressWarnings({ "rawtypes", "unchecked" })
    /**
     * Create a new ControlPanel
     * 
     * @param executionModelController
     *            Used to control the model
     * @param visualization
     *            Used to control visualization.
     */
    public ControlPanel (ExecutionModelController executionModelController, Visualization visualization) {

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

//        setMaxHeight(Double.MAX_VALUE);

        emController = executionModelController;
        emController.setExecutionTickListener(this, tickCount);
        this.visualization = visualization;

        // ============================================================= //
        /*
         * Initialise namespace items.
         */
        // ============================================================= //

        Map<String, Object> namespace = fxmlLoader.getNamespace();

        animationProgress = (ProgressBar) namespace.get("animationProgress");

        Slider speedSlider = (Slider) namespace.get("speedSlider");
        speedSlider.setValue(emController.getAutoExecutionSpeed());
        speedSlider.valueProperty().addListener(new ChangeListener() {
            @Override public void changed (ObservableValue observable, Object oldValue, Object newValue) {
                emController.setAutoExecutionSpeed((long) speedSlider.getValue());
            }
        });

        // Panel sizing.
        root.prefWidthProperty().bind(widthProperty());
        root.prefHeightProperty().bind(heightProperty());

        CheckBox animate = (CheckBox) namespace.get("animate");
        animate.setSelected(visualization.getAnimate());

        // Button binding

        Button play = (Button) namespace.get("play");
        play.disableProperty().bind(emController.getExecutionModel().executeNextProperty().not());
        emController.autoExecutingProperty().addListener(new ChangeListener() {

            //@formatter:off
            @Override public void changed (ObservableValue observable,
                    Object wasAutoExecuting, Object isAutoExecuting) {
                
                System.out.println("changed!");
                
                if ((Boolean) isAutoExecuting) {
                    play.setText("Pause");
                } else {
                    play.setText("Play");
                }
            }
            //@formatter:on

        });

        Button forward = (Button) namespace.get("forward");
        forward.disableProperty().bind(emController.getExecutionModel().executeNextProperty().not());

        Button back = (Button) namespace.get("back");
        back.disableProperty().bind(emController.getExecutionModel().executePreviousProperty().not());

        // Operation progress bar
        ListView lw = (ListView<Object>) namespace.get("operationList");
        ProgressBar modelProgress = (ProgressBar) namespace.get("modelProgress");
        lw.getItems().addListener(new ListChangeListener() {

            @Override public void onChanged (Change c) {
                modelProgress.setProgress(emController.getExecutionModel().getIndex() / lw.getItems().size());
            }

        });
    }

    // ============================================================= //
    /*
     *
     * Control / FXML onAction()
     *
     */
    // ============================================================= //

    public void play () {
        emController.startAutoExecution();
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
            int index = Integer.parseInt(input);
            emController.execute(index);
        } catch (Exception exc) {
            tf.setText("");
            URL resource = this.getClass().getResource("/assets/shortcircuit.mp3");
            Media media = new Media(resource.toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        }

    }

    @SuppressWarnings("rawtypes") public void listViewGoto (Event e) {
        ListView lw = (ListView) e.getSource();

        int index = lw.getSelectionModel().getSelectedIndex();
        emController.execute(index);
    }

    // ============================================================= //
    /*
     *
     * Interface
     *
     */
    // ============================================================= //

    @Override public void update (int tickNumber) {
        animationProgress.setProgress((double) tickNumber / tickCount);
    }
}
