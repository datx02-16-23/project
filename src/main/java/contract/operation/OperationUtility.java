package contract.operation;

import contract.Locator;
import contract.Operation;

/**
 * Utility class for operations.
 * @author Richard Sundqvist
 *
 */
public abstract class OperationUtility {

    public static Locator getLoactor (Operation op, Key locatorKey) {
        Locator ans = null;
        switch (locatorKey) {
        case source:
        case target:
        case var1:
        case var2:
            ans = (Locator) op.operationBody.get(locatorKey);
        default:
            break;
        }

        return ans;
    }

    public static String getIdentifier (Operation op) {
        return (String) op.operationBody.get(Key.identifier);
    }

    public static double[] getValue (Operation op) {
        return (double[]) op.operationBody.get(Key.value);
    }

    public static int[] getIndex (Operation op) {
        return (int[]) op.operationBody.get(Key.index);
    }

    public static int[] getSize (Operation op) {
        return (int[]) op.operationBody.get(Key.size);
    }

    public static OperationType getOperationTyope (Operation op) {
        return (OperationType) op.operationBody.get(Key.operation);
    }
    
    public boolean isAtomic(Operation op){
        return (op.operation == OperationType.read || op.operation == OperationType.write);
    }
}
