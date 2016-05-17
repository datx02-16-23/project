package application.visualization.render_FX;

import java.io.IOException;
import java.util.Arrays;

import application.gui.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import wrapper.datastructures.Element;
import wrapper.operations.OperationCounter;

/**
 * A visualisation element. Elements which use the
 * {@code VisualElement(Element element, ..)} constructor are bound to their
 * elements. Using the {@code VisualElement(double value, Color style, ..)}
 * allows the user to set values manually. Attempting to set values manually on
 * a bound element will cast an exception.
 * 
 * @author Richard Sundqvist
 *
 */
public class VisualElement extends Pane {
	
	/**
	 * The URL of the FXML file. Used in cloning.
	 */
	private String fxmlURL;
	

	/**
	 * The element this VisualElement represents.
	 */
	protected final Element element;
	/*
	 * FXML elements.
	 */
	protected Shape shape;
	protected Label value, index;
	protected Tooltip tooltip;
	private GridPane root;

	/*
	 * Width and height of the nodes.
	 */
	protected double node_width, node_height;

	/**
	 * Create a static, unbound VisualElement.
	 * 
	 * @param value
	 *            The initial value.
	 * @param style
	 *            The style to use.
	 * @param node_width
	 *            The width of the node.
	 * @param node_height
	 *            The height of the node. * @param fxmlUrl Path to the FXML file
	 *            containing the layout for the element.
	 */
	public VisualElement(double value, Color style, double node_width, double node_height, String fxmlUrl) {
		this.element = null;
		
		init(node_width, node_height, fxmlUrl);

		this.shape.setFill(style);
		this.value.setText(value + "");
	}

	/**
	 * Create a bound VisualElement.
	 * 
	 * @param element
	 *            The Element this VisualElement represents
	 * @param node_width
	 *            The width of the node.
	 * @param node_height
	 *            The height of the node.
	 * @param fxmlUrl
	 *            Path to the FXML file containing the layout for the element.
	 */
	public VisualElement(Element element, double node_width, double node_height, String fxmlUrl) {
		this.element = element;

		init(node_width, node_height, fxmlUrl);

		// Automatic updating of value
		value.textProperty().bind(element.valueProperty());
		shape.fillProperty().bind(element.fillProperty());
	}

	private void init(double node_width, double node_height, String fxmlUrl) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlUrl));
		fxmlLoader.setController(this);
		this.fxmlURL = fxmlUrl;
		// fxmlLoader.setController(this);

		try {
			root = (GridPane) fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		shape = (Shape) fxmlLoader.getNamespace().get("shape");
		value = (Label) fxmlLoader.getNamespace().get("value");
		index = (Label) fxmlLoader.getNamespace().get("index");
		tooltip = (Tooltip) fxmlLoader.getNamespace().get("tooltip");

		this.node_height = node_height;
		this.node_width = node_width;

		getChildren().add(root);
	}

	/**
	 * Set the tooltip for this VisualElement.
	 * 
	 * @param tooltip
	 *            A tooltip String.
	 */
	public void setTooltip(String tooltip) {
		this.tooltip.setText(tooltip);
	}

	/**
	 * Returns the element this VisualElement represents.
	 * 
	 * @return The element this VisualElement represents.
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * Creates a Border.
	 * 
	 * @return A new Border.
	 */
	private static Border createMouseOverBorder() {
		return new Border(
				new BorderStroke(Color.web("#123456"), BorderStrokeStyle.SOLID, null, new BorderWidths(3), null));
	}

	/**
	 * Listener for the onMouseClicked event.
	 */
	public void onMouseClicked() {
		OperationCounter oc = element.getCounter();
		Main.console.info("Statistics for \"" + element + "\":");
		Main.console.info("\tReads: " + oc.getReads());
		Main.console.info("\tWrites: " + oc.getWrites());
		Main.console.info("\tSwaps: " + oc.getSwap());
		
	}

	/**
	 * Listener for the onMouseEntered event.
	 */
	public void onMouseEntered() {
		root.setScaleX(1.25);
		root.setScaleY(1.25);
		this.toFront();
	}

	/**
	 * Listener for the onMouseExited event.
	 */
	public void onMouseExited() {
		root.setScaleX(1);
		root.setScaleY(1);
	}

	/**
	 * Determines whether this element should be shown as a ghost, with no value
	 * and a dashed border.
	 * 
	 * @param ghost
	 *            The new value.
	 */
	public void setGhost(boolean ghost) {
		if (ghost != value.isVisible()) {
			return; // Ghost status not changed.
		}
		if (ghost) {
			shape.setMouseTransparent(true);
			shape.fillProperty().unbind();
			shape.setFill(Color.TRANSPARENT);
			shape.getStrokeDashArray().addAll(5.0);
			value.setVisible(false);
		} else {
			shape.setMouseTransparent(false);
			shape.fillProperty().bind(element.fillProperty());
			shape.getStrokeDashArray().clear();
			value.setVisible(true);
		}
	}
	
	public VisualElement clone(){
		VisualElement clone;
		
		if(element == null){ //Unbound
			clone = new VisualElement(Double.parseDouble(value.getText()), (Color) shape.getFill(), node_height, node_height, fxmlURL);
		} else { //Bound
			clone = new VisualElement(element, node_height, node_height, fxmlURL);
		}
		
		return clone;
	}
	
	/**
	 * Unbind the element, leaving it in whatever state is is currently in.
	 */
	public void unbind(){
		value.textProperty().unbind();
		shape.fillProperty().unbind();
	}
	
	//TODO
	public void setIndex(int[] index){
		this.index.setText(Arrays.toString(index));
	}
}
