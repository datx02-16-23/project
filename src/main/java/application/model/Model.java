package application.model;

import wrapper.Operation;
import wrapper.datastructures.DataStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model implements iModel {

    private iStep                 step       = new Step();
    private final List<Operation> operations = new ArrayList<Operation>();
    private int                   index;

    @Override
    public void reset (){
        index = 0;
        step.reset();
    }
    
    /**
     * Restore the model to its inisual stet.
     */
    public void clear(){
        index = 0;
        step = new Step();
        operations.clear();
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

    @Override
    public void goToStep (int toStepNo){
        if (toStepNo <= 0) {
            reset();
            return;
        }
        else if (toStepNo >= operations.size()) {
            toStepNo = operations.size();
        }
        //Begin
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
        structs.values().forEach(DataStructure::clear); //TODO: Varf�r rensar vi h�r?
        step = new Step(new HashMap<String, DataStructure>(structs));
        operations.clear();
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
}
