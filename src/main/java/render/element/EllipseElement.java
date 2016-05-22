package render.element;

import contract.datastructure.Element;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;

public class EllipseElement extends AVElement {

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
	public void createShape() {
		super.createShape();
		Ellipse ellipse = new Ellipse();
		ellipse.setRadiusX(width / 2);
		ellipse.setRadiusY(height / 2);
		ellipse.setStroke(Color.BLACK);
		this.shape = ellipse;
	}

	@Override
	public void adjustSize(double value) {
		// TODO Auto-generated method stub
		
	}

//	public EllipseElement clone() {
//		EllipseElement clone;
//		if (element == null) {
//			clone = new EllipseElement(Double.parseDouble(value.getText()), shape.getFill(), node_width, node_height);
//		} else {
//			clone = new EllipseElement(element, node_width, node_height);
//
//		}
//		return clone;
//	}
}
