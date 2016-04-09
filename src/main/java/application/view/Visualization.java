package application.view;

import application.model.iModel;
import application.model.iStep;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import manager.datastructures.DataStructure;

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
        for (DataStructure struct: structs.values()){
            System.out.println(struct.toString());
        }

    }

    private void drawArray(GraphicsContext gc, List<Integer> values){
        gc.setFont(new Font(30));
        int width = values.size()*40;
        int height = 80;
        gc.strokeRect(0, 0, width, height);
        for (int i = 0; i < values.size(); i++){
            gc.fillText(values.get(i).toString(), width/values.size()*i + 20, 60);
        //    gc.strokeLine(10, 10, );
        }

    }

    private void renderStructure(Group group, String id, DataStructure struct){

    }


}