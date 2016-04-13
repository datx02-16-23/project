package application.model;

import java.util.Map;

import wrapper.Operation;
import wrapper.datastructures.DataStructure;


//TODO: Javadoc #51
public interface iStep {
    Map<String, DataStructure> getStructures();
    Operation getLastOp();
    void applyOperation(Operation op);
    void addDataStructure(String id, DataStructure struct);
    void reset();
}
