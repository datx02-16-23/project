package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import assets.Debug;
import contract.datastructure.DataStructure;
import contract.json.Locator;
import contract.json.Operation;
import contract.operation.Key;
import contract.operation.OP_Message;
import contract.operation.OperationType;
import contract.utility.OpUtil;
import gui.Main;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * 
 * @author Richard Sundqvist
 *
 */
public class ExecutionModel {

    /**
     * The default model instance.
     */
    public static final ExecutionModel                 INSTANCE = new ExecutionModel("INSTANCE");

    // ============================================================= //
    /*
     *
     * Field variables
     *
     */
    // ============================================================= //

    /**
     * The map of data structures in this model.
     */
    private final ObservableMap<String, DataStructure> dataStructures;

    /**
     * List of low level operations. <br>
     * <b>Atomic operations:</br>
     * {@link OperationType#read}<br>
     * {@link OperationType#write}<br>
     * {@link OperationType#message}<br>
     */
    // TODO Atomic Operations Execution.
    private final ObservableList<Operation>            atomicOperations;

    /**
     * List of operations which may include height level, non-atomic operations. <br>
     * <b>Atomic operations:</br>
     * {@link OperationType#read}<br>
     * {@link OperationType#write}<br>
     * {@link OperationType#message}<br>
     */
    private final ObservableList<Operation>            operations;

    /**
     * Current operation index for the atomic operations execution list.
     */
    private int                                        atomicIndex;

    /**
     * Current operation index.
     */
    private int                                        index;

    /**
     * Indicates whether parallel execution is permitted.
     */
    private boolean                                    parallelExecution;

    /**
     * The name of the model.
     */
    public final String                                name;

    /**
     * A list of the most recently executed operations.
     */
    private final ObservableList<Operation>            executedOperations;

    /**
     * The operations executed listener for the model.
     */
    private final List<OperationsExecutedListener>     operationsExecutedListeners;

    // ============================================================= //
    /*
     *
     * Constructors
     *
     */
    // ============================================================= //

    /**
     * Create a new ExecutionModel.
     * 
     * @param name
     *            The name of the model.
     * @param parallelExecution
     *            If {@code true}, the model may execute several operations per step.
     */
    public ExecutionModel (String name, boolean parallelExecution) {
        this.name = name;

        dataStructures = FXCollections.observableHashMap();
        atomicOperations = FXCollections.observableArrayList();
        operations = FXCollections.observableArrayList();
        executedOperations = FXCollections.observableArrayList();
        operationsExecutedListeners = new ArrayList<OperationsExecutedListener>();

        setParallelExecution(parallelExecution);
        setIndex(-1);
        setAtomicIndex(-1);
    }

    /**
     * Create a new ExecutionModel. {@code parallelExecution} will be set to {@code true}
     * .
     * 
     * @param name
     *            The name of the model.
     */
    public ExecutionModel (String name) {
        this(name, true);
    }

    /**
     * Create a new ExecutionModel with a random name.
     * 
     * @param parallelExecution
     *            If {@code true}, the model may execute several operations per step.
     */
    public ExecutionModel (boolean parallelExecution) {
        this(Math.random() * Integer.MAX_VALUE + "", parallelExecution);
    }

    /**
     * Create a new ExecutionModel with a random name. {@code parallelExecution} will be
     * set to {@code true}
     */
    public ExecutionModel () {
        this(Math.random() * Integer.MAX_VALUE + "");
    }

    // ============================================================= //
    /*
     *
     * Control
     *
     */
    // ============================================================= //

    /**
     * Execute the next operation, if possible.
     * 
     * @return A list containing the executed operations.
     */
    public ObservableList<Operation> executeNext () {
        executedOperations.clear();

        if (parallelExecution) {
            executeParallel();
        } else {
            executeLinear();
        }

        notifyExecutedOperationsListeners();

        return executedOperations;
    }

    /**
     * Execute the previous operation, if possible.
     * 
     * @return A list containing the executed operations.
     */
    public ObservableList<Operation> executePrevious () {

        if (tryExecutePrevious()) {
            int previousIndex = index - 2;
            reset();
            execute(previousIndex);
        }

        notifyExecutedOperationsListeners();
        return executedOperations;
    }

