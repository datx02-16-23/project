package application.visualization.render_NEW;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import wrapper.datastructures.Element;

public class RectangleElement extends VisualElement {
	private static final String url = "/visualization/RectangleElement.fxml";

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
	 */
	public RectangleElement(double value, Color style, double node_width, double node_height) {
		super(value, style, node_width, node_height, url);
		init();
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
		super(element, node_width, node_height, url);
		init();
	}
	
	private void init(){
		((Rectangle) shape).setWidth(node_width);
		((Rectangle) shape).setWidth(node_height);
	}
}