package wrapper.datastructures;

import wrapper.AnnotatedVariable;
import wrapper.operations.OP_Init;

/**
 * 
 * @author Richard
 *
 */
public class Array extends AnnotatedVariable{

	public Array(String identifier, String rawType, String abstractType, String visual) {
		super(identifier, rawType, abstractType, visual);
		
		if (!rawType.equals("array")){throw new IllegalArgumentException();}
	}
	
	public void init(OP_Init op_init){
		if (!op_init.getTarget().equals(super.identifier)){throw new IllegalArgumentException();}
	}

	/**
	 * 
	 * @author Richard
	 *
	 */
	public class ArrayElement{
		
	}
}
