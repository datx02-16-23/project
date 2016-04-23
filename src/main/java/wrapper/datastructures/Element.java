package wrapper.datastructures;

import javafx.scene.paint.Color;

/**
 * An element in a data structure.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class Element {

    protected double numericValue;
    protected String value;
    protected Color  color;

    /**
     * Returns the numeric value held by this Element.
     * 
     * @return The value held by this Element.
     */
    public final double getNumericValue (){
        return numericValue;
    }

    /**
     * Set the numeric value held by this Element.
     * 
     * @param newValue the new value for this Element.
     */
    public final void setNumericValue (double newValue){
        this.numericValue = newValue;
    }

    /**
     * Get the display value held by this Element. Returns the numeric value if not found.
     * 
     * @return The display value held by this Element
     */
    public String getValue (){
        if (value != null) {
            return value;
        }
        else {
            return (String) value;
        }
    }

    /**
     * Set the display value held by this Element.
     * 
     */
    public void setValue (String newValue){
        value = newValue;
    }

    /**
     * Returns the colour with which to draw this Element.
     * 
     * @return The colour with which to draw this Element.
     */
    public final Color getColor (){
        return color;
    }

    /**
     * Set The colour with which to draw this Element.
     * 
     * @param newColor The colour with which to draw this Element.
     */
    public final void setColor (Color newColor){
        this.color = newColor;
    }
}
