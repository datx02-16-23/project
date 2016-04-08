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

	/**
	 * Creates a new Init operation. Note that you must set the target, maxSize and initial values.
	 */
	public OP_Init() {
		super(OPERATION, new HashMap<OperationsBody, Object>());
	}
	
	/**
	 * Set the target variable for this Init operation.
	 * The identifier of the variable should be previously declared in the header.
	 * @param var The target variable for this Init operation.
	 */
	public void setTarget(Locator var){
		this.operationBody.put(OperationsBody.TARGET, var);
	}
	
	/**
	 * Set the declared maximum size of this variable, for each dimension.
	 * @param size The declared maximum size of this variable.
	 */
	public void setSize(int [] size){
		this.operationBody.put(OperationsBody.SIZE, size);
	}
	
	/**
	 * Set the value(s) with which to initialize this variable.
	 * @param value The value(s) with which to initialize this variable.
	 */
	public void setValue(double[] value){
		this.operationBody.put(OperationsBody.VALUE, value);
	}
	
	public Locator getTarget(){
		return (Locator)this.operationBody.get(OperationsBody.TARGET);
	}
	
	public int[] getSize(){
		return (int[])this.operationBody.get(OperationsBody.SIZE);
	}
	
	public double[] getValue(){
		return (double[])this.operationBody.get(OperationsBody.VALUE);
	}
	
	public String printBody(){
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		for( OperationsBody key : operationBody.keySet()){
			builder.append("\t\""+key+"\": ");
			switch(key){
				case TARGET:
					builder.append("\t" + ((Locator)operationBody.get(key)).toString() +",\n");
				break;
				case SIZE:
					builder.append("\t" + Arrays.toString((int[])operationBody.get(key))+",\n");
				break;
				case VALUE:
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