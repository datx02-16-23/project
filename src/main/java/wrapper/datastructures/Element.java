package wrapper.datastructures;

/**
 * An element in a data structure.
 * 
 * @author Richard
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
     */
    public abstract void setValue (double newValue);
    
    public abstract String getColor();
}
