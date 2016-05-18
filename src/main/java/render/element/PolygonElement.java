package render.element;

import contract.datastructure.Element;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;

public class PolygonElement extends VisualElement {

	/**
	 * Create a static, unbound RectangleElement.
	 * 
	 * @param value
	 *            The initial value.
	 * @param paint
	 *            The style to use.
	 * @param node_width
	 *            The width of the node.
	 * @param node_height
	 *            The height of the node.
	 * @param points
	 *            The list of points for this PolygonElement.
	 */

	public PolygonElement(double value, Paint paint, double node_width, double node_height, double[] points) {
		super(value, paint, node_width, node_height, points);
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
