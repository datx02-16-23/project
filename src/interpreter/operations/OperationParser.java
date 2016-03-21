package interpreter.operations;
import java.util.ArrayList;

import org.omg.Messaging.SyncScopeHelper;

import com.google.gson.internal.LinkedTreeMap;

import interpreter.wrapper.ArrayVariable;
import interpreter.wrapper.Operation;

public class OperationParser {
	private static final String KEY_TARGET = "target";
	private static final String KEY_IDENTIFIER = "identifier";
	private static final String KEY_INDEX = "index";
	private static final String KEY_SOURCE = "source";
	private static final String KEY_VALUE = "value";
	
	private OperationParser(){};
	
	public static ArrayVariable unpackArrayVariable(Object arrayVariable){
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
		return new ArrayVariable((String)identifier, index);
	}
	
	public static OP_ReadWrite parseReadWrite(Operation op){
		OP_ReadWrite op_rw;
		if (op.operation.equals("write")){
			op_rw = new OP_Write();
		} else if (op.operation.equals("read")){
			op_rw = new OP_Read();
		} else {
			throw new IllegalArgumentException("Operation must be \"read\" or \"write\".");
		}
		op_rw.setSource(unpackArrayVariable(op.operationBody.get(KEY_SOURCE)));
		op_rw.setTarget(unpackArrayVariable(op.operationBody.get(KEY_TARGET)));
		op_rw.setValue((String)op.operationBody.get(KEY_VALUE));

		return op_rw;
	}
	
	private static Operation unpackOperation(Operation op){
		//TODO: Set public. Add unpack operations to OP_Read, OP_Write, OP_Init, OP_Message.
		switch(op.operation){
			case "read":
				break;
			
			case "write":
				break;
				
			case "init":
				break;
			
			case "message":
				break;
			
			default:
				System.out.print("Unknown operation type: " + op);
				break;
		}
		return null;
	}
	
}
