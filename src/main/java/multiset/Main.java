package multiset;

import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import javafx.util.Duration;
import multiset.filter.Filter;
import multiset.filter.iFilter;
import multiset.model.Model;
import multiset.model.iModel;
import multiset.view.View;
import multiset.view.iView;

/**
 * Created by cb on 24/04/16.
 */
public class Main extends Application{
    private iModel model;
    private iView view;
    private iFilter filter;
    private final int width = 600;
    private final int height = 600;

    @Override
    public void start(Stage primaryStage) throws Exception {
        String input = "m, n";
        String result = "m";
        String conditional = "m = n";
        filter = new Filter(input, result, conditional);

        model = new Model(width, height, filter, 1, 11);
        setupView(primaryStage);
        setupContinousUpdates();
    }

    private void setupView(Stage primaryStage) {
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        view = new View(model, canvas);
        primaryStage.show();

    }

    private void setupContinousUpdates() {
        final Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(10), actionEvent -> {
            model.tick(10);
            view.render();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }


    public static void main(String[] args){
        launch(args);
    }
}
