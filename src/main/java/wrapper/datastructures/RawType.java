package wrapper.datastructures;

/**
 * The raw type of the data structure.
 * 
 * @author Richard Sundqvist
 *
 */
public enum RawType{
    array(AbstractType.tree), //An array of objects or primitivtes.
    independentElement; //A loose element, such as a tmp variable.

    /**
     * The permitted AbstractTypes for this RawType.
     */
    public final AbstractType[] abstractTypes;

    private RawType (AbstractType... types){
        abstractTypes = types;
    }

    /**
     * The abstract type of the data structure, if applicable.
     * 
     * @author Richard Sundqvist
     *
     */
    public enum AbstractType{
        tree//A tree with n children and one parent.
    }
}
