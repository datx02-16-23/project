package manager.operations;

import wrapper.Locator;

/**
 * Create a new Write operation, from the variable at index specified by {@code source}
 * to the variable at index specified by {@target}. The source may be set null if necessary.
 */
public class OP_Write extends OP_ReadWrite{
	private static final Operations OPERATION = Operations.write;
	private static final String KEY_TARGET = "target";
	private static final String KEY_SOURCE = "source";
	private static final String KEY_VALUE = "value";

	/**
	 * Create a new Write operation.  Note that you must set the target, source and value.
	 */
	public OP_Write() {
		super(OPERATION);
	}
	
	/**
	 * Set the target variable for this Write operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param target The target variable for this Write operation.
	 */
	public void setTarget(Locator target){
		if (target == null){
			throw new IllegalArgumentException("Target cannot be null in a Write operation!");
		}
		this.operationBody.put(KEY_TARGET, target);
	}
	
	/**
	 * Set the source variable for this Write operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param source The source variable for this Write operation.
	 */
	public void setSource(Locator source){
		this.operationBody.put(KEY_SOURCE, source);
	}
	
	/**
	 * Set the value(s) which were written to {@code target} (from {@code source}, if applicable).
	 * @param value Set the value(s) written to {@code target}.
	 */
	public void setValue(String value){
		this.operationBody.put(KEY_VALUE, value);
	}
	
	public Locator getTarget(){
		return (Locator)this.operationBody.get(KEY_TARGET);
	}

	public Locator getSource(){
		return (Locator)this.operationBody.get(KEY_SOURCE);
	}

	
	public String getValue(){
		return (String)this.operationBody.get(KEY_VALUE);
	}

	@Override
	public String toString() {
		return "{ \"operation\": "+OPERATION+", \"operationBody\":"+printOperationBody()+"}";
	}
	
	

}