    private void notifyExecutedOperationsListeners () {
        for (OperationsExecutedListener opl : operationsExecutedListeners) {
            opl.operationsExecuted(executedOperations);
        }
    }

    /**
     * Test to see if it is possible to execute the previous operation(s) in in the queue.
     * 
     * @return {@code true} if the model can execute backwards, {@code false} otherwise.
     */
    public boolean tryExecutePrevious () {
        boolean tryExecutePrevious = index > 1 && index < operations.size() + 1;
        executePreviousProperty.set(tryExecutePrevious);
        return tryExecutePrevious;
    }

    /**
     * Test to see if it is possible to execute the next operation(s) in in the queue.
     * 
     * @return {@code true} if the model can execute forward, {@code false} otherwise.
     */
    public boolean tryExecuteNext () {
        boolean tryExecuteNext = index >= 0 && index + 1 < operations.size() && !operations.isEmpty();
        executeNextProperty.set(tryExecuteNext);
        tryExecutePrevious(); // Update backwards property.
        return tryExecuteNext;
    }

    /**
     * Execute the operation(s) up to and including the given index. If the index is lower
     * than the current index, the model will reset and play from the beginning. Will
     * execute to the end if {@code index} is greater than the number of operations in the
     * queue.
     * 
     * @param toIndex
     *            The index to execute at.
     * @return A list containing the executed operations.
     */
    public ObservableList<Operation> execute (int toIndex) {
        executedOperations.clear();

        if (toIndex < 0) {
            toIndex = 0;
        }
        if (index == toIndex) {
            return executedOperations;
        }
        if (index < toIndex) {
            reset();
        }

        int targetIndex = toIndex < operations.size() ? toIndex : operations.size() - 1;
        System.out.println("targetIndex = " + targetIndex);
        System.out.println("toIndex = " + toIndex);

        for (int i = 0; i <= targetIndex; i++) {
            if (tryExecuteNext()) {
                execute();
            } else {
                System.out.println("stopped at: " + i);
                break;
            }
        }

        return executedOperations;
    }

    /**
     * Reset the model.
     */
    public void reset () {
        dataStructures.values().forEach(dataStructure -> {
            dataStructure.clear();
        });
        atomicIndex = 0;
        index = 0;
        updateProperties();
    }

    /**
     * Clear the model.
     */
    public void clear () {
        dataStructures.clear();
        operations.clear();
        atomicOperations.clear();

        index = -1;
        atomicIndex = -1;
        updateProperties();
    }

    // ============================================================= //
    /*
     *
     * Model Progression
     *
     */
    // ============================================================= //

    /**
     * Execute the
     */
    private void executeParallel () {
        // TODO: Implement parallel execution.
        executeLinear();
    }

    /**
     * Execute the next operation.
     */
    private void executeLinear () {
        execute();
    }

    /**
     * Execute the next operation in the queue and add it to {@link executedOperations}.
     */
    private void execute () {
        setIndex(index + 1);

        if (index > 0 && index < operations.size()) {
            Operation op = operations.get(index);
            setAtomicIndex(atomicIndex + op.operation.numAtomicOperations);
            executedOperations.add(op);
            execute(op);
        }
    }

    /**
     * Execute an operation.
     *
     * @param op
     *            The operation to execute.
     */
    private void execute (Operation op) {
        switch (op.operation) {

        case message:
            // ============================================================= //
            /*
             * Message
             */
            // ============================================================= //
            // TODO: Callback mechanism.
            Main.console.info("MESSAGE: " + ((OP_Message) op).getMessage());
            break;
        case read:
        case write:
            // ============================================================= //
            /*
             * Read and Write
             */
            // ============================================================= //
            Locator source = OpUtil.getLocator(op, Key.source);
            if (source != null) {
                DataStructure sourceStruct = dataStructures.get(source.identifier);
                if (sourceStruct != null) {
                    sourceStruct.applyOperation(op);
                }
            }

            Locator target = OpUtil.getLocator(op, Key.target);
            if (target != null) {
                DataStructure targetStruct = dataStructures.get(target.identifier);
                if (targetStruct != null) {
                    targetStruct.applyOperation(op);
                }
            }
            break;
        case swap:
            // ============================================================= //
            /*
             * Swap
             */
            // ============================================================= //
            Locator var1 = OpUtil.getLocator(op, Key.var1);
            dataStructures.get(var1.identifier).applyOperation(op);

            Locator var2 = OpUtil.getLocator(op, Key.var2);
            dataStructures.get(var2.identifier).applyOperation(op);
            break;
        case remove:
            // ============================================================= //
            /*
             * TODO Fix after renaming remove.
             */
            // ============================================================= //
            Locator removeTarget = OpUtil.getLocator(op, Key.target);
            DataStructure targetStruct = dataStructures.get(removeTarget.identifier);
            if (targetStruct != null) {
                targetStruct.applyOperation(op);
            }
            break;
        default:
            System.err.print("Unknown operation type: \"" + op.operation + "\"");
            break;
        }

        if (Debug.OUT) {
            System.out.println("ExecutionModel: execute(): " + op);
        }
    }

