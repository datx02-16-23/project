package application.model;

import manager.datastructures.DataStructure;

import java.util.Map;

import wrapper.Operation;


public interface iStep {
    Map<String, DataStructure> getStructures();
    Operation getLastOp();
    void applyOperation(Operation op);
    void addDataStructure(String id, DataStructure struct);
    void reset();
}
