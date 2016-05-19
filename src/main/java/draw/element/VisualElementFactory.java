package draw.element;

import contract.datastructure.Element;
import javafx.scene.paint.Paint;

/**
 * Factory class for {@link #VisualElement}.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class VisualElementFactory {

	private VisualElementFactory() {
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
	 *            Secondary size value, usually height. Sometimes ignored for
	 *            shapes like circles.
	 * @return The element to bind.
	 */
	public static VisualElement shape(ElemShape shape, Element e, double pri, double sec) {
		VisualElement vis = null;

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
			vis = new PolygonElement(e, pri, sec, trapezoid(pri, sec));
			break;
		case TRIANGLE:
			vis = new PolygonElement(e, pri, sec, triangle(pri, sec));
			break;
		case BAR_ELEMENT:
			vis = new BarchartElement(e, pri, sec);
			break;
		default:
			break;

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
	 *            Secondary size value, usually height. Sometimes ignored for
	 *            shapes like circles.
	 * @return A VisualElement.
	 */
	public static VisualElement shape(ElemShape shape, double value, Paint paint, double pri, double sec) {
		VisualElement vis = null;

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
			vis = new PolygonElement(value, paint, pri, sec, trapezoid(pri, sec));
			break;
		case TRIANGLE:
			vis = new PolygonElement(value, paint, pri, sec, triangle(pri, sec));
			break;
		case BAR_ELEMENT:
			vis = new BarchartElement(value, paint, pri, sec);
			break;
		default:
			break;
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
	 *            Secondary size value, usually height. Sometimes ignored for
	 *            shapes like circles.
	 * @return A {@link PolygonElement}.
	 */
	public static PolygonElement polygon(Element e, double pri, double sec, double[] points) {
		PolygonElement vis = new PolygonElement(e, pri, sec, points);
		vis.elemShape = ElemShape.POLYGON;
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
	 *            Secondary size value, usually height. Sometimes ignored for
	 *            shapes like circles.
	 * @return A {@link PolygonElement}.
	 */
	public static PolygonElement polygon(double value, Paint paint, double pri, double sec, double[] points) {
		PolygonElement vis = new PolygonElement(value, paint, pri, sec, points);
		vis.elemShape = ElemShape.POLYGON;
		return vis;
	}

	/*
	 * Polygon methods
	 */
	/**
	 * Creates the points for a relative polygon. Values in {@code points} are
	 * scaled using the width and height arguments. All values in {@code points}
	 * should lie in [0, 1].
	 * 
	 * @param w
	 *            The width of the polgyon.
	 * @param h
	 *            The height of the polgyon.
	 * @param points
	 *            Relative points for the polygon.
	 * @return A polygon scaled to {code w} and {code h}.
	 */
	public static double[] scalePolygon(double w, double h, double[] points) {
		double x, y;
		int xInd, yInd;

		for (int i = 1; i < points.length; i++) {
			xInd = i - 1;
			yInd = i;
			x = points[xInd];
			y = points[yInd];

			if (x < 0 || x > 1) {
				//Do not remove this printout //RS
				System.err.println("Bad x-coordinate at index " + xInd + ": " + x);
			}
			if (y < 0 || y > 1) {
				//Do not remove this printout //RS
				System.err.println("Bad y-coordinate at index " + yInd + ": " + y);
			}

			points[xInd] = x * w;
			points[yInd] = y * h;
		}
		addOne(points);
		return points;
	}

	/**
	 * Create triangle points.
	 * 
	 * @param w
	 *            The width of the polygon.
	 * @param h
	 *            The width of the polygon.
	 * @return The points for a triangle polygon.
	 */
	//@formatter:off
	public static double[] triangle(double w, double h) {
		double[] points = { //Points (x, y)
							w * 0.5, 0,
							w, h,
							0, h
						  };
		addOne(points);
		return points;
	}
	//@formatter:on

	/**
	 * Create trapezoid points.
	 * 
	 * @param w
	 *            The width of the polygon.
	 * @param h
	 *            The width of the plygon.
	 * @return The points for a trapezoid polygon.
	 */
	//@formatter:off
	public static double[] trapezoid(double w, double h) {
		double[] points = { //Points (x, y)
							w * 0.75, 0,
							w * 0.25, 0,
							0, h,
							w, h
			  			  };
		addOne(points);
		return points;
	}
	//@formatter:off
	
	private static void addOne(double[] points){
		for (double outer : points) {
			if (outer == 0){
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
	 * Settings class for use by JSON.
	 * 
	 * @author Richard Sundqvist
	 *
	 */
	private static class VisualElementSettings {
//		public ElemShape shape;
//		public double width;
//		public double height;
//		public double[] points;
	}

	/**
	 * Attempt to a visual element. Should not be used unless there is no other alternative.
	 * @param orig The original element.
	 * @return A clone of the original element.
	 */
	public static VisualElement clone(VisualElement orig) {
		VisualElement clone;
		//Shape
		if(orig.points == null){
			//Unbound
			if(orig.element == null){
				clone = shape(orig.elemShape, Double.parseDouble(orig.valueLabel.getText()), orig.getShape().getFill(),
						orig.width, orig.height);
			//Bound
			} else {
				clone = shape(orig.elemShape, orig.element,
						orig.width, orig.height);
			}
		//Polygon
		} else {
			//Unbound
			if(orig.element == null){
				clone = polygon(Double.parseDouble(orig.valueLabel.getText()), orig.getShape().getFill(),
						orig.width, orig.height, orig.points);
			//Bound
			} else {
				clone = polygon(orig.element,
						orig.width, orig.height, orig.points);
			}
		}
		return clone;
	}
}
