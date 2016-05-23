package multiset.view;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import multiset.model.Ball;
import multiset.model.iModel;

/**
 * Created by cb on 26/04/16.
 */
public class View implements iView {

    private final iModel model;
    private final Canvas canvas;

    public View (iModel model, Canvas canvas) {
        this.model = model;
        this.canvas = canvas;
    }

    @Override
    public void render () {
        this.clear();
        for (Ball b : this.model.getBalls()) {
            this.paintBall(b);
        }

    }

    private void clear () {
        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        gc.setFill(Color.WHEAT);
        gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

    private void paintBall (Ball b) {
        GraphicsContext gc = this.canvas.getGraphicsContext2D();

        // Draw ball
        gc.setFill(Color.RED);
        gc.fillOval(b.getX() - b.getR(), b.getY() - b.getR(), b.getR() * 2, b.getR() * 2);

        // Draw boll value
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("" + b.getValue(), b.getX(), b.getY());
    }
}
