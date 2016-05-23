package render.element;

import contract.datastructure.Element;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class SingleElement extends RectangleElement {

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
    public SingleElement (double value, Paint paint, double node_width, double node_height) {
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
    public SingleElement (Element element, double node_width, double node_height) {
        super(element, node_width, node_height);
    }

    @Override public void createShape () {
        super.createShape();
        Rectangle rect = (Rectangle) shape;
        rect.setArcWidth(width / 3);
        rect.setArcHeight(height / 3);
    }

}
