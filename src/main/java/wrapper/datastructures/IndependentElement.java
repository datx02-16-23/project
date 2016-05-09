package wrapper.datastructures;

import java.util.List;
import java.util.Map;

import application.assets.Strings;
import application.gui.Main;
import application.visualization.VisualType;
import javafx.scene.paint.Color;
import wrapper.Locator;
import wrapper.Operation;
import wrapper.operations.*;

/**
 * An independent variable holding a single element. May for example be used as a temporary variable when performing a
 * swap. IndependentElement extends AnnotatedVariable, and is notably both an Element and a DataStructure.
 * 
 * @author Richard Sundqvist
 *
 */
public class IndependentElement extends Array {

    /**
     * Version number for this class.
     */
    private static final long                  serialVersionUID = Strings.VERSION_NUMBER;
//    private transient final ArrayList<Element> elements         = new ArrayList<Element>();
    //	private transient final Element element = new IndependentElementContainer();

    /**
     * Create a new IndependentElement.
     * 
     * @param identifier The identifier for this IndependentElement.
     * @param abstractType The <b>raw</b> type of the element held by this IndependentElement.
     * @param visual The preferred visual style of the IndependentElement.
     */
    public IndependentElement (String identifier, RawType.AbstractType abstractType, VisualType visual, Map<String, Object> attributes){
        super(identifier, RawType.independentElement, abstractType, visual, attributes);
    }

    /**
     * Set the element held by this IndependentElement.
     * 
     * @param newElement The new element to be held by this IndependentElement.
     */
    public void setElement (Element newElement){
        elements.clear();
        elements.add(newElement);
    }

    /**
     * Initialize an element with value 0.
     * 
     * @param value The value to initialize with.
     */
    public void initElement (double value){
        Element init = new Array.ArrayElement(value, new int[]{0});
        elements.clear();
        elements.add(init);
    }

    /**
     * Get the value held by the element contained in this IndependentElement.
     * 
     * @return The value held by the element contained in this IndependentElement.
     */
    public double getNumericValue (){
        if (elements.isEmpty()) {
            return 0;
        }
        return elements.get(0).getNumericValue();
    }

    @Override
    public void clear (){
        elements.clear();
    }

    @Override
    public void applyOperation (Operation op){
        switch (op.operation) {
            case read:
            case write:
                readORwrite((OP_ReadWrite) op);
                break;
            case swap:
                swap((OP_Swap) op);
                break;
            default:
                Main.console.err("OperationType \"" + op.operation + "\" not applicable to " + getClass().getSimpleName());
                break;
        }
        repaintAll = true;
    }

    private void swap (OP_Swap op){
        Element e = elements.get(0);
        if (op.getVar1().identifier.equals(this.identifier)) {
            e.setNumericValue(op.getValue()[0]);
            e.setColor(OperationType.swap.color);
            return;
        }
        else if (op.getVar2().identifier.equals(this.identifier)) {
            e.setNumericValue(op.getValue()[1]);
            e.setColor(OperationType.swap.color);
            return;
        }
    }

    private void readORwrite (OP_ReadWrite op){
        if (elements.isEmpty()) {
            initElement(op.getValue()[0]);
        }
        Element e = elements.get(0);
        if (op.getTarget() != null && op.getTarget().identifier.equals(this.identifier)) {
            e.setNumericValue(op.getValue()[0]);
            modifiedElements.add(e);
            e.setColor(OperationType.write.color);
            return;
        }
        else if (op.getSource() != null && op.getSource().identifier.equals(this.identifier)) {
            modifiedElements.add(e);
            e.setColor(OperationType.read.color);
        }
    }

    public void setNumericValue (double newValue){
        if (elements.isEmpty()) {
            return;
        }
        elements.get(0).setNumericValue(newValue);
    }

    @Override
    public VisualType resolveVisual (){
        return VisualType.box;
    }

    public Color getColor (){
        return elements.get(0).getColor();
    }

    public void setColor (Color newColor){
        elements.get(0).setColor(newColor);
    }

    @Override
    public ArrayElement getElement (Locator locator){
        if(locator == null){
            return null;
        }
        if (locator.
                identifier.
                equals(
                        super.identifier) && 
                elements.isEmpty() == false) {
            return (ArrayElement) elements.get(0);
        }
        else {
            return null;
        }
    }
}
