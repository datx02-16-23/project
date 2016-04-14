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
    private transient Element                  element          = initElement();
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
        element = newElement;
        elements.clear();
        elements.add(newElement);
    }

    private Element initElement (){
        IndependentElementContainer init = new IndependentElementContainer();
        element = init;
        elements.clear();
        elements.add(init);
        return init;
    }

    /**
     * Returns the element held by this IndependentElement.
     * 
     * @return The element held by this IndependentElement.
     */
    public Element getElement (){
        return element;
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
        return element.getValue();
    }

    public int size (){
        return 1;
    }

    private void init (OP_Init op_init){
        if (!op_init.getTarget().equals(super.identifier)) {
            throw new IllegalArgumentException();
        }
        element.setValue(op_init.getValue()[0]);
    }

    @Override
    public void clear (){
        initElement();
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
        if (op.getVar1().identifier.equals(this.identifier)) {
            element.setValue(op.getValues()[0]);
            return;
        }
        if (op.getVar2().identifier.equals(this.identifier)) {
            element.setValue(op.getValues()[1]);
            return;
        }
    }

    private void readORwrite (OP_ReadWrite op){
        if (op.getTarget().identifier.equals(this.identifier)) {
            element.setValue(op.getValue()[0]);
            return;
        }
    }

    @Override
    public void setValue (double newValue){
        this.element.setValue(newValue);
    }

    public class IndependentElementContainer implements Element {

        private double value;

        public IndependentElementContainer (double value){
            this.value = value;
        }

        public IndependentElementContainer (){
            value = 0;
        }

        @Override
        public double getValue (){
            return value;
        }

        @Override
        public void setValue (double newValue){
            value = newValue;
        }
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
}
