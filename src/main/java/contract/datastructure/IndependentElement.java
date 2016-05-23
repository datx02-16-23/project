package contract.datastructure;

import java.util.Map;

import assets.Const;
import assets.Tools;
import contract.Locator;
import contract.Operation;
import contract.operation.OP_ReadWrite;
import contract.operation.OP_Swap;
import contract.operation.OperationType;

/**
 * An independent variable holding a single element. May for example be used as
 * a temporary variable when performing a swap.
 *
 * @author Richard Sundqvist
 *
 */
public class IndependentElement extends Array {

    /**
     * Version number for this class.
     */
    private static final long serialVersionUID = Const.VERSION_NUMBER;

    /**
     * Create a new IndependentElement.
     * 
     * @param identifier
     *            The identifier for this IndependentElement.
     * @param abstractType
     *            The <b>raw</b> type of the element held by this
     *            IndependentElement.
     * @param visual
     *            The preferred visual style of the IndependentElement.
     */
    public IndependentElement (String identifier, RawType.AbstractType abstractType, VisualType visual,
            Map<String, Object> attributes) {
        super(identifier, RawType.independentElement, abstractType, visual, attributes);
    }

    /**
     * Set the element held by this IndependentElement.
     * 
     * @param newElement
     *            The new element to be held by this IndependentElement.
     */
    public void setElement (Element newElement) {
        this.elements.clear();
        this.elements.add(newElement);
    }

    /**
     * Initialize an element with value 0.
     * 
     * @param value
     *            The value to initialize with.
     */
    public void initElement (double value) {
        Element init = new Array.IndexedElement(value, new int[] { 0 });
        this.elements.clear();
        this.elements.add(init);
    }

    /**
     * Get the value held by the element contained in this IndependentElement.
     * 
     * @return The value held by the element contained in this
     *         IndependentElement.
     */
    public double getNumericValue () {
        if (this.elements.isEmpty()) {
            return 0;
        }
        return this.elements.get(0).getNumValue();
    }

    @Override
    public void clear () {
        this.elements.clear();
        this.oc.reset();
        this.repaintAll = true;
    }

    @Override
    public void applyOperation (Operation op) {
        super.applyOperation(op);
        this.repaintAll = true;
    }

    @Override
    protected void executeSwap (OP_Swap op) {
        Element e = this.elements.get(0);
        if (op.getVar1().identifier.equals(this.identifier)) {
            e.setValue(op.getValue() [0]);
            e.count(OperationType.swap);
            this.oc.count(OperationType.swap);
            return;
        } else if (op.getVar2().identifier.equals(this.identifier)) {
            e.setValue(op.getValue() [1]);
            e.count(OperationType.swap);
            this.oc.count(OperationType.swap);
            return;
        }
    }

    @Override
    protected void executeRW (OP_ReadWrite op) {
        if (this.elements.isEmpty()) {
            this.initElement(op.getValue() [0]);
        }
        Element e = this.elements.get(0);
        if (op.getTarget() != null && op.getTarget().identifier.equals(this.identifier)) {
            e.setValue(op.getValue() [0]);
            this.modifiedElements.add(e);
            e.count(OperationType.write);
            this.oc.count(OperationType.write);
            return;
        } else if (op.getSource() != null && op.getSource().identifier.equals(this.identifier)) {
            this.modifiedElements.add(e);
            e.count(OperationType.read);
            this.oc.count(OperationType.read);
        }
    }

    @Override
    public VisualType resolveVisual () {
        this.setVisual(VisualType.single);
        return VisualType.single;
    }

    @Override
    public IndexedElement getElement (Locator locator) {
        if (locator == null) {
            return null;
        }

        if (locator.identifier.equals(this.identifier) && !this.elements.isEmpty()) {
            return (IndexedElement) this.elements.get(0);
        } else {
            return null;
        }
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder();
        sb.append("\"" + Tools.stripQualifiers(this.identifier) + "\": " + this.rawType);
        return sb.toString();
    }
}
