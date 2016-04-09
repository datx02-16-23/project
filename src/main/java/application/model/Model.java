package application.model;

import wrapper.Operation;

import java.util.List;
import java.util.Map;

import manager.datastructures.DataStructure;


public class Model implements iModel {
    private iStep step = new Step();
    private List<Operation> operations;
    private int index;


    @Override
    public void addStructure(String id, DataStructure structure) {
        step.addDataStructure(id, structure);
    }

    @Override
    public void reset() {
        index = 0;
        step.reset();
    }

    @Override
    public void stepForward() {
        if(operations != null && index < operations.size()){
            step.applyOperation(operations.get(index));
            index += 1;
        }
    }

    @Override
    public void stepBackward() {
        System.err.println("This is not implemented, try reset instead");
    }

    @Override
    public void set(Map<String, DataStructure> structs, List<Operation> ops) {
        //TODO Ensure that the structs don't contain elements inside
        structs.values().forEach(DataStructure::clear);
        step = new Step(structs);
        operations = ops;
        index = 0;
    }

    @Override
    public iStep getCurrentStep() {
        return step;
    }


}
