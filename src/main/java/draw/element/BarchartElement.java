package draw.element;

import contract.datastructure.Element;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * A resizable Rectangle element used by BarChart.
 * 
 * @author Richard
 *
 */
public class BarchartElement extends RectangleElement {

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
	public BarchartElement(double value, Paint paint, double node_width, double node_height) {
		super(value, paint, node_width, node_height);
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
	public BarchartElement(Element element, double node_width, double node_height) {
		super(element, node_width, node_height);
		init();
	}

	/**
	 * Set the height of the bar.
	 * 
	 * @param barHeight
	 *            The new height.
	 */
	public void setBarHeight(double barHeight) {
		this.height = barHeight;
//
//		// CP java?
		Rectangle rect = ((Rectangle) shape);
		rect.setHeight(height);
//		rect.setTranslateY(height / 2);
	}

	@Override
	public Shape createShape() {
		Rectangle rect = new Rectangle();
		rect.setWidth(width);
		rect.setHeight(height);
		rect.setStroke(Color.BLACK);
		rect.translateYProperty().bind(rect.heightProperty().divide(2));
		return rect;
	}

	private void init() {
		value.setTranslateY(-15);
	}
}
