package contract.datastructure;

import contract.Operation;
import contract.operation.OperationCounter;
import contract.operation.OperationCounter.OperationCounterHaver;
import contract.operation.OperationType;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Paint;

/**
 * An element in a data structure.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class Element implements OperationCounterHaver {

	/**
	 * String property for this element. Will be updated when the string
	 * <b>or</b> double value is changed.
	 */
	public final SimpleStringProperty stringProperty = new SimpleStringProperty();
	/**
	 * Double property for this element. Updated whenever the value is changed.
	 */
	public final SimpleDoubleProperty numProperty = new SimpleDoubleProperty();
	/**
	 * Fill property for this element. Updated when the
	 * {@link #execute(Operation)} method is called.
	 */
	public final SimpleObjectProperty<Paint> fillProperty = new SimpleObjectProperty<Paint>();

	public final OperationCounter oc = new OperationCounter();

	/*
	 * Value and fill
	 */

	/**
	 * The previous numeric value for this element. {@link Double#NaN} indicates
	 * that the element is inactive.
	 */
	private double previousNumValue = Double.NaN;
	/**
	 * Numeric value for this element.
	 */
	private double numValue = Double.NaN;
	/**
	 * String value for this element. Useful when representing objects.
	 */
	private String stringValue = null;
	/**
	 * The paint for this element.
	 */
	private Paint paint = null;

	/**
	 * Constructs a new Element.
	 */
	public Element() {
	};

	/*
	 * Setters and Getters
	 */

	/**
	 * Returns the numeric value held by this Element.
	 * 
	 * @return The value held by this Element.
	 */
	public final double numValue() {
		return numValue;
	}

	/**
	 * Get the display value held by this Element. Returns the numeric value if
	 * not found.
	 * 
	 * @return The display value held by this Element
	 */
	public String stringValue() {
		return stringValue;
	}

	/**
	 * Set the numeric value held by this Element.
	 * 
	 * @param newValue
	 *            the new value for this Element.
	 */
	public void setValue(double newValue) {
		if (numValue != newValue || newValue == Double.NaN) {
			numValue = newValue;
			numProperty.set(newValue);

			if (newValue != Double.NaN) {
				stringProperty.setValue(numValue + "");
			}
		}
	}

	/**
	 * Set the display value held by this Element.
	 * 
	 */
	public void setValue(String newValue) {
		if (stringValue.equals(newValue) == false) {
			stringValue = newValue;
			stringProperty.setValue(stringValue);
		}
	}

	/**
	 * Returns the paint with which to draw this Element.
	 * 
	 * @return The paint with which to draw this Element.
	 */
	public final Paint getPaint() {
		return paint;
	}

	/**
	 * Indicate to the element that it it has been involved in an operation.
	 * 
	 * @param op
	 *            The operation type which was applied.
	 */
	public final void execute(Operation op) {
		this.paint = op.operation.paint;
		fillProperty.setValue(paint);
		oc.count(op);
		if (op.operation == OperationType.remove) {
			setValue(Double.NaN);
		}
	}

	/**
	 * Set the Paint for this element.
	 * 
	 * @param c
	 *            The paint to use
	 */
	public void setColor(Paint c) {
		this.paint = c;
		fillProperty.setValue(paint);
	}

	public void restoreValue() {
		this.setValue(previousNumValue);
	}

	@Override
	public OperationCounter getCounter() {
		return oc;
	}
}
