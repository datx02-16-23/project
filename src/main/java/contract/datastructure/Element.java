package contract.datastructure;

import contract.Operation;
import contract.operation.OperationCounter;
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
public abstract class Element {

	/* 
	 * Properties
	 */
	private final SimpleStringProperty valueStringProperty = new SimpleStringProperty();
	private final SimpleDoubleProperty valueDoubleProperty = new SimpleDoubleProperty();
	
	private final SimpleObjectProperty<Paint> fillProperty = new SimpleObjectProperty<Paint>();
	
	private final OperationCounter oc = new OperationCounter();
	
	/**
	 * Returns the operation counter.
	 * @return An OperationCounter.
	 */
	public OperationCounter getCounter() {
		return oc;
	}

	private double numericValue = Double.NaN;
	private String value;
	private Paint paint;

	public SimpleStringProperty valueStringProperty() {
		return valueStringProperty;
	}

	public SimpleObjectProperty<Paint> fillProperty() {
		return fillProperty;
	}
	
	public SimpleDoubleProperty numValueProperty() {
		return valueDoubleProperty;
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
			valueDoubleProperty.set(newValue);
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
	 * @param appliedOp The operation type which was applied.
	 */
	public final void count(Operation appliedOp) {
		this.paint = appliedOp.operation.paint;
		fillProperty.setValue(paint);
		oc.count(appliedOp);
	}
	
	/**
	 * Set the Paint for this element.
	 * @param c The paint to use
	 */
	public void setColor(Paint c){
		this.paint = c;
		fillProperty.setValue(paint);
	}
}
