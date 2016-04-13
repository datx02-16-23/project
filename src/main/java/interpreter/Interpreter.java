package interpreter;

import java.util.ArrayList;
import java.util.List;

import wrapper.Operation;
import wrapper.operations.*;

/**
 * Class for consolidating groups of low level operations (read/write).
 * @author Richard
 *
 */
public class Interpreter {
	/**
	 * Halt execution if a high-level operation is found.
	 */
	public static final int ABORT = 0;
	/**
	 * Add any high-level operation found to processedOperations, then continue on the current working set.
	 */
	public static final int KEEP_SET_ADD_HIGH = 1;
	/**
	 * Flush the working set into processedOperations, then add the high-level operation as well.
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
	
	private int highOrderRoutine;
	
	private final List<Operation> before;
	private List<Operation> after;
	private List<OP_ReadWrite> workingSet;
	private Consolidator consolidator;

	/**
	 * Create a new Interpreter with the high order routine set to FLUSH_SET_ADD_HIGH. Use the setOperations()
	 * and getConsolidatedOperations() methods to interpret lists of operations.
	 */
	public Interpreter(){
		before = new ArrayList<Operation>();
		workingSet = new ArrayList<OP_ReadWrite>();
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
		if (highOrderRoutine == ABORT){
			for(Operation op : operations){
				
				if (op.operation == OperationType.message || op.operation == OperationType.init){
					continue; //Acceptable non read/write operation found.
				} else if (isReadOrWrite(op) == false){
					return false;
				}
				
			}
		}
		
		int oldSize = operations.size();
		
		before.clear(); //Not really needed.
		before.addAll(operations);		
		
		after = operations;
		after.clear();
		
		workingSet.clear();
		
		candidate = null;
		consolidateOperations();
		
		return oldSize == operations.size();
	}
	
	/**
	 * Build and evaluate working sets until all operations in lowLevelOperations have been processed.
	 * When this method returns, lowLevelOperations.size() will be 0.
	 */
	private void consolidateOperations(){
		int minWorkingSetSize = consolidator.getMinimumSetSize();
		int maxWorkingSetSize = consolidator.getMaximumSetSize();
		
		if(minWorkingSetSize < 0 || maxWorkingSetSize < 0){
			after.addAll(before);
			return; //No operations in Consolidator.
		}
		
		//Continue until all operations are handled
		outer: while(before.isEmpty() == false || workingSet.isEmpty() == false){
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
			after.add(workingSet.remove(0));
			
			//Reduce the working set.
			while(workingSet.size() > minWorkingSetSize){
				reduceWorkingSet();
			}
		}
		
		after.addAll(workingSet);
	}
	
	/**
	 * Reduce the size of the working set by removing the last element and adding it first 
	 * to the list of low level operations.
	 */
	private void reduceWorkingSet() {
		//Add the last element of working set to the first position in low level operations.
		before.add(0, workingSet.remove(workingSet.size()-1));
	}

	/**
	 * Try to expend the current working set. Messages are immediately added to high level operations,
	 * as are initialization. 
	 * @return False if the working set could not be expanded.
	 */
	private Operation candidate;
	private boolean tryExpandWorkingSet() {
		if(before.isEmpty()){
			return false;
		}
		
		candidate = before.remove(0); 
		if (candidate.operation == OperationType.message){
			keepSet_addCandidate();
			return tryExpandWorkingSet(); 
			
		} else if (candidate.operation == OperationType.init){
			flushSet_addCandidate();
			return tryExpandWorkingSet();
			
		//Only read/write operations should remain at this point.
		} else if (isReadOrWrite(candidate) == false){
			handleHighLevelOperation();
			return tryExpandWorkingSet();
		}
		
		//Add the read/write operation to the working set.
		workingSet.add((OP_ReadWrite) candidate);
		return true;
	}
	
