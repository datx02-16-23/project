package render.element;

import assets.Debug;
import contract.datastructure.Element;
import javafx.scene.paint.Paint;

/**
 * Factory class for {@link #VisualElement}.
 *
 * @author Richard Sundqvist
 *
 */
public abstract class AVElementFactory {

    public static final double[] TRAPEZOID_POINTS = { 0.75, 0, 0.25, 0, 0, 1, 1, 1 };
    public static final double[] TRIANGLE_POINTS  = { 0.5, 0, 1, 1, 0, 1 };

    private AVElementFactory () {
    } // Not to be instantiated.

    /**
     * Create a VisualElement bound to an {@link #Element}.
     *
     * @param shape
     *            The shape of the element.
     * @param e
     *            The element to bind to.
     * @param pri
     *            Primary size value, usually width.
     * @param sec
     *            Secondary size value, usually height. Sometimes ignored for shapes like circles.
     * @return The element to bind.
     */
    public static AVElement shape (ElementShape shape, Element e, double pri, double sec) {
        AVElement vis = null;

        switch (shape) {
        case CIRCLE:
            vis = new EllipseElement(e, pri, pri);
            break;
        case ELLIPSE:
            vis = new EllipseElement(e, pri, sec);
            break;
        case RECTANGLE:
            vis = new RectangleElement(e, pri, sec);
            break;
        case SINGLE:
            vis = new SingleElement(e, pri, sec);
            break;
        case SQUARE:
            vis = new RectangleElement(e, pri, pri);
            break;
        case TRAPEZOID:
            vis = new PolygonElement(e, pri, sec, scalePolygon(pri, sec, TRAPEZOID_POINTS));
            break;
        case TRIANGLE:
            vis = new PolygonElement(e, pri, sec, scalePolygon(pri, sec, TRIANGLE_POINTS));
            break;
        case BAR_ELEMENT:
            vis = new BarchartElement(e, pri, sec);
            break;
        case RANDOM:
            return shape(ElementShape.random(), e, pri, sec);
        case POLYGON: // Pick a polygon at random if no points are provided.
            return shape(ElementShape.randomPolygon(), e, pri, sec);
        }

        vis.elemShape = shape;
        return vis;
    }

    /**
     *
     * /** Create an unbound VisualElement.
     *
     * @param shape
     *            The shape of the element.
     * @param value
     *            The value of the element.
     * @param paint
     *            The paint to use
     * @param pri
     *            Primary size value, usually width.
     * @param sec
     *            Secondary size value, usually height. Sometimes ignored for shapes like circles.
     * @return A VisualElement.
     */
    public static AVElement shape (ElementShape shape, double value, Paint paint, double pri, double sec) {
        AVElement vis = null;

        switch (shape) {
        case CIRCLE:
            vis = new EllipseElement(value, paint, pri, pri);
            break;
        case ELLIPSE:
            vis = new EllipseElement(value, paint, pri, sec);
            break;
        case RECTANGLE:
            vis = new RectangleElement(value, paint, pri, sec);
            break;
        case SINGLE:
            vis = new SingleElement(value, paint, pri, sec);
            break;
        case SQUARE:
            vis = new RectangleElement(value, paint, pri, pri);
            break;
        case TRAPEZOID:
            vis = new PolygonElement(value, paint, pri, sec, scalePolygon(pri, sec, TRAPEZOID_POINTS));
            break;
        case TRIANGLE:
            vis = new PolygonElement(value, paint, pri, sec, scalePolygon(pri, sec, TRIANGLE_POINTS));
            break;
        case BAR_ELEMENT:
            vis = new BarchartElement(value, paint, pri, sec);
            break;
        case POLYGON:
            return shape(ElementShape.randomPolygon(), value, paint, pri, sec);
        case RANDOM:
            return shape(ElementShape.random(), value, paint, pri, sec);
        }
        vis.elemShape = shape;
        return vis;
    }

    /**
     * Create a VisualElement bound to an {@link #Element}.
     *
     * @param shape
     *            The shape of the element.
     * @param e
     *            The element to bind to.
     * @param pri
     *            Primary size value, usually width.
     * @param sec
     *            Secondary size value, usually height. Sometimes ignored for shapes like circles.
     * @return A {@link PolygonElement}.
     */
    public static PolygonElement polygon (Element e, double pri, double sec, double[] points) {
        PolygonElement vis = new PolygonElement(e, pri, sec, points);
        vis.elemShape = ElementShape.POLYGON;
        return vis;
    }

    /**
     *
     * /** Create an unbound polygon.
     *
     * @param shape
     *            The shape of the element.
     * @param value
     *            The value of the element.
     * @param paint
     *            The paint to use
     * @param pri
     *            Primary size value, usually width.
     * @param sec
     *            Secondary size value, usually height. Sometimes ignored for shapes like circles.
     * @return A {@link PolygonElement}.
     */
    public static PolygonElement polygon (double value, Paint paint, double pri, double sec, double[] points) {
        PolygonElement vis = new PolygonElement(value, paint, pri, sec, points);
        vis.elemShape = ElementShape.POLYGON;
        return vis;
    }

    /*
     * Polygon methods
     */
    /**
     * Creates the points for a relative polygon. Values in {@code points} are scaled using the
     * width and height arguments. All values in {@code points} should lie in [0, 1].<br>
     *
     * The original list is not changed.
     *
     * @param w
     *            The width of the polgyon.
     * @param h
     *            The height of the polgyon.
     * @param points
     *            Relative points for the polygon.
     * @return A polygon scaled to {code w} and {code h}.
     */
    private static double[] scalePolygon (double w, double h, double[] points) {
        double x, y;
        int xInd, yInd;

        double[] scaled = new double[points.length];

        for (int i = 1; i < points.length; i = i + 2) {
            xInd = i - 1;
            yInd = i;
            x = points [xInd];
            y = points [yInd];

            if (Debug.ERR) {
                if (x < 0 || x > 1) {
                    System.err.println("Bad x-coordinate at index " + xInd + ": " + x);
                }
                if (y < 0 || y > 1) {
                    System.err.println("Bad y-coordinate at index " + yInd + ": " + y);
                }
            }

            scaled [xInd] = x * w;
            scaled [yInd] = y * h;
        }
        // addOne(points);
        return scaled;
    }

    public static void addOne (double[] points) {
        for (double outer : points) {
            if (outer == 0) {
                for (double inner : points) {
                    inner = inner + 1;
                }
                return;
            }
        }
    }

    /*
     *
     *
     * Supporter classes.
     *
     *
     */

    /**
     * Attempt to a visual element. Should not be used unless there is no other alternative.
     *
     * @param orig
     *            The original element.
     * @return A clone of the original element.
     */
    public static AVElement clone (AVElement orig) {
        AVElement clone;
        // Shape
        if (orig.points == null) {
            // Unbound
            if (orig.element == null) {
                clone = shape(orig.elemShape, Double.parseDouble(orig.valueLabel.getText()), orig.getShape().getFill(),
                        orig.width, orig.height);
                // Bound
            } else {
                clone = shape(orig.elemShape, orig.element, orig.width, orig.height);
            }
            // Polygon
        } else {
            // Unbound
            if (orig.element == null) {
                clone = polygon(Double.parseDouble(orig.valueLabel.getText()), orig.getShape().getFill(), orig.width,
                        orig.height, orig.points);
                // Bound
            } else {
                clone = polygon(orig.element, orig.width, orig.height, orig.points);
            }
        }

        return clone;
    }
}
