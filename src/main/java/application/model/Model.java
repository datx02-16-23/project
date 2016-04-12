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
        } else {
        	System.err.println("No more operations. Try reset.");
        }
    }

    @Override
    public void stepBackward() {
    	int oldIndex = index-1;
    	reset(); //Can't go backwards: Start from the beginning
    	while (index < oldIndex){
    		stepForward();
    	}
    }
    
    public void goToStep(int toStepNo){
    	if(toStepNo < 0){
    		System.err.println("Step must be >= 0.");
    		return;
    	}
    	
    	if (toStepNo < index){
    		reset(); //Can't go backwards: Start from the beginning
        	while (index < toStepNo){
        		stepForward();
        	}
        	
    	} else if (toStepNo > index){
        	while (index < toStepNo){
        		stepForward();
        	}
    	}
    	
    	//Do nothing if index == toStepNo
    	
    }

    @Override
    public void set(Map<String, DataStructure> structs, List<Operation> ops) {
        structs.values().forEach(DataStructure::clear);
        step = new Step(structs);
        operations = ops;
        index = 0;
    }

    @Override
    public iStep getCurrentStep() {
        return step;
    }
    
    public int getIndex(){
    	return index;
    }


}
