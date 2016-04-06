package application.view;

import application.model.iModel;
import application.model.iStep;

public class View{
    private final iModel model;

    public View(iModel model){
        this.model = model;
    }

    public void render(){
        iStep currStep = model.getCurrentStep();
        //For each structure in the current step
        for (String id:currStep.getStructures().keySet()){
            if(currStep.getStructures().get(id))
        }


    }


}