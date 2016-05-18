package application.visualization.render_FX.elements;

import java.io.IOException;
import java.util.Arrays;
import application.gui.Main;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import wrapper.datastructures.Element;
import wrapper.operations.OperationCounter;

/**
 * A visualisation element. Elements which use the
 * {@code VisualElement(Element element, ..)} constructor are bound to their
 * elements. Using the {@code VisualElement(double value, Paint style, ..)}
 * allows the user to set values manually. Attempting to set values manually on
 * a bound element will cast an exception.
 * 
 * @author Richard Sundqvist
 *
 */
public class VisualElement extends Pane {

	private static final String url = "/visualization/FXMLElement.fxml";

	/**
	 * The element this VisualElement represents.
	 */
	protected final Element element;
	/*
	 * FXML elements.
	 */
	protected Shape shape;
	protected Label value, info;
	protected Tooltip tooltip;
	private GridPane root;

	/**
	 * Width and height of the nodes.
	 */
	protected double node_width, node_height;

	/**
	 * Points used by Polygons.
	 */
	private final double[] points;

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
	public VisualElement(double value, Paint style, double node_width, double node_height) {
		this.element = null;
		points = null;
		init(node_width, node_height);

		this.value.setText("" + value);
		this.shape.setFill(style);
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
	public VisualElement(Element element, double node_width, double node_height) {
		this.element = element;
		points = null;
		init(node_width, node_height);

		// Automatic updating of value
		value.textProperty().bind(element.valueProperty());
		shape.fillProperty().bind(element.fillProperty());
	}

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
	 *            The height of the node.
	 */
	public VisualElement(double value, Paint style, double node_width, double node_height, double[] points) {
		this.element = null;
		this.points = points;
		init(node_width, node_height);

		this.value.setText("" + value);
		this.shape.setFill(style);
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
	 */
	public VisualElement(Element element, double node_width, double node_height, double[] points) {
		this.element = element;
		this.points = points;
		init(node_width, node_height);

		// Automatic updating of value
		value.textProperty().bind(element.valueProperty());
		shape.fillProperty().bind(element.fillProperty());
	}

	/**
	 * Create a shape to use as the holder of the element value. The default
	 * implementation will create a trapezoid using the {@code #createTrapezoid}
	 * method.
	 * 
	 * @return A Shape.
	 */
	public Shape createShape() {
		System.out.println("default");
		Polygon p = new Polygon(points != null ? points : createTrapezoid());
		p.setStroke(Color.BLACK);
		return p;
	}

	/**
	 * 
	 * Returns the points for a trapezoid.
	 * 
	 * @return A double[] array of trapezoid points.
	 */
	//@formatter:off
	public double[] createTrapezoid() {
		double[] points = { //Points x, y
							node_width * 0.75, 0,		//
							node_width * 0.25, 0,		//
							0, node_height,				//
							node_width, node_height,	//
						  };

		return points;
	}
	//@formatter:on

	private void init(double node_width, double node_height) {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(url));
		fxmlLoader.setController(this);
		// fxmlLoader.setController(this);

		try {
			root = (GridPane) fxmlLoader.load();
			for(ColumnConstraints cr : root.getColumnConstraints()){
				cr.setPrefWidth(0);
			}
			for(RowConstraints cr : root.getRowConstraints()){
				cr.setPrefHeight(0);
			}
			root.setLayoutX(0);
			root.setLayoutY(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		this.node_height = node_height;
		this.node_width = node_width;

		Pane shapePane = (Pane) fxmlLoader.getNamespace().get("shape");
		shapePane.setCursor(Cursor.HAND);
		shape = createShape();
		shapePane.setPickOnBounds(true);
		shape.setPickOnBounds(true);
		shapePane.getChildren().add(shape);

		value = (Label) fxmlLoader.getNamespace().get("value");
		info = (Label) fxmlLoader.getNamespace().get("index");
		info = new Label(); //TODO
		tooltip = (Tooltip) fxmlLoader.getNamespace().get("tooltip");

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
			this.setMouseTransparent(true);
			shape.fillProperty().unbind();
			shape.setFill(Color.TRANSPARENT);
			shape.getStrokeDashArray().addAll(5.0);
			value.setVisible(false);
		} else {
			this.setMouseTransparent(false);
			shape.fillProperty().bind(element.fillProperty());
			shape.getStrokeDashArray().clear();
			value.setVisible(true);
		}
	}

	/**
	 * Unbind the element, leaving it in whatever state is is currently in.
	 */
	public void unbind() {
		value.textProperty().unbind();
		shape.fillProperty().unbind();
	}

	// TODO
	public void setIndex(int[] index) {
		this.info.setText(Arrays.toString(index));
	}

	/**
	 * Set the position of the extra info label. Will default to BOTTOM_CENTER.
	 * 
	 * @param position
	 *            The new position for the info label.
	 */
	public void setLabelPos(Pos position) {
		// Default BOTTOM_CENTER.
		int row = 2;
		int col = 1;

		switch (position) {
		case TOP_LEFT:
		case TOP_CENTER:
		case TOP_RIGHT:
			row = 0;
			break;
		case CENTER_LEFT:
		case CENTER_RIGHT:
			row = 1;
			break;
		case BOTTOM_CENTER:
		case BOTTOM_LEFT:
		case BOTTOM_RIGHT:
			row = 2;
			break;
		default:
			System.out.println("Unsupported position: " + position);
			break;
		}

		switch (position) {
		case TOP_LEFT:
		case CENTER_LEFT:
		case BOTTOM_LEFT:
			col = 0;
			break;
		case TOP_CENTER:
		case BOTTOM_CENTER:
			col = 1;
			break;
		case TOP_RIGHT:
		case CENTER_RIGHT:
		case BOTTOM_RIGHT:
			col = 2;
			break;
		default:
			System.out.println("Unsupported position: " + position);
			break;
		}

		GridPane.setRowIndex(info, row);
		GridPane.setColumnIndex(info, col);
	}

	public VisualElement clone() {
		VisualElement clone = null;
		if (points != null) {
			if (element == null) {
				clone = new VisualElement(Double.parseDouble(value.getText()), shape.getFill(), node_width, node_height,
						points);
			} else {
				clone = new VisualElement(element, node_width, node_height, points);
			}
		} else {
			if (element == null) {
				clone = new VisualElement(Double.parseDouble(value.getText()), shape.getFill(), node_width,
						node_height);
			} else {
				clone = new VisualElement(element, node_width, node_height);
			}
		}

		return clone;
	}
}
