package draw.element;

import java.io.IOException;
import java.util.Arrays;

import contract.datastructure.Element;
import contract.operation.OperationCounter;
import gui.Main;
import javafx.animation.RotateTransition;
import javafx.animation.StrokeTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

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
public abstract class VisualElement extends Pane {

	private static final String url = "/render/FXMLElement.fxml";

	/**
	 * Enum indicating the shape of this polygon. Used by the factory.
	 */
	public ElemShape elemShape;

	/**
	 * Current info label position.
	 */
	private Pos infoPos;

	/**
	 * The element this VisualElement represents.
	 */
	protected final Element element;
	/**
	 * Extra info label for stuff such as index.
	 */
	protected final Label infoLabel = new Label();
	/*
	 * FXML elements.
	 */
	protected Shape shape;
	protected Label valueLabel;
	private GridPane root;

	/**
	 * Width of the node.
	 */
	public double width;
	/**
	 * Height of the node.
	 */
	public double height;

	/**
	 * Points used by Polygons.
	 */
	public final double[] points;

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

		this.valueLabel.setText("" + value);
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
		valueLabel.textProperty().bind(element.valueStringProperty());
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

		this.valueLabel.setText("" + value);
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
		valueLabel.textProperty().bind(element.valueStringProperty());
		shape.fillProperty().bind(element.fillProperty());
	}

	/**
	 * Create a shape to use as the holder of the element value. Must never
	 * return null.
	 */
	public abstract void createShape();

	private void init(double node_width, double node_height) {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(url));
		fxmlLoader.setController(this);

		try {
			root = (GridPane) fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		this.height = node_height;
		this.width = node_width;

		Pane shapePane = (Pane) fxmlLoader.getNamespace().get("shape");
		shapePane.setCursor(Cursor.HAND);
		createShape();
		shape.setPickOnBounds(true);
		shapePane.setPickOnBounds(true);
		shapePane.getChildren().add(shape);

		valueLabel = (Label) fxmlLoader.getNamespace().get("value");
		
		infoLabel.setMouseTransparent(true);
		infoLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");
		root.getChildren().add(infoLabel);
		getChildren().add(root);
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
	 * Returns the shape containing the element.
	 * 
	 * @return The shape containing the element.
	 */
	public Shape getElementShape() {
		return shape;
	}

	/**
	 * Listener for the onMouseClicked event.
	 */
	public void onMouseClicked() {
		showSelected();
		OperationCounter oc = element.getCounter();
		Main.console.info("Statistics for \"" + element + "\":");
		Main.console.info("\tReads: " + oc.getReads());
		Main.console.info("\tWrites: " + oc.getWrites());
		Main.console.info("\tSwaps: " + oc.getSwap());
	}

	/**
	 * Indicate to the user that the element has been clicked.
	 */
	private void showSelected() {
		// Rotate.
		shape.setRotate(0);
		shape.setStrokeWidth(5);
		int sign = Math.random() < 0.2 ? -1 : 1;
		RotateTransition rt = new RotateTransition(Duration.millis(900), shape);
		rt.setByAngle(360 * sign);

		// Change border, cannot see clicked on circular elements otherwise.
		StrokeTransition ft = new StrokeTransition(Duration.millis(900), shape, Color.BLACK, Color.SKYBLUE);
		ft.setOnFinished(event -> {
			shape.setStrokeWidth(1);
			shape.setStroke(Color.BLACK);
		});
		ft.setAutoReverse(true);

		ft.play();
		rt.play();
	}

	/**
	 * Listener for the onMouseEntered event.
	 */
	public void onMouseEntered() {
		root.setScaleX(1.20);
		root.setScaleY(1.20);
		this.toFront();
	}

	/**
	 * Listener for the onMouseExited event.
	 */
	public void onMouseExited() {
		root.setScaleX(1);
		root.setScaleY(1);
		shape.setStrokeWidth(1);
	}

	/**
	 * Determines whether this element should be shown as a ghost, with no value
	 * and a dashed border.
	 * 
	 * @param ghost
	 *            The new value.
	 */
	public void setGhost(boolean ghost) {
		if (ghost != valueLabel.isVisible()) {
			return; // Ghost status not changed.
		}
		if (ghost) {
			this.setMouseTransparent(ghost);
			shape.fillProperty().unbind();
			shape.setFill(Color.TRANSPARENT);
			shape.getStrokeDashArray().addAll(5.0);
			valueLabel.setVisible(false);
		} else {
			this.setMouseTransparent(ghost);
			shape.fillProperty().bind(element.fillProperty());
			shape.getStrokeDashArray().clear();
			valueLabel.setVisible(true);
		}
	}

	/**
	 * Unbind the element, leaving it in whatever state is is currently in.
	 */
	public void unbind() {
		valueLabel.textProperty().unbind();
		shape.fillProperty().unbind();
	}

	/**
	 * Show an array using the info label.
	 * 
	 * @param array
	 *            The array to show.
	 */
	public void setInfoArray(int[] array) {
		setInfoText(Arrays.toString(array));
	}

	/**
	 * Enables and disables visibility for the info label.
	 * 
	 * @param visible
	 *            The new visibility setting.
	 */
	public void setInfoVisible(boolean visible) {
		infoLabel.setVisible(visible);
	}

	/**
	 * Set text for the label. Will render at {@link Pos#BOTTOM_CENTER} if no
	 * position is specified.
	 * 
	 * @param text
	 *            The text to render.
	 */
	public void setInfoText(String text) {
		infoLabel.setText(text);
		setInfoVisible(true);
		setInfoPos(infoPos == null ? Pos.BOTTOM_CENTER : infoPos);
	}

	/**
	 * Set the position of the extra info label. Will hide the label if
	 * {@code pos == null}. <br>
	 * 
	 * @param pos
	 *            The new position for the info label.
	 */
	public void setInfoPos(Pos pos) {
		this.infoPos = pos;

		if (infoPos == null) {
			setInfoVisible(false);
			return;
		} else {
			setInfoVisible(true);
		}
		String text = infoLabel.getText();

		if (text.length() == 0) {
			return;
		}

		Label tmp = new Label(text);

		new Scene(new Group(tmp));
		tmp.applyCss();
		final double textW = tmp.getLayoutBounds().getWidth();
		final double textH = tmp.getLayoutBounds().getHeight() / 2;

		double tx = 0;
		double ty = 0;

		switch (pos.getHpos()) {
		case LEFT:
			tx = -(width / 2 + textW);
			break;
		case CENTER:
			// tx already 0.
			break;
		case RIGHT:
			tx = width / 2 + textW;
			break;
		}

		switch (pos.getVpos()) {
		case BOTTOM:
			ty = height / 2 + textH;
			break;
		case CENTER:
			// ty already 0.
			break;
		case TOP:
			ty = -(height / 2 + textH);
			break;
		}

		infoLabel.setTranslateX(tx);
		infoLabel.setTranslateY(ty);
	}

	public VisualElement clone() {
		return VisualElementFactory.clone(this);
	}
}