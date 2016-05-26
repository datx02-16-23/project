package gui.panel;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
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
import render.assets.Visualization;

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

    @SuppressWarnings({ "rawtypes", "unchecked" }) public ControlPanel (
            ExecutionModelController executionModelController, Visualization visualization) {

        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/panel/ControlPanel.fxml"));
        fxmlLoader.setController(this);

        Pane root = null;
        try {
            root = (Pane) fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getChildren().add(root);

        this.emController = executionModelController;
        emController.setExecutionTickListener(this, tickCount);
        this.visualization = visualization;

        // ============================================================= //
        /*
         * Initialise namespace items.
         */
        // ============================================================= //

        Map<String, Object> namespace = fxmlLoader.getNamespace();

        animationProgress = (ProgressBar) namespace.get("ProgressBar");

        Slider speedSlider = (Slider) namespace.get("speedSlider");
        speedSlider.setValue(emController.getAutoExecutionSpeed());

        CheckBox animate = (CheckBox) namespace.get("animate");
        animate.setSelected(visualization.getAnimate());

        // Button binding

        Button play = (Button) namespace.get("play");
        play.disableProperty().bind(emController.execModel.executeNextProperty());

        Button forward = (Button) namespace.get("forward");
        forward.disableProperty().bind(emController.execModel.executeNextProperty());

        Button back = (Button) namespace.get("back");
        back.disableProperty().bind(emController.execModel.executePreviousProperty());

        Button restart = (Button) namespace.get("restart");
        ReadOnlyBooleanProperty epp = emController.execModel.executePreviousProperty();
        BooleanBinding ncp = emController.execModel.clearProperty().not();
        restart.disableProperty().bind(epp.and(ncp));

        Button clear = (Button) namespace.get("back");
        back.disableProperty().bind(emController.execModel.clearProperty().not());

        // Operation progress bar
        ListView lw = (ListView<Object>) namespace.get("operationList");
        ProgressBar modelProgress = (ProgressBar) namespace.get("modelProgress");
        lw.getItems().addListener(new ListChangeListener() {

            @Override public void onChanged (Change c) {
                modelProgress.setProgress(emController.execModel.getIndex() / lw.getItems().size());
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
        emController.execModel.executeNext();
    }

    public void back () {
        emController.execModel.executePrevious();
    }

    public void restart () {
        emController.execModel.reset();
    }

    public void clear () {
        emController.execModel.clear();
    }

    public void animate (Event e) {
        CheckBox cb = (CheckBox) e.getSource();
        visualization.setAnimate(cb.isSelected());
    }

    public void speedChanged (Event e) {
        Slider s = (Slider) e.getSource();
        emController.setAutoExecutionSpeed((long) s.getValue());
    }

    public void oooooOOoooOOOooooOOooo (Event e) {
        Button b = (Button) e.getSource();
        b.setOpacity(1);

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
            emController.execModel.execute(index);
        } catch (Exception exc) {
            URL resource = this.getClass().getResource("/assets/shortcircuit.mp3");
            Media media = new Media(resource.toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        }

    }

    @SuppressWarnings("rawtypes") public void listViewGoto (Event e) {
        ListView lw = (ListView) e.getSource();

        int index = lw.getSelectionModel().getSelectedIndex();
        emController.execModel.execute(index);
    }

    // ============================================================= //
    /*
     *
     * Interface
     *
     */
    // ============================================================= //

    @Override public void update (int tickNumber) {
        animationProgress.setProgress(tickNumber / tickCount);
    }
}
