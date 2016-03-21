package interpreter.wrapper;

import java.util.Arrays;

/**
 * A variable used by the {@code Operation} class.
 */
public class ArrayVariable {
	
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
	 * Create a new ArrayVariable with a given identifier and index.
	 * @param identifier The identifier for this variable.
	 * @param index The index from which the fetch a value in the variable identified by {@code identifier}.
	 */
	public ArrayVariable(String identifier, int[] index){
		this.identifier = identifier;
		this.index = index;
	}

	@Override
	public String toString() {
		return "{\"identifier\":"+identifier+", \"index\": "+Arrays.toString(index)+"}";
	}
	
	@Override
	public boolean equals(Object other){
		if (this == other){
			return true;
		}
		if(other instanceof ArrayVariable == false){
			return false;
		}
		
		ArrayVariable rhs = (ArrayVariable) other;
		
		return this.identifier.equals(rhs.identifier) && Arrays.equals(this.index, rhs.index);
	}
}
