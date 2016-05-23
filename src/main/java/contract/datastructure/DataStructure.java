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
import contract.operation.OperationCounter;
import contract.operation.OperationCounter.OperationCounterHaver;
import contract.operation.OperationType;
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

    /**
     * Version number for this class.
     */
    private static final long                         serialVersionUID = Const.VERSION_NUMBER;
    /**
     * The elements held by this DataStructure.
     */
    protected transient final ObservableList<Element> elements         = FXCollections.observableArrayList();
    /**
     * Elements which have been modified and should be drawn with their
     * preferred colour.
     */
    protected transient final ObservableList<Element> modifiedElements = FXCollections.observableArrayList();
    /**
     * Elements which are to be reset after being drawn with their preferred
     * colour.
     */
    protected transient final ObservableList<Element> resetElements    = FXCollections.observableArrayList();
    /**
     * Elements which should not be drawn or should be masked.
     */
    protected transient final ObservableList<Element> inactiveElements = FXCollections.observableArrayList();
    /**
     * If false, this entire DataStructure is considered inactive (as opposed to
     * just a single element).
     */
    private transient boolean                         active           = true;
    /**
     * Counter for operations performed on the structure.
     */
    protected transient final OperationCounter        oc               = new OperationCounter();

    @Override
    public OperationCounter getCounter () {
        return this.oc;
    }

    /**
     * Indicates that major changes have occurred, justifying a
     * re-initialisation.
     */
    public transient boolean repaintAll = false;

    public DataStructure (String identifier, RawType rawType, RawType.AbstractType abstractType, VisualType visual,
            Map<String, Object> attributes) {
        super(identifier, rawType, abstractType, visual, attributes);
    }

    /**
     * Returns the list of elements held by this DataStructure. Used when
     * drawing the elements.
     * 
     * @return The list of elements held by this DataStructure.
     */
    public ObservableList<Element> getElements () {
        return this.elements;
    }

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
        switch (op.operation) {
        case read:
        case write:
            this.executeRW((OP_ReadWrite) op);
            break;
        case swap:
            this.executeSwap((OP_Swap) op);
            break;
        case remove:
            this.executeRemove((OP_ToggleScope) op);
            return;
        default:
            Main.console.err("OperationType \"" + op.operation + "\" unknown.");
            break;
        }
        this.setActive(true);
    }

    /**
     * Execute a read/write operation.
     * 
     * @param op
     *            The operation to execute.
     */
    protected abstract void executeRW (OP_ReadWrite op);

    /**
     * Execute a swap operation.
     * 
     * @param op
     *            The operation to execute.
     */
    protected abstract void executeSwap (OP_Swap op);

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder();
        sb.append("\"" + Tools.stripQualifiers(this.identifier) + "\": " + this.rawType + " [");

        sb.append(this.abstractType + ", ");
        sb.append(this.visual);
        sb.append("]");

        return sb.toString();
    }

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

    /**
     * A list of elements which have been modified. They should be drawn with a
     * different colour. All elements which were in the list when this method is
     * called are copied to the list returned by {@code getResetElements}. The
     * list should be cleared manually once drawing is done.
     * 
     * @return A list of elements which have been modified.
     */
    public ObservableList<Element> getModifiedElements () {
        return this.modifiedElements;
    }

    /**
     * A list of elements which were modified but have already been drawn as
     * such. Their colours should be reset. As the list is never cleared, it
     * should be done manually once elements have been drawn.
     * 
     * @return A list of elements whose colour should be reset.
     */
    public ObservableList<Element> getResetElements () {
        return this.resetElements;
    }

    /**
     * Clear {@code modifierElements()}, {@code resetElements()} and
     * {@code inactiveElements()}lists.
     */
    public void clearElementLists () {
        this.modifiedElements.clear();
        this.resetElements.clear();
        this.inactiveElements.clear();
    }

    /**
     * Returns the list of inactive elements.
     * 
     * @return The list of inactive elements.
     */
    public ObservableList<Element> getInactiveElements () {
        return this.inactiveElements;
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
        this.resetElements.removeAll(this.modifiedElements);
        // Inactive elements should not have their colour reset.
        this.resetElements.removeAll(this.inactiveElements);

        for (Element e : this.resetElements) {
            e.setColor(paint);
        }

        this.resetElements.setAll(this.modifiedElements);
        this.modifiedElements.clear();
    }

    /**
     * Returns an element based on a Locator.
     * 
     * @param locator
     *            The locator for the Element.
     * @return An element if it could be found, null otherwise.
     */
    public abstract Element getElement (Locator locator);

    /**
     * Mark an element as inactive. If this method is called with an active
     * element as target, the element will be reactivated. If the target of the
     * Remove operation has an identifier equalling the identifier of this
     * DataStructure but no index, the entire structure will become inactive.
     * 
     * @param op
     *            A Remove operation.
     */
    protected void executeRemove (OP_ToggleScope op) {
        Locator target = op.getTarget();

        /*
         * Entire structure
         */
        if (target.identifier.equals(this.identifier) && target.index == null) {
            this.toggleActive();
            return;
        }

        /*
         * Single element
         */
        Element e = this.getElement(target);
        if (e != null) {
            if (this.inactiveElements.remove(e)) {
                this.resetElements.add(e); // Reactive element
            } else {
                this.inactiveElements.add(e);
                e.count(OperationType.remove);
            }
        }
    }

    /**
     * Returns True if this structure is active. False otherwise.
     * 
     * @return True if this structure is active.
     */
    public boolean isActive () {
        return this.active;
    }

    /**
     * Set the active status of this DataStructure.
     * 
     * @param value
     *            The new active status of this DataStructure.
     */
    public void setActive (boolean value) {
        if (value != this.active) {
            this.toggleActive();
        }
    }

    /**
     * Set the entire structure active or inactive
     */
    public void toggleActive () {
        this.active = !this.active;

        if (this.active) { // Reactive the structure.
            for (Element e : this.elements) {
                e.restoreValue();
            }
        } else { // Deactiveate the structure.
            for (Element e : this.elements) {
                e.count(OperationType.remove);
            }
        }
        this.repaintAll = true;
    }

    /**
     * Set the VisualType.
     * 
     * @param vt
     *            The new VisualType.
     */
    public void setVisual (VisualType vt) {
        if (vt != this.visual) {
            this.visual = vt;
            if (this.visualListener != null) {
                this.visualListener.visualChanged(vt);
            }
        }
    }

    private transient VisualListener visualListener;

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

    /**
     * Set a listener to be notified when the visual type changes.
     * 
     * @param listener
     *            A VisualListener.
     */
    public void setListener (VisualListener listener) {
        this.visualListener = listener;
    }

}
