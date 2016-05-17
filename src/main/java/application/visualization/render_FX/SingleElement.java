package application.visualization.render_FX;

import java.util.Arrays;

import javafx.scene.paint.Color;
import wrapper.datastructures.Element;

/**
 * A data structure containing a single element.
 * 
 * @author Richard Sundqvist
 *
 */
public class SingleElement extends PolygonElement {

	/**
	 * Create the shape for the element.
	 * 
	 * @param w
	 *            The width of the element.
	 * @param h
	 *            The width of the element.
	 * @return Points for a polygon.
	 */
	//@formatter:off
	public static double[] create_points(double w, double h) {
		double[] points = {
				w*0.15, 0,
				w*0.85, 0,
				
				w, h*0.25,
				w, h*0.75,
				w*0.85, h,
				
				w*0.15, h,
				0, h*0.75,
				0, h*0.25
						  };
		for(double d : points){
			d = d + 1;
		}
		return points;
	}
	//@formatter:off
	
	/**
	 * Create a static, unbound SingleElement.
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
	public SingleElement(double value, Color style, double node_width, double node_height) {
		super(value, style, node_width, node_height, create_points(node_width, node_height));
	}

	/**
	 * Create a bound SingleElement.
	 * 
	 * @param element
	 *            The Element this VisualElement represents
	 * @param node_width
	 *            The width of the node.
	 * @param node_height
	 *            The height of the node.
	 */
	public SingleElement(Element element, double node_width, double node_height) {
		super(element, node_width, node_height, create_points(node_width, node_height));
	}

}
