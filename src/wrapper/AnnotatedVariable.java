package wrapper;

import java.util.HashMap;

/**
 * The declaration for an annotated (observed) variable in source.
 */
public class AnnotatedVariable {
	
	/**
	 * The identifier for this variable. For example, the indentifier for the variable created
	 * by the line "{@code int myVar = 1337;}" would be "myVar".
	 */
	public final String identifier;
	/**
	 * The basic data structure for this variable. May be an array or something more complicated
	 * like a tree or linked list.
	 */
	public final String rawType;
	/**
	 * The type of data structure this variable logically represents. For example, a variable
	 * could have the {@code rawType} array but the {@code abstractType} binaryTree.
	 */
	public final String abstractType;
	/**
	 * The preferred graphical representation for this variable, such as BarChart.
	 */
	public final String visual;
	/**
	 * A map of attribute names and their values for this variable, such as maximum size.
	 */
	public final HashMap<String, Object> attributes;
	
	/**
	 * Creates a new AnnotatedVariable.
	 * @param identifier The identifier for this variable.
	 * @param rawType The basic data structure for this variable.
	 * @param abstractType The type of data structure this variable logically represents.
	 * @param visual The preferred graphical representation for this variable.
	 */
	public AnnotatedVariable(String identifier, String rawType, String abstractType, String visual){
		this.identifier = identifier;
		this.rawType = rawType;
		this.abstractType = abstractType;
		this.visual = visual;
		this.attributes = new HashMap<String, Object>();
	}
}
