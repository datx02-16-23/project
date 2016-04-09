package application.view;

import application.model.iModel;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import manager.datastructures.Array;
import manager.datastructures.DataStructure;
import manager.datastructures.Element;
import manager.datastructures.IndependentElement;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Visualization extends Pane {
    private final iModel model;
    private final Canvas canvas = new Canvas();
    private final int structHeight = 100;

    public Visualization(iModel model){
        this.model = model;
        getChildren().add(canvas);
    }

    @Override
    protected void layoutChildren() {
        final int top = (int)snappedTopInset();
        final int right = (int)snappedRightInset();
        final int bottom = (int)snappedBottomInset();
        final int left = (int)snappedLeftInset();
        final int w = (int)getWidth() - left - right;
        final int h = (int)getHeight() - top - bottom;
        canvas.setLayoutX(left);
        canvas.setLayoutY(top);
        if (w != canvas.getWidth() || h != canvas.getHeight()) {
            canvas.setWidth(w);
            canvas.setHeight(h);
        }
        render();
    }

    private void clear(){
        final int top = (int)snappedTopInset();
        final int right = (int)snappedRightInset();
        final int bottom = (int)snappedBottomInset();
        final int left = (int)snappedLeftInset();
        final int w = (int)getWidth() - left - right;
        final int h = (int)getHeight() - top - bottom;
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);
    }

    public void render(){
        clear();
        Map<String, DataStructure> structs = model.getCurrentStep().getStructures();
        Iterator<String> structNames = structs.keySet().iterator();
        int numStruct = 0;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);

        while (structNames.hasNext()){
            String structName = structNames.next();
            gc.fillText(structName, 20, 20 + 100*numStruct);
            numStruct++;
            //renderStructure(id, structs.get(id));
        }

    }

    private void drawArray(Array array){
        int width = array.size()*40;
        int height = 80;
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.strokeRect(5, 5, 5+width, 5+height);

        List<Element> elements = array.getElements();
        for(int i = 0; i < elements.size(); i++){

        }

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