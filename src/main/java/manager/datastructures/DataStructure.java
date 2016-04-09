package manager.datastructures;

import java.util.List;

import manager.operations.OP_Init;
import wrapper.AnnotatedVariable;
import wrapper.Operation;

/**
 * A data structure for use in visualisation.
 * @author Richard
 *
 */
public abstract class DataStructure extends AnnotatedVariable{

	public DataStructure(String identifier, String rawType, String abstractType, String visual) {
		super(identifier, rawType, abstractType, visual);
	}

	/**
	 * Returns the list of elements held by this DataStructure. Used when drawing the elements.
	 * @return The list of elements held by this DataStructure. 
	 */
	public abstract List<Element> getElements();
	
	/**
	 * Returns the number of elements held by this DataStructure.
	 * @return The number of elements held by this DataStructure.
	 */
	public abstract int size();

	/**
	 * Reset the structure to the state before any applied operations
	 */
	public abstract void reset();

	/**
	 * Apply an operation to the structure
	 * @param op to be applied
     */
	public abstract void applyOperation(Operation op);
	
	public String toString(){
		return super.identifier + ": " + super.rawType;
	}
}