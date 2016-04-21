package application.visualization.animation;

import application.visualization.render2d.Render;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import wrapper.datastructures.Element;

/**
 * Helper class to move an element in a straight line.
 * 
 * @author Richard Sundqvist
 *
 */
public class LinearAnimation extends Animation {

    //Params
    private final Element e;
    private final double  end_x, end_y;

    public LinearAnimation (Render owner, Element e, double end_x, double end_y){
        super(owner);
        this.e = e;
        this.end_x = end_x;
        this.end_y = end_y;
        double points[][] = linearPoints(owner.getX(e), owner.getY(e), end_x, end_x, FRAMES); 
        KeyFrame keyframe = new KeyFrame(Duration.millis(ANIMATION_TIME / FRAMES), event -> {
          owner.drawAnimatedElement(e, points[0][frame], points[1][frame], e.getColor());
          frame++;
          System.out.println("frame!");
        });
        keyframes.add(keyframe);
        timeline.playFromStart();
    }
}
