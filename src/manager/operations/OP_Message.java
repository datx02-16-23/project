package manager.operations;
import java.util.HashMap;

import wrapper.Operation;


/**
 * Creates an operation to initialize an {@code AnnotatedVariable}.
 */
public class OP_Message extends Operation{
	private static final Operations OPERATION = Operations.message;

	/**
	 * Creates a new Init operation. Note that you must set the target, maxSize and initial values.
	 */
	public OP_Message() {
		super(OPERATION, new HashMap<Key, Object>());
	}
	
	/**
	 * Simply calls setValue().
	 * @param message The message to attach to this OP_Message.
	 */
	public void setMessage(String message){
		setValue(message);
	}
	public String getMessage(){
		return getValue();
	}
	/**
	 * Set the value(s) with which to initialize this variable.
	 * @param value The value(s) with which to initialize this variable.
	 */
	public void setValue(String value){
		this.operationBody.put(Key.value, value);
	}
	public String getValue(){
		return (String)this.operationBody.get(Key.value);
	}
	
	public String toString(){
		return "MESSAGE: " + getValue();
	}
	

}
