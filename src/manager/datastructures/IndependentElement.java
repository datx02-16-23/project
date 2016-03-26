package manager.datastructures;

import java.util.ArrayList;
import java.util.List;

import wrapper.AnnotatedVariable;

/**
 * An independent variable holding a single element. May for example be used as a temporary variable when performing a swap.
 * IndependentElement extends AnnotatedVariable, and is notably both an Element and a DataStructure.
 * @author Richard
 *
 */
public class IndependentElement extends AnnotatedVariable implements Element, DataStructure{

	private transient Element element;
	private transient final ArrayList<Element> elements; 
	
	/**
	 * Create a new IndependentElement.
	 * @param identifier The identifier for this IndependentElement.
	 * @param abstractType The <b>raw</b> type of the element held by this IndependenElement.
	 * @param visual The preferred visual style of the IndependentElement.
	 */
	public IndependentElement(String identifier, String abstractType, String visual) {
		super(identifier, "independentElement", abstractType, visual);
		elements = new ArrayList<Element>();
	}

	/**
	 * Set the element held by this IndependentElement.
	 * @param newElement The new element to be held by this IndependentElement.
	 */
	public void setElement(Element newElement){
		element = newElement;
	}
	
	/**
	 * Returns the element held by this IndependentElement.
	 * @return The element held by this IndependentElement.
	 */
	public Element getElement(){
		return element;
	}
	
	public List<Element> getElements(){
		elements.clear();
		elements.add(element);
		return elements;
	}
	
	
	/**
	 * Get the value held by the element contained in this IndependentElement.
	 * @return The value held by the element contained in this IndependentElement.
	 */
	public double getValue() {
		return element.getValue();
	}
	
	public int size(){
		return 1;
	}
}
