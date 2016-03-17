import java.util.ArrayList;
import java.util.List;

import operations.OP_ReadWrite;
import operations.OP_Swap;
import operations.OperationParser;
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
	
	private List<Operation> unprocessedOperations;
	private List<OP_ReadWrite> workingSet;
	private List<Operation> processedOperations;

	/**
	 * Create a new Interpreter. Use the setOperations() and getConsolidatedOperations() methods to
	 * interpret lists of operations.
	 */
	public Interpreter(){
		unprocessedOperations = new ArrayList<Operation>();
		workingSet = new ArrayList<OP_ReadWrite>();
		processedOperations = new ArrayList<Operation>();
		//Different list types? Array lists may become a performance issue in the future.
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
		//TODO: Ta fram min/max working size automagiskt.
		int minWorkingSetSize = 3; //Must be > 0.
		int maxWorkingSetSize = 3; //Must be >= minWorkingSetSize.
		
		//Continue until all operations are handled
		outer: while(unprocessedOperations.isEmpty() == false || workingSet.isEmpty() == false){
			//Expand working set.
			while(workingSet.size() < minWorkingSetSize){
				if (tryExpandWorkingSet() == false){
					break;
				}
			}
			
			//Expand working set and attempt consolidation.
			while (workingSet.size() <= maxWorkingSetSize) {
				if(attemptConsolidateWorkingSet() == true){ //Attempt to consolidate the set.
					workingSet.clear();
					continue outer; //Working set converted to a more complex operation. Begin work on new set.
				}
				
				if (tryExpandWorkingSet() == false){
					break;
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
	private boolean tryExpandWorkingSet() {
		if(unprocessedOperations.isEmpty()){
			return false;
		}
		
		Operation op = unprocessedOperations.remove(0);
		
		//Found a message. Add qne continue expansion.
		if (op.operation.equals("message")){
			processedOperations.add(op);
			return tryExpandWorkingSet(); //Call self until working set has been expanded.
			
		//Found an init operation. Flush working set into high level operations, then add the init.
		} else if (op.operation.equals("init")){ //TODO: Do this for any high level operations?
			processedOperations.addAll(workingSet);
			processedOperations.add(op);
			return tryExpandWorkingSet(); //Call self until working set has been expanded.
			
		//Only read/write operations should remain at this point.
		} else if (isReadOrWrite(op) == false){
			throw new IllegalArgumentException("Interpreter cannot handle operations of type: " + op.operation);
		}
		
		//Add the read/write operation to the working set.
		workingSet.add(OperationParser.parseReadWrite(op));
		return true;
	}
	
	private boolean isReadOrWrite(Operation op){
		return op.operation.equals("read") || op.operation.equals("write");
	}
	
	private boolean attemptConsolidateWorkingSet(){
		switch(workingSet.size()){
			case 3:
				OP_Swap op_swap = OP_Swap.consolidate(workingSet);
				if (op_swap != null){
					processedOperations.add(op_swap);
					return true;
				}
				break;
				
			default:
				break;
		}

		return false;
	}
}
