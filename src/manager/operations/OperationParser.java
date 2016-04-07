package manager.operations;
import java.util.ArrayList;

import com.google.gson.internal.LinkedTreeMap;

import assets.Strings;
import wrapper.Locator;
import wrapper.Operation;

/**
 * Contains methods to parse operations. Cannot be instantiated.
 * @author Richard
 *
 */
public class OperationParser {
	
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
		if(arrayVariable instanceof Locator){
			return (Locator) arrayVariable;
		}
		LinkedTreeMap<String, Object> linkedTreeMap = (LinkedTreeMap<String, Object>)arrayVariable;
		
		
		Object identifier = linkedTreeMap.get(Strings.KEY_IDENTIFIER);
		if (identifier == null){
			return null; //No identifier -> return null.
		}
		
		Object indexAL = linkedTreeMap.get(Strings.KEY_INDEX);
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
		if (op.operation == Operations.write){
			op_rw = new OP_Write();
		} else if (op.operation == Operations.read){
			op_rw = new OP_Read();
		} else {
			System.err.println(("Operation must be \"read\" or \"write\". Got: " + op.operation));
			return null;
		}
		op_rw.setSource(unpackArrayVariable(op.operationBody.get(Strings.KEY_SOURCE)));
		op_rw.setTarget(unpackArrayVariable(op.operationBody.get(Strings.KEY_TARGET)));
		op_rw.setValue(parseValue(op));

		return op_rw;
	}

	private static Operation parseSwap(Operation op) {
		OP_Swap op_swap = new OP_Swap();
		op_swap.setVar1(unpackArrayVariable(op.operationBody.get(Strings.KEY_VAR1)));
		op_swap.setVar2(unpackArrayVariable(op.operationBody.get(Strings.KEY_VAR2)));
		op_swap.setValues((String) op.operationBody.get(Strings.KEY_VALUE));
		return op_swap;
	}

	private static Operation parseMessage(Operation op) {
		OP_Message op_message = new OP_Message();
		op_message.setMessage((String) op.operationBody.get(Strings.KEY_VALUE));
		return op_message;
	}

	private static Operation parseInit(Operation op) {
		OP_Init op_init = new OP_Init();
		op_init.setSize(
				ensureIntArray(
						op.operationBody.get(Strings.KEY_SIZE)
						)
				);
		op_init.setTarget(unpackArrayVariable(op.operationBody.get(Strings.KEY_TARGET)));
		op_init.setValue(parseMultiValue(op));
		return op_init;
	}
	
	
	private static double[] parseMultiValue(Operation op) {
		Object object = op.operationBody.get(Strings.KEY_VALUE);
		if (object == null){
			return null;
		}
		if (object instanceof double[]){
			return (double[])object;
		}
		
		ArrayList<Object> nested = (ArrayList<Object>) object;
		
		ArrayList<Double> simple = new ArrayList<Double>();	
		
		unwrapNestedList(nested, simple);
		return ensureDoubleArray(simple);
	}
	
	/**
	 * Naive implementation to flatten a multi-dimensional list into a single dimension.
	 * @param list The multi-dimensional list to flatten.
	 * @param ack The result.
	 */
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
	
	private static int[] parseIndex(Operation op){
		return ensureIntArray((ArrayList<Object>)op.operationBody.get(Strings.KEY_INDEX));
	}
	
	private static double[] parseValue(Operation op){
		return ensureDoubleArray(op.operationBody.get(Strings.KEY_VALUE));
	}
	
	private static double[] ensureDoubleArray(Object object){
		if (object == null){
			return null;
		}
		if(object instanceof double[]){
			return (double[]) object;
		}
		if(object instanceof Double){
			return new double[]{(Double) object};
		}
		
		ArrayList<Double> listOfDoubles = (ArrayList<Double>) object;
		double[] array = new double[listOfDoubles.size()];
		int i = 0;
		for(Double d : listOfDoubles){
			array[i] = d.doubleValue();
			i++;
		}
		return array;
	}
	
	/**
	 * What is returned depends heavily on how the data was received. If passed directly,
	 * it will be an likely be and array. If Processed by Gson, it will probably be an ArrayList<Double>.
	 * @param listOrArray Should be an int array, or an ArrayList of Integers or Doubles.
	 * @return An array of ints, or null.
	 */
	private static int[] ensureIntArray(Object listOrArray){
		if (listOrArray == null){
			return null;
		}
		
		if (listOrArray instanceof int[]){
			return (int[]) listOrArray;
		}
		ArrayList<Object> listOfNumbers = (ArrayList<Object>)listOrArray;
		//JSon will convert to Double, but if sent directly they will be Integer.

		int[] array = new int[listOfNumbers.size()];
		int i = 0;
		
		if (listOfNumbers.isEmpty()){
			return array;
		}
		Object testCase = listOfNumbers.get(0);
		//Integers
		if (testCase instanceof Integer){
			for(Object d : listOfNumbers){
				array[i] = ((Integer) d).intValue();
				i++;
			}
		} else {
			//Doubles
			for(Object d : listOfNumbers){
				array[i] = ((Double) d).intValue();
				i++;
			}
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