	private void handleHighLevelOperation(){

		switch(highOrderRoutine){
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
	private void deconstruct(){
		System.err.println("DECONSTRUCT has not been implemented yet. Sorry :/.");
	}
	
	/**
	 * Add high-level operation found to processedOperations, then continue on the current working set.
	 */
	private void keepSet_addCandidate(){
		after.add(candidate);
	}
	
	/**
	 * Flush the working set into processedOperations, then add the high-level operation as well.
	 */
	private void flushSet_addCandidate(){
		after.addAll(workingSet);
		after.add(candidate);
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
		 Operation consolidatedOperation = consolidator.attemptConsolidate(workingSet);
		 
		if (consolidatedOperation != null){
			after.add(consolidatedOperation);
			return true;
		}

		return false;
	}

	/**
	 * Returns a list of all active test cases for this Consolidator.
	 * @return A list of all active test cases for this Consolidator.
	 */
	public List<OperationType> getTestCases(){
		return consolidator.getTestCases();
	}

	/**
	 * Remove a given testcase.
	 * @param testCase The testcase to remove.
	 * @param c The Consolidable associated with the testCase.
	 */
	public void removeTestCase(OperationType testCase, Consolidable c){
		consolidator.removeTestCase(testCase, c.getRWcount());
	}

	/**
	 * Add a test case to the Interpreter.
	 * @param testCase The test case to add.
	 */
	public void addTestCase(OperationType testCase){
		switch(testCase){
		
			case swap:
				consolidator.addConsolidable(new OP_Swap());
				break;
			
			default:
				System.err.println("Cannot consolidate the type: " + testCase.toString().toUpperCase());
				break;
		}
	}
		
		/*
		 *	CONSOLIDATOR 
		 */
		
		/**
		 * A Consolidator attempts to consolidate low level (read/write) operations into higher level operations.
		 * @author Richard
		 *
		 */
		private class Consolidator {
			
			private static final int MAX_SIZE = 10;
			private int minimumSetSize = Integer.MAX_VALUE;
			private int maximumSetSize = Integer.MIN_VALUE;
			private final ArrayList<Consolidable>[] invokers;
			
			/**
			 * Create a new Consolidator with the default types.
			 */
			@SuppressWarnings("unchecked")
			public Consolidator(){
				
				invokers = new ArrayList[MAX_SIZE];
				for(int i = 0; i < MAX_SIZE; i++){
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
			 * Returns true if the type of the OperationType given as argument is among
			 * the test cases used by this Consolidator.
			 * @param testCase The OperationType to test.
			 * @return True if the test case type is among the tested, false otherwise.
			 */
			public boolean checkTestCase(OperationType testCase){
				List<OperationType> testCases = getTestCases();
				
				for(OperationType ot : testCases){
					if(ot == testCase){
						return true;
					}
				}
				
				return false;
			}
			
			/**
			 * Adds another Consolidable for this Consolidator.
			 * Will not any any Consolidable type more than once.
			 * @param newConsolidable The Consolidable to add.
			 */
			public void addConsolidable(Consolidable newConsolidable){
				int rwCount = newConsolidable.getRWcount();
				
				if (rwCount < minimumSetSize){
					minimumSetSize = rwCount;
				}
				if (rwCount > maximumSetSize){
					maximumSetSize = rwCount;
				}
				
				Operation newOp = (Operation) newConsolidable;
				for(Consolidable c : invokers[rwCount]){
					Operation op = (Operation) c;
					if(newOp.operation == op.operation){
						return;
					}
				}
				
				invokers[rwCount].add(newConsolidable);
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
					if (consolidatedOperation != null){
						return consolidatedOperation;
					}
				}
				return null;
			}
			
			/**
			 * Returns the maximum number of read/write operations this Consolidator will use.
			 * @return The maximum number of read/write operations this Consolidator will use.
			 */
			public int getMaximumSetSize() {
				if(minimumSetSize == Integer.MIN_VALUE){
					return -1;
				}
				return maximumSetSize;
			}
			/**
			 * Returns the minimum number of read/write operations this Consolidator will use.
			 * @return The minimum number of read/write operations this Consolidator will use.
			 */
			public int getMinimumSetSize() {
				if(minimumSetSize == Integer.MAX_VALUE){
					return -1;
				}
				return minimumSetSize;
			}	
			
			/**
			 * Print a human-readable list of all the Operation types this Consolidator tests.
			 * @return A human-readable list of all the Operation types this Consolidator tests.
			 */
			public List<OperationType> getTestCases(){
				ArrayList<OperationType> simpleNames = new ArrayList<OperationType>();
					
				for(int i = 0; i < MAX_SIZE; i++){
					for(Consolidable c : invokers[i]){
						Operation op = (Operation) c;
						simpleNames.add(op.operation);
					}
					
				}
				return simpleNames;
			}
	
			/**
			 * Remove a given testCase. When this method returns, the testcase is guaranteed to be removed.
			 * @param testCase The testcase to remove.
			 * @param rwCount The number of variables the test case consists of.
			 */
			public void removeTestCase(OperationType testCase, int rwCount){
				Consolidable victim = null;
				
				for (Consolidable c : invokers[rwCount]){
					Operation op = (Operation) c;
					if(testCase == op.operation){
						victim = c;
						break;
					}
				}
				
				if(victim != null){
					invokers[rwCount].remove(victim);
				}
				
				if(victim.getRWcount() == maximumSetSize){
					recalculateMaxSetSize();
				}
				if(victim.getRWcount() == minimumSetSize){
					recalculateMinSetSize();
				}
			}
			
			/**
			 * Recalculate the maximum set size used by the Consolidator.
			 */
			private void recalculateMaxSetSize(){
				maximumSetSize = Integer.MIN_VALUE;
				
				for(int i = 0; i < MAX_SIZE; i++){
					for(Consolidable c : invokers[i]){
						if(c.getRWcount() > maximumSetSize){
							maximumSetSize = c.getRWcount();
						}
					}
				}
				
			}
			
			/**
			 * Recalculate the minimum set size used by the Consolidator.
			 */
			private void recalculateMinSetSize(){
				minimumSetSize = Integer.MAX_VALUE;
				
				for(int i = 0; i < MAX_SIZE; i++){
					for(Consolidable c : invokers[i]){
						if(c.getRWcount() > minimumSetSize){
							minimumSetSize = c.getRWcount();
						}
					}
				}
				
			}
		}
	
}
