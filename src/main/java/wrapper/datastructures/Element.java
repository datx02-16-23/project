package wrapper.datastructures;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * An element in a data structure.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class Element {

	private final SimpleStringProperty valueStringProperty = new SimpleStringProperty();
	private final SimpleDoubleProperty valueDoubleProperty = new SimpleDoubleProperty();
	private final SimpleObjectProperty<Paint> fillProperty = new SimpleObjectProperty<Paint>();
	private double numericValue = Double.NaN;
	private String value;
	private Color color;

	public SimpleStringProperty valueProperty() {
		return valueStringProperty;
	}

	public SimpleObjectProperty<Paint> fillProperty() {
		return fillProperty;
	}

	/**
	 * Returns the numeric value held by this Element.
	 * 
	 * @return The value held by this Element.
	 */
	public final double getNumericValue() {
		return numericValue;
	}

	/**
	 * Set the numeric value held by this Element.
	 * 
	 * @param newValue
	 *            the new value for this Element.
	 */
	public final void setNumValue(double newValue) {
		if (numericValue != newValue) {
			numericValue = newValue;
			valueStringProperty.setValue(numericValue + "");
		}
	}

	/**
	 * Get the display value held by this Element. Returns the numeric value if
	 * not found.
	 * 
	 * @return The display value held by this Element
	 */
	public String getValue() {
		if (value != null) {
			return value;
		} else {
			return (String) value;
		}
	}

	/**
	 * Set the display value held by this Element.
	 * 
	 */
	public void setValue(String newValue) {
		if (value.equals(newValue) == false) {
			value = newValue;
			valueStringProperty.setValue(value);
		}
	}

	/**
	 * Returns the colour with which to draw this Element.
	 * 
	 * @return The colour with which to draw this Element.
	 */
	public final Color getColor() {
		return color;
	}

	/**
	 * Set The colour with which to draw this Element.
	 * 
	 * @param newColor
	 *            The colour with which to draw this Element.
	 */
	public final void setColor(Color newColor) {
		this.color = newColor;
		fillProperty.setValue(color);
	}

	@Override
	public int hashCode() {
		return (int) (numericValue * 17) + (value == null ? 0 : value.hashCode());
	}

}
