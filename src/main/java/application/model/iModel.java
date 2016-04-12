package application.model;

import wrapper.Operation;

import java.util.List;
import java.util.Map;

import manager.datastructures.DataStructure;


public interface iModel {
    void addStructure(String id, DataStructure structure);
    void reset();
    void stepForward();
    void stepBackward();
    void set(Map<String, DataStructure> structs, List<Operation> ops);
    iStep getCurrentStep();
    int getIndex();
}
