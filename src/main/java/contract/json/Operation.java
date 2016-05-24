package contract.json;

import java.io.Serializable;
import java.util.HashMap;

import assets.Const;
import contract.operation.Key;
import contract.operation.OperationType;

/**
 * Abstract wrapper class containing the necessary data to recreate a given operation. Should be
 * inherited to create specific operations.
 */
public class Operation implements Serializable {

    /**
     * Version number for this class.
     */
    private static final long         serialVersionUID = Const.VERSION_NUMBER;
    /**
     * The literal name of the operation.
     */
    public final OperationType        operation;
    /**
     * A map containing the identifier of the field (such as "target" or "value") and the data
     * they contained.
     */
    public final HashMap<Key, Object> operationBody;
    /**
     * The name of the source file this Operation originates from.
     */
    public String                     source;
    public int                        beginLine;
    public int                        endLine;
    public int                        beginColumn;
    public int                        endColumn;

    /**
     * /** Create a new Operation.
     *
     * @param operation
     *            The literal name of the operation, such as "init" (initialize) or "read".
     * @param operationBody
     *            A map containing the identifier of the field (such as "destination" or "value")
     *            and the data they contained.
     * @param source
     *            The source file this operation originates from.
     * @param beginLine
     *            The first line this operation originates from.
     * @param endLine
     *            The last line this operation originates from.
     * @param beginColumn
     *            The first column this operation originates from.
     * @param endColumn
     *            The last line column operation originates from.
     */
    public Operation (OperationType operation, HashMap<Key, Object> operationBody, String source, int beginLine,
            int endLine, int beginColumn, int endColumn) {
        this.operation = operation;
        this.operationBody = operationBody;
        this.source = source;
        this.beginLine = beginLine;
        this.endLine = endLine;
        this.beginColumn = beginColumn;
        this.endColumn = beginColumn;
    }

    public String toSimpleString () {
        return operation.toString().toUpperCase();
    }

    @Override public String toString () {
        return operationBody == null ? "null" : operationBody.toString();
    }
}
