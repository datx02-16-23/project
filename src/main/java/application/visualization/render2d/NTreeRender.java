package application.visualization.render2d;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
public class NTreeRender extends Render {

    private static final double      DIAMETER               = 50;
    private final int                K;
    private final DataStructure      struct;
    private final GraphicsContext    context;
    private final ArrayList<Integer> lowerLevelSums         = new ArrayList<Integer>();
    private int                      elementsPreviousRender = 0;
    private int                      totDepth, totBreadth, completedSize;
    private static final Color       COLOR_READ             = Color.valueOf(Element.COLOR_READ);
    private static final Color       COLOR_WRITE            = Color.valueOf(Element.COLOR_WRITE);
    private static final Color       COLOR_SWAP             = Color.valueOf(Element.COLOR_SWAP);
    private static final Color       COLOR_WHITE            = Color.WHITE;
    private static final Color       COLOR_BLACK            = Color.BLACK;

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
        lowerLevelSums.add(new Integer(0));
//        this.setStyle("-fx-background-color: orange ;");
        //Build Canvas
        Canvas canvas = new Canvas();
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());
        canvas.maxWidth(Double.MAX_VALUE);
        canvas.maxHeight(Double.MAX_VALUE);
        context = canvas.getGraphicsContext2D();
//        BorderPane bp = new BorderPane();
        //Struct name
//        bp.setTop(new Label("\tidentifier:\n\n\n " + struct.identifier));
//        bp.setStyle("-fx-background-color: blue ;");
//        bp.setCenter(canvas);
        //Build
        this.getChildren().add(canvas);
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
        this.completedSize = lowerLevelSums.get(totDepth);
        totBreadth = K_pow(totDepth);
        calculateSize();
    }

    private double vspace = DIAMETER;
    private double hspace = DIAMETER;

    /**
     * Recalculate size.
     */
    private void calculateSize (){
        double width = hspace * (DIAMETER * totBreadth + 1);
        double height = totDepth * (DIAMETER + vspace);
        this.setMinSize(width, height);
        this.setMaxSize(width, height);
    }

    /**
     * Create and render all elements.
     */
    private void init (){
        calculateDepthAndBreadth();
        //TODO: Clear canvas
        context.setFill(COLOR_WHITE);
        context.fillRect(0, 0, 500, 700);
        for (Element e : struct.getElements()) {
            ArrayElement ae = (ArrayElement) e;
            drawElement(ae.getValue() + "", ae.getIndex()[0], null);
        }
        int i = struct.getElements().size();
        for (; i < completedSize; i++) {
            drawElement("zombie", i, "spooky zombie"); //Draw zombies. String will evaluate to black fill.
        }
        struct.elementsDrawn();
    }

    private double getX (int breadth, int depth){
        int nodesOnLevel = this.K_pow(depth);
        return (double) ((breadth + 1) * this.getMaxWidth()) / (double) (nodesOnLevel + 1);
    }

    private double getY (int depth){
        return depth * DIAMETER * 2;
    }

    /**
     * Draw an element.
     * 
     * @param value The value to print in this node.
     * @param index The index of this element.
     * @param style The style for this element.
     */
    private void drawElement (String value, int index, String style){
        System.out.println("value = " + value);
        int breadth, depth;
        double x, y;
        if (index == 0) { //Root element
            x = this.getMaxWidth() / 2;
            y = 0;
            breadth = 0;
            depth = 0;
            //TODO: Possible to solve root element without special case?
        }
        else {
            depth = 1;
            //Calculate depth and breadth
            while(lowerLevelSum(depth) <= index) {
                depth++;
            }
            depth--;
            breadth = (int) (index - lowerLevelSum(depth));
            y = getY(depth);
            x = getX(breadth, depth);
        }
        //Dispatch
        drawNode(value, x, y, getFillColor(style), depth, breadth);
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
    private void drawNode (String value, double x, double y, Color fill, int depth, int breadth){
        System.out.println("fill = " + fill);
        context.setFill(fill);
        context.fillOval(x, y, DIAMETER, DIAMETER);
        //Outline, text, children
        context.setFill(COLOR_BLACK);
        context.setStroke(COLOR_BLACK);
        context.strokeOval(x, y, DIAMETER, DIAMETER);
        context.fillText(value, x + 6, y + DIAMETER / 2);
        //Connect to children
        if (depth == totDepth) {
            return; //Don't connect bottom level
        }
        x = x + DIAMETER / 2; //x-mid of current node.
        y = y + DIAMETER; //Bottom of current node.
        double xOffset = this.getMaxWidth() / Math.pow(K, depth + 1);
        //chilyY OK
        double childY = y + DIAMETER * 2; //OK
        context.strokeLine(x, y, x - xOffset, childY);
        context.strokeLine(x, y, x + xOffset, childY);
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

    /**
     * Returns the number nodes at depth d (K^d). Really just a simplified pow function for ints.
     * 
     * @param d the depth to calculate #nodes for.
     * @return The number of nodes at depth d.
     */
    private int K_pow (int d){
        int parts = 1;
        for (int i = 0; i < d; i++) {
            parts = parts * K;
        }
        return parts;
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
}
