package interpreter;

import java.util.ArrayList;
import java.util.List;

import manager.operations.OP_ReadWrite;
import manager.operations.OP_Swap;
import wrapper.Operation;

/**
 * A Consolidator attempts to consolidate low level (read/write) operations into higher level operations.
 * @author Richard
 *
 */
public class Consolidator {
	
	private int minimumSetSize = Integer.MAX_VALUE;
	private int maximumSetSet = Integer.MIN_VALUE;
	private final ArrayList<Consolidable>[] invokers;
	
	@SuppressWarnings("unchecked")
	public Consolidator(){
		
		invokers = new ArrayList[10];
		for(int i = 0; i < 10; i++){
			invokers[i] = new ArrayList<Consolidable>();
		}
		
		addDefaultInvokers();
	}
	
	/**
	 * Add the default invokers to the list of possible consolidatons.
	 */
	private void addDefaultInvokers(){
		addConsolidable(new OP_Swap());
	}
	
	/**
	 * Returns true if the type of the Consolidable given as argument is among the test cases used by this Consolidator.
	 * @param testCase The Consolidable to test.
	 * @return True if the test case type is among the tested.
	 */
	public boolean checkConsolidable(Consolidable testCase){
		
		for(Consolidable c : invokers[testCase.getRWcount()]){
			if(c.getClass().equals(testCase.getClass())){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Adds another Consolidable for this Consolidator 
	 * @param c The Consolidable to add.
	 */
	public void addConsolidable(Consolidable c){
		int rwCount = c.getRWcount();
		
		if (rwCount < minimumSetSize){
			minimumSetSize = rwCount;
		}
		if (rwCount > maximumSetSet){
			maximumSetSet = rwCount;
		}
		invokers[rwCount].add(c);
	}
	
	/**
	 * Attempt to consolidate the working set supplied. Returns a consolidated operation if successful, null otherwise.
	 * @param rwList The list of read/write operations to consolidate.
	 * @return A consolidated operation if successful, null otherwise.
	 */
	public Operation attemptConsolidate(List<OP_ReadWrite> rwList){
		Operation consolidatedOperation = null;
		
		for(Consolidable c : invokers[rwList.size()]){
			consolidatedOperation = c.consolidate(rwList);
		}
		return consolidatedOperation;
	}
	
	/**
	 * Returns the maximum number of read/write operations this Consolidator will use.
	 * @return The maximum number of read/write operations this Consolidator will use.
	 */
	public int getMaximumSetSet() {
		return maximumSetSet;
	}
	/**
	 * Returns the minimum number of read/write operations this Consolidator will use.
	 * @return The minimum number of read/write operations this Consolidator will use.
	 */
	public int getMinimumSetSize() {
		return minimumSetSize;
	}	
	
}
