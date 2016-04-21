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

    private final double points[][];

    public LinearAnimation (Render owner, Element e, double end_x, double end_y){
        super(owner, e);
        double bx = owner.getCanvas().getTranslateX() + owner.getLayoutX();
        double by = owner.getCanvas().getTranslateY() + owner.getLayoutY();
//        System.out.println("owner = " + owner.getDataStructure());
//        System.out.println("getTranslate = " + owner.getTranslateX() +", " + owner.getTranslateY());
//        System.out.println("getLayout = " + owner.getLayoutX() +", " + owner.getLayoutY());
        points = linearPoints(bx + owner.getX(e), by + owner.getY(e), end_x, end_y, FRAMES);
        KeyFrame keyframe = new KeyFrame(Duration.millis(ANIMATION_TIME / FRAMES), event -> {
            owner.clearAnimatedElement(e, points[0][frame - 1], points[1][frame - 1]);
            owner.drawAnimatedElement(e, points[0][frame], points[1][frame], e.getColor());
            frame++;
        });
        keyframes.add(keyframe);
    }

    @Override
    protected void clearFinalFrame (){
        owner.clearAnimatedElement(e, points[0][FRAMES], points[1][FRAMES]);
    }
}
