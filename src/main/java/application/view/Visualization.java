package application.view;

import application.model.iModel;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import manager.datastructures.Array;
import manager.datastructures.DataStructure;
import manager.datastructures.Element;
import manager.datastructures.IndependentElement;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Visualization extends Pane {
    private final iModel model;
    private final Canvas canvas = new Canvas();
    private final int structHeight = 100;
    private final double headerTextSize = 12;

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
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void render(){
        clear();
        Map<String, DataStructure> structs = model.getCurrentStep().getStructures();
        Iterator<String> structNames = structs.keySet().iterator();
        int numStruct = 0;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);

        while (structNames.hasNext()){
            final int x = 0;
            final int y = 0 + structHeight*numStruct;
            final int width = (int)canvas.getWidth();
            final String id = structNames.next();
            gc.setFont(new Font(headerTextSize));
            gc.fillText(generateStructHeader(id, structs.get(id)), 20 + x, 20 + y);
            numStruct++;
            renderStructure(structs.get(id), x, y, width, structHeight);
            gc.strokeLine(0, structHeight*numStruct, canvas.getWidth(), structHeight*numStruct);

        }

    }

    private String generateStructHeader(String id, DataStructure struct){
        StringBuilder sB = new StringBuilder();
        sB.append("Identifier: ");
        sB.append(id);
        sB.append("\t");
        sB.append("Type: ");
        sB.append(struct.rawType);
        return sB.toString();
    }

    private void drawArray(Array array, int x, int y, int width, int height){
        int elemWidth = 40;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);

        List<Element> elements = array.getElements();
        for(int i = 0; i < elements.size(); i++){

        }

    }

    private void drawIndependentElement(IndependentElement element){
        System.out.println("Drawing independent element");
    }

    private void renderStructure(DataStructure struct, int x, int y, int width, int height){
        Class structClass = struct.getClass();
        if (structClass.equals(Array.class)){
            drawArray((Array)struct, x, y, width, height);
        } else if (structClass.equals(IndependentElement.class)){
            drawIndependentElement((IndependentElement)struct);
        }

    }


}