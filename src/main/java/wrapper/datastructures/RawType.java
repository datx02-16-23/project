package wrapper.datastructures;

/**
 * The raw type of the data structure.
 * 
 * @author Richard Sundqvist
 *
 */
public enum RawType{
    array("Array", AbstractType.tree), //An array of objects or primitivtes.
    tree("Tree"),
    independentElement("Independent Element"); //A loose element, such as a tmp variable.

    /**
     * The permitted AbstractTypes for this RawType.
     */
    public final AbstractType[] absTypes;
    public final String         prettyName;

    private RawType (String prettyName, AbstractType... types){
        this.prettyName = prettyName;
        absTypes = types;
    }

    /**
     * The abstract type of the data structure, if applicable.
     * 
     * @author Richard Sundqvist
     *
     */
    public enum AbstractType{
        tree //A tree with n children and one parent.
    }
}
