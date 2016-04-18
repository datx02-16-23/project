package application.visualization.render2d;

import java.util.ArrayList;
import java.util.List;

import application.gui.Main;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;
import wrapper.datastructures.Array.ArrayElement;

/**
 * A Render for Arrays with abstract type Tree. Can draw any K-ary tree for K >= 2, where K is the number of children a
 * node has (at most). All nodes except root have one parent. All the elements of the supplied DataStruture must have an
 * index[] of length 1. No checking of index length is performed. Behaviour is undefined for K < 2 and index.length !=
 * 1. Implementation based on {@link https://en.wikipedia.org/wiki/K-ary_tree}.
 * 
 * @author Richard Sundqvist
 *
 */
public class KTreeRender extends Render {

    public static final double       DEFAULT_SIZE           = 40;
    private final double             node_width, node_height;
    private final double             hspace, vspace;
    private final Canvas             canvas;
    private final int                K;
    private final DataStructure      struct;
    private final ArrayList<Integer> lowerLevelSums         = new ArrayList<Integer>();
    private int                      elementsPreviousRender = 0;
    private int                      totDepth, totBreadth, completedSize;
    private static final Color       COLOR_READ             = Color.valueOf(Element.COLOR_READ);
    private static final Color       COLOR_WRITE            = Color.valueOf(Element.COLOR_WRITE);
    private static final Color       COLOR_SWAP             = Color.valueOf(Element.COLOR_SWAP);
    private static final Color       COLOR_INACTIVE         = Color.valueOf(Element.COLOR_INACTIVE);
    private static final Color       COLOR_WHITE            = Color.WHITE;
    private static final Color       COLOR_BLACK            = Color.BLACK;
    private double transX, transY;

    /**
     * Create a new NTreeRender with K children and one parent. Note that the behaviour of this Render is undefined for
     * arrays with more than one indices.
     * 
     * @param struct The structure to draw as an K-ary tree.
     * @param K The number of children each node has.
     * @param width The width of the nodes.
     * @param height The height of the nodes.
     * @param hspace The horizontal space between elements.
     * @param vspace The verital space between elements.
     * @throws IllegalArgumentException If K < 2.
     */
    public KTreeRender (DataStructure struct, int K, double width, double height, double hspace, double vspace) throws IllegalArgumentException{
        if (K < 2) {
            throw new IllegalArgumentException("K must be greater than or equal to 2.");
        }
        this.struct = struct;
        this.K = K;
        lowerLevelSums.add(new Integer(0));
        //Build Canvas
        canvas = new Canvas();
        moveAndZoom();
        canvas.widthProperty().bind(this.maxWidthProperty());
        canvas.heightProperty().bind(this.maxHeightProperty());
        canvas.maxWidth(Double.MAX_VALUE);
        canvas.maxHeight(Double.MAX_VALUE);
        //Sizing and spacing
        this.node_width = width;
        this.node_height = height;
        this.hspace = hspace;
        this.vspace = vspace;
        this.getChildren().add(canvas);
        this.setMinSize(200, 100);
        this.setMaxSize(200, 100);
        
        Main.console.force("WARNING: At the time of writing (2016-04-18) the KTreeRender class, JavaFX may crash with a NullPointerException when Canvas grows too large.");
    }

    /**
     * Create a new NTreeRender with K children and one parent. Note that the behaviour of this Render is undefined for
     * arrays with more than one indices.
     * 
     * @param struct The structure to draw as an K-ary tree.
     * @param K The number of children each node has.
     */
    public KTreeRender (DataStructure struct, int K){
        this(struct, K, DEFAULT_SIZE * 2, DEFAULT_SIZE, DEFAULT_SIZE / 2, DEFAULT_SIZE / 2);
    }

