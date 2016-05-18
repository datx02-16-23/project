package application.visualization.render_FX.elements;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import wrapper.datastructures.Element;

public class RectangleElement extends VisualElement {

	/**
	 * Create a static, unbound RectangleElement.
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
	public RectangleElement(double value, Paint paint, double node_width, double node_height) {
		super(value, paint, node_width, node_height);
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
	 */
	public RectangleElement(Element element, double node_width, double node_height) {
		super(element, node_width, node_height);
	}

	@Override
	public Shape createShape() {
		Rectangle rectangle = new Rectangle();
		rectangle.setWidth(node_width);
		rectangle.setHeight(node_height);
		rectangle.setStroke(Color.BLACK);
		return rectangle;
	}
	
	public RectangleElement clone() {
		RectangleElement clone;
		if (element == null) {
			System.out.println("clone free shape");
			clone = new RectangleElement(Double.parseDouble(value.getText()), shape.getFill(), node_width, node_height);
		} else {
			System.out.println("clone bound shape");
			clone = new RectangleElement(element, node_width, node_height);

		}
		return clone;
	}
}
