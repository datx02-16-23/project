import java.util.ArrayList;
import java.util.List;

import operations.OP_ReadWrite;
import operations.OP_Swap;
import wrapper.Operation;

/**
 * 
 * @author Richard
 *
 */
public class Interpreter {
	
	private List<Operation> lowLevelOperations;
	
	private final List<OP_ReadWrite> workingSet;
	private final List<Operation> consolidatedOperations;

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
	 * read/write operations if they cannot be consolidated into more complex operations. Initilizations'
	 * and messages are added to the list of high level operations as they are found.
	 * @return A consolidated list of operations.
	 */
	public List<Operation> getConsolidatedOperations(){
		consolidateOperations();
		return consolidatedOperations;
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
	
	private void consolidateOperations(){
		//TODO: Ta fram min/max working size automagiskt.
		int minWorkingSetSize = 3;
		int maxWorkingSetSize = 3;
		
		//Expand working set until the minimum size is reached.
		while(workingSet.size() < minWorkingSetSize){
			if (tryExpandWorkingSet() == false){
				return; //Body processed. Consolidation completed.
			}
		}
		
		//Continue until all operations are handled
		outer: while(true){

			while (workingSet.size() < maxWorkingSetSize) {
				if(attemptConsolidateWorkingSet() == true){ //Attempt to consolidate the set.
					continue outer; //Working set converted to a more complex operation. Begin work on new set.
				}
				
				if (tryExpandWorkingSet() == false){
					return; //Body processed. Consolidation completed.
				}
			} 
			
			consolidatedOperations.add(workingSet.remove(0)); //Add the first operation of working set to consolidated operations.
			
			while(workingSet.size() > minWorkingSetSize){
				if (reduceWorkingSet() == false){
					return; //Body processed. Consolidation completed.
				}
			}
		}
	}
	
	/**
	 * Try to reduce the working set. If it fails (because the working set is already depleted)
	 * this function will add the remaining operations in lowLevelOperations to consolidatedOperations.
	 * @return False if the working set could not be reduced.
	 */
	private boolean reduceWorkingSet() {
		if (workingSet.isEmpty()){
			consolidatedOperations.addAll(lowLevelOperations);
			return false;			
		}
		//Add the last element of working set to the first position in low level operations.
		lowLevelOperations.add(0, workingSet.remove(workingSet.size()-1)); 
		return true;
	}

	/**
	 * Try to expend the current working set. Messages are immediately added to high level operations,
	 * as are initialization. 
	 * @return False if the working set could not be expanded.
	 */
	private boolean tryExpandWorkingSet() {
		if(lowLevelOperations.isEmpty()){
			consolidatedOperations.addAll(workingSet); //Add unconsolidated read/write operations.
			return false;
		}
		
		Operation op = lowLevelOperations.remove(0);
		
		//Found a message. Add to high level oprations.
		if (op.operation.equals("message")){
			consolidatedOperations.add(op);
			return tryExpandWorkingSet(); //Call self until working set has been expanded.
			
		//Found an init operation. Flush working set into high level operations, then add the init as well.
		} else if (op.operation.equals("init")){
			consolidatedOperations.addAll(workingSet); //Add unconsolidated read/write operations.
			consolidatedOperations.add(op); //Add the init.
			return false;
			//return tryExpandWorkingSet(); //Call self until working set has been expanded.
			
		//Only read/write operations should remain at this point.
		} else if (isReadOrWrite(op) == false){
			throw new IllegalArgumentException("Interpreter cannot handle operations of type: " + op.operation);
		}
		
		//Add the read/write operation to the working set.
		workingSet.add((OP_ReadWrite)op);
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
