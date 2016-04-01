package manager.datastructures;

import java.util.List;

import manager.datastructures.Element;
import manager.operations.OP_Init;
import wrapper.AnnotatedVariable;

/**
 * A data structure for use in visualisation.
 * @author Richard
 *
 */
public abstract class DataStructure extends AnnotatedVariable{

	public DataStructure(String identifier, String rawType, String abstractType, String visual) {
		super(identifier, rawType, abstractType, visual);
		// TODO Auto-generated constructor stub
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
	 * Apply an initialise to this DataStrucutre.
	 * @param op_init The init operation to process.
	 */
	public abstract void init(OP_Init op_init);
}
