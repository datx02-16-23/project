package application.visualization.render_NEW;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;
import wrapper.datastructures.Element;

/**
 * A circular visualisation element.
 * 
 * @author Richard
 *
 */
public class EllipseElement extends Pane {
	private static final Border BORDER_MOUSEOVER = createMouseOverBorder();

	/**
	 * The element this VisualElement represents.
	 */
	protected final Element element;
	/*
	 * FXML elements.
	 */
	protected Shape shape;
	protected Label value;
	protected Tooltip tooltip;
	private StackPane root;

	/**
	 * Create a static, unbound element.
	 * 
	 * @param value
	 *            The initial value.
	 * @param style
	 *            The style to use.
	 * @param x_dia
	 * @param y_dia
	 */
	public EllipseElement(double value, Color style, double x_dia, double y_dia) {
		this.element = null;
		
		init(x_dia, y_dia);
		
		this.shape.setFill(style);
		this.value.setText(value + "");
	}

	/**
	 * Create a dynamic, bound visual element.
	 * 
	 * @param element
	 *            The Element this VisualElement represents
	 * @param x_dia
	 *            The x diameter of the ellipse.
	 * @param y_dia
	 *            The y diameter of the ellipse.
	 */
	public EllipseElement(Element element, double x_dia, double y_dia) {
		this.element = element;
		
		init(x_dia, y_dia);

		// Automatic updating of value
		value.textProperty().bind(element.valueProperty());
		shape.fillProperty().bind(element.fillProperty());
	}
	

	private void init(double x_dia, double y_dia) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/visualization/EllipseElement.fxml"));
		// fxmlLoader.setController(this);

		try {
			root = (StackPane) fxmlLoader.load();
		} catch (IOException e) {
			System.exit(-1);
			// e.printStackTrace();
		}

		shape = (Ellipse) fxmlLoader.getNamespace().get("shape");
		value = (Label) fxmlLoader.getNamespace().get("value");
		tooltip = (Tooltip) fxmlLoader.getNamespace().get("tooltip");

		((Ellipse) shape).setRadiusX(x_dia);
		((Ellipse) shape).setRadiusY(y_dia);
		
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
	public void mouseClicked(MouseEvent me) {
		System.out.println(element);
		System.out.println(value);
	}

	/**
	 * Listener for the onMouseEntered event.
	 */
	public void mouseEntered() {
		System.out.println("over");
		root.setBorder(BORDER_MOUSEOVER);
	}

	/**
	 * Listener for the onMouseExited event.
	 */
	public void mouseExited() {
		root.setBorder(null);
	}

	/**
	 * Determines whether this element should be shown as a ghost, with no value
	 * and a dashed border.
	 * 
	 * @param ghost
	 *            The new value.
	 */
	public void setGhost(boolean ghost) {
		if (ghost) {
			shape.fillProperty().unbind();
			shape.setFill(Color.TRANSPARENT);
			shape.getStrokeDashArray().addAll(5.0);
			value.setVisible(false);
		} else {
			shape.fillProperty().bind(element.fillProperty());
			shape.getStrokeDashArray().clear();
			value.setVisible(true);
		}
	}

}
