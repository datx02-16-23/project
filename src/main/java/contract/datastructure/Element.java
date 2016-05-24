package contract.datastructure;

import contract.json.Operation;
import contract.operation.OperationType;
import contract.utility.OperationCounter;
import contract.utility.OperationCounter.OperationCounterHaver;
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
public abstract class Element implements OperationCounterHaver {

    /**
     * String property for this element. Will be updated when the string <b>or</b> double value is
     * changed.
     */
    public final SimpleStringProperty        stringProperty = new SimpleStringProperty();
    /**
     * Double property for this element. Updated whenever the value is changed.
     */
    public final SimpleDoubleProperty        numProperty    = new SimpleDoubleProperty();
    /**
     * Fill property for this element. Updated when the {@link #count(Operation)} method is called.
     */
    public final SimpleObjectProperty<Paint> fillProperty   = new SimpleObjectProperty<Paint>();
    /**
     * Counter for operations performed on the element.
     */
    protected final OperationCounter         oc             = new OperationCounter();

    /*
     * Value and fill
     */

    /**
     * The numeric value for this element before the last call to {@link #setValue(double)}. <br>
     * {@link Double#POSITIVE_INFINITY} indicates that the element is inactive. <br>
     * {@link Double#NEGATIVE_INFINITY} indicates that the value has never been set using
     * {@link #setValue(double)}.
     */
    private double                           prevNumValue   = Double.NEGATIVE_INFINITY;
    /**
     * Numeric value for this element.
     */
    private double                           numValue       = Double.NaN;
    /**
     * String value for this element. Useful when representing objects.
     */
    private String                           stringValue    = null;
    /**
     * The paint for this element.
     */
    private Paint                            paint          = null;

    /**
     * Constructs a new Element.
     */
    public Element () {
    };

    /*
     * Setters and Getters
     */

    /**
     * Returns the numeric value held by this Element.
     *
     * @return The value held by this Element.
     */
    public final double getNumValue () {
        return numValue;
    }

    /**
     * Get the display value held by this Element. Returns the numeric value if not found.
     *
     * @return The display value held by this Element
     */
    public String getStringValue () {
        return stringValue;
    }

    /**
     * Set the numeric value held by this Element.
     *
     * @param newValue
     *            the new value for this Element.
     */
    public final void setValue (double newValue) {
        prevNumValue = numValue;

        if (numValue != newValue || newValue == Double.NaN) {
            numValue = newValue;
            numProperty.set(newValue);

            if (newValue != Double.NaN) {
                stringProperty.setValue(" " + numValue + " ");
            } else {
                fillProperty.set(Color.BLACK);
            }
        }
    }

    /**
     * Set the display value held by this Element.
     *
     */
    public final void setValue (String newValue) {
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
    public final Paint getPaint () {
        return paint;
    }

    /**
     * Indicate to the element that it it has been involved in an operation.
     *
     * @param op
     *            The operation type which was applied.
     */
    public final void count (OperationType ot) {
        oc.count(ot);
        setColor(ot.color);
        if (ot == OperationType.remove) {
            this.setValue(Double.NaN);
        }
    }

    /**
     * Set the Paint for this element.
     *
     * @param c
     *            The paint to use
     */
    public final void setColor (Paint c) {
        paint = c;
        fillProperty.setValue(paint);
    }

    /**
     * Restores the previous value for this Element by calling {@link #setValue(double)}, and
     * returns the value to the caller. <br>
     * <br>
     * The previous value for this element before the last call to {@link #setValue(double)}, which
     * is generally invoked by the the model. <br>
     * <br>
     * {@link Double#POSITIVE_INFINITY} indicates that the element is inactive. <br>
     * {@link Double#NEGATIVE_INFINITY} indicates that the value has never been set using
     * {@link #setValue(double)}.
     *
     * @return The previous value.
     */
    public final double restoreValue () {
        this.setValue(prevNumValue);
        return prevNumValue;
    }

    /**
     * Returns the last numeric value of this element, without restoring it as done by
     * {@link #restoreValue()}.
     *
     * @return The previous value.
     */
    public final double getPrevNumValue () {
        return prevNumValue;
    }

    @Override public OperationCounter getCounter () {
        return oc;
    }
}
