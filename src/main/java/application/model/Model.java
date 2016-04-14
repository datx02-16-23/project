package application.model;

import wrapper.Operation;
import wrapper.datastructures.DataStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model implements iModel {

    private iStep           step = new Step();
    private final List<Operation> operations = new ArrayList<Operation>();
    private int             index;

    @Override
    public void addStructure (String id, DataStructure structure){
        step.addDataStructure(id, structure);
    }

    @Override
    public void reset (){
        index = 0;
        step.reset();
    }

    @Override
    public boolean stepForward (){
        if (operations != null && index < operations.size()) {
            step.applyOperation(operations.get(index));
            index += 1;
            return true;
        }
        return false;
    }

    @Override
    public boolean stepBackward (){
        if (index == 0) {
            return false;
        }
        int oldIndex = index - 1;
        reset(); //Can't go backwards: Start from the beginning
        while(index < oldIndex) {
            stepForward();
        }
        return true;
    }

    public void goToStep (int toStepNo){
        if (operations == null || toStepNo >= operations.size() || toStepNo < 0) {
            return;
        }
        if (toStepNo < index) {
            reset(); //Can't go backwards: Start from the beginning
            while(index < toStepNo) {
                stepForward();
            }
        }
        else if (toStepNo > index) {
            while(index < toStepNo) {
                stepForward();
            }
        }
        //Do nothing if index == toStepNo
    }

    @Override
    public void set (Map<String, DataStructure> structs, List<Operation> ops){
        structs.values().forEach(DataStructure::clear); //TODO: Varför rensar vi här?
        step = new Step(new HashMap<String, DataStructure>(structs));
        operations.addAll(ops);
        index = 0;
    }

    @Override
    public iStep getCurrentStep (){
        return step;
    }

    @Override
    public int getIndex (){
        return index;
    }

    @Override
    public void goToEnd (){
        while(operations != null && index < operations.size()) {
            step.applyOperation(operations.get(index));
            index += 1;
        }
    }

    @Override
    public Map<String, DataStructure> getStructures (){
        return step.getStructures();
    }

    @Override
    public List<Operation> getOperations (){
        return operations;
    }

    @Override
    public void setOperations (List<Operation> newOperations){
        operations.clear();
        operations.addAll(newOperations);
        index = 0;
    }

    @Override
    public void setStructures (Map<String, DataStructure> newStructures){
        newStructures.values().forEach(DataStructure::clear);
        step = new Step(new HashMap<String, DataStructure>(newStructures));
        index = 0;
    }
}
