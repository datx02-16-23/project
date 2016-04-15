package wrapper.datastructures;

import java.util.ArrayList;
import java.util.List;

import application.assets.Strings;
import application.gui.Main;
import wrapper.Operation;
import wrapper.operations.*;

/**
 * An independent variable holding a single element. May for example be used as a temporary variable when performing a
 * swap. IndependentElement extends AnnotatedVariable, and is notably both an Element and a DataStructure.
 * 
 * @author Richard
 *
 */
public class IndependentElement extends DataStructure implements Element {

    /**
     * Version number for this class.
     */
    private static final long                  serialVersionUID = Strings.VERSION_NUMBER;
    private transient final ArrayList<Element> elements         = new ArrayList<Element>();
    //	private transient final Element element = new IndependentElementContainer();

    /**
     * Create a new IndependentElement.
     * 
     * @param identifier The identifier for this IndependentElement.
     * @param abstractType The <b>raw</b> type of the element held by this IndependenElement.
     * @param visual The preferred visual style of the IndependentElement.
     */
    public IndependentElement (String identifier, String abstractType, String visual){
        super(identifier, "independentElement", abstractType, visual);
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
     */
    public void initElement (){
        Element init = new Array.ArrayElement(1337, new int[] {1337});
        elements.clear();
        elements.add(init);
    }

    public List<Element> getElements (){
        return elements;
    }

    /**
     * Get the value held by the element contained in this IndependentElement.
     * 
     * @return The value held by the element contained in this IndependentElement.
     */
    public double getValue (){
        if (elements.isEmpty()) {
            return 0;
        }
        return elements.get(0).getValue();
    }

    public int size (){
        return 1;
    }

    private void init (OP_Init op_init){
        int[] index = {1337};
        elements.add(new Array.ArrayElement(op_init.getValue()[0], index));
    }

    @Override
    public void clear (){
        elements.clear();
    }

    @Override
    public void applyOperation (Operation op){
        switch (op.operation) {
            case init:
                init((OP_Init) op);
                break;
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
    }

    private void swap (OP_Swap op){
        if(elements.isEmpty()){
            return;
        }
        Element e = elements.get(0);
        if (op.getVar1().identifier.equals(this.identifier)) {
            e.setValue(op.getValues()[0]);
            e.setColor(COLOR_SWAP);
            return;
        }
        if (op.getVar2().identifier.equals(this.identifier)) {
            e.setValue(op.getValues()[1]);
            e.setColor(COLOR_SWAP);
            return;
        }
    }

    private void readORwrite (OP_ReadWrite op){
        if(elements.isEmpty()){
            return;
        }
        Element e = elements.get(0);
        if (op.getTarget().identifier.equals(this.identifier)) {
            e.setValue(op.getValue()[0]);
            modifiedElements.add(e);
            e.setColor(COLOR_WRITE);
            return;
        }
        else if (op.getSource().identifier.equals(this.identifier)) {
            modifiedElements.add(e);
            e.setColor(COLOR_READ);
        }
    }

    @Override
    public void setValue (double newValue){
        if(elements.isEmpty()){
            return;
        }
        elements.get(0).setValue(newValue);
    }

    @Override
    public String getRawVisual (){
        Main.console.err("WARNING: getRawVisual has not been implemented for Independent Element.");
        return "";
    }

    @Override
    public String getAbstractVisual (){
        Main.console.err("WARNING: getAbstractVisual has not been implemented for Independent Element.");
        return "";
    }

    @Override
    public String getColor (){
        return elements.get(0).getColor();
    }

    @Override
    public void setColor (String newColor){
        elements.get(0).setColor(newColor);
    }
}
