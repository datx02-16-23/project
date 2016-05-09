package wrapper.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import application.assets.Strings;
import application.visualization.VisualType;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private static final long                         serialVersionUID = Strings.VERSION_NUMBER;
    /**
     * The elements held by this DataStructure.
     */
    protected transient final ObservableList<Element> elements         = FXCollections.observableArrayList();
    /**
     * Elements which have been modified and should be drawn with their preferred colour.
     */
    protected transient final ObservableList<Element> modifiedElements = FXCollections.observableArrayList();
    /**
     * Elements which are to be reset after being drawn with their preferred colour.
     */
    protected transient final ObservableList<Element> resetElements    = FXCollections.observableArrayList();
    /**
     * Elements which should not be drawn or should be masked.
     */
    protected transient final ObservableList<Element> inactiveElements = FXCollections.observableArrayList();
    protected transient final SimpleIntegerProperty   numReads         = new SimpleIntegerProperty(0);
    
    /**
     * Returns the SimpleIntegerProperty counting number of Read operations performed on this DataStructure.
     * @return A SimpleIntegerProperty.
     */
    public SimpleIntegerProperty getNumReads (){
        return numReads;
    }

    /**
     * Returns the SimpleIntegerProperty counting number of Write operations performed on this DataStructure.
     * @return A SimpleIntegerProperty.
     */
    public SimpleIntegerProperty getNumWrites (){
        return numWrites;
    }

    /**
     * Returns the SimpleIntegerProperty counting number of Swap operations performed on this DataStructure.
     * @return A SimpleIntegerProperty.
     */
    public SimpleIntegerProperty getNumSwaps (){
        return numSwaps;
    }

    protected transient final SimpleIntegerProperty   numWrites        = new SimpleIntegerProperty(0);
    protected transient final SimpleIntegerProperty   numSwaps         = new SimpleIntegerProperty(0);
    /**
     * Number of children in KTree, row/vs column major etc.
     */
    public transient int                              visualOption     = 2;
    public transient boolean                          repaintAll       = false;

    public DataStructure (String identifier, RawType rawType, RawType.AbstractType abstractType, VisualType visual, Map<String, Object> attributes){
        super(identifier, rawType, abstractType, visual, attributes);
    }

    /**
     * Returns the list of elements held by this DataStructure. Used when drawing the elements.
     * 
     * @return The list of elements held by this DataStructure.
     */
    public ObservableList<Element> getElements (){
        return elements;
    }

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
     * Resolves the VisualType for this DataStructure. Will filter {@code visual, abstractType} and {@code rawType}, in
     * that order. This method never returns null.
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
    public ObservableList<Element> getModifiedElements (){
        return modifiedElements;
    }

    /**
     * A list of elements which were modified but have already been drawn as such. Their colours should be reset. As the
     * list is never cleared, it should be done manually once elements have been drawn.
     * 
     * @return A list of elements whose colour should be reset.
     */
    public ObservableList<Element> getResetElements (){
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

    public ObservableList<Element> getInactiveElements (){
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
