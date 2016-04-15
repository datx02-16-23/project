package wrapper;

import java.io.Serializable;
import java.util.HashMap;

import application.assets.Strings;
import wrapper.operations.Key;
import wrapper.operations.OperationType;

/**
 * Abstract wrapper class containing the necessary data to recreate a given operation. Should be inherited to create
 * specific operations.
 */
public class Operation implements Serializable {

    /**
     * Version number for this class.
     */
    private static final long         serialVersionUID = Strings.VERSION_NUMBER;
    /**
     * The literal name of the operation, such as "init" (initialize) or "read".
     */
    public final OperationType        operation;
    /**
     * A map containing the identifier of the field (such as "destination" or "value") and the data they contained.
     */
    public final HashMap<Key, Object> operationBody;
    
    /**
     * The name of the source file this Operation originates from.
     */
    public String source;
    
    /**
     * The line number this Operation originates from.
     */
    public int beginLine;
    /**
     * The last line of this Operation.
     */
    public int endLine;
    /**
     * TODO: Javadoc.
     */
    public int beginColumn;
    /**
     * TODO: Javadoc.
     */
    public int endColumn;

    /**
     * Create a new Operation with the given operation identifier and body.
     * 
     * @param operation The literal name of the operation, such as "init" (initialize) or "read".
     * @param operationBody A map containing the identifier of the field (such as "destination" or "value") and the data
     * they contained.
     */
    public Operation (OperationType operation, HashMap<Key, Object> operationBody, String source, int beginLine, int endLine, int beginColumn, int endColumn){
        this.operation = operation;
        this.operationBody = operationBody;
        this.source = source;
        this.beginLine = beginLine;
        this.endLine = endLine;
        this.beginColumn = beginColumn;
        this.endColumn = beginColumn;
    }

    public String printOperationBody (){
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Key key : operationBody.keySet()) {
            if (operationBody.get(key) != null) {
                builder.append("\"" + key + "\": " + operationBody.get(key).toString() + ",\n");
            }
            else {
                builder.append("\"" + key + "\": NULL");
            }
        }
        builder.delete(builder.length() - 2, builder.length());
        builder.append("}");
        return builder.toString();
    }

    public String toSimpleString (){
        return operation.toString().toUpperCase();
    }

    public String toString (){
        return operationBody.toString();
    }
}
