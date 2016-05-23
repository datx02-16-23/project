package contract.datastructure;

import java.util.Arrays;
import java.util.Map;

import assets.Const;
import contract.Locator;
import contract.datastructure.RawType.AbstractType;
import contract.operation.OP_ReadWrite;
import contract.operation.OP_Swap;
import contract.operation.OperationType;
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
    private transient int[]   capacity;
    private MinMaxListener    mmListener;

    // private transient BoundaryChangeListener listener;

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
    public Array (String identifier, RawType.AbstractType abstractType, VisualType visual,
            Map<String, Object> attributes) {
        super(identifier, RawType.array, abstractType, visual, attributes);
        this.capacity = this.getCapacity();
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
    protected Array (String identifier, RawType rawType, AbstractType abstractType, VisualType visual,
            Map<String, Object> attributes) {
        super(identifier, rawType, abstractType, visual, attributes);
    }

    /**
     * Set the listener for this Array.
     *
     * @param listener
     *            A BoundaryChangeListener.
     */
    public void setListener (MinMaxListener listener) {
        this.mmListener = listener;
    }

    /**
     * Returns the declared capacity of this Array.
     *
     * @return The declared capacity of this Array.
     */
    public int[] getCapacity () {
        return DataStructureParser.parseSize(this);
    }

    private void init (OP_ReadWrite rw) {

        this.repaintAll = true;
        this.elements.clear();

        double[] values = rw.getValue();

        if (this.capacity == null) { // Fall back to size declared in header
            this.capacity = this.getCapacity();
        }
        if (this.capacity == null) { // Use size of values as a last resort.
            this.capacity = new int[] { values.length };
        }

        // Initialize specified by the values argument of the init operation.
        int linearIndex = 0;
        for (; linearIndex < values.length; linearIndex++) {
            IndexedElement ae = new IndexedElement(values [linearIndex],
                    this.getIndexInNDimensions(linearIndex, this.capacity));
            ae.count(OperationType.write);
            this.putElement(ae);
        }

        // Initialise elements without given values to 0.
        int linearTotal = 1;
        for (int i = 0; i < this.capacity.length; i++) {
            linearTotal = linearTotal * this.capacity [i];
        }

        for (linearIndex++; linearIndex < linearTotal; linearIndex++) {
            IndexedElement ae = new IndexedElement(0.0, this.getIndexInNDimensions(linearIndex, this.capacity));
            this.modifiedElements.add(ae);
            ae.count(OperationType.write);
            this.putElement(ae);
        }
        this.modifiedElements.addAll(this.elements);

        // Dont spam the listener.
        double initMin = values [0];
        double initMax = values [0];
        for (int i = 1; i < values.length; i++) {
            if (values [i] < initMin) {
                initMin = values [i];
            }
            if (values [i] > initMin) {
                initMin = values [i];
            }
        }
        this.checkMinMaxChanged(initMin);
        this.checkMinMaxChanged(initMax);
    }

    @Override
    public void clear () {
        this.elements.clear();
        this.clearElementLists();
        this.resetMinMax();
        this.oc.reset();
        this.repaintAll = true;
    }

    @Override
    protected void executeSwap (OP_Swap op) {
        Locator var1 = op.getVar1();
        Locator var2 = op.getVar2();
        IndexedElement var1Element = this.getElement(var1);
        if (var1Element != null) {
            var1Element.setValue(op.getValue() [0]);
            var1Element.count(OperationType.swap);
            this.modifiedElements.add(var1Element);
            this.inactiveElements.remove(var1Element);
            this.oc.count(OperationType.swap);
            this.checkMinMaxChanged(op.getValue() [0]);
        }
        IndexedElement var2Element = this.getElement(var2);
        if (var2Element != null) {
            var2Element.setValue(op.getValue() [1]);
            var2Element.count(OperationType.swap);
            this.modifiedElements.add(var2Element);
            this.inactiveElements.remove(var2Element);
            this.oc.count(OperationType.swap);
            this.checkMinMaxChanged(op.getValue() [1]);
        }
    }

    @Override
    protected void executeRW (OP_ReadWrite op) {
        double[] value = op.getValue();
        if (value == null || value.length < 1) {
            Main.console.err("Bad value in operation: " + op);
            return;
        }

        if (value.length > 1) {
            this.init(op);
            return;
        }

        this.oc.count(op.operation); // Update structure count

        Locator target = op.getTarget();
        Locator source = op.getSource();

        /*
         * Write operation targeting this Array.
         */
        if (target != null && target.identifier.equals(this.identifier)) {
            IndexedElement targetElement = this.getElement(target);

            if (targetElement != null) {
                // Element was found
                this.modifiedElements.add(targetElement);
                this.inactiveElements.remove(targetElement);

                targetElement.setValue(value [0]);
                targetElement.count(OperationType.write);
                this.checkMinMaxChanged(op.getValue() [0]);
            } else {
                // Create the element
                IndexedElement newElement = new IndexedElement(value [0],
                        target.index == null ? new int[] { this.elements.size() } : target.index);
                this.modifiedElements.add(newElement);
                this.putElement(newElement);

                newElement.count(OperationType.write);
                this.repaintAll = true;
                this.checkMinMaxChanged(op.getValue() [0]);
            }
        }

        /*
         * Read operation targeting this Array.
         */
        if (source != null && source.identifier.equals(this.identifier)) {
            IndexedElement sourceElement = this.getElement(source);

            // Element was found
            if (sourceElement != null) {
                this.modifiedElements.add(sourceElement);
                this.inactiveElements.remove(sourceElement);

                sourceElement.setValue(value [0]);
                sourceElement.count(OperationType.read);
                this.checkMinMaxChanged(op.getValue() [0]);
            } else {
                // Create the element
                IndexedElement newElement = new IndexedElement(value [0],
                        source.index == null ? new int[] { this.elements.size() } : source.index);
                this.modifiedElements.add(newElement);
                this.putElement(newElement);

                newElement.count(OperationType.read);
                this.repaintAll = true;
                this.checkMinMaxChanged(op.getValue() [0]);
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
    public int[] getIndexInNDimensions (int linearIndex, final int[] dimensionSizes) {
        int[] index = new int[dimensionSizes.length];
        /*
         * http://stackoverflow.com/questions/14015556/how-to-map-the-indexes-of
         * -a-matrix-to-a-1-dimensional-array-c Matrix has size, n by m. That is
         * i = [0, n-1] and j = [0, m-1]. matrix[i][j] = array[i*m + j]. For
         * higher dimension, this idea generalizes, i.e. for a 3D matrix L by N
         * by M: matrix[i][j][k] = array[i*(N*M) + j*M + k]
         */
        for (int currDim = 0; currDim < dimensionSizes.length; currDim++) {
            index [currDim] = linearIndex;
            // Subtract others
            for (int otherDim = 0; otherDim < dimensionSizes.length; otherDim++) {
                if (otherDim == currDim) {
                    continue; // Don't subtract self.
                }
                index [currDim] = index [currDim] - index [otherDim] * this.higherDimSizesProduct(otherDim);
            }
            index [currDim] = index [currDim] / this.higherDimSizesProduct(currDim);
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
    private int higherDimSizesProduct (int dim) {
        int product = 1;
        for (int i = dim + 1; i < this.capacity.length; i++) {
            product = product * this.capacity [i];
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
    @Override
    public IndexedElement getElement (Locator locator) {
        if (locator == null || locator.identifier.equals(this.identifier) == false) {
            return null;
        }
        return this.getElement(locator.index);
    }

    /**
     * Get the element at the specified index.
     *
     * @param index
     *            The index from which to get an element.
     * @return The element at the given index if the index was valid, null
     *         otherwise.
     */
    public IndexedElement getElement (int[] index) {
        for (Element e : this.elements) {
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
    public IndexedElement putElement (IndexedElement newElement) {
        IndexedElement old = null;
        old = this.getElement(newElement.index);
        if (old != null) {
            if (newElement.getNumValue() == old.getNumValue()) {
                return null;
            }
            int replacedElementIndex = this.elements.indexOf(old);
            this.elements.remove(old);
            this.elements.add(replacedElementIndex, newElement);
        }

        this.elements.add(newElement);
        return old;
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
        public IndexedElement (double value, int[] index) {
            this.setValue(value);
            this.setIndex(index);
        }

        private final int primes[] = { 607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691, 701,
                709, 719, 727, 733 };

        // TODO
        @Override
        public int hashCode () {
            if (this.index == null) {
                return -1;
            }

            int indexHash = 0;
            for (int i = 0; i < this.index.length; i++) {
                indexHash = this.index [i] * this.primes [i];
            }
            return indexHash;
        }

        /**
         * Get the index of this ArrayElement.
         * 
         * @return The index of this ArrayElement.
         */
        public int[] getIndex () {
            return this.index;
        }

        /**
         * Set the index of this ArrayElement.
         * 
         * @param newIndex
         *            The new index of this ArrayElement.
         */
        public void setIndex (int[] newIndex) {
            this.index = newIndex;
        }

        /**
         * Returns true if value and index are equal, false otherwise.
         * 
         * @param obj
         *            The object to compare this ArrayVariable to.
         */
        @Override
        public boolean equals (Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof IndexedElement == false) {
                return false;
            }
            IndexedElement rhs = (IndexedElement) obj;
            return this.getNumValue() == rhs.getNumValue() && Arrays.equals(this.index, rhs.index);
        }

        // TODO
        @Override
        public String toString () {
            // return hashCode() + "";
            return Arrays.toString(this.index) + " = " + this.getNumValue();
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
     * @return The {@link #VisualType} to use for this Array.
     */
    @Override
    public VisualType resolveVisual () {
        if (this.visual != null) {
            return this.visual;

        } else if (this.abstractType != null) {
            if (this.abstractType == AbstractType.tree) {
                this.visual = VisualType.tree;
            }

        } else {
            int[] capacity = this.getCapacity();
            if (capacity == null || capacity.length <= 1) {
                this.visual = VisualType.bar;
            } else {
                this.visual = VisualType.box;
            }
        }
        this.setVisual(this.visual);
        return this.visual;
    }

    /**
     * Set min/max values and notify listener if there was a change.
     *
     * @param x
     *            The value to test.
     */
    private void checkMinMaxChanged (double x) {
        if (this.mmListener == null) {
            return;
        }

        if (x < this.min) {
            this.min = x;
            this.mmListener.minChanged(x);
        }

        if (x > this.max) {
            this.max = x;
            this.mmListener.maxChanged(x);
        }
    }

    private double min = Double.MAX_VALUE, max = Double.MIN_VALUE;

    /**
     * Restore default values for min and max.
     */
    private void resetMinMax () {
        this.min = Double.MAX_VALUE;
        this.max = Double.MIN_VALUE;
    }

    /**
     * The minimum value.
     * 
     * @return The minimum value.
     */
    public double getMin () {
        return this.min;
    }

    /**
     * The maximum value.
     * 
     * @return The maximum value.
     */
    public double getMax () {
        return this.max;
    }

    /**
     * Interface for listening to changes in min and max values.
     *
     * @author Richard Sundqvist
     *
     */
    public interface MinMaxListener {
        /**
         * Called when the min value changes.
         * 
         * @param newMax
         *            The new maximum.
         */
        public void maxChanged (double newMax);

        /**
         * Called when the max value changes.
         * 
         * @param newMin
         *            The new minimum.
         * @param diff
         */
        public void minChanged (double newMin);
    }
}
