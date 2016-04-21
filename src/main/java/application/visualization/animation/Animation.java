package application.visualization.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import application.visualization.render2d.Render;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import wrapper.datastructures.Element;

public abstract class Animation {

    private static final ArrayList<Animation> REGISTERED     = new ArrayList<Animation>();
    /**
     * The animation time in milliseconds.
     */
    protected static long                     ANIMATION_TIME = 5000;
    /**
     * The number of frames animations use.
     */
    protected static short                    frame_count    = 255;
    /**
     * Instance stuff
     */
    protected final Render                    owner;
    protected final Timeline                  timeline;
    protected final List<KeyFrame>            keyframes;
    protected final Element                   e;
    //Current animation frame.
    protected short                           frame;

    protected Animation (Render owner, Element e){
        this.owner = owner;
        this.e = e;
        timeline = new Timeline();
        keyframes = timeline.getKeyFrames();
        timeline.setCycleCount(frame_count);
        timeline.setOnFinished(event -> {
            finish();
        });
        REGISTERED.add(this);
    }

    /**
     * Start the animation from the beginning.
     */
    public void start (){
        frame = 1;
        timeline.playFromStart();
    }

    /**
     * Forces the animation to finish. Called automatically after the last frame.
     */
    public void finish (){
        timeline.stop();
        ensureCleared();
    }

    /**
     * Called to ensure the element path is cleared.
     */
    protected abstract void ensureCleared ();

    /**
     * Calls finish() on all Animations and clears the list of animations.
     */
    public static void finishAll (){
        for (Animation a : REGISTERED) {
            a.finish();
        }
        REGISTERED.clear();
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
            Animation.frame_count = frames;
        }
    }

    /**
     * Generate points on a straight line between a a starting point and an end point.
     * 
     * @param x1 Starting x.
     * @param y1 Starting y.
     * @param x2 End x.
     * @param y2 End y.
     * @param frame_count The number of points to return.
     * @param restframes The proportion frames at rest at the end of the animation the{@code points}.
     * @return An array with the x-coordinate for step i at ans[0][i] and the y-coordinte at ans[1][i].
     */
    public double[][] linearAnimationPath (double x1, double y1, double x2, double y2, int frame_count, double restframes){
        int resting_frames = (int) (frame_count * restframes);
        double[][] ans = new double[2][frame_count + 2];
        double lasty;
        int i = 1;
        double x = x1;
        ans[0][0] = x1;
        ans[1][0] = y1;
        if (x1 != x2) {
            double x_step = (x2 - x1) / (frame_count - resting_frames + 1);
            double k = (y2 - y1) / (x2 - x1);
            double m = y1 - k * x1;
            for (; i < frame_count - resting_frames + 1; i++) {
                x += x_step;
                ans[0][i] = x;
                ans[1][i] = k * x + m;
            }
            lasty = k * x + m;
        }
        else {
            double y_step = (y2 - y1) / (frame_count - resting_frames + 1);
            lasty = y2;
            double y = y1;
            for (; i < frame_count - resting_frames + 1; i++) {
                y += y_step;
                ans[0][i] = x;
                ans[1][i] = y;
            }
        }
        for (; i < ans[0].length; i++) {
            ans[0][i] = x;
            ans[1][i] = lasty;
        }
        return ans;
    }
}
