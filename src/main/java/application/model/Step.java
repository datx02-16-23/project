package application.model;

import manager.datastructures.DataStructure;
import manager.operations.Key;
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
        structs.values().forEach(DataStructure::reset);
    }


    @Override
    public Map<String, DataStructure> getStructures() {
        //Should do a deep copy
        return structs;
    }

    @Override
    public void applyOperation(Operation op) {
        //System.out.println("Applying op");
        //for(String struct:structs.keySet()){
            //String source = op.operationBody.get(Key.source);

        //}
    }

}
