package multiset;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import multiset.model.Model;
import multiset.view.View;

import java.io.IOException;

/**
 * Created by Smith on 26/04/16.
 */
public class MultisetAnimator extends Scene{

    private long previousTime = 0;
    private float canvasWidth = 600;
    private float canvasHeight = 600;

    public MultisetAnimator(VBox root, double width, double height, FXMLLoader fxmlLoader){
        super(root, width, height);

        Canvas canvas = (Canvas) fxmlLoader.getNamespace().get("ballCanvas");

        canvas.setHeight(canvasWidth);
        canvas.setWidth(canvasHeight);

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
