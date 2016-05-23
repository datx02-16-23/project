package contract.operation;

import assets.Const;
import contract.Locator;

/**
 * Create a new Read operation.
 */
public class OP_Read extends OP_ReadWrite {

    /**
     * Version number for this class.
     */
    private static final long          serialVersionUID = Const.VERSION_NUMBER;
    private static final OperationType OPERATION        = OperationType.read;

    /**
     * Create a new Read operation. Note that you must set the target, source
     * and value.
     */
    public OP_Read () {
        super(OPERATION);
    }

    public OP_Read (String source, int beginLine, int endLine, int beginColumn, int endColumn) {
        super(OPERATION, source, beginLine, endLine, beginColumn, endColumn);
    }

    /**
     * Set the target variable for this Read operation. The identifier of the
     * variable should be previously declared in the header.
     * 
     * @param target
     *            The target variable for this Read operation.
     */
    @Override
    public void setTarget (Locator target) {
        this.operationBody.put(Key.target, target);
    }

    /**
     * Set the source variable for this Read operation. The identifier of the
     * variable should be previously declared in the header.
     * 
     * @param source
     *            The source variable for this Read operation.
     */
    @Override
    public void setSource (Locator source) {
        if (source == null) {
            System.err.println("Source be null in Write operation!");
        }
        this.operationBody.put(Key.source, source);
    }

    /**
     * Set the value(s) which were read from {@code source}. This should be the
     * value of {@code target} and the specified index after operation
     * execution, if applicable.
     * 
     * @param value
     *            Set the value(s) which were read from {@code source}.
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
