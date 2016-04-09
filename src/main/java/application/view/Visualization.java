package application.view;

import application.model.iModel;
import application.model.iStep;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import manager.datastructures.Array;
import manager.datastructures.DataStructure;
import manager.datastructures.Element;
import manager.datastructures.IndependentElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Visualization {
    private final iModel model;
    private final Group group;

    public Visualization(iModel model, Group group){
        this.model = model;
        this.group = group;
    }

    public void render(){
        group.getChildren().clear();
        Map<String, DataStructure> structs = model.getCurrentStep().getStructures();
        for (String id: structs.keySet()){
            renderStructure(id, structs.get(id));
        }

    }

    private void drawArray(Array array){
        System.out.println(group.getChildren().size());
        int width = array.size()*40;
        int height = 80;
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.strokeRect(5, 5, 5+width, 5+height);

        List<Element> elements = array.getElements();
        for(int i = 0; i < elements.size(); i++){

        }


        group.getChildren().add(canvas);

    }

    private void drawIndependentElement(IndependentElement element){
        System.out.println("Drawing independent element");
    }

    private void renderStructure(String id, DataStructure struct){
        Class structClass = struct.getClass();
        if (structClass.equals(Array.class)){
            drawArray((Array)struct);
        } else if (structClass.equals(IndependentElement.class)){
            drawIndependentElement((IndependentElement)struct);
        }

    }


}