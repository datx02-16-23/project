package contract.operation;

import assets.Strings;
import contract.Locator;

public class OP_Remove extends OP_ReadWrite {

	private static final long serialVersionUID = Strings.VERSION_NUMBER;

	/**
	 * Create an empty Remove operation.
	 */
	public OP_Remove() {
		super(OperationType.remove);
	}

	/**
	 * Set the target variable for this Remove operation.
	 * 
	 * @param target
	 *            The target variable for this Remove operation.
	 */
	public void setTarget(Locator target) {
		this.operationBody.put(Key.target, target);
	}

	@Override
	public String toString() {
		return super.operation.toString().toUpperCase() + ": " + getTarget().toString();
	}

	public Locator getTarget() {
		return (Locator) this.operationBody.get(Key.target);
	}
}
