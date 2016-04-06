package application.model;

import manager.datastructures.DataStructure;

import java.util.List;
import java.util.Map;

import manager.datastructures.DataStructure;
import wrapper.Operation;


public interface iStep {
    Map<String, DataStructure> getStructures();
    void applyOperation(Operation op);
    void addDataStructure(String id, DataStructure struct);
}
