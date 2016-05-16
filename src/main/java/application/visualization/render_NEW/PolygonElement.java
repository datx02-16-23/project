package application.visualization.render_NEW;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import wrapper.datastructures.Element;

//TODO
public class PolygonElement extends VisualElement {
	private static final String url = "/visualization/PolygonElement.fxml";

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
		super(value, style, node_width, node_height, url);
		init(points);
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
		super(element, node_width, node_height, url);
		init(points);
	}

	private void init(double[] points) {
//		((Polygon) shape).getPoints().addAll(points);
	}
}
