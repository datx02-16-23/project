package application.visualization.render_FX;

import java.util.Arrays;
import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import wrapper.datastructures.Element;

//TODO
public class PolygonElement extends VisualElement {
	private static final String url = "/visualization/PolygonElement.fxml";
	private double[] points;

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

	/**
	 * Init the shape of the polygon. Even numbers are x-values, odd numbers are y-values.
	 * @param points The list of shapes.
	 */
	private void init(double[] points) {
		List<Double> polygon_points = ((Polygon) shape).getPoints();
		polygon_points.clear();
		for(int i = 0; i < points.length; i++){
			polygon_points.add(points[i]);
		}
		this.points = points;
	}
	
	public PolygonElement clone(){
		PolygonElement clone;
		
		if(element == null){ //Unbound
			clone = new PolygonElement(Double.parseDouble(value.getText()), (Color) shape.getFill(), node_width, node_height, points);
		} else { //Bound
			clone = new PolygonElement(element, node_width, node_height, points);
		}
		
		return clone;
	}
	
	//TODO
	public void setIndex(int[] index){
		super.index.setText(Arrays.toString(index));
	}
}
