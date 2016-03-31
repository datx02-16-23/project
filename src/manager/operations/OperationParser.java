package manager.operations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;

import com.google.gson.internal.LinkedTreeMap;

import wrapper.Locator;
import wrapper.Operation;

/**
 * Contains methods to parse operations. Cannot be instantiated.
 * @author Richard
 *
 */
public class OperationParser {
	private static final String KEY_TARGET = "target";
	private static final String KEY_IDENTIFIER = "identifier";
	private static final String KEY_INDEX = "index";
	private static final String KEY_SOURCE = "source";
	private static final String KEY_VALUE = "value";
	private static final String KEY_SIZE = "size"; //TODO: Use.
	private static final String KEY_VAR1 = "var1";
	private static final String KEY_VAR2 = "var2";
	
	private OperationParser(){};
	
	
	public static Operation unpackOperation(Operation op){
		switch(op.operation){
			case read:
			case write:
				return parseReadWrite(op);
				
			case init:
				return parseInit(op);
			
			case message:
				return parseMessage(op);
				
			case swap:
				return parseSwap(op);
			
			default:
				System.out.print("Unknown operation type: " + op.operation);
				break;
		}
		return null;
	}

	
	@SuppressWarnings("unchecked")
	/**
	 * Unpack the arrayVariable used as target/source in some operations.
	 * @param arrayVariable The array variable to unpack.
	 * @return An array variable, if parsing was successful. Null otherwise.
	 */
	public static Locator unpackArrayVariable(Object arrayVariable){
		if (arrayVariable == null){
			return null;
		}
		LinkedTreeMap<String, Object> linkedTreeMap = (LinkedTreeMap<String, Object>)arrayVariable;
		
		
		Object identifier = linkedTreeMap.get(KEY_IDENTIFIER);
		if (identifier == null){
			return null; //No identifier -> return null.
		}
		
		Object indexAL = linkedTreeMap.get(KEY_INDEX);
		int[] index = null;
		
		if (indexAL != null){
			index = new int[((ArrayList<Double>) indexAL).size()];
			int i = 0;
			for (Double integer : (ArrayList<Double>) indexAL){
				index[i] = integer.intValue();
				i++;
			}
		}
		return new Locator((String)identifier, index);
	}
	
	public static OP_ReadWrite parseReadWrite(Operation op){
		OP_ReadWrite op_rw;
		if (op.operation.equals(Operations.write)){
			op_rw = new OP_Write();
		} else if (op.operation.equals(Operations.read)){
			op_rw = new OP_Read();
		} else {
			throw new IllegalArgumentException("Operation must be \"read\" or \"write\".");
		}
		op_rw.setSource(unpackArrayVariable(op.operationBody.get(KEY_SOURCE)));
		op_rw.setTarget(unpackArrayVariable(op.operationBody.get(KEY_TARGET)));
		op_rw.setValue(parseValue(op));

		return op_rw;
	}

	private static Operation parseSwap(Operation op) {
		OP_Swap op_swap = new OP_Swap();
		op_swap.setVar1(unpackArrayVariable(op.operationBody.get(KEY_VAR1)));
		op_swap.setVar2(unpackArrayVariable(op.operationBody.get(KEY_VAR2)));
		op_swap.setValues((String) op.operationBody.get(KEY_VALUE));
		return op_swap;
	}

	private static Operation parseMessage(Operation op) {
		OP_Message op_message = new OP_Message();
		op_message.setMessage((String) op_message.operationBody.get(KEY_VALUE));
		return op_message;
	}

	private static Operation parseInit(Operation op) {
		OP_Init op_init = new OP_Init();
		op_init.setSize(parseIndex(op));
		op_init.setTarget(unpackArrayVariable(op.operationBody.get(KEY_TARGET)));
		op_init.setValue(parseMultiValue(op));
		return op_init;
	}
	
	
	private static double[] parseMultiValue(Operation op) {
		@SuppressWarnings("unchecked")
		ArrayList<Object> nested = (ArrayList<Object>)op.operationBody.get(KEY_VALUE);
		if (nested == null){
			return null;
		}
		
		ArrayList<Double> simple = new ArrayList<Double>();	
		
		unwrapNestedList(nested, simple);
		System.out.println("simple = " + simple);
		return doubleListToDoubleArray(simple);
	}
	
	private static <T> void unwrapNestedList(ArrayList<Object> list, ArrayList<T> ack){
		if (list.isEmpty()){
			return;
		}
		Object firstElement = list.get(0);
		if (firstElement instanceof ArrayList){
			for(Object subList : list){
				unwrapNestedList((ArrayList<Object>) subList, ack);
			}
		} else {
			for(Object o : list){
				ack.add((T) o);
			}
		}
	}
	
	

	@SuppressWarnings("unchecked")
	private static int[] parseIndex(Operation op){
		return doubleListToIntArray((ArrayList<Double>)op.operationBody.get(KEY_INDEX));
	}
	
	@SuppressWarnings("unchecked")
	private static double[] parseValue(Operation op){
		return doubleListToDoubleArray((ArrayList<Double>)op.operationBody.get(KEY_VALUE));
	}
	
	private static double[] doubleListToDoubleArray(ArrayList<Double> listOfDoubles){
		if (listOfDoubles == null){
			return null;
		}
		
		double[] array = new double[listOfDoubles.size()];
		int i = 0;
		for(Double d : listOfDoubles){
			array[i] = d.doubleValue();
			i++;
		}
		return array;
	}
	
	private static int[] doubleListToIntArray(ArrayList<Double> listOfDoubles){
		if (listOfDoubles == null){
			return null;
		}
		int[] array = new int[listOfDoubles.size()];
		int i = 0;
		for(Double d : listOfDoubles){
			array[i] = d.intValue();
			i++;
		}
		return array;
	}
	
	public static double[] stringToDoubleArray(String str){
		str = str.substring(1, str.length()-1);
		String[] strs = str.split(",");
		double[] dbls = new double[strs.length];
		for(int i = 0; i < strs.length; i++){
			dbls[i] = Double.parseDouble(strs[i]);			
		}
		return dbls;
	}
	
}
