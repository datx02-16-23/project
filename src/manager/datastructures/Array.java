package manager.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import manager.operations.OP_Init;
import wrapper.AnnotatedVariable;
import wrapper.Locator;

/**
 * A representation of the Array data structure, using doubles as values.
 * <br><br><b>IMPORTANT</b>: Can only handle single-dimension Arrays at the moments.
 * @author Richard
 *
 */
//TODO: Add handling of multi-dimensional arrays.
//TODO: Add access logging to enable presenting statistics.
public class Array extends AnnotatedVariable implements DataStructure{

	private transient final List<Element> elements;
	private transient int[] size;
	
	/**
	 * Construct a new Array with the given parameters.
	 * @param identifier The identifier for this Array.
	 * @param abstractType The abstract type for this Array.
	 * @param visual The preferred visual for this Array.
	 */
	public Array(String identifier, String abstractType, String visual) {
		super(identifier, "array", abstractType, visual);
		elements = new ArrayList<Element>();
		size = new int[]{-1};
	}
	
	/**
	 * Returns the list of elements held by this Array. Used when drawing.
	 * @return The list of elements held by this Array. 
	 */
	public List<Element> getElements(){
		return elements;
	}
	
	/**
	 * Remove all elements from this Array.
	 */
	public void clearElements(){
		elements.clear();
	}
	
	/**
	 * Apply an initialise to this Array.
	 * @param op_init The init operation to process.
	 */

	public void init(OP_Init op_init){
		if (!op_init.getTarget().equals(super.identifier)){throw new IllegalArgumentException();}
		
		int oldSize = this.size[0];
		this.size = op_init.getSize();
		
		if (op_init.getValue() == null){
			
			//Initialize any new values to zero, if the new size is greater than the old one.
			if (this.size[0] > oldSize){
				for(int i = oldSize+1; i < size[0]; i++){
					elements.add(new ArrayElement(0, new int[]{i}));
				}
			}
			return; //Return after changing the size of the array if no values were provided.
			
		}
		
//		double[] valuesDbl = OperationParser.stringToDoubleArray(op_init.getValue());
		double[] valuesDbl = op_init.getValue();
		//Initialize specified by the values argument of the init operation.
		for(int i = 0; i < valuesDbl.length; i++){
			elements.add(new ArrayElement(valuesDbl[i], new int[]{i}));
		}
		if (valuesDbl.length > size[0]){
			size[0] = valuesDbl.length; //Set the size to exactly the number of elements just initialized.
		}
		
		//Initialize any values not specified to zero.
		for(int i = valuesDbl.length+1; i < size[0]; i++){
			elements.add(new ArrayElement(0, new int[]{i}));
		}
	}
	
	/**
	 * Get the element at the specified by the given Locator.
	 * @param locator A Locator to specify the element to retrieve.
	 * @return The element at the location specified by the given locator, if it was valid. Null otherwise.
	 */
	public ArrayElement getElement(Locator locator){
		if(locator.identifier.equals(identifier) == false){throw new IllegalArgumentException();}
		
		return getElement(locator.index);
	}
	
	/**
	 * Get the element at the specified index.
	 * @param index The index from which to get an element.
	 * @return The element at the given index if the index was valid, null otherwise.
	 */
	public ArrayElement getElement(int[] index){
		
		for(Element e : elements){
			ArrayElement ae = (ArrayElement) e;
			if (Arrays.equals(index, ae.index)){
				return ae; //Found the element with the same index.
			}
		}
		return null; //Could not find an element with the same index.
	}
	
	/**
	 * Add a new element to this Array. If there was already an element at the index of the
	 * new element, this old value element will be lost.
	 * @param newElement The new element to insert.
	 * @return The element which was replaced, if applicable. Null otherwise.
	 */
	public ArrayElement putElement(ArrayElement newElement){
		ArrayElement old = null;
		
		old = getElement(newElement.index);
		if (old != null){
			int replacedElementIndex = elements.indexOf(old);
			elements.remove(old);
			elements.add(replacedElementIndex, newElement);
		}
		elements.add(newElement);
		
		return old;
	}
	
	@Override
	public int size(){
		return elements.size();
	}

	/*									   
	 		Internal class for holding elements
	 */
	
	/**
	 * An element in an Array. The elements do not keep track of which Array they belong to.
	 * @author Richard
	 *
	 */
	public class ArrayElement implements Element{
		private double value;
		private int[] index;
		
		/**
		 * Construct a new ArrayElement with the given value and index.
		 */
		public ArrayElement(double value, int[] index){
			this.value = value;
			this.index = index;
		}
		
		/**
		 * Get the value held by this ArrayElement.
		 * @return The value held by this ArrayElement.
		 */
		public double getValue() {
			return value;
		}

		/**
		 * Set the value of this ArrayElement.
		 * @param newValue The new value of this ArrayElement.
		 */
		public void setValue(double newValue) {
			this.value = newValue;
		}

		/**
		 * Get the index of this ArrayElement.
		 * @return The index of this ArrayElement.
		 */
		public int[] getIndex() {
			return index;
		}

		/**
		 * Set the index of this ArrayElement.
		 * @param index The new index of this ArrayElement.
		 */
		public void setIndex(int[] newIndex) {
			this.index = newIndex;
		}
		
		/**
		 * Returns true if value and index are equal, false otherwise.
		 * @param obj The object to compare this ArrayVariable to.
		 */
		@Override
		public boolean equals(Object obj){
			if (obj == this){
				return true;
			}
			if (obj instanceof ArrayElement == false){
				return false;
			}
			ArrayElement rhs = (ArrayElement) obj;
			
			return this.value == rhs.value && Arrays.equals(this.index, rhs.index);
		}
	}
}