package manager.operations;

import application.Strings;
import wrapper.Locator;

/**
 * Create a new Read operation, from the variable at index specified by {@code source}
 * to the variable at index specified by {@target}. The target may be set null if necessary.
 */
public class OP_Read extends OP_ReadWrite{
	private static final Operations OPERATION = Operations.read;

	/**
	 * Create a new Read operation. Note that you must set the target, source and value.
	 */
	public OP_Read() {
		super(OPERATION);
	}
	
	/**
	 * Set the target variable for this Read operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param target The target variable for this Read operation.
	 */
	public void setTarget(Locator target){
		this.operationBody.put(Strings.KEY_TARGET, target);
	}
	
	/**
	 * Set the source variable for this Read operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param source The source variable for this Read operation.
	 */
	public void setSource(Locator source){
		if (source == null){
			throw new IllegalArgumentException("Source cannot be null in a Read operation!");
		}
		this.operationBody.put(Strings.KEY_SOURCE, source);
	}
	
	/**
	 * Set the value(s) which were read from {@code source}. This should be the value
	 * of {@code target} and the specified index after operation execution, if applicable.
	 * @param value Set the value(s) which were read from {@code source}.
	 */
	public void setValue(double[] value){
		this.operationBody.put(Strings.KEY_VALUE, value);
	}
	
	
	
	public Locator getTarget(){
		return (Locator)this.operationBody.get(Strings.KEY_TARGET);
	}
	public Locator getSource(){
		return (Locator)this.operationBody.get(Strings.KEY_SOURCE);
	}
	public double[] getValue(){
		return (double[])this.operationBody.get(Strings.KEY_VALUE);
	}
}
