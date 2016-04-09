package manager.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import manager.operations.OP_Init;
import manager.operations.OP_Write;
import wrapper.Locator;
import wrapper.Operation;

/**
 * A representation of the Array data structure, using doubles as values.
 * <br><br><b>IMPORTANT</b>: Can only handle single-dimension Arrays at the moments.
 * @author Richard
 *
 */
//TODO: Add access logging to enable presenting statistics.
public class Array extends DataStructure{

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
		size = (int[]) super.attributes.get("size");
		if (size == null){
			size = new int[]{-1};			
		}
	}
	
	/**
	 * Returns the list of elements held by this Array. Used when drawing.
	 * @return The list of elements held by this Array. 
	 */
	public List<Element> getElements(){
		return elements;
	}

	private void init(OP_Init op_init){
		if (!op_init.getTarget().getIdentifier().equals(super.identifier)){throw new IllegalArgumentException();}
		
		double[] linearArray = op_init.getValue();
		size = op_init.getSize();
		if (size == null){
			size = new int[]{linearArray.length}; //Assume one-dimensional if size is not specified.
		}
	
		//Initialize specified by the values argument of the init operation.
		int linearIndex = 0;
		

		for(; linearIndex < linearArray.length; linearIndex++){
			System.out.println(new ArrayElement(linearArray[linearIndex], getIndexInNDimensions(linearIndex, size)));
			elements.add(new ArrayElement(linearArray[linearIndex], getIndexInNDimensions(linearIndex, size)));
		}
		
		//Initialize elements without given values to 0.
		int linearTotal = 1;
		for(int i = 0; i < size.length; i++){
			linearTotal = linearTotal * size[i];
		}
		
		for(linearIndex++ ; linearIndex < linearTotal; linearIndex++){
			elements.add(new ArrayElement(0.0 , getIndexInNDimensions(linearIndex, size)));
		}
	}



	@Override
	public void reset() {
		elements.clear();
	}

	@Override
	public void applyOperation(Operation op) {
		switch(op.operation){
			case init:
				init((OP_Init) op);
				break;
			case message:
				break;
			case read:
				break;
			case write:
				break;
			case swap:
				break;
		}
	}

	/**
	 * Given a linear index, returns the index in N dimensions (dimeionSizes.length).
	 * @param linearIndex The linear index.
	 * @param dimensionSizes Sizes of the dimensions.
	 * @return The linear index translated to an index in N dimensions.
	 */
	private int[] getIndexInNDimensions(int linearIndex, final int[] dimensionSizes){
		
		int[] index = new int[dimensionSizes.length];

		/*
		 * http://stackoverflow.com/questions/14015556/how-to-map-the-indexes-of-a-matrix-to-a-1-dimensional-array-c
		 Matrix has size, n by m. That is i = [0, n-1] and j = [0, m-1].
		 matrix[i][j] = array[i*m + j].
		 
		 For higher dimension, this idea generalizes, i.e. for a 3D matrix L by N by M:
		 matrix[i][j][k] = array[i*(N*M) + j*M + k]
		 
		 */
		
		for (int currDim = 0; currDim < dimensionSizes.length ; currDim++){
			index[currDim] = linearIndex;
			
			//Subtract others
			for(int otherDim = 0; otherDim < dimensionSizes.length; otherDim++){
				if (otherDim == currDim){
					continue; //Don't subtract self.
				}
				index[currDim] = index[currDim] - index[otherDim]*higherDimSizesProduct(otherDim);
			}
			index[currDim] = index[currDim]/higherDimSizesProduct(currDim);
		}
		
		
		
		return index;
	}
	
	/**
	 * Calculate the product of all lower (to the right) dimension sizes. That is, for
	 * dim = 0 in array[i][j][k], dim refers to the dimension indexed by i
	 * and the method returns size[1]*size[2].
	 * 
	 * @param dim The current dimension. Must be non-negative.
	 * @return The product of all lower dimension sizes
	 */
	private int higherDimSizesProduct(int dim){
		
		int product = 1;
		
		for(int i = dim+1; i < size.length; i++){
			product = product * size[i];
		}
		
		return product;
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
		
		public String toString(){
			return Arrays.toString(index) + " = " + value;
		}
	}
}