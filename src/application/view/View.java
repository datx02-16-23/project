package application.view;

import application.model.iModel;
import application.model.iStep;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import manager.datastructures.DataStructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class View{
    private final iModel model;

    public View(iModel model){
        this.model = model;
    }

    public void render(Group group){
        //Do fake stuff with made up data to implement rendering side
        String id = "a1";
        List<Integer> values = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            values.add(i);
        }

        Canvas canvas = new Canvas(500, 300);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawArray(gc, values);
        group.getChildren().add(canvas);
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