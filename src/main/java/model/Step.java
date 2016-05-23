package model;

import java.util.HashMap;
import java.util.Map;

import assets.Debug;
import contract.Locator;
import contract.Operation;
import contract.datastructure.DataStructure;
import contract.operation.Key;
import contract.operation.OP_Message;
import gui.Main;

public class Step {
    private final Map<String, DataStructure> structs;
    private Operation                        lastOp;

    public Step () {
        structs = new HashMap<>();
    }

    public Step (Map<String, DataStructure> structs) {
        this.structs = structs;
    }

    public void reset () {
        lastOp = null;
        structs.values().forEach(DataStructure::clear);
    }

    public Map<String, DataStructure> getStructures () {
        // Should do a deep copy
        return structs;
    }

    public Operation getLastOp () {
        return lastOp;
    }

    /**
     * Apply an operation to the model.
     *
     * @param op
     *            The operation to apply.
     * @return 0 if the operation should be applied.
     */
    public void applyOperation (Operation op) {
        Locator locator;
        DataStructure struct;
        switch (op.operation) {
        case message:
            Main.console.info("MESSAGE: " + ((OP_Message) op).getMessage());
            break;
        case read:
        case write:
            // Technically not identical, as read will always have a source and
            // write will always have a target.

            locator = (Locator) op.operationBody.get(Key.source);
            if (locator != null) {
                struct = structs.get(locator.identifier);
                if (struct != null) {
                    struct.applyOperation(op);
                }
            }

            locator = (Locator) op.operationBody.get(Key.target);
            if (locator != null) {
                struct = structs.get(locator.identifier);
                if (struct != null) {
                    struct.applyOperation(op);
                }
            }
            break;
        case swap:
            locator = (Locator) op.operationBody.get(Key.var1);
            structs.get(locator.identifier).applyOperation(op);

            locator = (Locator) op.operationBody.get(Key.var2);
            structs.get(locator.identifier).applyOperation(op);
            break;
        case remove:
            locator = (Locator) op.operationBody.get(Key.target);
            struct = structs.get(locator.identifier);
            if (struct != null) {
                struct.applyOperation(op);
            }
            break;
        default:
            Main.console.err("Unknown operation type: \"" + op.operation + "\"");
            break;
        }

        if (Debug.OUT) {
            System.out.print("Step.applyOperation(): " + op + "\n");
        }

        lastOp = op;
    }
}