    // ============================================================= //
    /*
     *
     * Setters and Getters
     *
     */
    // ============================================================= //

    /**
     * Set the data structures, operations, and atomic operations for this model. Will
     * keep the current collection if the corresponding argument is {@code null}. Always
     * calls {@link #updateProperties()}.
     * 
     * @param dataStructures
     *            A map of data structures.
     * @param operations
     *            A list of operations.
     * @param atomicOperations
     *            A list of atomic operations.
     */
    public void set (Map<String, DataStructure> dataStructures, List<Operation> operations,
            ObservableList<Operation> atomicOperations) {

        if (dataStructures != null) {
            setDataStructures(dataStructures);
        }
        if (operations != null) {
            setOperations(operations);
        }
        if (atomicOperations != null) {
            setAtomicOperations(atomicOperations);
        }

    }

    /**
     * Set the data structures for this model.
     * 
     * @param dataStructures
     *            A map of data structures.
     */
    public void setDataStructures (Map<String, DataStructure> dataStructures) {
        if (dataStructures != null) {
            this.dataStructures.clear();
            this.dataStructures.putAll(dataStructures);
            updateProperties();
        }
    }

    /**
     * Set the operations for this model.
     * 
     * @param operations
     *            A list of operations.
     */
    public void setOperations (List<Operation> operations) {
        if (this.operations != null) {
            this.operations.clear();
            this.operations.addAll(operations);
            updateProperties();
        }
    }

    /**
     * Set the atomic operations for this model.
     * 
     * @param atomicOperations
     *            A list of atomic operations.
     * @throws IllegalArgumentException
     *             If the list contained non-atomic operations.
     */
    public void setAtomicOperations (List<Operation> atomicOperations) {
        if (atomicOperations != null) {

            // Search for forbidden operation types.
            for (Operation op : atomicOperations) {

                if (!OpUtil.isAtomic(op)) {
                    int index = atomicOperations.indexOf(op);
                    throw new IllegalArgumentException("Non-atomic operation: " + op + " at index: " + index);
                }
            }

            this.atomicOperations.clear();
            this.atomicOperations.addAll(atomicOperations);
            updateProperties();
        }
    }

    /**
     * Returns the list of atomic operations in use by this model.
     * 
     * @return A list of atomic operations.
     */
    public ObservableList<Operation> getAtomicOperations () {
        return atomicOperations;
    }

    /**
     * Returns the list of operations in use by this model.
     * 
     * @return A list of operations.
     */
    public ObservableList<Operation> getOperations () {
        return operations;
    }

    /**
     * Returns the map of data structures in use by this model.
     * 
     * @return A map of data structures.
     */
    public Map<String, DataStructure> getDataStructures () {
        return dataStructures;
    }

    /**
     * Returns the parallel execution setting of this model.
     * 
     * @return {@code true} if parallel execution is enabled, false otherwise.
     */
    public boolean isParallelExecution () {
        return parallelExecution;
    }

    /**
     * Set the parallel execution setting of this model.
     * 
     * @param parallelExecution
     *            The new parallel execution setting.
     */
    public void setParallelExecution (boolean parallelExecution) {
        if (this.parallelExecution != parallelExecution) {

            this.parallelExecution = parallelExecution;
            parallelExecutionProperty.set(parallelExecution);
        }
    }

