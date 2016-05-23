package interpreter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import contract.Operation;
import contract.operation.OP_ReadWrite;
import contract.operation.OP_Swap;
import contract.operation.OP_Write;
import contract.operation.OperationType;
import gui.Main;

/**
 * An Interpreter attempts to increase the abstraction level of operation logs
 * be consolidating read and write operations into higher level operations.
 *
 * @author Richard Sundqvist
 *
 */
public class Interpreter {

    /**
     * Halt execution if a high-level operation is found.
     */
    public static final int                ABORT              = 0;
    /**
     * Add any high-level operation found to processedOperations, then continue
     * on the current working set.
     */
    public static final int                KEEP_SET_ADD_HIGH  = 1;
    /**
     * Flush the working set into processedOperations, then add the high-level
     * operation as well.
     */
    public static final int                FLUSH_SET_ADD_HIGH = 2;
    /**
     * Discard high-level operations as they are found.
     */
    public static final int                DISCARD            = 3;
    /**
     * Deconstruct high-level operations into read/write operations.
     */
    public static final int                DECONSTRUCT        = 4;
    private int                            count;
    private int                            highOrderRoutine;
    private final LinkedList<Operation>    before;
    private final LinkedList<Operation>    after;
    private final LinkedList<OP_ReadWrite> workingSet;
    private Consolidator                   consolidator;

    /**
     * Create a new Interpreter with the high order routine set to
     * FLUSH_SET_ADD_HIGH. Use the setOperations() and
     * getConsolidatedOperations() methods to interpret lists of operations.
     */
    public Interpreter () {
        this.before = new LinkedList<Operation>();
        this.after = new LinkedList<Operation>();
        this.workingSet = new LinkedList<OP_ReadWrite>();
        this.highOrderRoutine = FLUSH_SET_ADD_HIGH;
        this.consolidator = new Consolidator();
    }

    /**
     * Returns the high order routine currently in use. See static declarations
     * of this class for possible routines.
     * 
     * @return The high order routine currently in use.
     */
    public int getHighOrderRoutine () {
        return this.highOrderRoutine;
    }

    /**
     * Set the routine for handling high level operation if they are found in
     * the operations list when interpreting.
     * 
     * @param newRoutine
     *            The new routine.
     */
    public void setHighOrderRoutine (int newRoutine) {
        if (newRoutine < 0 || newRoutine > 4) {
            Main.console.err("INTERPRETER: setHighOrderRoutine(): Invalid high-order routine.");
            return;
        }
        this.highOrderRoutine = newRoutine;
    }

    /**
     * Add a test case to the Interpreter.
     * 
     * @param testCase
     *            The test case to add.
     */
    public void addTestCase (OperationType testCase) {
        switch (testCase) {
        case swap:
            this.consolidator.addConsolidable(new OP_Swap());
            break;
        default:
            Main.console.err("Cannot consolidate OperationType: " + testCase.toString().toUpperCase());
            break;
        }
    }

    /**
     * Remove a given testCase. When this method returns, the testcase is
     * guaranteed to be removed.
     * 
     * @param testCase
     *            The testcase to remove.
     */
    public void removeTestCase (OperationType testCase) {
        switch (testCase) {
        case swap:
            this.consolidator.removeTestCase(testCase, new OP_Swap().getRWcount());
            break;
        default:
            Main.console.err("Unknown Consolidable type: " + testCase);
            break;
        }
    }

    /**
     * Attempt to consolidate the supplied list of operations. Returns True if
     * the size of the list has changed as a result on the attempted
     * consolidation. The list provided as argument will not be changed.
     * 
     * @param operationsToConsolidate
     *            The operations to consolidate.
     * @return A consolidated list of operations.
     */
    public List<Operation> consolidateSafe (List<Operation> operationsToConsolidate) {
        ArrayList<Operation> result = new ArrayList<Operation>(operationsToConsolidate);
        this.consolidate(result);
        return result;
    }

    /**
     * Attempt to consolidate the supplied list of operations. Returns True if
     * the size of the list has changed as a result on the attempted
     * consolidation. <br>
     * <b>NOTE:</b> The list given by the argument {@code operations} will be
     * modified by this method!
     * 
     * @param listToConsolidate
     *            The List to consolidate.
     * @return The number operations creates.
     */
    public int consolidate (List<Operation> listToConsolidate) {
        this.count = 0;
        if (this.highOrderRoutine == ABORT) {
            for (Operation op : listToConsolidate) {
                if (op.operation == OperationType.message) {
                    continue; // Acceptable non read/write operation found.
                } else if (this.isReadOrWrite(op) == false) {
                    Main.console.info("ABORT: High level operation found: " + op);
                    return this.count;
                }
            }
        }
        // Clean up data from previous executions
        this.before.clear();
        this.after.clear();
        this.workingSet.clear();
        // Set before list and begin
        this.before.addAll(listToConsolidate);
        this.consolidate();
        // Transfer result to operations
        listToConsolidate.clear();
        listToConsolidate.addAll(this.after);
        return this.count;
    }

