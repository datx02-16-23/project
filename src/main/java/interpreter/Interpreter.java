package interpreter;

import java.util.ArrayList;
import java.util.List;

import manager.operations.OP_ReadWrite;
import manager.operations.OperationType;
import wrapper.Operation;

/**
 * Class for consolidating groups of low level operations (read/write).
 * @author Richard
 *
 */
public class Interpreter {
	/**
	 * Halt execution if a high-level operation is found.
	 */
	public final int HALT = 0;
	/**
	 * Add any high-level operation found to processedOperations, then continue on the current working set.
	 */
	public final int KEEP_SET_ADD_HIGH = 1;
	/**
	 * Flush the working set into processedOperations, then add the high-level operation as well.
	 */
	public final int FLUSH_SET_ADD_HIGH = 2;
	/**
	 * Discard high-level operations as they are found.
	 */
	public final int DISCARD = 3;
	/**
	 * Deconstruct high-level operations into read/write operations.
	 */
	public final int DECONSTRUCT = 4;
	
	private int highOrderRoutine;
	
	private List<Operation> unprocessedOperations, processedOperations;
	private List<OP_ReadWrite> workingSet;
	private Consolidator consolidator;

	/**
	 * Create a new Interpreter with the high order routine set to FLUSH_SET_ADD_HIGH. Use the setOperations()
	 * and getConsolidatedOperations() methods to interpret lists of operations.
	 */
	public Interpreter(){
		unprocessedOperations = new ArrayList<Operation>();
		workingSet = new ArrayList<OP_ReadWrite>();
		processedOperations = new ArrayList<Operation>();
		highOrderRoutine = FLUSH_SET_ADD_HIGH;
		consolidator = new Consolidator();
		//Different list types? Array lists may become a performance issue in the future.
	} 

	public int getHighOrderRoutine(){
		return highOrderRoutine;
	}
	/**
	 * Set the routine for handling high level operation if they are found in the operations list when interpreting.
	 * @param newRoutine The new routine.
	 */
	public void setHighOrderRoutine(int newRoutine){
		if (newRoutine < 0 || newRoutine > 4){
			System.err.println("INTERPRETER: setHighOrderRoutine(): Invalid high-order routine.");
			return;
		}
		highOrderRoutine = newRoutine;
	}

	/**
	 * Attempt to consolidate the supplied list of operations. Returns True if the size of the list has changed
	 * as a result on the attempted consolidation.
	 * <br><b>NOTE:</b> The list given by the argument {@code operations} will be modified by this method!
	 * @param operations The operations to consolidate.
	 * @return True if the list has been changed. False otherwise.
	 */
	public boolean consolidate(List<Operation> operations){
		int oldSize = operations.size();
		
		unprocessedOperations.clear(); //Not really needed.
		unprocessedOperations.addAll(operations);
		
		processedOperations.clear();
		consolidateOperations();
		
		operations.clear();
		operations.addAll(processedOperations);
		
		return oldSize == operations.size();
	}
	
	/**
	 * Build and evaluate working sets until all operations in lowLevelOperations have been processed.
	 * When this method returns, lowLevelOperations.size() will be 0.
	 */
	private void consolidateOperations(){
		int minWorkingSetSize = consolidator.getMinimumSetSize();
		int maxWorkingSetSize = consolidator.getMaximumSetSize();
		
		//Continue until all operations are handled
		outer: while(unprocessedOperations.isEmpty() == false || workingSet.isEmpty() == false){
			while(workingSet.size() < minWorkingSetSize){
				if (tryExpandWorkingSet() == false){
					break outer;
				}
			}
			
			//Expand working set and attempt consolidation.
			while (workingSet.size() <= maxWorkingSetSize) {
				if(attemptConsolidateWorkingSet() == true){ //Attempt to consolidate the set.
					workingSet.clear();
					continue outer; //Working set converted to a more complex operation. Begin work on new set.
				}
				
				if (tryExpandWorkingSet() == false){
					break outer;
				}
			} 
			
			//Add the first operation of working set to consolidated operations.
			processedOperations.add(workingSet.remove(0));
			
			//Reduce the working set.
			while(workingSet.size() > minWorkingSetSize){
				reduceWorkingSet();
			}
		}
		processedOperations.addAll(workingSet);
	}
	
