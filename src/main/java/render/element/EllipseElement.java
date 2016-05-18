package render.element;

import contract.datastructure.Element;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;

public class EllipseElement extends VisualElement {

	/**
	 * Create a static, unbound EllipseElement.
	 * 
	 * @param value
	 *            The initial value.
	 * @param paint
	 *            The paint to use.
	 * @param node_width
	 *            The width of the node.
	 * @param node_height
	 *            The height of the node.
	 */
	public EllipseElement(double value, Paint paint, double node_width, double node_height) {
		super(value, paint, node_width, node_height);
	}

	/**
	 * Create a bound visual EllipseElement.
	 * 
	 * @param element
	 *            The Element this VisualElement represents
	 * @param node_width
	 *            The width of the node.
	 * @param node_height
	 *            The height of the node.
	 */
	public EllipseElement(Element element, double node_width, double node_height) {
		super(element, node_width, node_height);
	}

	@Override
	public Shape createShape() {
		Ellipse ellipse = new Ellipse();
		ellipse.setRadiusX(node_width / 2);
		ellipse.setRadiusY(node_height / 2);
		ellipse.setStroke(Color.BLACK);
		return ellipse;
	}

	public EllipseElement clone() {
		EllipseElement clone;
		if (element == null) {
			clone = new EllipseElement(Double.parseDouble(value.getText()), shape.getFill(), node_width, node_height);
		} else {
			clone = new EllipseElement(element, node_width, node_height);

		}
		return clone;
	}
}
