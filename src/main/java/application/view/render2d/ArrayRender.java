package application.view.render2d;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import manager.datastructures.Array;
import manager.datastructures.DataStructure;
import manager.datastructures.Element;
import wrapper.Operation;

import java.util.List;

/**
 * Created by Ivar on 2016-04-11.
 */
public class ArrayRender implements iRender{

    private Canvas canvas;
    private Operation op;
    private Array array;
    private int x;
    private int y;
    private int width;
    private int height;

    public ArrayRender(Canvas canvas, DataStructure struct, Operation op, int x, int y, int width, int height){
        this.canvas = canvas;
        this.op = op;
        this.array = (Array)struct;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    private void renderStructure(){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);

        List<Element> elements = array.getElements();
        for(int i = 0; i < elements.size(); i++){

        }
    }

    private void renderHilights(){

    }

    private void renderValues(){

    }

    public void render(){
        renderStructure();
        renderHilights();
        renderValues();
    }
}
