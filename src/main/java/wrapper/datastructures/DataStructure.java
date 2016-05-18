package wrapper.datastructures;

import java.util.Map;
import application.assets.Strings;
import application.gui.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import wrapper.AnnotatedVariable;
import wrapper.Locator;
import wrapper.Operation;
import wrapper.operations.OP_ReadWrite;
import wrapper.operations.OP_Remove;
import wrapper.operations.OP_Swap;
import wrapper.operations.OperationCounter;
import wrapper.operations.OperationType;

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
	private static final long serialVersionUID = Strings.VERSION_NUMBER;
	/**
	 * The elements held by this DataStructure.
	 */
	protected transient final ObservableList<Element> elements = FXCollections.observableArrayList();
	/**
	 * Elements which have been modified and should be drawn with their
	 * preferred colour.
	 */
	protected transient final ObservableList<Element> modifiedElements = FXCollections.observableArrayList();
	/**
	 * Elements which are to be reset after being drawn with their preferred
	 * colour.
	 */
	protected transient final ObservableList<Element> resetElements = FXCollections.observableArrayList();
	/**
	 * Elements which should not be drawn or should be masked.
	 */
	protected transient final ObservableList<Element> inactiveElements = FXCollections.observableArrayList();
	/**
	 * If false, this entire DataStructure is considered inactive (as opposed to
	 * just a single element).
	 */
	private transient boolean active = true;
	/**
	 * The current background paint for elements with no preference.
	 */
	protected transient Paint backgroundColor;
	/**
	 * The preferred background paint for elements with no preference.
	 */
	protected transient final Color baseColor;
	/**
	 * Counter for operations performed on the structure.
	 */
	protected transient final OperationCounter oc = new OperationCounter();

	/**
	 * Returns the OperationCounter for this structure.
	 * 
	 * @return An OperationCounter.
	 */
	public OperationCounter getCounter() {
		return oc;
	}

	/**
	 * Number of children in KTree, row/vs column major etc.
	 */
	public transient int visualOption = 2;
	public transient boolean repaintAll = false;

	/**
	 * Create a new DataStructure.
	 * 
	 * @param identifier
	 * @param rawType
	 * @param abstractType
	 * @param visual
	 * @param attributes
	 * @param baseColor
	 */
	public DataStructure(String identifier, RawType rawType, RawType.AbstractType abstractType, VisualType visual,
			Map<String, Object> attributes, Color baseColor) {
		super(identifier, rawType, abstractType, visual, attributes);
		this.baseColor = baseColor;
		this.backgroundColor = baseColor;
	}

	/**
	 * Returns the list of elements held by this DataStructure. Used when
	 * drawing the elements.
	 * 
	 * @return The list of elements held by this DataStructure.
	 */
	public ObservableList<Element> getElements() {
		return elements;
	}

	/**
	 * Clear all set values in the structure
	 */
	public abstract void clear();

	/**
	 * Apply an operation to the structure
	 * 
	 * @param op
	 *            The operation to be apply.
	 */
	public void applyOperation(Operation op) {
		switch (op.operation) {
		case read:
		case write:
			executeRW((OP_ReadWrite) op);
			break;
		case swap:
			executeSwap((OP_Swap) op);
			break;
		case remove:
			executeRemove((OP_Remove) op);
			return;
		default:
			Main.console.err("OperationType \"" + op.operation + "\" unknown.");
			break;
		}
		setActive(true);
	}

	/**
	 * Execute a read/write operation.
	 * 
	 * @param op
	 *            The operation to execute.
	 */
	protected abstract void executeRW(OP_ReadWrite op);

	/**
	 * Execute a swap operation.
	 * 
	 * @param op
	 *            The operation to execute.
	 */
	protected abstract void executeSwap(OP_Swap op);

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.identifier + ": " + super.rawType);
		if (super.abstractType != null) {
			sb.append(" (" + super.abstractType + ")");
		}
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
	public abstract VisualType resolveVisual();

	/**
	 * A list of elements which have been modified. They should be drawn with a
	 * different colour. All elements which were in the list when this method is
	 * called are copied to the list returned by {@code getResetElements}. The
	 * list should be cleared manually once drawing is done.
	 * 
	 * @return A list of elements which have been modified.
	 */
	public ObservableList<Element> getModifiedElements() {
		return modifiedElements;
	}

	/**
	 * A list of elements which were modified but have already been drawn as
	 * such. Their colours should be reset. As the list is never cleared, it
	 * should be done manually once elements have been drawn.
	 * 
	 * @return A list of elements whose colour should be reset.
	 */
	public ObservableList<Element> getResetElements() {
		return resetElements;
	}

	/**
	 * Clear {@code modifierElements()}, {@code resetElements()} and
	 * {@code inactiveElements()}lists.
	 */
	public void clearElementLists() {
		modifiedElements.clear();
		resetElements.clear();
		inactiveElements.clear();
	}

	/**
	 * Returns the list of inactive elements.
	 * 
	 * @return The list of inactive elements.
	 */
	public ObservableList<Element> getInactiveElements() {
		return inactiveElements;
	}

	/**
	 * Indicate to the DataStructure that the lists returned by
	 * {@code getModifiedElements()} <b>and</b> {@code getResetElements} have
	 * been drawn.
	 * 
	 * @param color
	 *            The color to use for this element after reset. Null default to
	 *            white.
	 */
	public void elementsDrawn(Color color) {
		color = color == null ? Color.WHITE : color;

		// TODO
		for (Element e : getResetElements()) {
			if (getModifiedElements().contains(e) == false) {
				e.setColor(color);
			}
		}
		// resetElements.clear();
		resetElements.setAll(modifiedElements);
		modifiedElements.clear();
	}

	/**
	 * Returns an element based on a Locator.
	 * 
	 * @param locator
	 *            The locator for the Element.
	 * @return An element if it could be found, null otherwise.
	 */
	public abstract Element getElement(Locator locator);

	/**
	 * Mark an element as inactive. If this method is called with an active
	 * element as target, the element will be reactivated. If the target of the
	 * Remove operation has an identifier equaling the identifier of this
	 * DataStructure but no index, the entire structure will become inactive.
	 * 
	 * @param op
	 *            A Remove operation.
	 */
	protected void executeRemove(OP_Remove op) {
		Locator target = op.getTarget();

		/*
		 * Entire structure
		 */
		if (target.identifier.equals(this.identifier) && target.index == null) {
			toggleActive();
			return;
		}

		/*
		 * Single element
		 */
		Element e = this.getElement(target);
		if (e != null) {
			if (inactiveElements.remove(e)) {
				resetElements.add(e); // Reactive element
			} else {
				inactiveElements.add(e);
				e.count(op);
			}
		}
	}

	/**
	 * Toggle the active setting of this DataStructure.
	 */
	protected void toggleActive() {
		active = !active;
		repaintAll = true;
		if (active == false) {
			backgroundColor = OperationType.remove.paint;
		} else {
			backgroundColor = baseColor;
		}
	}

	/**
	 * Returns True if this structure is active. False otherwise.
	 * 
	 * @return True if this structure is active.
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Set the active status of this DataStructure.
	 * 
	 * @param value
	 *            The new active status of this DataStructure.
	 */
	public void setActive(boolean value) {
		if (value != active) {
			toggleActive();
		}
	}

	/**
	 * Returns the background Color to use for elements unaffected by recent
	 * operations.
	 * 
	 * @return A Color to use as background.
	 * 
	 */
	public Paint getDefaultElementBackground() {
		return backgroundColor;
	}

	/**
	 * Set the VisualType.
	 * 
	 * @param vt
	 *            The new VisualType.
	 */
	public void setVisual(VisualType vt) {
		if (vt != visual) {
			visual = vt;
			if (listener != null) {
				listener.visualChanged(vt);
			}
		}
	}

	private VisualListener listener;

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
		public void visualChanged(VisualType newVisual);
	}

	/**
	 * Set a listener to be notified when the visual type changes.
	 * 
	 * @param listener
	 *            A VisualListener.
	 */
	public void setListener(VisualListener listener) {
		this.listener = listener;
	}

}
