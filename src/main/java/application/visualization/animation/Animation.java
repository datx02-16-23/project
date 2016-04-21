package application.visualization.animation;

import java.util.List;

import application.visualization.render2d.Render;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public abstract class Animation {

    /**
     * The animation time in milliseconds.
     */
    protected static long    ANIMATION_TIME;
    /**
     * The number of frames animations use.
     */
    protected static short   FRAMES = 100;
    /**
     * Instance stuff
     */
    protected final Render   owner;
    protected final Timeline timeline;
    protected final List<KeyFrame> keyframes;
    //Current animation frame.
    protected short          frame;

    protected Animation (Render owner){
        this.owner = owner;
        timeline = new Timeline();
        keyframes = timeline.getKeyFrames();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setOnFinished(event -> {
            finish();
        });
        timeline.setAutoReverse(true);
    }

    /**
     * Start the animation from the beginning.
     */
    public void start (){
//        frame = 0;
//        timeline.playFromStart();
//        System.out.println("\nSTART: keyframes.size() = " + keyframes.size());
    }

    /**
     * Forces the animation to finish. Called automatically after the last frame.
     */
    public void finish (){
        System.out.println("done!");
    }

    /**
     * Set the animation time in millisections for <b>ALL</b> animations.
     * 
     * @param millis The new animation time in milliseconds.
     */
    public static final void setAnimationTime (long millis){
        if (millis >= 0) {
            ANIMATION_TIME = millis;
        }
    }

    /**
     * Set the number of frames for <b>ALL</b> animations.
     * 
     * @param frames The new number of frames.
     */
    public static final void setFrames (short frames){
        if (frames >= 0) {
            Animation.FRAMES = frames;
        }
    }
    /**
     * Generate points on a straight line between a a starting point and an end point.
     * @param x1 Starting x.
     * @param y1 Starting y.
     * @param x2 End x.
     * @param y2 End y.
     * @param points The number of points to return.
     * @return An array with the x-coordinate for step i at ans[0][i] and the y-coordinte at ans[1][i].
     */
    public double[][] linearPoints (double x1, double y1, double x2, double y2, int points){
        double[][] ans = new double[2][points];
        double xstep = (x2 - x1) / points;
        double k = (y2 - y1) / (x2 - x1);
        double m = y1 - k * x1;
        double x = xstep;
        for (int i = 0; i < points; i++) {
            x += xstep;
            ans[0][i] = x;
            ans[1][i] = k * i + m;
        }
        return ans;
    }
}
