package contract.operation;

import assets.Const;
import contract.json.Locator;

public class OP_ToggleScope extends OP_ReadWrite {

    private static final long serialVersionUID = Const.VERSION_NUMBER;

    /**
     * Create an empty Remove operation.
     */
    public OP_ToggleScope () {
        super(OperationType.remove);
    }

    /**
     * Set the target variable for this Remove operation.
     *
     * @param target
     *            The target variable for this Remove operation.
     */
    @Override public void setTarget (Locator target) {
        operationBody.put(Key.target, target);
    }

    @Override public String toString () {
        return super.operation.toString().toUpperCase() + ": " + getTarget().toString();
    }

    @Override public Locator getTarget () {
        return (Locator) operationBody.get(Key.target);
    }
}
