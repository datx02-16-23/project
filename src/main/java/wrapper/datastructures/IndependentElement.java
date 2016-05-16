package wrapper.datastructures;

import java.util.Map;

import application.assets.Strings;
import application.visualization.VisualType;
import wrapper.Locator;
import wrapper.Operation;
import wrapper.operations.OP_ReadWrite;
import wrapper.operations.OP_Swap;
import wrapper.operations.OperationType;

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
	private static final long serialVersionUID = Strings.VERSION_NUMBER;

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
		return elements.get(0).getNumericValue();
	}

	@Override
	public void clear() {
		elements.clear();
	}

	@Override
	public void applyOperation(Operation op) {
		super.applyOperation(op);
		repaintAll = true; // Clears background. Theres only one Element present
							// anyway.
	}

	protected void executeSwap(OP_Swap op) {
		Element e = elements.get(0);
		if (op.getVar1().identifier.equals(this.identifier)) {
			e.setNumValue(op.getValue()[0]);
			e.setColor(OperationType.swap.color);
			return;
		} else if (op.getVar2().identifier.equals(this.identifier)) {
			e.setNumValue(op.getValue()[1]);
			e.setColor(OperationType.swap.color);
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
			e.setNumValue(op.getValue()[0]);
			modifiedElements.add(e);
			e.setColor(OperationType.write.color);
			return;
		} else if (op.getSource() != null && op.getSource().identifier.equals(this.identifier)) {
			modifiedElements.add(e);
			e.setColor(OperationType.read.color);
		}
	}

	@Override
	public VisualType resolveVisual() {
		return VisualType.box;
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
