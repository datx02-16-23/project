package manager.datastructures;

import wrapper.AnnotatedVariable;

/**
 * Contains methods to parse data structures. Cannot be instantiated.
 * @author Richard
 *
 */
public class DataStructureParser {
	private DataStructureParser(){};
	
	public static AnnotatedVariable unpackAnnotatedVariable(AnnotatedVariable av){
		switch(av.rawType){
			case "array":
				return unpackArray(av);
			case "tree":
				return null; //TODO: Add parsing of trees.
			default:
				System.out.print("Unknown data structure raw type: " + av.rawType);
				break;
		}
		return null;
	}
	
	public static Array unpackArray(AnnotatedVariable av){
		return new Array(av.identifier, av.abstractType, av.visual);
	}
}
