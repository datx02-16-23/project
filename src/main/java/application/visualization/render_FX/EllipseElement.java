package application.visualization.render_FX;

import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import wrapper.datastructures.Element;

public class EllipseElement extends VisualElement {
	private static final String url = "/visualization/EllipseElement.fxml";

	/**
	 * Create a static, unbound EllipseElement.
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
	public EllipseElement(double value, Color style, double node_width, double node_height) {
		super(value, style, node_width, node_height, url);
		init();
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
		super(element, node_width, node_height, url);
		init();
	}
	
	private void init(){
		((Ellipse) shape).setRadiusX(node_width/2);
		((Ellipse) shape).setRadiusY(node_height/2);
	}
	
	public EllipseElement clone(){
		EllipseElement clone;
		
		if(element == null){ //Unbound
			clone = new EllipseElement(Double.parseDouble(value.getText()), (Color) shape.getFill(), node_width, node_height);
		} else { //Bound
			clone = new EllipseElement(element, node_width, node_height);
		}
		
		return clone;
	}
}
