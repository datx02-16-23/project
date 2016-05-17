package application.visualization.render_FX;

import java.util.Arrays;

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
		((Rectangle) shape).setHeight(node_height);
	}
	
	public RectangleElement clone(){
		RectangleElement clone;
		
		if(element == null){ //Unbound
			clone = new RectangleElement(Double.parseDouble(value.getText()), (Color) shape.getFill(), node_width, node_height);
		} else { //Bound
			clone = new RectangleElement(element, node_width, node_height);
		}
		
		return clone;
	}
	
	//TODO
	public void setIndex(int[] index){
		super.index.setText(Arrays.toString(index));
	}
}
