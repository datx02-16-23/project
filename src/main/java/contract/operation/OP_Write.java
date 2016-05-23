package contract.operation;

import assets.Const;
import contract.Locator;

/**
 * Create a new Write operation.
 */
public class OP_Write extends OP_ReadWrite {

    /**
     * Version number for this class.
     */
    private static final long          serialVersionUID = Const.VERSION_NUMBER;
    private static final OperationType OPERATION        = OperationType.write;

    /**
     * Create a new Write operation. Note that you must set the target, source
     * and value.
     */
    public OP_Write () {
        super(OPERATION);
    }

    public OP_Write (String source, int beginLine, int endLine, int beginColumn, int endColumn) {
        super(OPERATION, source, beginLine, endLine, beginColumn, endColumn);
    }

    /**
     * Set the target variable for this Write operation. The identifier of the
     * variable should be previously declared in the header.
     * 
     * @param target
     *            The target variable for this Write operation.
     */
    @Override
    public void setTarget (Locator target) {
        if (target == null) {
            System.err.println("Target null in Write operation!");
        }
        this.operationBody.put(Key.target, target);
    }

    /**
     * Set the source variable for this Write operation. The identifier of the
     * variable should be previously declared in the header.
     * 
     * @param source
     *            The source variable for this Write operation.
     */
    @Override
    public void setSource (Locator source) {
        this.operationBody.put(Key.source, source);
    }

    /**
     * Set the value(s) which were written to {@code target} (from
     * {@code source}, if applicable).
     * 
     * @param value
     *            Set the value(s) written to {@code target}.
     */
    @Override
    public void setValue (double[] value) {
        this.operationBody.put(Key.value, value);
    }

    @Override
    public Locator getTarget () {
        return (Locator) this.operationBody.get(Key.target);
    }

    @Override
    public Locator getSource () {
        return (Locator) this.operationBody.get(Key.source);
    }

    @Override
    public double[] getValue () {
        return (double[]) this.operationBody.get(Key.value);
    }
}
