package application.view;

import application.model.iModel;
import application.model.iStep;
import javafx.scene.Group;
import manager.datastructures.DataStructure;

import java.util.Set;

public class View{
    private final iModel model;

    public View(iModel model){
        this.model = model;
    }

    public void render(Group group){
        iStep currStep = model.getCurrentStep();
        Set<String> ids = currStep.getStructures().keySet();
        for (String id:ids){
            //Render
        }
    }

    private void renderStructure(Group group, String id, DataStructure struct){

    }


}