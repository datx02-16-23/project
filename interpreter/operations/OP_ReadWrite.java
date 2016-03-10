package operations;
import java.util.HashMap;

import wrapper.ArrayVariable;
import wrapper.Operation;


public abstract class OP_ReadWrite extends Operation{
	private static final String OPERATION = "ReadWrite";
	private static final String KEY_TARGET = "target";
	private static final String KEY_SOURCE = "source";
	private static final String KEY_VALUE = "value";

	
	/**
	 * Create a new ReadWrite operation. Note that you must set the target, source and value.
	 */
	public OP_ReadWrite(String operation, HashMap<String, Object> operationBody) {
		super(operation, operationBody);
	}
	
	/**
	 * Create a new ReadWrite operation. Note that you must set the target, source and value.
	 */
	public OP_ReadWrite() {
		super(OPERATION, new HashMap<String, Object>());
	}
	
	/**
	 * Set the target variable for this ReadWrite operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param target The target variable for this ReadWrite operation.
	 */
	public void setTarget(ArrayVariable target){
		this.operationBody.put(KEY_TARGET, target);
	}
	
	/**
	 * Set the source variable for this ReadWrite operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param source The source variable for this ReadWrite operation.
	 */
	public void setSource(ArrayVariable source){
		this.operationBody.put(KEY_SOURCE, source);
	}
	
	/**
	 * Set the value(s) which were ReadWrite from {@code source}. This should be the value
	 * of {@code target} and the specified index after operation execution, if applicable.
	 * @param value Set the value(s) which were ReadWrite from {@code source}.
	 */
	public void setValue(String value){
		this.operationBody.put(KEY_VALUE, value);
	}
	
	
	
	public ArrayVariable getTarget(){
		return (ArrayVariable)this.operationBody.get(KEY_TARGET);
	}
	public ArrayVariable getSource(){
		return (ArrayVariable)this.operationBody.get(KEY_SOURCE);
	}
	public String getValue(){
		return (String)this.operationBody.get(KEY_VALUE);
	}

	@Override
	public String toString() {
		return "{ \"operation\": "+OPERATION+", \"operationBody\":"+Operation.printOperationBody(this)+"}";
	}
	
}
