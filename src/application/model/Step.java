package application.model;

import manager.datastructures.DataStructure;
import manager.operations.OperationType;
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
    public Map<String, DataStructure> getStructures() {
        //Should do a deep copy
        return structs;
    }

    @Override
    public void applyOperation(Operation op) {
        updateStructs(op);
        lastOp = op;
    }

    private void updateStructs(Operation op) {
        //op.
    }

}
