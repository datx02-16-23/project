import java.util.ArrayList;
import java.util.List;

import operations.OP_ReadWrite;
import operations.OP_Swap;
import operations.OperationParser;
import wrapper.Operation;

/**
 * The Interpreter class contains methods for consolidating read/write operations into more complex operations.
 * The class would typically be used by creating a new instance, setting the list of low level operations and then
 * calling getConsolidatedOperations(). The list of consolidated operations is never cleared, so once all low level
 * operations have been processed a new list of low level operations may be added. The consolidated operations resulting
 * from the new list will be appended and the end of the list of consolidated operations held by this Interpreter.
 * @author Richard
 *
 */
//TODO: Create add handling for all high level operations encounter in lowLevelOperations when calling
// consolidateOperations(), make it possible to reconsolidate the operations found in consolidatedOperations. 
// This will allow combination of low level operations originally found in different lowLevelOperations lists.
public class Interpreter {
	
	private List<Operation> lowLevelOperations;
	private List<OP_ReadWrite> workingSet;
	private List<Operation> consolidatedOperations;

	/**
	 * Create a new Interpreter. 
	 */
	public Interpreter(){
		lowLevelOperations = new ArrayList<Operation>();
		workingSet = new ArrayList<OP_ReadWrite>();
		consolidatedOperations = new ArrayList<Operation>();
	}
	
	/**
	 * Consolidate the list of low level operations (read/write) held by this Interpreter. May still contain
	 * read/write operations if they cannot be consolidated into more complex operations. Initilizations
	 * and messages are added to the list of high level operations as they are found. When this method returns,
	 * getLowLevelOperations.size() will be 0. You may then supply a new list to be consolidated and appended to the list
	 * of consolidated operations. The list of consolidated operations will never be cleared automatically.
	 * @return A consolidated list of operations.
	 */
	public List<Operation> getConsolidatedOperations(){
		consolidateOperations();
		return consolidatedOperations;
	}
	
	/**
	 * Clear the list of consolidated operations. This method must be called manually if you do not wish
	 * for the result of several consolidated list of low level operations to be combined.
	 */
	public void clearConsoloidatedOperations(){
		consolidatedOperations.clear();
	}
	
	/**
	 * Returns the list of low level operations held by this Interpreter. 
	 * @return The list of low level operations held by this Interpreter.
	 */
	public List<Operation> getLowLevelOperations() {
		return lowLevelOperations;
	}

	/**
	 * Set the list of low level operations used by this Interpreter.
	 * The interpreter can handle read/write, message and init.
	 * @param lowLevelOperations A new list of low level operations held by this interpreter.
	 */
	public void setLowLevelOperations(List<Operation> lowLevelOperations) {
		if (lowLevelOperations == null){
			throw new NullPointerException("List of low level operations cannot be null.");
		}
		this.lowLevelOperations = lowLevelOperations;
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
		outer: while(lowLevelOperations.isEmpty() == false){
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
			consolidatedOperations.add(workingSet.remove(0));
			
			//Reduce the working set.
			while(workingSet.size() > minWorkingSetSize){
				reduceWorkingSet();
			}
		}
		consolidatedOperations.addAll(workingSet);
		
	}
	
	/**
	 * Reduce the size of the working set by removing the last element and adding it first 
	 * to the list of low level operations.
	 */
	private void reduceWorkingSet() {
		//Add the last element of working set to the first position in low level operations.
		lowLevelOperations.add(0, workingSet.remove(workingSet.size()-1)); 
	}

	/**
	 * Try to expend the current working set. Messages are immediately added to high level operations,
	 * as are initialization. 
	 * @return False if the working set could not be expanded.
	 */
	private boolean tryExpandWorkingSet() {
		if(lowLevelOperations.isEmpty()){
			return false;
		}
		
		Operation op = lowLevelOperations.remove(0);
		
		//Found a message. Add qne continue expansion.
		if (op.operation.equals("message")){
			consolidatedOperations.add(op);
			return tryExpandWorkingSet(); //Call self until working set has been expanded.
			
		//Found an init operation. Flush working set into high level operations, then add the init.
		} else if (op.operation.equals("init")){ //TODO: Do this for any high level operations?
			consolidatedOperations.addAll(workingSet);
			consolidatedOperations.add(op);
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
		return op.operation.equals("read") || op.operation.equals("write") || op.operation.equals("readwrite");
	}
	
	private boolean attemptConsolidateWorkingSet(){
		switch(workingSet.size()){
			case 3:
				OP_Swap op_swap = OP_Swap.consolidate(workingSet);
				if (op_swap != null){
					consolidatedOperations.add(op_swap);
					return true;
				}
				break;
				
			default:
				break;
		}

		return false;
	}
}
