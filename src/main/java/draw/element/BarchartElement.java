package draw.element;

import contract.datastructure.Element;
import javafx.beans.binding.DoubleBinding;
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
	
	private Rectangle rect;

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
		this.valueLabel.setTranslateY(-15);
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
		valueLabel.setTranslateY(-15);
		valueLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");
		//Restore to computed size.
		valueLabel.setMaxSize(-1, -1);
		valueLabel.setPrefSize(-1, -1);
		valueLabel.setMinSize(-1, -1);
	}

	/**
	 * Set the height of the bar.
	 * 
	 * @param unitHeight
	 *            The new height.
	 */
	public void updateUnitHeight(double unitHeight) {
		this.height = unitHeight;
		
		Rectangle rect = ((Rectangle) shape);
		rect.heightProperty().bind((element.numValueProperty().multiply(unitHeight)));
		System.out.println("unit height update");
	}

	@Override
	public Shape createShape() {
		rect = new Rectangle();
		rect.setWidth(width);
		rect.setHeight(height);
		rect.setStroke(Color.BLACK);
		
		botprop(0);
		return rect;
	}


	/**
	 * Set the Y-coordinate of the bottom left of the bar.
	 * 
	 * @param y
	 *            The y coordinate at the bottom of the bar.
	 */
	public void setBotY(double y) {
		layoutYProperty().unbind();
		botprop(y);
	}
	
	private void botprop(double y){
		DoubleBinding neg_half_height = rect.heightProperty().divide(2).multiply(-1); //- height/2
		this.layoutYProperty().bind(neg_half_height.add(y));
	}
}
