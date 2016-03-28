package manager.datastructures;

import java.util.List;

import manager.datastructures.Element;
import manager.operations.OP_Init;

/**
 * A data structure for use in visualisation.
 * @author Richard
 *
 */
public interface DataStructure {

	/**
	 * Returns the list of elements held by this DataStructure. Used when drawing the elements.
	 * @return The list of elements held by this DataStructure. 
	 */
	public List<Element> getElements();
	
	/**
	 * Returns the number of elements held by this DataStructure.
	 * @return The number of elements held by this DataStructure.
	 */
	public int size();
	
	/**
	 * Apply an initialise to this DataStrucutre.
	 * @param op_init The init operation to process.
	 */
	public void init(OP_Init op_init);
}
