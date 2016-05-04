package wrapper.datastructures;

import java.util.ArrayList;
import java.util.List;

import application.assets.Strings;
import application.visualization.VisualType;
import wrapper.AnnotatedVariable;
import wrapper.Locator;
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

    /**
     * The elements held by this DataStructure.
     */
    protected transient final List<Element> elements = new ArrayList<Element>();
    /**
     * Elements which have been modified and should be drawn with their preferred color.
     */
    protected transient final List<Element> modifiedElements = new ArrayList<Element>();
    /**
     * Elements which are to be reset after being drawn with their preferred color.
     */
    protected transient final List<Element> resetElements    = new ArrayList<Element>();
    /**
     * Elements which should not be drawn or should be masked.
     */
    protected transient final List<Element> inactiveElements = new ArrayList<Element>();
    /**
     * Number of children in KTree, row/vs column major etc.
     */
    public transient int                    visualOption    = 2;
    public transient boolean                repaintAll       = false;

    public DataStructure (String identifier, RawType rawType, RawType.AbstractType abstractType, VisualType visual){
        super(identifier, rawType, abstractType, visual);
    }

    /**
     * Returns the list of elements held by this DataStructure. Used when drawing the elements.
     * 
     * @return The list of elements held by this DataStructure.
     */
    public abstract List<Element> getElements ();

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
        StringBuilder sb = new StringBuilder();
        sb.append(super.identifier + ": " + super.rawType);
        if (super.abstractType != null) {
            sb.append(" (" + super.abstractType + ")");
        }
        return sb.toString();
    }

    /**
     * Resolves the VisualType for this DataStructure. Will filter {@code visual, abstractType} and {@code rawType},
     * in that order. This method never returns null.
     * 
     * @return The Visual to use for this DataStructure.
     */
    public abstract VisualType resolveVisual ();

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

    public List<Element> getInactiveElements (){
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

    /**
     * Returns an element based on a Locator.
     * 
     * @param locator The locator to look for.
     * @return An element if it could be found, null otherwise.
     */
    public abstract Element getElement (Locator locator);
}
