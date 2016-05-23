package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import assets.Debug;
import contract.Operation;
import contract.datastructure.DataStructure;
import contract.operation.OperationType;

public class Model {

    private static final Model    INSTANCE = new Model("INSTANCE");

    /**
     * List of low level operations. <br>
     * <b>Atomic operations:</br>
     * {@link OperationType#read}<br>
     * {@link OperationType#write}<br>
     * {@link OperationType#message}<br>
     */
    private final List<Operation> atomicOperations;

    /**
     * List of operations which may include height level, non-atomic operations.
     * <br>
     * <b>Atomic operations:</br>
     * {@link OperationType#read}<br>
     * {@link OperationType#write}<br>
     * {@link OperationType#message}<br>
     */
    private final List<Operation> allOperations;
    /**
     * The name of the model.
     */
    public final String           name;
    private boolean               inInitialState;
    /**
     * Used to execute operations.
     */
    private Step                  step;
    /**
     * Current operation index.
     */
    private int                   index;

    /**
     * Returns the Model instance.
     * 
     * @return The Model instance.
     */
    public static Model instance () {
        return INSTANCE;
    }

    /**
     * Constructs a new Model.
     * 
     * @param name
     *            The name of the model.
     */
    public Model (String name) {
        this.name = name;

        this.atomicOperations = new ArrayList<Operation>();
        this.allOperations = new ArrayList<Operation>();
        this.step = new Step();
        this.index = 0;
        this.inInitialState = true;
    }

    /**
     * Constructs a new Model with a random int value as name.
     */
    public Model () {
        this("" + (int) (Math.random() * Integer.MAX_VALUE));
    }

    public void reset () {
        this.index = 0;
        this.step.reset();
    }

    /**
     * Restore the model to its initial state.
     */
    public void clear () {
        this.index = 0;
        this.step.reset();
        this.atomicOperations.clear();
    }

    /**
     * Wipe the model clean.
     */
    public void hardClear () {
        this.index = 0;
        this.step = new Step();
        this.atomicOperations.clear();
        this.inInitialState = true;
    }

    /**
     * Returns true if the model can step forward.
     * 
     * @return True if the model can step forward. False otherwise.
     */
    public boolean tryStepForward () {
        return this.atomicOperations != null && this.index < this.atomicOperations.size();
    }

    /**
     * Returns true if the model can step backward.
     * 
     * @return True if the model can step backward. False otherwise.
     */
    public boolean tryStepBackward () {
        return this.index != 0;
    }

    /**
     * Step the model forward.
     * 
     * @return True if the model was successfully moved forward. False
     *         otherwise.
     */
    public boolean stepForward () {
        if (this.tryStepForward()) {
            this.step.applyOperation(this.atomicOperations.get(this.index));
            if (Debug.OUT) {
                System.out.print("Model.stepForward(): index = " + this.index + " -> ");
            }
            this.index += 1;
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
    public boolean stepBackward () {
        if (this.tryStepBackward()) {
            int oldIndex = this.index - 1;
            this.reset(); // Can't go backwards: Start from the beginning
            while (this.index < oldIndex) {
                this.stepForward();
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
    public void goToStep (int toStepNo) {
        if (toStepNo <= 0) {
            this.reset();
            return;
        } else if (toStepNo >= this.atomicOperations.size()) {
            toStepNo = this.atomicOperations.size();
        }
        // Begin
        if (toStepNo < this.index) {
            this.reset(); // Can't go backwards: Start from the beginning
            while (this.index < toStepNo) {
                this.stepForward();
            }
        } else if (toStepNo > this.index) {
            this.goToEnd();
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
    public void set (Map<String, DataStructure> structs, List<Operation> ops) {
        if ((structs == null || structs.isEmpty()) && (ops == null || ops.isEmpty())) {
            return;
        }
        structs.values().forEach(DataStructure::clear);
        this.step = new Step(new HashMap<String, DataStructure>(structs));
        this.atomicOperations.clear();
        this.atomicOperations.addAll(ops);
        this.index = 0;
    }

    /**
     * Returns the last operation.
     * 
     * @return The most recently executed Operation. May be null.
     */
    public Operation getLastOp () {
        return this.step.getLastOp();
    }

    /**
     * Returns the current index.
     * 
     * @return The current index.
     */
    public int getIndex () {
        return this.index;
    }

    /**
     * Advance the model to the end.
     */
    public void goToEnd () {
        boolean success;
        do {
            success = this.stepForward();
        } while (success);
    }

    /**
     * Returns the DataStructure map held by this Model.<br>
     * <br>
     * <b>Should not be used to add or remove structures!</b>
     * 
     * @return The DataStructure map held by this Model.
     */
    public Map<String, DataStructure> getStructures () {
        return this.step.getStructures();
    }

    /**
     * Returns the Operation list held by this Model.<br>
     * <br>
     * <b>Should not be used to add or removed operations!</b>
     * 
     * @return The Operation list held by this Model.
     */
    public List<Operation> getOperations () {
        return this.atomicOperations;
    }

    /**
     * Set the Operation list used by this Model, and reset the index. The
     * previous list will be lost.
     * 
     * @param newOperations
     *            The new list of operations to use.
     */
    public void setOperations (List<Operation> newOperations) {
        this.atomicOperations.clear();
        this.atomicOperations.addAll(newOperations);
        this.index = 0;
    }

    /**
     * Returns true if this model has been hard cleared, or if it has not been
     * changed since the the constructor was called.
     * 
     * @return True if this Model is in its initial state.
     */
    public boolean isHardCleared () {
        this.inInitialState = this.step.getStructures().isEmpty() && this.atomicOperations.isEmpty();
        return this.inInitialState;
    }
}
