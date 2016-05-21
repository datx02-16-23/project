package contract.operation;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.internal.LinkedTreeMap;

import contract.Locator;
import contract.Operation;
import gui.Main;

/**
 * Contains methods to parse operations. Cannot be instantiated.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class OperationParser {

	private OperationParser() {
	};

	public static Operation unpackOperation(Operation op) {
		switch (op.operation) {
		case read:
		case write:
			return parseReadWrite(op);
		case message:
			return parseMessage(op);
		case swap:
			return parseSwap(op);
		case remove:
			return parseRemove(op);
		default:
			Main.console.info("Unknown operation type: " + op.operation);
			break;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	/**
	 * Unpack the arrayVariable used as target/source in some operations.
	 * 
	 * @param arrayVariable
	 *            The array variable to unpack.
	 * @return An array variable, if parsing was successful. Null otherwise.
	 */
	public static Locator unpackArrayVariable(Object arrayVariable) {
		if (arrayVariable == null) {
			return null;
		}
		if (arrayVariable instanceof Locator) {
			return (Locator) arrayVariable;
		}
		LinkedTreeMap<String, Object> linkedTreeMap = (LinkedTreeMap<String, Object>) arrayVariable;
		Object identifier = linkedTreeMap.get(Key.identifier.toString());
		if (identifier == null) {
			return null; // No identifier -> return null.
		}
		Object indexAL = linkedTreeMap.get(Key.index.toString());
		int[] index = null;
		if (indexAL != null) {
			index = new int[((ArrayList<Double>) indexAL).size()];
			int i = 0;
			for (Double integer : (ArrayList<Double>) indexAL) {
				index[i] = integer.intValue();
				i++;
			}
		}
		return new Locator((String) identifier, index);
	}

	public static OP_ReadWrite parseReadWrite(Operation op) {
		OP_ReadWrite op_rw;
		if (op.operation == OperationType.write) {
			op_rw = new OP_Write();
		} else if (op.operation == OperationType.read) {
			op_rw = new OP_Read();
		} else {
			Main.console.err(("Operation must be \"read\" or \"write\". Got: " + op.operation));
			return null;
		}
		op_rw.setSource(unpackArrayVariable(op.operationBody.get(Key.source)));
		op_rw.setTarget(unpackArrayVariable(op.operationBody.get(Key.target)));
		op_rw.setValue(parseValue(op));
		copySourceInfo(op, op_rw);
		return op_rw;
	}

	public static OP_ToggleScope parseRemove(Operation op) {
		OP_ToggleScope op_remove = new OP_ToggleScope();
		op_remove.setTarget(unpackArrayVariable(op.operationBody.get(Key.target)));
		copySourceInfo(op, op_remove);
		return op_remove;
	}

	private static void copySourceInfo(Operation from, Operation to) {
		if (from.source == null) {
			return;
		}
		to.source = from.source;
		to.beginLine = from.beginLine;
		to.endLine = from.endLine;
		to.beginColumn = from.beginColumn;
		to.endColumn = from.endColumn;
	}

	private static Operation parseSwap(Operation op) {
		OP_Swap op_swap = new OP_Swap();
		op_swap.setVar1(unpackArrayVariable(op.operationBody.get(Key.var1)));
		op_swap.setVar2(unpackArrayVariable(op.operationBody.get(Key.var2)));
		op_swap.setValues(ensureDoubleArray(op.operationBody.get(Key.value)));
		copySourceInfo(op, op_swap);
		return op_swap;
	}

	private static Operation parseMessage(Operation op) {
		OP_Message op_message = new OP_Message();
		op_message.setMessage((String) op.operationBody.get(Key.value));
		copySourceInfo(op, op_message);
		return op_message;
	}

//	private static double[] parseMultiValue(Operation op) {
//		Object object = op.operationBody.get(Key.value);
//		if (object == null) {
//			return null;
//		}
//		if (object instanceof double[]) {
//			return (double[]) object;
//		}
//		@SuppressWarnings("unchecked")
//		ArrayList<Object> nested = (ArrayList<Object>) object;
//		ArrayList<Double> simple = new ArrayList<Double>();
//		unwrapNestedList(nested, simple);
//		return ensureDoubleArray(simple);
//	}

	/**
	 * Naive implementation to flatten a multi-dimensional list into a single
	 * dimension.
	 * 
	 * @param list
	 *            The multi-dimensional list to flatten.
	 * @param ack
	 *            The result.
	 */
//	@SuppressWarnings("unchecked")
//	private static <T> void unwrapNestedList(ArrayList<Object> list, ArrayList<T> ack) {
//		if (list.isEmpty()) {
//			return;
//		}
//		Object firstElement = list.get(0);
//		if (firstElement instanceof ArrayList) {
//			for (Object subList : list) {
//				unwrapNestedList((ArrayList<Object>) subList, ack);
//			}
//		} else {
//			for (Object o : list) {
//				ack.add((T) o);
//			}
//		}
//	}

	private static double[] parseValue(Operation op) {
		return ensureDoubleArray(op.operationBody.get(Key.value));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static double[] ensureDoubleArray(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof double[]) {
			return (double[]) object;
		}
		if (object instanceof Double) {
			return new double[] { (Double) object };
		}
		if (object instanceof ArrayList && ((ArrayList) object).get(0) instanceof ArrayList) {
			ArrayList<Double> fulhack = new ArrayList<Double>();
			for (List<Double> list : (ArrayList<ArrayList<Double>>) object) {
				for (Double d : list) {
					fulhack.add(d);
				}
			}
			double[] array = new double[fulhack.size()];
			int i = 0;
			for (Double d : fulhack) {
				array[i] = d.doubleValue();
				i++;
			}
			return array;
		} else {
			ArrayList<Double> listOfDoubles = (ArrayList<Double>) object;
			double[] array = new double[listOfDoubles.size()];
			int i = 0;
			for (Double d : listOfDoubles) {
				array[i] = d.doubleValue();
				i++;
			}
			return array;
		}
	}

	/**
	 * Convert a List of ints or doubles to an array of ints.
	 * 
	 * @param listOrArray
	 *            Should be an int array, or an ArrayList of Integers or
	 *            Doubles.
	 * @return An array of ints, or null.
	 */
	@SuppressWarnings("unchecked")
	public static int[] ensureIntArray(Object listOrArray) {
		if (listOrArray == null) {
			return null;
		}
		if (listOrArray instanceof int[]) {
			return (int[]) listOrArray;
		}
		ArrayList<Object> listOfNumbers = (ArrayList<Object>) listOrArray;
		// JSon will convert to Double, but if sent directly they will be
		// Integer.
		int[] array = new int[listOfNumbers.size()];
		int i = 0;
		if (listOfNumbers.isEmpty()) {
			return array;
		}
		Object testCase = listOfNumbers.get(0);
		// Integers
		if (testCase instanceof Integer) {
			for (Object d : listOfNumbers) {
				array[i] = ((Integer) d).intValue();
				i++;
			}
		} else {
			// Doubles
			for (Object d : listOfNumbers) {
				array[i] = ((Double) d).intValue();
				i++;
			}
		}
		return array;
	}

	public static double[] stringToDoubleArray(String str) {
		str = str.substring(1, str.length() - 1);
		String[] strs = str.split(",");
		double[] dbls = new double[strs.length];
		for (int i = 0; i < strs.length; i++) {
			dbls[i] = Double.parseDouble(strs[i]);
		}
		return dbls;
	}
}
