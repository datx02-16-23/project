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

	/**
	 * Creates a LinearAnimation between two points.
	 * 
	 * @param owner
	 *            The owner of the moving element.
	 * @param e
	 *            The moving element.
	 * @param start_x
	 *            Starting x.
	 * @param start_y
	 *            Starting y.
	 * @param end_x
	 *            End x.
	 * @param end_y
	 *            End y.
	 */
	public LinearAnimation(Render owner, Element e, double start_x, double start_y, double end_x, double end_y) {
		super(owner, e);
		points = linearAnimationPath(start_x, start_y, end_x, end_y, frame_count, 0.3);
		KeyFrame keyframe = new KeyFrame(Duration.millis((ANIMATION_TIME / frame_count) * 0.5), event -> {
			owner.clearAnimatedElement(e, points[0][frame - 1], points[1][frame - 1]);
			owner.drawAnimatedElement(e, points[0][frame], points[1][frame], e.getColor());
			frame++;
		});
		keyframes.add(keyframe);
	}

	/**
	 * Creates a LinearAnimation between the moving element e's current position
	 * and an end point.
	 * 
	 * @param owner
	 *            The owner of the moving element.
	 * @param e
	 *            The moving element.
	 * @param end_x
	 *            End x.
	 * @param end_y
	 *            End y.
	 */
	public LinearAnimation(Render owner, Element e, double end_x, double end_y) {
		this(owner, e, owner.absX(e), owner.absY(e), end_x, end_y);
	}

	@Override
	protected void ensureCleared() {
//		for (int i = 0; i < points[0].length; i++) {
		for (int i = 0; i <= frame; i++) {
			owner.clearAnimatedElement(e, points[0][i], points[1][i]);
		}
	}
}
