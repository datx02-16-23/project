package application.visualization.render_FX.elements;

import java.util.Arrays;

import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import wrapper.datastructures.Element;

//TODO
public class PolygonElement extends VisualElement {

	/**
	 * Create a static, unbound RectangleElement.
	 * 
	 * @param value
	 *            The initial value.
	 * @param style
	 *            The style to use.
	 * @param node_width
	 *            The width of the node.
	 * @param node_height
	 *            The height of the node.
	 * @param points
	 *            The list of points for this PolygonElement.
	 */

	public PolygonElement(double value, Color style, double node_width, double node_height, double[] points) {
		super(value, style, node_width, node_height, points);
	}

	/**
	 * Create a bound visual RectangleElement.
	 * 
	 * @param element
	 *            The Element this VisualElement represents
	 * @param node_width
	 *            The width of the node.
	 * @param node_height
	 *            The height of the node.
	 * @param points
	 *            The list of points for this PolygonElement.
	 */
	public PolygonElement(Element element, double node_width, double node_height, double[] points) {
		super(element, node_width, node_height, points);
	}

	@Override
	public Shape createShape() {
		return super.createShape();
	}
}
