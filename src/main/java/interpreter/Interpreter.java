package interpreter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import contract.Operation;
import contract.operation.*;
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
	public static final int ABORT = 0;
	/**
	 * Add any high-level operation found to processedOperations, then continue
	 * on the current working set.
	 */
	public static final int KEEP_SET_ADD_HIGH = 1;
	/**
	 * Flush the working set into processedOperations, then add the high-level
	 * operation as well.
	 */
	public static final int FLUSH_SET_ADD_HIGH = 2;
	/**
	 * Discard high-level operations as they are found.
	 */
	public static final int DISCARD = 3;
	/**
	 * Deconstruct high-level operations into read/write operations.
	 */
	public static final int DECONSTRUCT = 4;
	private int count;
	private int highOrderRoutine;
	private final LinkedList<Operation> before;
	private LinkedList<Operation> after;
	private LinkedList<OP_ReadWrite> workingSet;
	private Consolidator consolidator;

	/**
	 * Create a new Interpreter with the high order routine set to
	 * FLUSH_SET_ADD_HIGH. Use the setOperations() and
	 * getConsolidatedOperations() methods to interpret lists of operations.
	 */
	public Interpreter() {
		before = new LinkedList<Operation>();
		after = new LinkedList<Operation>();
		workingSet = new LinkedList<OP_ReadWrite>();
		highOrderRoutine = FLUSH_SET_ADD_HIGH;
		consolidator = new Consolidator();
	}

	/**
	 * Returns the high order routine currently in use. See static declarations
	 * of this class for possible routines.
	 * 
	 * @return The high order routine currently in use.
	 */
	public int getHighOrderRoutine() {
		return highOrderRoutine;
	}

	/**
	 * Set the routine for handling high level operation if they are found in
	 * the operations list when interpreting.
	 * 
	 * @param newRoutine
	 *            The new routine.
	 */
	public void setHighOrderRoutine(int newRoutine) {
		if (newRoutine < 0 || newRoutine > 4) {
			Main.console.err("INTERPRETER: setHighOrderRoutine(): Invalid high-order routine.");
			return;
		}
		highOrderRoutine = newRoutine;
	}

	/**
	 * Add a test case to the Interpreter.
	 * 
	 * @param testCase
	 *            The test case to add.
	 */
	public void addTestCase(OperationType testCase) {
		switch (testCase) {
		case swap:
			consolidator.addConsolidable(new OP_Swap());
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
	public void removeTestCase(OperationType testCase) {
		switch (testCase) {
		case swap:
			consolidator.removeTestCase(testCase, new OP_Swap().getRWcount());
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
	public List<Operation> consolidateSafe(List<Operation> operationsToConsolidate) {
		ArrayList<Operation> result = new ArrayList<Operation>(operationsToConsolidate);
		consolidate(result);
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
	public int consolidate(List<Operation> listToConsolidate) {
		count = 0;
		if (highOrderRoutine == ABORT) {
			for (Operation op : listToConsolidate) {
				if (op.operation == OperationType.message) {
					continue; // Acceptable non read/write operation found.
				} else if (isReadOrWrite(op) == false) {
					Main.console.info("ABORT: High level operation found: " + op);
					return count;
				}
			}
		}
		// Clean up data from previous executions
		before.clear();
		after.clear();
		workingSet.clear();
		// Set before list and begin
		before.addAll(listToConsolidate);
		consolidate();
		// Transfer result to operations
		listToConsolidate.clear();
		listToConsolidate.addAll(after);
		return count;
	}

	/**
	 * Build and filter working sets until all operations in {@code before} have
	 * been processed. When this method returns, {@code before.size()} will be
	 * 0.
	 */
	private void consolidate() {
		int minWorkingSetSize = consolidator.getMinimumSetSize();
		int maxWorkingSetSize = consolidator.getMaximumSetSize();
		if (minWorkingSetSize < 0 || maxWorkingSetSize < 0) {
			after.addAll(before);
			return; // No operations in Consolidator.
		}
		// Continue until all operations are handled
		outer: while (before.isEmpty() == false || workingSet.isEmpty() == false) {
			while (workingSet.size() < minWorkingSetSize) {
				if (tryExpandWorkingSet() == false) {
					break outer;
				}
			}
			// Expand working set and attempt consolidation.
			while (workingSet.size() <= maxWorkingSetSize) {
				if (attemptConsolidateWorkingSet() == true) { // Attempt to
																// consolidate
																// the set.
					workingSet.clear();
					continue outer; // Working set converted to a more complex
									// operation. Begin work on new set.
				}
				if (tryExpandWorkingSet() == false) {
					break outer;
				}
			}
			// Add the first operation of working set to consolidated
			// operations.
			after.add(workingSet.removeFirst());
			// Reduce the working set.
			while (workingSet.size() > minWorkingSetSize) {
				reduceWorkingSet();
			}
		}
		after.addAll(workingSet);
	}

	/**
	 * Reduce the size of the working set by removing the last element and
	 * adding it first to the list of low level operations.
	 */
	private void reduceWorkingSet() {
		// Add the last element of working set to the first position in low
		// level operations.
		before.addFirst(workingSet.removeLast());
	}

	/**
	 * Try to expend the current working set. Messages are immediately added to
	 * high level operations, as are initialization.
	 * 
	 * @return False if the working set could not be expanded.
	 */
	private Operation candidate;

	private boolean tryExpandWorkingSet() {
		if (before.isEmpty()) {
			return false;
		}
		candidate = before.remove(0);
		if (candidate.operation == OperationType.message) {
			keepSet_addCandidate();
			return tryExpandWorkingSet();
		} else if (candidate.operation == OperationType.write) {
			OP_Write write_candidate = (OP_Write) candidate;
			if (write_candidate.getValue().length > 1) {
				flushSet_addCandidate();
				return tryExpandWorkingSet();
			}
		}
		if (isReadOrWrite(candidate) == false) {
			handleHighLevelOperation();
			return tryExpandWorkingSet();
		}
		// Add the read/write operation to the working set.
		workingSet.add((OP_ReadWrite) candidate);
		return true;
	}

	private void handleHighLevelOperation() {
		switch (highOrderRoutine) {
		case KEEP_SET_ADD_HIGH:
			keepSet_addCandidate();
			break;
		case FLUSH_SET_ADD_HIGH:
			flushSet_addCandidate();
			break;
		case DISCARD:
			tryExpandWorkingSet();
			break;
		case DECONSTRUCT:
			deconstruct();
			break;
		}
	}

	/**
	 * Deconstruct operation into read/write operations.
	 */
	private void deconstruct() {
		Main.console.err("DECONSTRUCT has not been implemented yet. Sorry :/.");
	}

	/**
	 * Add high-level operation found to processedOperations, then continue on
	 * the current working set.
	 */
	private void keepSet_addCandidate() {
		after.add(candidate);
	}

	/**
	 * Flush the working set into processedOperations, then add the high-level
	 * operation as well.
	 */
	private void flushSet_addCandidate() {
		after.addAll(workingSet);
		after.add(candidate);
		workingSet.clear();
	}

	/**
	 * Returns true if the operation is a read or write operation, thus being
	 * capable of inheriting OP_ReadWrite.
	 * 
	 * @param op
	 *            The operation to test.
	 * @return True if the operation is a read/write operation. False otherwise.
	 */
	private boolean isReadOrWrite(Operation op) {
		return (op.operation == OperationType.read || op.operation == OperationType.write);
	}

	/**
	 * Attempt to consolidate the working set held by this Interpreter. Will
	 * return true and add the new operation to processedOperations if
	 * successful. Will not clear the working set.
	 * 
	 * @return True if workingSet was successfully consolidated, false
	 *         otherwise.
	 */
	private boolean attemptConsolidateWorkingSet() {
		Operation consolidatedOperation = consolidator.attemptConsolidate(workingSet);
		if (consolidatedOperation != null) {
			after.add(consolidatedOperation);
			count++;
			return true;
		}
		return false;
	}

	/**
	 * Returns a list of all active test cases for this Consolidator.
	 * 
	 * @return A list of all active test cases for this Consolidator.
	 */
	public List<OperationType> getTestCases() {
		return consolidator.getTestCases();
	}

	/**
	 * Returns the Consolidator used by the interpreter.
	 * 
	 * @return The Consolidator used by the interpreter.
	 */
	public Consolidator getConsolidator() {
		return consolidator;
	}

	/**
	 * Set the Consolidator used by this interpreter.
	 * 
	 * @param newConsolidator
	 *            A new Consolidator to use.
	 */
	public void setConsolidator(Consolidator newConsolidator) {
		consolidator = newConsolidator;
	}
}