    /**
     * Build and filter working sets until all operations in {@code before} have
     * been processed. When this method returns, {@code before.size()} will be
     * 0.
     */
    private void consolidate () {
        int minWorkingSetSize = this.consolidator.getMinimumSetSize();
        int maxWorkingSetSize = this.consolidator.getMaximumSetSize();
        if (minWorkingSetSize < 0 || maxWorkingSetSize < 0) {
            this.after.addAll(this.before);
            return; // No operations in Consolidator.
        }
        // Continue until all operations are handled
        outer: while (this.before.isEmpty() == false || this.workingSet.isEmpty() == false) {
            while (this.workingSet.size() < minWorkingSetSize) {
                if (this.tryExpandWorkingSet() == false) {
                    break outer;
                }
            }
            // Expand working set and attempt consolidation.
            while (this.workingSet.size() <= maxWorkingSetSize) {
                if (this.attemptConsolidateWorkingSet() == true) { // Attempt to
                                                                   // consolidate
                                                                   // the set.
                    this.workingSet.clear();
                    continue outer; // Working set converted to a more complex
                                    // operation. Begin work on new set.
                }
                if (this.tryExpandWorkingSet() == false) {
                    break outer;
                }
            }
            // Add the first operation of working set to consolidated
            // operations.
            this.after.add(this.workingSet.removeFirst());
            // Reduce the working set.
            while (this.workingSet.size() > minWorkingSetSize) {
                this.reduceWorkingSet();
            }
        }
        this.after.addAll(this.workingSet);
    }

    /**
     * Reduce the size of the working set by removing the last element and
     * adding it first to the list of low level operations.
     */
    private void reduceWorkingSet () {
        // Add the last element of working set to the first position in low
        // level operations.
        this.before.addFirst(this.workingSet.removeLast());
    }

    /**
     * Try to expend the current working set. Messages are immediately added to
     * high level operations, as are initialization.
     * 
     * @return False if the working set could not be expanded.
     */
    private Operation candidate;

    private boolean tryExpandWorkingSet () {
        if (this.before.isEmpty()) {
            return false;
        }
        this.candidate = this.before.remove(0);
        if (this.candidate.operation == OperationType.message) {
            this.keepSet_addCandidate();
            return this.tryExpandWorkingSet();
        } else if (this.candidate.operation == OperationType.write) {
            OP_Write write_candidate = (OP_Write) this.candidate;
            if (write_candidate.getValue().length > 1) {
                this.flushSet_addCandidate();
                return this.tryExpandWorkingSet();
            }
        }
        if (this.isReadOrWrite(this.candidate) == false) {
            this.handleHighLevelOperation();
            return this.tryExpandWorkingSet();
        }
        // Add the read/write operation to the working set.
        this.workingSet.add((OP_ReadWrite) this.candidate);
        return true;
    }

    private void handleHighLevelOperation () {
        switch (this.highOrderRoutine) {
        case KEEP_SET_ADD_HIGH:
            this.keepSet_addCandidate();
            break;
        case FLUSH_SET_ADD_HIGH:
            this.flushSet_addCandidate();
            break;
        case DISCARD:
            this.tryExpandWorkingSet();
            break;
        case DECONSTRUCT:
            this.deconstruct();
            break;
        }
    }

    /**
     * Deconstruct operation into read/write operations.
     */
    private void deconstruct () {
        Main.console.err("DECONSTRUCT has not been implemented yet. Sorry :/.");
    }

    /**
     * Add high-level operation found to processedOperations, then continue on
     * the current working set.
     */
    private void keepSet_addCandidate () {
        this.after.add(this.candidate);
    }

    /**
     * Flush the working set into processedOperations, then add the high-level
     * operation as well.
     */
    private void flushSet_addCandidate () {
        this.after.addAll(this.workingSet);
        this.after.add(this.candidate);
        this.workingSet.clear();
    }

    /**
     * Returns true if the operation is a read or write operation, thus being
     * capable of inheriting OP_ReadWrite.
     * 
     * @param op
     *            The operation to test.
     * @return True if the operation is a read/write operation. False otherwise.
     */
    private boolean isReadOrWrite (Operation op) {
        return op.operation == OperationType.read || op.operation == OperationType.write;
    }

    /**
     * Attempt to consolidate the working set held by this Interpreter. Will
     * return true and add the new operation to processedOperations if
     * successful. Will not clear the working set.
     * 
     * @return True if workingSet was successfully consolidated, false
     *         otherwise.
     */
    private boolean attemptConsolidateWorkingSet () {
        Operation consolidatedOperation = this.consolidator.attemptConsolidate(this.workingSet);
        if (consolidatedOperation != null) {
            this.after.add(consolidatedOperation);
            this.count++;
            return true;
        }
        return false;
    }

    /**
     * Returns a list of all active test cases for this Consolidator.
     * 
     * @return A list of all active test cases for this Consolidator.
     */
    public List<OperationType> getTestCases () {
        return this.consolidator.getTestCases();
    }

    /**
     * Returns the Consolidator used by the interpreter.
     * 
     * @return The Consolidator used by the interpreter.
     */
    public Consolidator getConsolidator () {
        return this.consolidator;
    }

    /**
     * Set the Consolidator used by this interpreter.
     * 
     * @param newConsolidator
     *            A new Consolidator to use.
     */
    public void setConsolidator (Consolidator newConsolidator) {
        this.consolidator = newConsolidator;
    }
}
