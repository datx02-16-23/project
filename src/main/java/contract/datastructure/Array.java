package contract.datastructure;

import java.util.Arrays;
import java.util.Map;

import assets.Const;
import contract.Locator;
import contract.datastructure.RawType.AbstractType;
import contract.operation.*;
import gui.Main;

/**
 * A representation of the Array data structure, using doubles as values.
 * 
 * @author Richard Sundqvist
 *
 */
public class Array extends DataStructure {

	/**
	 * Version number for this class.
	 */
	private static final long serialVersionUID = Const.VERSION_NUMBER;
	private transient int[] capacity;
	private transient double min = Integer.MAX_VALUE;
	private transient double max = Integer.MIN_VALUE;

	private transient BoundaryChangeListener listener;

	/**
	 * Construct a new Array with the given parameters.
	 * 
	 * @param identifier
	 *            The identifier for this Array.
	 * @param abstractType
	 *            The abstract type for this Array.
	 * @param visual
	 *            The preferred visual for this Array.
	 * @param attributes
	 *            The attributes for this Array.
	 */
	public Array(String identifier, RawType.AbstractType abstractType, VisualType visual,
			Map<String, Object> attributes) {
		super(identifier, RawType.array, abstractType, visual, attributes);
		capacity = getCapacity();
	}

	/**
	 * Construct a new Array with the given parameters. This constructor exists
	 * for use by the IndependentElement structure.
	 * 
	 * @param identifier
	 *            The identifier for this Array.
	 * @param rawType
	 *            The rawType for this Array.
	 * @param abstractType
	 *            The abstract type for this Array.
	 * @param visual
	 *            The preferred visual for this Array.
	 * @param attributes
	 *            The attributes for this Array.
	 */
	protected Array(String identifier, RawType rawType, AbstractType abstractType, VisualType visual,
			Map<String, Object> attributes) {
		super(identifier, rawType, abstractType, visual, attributes);
	}

	/**
	 * Set the listener for this Array.
	 * 
	 * @param listener
	 *            A BoundaryChangeListener.
	 */
	public void setListener(BoundaryChangeListener listener) {
		this.listener = listener;
	}

	/**
	 * Returns the declared capacity of this Array.
	 * 
	 * @return The declared capacity of this Array.
	 */
	public int[] getCapacity() {
		return DataStructureParser.parseSize(this);
	}

	private void init(OP_Write init) {

		repaintAll = true;

		elements.clear();
		double[] init_values = init.getValue();
		if (capacity == null) { // Fall back to size declared in header
			capacity = getCapacity();
		}
		if (capacity == null) { // Use size of values as a last resort.
			capacity = new int[] { init_values.length };
		}
		// Initialize specified by the values argument of the init operation.
		int linearIndex = 0;
		for (; linearIndex < init_values.length; linearIndex++) {
			IndexedElement ae = new IndexedElement(init_values[linearIndex],
					getIndexInNDimensions(linearIndex, capacity));
			ae.execute(init);
			putElement(ae);
		}
		
		// Initialise elements without given values to 0.
		int linearTotal = 1;
		for (int i = 0; i < capacity.length; i++) {
			linearTotal = linearTotal * capacity[i];
		}
		for (linearIndex++; linearIndex < linearTotal; linearIndex++) {
			IndexedElement ae = new IndexedElement(0.0, getIndexInNDimensions(linearIndex, capacity));
			modifiedElements.add(ae);
			ae.execute(init);
			putElement(ae);
		}
		modifiedElements.addAll(elements);
	}

	@Override
	public void clear() {
		elements.clear();
		clearElementLists();
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		oc.reset();
	}

	protected void executeSwap(OP_Swap op) {
		Locator var1 = op.getVar1();
		Locator var2 = op.getVar2();
		IndexedElement var1Element = this.getElement(var1);
		if (var1Element != null) {
			var1Element.setValue(op.getValue()[0]);
			var1Element.execute(op);
			modifiedElements.add(var1Element);
			inactiveElements.remove(var1Element);
			oc.count(op);
		}
		IndexedElement var2Element = this.getElement(var2);
		if (var2Element != null) {
			var2Element.setValue(op.getValue()[1]);
			var2Element.execute(op);
			modifiedElements.add(var2Element);
			inactiveElements.remove(var2Element);
			oc.count(op);
		}
	}

