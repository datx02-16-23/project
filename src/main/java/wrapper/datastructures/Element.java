package wrapper.datastructures;

/**
 * An element in a data structure.
 * @author Richard
 *
 */
public interface Element {

	/**
	 * Returns the value held by this Element.
	 * @return The value held by this Element.
	 */
	public abstract double getValue();
	
	/**
	 * Set the value held by this Element.
	 */
	public abstract void setValue(double newValue);
}
