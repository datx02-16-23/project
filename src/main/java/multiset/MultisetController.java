package multiset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import multiset.filter.Filter;
import multiset.filter.iFilter;
import multiset.model.Model;
import multiset.model.RangePatterns;
import multiset.model.iModel;
import multiset.view.View;
import multiset.view.iView;

/**
 * Created by Smith on 26/04/16.
 */
public class MultisetController {

    private final FXMLLoader       fxmlLoader;
    private TextField              range, cond, input, output;
    private ListView<String>       history;
    private iModel                 model;
    private iView                  view;
    private final Stage            window;
    private final Scene            previousScene;
    private final Timeline         timeline;
    private ObservableList<String> items;

    public MultisetController (Stage window) {
        this.window = window;
        this.previousScene = window.getScene();
        this.fxmlLoader = new FXMLLoader(this.getClass().getResource("/view/MultisetView.fxml"));
        this.fxmlLoader.setController(this);
        VBox p = null;
        try {
            p = this.fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.loadNamespaceItems(this.fxmlLoader.getNamespace());

        window.setScene(new Scene(p));
        this.timeline = new Timeline();
        this.setupTimeline();
    }

    private void setupTimeline () {
        final int renderTime = 17; // In ms
        this.timeline.getKeyFrames().add(new KeyFrame(Duration.millis(renderTime), actionEvent -> {
            this.model.tick(renderTime);
            this.view.render();
        }));
        this.timeline.setCycleCount(Animation.INDEFINITE);
    }

    /**
     * Called when the "Back" button is pressed.
     */
    public void goBackPressed () {
        this.timeline.stop();
        this.window.setScene(this.previousScene);
    }

    /**
     * Called when the "Go!" button is pressed. (or enter is pressed...)
     */
    public void run () {
        this.timeline.stop();
        Canvas ballCanvas = (Canvas) this.fxmlLoader.getNamespace().get("ballCanvas");

        iFilter filter = new Filter(this.input.getText(), this.output.getText(), this.cond.getText());
        ArrayList<Double> list = new RangePatterns(this.range.getText()).getList();
        this.model = new Model(ballCanvas.getWidth(), ballCanvas.getHeight(), filter, list, this.items);
        this.view = new View(this.model, ballCanvas);

        this.timeline.play();
    }

    /**
     * Called from textfields
     */
    public void keyListener (KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            this.run(); // Run animation
            event.consume(); // "You shall not pass! (for safety reasons)"
        }
    }

    /**
     * Load assets from theMultisetController FXML loader namespace.
     *
     * @param namespace
     *            The namespace of the FXML loader.
     */
    private void loadNamespaceItems (Map<String, Object> namespace) {
        this.input = (TextField) namespace.get("input");
        this.output = (TextField) namespace.get("output");
        this.cond = (TextField) namespace.get("cond");
        this.range = (TextField) namespace.get("range");

        // List stuff
        this.history = (ListView<String>) namespace.get("collisionHistory");
        this.items = FXCollections.observableArrayList();
        this.history.setItems(this.items);
        this.items.add("History:");
    }

}