	protected void executeRW(OP_ReadWrite op) {
		double[] value = op.getValue();
		if (value == null || value.length < 1) {
			Main.console.err("Bad value in operation: " + op);
			return;
		}

		if (value.length > 1) {
			init((OP_Write) op);
			return;
		}

		oc.count(op); // Count the operation.

		Locator target = op.getTarget();
		Locator source = op.getSource();

		/*
		 * Write operation targeting this Array.
		 */
		if (target.identifier.equals(this.identifier)) {
			IndexedElement targetElement = getElement(target);

			if (targetElement != null) {
				// Element was found
				modifiedElements.add(targetElement);
				inactiveElements.remove(targetElement);
				
				targetElement.setValue(value[0]);
				targetElement.execute(op);
				
			} else {
				// Create the element
				IndexedElement newElement = new IndexedElement(value[0],
						target.index == null ? new int[] { this.elements.size() } : target.index);
				modifiedElements.add(newElement);
				putElement(newElement);
				
				newElement.execute(op);
			}
		} else //Should be called again if the source also targets this Array!!

		/*
		 * Read operation targeting this Array.
		 */
		if (source.identifier.equals(this.identifier)) {
			IndexedElement sourceElement = getElement(source);

			// Element was found
			if (sourceElement != null) {
				modifiedElements.add(sourceElement);
				inactiveElements.remove(sourceElement);
				
				sourceElement.setValue(value[0]);
				sourceElement.execute(op);
			} else {
				//Create the element
				IndexedElement newElement = new IndexedElement(value[0],
						source.index == null ? new int[] { this.elements.size() } : source.index);
				modifiedElements.add(newElement);
				putElement(newElement);
				
				newElement.execute(op);
			}
		}
	}

	/**
	 * Given a linear index, returns the index in N dimensions
	 * (dimeionSizes.length).
	 * 
	 * @param linearIndex
	 *            The linear index.
	 * @param dimensionSizes
	 *            Sizes of the dimensions.
	 * @return The linear index translated to an index in N dimensions.
	 */
	private int[] getIndexInNDimensions(int linearIndex, final int[] dimensionSizes) {
		int[] index = new int[dimensionSizes.length];
		/*
		 * http://stackoverflow.com/questions/14015556/how-to-map-the-indexes-of
		 * -a-matrix-to-a-1-dimensional-array-c Matrix has size, n by m. That is
		 * i = [0, n-1] and j = [0, m-1]. matrix[i][j] = array[i*m + j]. For
		 * higher dimension, this idea generalizes, i.e. for a 3D matrix L by N
		 * by M: matrix[i][j][k] = array[i*(N*M) + j*M + k]
		 */
		for (int currDim = 0; currDim < dimensionSizes.length; currDim++) {
			index[currDim] = linearIndex;
			// Subtract others
			for (int otherDim = 0; otherDim < dimensionSizes.length; otherDim++) {
				if (otherDim == currDim) {
					continue; // Don't subtract self.
				}
				index[currDim] = index[currDim] - index[otherDim] * higherDimSizesProduct(otherDim);
			}
			index[currDim] = index[currDim] / higherDimSizesProduct(currDim);
		}
		return index;
	}

	/**
	 * Calculate the product of all lower (to the right) dimension sizes. That
	 * is, for dim = 0 in array[i][j][k], dim refers to the dimension indexed by
	 * i and the method returns size[1]*size[2].
	 * 
	 * @param dim
	 *            The current dimension.
	 * @return The product of all lower dimension sizes
	 */
	private int higherDimSizesProduct(int dim) {
		int product = 1;
		for (int i = dim + 1; i < capacity.length; i++) {
			product = product * capacity[i];
		}
		return product;
	}

	/**
	 * Get the element at the specified by the given Locator.
	 * 
	 * @param locator
	 *            A Locator to specify the element to retrieve.
	 * @return The element at the location specified by the given locator, if it
	 *         was valid. Null otherwise.
	 */
	public IndexedElement getElement(Locator locator) {
		if (locator == null || locator.identifier.equals(identifier) == false) {
			return null;
		}
		return getElement(locator.index);
	}

	/**
	 * Get the element at the specified index.
	 * 
	 * @param index
	 *            The index from which to get an element.
	 * @return The element at the given index if the index was valid, null
	 *         otherwise.
	 */
	public IndexedElement getElement(int[] index) {
		for (Element e : elements) {
			IndexedElement ae = (IndexedElement) e;
			if (Arrays.equals(index, ae.index)) {
				return ae; // Found the element with the same index.
			}
		}
		return null; // Could not find an element with the same index.
	}

