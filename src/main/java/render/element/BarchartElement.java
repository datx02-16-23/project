package render.element;

import contract.datastructure.Element;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 * A resizable Rectangle element used by BarChart.
 *
 * @author Richard
 *
 */
public class BarchartElement extends RectangleElement {

    private Rectangle rect;

    private double    unitHeight;

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
    public BarchartElement (double value, Paint paint, double node_width, double node_height) {
        super(value, paint, node_width, node_height);
        valueLabel.setTranslateY(-15);
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
    public BarchartElement (Element element, double node_width, double node_height) {
        super(element, node_width, node_height);
        valueLabel.setTranslateY(-15); // Raise slightly does it doesn't
                                       // cover
        // the x-axis.
    }

    /**
     * Set the height of the bar.
     *
     * @param unitHeight
     *            The new height.
     */
    public void updateSize (double unitHeight, double foo) {
        this.unitHeight = unitHeight;

        Rectangle rect = (Rectangle) shape;
        rect.heightProperty().bind(element.numProperty.multiply(unitHeight));
        // root.setPrefHeight(unitHeight * element.getNumValue()); //TODO
        // Uncomment, but fucks up positioning on the Y-axis.
    }

    @Override public void setSize (double width, double height) {
        // Not used by barcharts.
    }

    @Override public void createShape () {
        // super.createShape(); TODO Uncomment, but fucks up positioning on the
        // Y-axis.

        rect = new Rectangle();
        rect.setWidth(width);
        rect.setHeight(height);
        rect.setStroke(Color.BLACK);
        shape = rect;

        fixPositioning(0);
    }

    /**
     * Set the Y-coordinate of the bottom left of the bar.
     *
     * @param y
     *            The y coordinate at the bottom of the bar.
     */
    public void setBotY (double y) {
        layoutYProperty().unbind();
        fixPositioning(y);
    }

    private void fixPositioning (double y) {
        DoubleBinding neg_half_height = rect.heightProperty().divide(2).multiply(-1); // -
        // height/2
        layoutYProperty().bind(neg_half_height.add(y).subtract(render.assets.Const.DEFAULT_ELEMENT_HEIGHT / 2)); // TODO
        // fix
    }

    @Override public BarchartElement clone () {
        BarchartElement clone = (BarchartElement) AVElementFactory.clone(this);

        clone.updateSize(unitHeight, -1);

        return clone;
    }
}