    /**
     * Returns {@code atomicIndex} for this model.
     * 
     * @return The {@code atomicIndex} for this model.
     */
    public int getAtomicIndex () {
        return atomicIndex;
    }

    /**
     * Get the current execution index. Return value may vary from the value returned by
     * {@link getAtomicIndex()} if {@code parallelExecution} is set to {@code true}.
     * 
     * @return The current execution index.
     */
    public int getIndex () {
        return index;
    }

    private void setIndex (int index) {
        if (operations.isEmpty()) {
            index = -1;
        } else if (index > operations.size()) {
            index = operations.size();
        } else if (index < 0) {
            index = 0;
        }
        indexPropery.set(index);
        this.index = index;
    }

    private void setAtomicIndex (int atomicIndex) {
        if (atomicIndex < 0) {
            atomicIndex = 0;
        } else if (atomicIndex > atomicOperations.size()) {
            atomicIndex = atomicOperations.size();
        }
        atomicIndexProperty.set(atomicIndex);
        this.atomicIndex = atomicIndex;
    }

    /**
     * Add a listener to be called each time operation(s) are executed.
     * 
     * @param operationsExecutedListener
     *            A {@code OperationsExecutedListener}.
     */
    public void addOperationsExecutedListener (OperationsExecutedListener operationsExecutedListener) {
        if (Debug.ERR) {
            System.err.println("operationsExecutedListener added: " + operationsExecutedListener);
        }
        operationsExecutedListeners.add(operationsExecutedListener);
    }

    // ============================================================= //
    /*
     *
     * Properties / Getters and Setters
     *
     */
    // ============================================================= //

    private final ReadOnlyBooleanWrapper parallelExecutionProperty = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper executeNextProperty       = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyBooleanWrapper executePreviousProperty   = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyBooleanWrapper clearProperty             = new ReadOnlyBooleanWrapper(true);

    private final ReadOnlyIntegerWrapper indexPropery              = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper atomicIndexProperty       = new ReadOnlyIntegerWrapper();

    /**
     * Force updating of all properties;
     */
    public void updateProperties () {
        isClear();
        tryExecuteNext();
        tryExecutePrevious();
        setIndex(index);
        setAtomicIndex(atomicIndex);
    }

    /**
     * Returns {@code true} if the model is clear, {@code false} otherwise.
     * 
     * @return {@code true} if the model is clear, {@code false} otherwise.
     */
    private boolean isClear () {
        boolean isClear = dataStructures.isEmpty() && operations.isEmpty() && atomicOperations.isEmpty();
        clearProperty.set(isClear);
        return isClear;
    }

    /**
     * Returns a property indicating whether this model is cleared.
     * 
     * @return A ReadOnlyBooleanProperty.
     */
    public ReadOnlyBooleanProperty clearProperty () {
        return clearProperty.getReadOnlyProperty();
    }

    /**
     * Returns a property indicating whether this model is in parallel execution mode.
     * 
     * @return A ReadOnlyBooleanProperty.
     */
    public ReadOnlyBooleanProperty parallelExecutionProperty () {
        return parallelExecutionProperty.getReadOnlyProperty();
    }

    /**
     * Returns a property indicating whether this model is able to execute forwards from
     * the current index.
     * 
     * @return A ReadOnlyBooleanProperty.
     */
    public ReadOnlyBooleanProperty executeNextProperty () {
        return executeNextProperty.getReadOnlyProperty();
    }

    /**
     * Returns a property indicating whether this model is able to execute backwards from
     * the current index.
     * 
     * @return A ReadOnlyBooleanProperty.
     */
    public ReadOnlyBooleanProperty executePreviousProperty () {
        return executePreviousProperty.getReadOnlyProperty();
    }

    /**
     * Returns a property indicating which index this model is currently at.
     * 
     * @return A ReadOnlyIntegerProperty.
     */
    public ReadOnlyIntegerProperty indexProperty () {
        return indexPropery.getReadOnlyProperty();
    }

    /**
     * Returns a property indicating which index this model is currently at in the atomic
     * operations list.
     * 
     * @return A ReadOnlyIntegerProperty.
     */
    public ReadOnlyIntegerProperty atomicIndexProperty () {
        return atomicIndexProperty.getReadOnlyProperty();
    }
}
