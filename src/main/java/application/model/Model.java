package application.model;

import wrapper.Operation;
import wrapper.datastructures.DataStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {

    private static Model          INSTANCE;
    private Step                  step       = new Step();
    private final List<Operation> operations = new ArrayList<Operation>();
    private int                   index;

    /**
     * Returns the Model instance.
     * 
     * @return The Model instance.
     */
    public static Model instance (){
        if (INSTANCE == null) {
            INSTANCE = new Model();
        }
        return INSTANCE;
    }

    private Model (){
    }

    public void reset (){
        index = 0;
        step.reset();
    }

    /**
     * Restore the model to its inisual stet.
     */
    public void clear (){
        index = 0;
        step = new Step();
        operations.clear();
    }

    public boolean stepForward (){
        if (operations != null && index < operations.size()) {
            step.applyOperation(operations.get(index));
            index += 1;
            return true;
        }
        return false;
    }

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

    public void set (Map<String, DataStructure> structs, List<Operation> ops){
        structs.values().forEach(DataStructure::clear); //TODO: Varf�r rensar vi h�r?
        step = new Step(new HashMap<String, DataStructure>(structs));
        operations.clear();
        operations.addAll(ops);
        index = 0;
    }

    public Step getCurrentStep (){
        return step;
    }

    public int getIndex (){
        return index;
    }

    public void goToEnd (){
        while(operations != null && index < operations.size()) {
            step.applyOperation(operations.get(index));
            index += 1;
        }
    }

    public Map<String, DataStructure> getStructures (){
        return step.getStructures();
    }

    public List<Operation> getOperations (){
        return operations;
    }

    public void setOperations (List<Operation> newOperations){
        operations.clear();
        operations.addAll(newOperations);
        index = 0;
    }
}
