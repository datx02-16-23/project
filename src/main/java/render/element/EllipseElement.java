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
    public EllipseElement (double value, Paint paint, double node_width, double node_height) {
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
    public EllipseElement (Element element, double node_width, double node_height) {
        super(element, node_width, node_height);
    }

    @Override public void createShape () {
        super.createShape();
        Ellipse ellipse = new Ellipse();
        // Width and height are the size of the bounding box.
        ellipse.setRadiusX(width / 2);
        ellipse.setRadiusY(height / 2);
        ellipse.setStroke(Color.BLACK);
        shape = ellipse;
    }

    @Override public void setSize (double newWidth, double newHeight) {
        super.setSize(newWidth, newHeight);
        Ellipse ellipse = (Ellipse) shape;
        ellipse.setRadiusX(newWidth / 2);
        ellipse.setRadiusY(newHeight / 2);
    }
}
