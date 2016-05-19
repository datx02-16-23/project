package contract.datastructure;

import java.util.Map;

import assets.DasConstants;
import contract.Locator;
import contract.Operation;
import contract.operation.OP_ReadWrite;
import contract.operation.OP_Swap;

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
	private static final long serialVersionUID = DasConstants.VERSION_NUMBER;

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
	public IndependentElement(String identifier, RawType.AbstractType abstractType, VisualType visual,
			Map<String, Object> attributes) {
		super(identifier, RawType.independentElement, abstractType, visual, attributes);
	}

	/**
	 * Set the element held by this IndependentElement.
	 * 
	 * @param newElement
	 *            The new element to be held by this IndependentElement.
	 */
	public void setElement(Element newElement) {
		elements.clear();
		elements.add(newElement);
	}

	/**
	 * Initialize an element with value 0.
	 * 
	 * @param value
	 *            The value to initialize with.
	 */
	public void initElement(double value) {
		Element init = new Array.IndexedElement(value, new int[] { 0 });
		elements.clear();
		elements.add(init);
	}

	/**
	 * Get the value held by the element contained in this IndependentElement.
	 * 
	 * @return The value held by the element contained in this
	 *         IndependentElement.
	 */
	public double getNumericValue() {
		if (elements.isEmpty()) {
			return 0;
		}
		return elements.get(0).numValue();
	}

	@Override
	public void clear() {
		elements.clear();
		oc.reset();
	}

	@Override
	public void applyOperation(Operation op) {
		super.applyOperation(op);
		repaintAll = true;
	}

	protected void executeSwap(OP_Swap op) {
		Element e = elements.get(0);
		if (op.getVar1().identifier.equals(this.identifier)) {
			e.setValue(op.getValue()[0]);
			e.execute(op);
			oc.count(op);
			return;
		} else if (op.getVar2().identifier.equals(this.identifier)) {
			e.setValue(op.getValue()[1]);
			e.execute(op);
			oc.count(op);
			return;
		}
	}

	@Override
	protected void executeRW(OP_ReadWrite op) {
		if (elements.isEmpty()) {
			initElement(op.getValue()[0]);
		}
		Element e = elements.get(0);
		if (op.getTarget() != null && op.getTarget().identifier.equals(this.identifier)) {
			e.setValue(op.getValue()[0]);
			modifiedElements.add(e);
			e.execute(op);
			oc.count(op);
			return;
		} else if (op.getSource() != null && op.getSource().identifier.equals(this.identifier)) {
			modifiedElements.add(e);
			e.execute(op);
			oc.count(op);
		}
	}

	@Override
	public VisualType resolveVisual() {
		setVisual(VisualType.single);
		return VisualType.single;
	}

	@Override
	public IndexedElement getElement(Locator locator) {
		if (locator == null) {
			return null;
		}
		if (locator.identifier.equals(super.identifier) && elements.isEmpty() == false) {
			return (IndexedElement) elements.get(0);
		} else {
			return null;
		}
	}
}
