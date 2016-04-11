package application.model;

import manager.datastructures.DataStructure;
import manager.operations.Key;
import manager.operations.OperationType;
import wrapper.Locator;
import wrapper.Operation;

import java.util.HashMap;
import java.util.Map;

public class Step implements iStep {
    private final Map<String, DataStructure> structs;
    private Operation lastOp;

    public Step(){
        structs = new HashMap<>();
    }

    public Step(Map<String, DataStructure> structs){
        this.structs = structs;
    }

    @Override
    public void addDataStructure(String identifier, DataStructure struct) {
        structs.put(identifier, struct);
    }

    @Override
    public void reset() {
        lastOp = null;
        structs.values().forEach(DataStructure::clear);
    }


    @Override
    public Map<String, DataStructure> getStructures() {
        //Should do a deep copy
        return structs;
    }

    @Override
    public Operation getLastOp(){
        return lastOp;
    }

    @Override
    public void applyOperation(Operation op) {
        OperationType opType = op.operation;
        switch(opType){
            case init:
                //Has the operation body value, target and size
                String identifier = ((Locator)op.operationBody.get(Key.target)).getIdentifier();
                structs.get(identifier).applyOperation(op);
                break;
            case message:
                break;
            case read:
                break;
            case write:
                break;
            case swap:
                break;
        }
        
        lastOp = op;

    }

}
