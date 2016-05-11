package wrapper.datastructures;

import java.util.Map;

import application.assets.Strings;
import application.gui.Main;
import application.visualization.VisualType;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import wrapper.AnnotatedVariable;
import wrapper.Locator;
import wrapper.Operation;
import wrapper.operations.OP_Remove;
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
	 * If True, this entire DataStructure is considered inactive (as opposed to
	 * just a single element).
	 */
	protected transient boolean inactive = false;
	/**
	 * The current background color for elements with no preference.
	 */
	protected transient Color backgroundColor;
	/**
	 * The preferred background color for elements with no preference.
	 */
	protected transient final Color baseColor;
	protected transient final SimpleIntegerProperty numReads = new SimpleIntegerProperty(0);

	/**
	 * Returns the SimpleIntegerProperty counting number of Read operations
	 * performed on this DataStructure.
	 * 
	 * @return A SimpleIntegerProperty.
	 */
	public SimpleIntegerProperty getNumReads() {
		return numReads;
	}

	/**
	 * Returns the SimpleIntegerProperty counting number of Write operations
	 * performed on this DataStructure.
	 * 
	 * @return A SimpleIntegerProperty.
	 */
	public SimpleIntegerProperty getNumWrites() {
		return numWrites;
	}

	/**
	 * Returns the SimpleIntegerProperty counting number of Swap operations
	 * performed on this DataStructure.
	 * 
	 * @return A SimpleIntegerProperty.
	 */
	public SimpleIntegerProperty getNumSwaps() {
		return numSwaps;
	}

	protected transient final SimpleIntegerProperty numWrites = new SimpleIntegerProperty(0);
	protected transient final SimpleIntegerProperty numSwaps = new SimpleIntegerProperty(0);
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
	public abstract void applyOperation(Operation op);

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
	 * Resolves the VisualType for this DataStructure. Will filter
	 * {@code visual, abstractType} and {@code rawType}, in that order. This
	 * method never returns null.
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
	 */
	public void elementsDrawn() {
		resetElements.clear();
		resetElements.addAll(modifiedElements);
		modifiedElements.clear();
	}

	/**
	 * Returns an element based on a Locator.
	 * 
	 * @param locator
	 *            The locator to look for.
	 * @return An element if it could be found, null otherwise.
	 */
	public abstract Element getElement(Locator locator);

	/**
	 * Mark an element as inactive. If this method is called
	 * 
	 * @param op
	 *            A Remove operation.
	 */
	protected void remove(OP_Remove op) {
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
				e.setColor(OperationType.remove.color);
			}
		}
	}

	/**
	 * Toggle the active setting of this DataStructure.
	 */
	protected void toggleActive() {
		inactive = !inactive;
		repaintAll = true;
		if (inactive) {
			backgroundColor = OperationType.remove.color;
		} else {
			backgroundColor = baseColor;
		}
	}
	
	/**
	 * Returns True if this structure is active. False otherwise.
	 * @return True if this structure is active.
	 */
	public boolean isActive(){
		return !inactive;
	}

	/**
	 * Returns the background Color to use for regular elements unaffected by recent operations.
	 * @return A Color to use as background.
	 * 
	 */
	public Color getDefaultElementBackground() {
		return backgroundColor;
	}
}
