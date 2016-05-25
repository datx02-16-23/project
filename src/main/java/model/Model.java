package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import assets.Debug;
import contract.datastructure.DataStructure;
import contract.json.Operation;
import contract.operation.OperationType;

public class Model {

    /**
     * The default model instance.
     */
    public static final Model     INSTANCE = new Model("INSTANCE");

    /**
     * List of low level operations. <br>
     * <b>Atomic operations:</br>
     * {@link OperationType#read}<br>
     * {@link OperationType#write}<br>
     * {@link OperationType#message}<br>
     */
    private final List<Operation> atomicOperations;

    /**
     * List of operations which may include height level, non-atomic operations. <br>
     * <b>Atomic operations:</br>
     * {@link OperationType#read}<br>
     * {@link OperationType#write}<br>
     * {@link OperationType#message}<br>
     */
    private final List<Operation> mixedOperations;
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

        atomicOperations = new ArrayList<Operation>();
        mixedOperations = new ArrayList<Operation>();
        step = new Step();
        index = 0;
        inInitialState = true;
    }

    /**
     * Constructs a new Model with a random int value as name.
     */
    public Model () {
        this("" + (int) (Math.random() * Integer.MAX_VALUE));
    }

    public void restart () {
        index = 0;
        step.reset();
    }

    /**
     * Wipe the model clean.
     */
    public void clear () {
        index = 0;
        step = new Step();
        atomicOperations.clear();
        mixedOperations.clear();
        inInitialState = true;
    }

    /**
     * Returns true if the model can step forward.
     *
     * @return True if the model can step forward. False otherwise.
     */
    public boolean tryStepForward () {
        return index < atomicOperations.size();
    }

    /**
     * Returns true if the model can step backward.
     *
     * @return True if the model can step backward. False otherwise.
     */
    public boolean tryStepBackward () {
        return index != 0;
    }

    /**
     * Step the model forward.
     *
     * @return True if the model was successfully moved forward. False otherwise.
     */
    public boolean stepForward () {
        if (tryStepForward()) {
            step.applyOperation(atomicOperations.get(index));
            if (Debug.OUT) {
                System.out.print("Model.stepForward(): index = " + index + " -> ");
            }
            index += 1;
            return true;
        }
        return false;
    }

    /**
     * Step the model backwards. This method will reset the model and call stepForward()
     * index - 1 times.
     *
     * @return True if the model was successfully moved backward. False otherwise.
     */
    public boolean stepBackward () {
        if (tryStepBackward()) {
            int oldIndex = index - 1;
            restart(); // Can't go backwards: Start from the beginning
            while (index < oldIndex) {
                stepForward();
            }
            return true;
        }
        return false;
    }

    /**
     * Jump to a given step. Will jump to the beginning if {@code toStepNo < 0}, or to the
     * end if {@code toStepNo > operations.size()}.
     *
     * @param toStepNo
     *            The step to jump to.
     */
    public void goToStep (int toStepNo) {
        if (toStepNo <= 0) {
            restart();
            return;
        } else if (toStepNo >= atomicOperations.size()) {
            toStepNo = atomicOperations.size();
        }
        // Begin
        if (toStepNo < index) {
            restart(); // Can't go backwards: Start from the beginning
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
    public void set (Map<String, DataStructure> structs, List<Operation> ops) {
        if ((structs == null || structs.isEmpty()) && (ops == null || ops.isEmpty())) {
            return;
        }
        structs.values().forEach(DataStructure::clear);
        step = new Step(new HashMap<String, DataStructure>(structs));
        atomicOperations.clear();
        mixedOperations.clear();
        atomicOperations.addAll(ops);
        index = 0;
    }

    /**
     * Returns the last operation.
     *
     * @return The most recently executed Operation. May be null.
     */
    public Operation getLastOp () {
        return step.getLastOp();
    }

    /**
     * Returns the current index.
     *
     * @return The current index.
     */
    public int getIndex () {
        return index;
    }

    /**
     * Advance the model to the end.
     */
    public void goToEnd () {
        boolean success;
        do {
            success = stepForward();
        } while (success);
    }

    /**
     * Returns the DataStructure map held by this Model. Should not be used to add or
     * remove structures.
     *
     * @return The DataStructure map held by this Model.
     */
    public Map<String, DataStructure> getStructures () {
        return step.getStructures();
        // return Collections.unmodifiableMap(step.getStructures());
    }

    /**
     * Returns the Operation list held by this Model. Should not be used to add or removed
     * operations.
     *
     * @return The Operation list held by this Model.
     */
    public List<Operation> getOperations () {
        return atomicOperations;
        // return Collections.unmodifiableList(atomicOperations);
    }

    /**
     * Set the Operation list used by this Model, and reset the index. The previous list
     * will be lost.
     *
     * @param newOperations
     *            The new list of operations to use.
     */
    public void setOperations (List<Operation> newOperations) {
        atomicOperations.clear();
        atomicOperations.addAll(newOperations);
        index = 0;
    }

    /**
     * Returns true if this model has been hard cleared, or if it has not been changed
     * since the the constructor was called.
     *
     * @return True if this Model is in its initial state.
     */
    public boolean isCleared () {
        inInitialState = step.getStructures().isEmpty() && atomicOperations.isEmpty() && mixedOperations.isEmpty();
        return inInitialState;
    }
}
