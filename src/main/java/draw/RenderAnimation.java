package draw;

import java.util.Arrays;
import contract.datastructure.Element;
import contract.datastructure.Array.IndexedElement;
import draw.element.VisualElement;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

/**
 * Utility class for animating operations. Cannot be instantiated.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class RenderAnimation {

	private RenderAnimation() {
	}// Not to be instantiated.

	/**
	 * Start an animation of an element to a point.
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
	 */
	//@formatter:off
	public static void animate(Element e,
			double x1, double y1,
			double x2, double y2,
			long millis, ARender render,
			AnimationOption... options) {
	//@formatter:on	

		ParallelTransition transition = new ParallelTransition();

		// VisualElement real = visualElementsMapping.get(e);
		int[] i = ((IndexedElement) e).getIndex();
		Arrays.copyOf(i, i.length);

		final VisualElement real = render.visualMap.get(Arrays.toString(i));
		if (real == null) {
			// Do not remove this printout //RS
			// TODO render.visualMap.get(Arrays.toString(i)) may return null
			System.err.println("Animation failed: Failed resolve element for: " + render.struct);
			return;
		}

		VisualElement animated = real.clone();
		animated.unbind();
		animated.setScaleX(render.getScaleX());
		animated.setScaleY(render.getScaleY());

		render.animation_pane.getChildren().add(animated);

		final boolean finalUseGhost;
		boolean ghost = false;

		for (AnimationOption opt : options) {
			switch (opt) {
			case FADE_IN:
				transition.getChildren().add(fadeIn(millis));
				break;
			case FADE_OUT:
				transition.getChildren().add(fadeOut(millis));
			case SHRINK:
				transition.getChildren().add(shrink(millis));
				break;
			case GROW:
				transition.getChildren().add(grow(millis));
				break;
			case SPIN:
				// TODO: Implement SPIN
				break;
			case USE_GHOST:
				ghost = true;
				break;
			default:
				break;
			}
		}
		finalUseGhost = ghost; // Must have final value for setOnFinished().
		real.setGhost(finalUseGhost);

		/*
		 * Move
		 */
		TranslateTransition tt = new TranslateTransition(Duration.millis(millis));
		tt.setOnFinished(event -> {
			render.animation_pane.getChildren().remove(animated);
			if (finalUseGhost) {
				real.setGhost(false);
			}
		});
		tt.setFromX(x1);
		tt.setFromY(y1);
		tt.setToX(x2);
		tt.setToY(y2);
		transition.getChildren().add(tt);

		/*
		 * Showtime!!
		 */
		transition.setNode(animated);
		transition.play();
	}

	private static Animation grow(long millis) {
		ScaleTransition st = new ScaleTransition(Duration.millis(millis));
		st.setFromX(0);
		st.setFromY(0);
		st.setToX(1);
		st.setToY(1);
		return st;
	}

	private static Animation shrink(long millis) {
		ScaleTransition st = new ScaleTransition(Duration.millis(millis));
		st.setFromX(1);
		st.setFromY(1);
		st.setToX(0);
		st.setToY(0);
		return st;
	}

	private static final Transition fadeIn(long millis) {
		FadeTransition ft = new FadeTransition(Duration.millis(millis));

		ft.setFromValue(1.0);
		ft.setToValue(0);
		return ft;
	}

	public static final Transition fadeOut(long millis) {
		FadeTransition ft = new FadeTransition(Duration.millis(millis));
		ft.setFromValue(1.0);
		ft.setToValue(0);
		return ft;
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
		 * Make the element to fade in.
		 */
		FADE_OUT,
		/**
		 * Make the element to fade in.
		 */
		SPIN,
		/**
		 * Make the element to fade in.
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
	}
}
