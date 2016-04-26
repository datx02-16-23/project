package multiset;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by Smith on 26/04/16.
 */
public class MultisetAnimation extends Scene{



    public MultisetAnimation(VBox root, double w, double h){
        super(null,w, h);
//        this.parentStage = parentStage;

//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MultisetView.fxml"));
//        fxmlLoader.setController(this);
////        multisetView.getIcons().add(new Image(GUI_Controller.class.getResourceAsStream("/assets/icon_settings.png")));
//
////        initOwner(parentStage);
//        VBox p = null;
//        try {
//            p = fxmlLoader.load();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        Scene theScene = new Scene(p, parentStage.getWidth() * 0.75, parentStage.getHeight() * 0.75);

        setRoot(p);

        Canvas canvas = (Canvas) fxmlLoader.getNamespace().get("ballCanvas");

        canvas.setHeight(512);
        canvas.setWidth(512);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        Image earth = new Image( "assets/earth.png" );
        Image sun   = new Image( "assets/sun.png" );
        Image space = new Image( "assets/space.png" );

        final long startNanoTime = System.nanoTime();

        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                double t = (currentNanoTime - startNanoTime) / 1000000000.0;

                double x = 232 + 128 * Math.cos(t);
                double y = 232 + 128 * Math.sin(t);

                // background image clears canvas
                gc.drawImage( space, 0, 0 );
                gc.drawImage( earth, x, y );
                gc.drawImage( sun, 196, 196 );
            }
        }.start();
    }

}
