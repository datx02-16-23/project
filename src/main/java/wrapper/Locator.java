package wrapper;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A variable used by the {@code Operation} class.
 */
public class Locator implements Serializable {
	
	/**
	 * The identifier for this variable. Will generally match one of the identifiers used as
	 * keys in the {@code annotatedVariables} HashMap of the variables declared in the header.
	 */
	public final String identifier;

	/**
	 * The index from which the fetch a value in the variable identified by {@code identifier}.
	 */
	public final int[] index;
	
	/**
	 * Create a new Locator with a given identifier and index.
	 * @param identifier The identifier for this variable.
	 * @param index The index from which the fetch a value in the variable identified by {@code identifier}.
	 */
	public Locator(String identifier, int[] index){
		this.identifier = identifier;
		this.index = index;
	}

	@Override
	public String toString() {
		return "{\"identifier\":"+identifier+", \"index\": "+Arrays.toString(index)+"}";
	}
	
	public String toSimpleString(){
		return identifier + (index == null ? "" : Arrays.toString(index));
	}
	
	@Override
	public boolean equals(Object other){
		if (this == other){
			return true;
		}
		if(other instanceof Locator == false){
			return false;
		}
		
		Locator rhs = (Locator) other;
		
		return this.identifier.equals(rhs.identifier) && Arrays.equals(this.index, rhs.index);
	}
	
	/**
	 * Compares the index only, ignoring identifier.
	 * @param other The other Locator to compare.
	 * @return True if the index of this and other are equal, false otherwise.
	 */
	public boolean indexEquals(Locator other){
		return Arrays.equals(this.index, other.index);
	}
	
	/**
	 * Compares the index only, ignoring identifier.
	 * @param other The index to compare.
	 * @return True if the index of this and other are equal, false otherwise.
	 */
	public boolean indexEquals(int[] other){
		return Arrays.equals(this.index, other);
	}
	
	/**
	 * Returns the identifier for this Locator.
	 * @return The identifier for this Locator.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the index for this Locator.
	 * @return The index for this Locator.
	 */
	public int[] getIndex() {
		return index;
	}

}
