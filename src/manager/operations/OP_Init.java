package manager.operations;

import java.util.Arrays;
import java.util.HashMap;

import wrapper.Locator;
import wrapper.Operation;

/**
 * Creates an operation to initialize an {@code AnnotatedVariable}.
 */
public class OP_Init extends Operation{
	private static final Operations OPERATION = Operations.init;
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
	public void setTarget(Locator var){
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
	public void setValue(double[] value){
		this.operationBody.put(KEY_VALUE, value);
	}
	
	public Locator getTarget(){
		return (Locator)this.operationBody.get(KEY_TARGET);
	}
	
	public int[] getSize(){
		return (int[])this.operationBody.get(KEY_SIZE);
	}
	
	public double[] getValue(){
		return (double[])this.operationBody.get(KEY_VALUE);
	}
	
	public String printBody(){
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		for( String key : operationBody.keySet()){
			builder.append("\t\""+key+"\": ");
			switch(key){
				case KEY_TARGET:
					builder.append("\t" + ((Locator)operationBody.get(key)).toString() +",\n");
				break;
				case KEY_SIZE:
					builder.append("\t" + Arrays.toString((int[])operationBody.get(key))+",\n");
				break;
				case KEY_VALUE:
					builder.append("\t" + Arrays.toString((double[])operationBody.get(key)) +",\n");
				break;
			}
			 
		}
		builder.delete(builder.length()-2, builder.length());
		builder.append("}\n");
		return builder.toString();
	}

	@Override
	public String toString() {
		return toSimpleString();
//		return "{ \"operation\": \""+OPERATION+"\", \"operationBody\":\n"+printBody()+"}\n";
	}
	
	public String toSimpleString(){
		return "INIT: " + getTarget().toSimpleString() + " <- " + Arrays.toString(getValue());
	}
	

}