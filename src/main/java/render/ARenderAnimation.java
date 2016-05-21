package render;

import java.util.Arrays;

import assets.Debug;
import contract.datastructure.Element;
import contract.datastructure.Array.IndexedElement;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import render.element.AVElement;

/**
 * Utility class for animating operations. Cannot be instantiated.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class ARenderAnimation {

	private ARenderAnimation() {
	}// Not to be instantiated.

	/**
	 * Animate moving an element from <i>(x1, y1)</i> to <i>(x2, y2))</i>.
	 * Calling
	 * {@link ParallelTransition#setOnFinished(javafx.event.EventHandler)} on
	 * the transition returned by this method may have unwanted side effects.
	 * 
	 * @param e
	 *            The element to animate.
	 * @param x1
	 *            Start point x-coordinate.
	 * @param y1
	 *            Start point y-coordinate.
	 * @param x2
	 *            End point x-coordinate.
	 * @param y2
	 *            End point y-coordinate.
	 * @param millis
	 *            The time in milliseconds the animation should last.
	 * @return A ParallelTransition with the requested transitions as children.
	 */
	//@formatter:off
	public static ParallelTransition linear(Element e,
			double x1, double y1,
			double x2, double y2,
			long millis, ARender render,
			AnimationOption... options) {
	//@formatter:on	

		// Fetch the element to animate
		// VisualElement orig = visualElementsMapping.get(e);
		int[] i = ((IndexedElement) e).getIndex();
		Arrays.copyOf(i, i.length);
		final AVElement orig = render.visualMap.get(Arrays.toString(i));
		if (Debug.ERR) {
			if (orig == null) {
				System.err.println("ARender.animte() failure: Could not resolve element for using:" + render);
				java.awt.Toolkit.getDefaultToolkit().beep();
				return new ParallelTransition();
			}
		}
		ParallelTransition transition = AnimationOption.buildTransition(orig, render, millis, options);

		/*
		 * Add movement.
		 */
		TranslateTransition tt = new TranslateTransition(Duration.millis(millis));
		tt.setFromX(x1);
		tt.setFromY(y1);
		tt.setToX(x2);
		tt.setToY(y2);

		transition.getChildren().add(tt);

		/*
		 * Showtime!!
		 */
		return transition;
	}

	/**
	 * Animate an element without translation. Calling
	 * {@link ParallelTransition#setOnFinished(javafx.event.EventHandler)} on
	 * the transition returned by this method may have unwanted side effects.
	 * 
	 * @param e
	 *            The element to animate.
	 * @param x
	 *            X-coordinate for the animation.
	 * @param y
	 *            Y-coordinate for the animation.
	 * @param millis
	 *            The time in milliseconds the animation should last.
	 * @return A ParallelTransition with the requested transitions as children.
	 */
	//@formatter:off
	public static ParallelTransition stationary(Element e,
			double x, double y,
			long millis, ARender render,
			AnimationOption... options) {
	//@formatter:on	

		// Fetch the element to animate
		// VisualElement orig = visualElementsMapping.get(e);
		int[] i = ((IndexedElement) e).getIndex();
		Arrays.copyOf(i, i.length);
		final AVElement orig = render.visualMap.get(Arrays.toString(i));
		if (Debug.ERR) {
			if (orig == null) {
				System.err.println("ARender.animte() failure: Could not resolve element for using:" + render);
				java.awt.Toolkit.getDefaultToolkit().beep();
				return new ParallelTransition();
			}
		}
		ParallelTransition pt = AnimationOption.buildTransition(orig, render, millis, options);

		TranslateTransition tt = new TranslateTransition(Duration.ZERO);
		tt.setToX(x);
		tt.setToY(y);
		pt.getChildren().add(tt);

		return pt;
	}

	/**
	 * Animation options.
	 * 
	 * @author Richard
	 *
	 */
	public static enum AnimationOption {
		/**
		 * Make the element to fade in.
		 */
		FADE_IN,
		/**
		 * Make the element to fade out.
		 */
		FADE_OUT,
		/**
		 * Make the element rotate 180 degrees on the Y-axis.
		 */
		FLIP,
		/**
		 * Turn the element into a ghost until animation is complete.
		 */
		USE_GHOST,
		/**
		 * Make the element scale from 1.0 to 0.0.
		 */
		SHRINK,
		/**
		 * Make the element scale from 0.0 to 1.0.
		 */
		GROW;

		/**
		 * Add transitions for the given options to the parent transition.
		 * 
		 * @param orig
		 *            The original element. <b>Must not be null.</b>
		 * @param render
		 *            The render which should do the animation. <b>Must not be
		 *            null.</b>
		 * @param millis
		 *            The animation time in milliseconds.
		 * @param options
		 *            A list of options.
		 * @return A ParallelTransition with child transitions specified by
		 *         {@code options}.
		 */
		public static ParallelTransition buildTransition(AVElement orig, ARender render, long millis,
				AnimationOption... options) {

			final AVElement animated = orig.clone();

			// Make sure the animated element doesn't update with the model.
			animated.unbind();
			animated.setScaleX(render.getScaleX());
			animated.setScaleY(render.getScaleY());

			render.animPane.getChildren().add(animated);

			/**
			 * Create transition and add optional transitions.
			 */
			ParallelTransition parent = new ParallelTransition(animated);

			boolean originalGhostDuringAnimation = false;

			for (AnimationOption opt : options) {
				switch (opt) {
				case FADE_IN:
					parent.getChildren().add(fadeIn(millis));
					break;
				case FADE_OUT:
					parent.getChildren().add(fadeOut(millis));
					break;
				case SHRINK:
					parent.getChildren().add(shrink(millis));
					break;
				case GROW:
					parent.getChildren().add(grow(millis));
					break;
				case FLIP:
					parent.getChildren().add(flip(millis));
					break;
				case USE_GHOST:
					originalGhostDuringAnimation = true;
					break;
				default:
					break;
				}
			}

			// Must have final value for setOnFinished().
			final boolean finalGhost = originalGhostDuringAnimation;
			orig.setGhost(finalGhost);

			parent.setOnFinished(event -> {
				if (finalGhost) {
					orig.setGhost(false);
				}
				render.animPane.getChildren().remove(animated);
			});

			return parent;
		}

		private static Animation flip(long millis) {
			RotateTransition rt = new RotateTransition(Duration.millis(millis));
			rt.setByAngle(180);
			return rt;
		}

		public static Animation grow(long millis) {
			ScaleTransition st = new ScaleTransition(Duration.millis(millis));
			st.setFromX(0);
			st.setFromY(0);
			st.setToX(1);
			st.setToY(1);
			return st;
		}

		public static Animation shrink(long millis) {
			ScaleTransition st = new ScaleTransition(Duration.millis(millis));
			st.setFromX(1);
			st.setFromY(1);
			st.setToX(0);
			st.setToY(0);
			return st;
		}

		public static final Transition fadeIn(long millis) {
			FadeTransition ft = new FadeTransition(Duration.millis(millis));
			ft.setFromValue(0);
			ft.setToValue(1.0);
			return ft;
		}

		public static final Transition fadeOut(long millis) {
			FadeTransition ft = new FadeTransition(Duration.millis(millis));
			ft.setFromValue(1.0);
			ft.setToValue(0);
			return ft;
		}
	}
}
