package application.visualization.render2d;

import java.util.Arrays;
import java.util.List;

import application.gui.Main;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;
import wrapper.datastructures.Array.ArrayElement;

public class NTreeRender extends Render {

    public static final double  DIAMETER               = 50;
    private final DataStructure struct;
    private final Canvas        canvas;
    private int                 elementsPreviousRender = 0;
    private int                 K, depth, breadth, completeSize;

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
        canvas = new Canvas();
        BorderPane bp = new BorderPane();
        bp.setTop(new Label("\tidentifier: " + struct.identifier));
        bp.setCenter(canvas);
        calculateDepthAndBreadth();
        this.getChildren().add(bp);
        this.setMinSize(200, 100);
        this.setMaxSize(200, 100);
    }

    /**
     * Calculate the depth and breath of the tree. https://en.wikipedia.org/wiki/K-ary_tree
     */
    private void calculateDepthAndBreadth (){
        double completeSize = 1;
        double structSize = struct.size();
        //Calculate the minimum depth which can hold all elements of the array.
        for (depth = 1; completeSize < structSize; depth++) {
            completeSize = Math.pow(completeSize, K);
        }
        this.completeSize = (int) completeSize;
        System.out.println("completeSize = " + completeSize);
        System.out.println("completeSize (int) = " + this.completeSize);
        breadth = (int) Math.pow(K, depth);
        System.out.println("breadth = " + Math.pow(K, depth));
        System.out.println("breadth (int) = " + breadth);
//      depth = (int) (Math.log(K - 1) / Math.log(K) + Math.log(completeSize) / Math.log(K) - 1) + 1; //Add one for the root
//      System.out.println("depth = " + (Math.log(K - 1) / Math.log(K) + Math.log(completeSize) / Math.log(K) - 1) + 1);
//      System.out.println("depth (int) = " + depth);
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
            for (Element e : struct.getElements()) {
                if (modifiedElements.contains(e)) {
                    addElementTocanvas(e, e.getColor());
                }
                else if (resetElements.contains(e)) {
                    addElementTocanvas(e, null);
                }
            }
            struct.elementsDrawn();
        }
        elementsPreviousRender = structElements.size();
    }

    /**
     * Create and render all elements.
     */
    private void init (){
//        calculateSize();
        //TODO: CLear canvas
        for (Element e : struct.getElements()) {
            addElementTocanvas(e, null);
        }
        elementsPreviousRender = struct.getElements().size();
        struct.elementsDrawn();
    }

    //Ugly way of doing it, but I cant be bothered checking if the element moved.
    private void addElementTocanvas (Element e, String style){
        ArrayElement ae = (ArrayElement) e;
        int[] index = ae.getIndex();
    }

    /**
     * The actual drawing surface for the elements.
     * 
     * @author Richard Sundqvist
     *
     */
    private class canvasElement extends StackPane {

        private static final String BASE = "-fx-border-color: black;\n" + "-fx-border-insets: 1;\n" + "-fx-border-width: 2;\n" + "-fx-border-radius: 25;\n" + "-fx-background-radius: 25;";

        private canvasElement (Element e, String style){
            if (style == null) {
                this.setStyle(BASE + "\n-fx-background-color: white ;");
            }
            else {
                this.setStyle(BASE + "\n-fx-background-color: " + style + ";");
            }
            this.setPrefWidth(DIAMETER);
            this.setPrefHeight(DIAMETER);
            this.setMaxWidth(DIAMETER);
            this.setMaxHeight(DIAMETER);
            Label label = new Label(e.getValue() + "");
            this.getChildren().add(label);
            StackPane.setAlignment(label, Pos.CENTER);
        }
    }
}
