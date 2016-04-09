package manager.operations;
import java.util.HashMap;

import wrapper.Locator;
import wrapper.Operation;


/**
 * A primitive operation from which all other operations on data structures may be constructed.
 * @author Richard
 *
 */
public abstract class OP_ReadWrite extends Operation{
	
	/**
	 * Create a new ReadWrite operation. Note that you must set the target, source and value.
	 * @param operation The name of the operation. Should be "read" or "write".
	 */
	public OP_ReadWrite(OperationType operation) {
		super(operation, new HashMap<Key, Object>());
	}
	
	/**
	 * Set the target variable for this ReadWrite operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param target The target variable for this ReadWrite operation.
	 */
	public void setTarget(Locator target){
		this.operationBody.put(Key.target, target);
	}
	
	/**
	 * Set the source variable for this ReadWrite operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param source The source variable for this ReadWrite operation.
	 */
	public void setSource(Locator source){
		this.operationBody.put(Key.source, source);
	}
	
	/**
	 * Set the value(s) which were ReadWrite from {@code source}. This should be the value
	 * of {@code target} and the specified index after operation execution, if applicable.
	 * @param value Set the value(s) which were ReadWrite from {@code source}.
	 */
	public void setValue(double[] value){
		this.operationBody.put(Key.value, value);
	}
	
	public String toString(){
		return toSimpleString();		
	}
	
	public String toSimpleString(){
		return super.operation.toString().toUpperCase() + ": " + (getSource() == null ? "?" : getSource().toSimpleString()) 
				+ " -> " + (getTarget() == null ? "?" : getTarget().toSimpleString()) ;
	}
	
	public Locator getTarget(){
		return (Locator)this.operationBody.get(Key.target);
	}
	public Locator getSource(){
		return (Locator)this.operationBody.get(Key.source);
	}
	public double[] getValue(){
		return (double[])this.operationBody.get(Key.value);
	}
}
