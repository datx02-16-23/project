package wrapper.datastructures;

import java.util.ArrayList;
import java.util.List;

import application.assets.Strings;
import wrapper.AnnotatedVariable;
import wrapper.Operation;

/**
 * A data structure for use in visualisation.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class DataStructure extends AnnotatedVariable {

    /**
     * Version number for this class.
     */
    private static final long               serialVersionUID = Strings.VERSION_NUMBER;
    protected transient final List<Element> modifiedElements = new ArrayList<Element>();
    protected transient final List<Element> resetElements    = new ArrayList<Element>();
    protected transient final List<Element> inactiveElements    = new ArrayList<Element>();

    public DataStructure (String identifier, String rawType, String abstractType, String visual){
        super(identifier, rawType, abstractType, visual);
    }

    /**
     * Returns the list of elements held by this DataStructure. Used when drawing the elements.
     * 
     * @return The list of elements held by this DataStructure.
     */
    public abstract List<Element> getElements ();

    /**
     * Returns the number of elements held by this DataStructure.
     * 
     * @return The number of elements held by this DataStructure.
     */
    public abstract int size ();

    /**
     * Clear all set values in the structure
     */
    public abstract void clear ();

    /**
     * Apply an operation to the structure
     * 
     * @param op to be applied
     */
    public abstract void applyOperation (Operation op);

    @Override
    public String toString (){
        return super.identifier + ": " + super.rawType;
    }

    /**
     * Returns the default raw visual for this DataStructure. <br>
     * <b>NOTE:</b> Must never return {@code null}! Use {@code return "";} instead.
     * 
     * @return The default raw visual for this DataStructure.
     */
    public abstract String getRawVisual ();

    /**
     * Returns the default visual for the raw type held by this DataStructure. <br>
     * <b>NOTE:</b> Must never return {@code null}! Use {@code return "";} instead.
     * 
     * @return A visual style if available, null otherwise.
     */
    public abstract String getAbstractVisual ();

    /**
     * A list of elements which have been modified. They should be drawn with a different colour. All elements which
     * were in the list when this method is called are copied to the list returned by {@code getResetElements}. The list
     * should be cleared manually once drawing is done.
     * 
     * @return A list of elements which have been modified.
     */
    public List<Element> getModifiedElements (){
        return modifiedElements;
    }

    /**
     * A list of elements which were modified but have already been drawn as such. Their colours should be reset. As the
     * list is never cleared, it should be done manually once elements have been drawn.
     * 
     * @return A list of elements whose colour should be reset.
     */
    public List<Element> getResetElements (){
        return resetElements;
    }

    /**
     * Clear {@code modifierElements()} and {@code resetElements()} lists.
     */
    public void clearElementLists (){
        modifiedElements.clear();
        resetElements.clear();
        inactiveElements.clear();
    }
    
    public List<Element> getInactiveElements(){
        return inactiveElements;
    }

    /**
     * Indicate to the DataStructure that the lists returned by {@code getModifiedElements()} <b>and</b>
     * {@code getResetElements} have been drawn.
     */
    public void elementsDrawn (){
        resetElements.clear();
        resetElements.addAll(modifiedElements);
        modifiedElements.clear();
    }
}
