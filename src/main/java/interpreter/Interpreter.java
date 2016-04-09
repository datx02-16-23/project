package interpreter;

import java.util.ArrayList;
import java.util.List;

import manager.operations.OP_ReadWrite;
import manager.operations.OperationType;
import wrapper.Operation;

/**
 * The Interpreter class contains methods for consolidating read/write operations into more complex operations.
 * The class would typically be used by creating a new instance, setting the list of low level operations and then
 * calling getConsolidatedOperations().
 * @author Richard
 *
 */
//TODO: Create add handling for all high level operations encounter in lowLevelOperations when calling
// consolidateOperations(), make it possible to reconsolidate the operations found in consolidatedOperations. 
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
	
	private List<Operation> unprocessedOperations;
	private List<OP_ReadWrite> workingSet;
	private List<Operation> processedOperations;
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
			throw new IllegalArgumentException("something useful."); //TODO: Write something useful.
		}
		highOrderRoutine = newRoutine;
	}
	/**
	 * Attempt to consolidate the list of low level operations (read/write) held by this Interpreter.
	 * When this method returns, getLowLevelOperations.size() will be 0. You may then supply a new list
	 * to be consolidated and appended to the list of consolidated operations.
	 * @return A consolidated list of operations.
	 */
	public List<Operation> getConsolidatedOperations(){
		consolidateOperations();
		return processedOperations;
	}
	
	/**
	 * Clear the list of consolidated operations. This method must be called manually if you do not wish
	 * for the result of several consolidated list of low level operations to be combined.
	 */
	public void clearConsoloidatedOperations(){
		processedOperations.clear();
	}
	
	/**
	 * Returns the list of low level operations held by this Interpreter. 
	 * @return The list of low level operations held by this Interpreter.
	 */
	public List<Operation> getLowLevelOperations() {
		return unprocessedOperations;
	}

	/**
	 * Set the list of low level operations used by this Interpreter.
	 * The interpreter can handle read/write, message and init.
	 * @param operations A new list of low level operations held by this interpreter.
	 */
	public void setOperations(List<Operation> operations) {
		if (operations == null){
			throw new NullPointerException("List of low level operations cannot be null.");
		}
		this.unprocessedOperations = operations;
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
			keep_set_add_high();
			return tryExpandWorkingSet(); //Call self until working set has been expanded.
			
		//Found an init operation. Flush working set into high level operations, then add the init.
		} else if (candidate.operation == OperationType.init){
			flush_set_add_high();
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
				System.exit(-1); //TODO: Handle properly.
				break;
				
			case KEEP_SET_ADD_HIGH:
				keep_set_add_high();
				break;
				
			case FLUSH_SET_ADD_HIGH:
				flush_set_add_high();
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
	private void deconstruct(){
		throw new UnsupportedOperationException("Deconstruction has not been implemented.");
	}
	
	/**
	 * Add high-level operation found to processedOperations, then continue on the current working set.
	 */
	private void keep_set_add_high(){
		processedOperations.add(candidate);
	}
	
	/**
	 * Flush the working set into processedOperations, then add the high-level operation as well.
	 */
	private void flush_set_add_high(){
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
