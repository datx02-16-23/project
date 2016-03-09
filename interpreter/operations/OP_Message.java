package operations;
import java.util.HashMap;

/**
 * Creates an operation to initialize an {@code AnnotatedVariable}.
 */
public class OP_Message extends Operation{
	private static final String OPERATION = "message";

	/**
	 * Creates a new Init operation. Note that you must set the target, maxSize and initial values.
	 */
	public OP_Message() {
		super(OPERATION, new HashMap<String, Object>());
	}
	
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
		this.operationBody.put(OPERATION, value);
	}
	public String getValue(){
		return (String)this.operationBody.get(OPERATION);
	}
	
	

}
