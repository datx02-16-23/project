package manager.datastructures;

import java.util.List;

import manager.datastructures.Element;

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
}
