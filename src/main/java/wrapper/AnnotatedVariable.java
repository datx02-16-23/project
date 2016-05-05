package wrapper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import application.assets.Strings;
import application.visualization.VisualType;
import wrapper.datastructures.RawType;

/**
 * The declaration for an annotated (observed) variable in source.
 */
public class AnnotatedVariable implements Serializable {

    /**
     * Version number for this class.
     */
    private static final long            serialVersionUID = Strings.VERSION_NUMBER;
    /**
     * The identifier for this variable. For example, the indentifier for the variable created by the line
     * "{@code int myVar = 1337;}" would be "myVar".
     */
    public final String                  identifier;
    /**
     * The basic data structure for this variable. May be an array or something more complicated like a tree or linked
     * list.
     */
    public final RawType                 rawType;
    /**
     * The type of data structure this variable logically represents. For example, a variable could have the
     * {@code rawType} array but the {@code abstractType} binaryTree.
     */
    public RawType.AbstractType          abstractType;
    /**
     * The preferred graphical representation for this variable, such as BarChart.
     */
    public VisualType                    visual;
    /**
     * A map of attributes and their values for this variable, such as maximum size.
     */
    public final Map<String, Object> attributes;

    /**
     * Creates a new AnnotatedVariable.
     * 
     * @param identifier The identifier for this variable.
     * @param rawType The basic data structure for this variable.
     * @param abstractType The type of data structure this variable logically represents.
     * @param visual The preferred graphical representation for this variable.
     * @param attributes the map of attributes for this AnnotatedVariable.
     */
    public AnnotatedVariable (String identifier, RawType rawType, RawType.AbstractType abstractType, VisualType visual, Map<String, Object> attributes){
        if (identifier == null) {
            throw new NullPointerException("Identifier may not be null!");
        }
        this.identifier = identifier;
        this.rawType = rawType;
        this.abstractType = abstractType;
        this.visual = visual;
        this.attributes = attributes == null? new HashMap<String, Object>() : attributes;
    }

    public String toString (){
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName() + ": rawType = " + rawType + ", abstractType = " + abstractType + ", visual = " + visual);
        sb.append(", attributes = " + attributes);
        return sb.toString();
    }
}
