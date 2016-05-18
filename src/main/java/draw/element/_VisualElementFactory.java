package draw.element;

import contract.datastructure.Element;
import javafx.scene.paint.Paint;

/**
 * Factory class for {@link #VisualElement}.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class _VisualElementFactory {
	private _VisualElementFactory() {
	} // Not to be instantiated.

	/**
	 * Create a VisualElement bound to an {@link #Element}.
	 * 
	 * @param shape
	 *            The shape of the element.
	 * @param primary
	 *            Primary size value, usually width.
	 * @param secondary
	 *            Secondary size value, usually height. Sometimes ignored for
	 *            shapes like circles.
	 * @return The element to bind.
	 */
	public static VisualElement shape(ElemShape shape, Element e, double primary, double secondary) {
		VisualElement vis = null;

		switch (shape) {
		case CIRCLE:
			vis = new EllipseElement(e, primary, primary);
			break;
		case ELLIPSE:
			vis = new EllipseElement(e, primary, secondary);
			break;
		case RECTANGLE:
			vis = new RectangleElement(e, primary, secondary);
			break;
		case SQUARE:
			vis = new RectangleElement(e, primary, primary);
			break;
		case TRAPEZOID:
			vis = new PolygonElement(e, primary, secondary, trapezoid(primary, secondary));
			break;
		case TRIANGLE:
			vis = new PolygonElement(e, primary, secondary, triangle(primary, secondary));
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
	 * @param primary
	 *            Primary size value, usually width.
	 * @param secondary
	 *            Secondary size value, usually height. Sometimes ignored for
	 *            shapes like circles.
	 * @return A VisualElement.
	 */
	public static VisualElement shape(ElemShape shape, double value, Paint paint, double primary, double secondary) {
		VisualElement vis = null;

		switch (shape) {
		case CIRCLE:
			vis = new EllipseElement(value, paint, primary, primary);
			break;
		case ELLIPSE:
			vis = new EllipseElement(value, paint, primary, secondary);
			break;
		case RECTANGLE:
			vis = new RectangleElement(value, paint, primary, secondary);
			break;
		case SQUARE:
			vis = new RectangleElement(value, paint, primary, primary);
			break;
		case TRAPEZOID:
			vis = new PolygonElement(value, paint, primary, secondary, trapezoid(primary, secondary));
			break;
		case TRIANGLE:
			vis = new PolygonElement(value, paint, primary, secondary, triangle(primary, secondary));
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
	 * @param primary
	 *            Primary size value, usually width.
	 * @param secondary
	 *            Secondary size value, usually height. Sometimes ignored for
	 *            shapes like circles.
	 * @return A {@link PolygonElement}.
	 */
	public static VisualElement polygon(Element e, double primary, double secondary, double[] points) {
		VisualElement vis = new PolygonElement(e, primary, secondary, points);
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
	 * @param primary
	 *            Primary size value, usually width.
	 * @param secondary
	 *            Secondary size value, usually height. Sometimes ignored for
	 *            shapes like circles.
	 * @return A {@link PolygonElement}.
	 */
	public static VisualElement polygon(double value, Paint paint, double primary, double secondary, double[] points) {
		VisualElement vis = new PolygonElement(value, paint, primary, secondary, points);
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

		for (int i = 0; i < points.length; i++) {
			xInd = i;
			yInd = i + 1;
			x = points[xInd];
			y = points[yInd];

			if (x < 0 || x > 1) {
				System.err.println("Bad x-coordinate at index " + xInd + ": " + x);
			}
			if (y < 0 || y > 1) {
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
	public static class VisualElementSettings {
		public ElemShape shape;
		public double width;
		public double height;
	}

	/**
	 * Clone a visual element.
	 * @param orig The original element.
	 * @return A clone of the original element.
	 */
	public static VisualElement clone(VisualElement orig) {
		VisualElement vis;
		//Shape
		if(orig.points == null){
			//Unbound
			if(orig.element == null){
				vis = shape(orig.elemShape, Double.parseDouble(orig.value.getText()), orig.getShape().getFill(), orig.width, orig.height);
			//Bound
			} else {
				vis = shape(orig.elemShape, orig.element, orig.width, orig.height);
			}
		//Polygon
		} else {
			//Unbound
			if(orig.element == null){
				vis = polygon(Double.parseDouble(orig.value.getText()), orig.getShape().getFill(), orig.width, orig.height, orig.points);
			//Bound
			} else {
				vis = polygon(orig.element, orig.width, orig.height, orig.points);
			}
		}
		return vis;
	}
}
