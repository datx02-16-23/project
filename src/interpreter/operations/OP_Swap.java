package interpreter.operations;

import java.util.HashMap;
import java.util.List;

import interpreter.wrapper.ArrayVariable;
import interpreter.wrapper.Operation;

/**
 * Create a new Swap operation, shifting the values of {@code var1} and {@code var2}.
 */
public class OP_Swap extends Operation{
	private static final String OPERATION = "swap";
	private static final String KEY_VAR1 = "var1";
	private static final String KEY_VAR2 = "var2";
	private static final String KEY_VALUE = "value";

	/**
	 * Create a new Swap operation.  Note that you must set var1, var2, and value.
	 */
	public OP_Swap() {
		super(OPERATION, new HashMap<String, Object>());
	}
	
	/**
	 * Set var1 for this Swap operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param var1 Variable 1 for this Swap operation.
	 */
	public void setVar1(ArrayVariable var1){
		this.operationBody.put(KEY_VAR1, var1);
	}
	
	/**
	 * Set var2 for this Swap operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param var2 Variable 2 for this Swap operation.
	 */
	public void setVar2(ArrayVariable var2){
		this.operationBody.put(KEY_VAR2, var2);
	}
	
	
	/**
	 * The values contained at var1 and var2 respectively, AFTER this Swap operation has been executed.
	 * @param value The values in var1 and var2 after execution.
	 */
	public void setValues(String values){
		this.operationBody.put(KEY_VALUE, values);
	}
	
	
	
	public ArrayVariable getTarget(){
		return (ArrayVariable)this.operationBody.get(KEY_VAR1);
	}
	public ArrayVariable getSource(){
		return (ArrayVariable)this.operationBody.get(KEY_VAR2);
	}
	public String getValue(){
		return (String)this.operationBody.get(KEY_VALUE);
	}

	@Override
	public String toString() {
		return "{ \"operation\": "+OPERATION+", \"operationBody\":"+Operation.printOperationBody(this)+"}";
	}
	
	/**
	 * Attempt to create a Swap operation from 3 read/write operations.
	 * @param rwList The list of 3 read/write operations to test.
	 * @return A new Swap operation if the given testSet is a valid decomposition of a Swap operation, null otherwise.
	 */
	public static OP_Swap consolidate(List<OP_ReadWrite> rwList){
		if (rwList.size() != 3){
			throw new IllegalArgumentException("Swap operations are composed of 3 read/write operations.");
		}

		if (
				   rwList.get(0).getSource() == null || rwList.get(0).getTarget() == null
				|| rwList.get(1).getSource() == null || rwList.get(1).getTarget() == null
				|| rwList.get(2).getSource() == null || rwList.get(2).getTarget() == null
			){
			return null; //All sources/targets must be known.
		}
		
		ArrayVariable var1, tmp, var2;
		
	
		//Operation 1: Set var1 -> tmp
		var1 = rwList.get(0).getSource();
		tmp = rwList.get(0).getTarget();
		
		if(tmp.index != null){
			return null; //tmp should not be another array.�1
		}
		
		//Operation 2: x -> var1?
		if(rwList.get(1).getTarget().equals(var1)){
			var2 = rwList.get(1).getSource(); //Set x = var2
		} else {
			return null;
		}
		
		//Operation 3: tmp -> var2?
		if((rwList.get(2).getSource().equals(tmp) && rwList.get(2).getTarget().equals(var2)) == false){
			return null;
		}
		
		//Construct and return swap operation.
		OP_Swap op_swap = new OP_Swap();
		op_swap.setVar1(var1);;
		op_swap.setVar2(var2);
		//TODO: setValue() op_swap.setValues("");
		return op_swap;
	}

}
