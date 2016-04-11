package manager.operations;

import java.util.Arrays;
import java.util.HashMap;

import assets.Strings;
import wrapper.Locator;
import wrapper.Operation;


/**
 * Creates an operation to initialize an {@code AnnotatedVariable}.
 */
public class OP_Init extends Operation{
	/**
	 * Version number for this class.
	 */
	private static final long serialVersionUID = Strings.VERSION_NUMBER;
	
	private static final OperationType OPERATION = OperationType.init;

	/**
	 * Creates a new Init operation. Note that you must set the target, maxSize and initial values.
	 */
	public OP_Init() {
		super(OPERATION, new HashMap<Key, Object>());
	}
	
	/**
	 * Set the target variable for this Init operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param var The target variable for this Init operation.
	 */
	public void setTarget(Locator var){
		this.operationBody.put(Key.target, var);
	}
	
	/**
	 * Set the declared maximum size of this variable, for each dimension.
	 * @param size The declared maximum size of this variable.
	 */
	public void setSize(int [] size){
		this.operationBody.put(Key.size, size);
	}
	
	/**
	 * Set the value(s) with which to initialize this variable.
	 * @param value The value(s) with which to initialize this variable.
	 */
	public void setValue(double[] value){
		this.operationBody.put(Key.value, value);
	}
	
	public Locator getTarget(){
		return (Locator)this.operationBody.get(Key.target);
	}
	
	public int[] getSize(){
		return (int[])this.operationBody.get(Key.size);
	}
	
	public double[] getValue(){
		return (double[])this.operationBody.get(Key.value);
	}
	
	public String printBody(){
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		for( Key key : operationBody.keySet()){
			builder.append("\t\""+key+"\": ");
			switch(key){
				case target:
					builder.append("\t" + ((Locator)operationBody.get(key)).toString() +",\n");
				break;
				case size:
					builder.append("\t" + Arrays.toString((int[])operationBody.get(key))+",\n");
				break;
				case value:
					builder.append("\t" + Arrays.toString((double[])operationBody.get(key)) +",\n");
				break;
			default:
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