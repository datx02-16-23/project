package operations;

import java.util.Arrays;
import java.util.HashMap;

import wrapper.ArrayVariable;

/**
 * Creates an operation to initialize an {@code AnnotatedVariable}.
 */
public class OP_Init extends Operation{
	private static final String OPERATION = "init";
	private static final String KEY_TARGET = "target";
	private static final String KEY_SIZE = "size";
	private static final String KEY_VALUE = "value";

	/**
	 * Creates a new Init operation. Note that you must set the target, maxSize and initial values.
	 */
	public OP_Init() {
		super(OPERATION, new HashMap<String, Object>());
	}
	
	/**
	 * Set the target variable for this Init operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param var The target variable for this Init operation.
	 */
	public void setTarget(ArrayVariable var){
		this.operationBody.put(KEY_TARGET, var);
	}
	
	/**
	 * Set the declared maximum size of this variable, for each dimension.
	 * @param size The declared maximum size of this variable.
	 */
	public void setSize(int [] size){
		this.operationBody.put(KEY_SIZE, size);
	}
	
	/**
	 * Set the value(s) with which to initialize this variable.
	 * @param value The value(s) with which to initialize this variable.
	 */
	public void setValue(String value){
		this.operationBody.put(KEY_VALUE, value);
	}
	
	public ArrayVariable getTarget(){
		return (ArrayVariable)this.operationBody.get(KEY_TARGET);
	}
	
	public int[] getSize(){
		return (int[])this.operationBody.get(KEY_SIZE);
	}
	
	public String getValue(){
		return (String)this.operationBody.get(KEY_VALUE);
	}
	
	public String printBody(){
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		for( String key : operationBody.keySet()){
			builder.append("\""+key+"\": ");
			switch(key){
				case KEY_TARGET:
					builder.append(((ArrayVariable)operationBody.get(key)).toString() +",\n");
				break;
				case KEY_SIZE:
					builder.append(Arrays.toString((int[])operationBody.get(key))+",\n");
				break;
				case KEY_VALUE:
					builder.append((String)operationBody.get(key) +",\n");
				break;
			}
			 
		}
		builder.delete(builder.length()-2, builder.length());
		builder.append("}");
		return builder.toString();
	}

	@Override
	public String toString() {
		return "{ \"operation\": \""+OPERATION+"\", \"operationBody\":"+printBody()+"}";
	}
	
	

}
