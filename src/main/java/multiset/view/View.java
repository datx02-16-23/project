package multiset.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import multiset.model.Ball;
import multiset.model.iModel;

/**
 * Created by cb on 26/04/16.
 */
public class View implements iView{

    private final iModel model;
    private final Canvas canvas;

    public View(iModel model, Canvas canvas){
        this.model = model;
        this.canvas = canvas;
    }

    @Override
    public void render() {
        clear();
        for(Ball b:model.getBalls()){
            paintBall(b);
        }

    }

    private void clear(){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void paintBall(Ball b){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.RED);

        gc.fillOval(b.getX(), b.getY(), b.getR()*20, b.getR()*20);
    }
}
