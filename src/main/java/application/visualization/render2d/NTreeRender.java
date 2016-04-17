package application.visualization.render2d;

import java.util.Arrays;
import java.util.List;

import application.gui.Main;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;
import wrapper.datastructures.Array.ArrayElement;

/**
 * A Render for Arrays with abstract type Tree. Can draw any K-ary tree for K >= 2, where K is the number of children a
 * node has (at most). All nodes except root have one parent. All the elements of the supplied DataStruture must have an
 * index[] of length 1. No checking of index length is performed. Behaviour is undefined for K < 2 and index.length !=
 * 1.
 * 
 * @author Richard Sundqvist
 *
 */
public class NTreeRender extends Render {

    private static final double   DIAMETER               = 50;
    private final DataStructure   struct;
    private final GraphicsContext gc2d;
    private int                   elementsPreviousRender = 0;
    private int                   K;
    private int                   totDepth, totBreadth, completedSize;
    private static final Color    COLOR_READ             = Color.valueOf(Element.COLOR_READ);
    private static final Color    COLOR_WRITE            = Color.valueOf(Element.COLOR_WRITE);
    private static final Color    COLOR_SWAP             = Color.valueOf(Element.COLOR_SWAP);
    private static final Color    COLOR_WHITE            = Color.WHITE;
    private static final Color    COLOR_BLACK            = Color.BLACK;

    /**
     * Create a new NTreeRender with K children and one parent. Note that the behaviour of this Render is undefined for
     * arrays with more than one indices.
     * 
     * @param struct The structure to draw as an K-ary tree.
     * @param K The number of children each node has.
     * @throws IllegalArgumentException If K < 2.
     */
    public NTreeRender (DataStructure struct, int K) throws IllegalArgumentException{
        if (K < 2) {
            throw new IllegalArgumentException("K must be greater than or equal to 2.");
        }
        this.struct = struct;
        this.K = K;
        //Build Canvas
        Canvas canvas = new Canvas();
        gc2d = canvas.getGraphicsContext2D();
        BorderPane bp = new BorderPane();
        //Struct name
        bp.setTop(new Label("\tidentifier: " + struct.identifier));
        bp.setCenter(canvas);
        //Build
        this.getChildren().add(bp);
        this.setMinSize(200, 100);
        this.setMaxSize(200, 100);
    }

    /**
     * Create a new NTreeRender with two children and one parent. Note that the behaviour of this Render is undefined
     * for arrays with more than one indices.
     * 
     * @param struct The structure to draw as a binary tree.
     */
    public NTreeRender (DataStructure struct){
        this(struct, 2);
    }

    /**
     * Render only the necessary elements.
     */
    @Override
    public void render (){
        List<Element> structElements = struct.getElements();
        if (structElements.size() != elementsPreviousRender) {
            init();
        }
        else {
            List<Element> modifiedElements = struct.getModifiedElements();
            List<Element> resetElements = struct.getResetElements();
            ArrayElement ae;
            for (Element e : struct.getElements()) {
                ae = (ArrayElement) e;
                if (modifiedElements.contains(e)) {
                    drawElement(ae.getValue() + "", ae.getIndex()[0], e.getColor());
                }
                else if (resetElements.contains(e)) {
                    drawElement(ae.getValue() + "", ae.getIndex()[0], null);
                }
            }
            struct.elementsDrawn();
        }
        elementsPreviousRender = structElements.size();
    }

    /**
     * Calculate the depth and breath of the tree. https://en.wikipedia.org/wiki/K-ary_tree
     */
    private void calculateDepthAndBreadth (){
        double completeSize = 1;
        double structSize = struct.size();
        //Calculate the minimum depth which can hold all elements of the array.
        for (totDepth = 1; completeSize < structSize; totDepth++) {
            completeSize = Math.pow(completeSize, K);
        }
        this.completedSize = (int) completeSize;
        System.out.println("completeSize = " + completeSize);
        System.out.println("completeSize (int) = " + this.completedSize);
        totBreadth = (int) Math.pow(K, totDepth);
        System.out.println("breadth = " + Math.pow(K, totDepth));
        System.out.println("breadth (int) = " + totBreadth);
//      depth = (int) (Math.log(K - 1) / Math.log(K) + Math.log(completeSize) / Math.log(K) - 1) + 1; //Add one for the root
//      System.out.println("depth = " + (Math.log(K - 1) / Math.log(K) + Math.log(completeSize) / Math.log(K) - 1) + 1);
//      System.out.println("depth (int) = " + depth);
    }

    /**
     * Create and render all elements.
     */
    private void init (){
        calculateDepthAndBreadth();
        //TODO: Clear canvas
        for (Element e : struct.getElements()) {
            ArrayElement ae = (ArrayElement) e;
            drawElement(ae.getValue() + "", ae.getIndex()[0], null);
        }
        for (int i = struct.getElements().size(); i < completedSize; i++) {
            drawElement("", i, "spooky zombie");
        }
        elementsPreviousRender = struct.getElements().size();
        struct.elementsDrawn();
    }

    /**
     * Draw an element.
     * @param value The value to print in this node.
     * @param index The index of this element.
     * @param style The style for this element.
     */
    private void drawElement (String value, int index, String style){
        int depth, breadth;
        double x, y;
        if (index == 0) { //Root element
            x = totBreadth * DIAMETER / 2;
            y = 0;
            breadth = totBreadth;
        }
        else {
            depth = 0;
            breadth = 0;
            //Calculate row and column.
            x = totBreadth * DIAMETER / (K * breadth);
            y = depth * DIAMETER * 2;
        }
        //Dispatch
        drawNode(value, x, y, getFillColor(style), breadth == totBreadth);
    }

    /*
     * Actual drawing.
     */
    /**
     * Draw a node.
     * 
     * @param value The value to print in this node.
     * @param x The x value of this node.
     * @param y The y valueof this node
     * @param fill The fill color of this node.
     * @param lastRow If {@code true}, no child connection lines are drawn.
     */
    private void drawNode (String value, double x, double y, Color fill, boolean lastRow){
        gc2d.setFill(fill);
        gc2d.fillOval(x, y, DIAMETER, DIAMETER);
        //Outline, text, children
        gc2d.setFill(COLOR_BLACK);
        gc2d.strokeOval(x, y, DIAMETER, DIAMETER);
        gc2d.fillText(value, x + 6, y);
        //Connect to children
        if (lastRow) {
            return; //Don't connect bottom level
        }
        x = x + DIAMETER / 2; //x-mid of current node.
        y = y + DIAMETER; //Bottom of current node.
        gc2d.strokeLine(x, y, x - this.totBreadth * DIAMETER / (K), y + DIAMETER * 2);
        gc2d.strokeLine(x, y, x + this.totBreadth * DIAMETER / (K), y + DIAMETER * 2);
    }

    /**
     * Get the fill color for the center. Returns white for style == null and black as default.
     * 
     * @param style The style to return a color for.
     * @return A color.
     */
    private Color getFillColor (String style){
        if (style == null) {
            return COLOR_WHITE;
        }
        switch (style) {
            case Element.COLOR_READ:
                return COLOR_READ;
            case Element.COLOR_WRITE:
                return COLOR_WRITE;
            case Element.COLOR_SWAP:
                return COLOR_SWAP;
            default:
                return COLOR_BLACK;
        }
    }
}