	/**
	 * Add a new element to this Array. If there was already an element at the
	 * index of the new element, the old element will be returned to the caller.
	 * 
	 * @param newElement
	 *            The element to insert.
	 * @return The element which was replaced, if applicable. Null otherwise.
	 */
	public IndexedElement putElement(IndexedElement newElement) {
		IndexedElement old = null;
		old = getElement(newElement.index);
		if (old != null) {
			if (newElement.numValue() == old.numValue()) {
				return null;
			}
			int replacedElementIndex = elements.indexOf(old);
			elements.remove(old);
			elements.add(replacedElementIndex, newElement);
		}
		if (newElement.numValue() < min) {
			min = newElement.numValue();
			if (listener != null) {
				listener.minChanged(min, Math.abs(min) + Math.abs(max));
			}
		}
		if (newElement.numValue() > max) {
			max = newElement.numValue();
			if (listener != null) {
				listener.maxChanged(max, Math.abs(min) + Math.abs(max));
			}
		}
		elements.add(newElement);
		return old;
	}

	/**
	 * Returns the value of the largest element held by this Array.
	 * 
	 * @return The value of the largest element held by this Array.
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Returns the value of the smallest element held by this Array.
	 * 
	 * @return The value of the smallest element held by this Array.
	 */
	public double getMin() {
		return min;
	}

	/**
	 * An indexed element belonging to an Array.
	 * 
	 * @author Richard Sundqvist
	 *
	 */
	public static class IndexedElement extends Element {

		private int[] index;

		/**
		 * Construct a new ArrayElement with the given value and index.
		 * 
		 * @param value
		 *            The value for this ArrayElement.
		 * @param index
		 *            The index for this ArrayElement.
		 */
		public IndexedElement(double value, int[] index) {
			setValue(value);
			setIndex(index);
		}

		private final int primes[] = { 607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691, 701,
				709, 719, 727, 733 };

		// TODO
		@Override
		public int hashCode() {
			if (index == null) {
				return -1;
			}

			int indexHash = 0;
			for (int i = 0; i < index.length; i++) {
				indexHash = index[i] * primes[i];
			}
			return indexHash;
		}

		/**
		 * Get the index of this ArrayElement.
		 * 
		 * @return The index of this ArrayElement.
		 */
		public int[] getIndex() {
			return index;
		}

		/**
		 * Set the index of this ArrayElement.
		 * 
		 * @param newIndex
		 *            The new index of this ArrayElement.
		 */
		public void setIndex(int[] newIndex) {
			this.index = newIndex;
		}

		/**
		 * Returns true if value and index are equal, false otherwise.
		 * 
		 * @param obj
		 *            The object to compare this ArrayVariable to.
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof IndexedElement == false) {
				return false;
			}
			IndexedElement rhs = (IndexedElement) obj;
			return this.numValue() == rhs.numValue() && Arrays.equals(this.index, rhs.index);
		}

		// TODO
		@Override
		public String toString() {
			// return hashCode() + "";
			return Arrays.toString(index) + " = " + numValue();
		}
	}

	/**
	 * Resolves the VisualType for this DataStructure. Will check {@code visual}
	 * , {@code abstractType}, and {@code rawType}, in that order. <br>
	 * <br>
	 * Single-dimension Arrays default to {@link VisualType#bar} , higher
	 * dimension Arrays default to {@link VisualType#box}. This method will
	 * always return a type.
	 * 
	 * @return The Visual to use for this Array.
	 */
	@Override
	public VisualType resolveVisual() {
		if (visual != null) {
			return visual;
		} else if (abstractType != null) {
			if (abstractType == AbstractType.tree) {
				visual = VisualType.tree;
			}
		} else {
			int[] capacity = this.getCapacity();
			if (capacity == null || capacity.length <= 1) {
				visual = VisualType.bar;
			} else {
				visual = VisualType.box;
			}
		}
		setVisual(visual);
		return visual;
	}

	/**
	 * Interface for listening to changes in min and max values.
	 * 
	 * @author Richard Sundqvist
	 *
	 */
	public interface BoundaryChangeListener {
		/**
		 * Called when the max value of the Array changes.
		 * 
		 * @param newMin
		 *            The new maximum.
		 * @param diff
		 *            The difference between min and max.
		 */
		public void maxChanged(double newMin, double diff);

		/**
		 * Called when the max value of the Array changes.
		 * 
		 * @param newMin
		 *            The new minimum.
		 * @param diff
		 *            The difference between min and max.
		 */
		public void minChanged(double newMin, double diff);
	}
}