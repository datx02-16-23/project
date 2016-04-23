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

    private double points[][];

    public LinearAnimation (Render owner, Element e, double end_x, double end_y){
        super(owner, e);
        points = linearAnimationPath(Render.getAbsoluteX(owner, e), Render.getAbsoluteY(owner, e), end_x, end_y, frame_count, 0.3);
        KeyFrame keyframe = new KeyFrame(Duration.millis((ANIMATION_TIME / frame_count) * 0.5), event -> {
            owner.clearAnimatedElement(e, points[0][frame - 1], points[1][frame - 1]);
            owner.drawAnimatedElement(e, points[0][frame], points[1][frame], e.getColor());
            frame++;
        });
        keyframes.add(keyframe);
    }

    @Override
    protected void ensureCleared (){
        for (int i = 0; i < points[0].length; i++) {
            owner.clearAnimatedElement(e, points[0][i], points[1][i]);
        }
    }
}
