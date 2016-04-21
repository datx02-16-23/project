package application.visualization;

import application.visualization.render2d.Render;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import wrapper.datastructures.Element;

/**
 * Helper class to run animations of operations.
 * @author Richard Sundqvist
 *
 */
public class VisualiserAnimation {

    private static int     animationTime;
    private static int     frames;
    //Tools
    private final Timeline timeline;
    private final KeyFrame keyframe;
    //Params
    private final Element  e;
    private final Render   owner;
    private final double   end_x, end_y;
    private int            frame;

    public VisualiserAnimation (Render owner, Element e, double end_x, double end_y){
        this.owner = owner;
        this.e = e;
        this.end_x = end_x;
        this.end_y = end_y;
        timeline = new Timeline();
        keyframe = null;
    }
}