	/**
	 * Reduce the size of the working set by removing the last element and adding it first 
	 * to the list of low level operations.
	 */
	private void reduceWorkingSet() {
		//Add the last element of working set to the first position in low level operations.
		unprocessedOperations.add(0, workingSet.remove(workingSet.size()-1)); 
	}

	/**
	 * Try to expend the current working set. Messages are immediately added to high level operations,
	 * as are initialization. 
	 * @return False if the working set could not be expanded.
	 */
	private Operation candidate;
	private boolean tryExpandWorkingSet() {
		if(unprocessedOperations.isEmpty()){
			return false;
		}
		
		candidate = unprocessedOperations.remove(0); 
		//Found a message. Add continue expansion.
		if (candidate.operation == OperationType.message){
			keepSetAddHigh();
			return tryExpandWorkingSet(); //Call self until working set has been expanded.
			
		//Found an init operation. Flush working set into high level operations, then add the init.
		} else if (candidate.operation == OperationType.init){
			flushSetAddHigh();
			return tryExpandWorkingSet(); //Try to expand working set again.
			
		//Only read/write operations should remain at this point.
		} else if (isReadOrWrite(candidate) == false){
			handleHighLevelOperation();
			return tryExpandWorkingSet(); //Try to expand working set again.
		}
		
		//Add the read/write operation to the working set.
		workingSet.add((OP_ReadWrite) candidate);
		return true;
	}
	
	private void handleHighLevelOperation(){

		switch(highOrderRoutine){
			case HALT:
				System.err.println("HALT has not been implemented yet. Sorry :/.");
				System.exit(-1); //TODO: Handle properly.
				break;
				
			case KEEP_SET_ADD_HIGH:
				keepSetAddHigh();
				break;
				
			case FLUSH_SET_ADD_HIGH:
				flushSetAddHigh();
				break;
				
			case DISCARD:
				tryExpandWorkingSet();
				break;
			case DECONSTRUCT:
				deconstruct();
				System.exit(-1); //TODO: Handle properly.
				break;
		}
		
	}
	/**
	 * Deconstruct operation into read/write operations.
	 */
	private void deconstruct(){
		System.err.println("DECONSTRUCT has not been implemented yet. Sorry :/.");
	}
	
	/**
	 * Add high-level operation found to processedOperations, then continue on the current working set.
	 */
	private void keepSetAddHigh(){
		processedOperations.add(candidate);
	}
	
	/**
	 * Flush the working set into processedOperations, then add the high-level operation as well.
	 */
	private void flushSetAddHigh(){
		processedOperations.addAll(workingSet);
		processedOperations.add(candidate);
		workingSet.clear();
	}
	
	/**
	 * Returns true if the operation is a read or write operation, thus being capable of inheriting OP_ReadWrite.
	 * @param op The operation to test.
	 * @return True if the operation is a read/write operation. False otherwise.
	 */
	private boolean isReadOrWrite(Operation op){
		return (op.operation == OperationType.read || op.operation == OperationType.write);
	}
	
	/**
	 * Attempt to consolidate the working set held by this Interpreter. Will return true and add the new operation to 
	 * processedOperations if successful. Will not clear the working set.
	 * @return True if workingSet was successfully consolidated, false otherwise.
	 */
	private boolean attemptConsolidateWorkingSet(){
		Operation consolidatedOperation;
		consolidatedOperation = consolidator.attemptConsolidate(workingSet);
		if (consolidatedOperation != null){
			processedOperations.add(consolidatedOperation);
			return true;
		}

		return false;
	}
}
