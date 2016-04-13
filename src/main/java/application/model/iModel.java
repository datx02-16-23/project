package application.model;

import wrapper.Operation;

import java.util.List;
import java.util.Map;

import manager.datastructures.DataStructure;


public interface iModel {
    void addStructure(String id, DataStructure structure);
    void reset();
    boolean stepForward();
    boolean stepBackward();
    void set(Map<String, DataStructure> structs, List<Operation> ops);
    public Map<String, DataStructure> getStructures();
    public List<Operation> getOperations();
    
    iStep getCurrentStep();
    
    int getIndex();
    void goToStep(int toStepNo);
    void goToEnd();
}
