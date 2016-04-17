package wrapper.datastructures;

/**
 * An element in a data structure.
 * 
 * @author Richard Sundqvist
 *
 */
public interface Element {
    public static final String COLOR_WRITE = "red";
    public static final String COLOR_READ = "green";
    public static final String COLOR_SWAP = "cyan";

    /**
     * Returns the value held by this Element.
     * 
     * @return The value held by this Element.
     */
    public abstract double getValue ();

    /**
     * Set the value held by this Element.
     * @param newValue the new value for this Element.
     */
    public abstract void setValue (double newValue);
    
    /**
     * Returns the colour with which to draw this Element.
     * @return The colour with which to draw this Element.
     */
    public abstract String getColor();

    /**
     * Set The colour with which to draw this Element.
     * @param newColor The colour with which to draw this Element.
     */
    public abstract void setColor(String newColor);
}
