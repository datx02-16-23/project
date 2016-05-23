package render;

import java.util.Arrays;

import assets.Debug;
import contract.datastructure.Array.IndexedElement;
import contract.datastructure.Element;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import render.element.AVElement;

/**
 * Utility class for animating operations. Cannot be instantiated.
 *
 * @author Richard Sundqvist
 *
 */
public abstract class ARenderAnimation {

    private ARenderAnimation () {
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
    // @formatter:off
    public static ParallelTransition linear(Element e, double x1, double y1, double x2, double y2, long millis,
	    ARender render, Effect... options) {
	// @formatter:on

        // Fetch the element to animate
        // VisualElement orig = visualElementsMapping.get(e);
        int[] i = ((IndexedElement) e).getIndex();
        Arrays.copyOf(i, i.length);
        final AVElement orig = render.visualMap.get(Arrays.toString(i));
        if (Debug.ERR) {
            if (orig == null) {
                System.err.println(
                        "ARenderAnimation.linear() failure: Could not resolve element " + e + " using: " + render);
                java.awt.Toolkit.getDefaultToolkit().beep();
                return new ParallelTransition();
            }
        }
        ParallelTransition transition = Effect.buildTransition(orig, render, millis, options);

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
    // @formatter:off
    public static ParallelTransition stationary(Element e, double x, double y, long millis, ARender render,
	    Effect... options) {
	// @formatter:on

        // Fetch the element to animate
        // VisualElement orig = visualElementsMapping.get(e);
        int[] i = ((IndexedElement) e).getIndex();
        Arrays.copyOf(i, i.length);
        final AVElement orig = render.visualMap.get(Arrays.toString(i));
        if (Debug.ERR) {
            if (orig == null) {
                System.err.println("ARenderAnimation.stationary() failure: Could not resolve element  " + e
                        + "  using: " + render);
                java.awt.Toolkit.getDefaultToolkit().beep();
                return new ParallelTransition();
            }
        }
        ParallelTransition pt = Effect.buildTransition(orig, render, millis, options);

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
    public static enum Effect {
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
         * Turn the element into a ghost until animation is complete. The ghost
         * will use the old value (and size, if applicable) until animation is
         * complete.
         */
        GHOST,
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
         *            The original element. Will not change, but <b>must not be
         *            null.</b>
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
        public static ParallelTransition buildTransition (AVElement orig, ARender render, long millis,
                Effect... options) {

            if (orig == null) {
                System.err.println("Error in ARenderAnimation.ParallelTransition(): orig == null");
                return new ParallelTransition();
            }
            final AVElement clone = orig.clone();
            if (clone == null) {
                System.err.println("Error in ARenderAnimation.ParallelTransition(): orig.clone() == null");
                return new ParallelTransition();
            }

            // Make sure the animated element doesn't update with the model.
            clone.unbind();
            // clone.setScaleX(render.getScaleX());
            // clone.setScaleY(render.getScaleY());
            render.animPane.getChildren().add(clone);

            /**
             * Create parent transition and add optional transitions.
             */
            ParallelTransition pt = new ParallelTransition(clone);

            boolean useGhost = false;

            ObservableList<Animation> ptChildren = pt.getChildren();

            for (Effect opt : options) {
                switch (opt) {
                case FADE_IN:
                    ptChildren.add(fadeIn(millis));
                    break;
                case FADE_OUT:
                    ptChildren.add(fadeOut(millis));
                    break;
                case SHRINK:
                    ptChildren.add(shrink(millis));
                    break;
                case GROW:
                    ptChildren.add(grow(millis));
                    break;
                case FLIP:
                    ptChildren.add(flip(millis));
                    break;
                case GHOST:
                    useGhost = true;
                    break;
                }
            }

            setOnFinished(render, orig, clone, pt, useGhost);

            return pt;
        }

        private static void setOnFinished (ARender render, AVElement orig, final AVElement clone, ParallelTransition pt,
                final boolean ghost) {

            if (ghost) {
                orig.setGhost(true);
            }

            pt.setOnFinished(event -> {
                if (ghost) {
                    orig.setGhost(false);
                }

                render.animPane.getChildren().remove(clone);
            });
        }

        private static Animation flip (long millis) {
            RotateTransition rt = new RotateTransition(Duration.millis(millis));
            rt.setByAngle(180);
            return rt;
        }

        public static Animation grow (long millis) {
            ScaleTransition st = new ScaleTransition(Duration.millis(millis));
            st.setFromX(0);
            st.setFromY(0);
            st.setToX(1);
            st.setToY(1);
            return st;
        }

        public static Animation shrink (long millis) {
            ScaleTransition st = new ScaleTransition(Duration.millis(millis));
            st.setFromX(1);
            st.setFromY(1);
            st.setToX(0);
            st.setToY(0);
            return st;
        }

        public static final Transition fadeIn (long millis) {
            FadeTransition ft = new FadeTransition(Duration.millis(millis));
            ft.setFromValue(0);
            ft.setToValue(1.0);
            return ft;
        }

        public static final Transition fadeOut (long millis) {
            FadeTransition ft = new FadeTransition(Duration.millis(millis));
            ft.setFromValue(1.0);
            ft.setToValue(0);
            return ft;
        }
    }
}
