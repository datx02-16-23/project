package application.model;

import wrapper.Locator;
import wrapper.Operation;
import wrapper.datastructures.DataStructure;
import wrapper.operations.*;

import java.util.HashMap;
import java.util.Map;

import application.gui.Main;

public class Step implements iStep {

    private final Map<String, DataStructure> structs;
    private Operation                        lastOp;

    public Step (){
        structs = new HashMap<>();
    }

    public Step (Map<String, DataStructure> structs){
        this.structs = structs;
    }
    
    @Override
    public void reset (){
        lastOp = null;
        structs.values().forEach(DataStructure::clear);
    }

    @Override
    public Map<String, DataStructure> getStructures (){
        //Should do a deep copy
        return structs;
    }

    @Override
    public Operation getLastOp (){
        return lastOp;
    }

    @Override
    public void applyOperation (Operation op){
        OperationType opType = op.operation;
        String identifier;
        Locator locator;
        DataStructure struct;
        switch (opType) {
            case message:
                Main.console.info(((OP_Message)op).getMessage());
                break;
            case read:
            case write:
                //Technically not identical, as read will always have a source and write will always have a target.
                //They are treated the same in Array however, so will will treat them the same here as well.
                locator = ((Locator) op.operationBody.get(Key.source));
                if (locator != null) {
                    identifier = locator.getIdentifier();
                    struct = structs.get(identifier);
                    if (struct == null) {
                        Main.console.err("WARNING: Undeclared variable \"" + identifier + "\". " + op + " aborted.");
                        break;
                    }
                    struct.applyOperation(op);
                }
                locator = ((Locator) op.operationBody.get(Key.target));
                if (locator != null) {
                    identifier = locator.getIdentifier();
                    struct = structs.get(identifier);
                    if (struct == null) {
                        Main.console.err("WARNING: Undeclared variable \"" + identifier + "\". " + op + " aborted.");
                        break;
                    }
                    struct.applyOperation(op);
                }
                break;
            case swap:
                //No checking here - swap should always have a var1 and var2.
                identifier = ((Locator) op.operationBody.get(Key.var1)).getIdentifier();
                structs.get(identifier).applyOperation(op);
                identifier = ((Locator) op.operationBody.get(Key.var2)).getIdentifier();
                structs.get(identifier).applyOperation(op);
                break;
        }
        lastOp = op;
    }
}
