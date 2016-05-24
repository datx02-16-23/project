package model;

import java.util.HashMap;
import java.util.Map;

import assets.Debug;
import contract.datastructure.DataStructure;
import contract.json.Locator;
import contract.json.Operation;
import contract.operation.Key;
import contract.operation.OP_Message;
import contract.utility.OpUtils;
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
        switch (op.operation) {

        case message:
            Main.console.info("MESSAGE: " + ((OP_Message) op).getMessage());
            break;
        case read:
        case write:
            Locator source = OpUtils.getLocator(op, Key.source);
            if (source != null) {
                DataStructure sourceStruct = structs.get(source.identifier);
                if (sourceStruct != null) {
                    sourceStruct.applyOperation(op);
                }
            }

            Locator target = OpUtils.getLocator(op, Key.target);
            if (target != null) {
                DataStructure targetStruct = structs.get(target.identifier);
                if (targetStruct != null) {
                    targetStruct.applyOperation(op);
                }
            }
            break;
        case swap:
            Locator var1 = OpUtils.getLocator(op, Key.var1);
            structs.get(var1.identifier).applyOperation(op);

            Locator var2 = OpUtils.getLocator(op, Key.var2);
            structs.get(var2.identifier).applyOperation(op);
            break;
        case remove:
            Locator removeTarget = OpUtils.getLocator(op, Key.target);
            DataStructure targetStruct = structs.get(removeTarget.identifier);
            if (targetStruct != null) {
                targetStruct.applyOperation(op);
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
