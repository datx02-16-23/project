package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import contract.Operation;
import contract.datastructure.DataStructure;

public class Model {

	private static final Model INSTANCE = new Model();
	private Step step = new Step();
	private final List<Operation> operations = new ArrayList<Operation>();
	private int index;
	private boolean hardClear = true;

	/**
	 * Returns the Model instance.
	 * 
	 * @return The Model instance.
	 */
	public static Model instance() {
		return INSTANCE;
	}

	public void reset() {
		index = 0;
		step.reset();
	}

	/**
	 * Restore the model to its initial state.
	 */
	public void clear() {
		index = 0;
		step.reset();
		operations.clear();
	}

	/**
	 * Wipe the model clean.
	 */
	public void hardClear() {
		index = 0;
		step = new Step();
		operations.clear();
		hardClear = true;
	}

	/**
	 * Returns true if the model can step forward.
	 * 
	 * @return True if the model can step forward. False otherwise.
	 */
	public boolean tryStepForward() {
		return operations != null && index < operations.size();
	}

	/**
	 * Returns true if the model can step backward.
	 * 
	 * @return True if the model can step backward. False otherwise.
	 */
	public boolean tryStepBackward() {
		return index != 0;
	}

	/**
	 * Step the model forward.
	 * 
	 * @return True if the model was successfully moved forward. False
	 *         otherwise.
	 */
	public boolean stepForward() {
		//Dont remove yet!
//		double[] test = {1, 2, 3, 5, 6, 7, 24, 26};
//		ArrayList<Double> test2 = new ArrayList<Double>();
//		for(double d : test){
//			test2.add(d-1);
//		}
//		if(test2.contains(new Double(index))){ 
//			System.out.println("\nop: " + operations.get(index));
//		}
		if (tryStepForward()) {
			step.applyOperation(operations.get(index));
			index += 1;
			return true;
		}
		return false;
	}

	/**
	 * Step the model backwards. This method will reset the model and call
	 * stepForward() index - 1 times.
	 * 
	 * @return True if the model was successfully moved backward. False
	 *         otherwise.
	 */
	public boolean stepBackward() {
		if (tryStepBackward()) {
			int oldIndex = index - 1;
			reset(); // Can't go backwards: Start from the beginning
			while (index < oldIndex) {
				stepForward();
			}
			return true;
		}
		return false;
	}

	/**
	 * Jump to a given step. Will jump to the beginning if {@code toStepNo < 0},
	 * or to the end if {@code toStepNo > operations.size()}.
	 * 
	 * @param toStepNo
	 *            The step to jump to.
	 */
	public void goToStep(int toStepNo) {
		if (toStepNo <= 0) {
			reset();
			return;
		} else if (toStepNo >= operations.size()) {
			toStepNo = operations.size();
		}
		// Begin
		if (toStepNo < index) {
			reset(); // Can't go backwards: Start from the beginning
			while (index < toStepNo) {
				stepForward();
			}
		} else if (toStepNo > index) {
			goToEnd();
		}
		
	}
	
	/**
	 * Set the structures and operations used by this Model.
	 * 
	 * @param structs
	 *            The DataStructure map to use.
	 * @param ops
	 *            The Operation list to use.
	 */
	public void set(Map<String, DataStructure> structs, List<Operation> ops) {
		if ((structs == null || structs.isEmpty()) && (ops == null || ops.isEmpty())) {
			return;
		}
		structs.values().forEach(DataStructure::clear);
		step = new Step(new HashMap<String, DataStructure>(structs));
		operations.clear();
		operations.addAll(ops);
		index = 0;
	}

	/**
	 * Returns the last operation.
	 * 
	 * @return The most recently executed Operation. May be null.
	 */
	public Operation getLastOp() {
		return step.getLastOp();
	}

	/**
	 * Returns the current index.
	 * 
	 * @return The current index.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Advance the model to the end.
	 */
	public void goToEnd() {
		boolean success;
		do {
			success = stepForward();
		} while (success);
	}

	/**
	 * Returns the DataStructure map held by this Model.<br>
	 * <br>
	 * <b>Should not be used to add or remove structures!</b>
	 * 
	 * @return The DataStructure map held by this Model.
	 */
	public Map<String, DataStructure> getStructures() {
		return step.getStructures();
	}

	/**
	 * Returns the Operation list held by this Model.<br>
	 * <br>
	 * <b>Should not be used to add or removed operations!</b>
	 * 
	 * @return The Operation list held by this Model.
	 */
	public List<Operation> getOperations() {
		return operations;
	}

	/**
	 * Set the Operation list used by this Model, and reset the index. The
	 * previous list will be lost.
	 * 
	 * @param newOperations
	 *            The new list of operations to use.
	 */
	public void setOperations(List<Operation> newOperations) {
		operations.clear();
		operations.addAll(newOperations);
		index = 0;
	}

	/**
	 * Returns true if this model has been hard cleared, or if it has not been
	 * changed since the the constructor was called.
	 * 
	 * @return True if this Model is in its initial state.
	 */
	public boolean isHardCleared() {
		hardClear = step.getStructures().isEmpty() && operations.isEmpty();
		return hardClear;
	}
}
