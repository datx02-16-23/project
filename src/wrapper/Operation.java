package wrapper;

import java.util.HashMap;
import manager.operations.Operations;

/**
 * Abstract wrapper class containing the necessary data to recreate a given operation.
 * Should be inherited to create specific operations.
 */
public class Operation {
	/**
	 * The literal name of the operation, such as "init" (initialize) or "read".
	 */
	public final Operations operation;
	/**
	 * A map containing the identifier of the field (such as "destination" or "value") and the data they contained.
	 */
	public final HashMap<String, Object> operationBody;
	
	/**
	 * Create a new Operation with the given operation identifier and body.
	 * @param operation The literal name of the operation, such as "init" (initialize) or "read".
	 * @param operationBody A map containing the identifier of the field
	 * (such as "destination" or "value") and the data they contained.
	 */
	public Operation(Operations operation, HashMap<String, Object> operationBody){
		this.operation = operation;
		this.operationBody = operationBody;
	}
	
	public String printOperationBody(){
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		for( String key : operationBody.keySet()){
			if (operationBody.get(key) != null){
				builder.append("\""+key+"\": "+ operationBody.get(key).toString() +",\n");
			} else {
				builder.append("\""+key+"\": NULL");
			}
		}
		builder.delete(builder.length()-2, builder.length());
		builder.append("}");
		return builder.toString();
	}
}
