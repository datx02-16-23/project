package render.element;

import contract.datastructure.Element;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class RectangleElement extends AVElement {

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
    public RectangleElement (double value, Paint paint, double node_width, double node_height) {
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
    public RectangleElement (Element element, double node_width, double node_height) {
        super(element, node_width, node_height);
    }

    @Override
    public void createShape () {
        super.createShape();
        Rectangle rect = new Rectangle();
        rect.setStroke(Color.BLACK);
        rect.setWidth(this.width);
        rect.setHeight(this.height);
        this.shape = rect;
    }

    @Override
    public void setSize (double newWidth, double newHeight) {
        super.setSize(newWidth, newHeight);
        Rectangle rect = (Rectangle) this.shape;
        rect.setWidth(this.width);
        rect.setHeight(this.height);
    }
}
