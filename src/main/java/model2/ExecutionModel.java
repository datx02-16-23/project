package model2;

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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import model.Model;

/**
 * 
 * @author Richard Sundqvist
 *
 */
public class ExecutionModel {

    /**
     * The default model instance.
     */
    public static final Model          INSTANCE                  = new Model("INSTANCE");

    // ============================================================= //
    /*
     *
     * Properties
     *
     */
    // ============================================================= //

    private final BooleanProperty      parallelExecutionProperty = new SimpleBooleanProperty();
    private final BooleanProperty      executeNextProperty       = new SimpleBooleanProperty(false);
    private final BooleanProperty      executePreviousProperty   = new SimpleBooleanProperty(false);
    private final BooleanProperty      clearProperty             = new SimpleBooleanProperty(false);

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
    private Map<String, DataStructure> dataStructures;

    /**
     * List of low level operations. <br>
     * <b>Atomic operations:</br>
     * {@link OperationType#read}<br>
     * {@link OperationType#write}<br>
     * {@link OperationType#message}<br>
     */
    private List<Operation>            atomicOperations;

    /**
     * Current operation index for the atomic operations execution list.
     */
    private int                        atomicIndex;

    /**
     * List of operations which may include height level, non-atomic operations. <br>
     * <b>Atomic operations:</br>
     * {@link OperationType#read}<br>
     * {@link OperationType#write}<br>
     * {@link OperationType#message}<br>
     */
    private List<Operation>            operations;

    /**
     * Current operation index.
     */
    private int                        index;

    /**
     * Indicates whether parallel execution is permitted.
     */
    private boolean                    parallelExecution         = false;

    /**
     * The name of the model.
     */
    public final String                name;

    /**
     * A list of the most recently executed operations.
     */
    private List<Operation>            executedOperations;

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

        setparallelExecution(parallelExecution);
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
     * @return A list of the operations which were executed.
     */
    public List<Operation> executeNext () {

        Operation op;

        if (parallelExecution) {
            // TODO parallel execution.
            // executeParallel();
            executeLinear();
        } else {
            executeLinear();
        }

        return executedOperations;
    }

    /**
     * Execute the previous operation, if possible.
     */
    public void executePrevious () {

    }

    /**
     * Execute the operation(s) at the given index. If the index is lower than the current
     * index, the model will reset and play fromt he beginning.
     * 
     * @param index
     *            The index to execute at.
     * @return A list of the executed operations.
     */
    public List<Operation> execute (int index) {
        ArrayList<Operation> executedOperations = new ArrayList<Operation>();

        if (this.index == index) {
            // Do nothing.

        } else if (this.index < index) {
            reset();
            // TODO

        } else if (this.index > index) {
            // TODO

        }

        return executedOperations;
    }

    /**
     * Reset the model.
     */
    public void reset () {
        if (dataStructures != null) {
            dataStructures.values().forEach(dataStructure -> {
                dataStructure.clear();
            });
        }
        setIndex(0);
        setAtomicIndex(0);
    }

    /**
     * Clear the model.
     */
    public void clear () {
        dataStructures = null;
        operations = null;
        atomicOperations = null;

        setIndex(-1);
        setAtomicIndex(-1);
    }

    // ============================================================= //
    /*
     *
     * Model Execution
     *
     */
    // ============================================================= //

    private void executeParallel () {

    }

    private void executeLinear () {

    }

    /**
     * Execute an operation to drive the model forward.
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
             * TODO
             */
            // ============================================================= //
            Locator removeTarget = OpUtil.getLocator(op, Key.target);
            DataStructure targetStruct = dataStructures.get(removeTarget.identifier);
            if (targetStruct != null) {
                targetStruct.applyOperation(op);
            }
            break;
        default:
            Main.console.err("Unknown operation type: \"" + op.operation + "\"");
            break;
        }

        if (Debug.OUT) {
            System.out.print("Step.applyOperation(): " + op + "\n");
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
     * keep the current collection if the corresponding argument is {@code null}.
     * 
     * @param dataStructures
     *            A map of data structures.
     * @param operations
     *            A list of operations.
     * @param atomicOperations
     *            A list of atomic operations.
     */
    public void set (Map<String, DataStructure> dataStructures, List<Operation> operations,
            List<Operation> atomicOperations) {

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
        this.dataStructures = dataStructures;
    }

    /**
     * Set the operations for this model.
     * 
     * @param operations
     *            A list of operations.
     */
    public void setOperations (List<Operation> operations) {
        this.operations = operations;
    }

    /**
     * Set the atomic operations for this model.
     * 
     * @param atomicOperations
     *            A list of atomic operations.
     */
    public void setAtomicOperations (List<Operation> atomicOperations) {
        for (Operation op : atomicOperations) {

            if (!OpUtil.isAtomic(op)) {
                int index = atomicOperations.indexOf(op);
                throw new IllegalArgumentException("Non-atomic operation: " + op + " at index: " + index);
            }
        }

        this.atomicOperations = atomicOperations;
    }

    /**
     * Get the parallel execution setting of this model.
     * 
     * @return {@code true} if parallel execution is enabled, false otherwise.
     */
    public boolean isparallelExecution () {
        return parallelExecution;
    }

    /**
     * Set the parallel execution setting of this model.
     * 
     * @param parallelExecution
     *            The new parallel execution setting.
     */
    public void setparallelExecution (boolean parallelExecution) {
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
        this.index = index;
    }

    private void setAtomicIndex (int atomicIndex) {
        this.atomicIndex = atomicIndex;
    }
}
