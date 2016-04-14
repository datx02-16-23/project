package application.visualization.render2d;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import wrapper.Operation;
import wrapper.datastructures.Array;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

import java.util.List;

/**
 * Created by Ivar on 2016-04-11.
 */
public class ArrayRender implements iRender {

    private Canvas    canvas;
    private String    id;
    private Operation op;
    private Array     array;
    private int       x;
    private int       y;
    private int       width;
    private int       height;
    private int       elementWidth = 60;

    public ArrayRender (Canvas canvas, String id, DataStructure struct, Operation op, int x, int y, int width, int height){
        this.canvas = canvas;
        this.id = id;
        this.op = op;
        this.array = (Array) struct;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    private void renderHeader (){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFont(new Font(Consts.headerFontSize));
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.BOTTOM);
        gc.fillText(generateStructHeader(), 20 + x, 20 + y);
    }

    private String generateStructHeader (){
        StringBuilder sB = new StringBuilder();
        sB.append("Identifier: ");
        sB.append(id);
        sB.append("\t");
        sB.append("Type: ");
        sB.append(array.rawType);
        return sB.toString();
    }

    private void renderStructure (){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        int structWidth = array.size() * elementWidth;
        gc.strokeRect(x + 30, y + 30, structWidth, height - 30);
        for (int i = 1; i < array.size(); i++) {
            int xPos = x + 30 + elementWidth * i;
            int yStart = y + 30;
            int yEnd = y + height;
            gc.strokeLine(xPos, yStart, xPos, yEnd);
        }
    }

    private void renderHilights (){
    }

    private void renderValues (){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setFont(new Font(Consts.elementFontSize));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        for (int i = 0; i < array.size(); i++) {
            Element element = array.getElements().get(i);
            int xPos = x + 30 + elementWidth * i + elementWidth / 2;
            int yPos = y + 30 + height / 2;
            gc.fillText(String.valueOf(element.getValue()), xPos, yPos);
        }
    }

    public void render (){
        renderHeader();
        if (array.size() > 0) {
            renderStructure();
            renderHilights();
            renderValues();
        }
    }
}
