package application.visualization.render2d;

import wrapper.datastructures.Array.ArrayElement;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.IndependentElement;

public class IndependentElementRender extends BoxRender{
    
    

    public IndependentElementRender (DataStructure struct){
        super(struct);
        IndependentElement ie = (IndependentElement) struct;
        ArrayElement ae = new ArrayElement(ie.getElement().getValue(), null);
        ie.setElement(ae);
    }

    @Override
    public void render (){
        super.render();
    }}
