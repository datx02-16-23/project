package wrapper.datastructures;

import java.util.Arrays;
import java.util.Map;

import application.assets.Strings;
import application.gui.Main;
import application.visualization.VisualType;
import javafx.scene.paint.Color;
import wrapper.Locator;
import wrapper.Operation;
import wrapper.datastructures.RawType.AbstractType;
import wrapper.operations.*;

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
	private static final long serialVersionUID = Strings.VERSION_NUMBER;
	private transient int[] capacity;
	private transient double min = Integer.MAX_VALUE;
	private transient double max = Integer.MIN_VALUE;

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
		super(identifier, RawType.array, abstractType, visual, attributes, Color.WHITE);
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
		super(identifier, rawType, abstractType, visual, attributes, Color.WHITE);
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
		if (!init.getTarget().identifier.equals(super.identifier)) {
			return;
		}
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
			ArrayElement ae = new ArrayElement(init_values[linearIndex], getIndexInNDimensions(linearIndex, capacity));
			ae.color = OperationType.write.color;
			putElement(ae);
		}
		// Initialize elements without given values to 0.
		int linearTotal = 1;
		for (int i = 0; i < capacity.length; i++) {
			linearTotal = linearTotal * capacity[i];
		}
		for (linearIndex++; linearIndex < linearTotal; linearIndex++) {
			ArrayElement ae = new ArrayElement(0.0, getIndexInNDimensions(linearIndex, capacity));
			modifiedElements.add(ae);
			ae.color = OperationType.write.color;
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
	}

	@Override
	public void applyOperation(Operation op) {
		switch (op.operation) {
		case read:
		case write:
			readORwrite((OP_ReadWrite) op);
			break;
		case swap:
			swap((OP_Swap) op);
			break;
		case remove:
			remove((OP_Remove) op);
			return;
		default:
			Main.console.err("OperationType \"" + op.operation + "\" not applicable to " + getClass().getSimpleName());
			break;
		}
		inactive = false;
	}

	private void swap(OP_Swap op) {
		Locator var1 = op.getVar1();
		Locator var2 = op.getVar2();
		ArrayElement var1Element = this.getElement(var1);
		if (var1Element != null) {
			var1Element.numericValue = op.getValue()[0];
			var1Element.color = OperationType.swap.color;
			modifiedElements.add(var1Element);
			inactiveElements.remove(var1Element);
		}
		ArrayElement var2Element = this.getElement(var2);
		if (var2Element != null) {
			var2Element.numericValue = op.getValue()[1];
			var2Element.color = OperationType.swap.color;
			modifiedElements.add(var2Element);
			inactiveElements.remove(var2Element);
		}
		if (var1Element != null || var2Element != null) {
			numSwaps.set(numSwaps.get() + 1);
		}
	}

	private void readORwrite(OP_ReadWrite op) {
		if (op.operation == OperationType.write && (op.getValue().length > 1)) {
			init((OP_Write) op);
			return;
		}
		// Manage write
		ArrayElement targetElement = this.getElement(op.getTarget());
		ArrayElement sourceElement = this.getElement(op.getSource());
		double[] value = op.getValue();
		if (targetElement != null) {
			if (value != null) {
				targetElement.numericValue = op.getValue()[0];
				targetElement.color = OperationType.write.color;
				modifiedElements.add(targetElement);
				inactiveElements.remove(targetElement);
			} else {
				Main.console.err("WARNING: Null value in: " + op);
			}
		}
		// Manage read
		else if (sourceElement != null) {
			sourceElement.color = OperationType.read.color;
			modifiedElements.add(sourceElement);
			inactiveElements.remove(sourceElement);
		} else if (op.getSource() != null && op.getSource().identifier.equals(super.identifier)) {
			ArrayElement ae = new ArrayElement(0,
					op.getSource().index != null ? op.getSource().index : new int[] { elements.size() });
			ae.color = OperationType.read.color;
			putElement(ae);
		} else if (op.getTarget() != null && op.getTarget().identifier.equals(super.identifier)) {
			ArrayElement ae = new ArrayElement(0,
					op.getTarget().index != null ? op.getTarget().index : new int[] { elements.size() });
			ae.color = OperationType.write.color;
			putElement(ae);
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
	public ArrayElement getElement(Locator locator) {
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
	public ArrayElement getElement(int[] index) {
		for (Element e : elements) {
			ArrayElement ae = (ArrayElement) e;
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
	public ArrayElement putElement(ArrayElement newElement) {
		ArrayElement old = null;
		old = getElement(newElement.index);
		if (old != null) {
			if (newElement.numericValue == old.numericValue) {
				return null;
			}
			int replacedElementIndex = elements.indexOf(old);
			elements.remove(old);
			elements.add(replacedElementIndex, newElement);
		}
		if (newElement.numericValue < min) {
			min = newElement.numericValue;
		}
		if (newElement.numericValue > max) {
			max = newElement.numericValue;
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

	/*
	 * Internal class for holding elements
	 */
	/**
	 * An element in an Array. The elements do not keep track of which Array
	 * they belong to.
	 * 
	 * @author Richard Sundqvist
	 *
	 */
	public static class ArrayElement extends Element {

		private int[] index;

		/**
		 * Construct a new ArrayElement with the given value and index.
		 * 
		 * @param value
		 *            The value for this ArrayElement.
		 * @param index
		 *            The index for this ArrayElement.
		 */
		public ArrayElement(double value, int[] index) {
			this.numericValue = value;
			this.index = index;
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
			if (obj instanceof ArrayElement == false) {
				return false;
			}
			ArrayElement rhs = (ArrayElement) obj;
			return this.numericValue == rhs.numericValue && Arrays.equals(this.index, rhs.index);
		}

		@Override
		public String toString() {
			return Arrays.toString(index) + " = " + numericValue;
		}
	}

	@Override
	public VisualType resolveVisual() {
		if (visual != null) {
			return visual;
		} else if (abstractType != null) {
			if (abstractType == AbstractType.tree) {
				return VisualType.tree;
			} else {
				throw new IllegalArgumentException("");
			}
		} else {
			return VisualType.box;
		}
	}
}