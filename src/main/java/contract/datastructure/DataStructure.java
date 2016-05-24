package contract.datastructure;

import java.util.Map;

import assets.Const;
import assets.Tools;
import contract.AnnotatedVariable;
import contract.Locator;
import contract.Operation;
import contract.operation.OP_ReadWrite;
import contract.operation.OP_Swap;
import contract.operation.OP_ToggleScope;
import contract.operation.OperationType;
import contract.utility.OperationCounter;
import contract.utility.OperationCounter.OperationCounterHaver;
import gui.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * A data structure for use in visualisation.
 *
 * @author Richard Sundqvist
 *
 */
public abstract class DataStructure extends AnnotatedVariable implements OperationCounterHaver {

    // ============================================================= //
    /*
     *
     * Field variables - most should be transient!
     *
     */
    // ============================================================= //

    /**
     * Version number for this class.
     */
    private static final long                         serialVersionUID     = Const.VERSION_NUMBER;
    /**
     * The elements held by this DataStructure.
     */
    protected transient final ObservableList<Element> elements             = FXCollections.observableArrayList();
    /**
     * Elements which have been modified and should be drawn with their
     * preferred colour.
     */
    protected transient final ObservableList<Element> modifiedElements     = FXCollections.observableArrayList();
    /**
     * Elements which are to be reset after being drawn with their preferred
     * colour.
     */
    protected transient final ObservableList<Element> resetElements        = FXCollections.observableArrayList();
    /**
     * Elements which should not be drawn or should be masked.
     */
    protected transient final ObservableList<Element> inactiveElements     = FXCollections.observableArrayList();
    /**
     * If false, this entire DataStructure is considered inactive (as opposed to
     * just a single element).
     */
    private transient boolean                         active               = true;
    /**
     * Counter for operations performed on the structure.
     */
    protected transient final OperationCounter        oc                   = new OperationCounter();

    /**
     * Indicates that major changes have occurred, justifying a
     * re-initialisation.
     */
    protected transient boolean                       repaintAll           = false;

    /**
     * Indicates that this structure's {@link #applyOperation(Operation)} method
     * has been called. This variable is never reset.
     */
    protected transient boolean                       applyOperationCalled = false;

    // ============================================================= //
    /*
     *
     * Constructors
     *
     */
    // ============================================================= //

    /**
     * Creates a new DataStructure.
     *
     * @param identifier
     *            The identifier for this variable.
     * @param rawType
     *            The basic data structure for this variable.
     * @param abstractType
     *            The type of data structure this variable logically represents.
     * @param visual
     *            The preferred graphical representation for this variable.
     * @param attributes
     *            the map of attributes for this AnnotatedVariable.
     */
    public DataStructure (String identifier, RawType rawType, RawType.AbstractType abstractType, VisualType visual,
            Map<String, Object> attributes) {
        super(identifier, rawType, abstractType, visual, attributes);
    }

    // ============================================================= //
    /*
     *
     * Abstract Methods
     *
     */
    // ============================================================= //

    /**
     * Execute a swap operation.
     *
     * @param op
     *            The operation to execute.
     */
    protected abstract void executeSwap (OP_Swap op);

    /**
     * Execute a read/write operation.
     *
     * @param op
     *            The operation to execute.
     */
    protected abstract void executeRW (OP_ReadWrite op);

    /**
     * Returns an element based on a Locator.
     *
     * @param locator
     *            The locator for the Element.
     * @return An element if it could be found, null otherwise.
     */
    public abstract Element getElement (Locator locator);

    /**
     * Resolves the VisualType for this DataStructure. Will check {@code visual}
     * , {@code abstractType}, and {@code rawType}, in that order. This method
     * never may not null. <br>
     * <br>
     * <b>NOTE:</b> Implementations should call setVisual() to notify listeners
     * of any changes.
     *
     * @return The Visual to use for this DataStructure.
     */
    public abstract VisualType resolveVisual ();

    // ============================================================= //
    /*
     *
     * Control
     *
     */
    // ============================================================= //

    /**
     * Clear all set values in the structure
     */
    public abstract void clear ();

    /**
     * Apply an operation to the structure
     *
     * @param op
     *            The operation to be apply.
     */
    public void applyOperation (Operation op) {
        this.applyOperationCalled = true;

        switch (op.operation) {
        case read:
        case write:
            executeRW((OP_ReadWrite) op);
            break;
        case swap:
            executeSwap((OP_Swap) op);
            break;
        case remove:
            toggleScope((OP_ToggleScope) op);
            return;
        default:
            Main.console.err("OperationType \"" + op.operation + "\" unknown.");
            break;
        }
        setActive(true);
    }

    /**
     * Indicate to the DataStructure that the lists returned by
     * {@code getModifiedElements()} <b>and</b> {@code getResetElements} have
     * been drawn.
     *
     * @param paint
     *            The color to use for this element after reset. Null defaults
     *            to white.
     */
    public void elementsDrawn (Paint paint) {
        paint = paint == null ? Color.WHITE : paint;

        // Do not reset elements which were modified again.
        resetElements.removeAll(modifiedElements);
        // Inactive elements should not have their colour reset.
        resetElements.removeAll(inactiveElements);

        for (Element e : resetElements) {
            e.setColor(paint);
        }

        resetElements.setAll(modifiedElements);
        modifiedElements.clear();
    }

