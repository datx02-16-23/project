package wrapper.datastructures;

import java.util.ArrayList;

import application.gui.Main;
import wrapper.AnnotatedVariable;
import wrapper.operations.Key;
import wrapper.operations.OperationParser;

/**
 * Contains methods to parse data structures. Cannot be instantiated.
 * 
 * @author Richard
 *
 */
public class DataStructureParser {

    private DataStructureParser (){
    };

    public static DataStructure unpackAnnotatedVariable (AnnotatedVariable av){
        switch (av.rawType) {
            case "array":
                return unpackArray(av);
            case "independentElement":
                return unpackIndependentElement(av);
            case "tree":
                return null; //TODO: Add parsing of trees.
            default:
                Main.console.out("Unknown data structure raw type: " + av.rawType);
                break;
        }
        return null;
    }

    /**
     * Unpack an IndependentElement data structure variable.
     * 
     * @param av The variable to unpack.
     * @return An unpacked IndependentElement.
     */
    private static IndependentElement unpackIndependentElement (AnnotatedVariable av){
        return new IndependentElement(av.identifier, av.abstractType, av.visual);
    }

    /**
     * Unpack an Array data structure variable.
     * 
     * @param av The variable to unpack.
     * @return An unpacked Array.
     */
    public static Array unpackArray (AnnotatedVariable av){
        return new Array(av.identifier, av.abstractType, av.visual);
    }
    
    @SuppressWarnings("unchecked")
    public static int[] parseSize (AnnotatedVariable av){
        return OperationParser.ensureIntArray((ArrayList<Object>) av.attributes.get(Key.size));
    }
}
