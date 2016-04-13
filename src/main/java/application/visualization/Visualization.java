package application.visualization;

import application.model.iModel;
import application.visualization.render2d.ArrayRender;
import application.visualization.render2d.Consts;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import wrapper.Operation;
import wrapper.datastructures.Array;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.IndependentElement;

import java.util.Iterator;
import java.util.Map;

public class Visualization extends Pane {
    private final iModel model;
    private final Canvas canvas = new Canvas();

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

        while (structNames.hasNext()){
            final int x = 0;
            final int y = 0 + Consts.structHeight*numStruct;
            final String id = structNames.next();

            renderStructure(id, structs.get(id), x, y);
            numStruct++;
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



    private void drawIndependentElement(IndependentElement element){
//        System.out.println("Drawing independent element");
    }

    private void renderStructure(String id, DataStructure struct, int x, int y){
        int width = (int)canvas.getWidth();
        int height = Consts.structHeight;
        final Class<? extends DataStructure> structClass = struct.getClass();
        final Operation op = model.getCurrentStep().getLastOp();
        if (structClass.equals(Array.class)){
            ArrayRender arrayRender = new ArrayRender(canvas, id, struct, op, x, y, width, height);
            arrayRender.render();
        } else if (structClass.equals(IndependentElement.class)){
            drawIndependentElement((IndependentElement)struct);
        }

    }




}