    /**
     * Mark an element as inactive. If this method is called with an active
     * element as target, the element will be reactivated. If the target of the
     * Remove operation has an identifier equalling the identifier of this
     * DataStructure but no index, the entire structure will become inactive.
     *
     * @param op
     *            A Remove operation.
     */
    protected void toggleScope (OP_ToggleScope op) {
        Locator target = op.getTarget();

        /*
         * Entire structure
         */
        if (target.identifier.equals(identifier) && target.index == null) {
            toggleActive();
            return;
        }

        /*
         * Single element
         */
        Element e = getElement(target);
        if (e != null) {
            if (inactiveElements.remove(e)) {
                resetElements.add(e); // Reactive element
            } else {
                inactiveElements.add(e);
                e.count(OperationType.remove);
            }
        }
    }

    // ============================================================= //
    /*
     *
     * Getters and Setters
     *
     */
    // ============================================================= //

    /**
     * Returns the list of elements held by this DataStructure. Used when
     * drawing the elements.
     *
     * @return The list of elements held by this DataStructure.
     */
    public ObservableList<Element> getElements () {
        return elements;
    }

    /**
     * A list of elements which have been modified. They should be drawn with a
     * different colour. All elements which were in the list when this method is
     * called are copied to the list returned by {@code getResetElements}. The
     * list should be cleared manually once drawing is done.
     *
     * @return A list of elements which have been modified.
     */
    public ObservableList<Element> getModifiedElements () {
        return modifiedElements;
    }

    /**
     * A list of elements which were modified but have already been drawn as
     * such. Their colours should be reset. As the list is never cleared, it
     * should be done manually once elements have been drawn.
     *
     * @return A list of elements whose colour should be reset.
     */
    public ObservableList<Element> getResetElements () {
        return resetElements;
    }

    /**
     * Returns the list of inactive elements.
     *
     * @return The list of inactive elements.
     */
    public ObservableList<Element> getInactiveElements () {
        return inactiveElements;
    }

    /**
     * Clear {@code modifierElements()}, {@code resetElements()} and
     * {@code inactiveElements()}lists.
     */
    public void clearElementLists () {
        modifiedElements.clear();
        resetElements.clear();
        inactiveElements.clear();
    }

    /**
     * Set the VisualType.
     *
     * @param vt
     *            The new VisualType.
     */
    public void setVisual (VisualType vt) {
        if (vt != visual) {
            visual = vt;
            if (visualListener != null) {
                visualListener.visualChanged(vt);
            }
        }
    }

    /**
     * Indiciates whether the structure should be completely redrawn.
     * 
     * @return {@code true} if the data structure needs a complete rerender.
     */
    public boolean isRepaintAll () {
        return repaintAll;
    }

    /**
     * Set the flag indicate whether this structure has changed so significantly
     * that all elements should be drawn again.
     * 
     * @param repaintAll
     *            The new repaint status for the structure.
     */
    public void setRepaintAll (boolean repaintAll) {
        this.repaintAll = repaintAll;
    }

    /**
     * {@code true} indicates that this structure's
     * {@link #applyOperation(Operation)} method has been called. This variable
     * is never reset.
     * 
     * @return {@code true} if this data structure has been accessed using
     *         {@code applyOperation ()}.
     */
    public boolean isApplyOperationCalled () {
        return applyOperationCalled;
    }

    /**
     * Returns True if this structure is active. False otherwise.
     *
     * @return True if this structure is active.
     */
    public boolean isActive () {
        return active;
    }

    /**
     * Set the active status of this DataStructure.
     *
     * @param value
     *            The new active status of this DataStructure.
     */
    public void setActive (boolean value) {
        if (value != active) {
            toggleActive();
        }
    }

    /**
     * Set the entire structure active or inactive
     */
    public void toggleActive () {
        active = !active;

        if (active) { // Reactive the structure.
            for (Element e : elements) {
                e.restoreValue();
            }
        } else { // Deactiveate the structure.
            for (Element e : elements) {
                e.count(OperationType.remove);
            }
        }
        setRepaintAll(true);
    }

    /**
     * Set a listener to be notified when the visual type changes.
     *
     * @param listener
     *            A VisualListener.
     */
    public void setVisualListener (VisualListener listener) {
        visualListener = listener;
    }

    @Override public String toString () {
        StringBuilder sb = new StringBuilder();
        sb.append("\"" + Tools.stripQualifiers(identifier) + "\": " + rawType + " [");

        sb.append(abstractType + ", ");
        sb.append(visual);
        sb.append("]");

        return sb.toString();
    }

    // ============================================================= //
    /*
     *
     * Interfaces
     *
     */
    // ============================================================= //

    @Override public OperationCounter getCounter () {
        return oc;
    }

    /**
     * Interface for listening to changes in the VisualType.
     *
     * @author Richard Sundqvist
     *
     */

    public static interface VisualListener {
        /**
         * Called when the VisualType of the DataStructure changes.
         *
         * @param newVisual
         *            The new VisualType.
         */
        public void visualChanged (VisualType newVisual);
    }

    private transient VisualListener visualListener;

}
