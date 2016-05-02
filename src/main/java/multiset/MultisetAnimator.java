package multiset;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
//import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import multiset.model.Model;
import multiset.view.View;

/**
 * Created by Smith on 26/04/16.
 */
public class MultisetAnimator extends Scene{

    private long previousTime = 0;
//    private float canvasWidth = 600;
//    private float canvasHeight = 400;

    public MultisetAnimator(VBox root, double width, double height, FXMLLoader fxmlLoader){
        super(root, width, height);

        Canvas canvas = (Canvas) fxmlLoader.getNamespace().get("ballCanvas");

//        BorderPane parent = (BorderPane) canvas.getParent();
//        canvas.widthProperty().bind(parent.widthProperty());
//        canvas.heightProperty().bind(parent.heightProperty());
//        canvas.setHeight(canvasWidth);
//        canvas.setWidth(canvasHeight);

        Model model = new Model(canvas.getWidth(), canvas.getHeight());
        View view = new View(model, canvas);

        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                // Timing stuff:
                if (previousTime == 0) { // Ensuring that there was a previous time
                    previousTime = currentNanoTime;
                    return;
                }
                float delta = (currentNanoTime - previousTime) / 1e9f;
                previousTime = currentNanoTime;

                // Model stuff:
                model.tick(delta*1000);
                view.render();
            }
        }.start();
    }

}