    /**
     * Create a new NTreeRender with two children and one parent. Note that the behaviour of this Render is undefined
     * for arrays with more than one indices.
     * 
     * @param struct The structure to draw as a binary tree.
     */
    public KTreeRender (DataStructure struct){
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
            elementsPreviousRender = structElements.size();
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
            int i = struct.getElements().size();
            for (; i < completedSize; i++) {
                drawElement("", i, "spooky zombie");
            }
        }
    }

    /**
     * Calculate the depth and breath of the tree. {@code (double) (K_pow(totDepth + 1) - 1) / (double) (K - 1)} gives
     * total number of elements above {@code totDepth}. Using {@code calculateLowerLevelSums} instead.
     */
    private void calculateDepthAndBreadth (){
        double structSize = struct.size();
        totDepth = 0;
        //Calculate the minimum depth which can hold all elements of the array.
        while(lowerLevelSum(totDepth) < structSize) {
            totDepth++;
        }
        totDepth--;
        this.completedSize = lowerLevelSums.get(totDepth + 1);
        totBreadth = K_pow(totDepth);
        calculateSize();
    }

    /**
     * Recalculate size.
     */
    private void calculateSize (){
        double width = totBreadth * (node_width + hspace);
        double height = totDepth * (node_height + vspace) * 2 + vspace - node_height; //Depth doesnt include node + margain above.
        this.setMinSize(width, height);
        this.setMaxSize(width, height);
    }

    /**
     * Create and render all elements.
     */
    private void init (){
        calculateDepthAndBreadth(); //Calls calculateSize()
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.setFill(COLOR_WHITE);
        context.fillRect(0, 0, this.getMaxWidth(), this.getMaxHeight());
        context.setFill(COLOR_BLACK);
        context.fillText("identifier: " + struct.identifier + "( " + struct.abstractType + ")", hspace, vspace + 10);
        for (Element e : struct.getElements()) {
            ArrayElement ae = (ArrayElement) e;
            drawElement(ae.getValue() + "", ae.getIndex()[0], null);
        }
        int i = struct.getElements().size();
        for (; i < completedSize; i++) {
            drawElement("", i, "spooky zombie"); //Draw zombies. String will evaluate to black fill.
        }
        struct.elementsDrawn();
    }

    private double getX (int breadth, int depth){
        //Stepsize at this depth. Farther from root smaller steps
        double L = (double) K_pow(totDepth) / (double) K_pow(depth);
        //Apply indentation for every row except the last
        double indentation = 0;
        if (depth < totDepth) {
            indentation = (hspace + node_width) * ((L - 1) / 2);
        }   
        //Dont multiply by zero
        if (breadth > 0) {
            return hspace + indentation + breadth * L * ((hspace + node_width));
        }
        else {
            return hspace + indentation;
        }
    }

    private double getY (int depth){
        return depth * node_height * 2 + vspace; //Padding on top
    }

    /**
     * Draw an element.
     * 
     * @param value The value to print in this node.
     * @param index The index of this element.
     * @param style The style for this element.
     */
    private void drawElement (String value, int index, String style){
        int breadth, depth;
        double x, y;
        if (index == 0) { //Root element
            double p = K_pow(totDepth) / 2;
            x = hspace + (hspace + node_width) * (p) - ((K + 1) % 2) * (node_width + hspace) / 2;
            y = vspace;
            breadth = 0;
            depth = 0;
        }
        else {
            depth = getDepth(index);
            breadth = getBreadth(index, depth);
            y = getY(depth);
            x = getX(breadth, depth);
        }
        //Dispatch
        drawNode(value, x, y, getFillColor(style), depth, breadth, index);
    }

    private int getDepth (int index){
        int depth = 1;
        //Calculate depth and breadth
        while(lowerLevelSum(depth) <= index) {
            depth++;
        }
        return depth - 1;
    }

    private int getBreadth (int index, int depth){
        return index - lowerLevelSum(depth);
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
    private void drawNode (String value, double x, double y, Color fill, int depth, int breadth, int index){
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.setFill(fill);
        context.fillOval(x, y, node_width, node_height);
        //Outline, text, children
        context.setFill(COLOR_BLACK);
        context.setStroke(COLOR_BLACK);
        //Value
        context.strokeOval(x, y, node_width, node_height);
        context.fillText("v: " + value == null ? "null" : value, x + 6, y + node_height / 2);
        //Index
        context.strokeOval(x, y, node_width, node_height);
        context.fillText("[" + index + "]", x + node_width + 4, y + node_height / 2);
        //Connect to children
        if (depth == totDepth) {
            return; //Don't connect bottom level
        }
        //Origin is always the same.
        x = x + node_width / 2;
        y = y + node_height;
        double xOffset = node_width / 2;
        double childY = getY(depth + 1);
        //Stroke lines to all children
        for (int child = 0; child < K; child++) {
            context.strokeLine(x, y, getX(getBreadth(K * index + 1 + child, depth + 1), depth + 1) + xOffset, childY);
        }
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
                return COLOR_INACTIVE;
        }
    }

    /**
     * Returns the number nodes at depth d (K^d). Really just a simplified pow function for ints.
     * 
     * @param d the depth to calculate #nodes for.
     * @return The number of nodes at depth d.
     */
    private int K_pow (int d){
        int p = 1;
        for (int i = 0; i < d; i++) {
            p = p * K;
        }
        return p;
    }

    /**
     * Memoized function. Calculates the total number of elements below a given depth and saves it to higherLevelSums.
     * Once this method returns, total number of elements on any given depth up to {@code targetDepth} can be fetched
     * from {@code higherLevelSums}.
     * 
     * @param targetDepth The greatest depth to calculate for.
     * @return The total number of elements above {@code targetDepth} for a K-ary tree.
     */
    private int lowerLevelSum (int targetDepth){
        while(lowerLevelSums.size() <= targetDepth) {
            int sum = lowerLevelSums.get(lowerLevelSums.size() - 1) + K_pow(lowerLevelSums.size() - 1);
            lowerLevelSums.add(sum);
        }
        return lowerLevelSums.get(targetDepth);
    }
    
    private double scale = 1;
    private int sign = 1;

    private void moveAndZoom (){
        canvas.setOnContextMenuRequested(event -> {
            scale = scale + sign*0.1;
            System.out.println(scale);
            if(scale < 0.1){
                scale = 2;
            } else if (scale > 2){
                scale = 0.25;
            }
            canvas.setScaleX(scale);
            canvas.setScaleY(scale);
        });
        canvas.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle (MouseEvent mouseEvent){
                // record a delta distance for the drag and drop operation.
                transX = canvas.getLayoutX() - mouseEvent.getSceneX();
                transY = canvas.getLayoutY() - mouseEvent.getSceneY();
                canvas.setCursor(Cursor.MOVE);
            }
        });
        canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle (MouseEvent mouseEvent){
                canvas.setCursor(Cursor.HAND);
            }
        });
        canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle (MouseEvent mouseEvent){
                canvas.setLayoutX(mouseEvent.getSceneX() + transX);
                canvas.setLayoutY(mouseEvent.getSceneY() + transY);
            }
        });
        canvas.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle (MouseEvent mouseEvent){
                canvas.setCursor(Cursor.HAND);
            }
        });
    }
}
