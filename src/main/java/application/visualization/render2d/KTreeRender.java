package application.visualization.render2d;

import java.util.ArrayList;
import java.util.List;

import application.gui.Main;
import application.visualization.animation.Animation;
import application.visualization.animation.LinearAnimation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;
import wrapper.operations.OperationType;
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

    private static final RenderSpinnerVF rsvf           = createOptionsSpinner();
    private final int                    K;
    private final ArrayList<Integer>     lowerLevelSums = new ArrayList<Integer>();
    private int                          totDepth, totBreadth, completedSize;

    /**
     * Create a new KTreeRender with K children and one parent. Will set K = 2 for any K < 2.
     * 
     * @param struct The structure to draw as an K-ary tree.
     * @param K The number of children each node has.
     * @param width The width of the nodes.
     * @param height The height of the nodes.
     * @param hspace The horizontal space between elements.
     * @param vspace The verital space between elements.
     */
    public KTreeRender (DataStructure struct, int K, double width, double height, double hspace, double vspace) throws IllegalArgumentException{
        super(struct, width, height, hspace, vspace);
        this.K = K < 2 ? 2 : K;
        lowerLevelSums.add(new Integer(0));
        Main.console.force("WARNING: At the time of writing (2016-04-18) the KTreeRender class, JavaFX may crash with a NullPointerException when Canvas grows too large.");
    }

    private static RenderSpinnerVF createOptionsSpinner (){
        return new RenderSpinnerVF(2, 1337);
    }

    /**
     * Create a new KTreeRender with K children and one parent. Note that the behaviour of this Render is undefined for
     * arrays with more than one indices.
     * 
     * @param struct The structure to draw as an K-ary tree.
     * @param K The number of children each node has.
     * @throws IllegalArgumentException If K < 2.
     */
    public KTreeRender (DataStructure struct, int K){
        this(struct, K, DEFAULT_SIZE * 2, DEFAULT_SIZE, DEFAULT_SIZE / 2, DEFAULT_SIZE / 2);
    }

    /**
     * Create a new KTreeRender with two children and one parent.
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
        if (struct.repaintAll) {
            init();
            struct.repaintAll = false;
        }
        else {
            List<Element> modifiedElements = struct.getModifiedElements();
            List<Element> resetElements = struct.getResetElements();
            ArrayElement ae;
            for (Element e : struct.getElements()) {
                ae = (ArrayElement) e;
                if (modifiedElements.contains(e)) {
                    drawElement(ae, ae.getIndex()[0], e.getColor());
                }
                else if (resetElements.contains(e)) {
                    drawElement(ae, ae.getIndex()[0], Color.WHITE);
                }
            }
        }
        struct.elementsDrawn();
    }

    /**
     * Calculate the depth and breath of the tree. {@code (double) (K_pow(totDepth + 1) - 1) / (double) (K - 1)} gives
     * total number of elements above {@code totDepth}. Using {@code calculateLowerLevelSums} instead.
     */
    private void calculateDepthAndBreadth (){
        double structSize = struct.getElements().size();
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

    @Override
    public void calculateSize (){
        super.calculateSize();
        this.WIDTH = totBreadth * (node_width + hspace);
        this.HEIGHT = totDepth * (node_height + vspace) * 2 + vspace; //Depth doesnt include node + margain above.
        this.setPrefSize(WIDTH, HEIGHT);
    }

    /**
     * Create and render all elements.
     */
    private void init (){
        calculateDepthAndBreadth(); //Calls calculatePrefSize()
        GraphicsContext context = local_canvas.getGraphicsContext2D();
        context.clearRect(0, 0, this.WIDTH, this.WIDTH);
        context.setFill(COLOR_BLACK);
        context.fillText(struct.toString(), hspace, vspace + 10);
        for (Element e : struct.getElements()) {
            ArrayElement ae = (ArrayElement) e;
            drawElement(ae, ae.getIndex()[0], Color.WHITE);
        }
        int i = struct.getElements().size();
        for (; i < completedSize; i++) {
            ArrayElement ae = new ArrayElement(Double.NaN, new int[] {i});
            struct.getInactiveElements().add(ae);
            drawElement(ae, i, OperationType.remove.color); //Draw zombies. String will evaluate to black fill.
        }
        drawConnectors();
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
    //private void drawElement (String value, int index, String style){
    private void drawElement (Element e, int index, Color style){
        double x = getX(e);
        double y = getY(e);
        //Dispatch
        drawNode(e == null ? "" : e.getNumericValue() + "", x, y, style, local_canvas);
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
    private void drawNode (String value, double x, double y, Color fill, Canvas c){
        GraphicsContext context = c.getGraphicsContext2D();
        context.setFill(fill);
        context.fillOval(x, y, node_width, node_height);
        //Outline, text, children
        context.setFill(COLOR_BLACK);
        context.setStroke(COLOR_BLACK);
        //Value
        context.strokeOval(x, y, node_width, node_height);
        final Text text = new Text(value);
        new Scene(new Group(text));
        text.applyCss();
        double tw = text.getLayoutBounds().getWidth();
//        double th = text.getLayoutBounds().getHeight();
        context.fillText(value, x + node_width / 2 - tw / 2, y + node_height / 2);
        //Index
        context.strokeOval(x, y, node_width, node_height);
    }

    private void drawConnectors (){
        GraphicsContext gc = local_canvas.getGraphicsContext2D();
        ArrayElement ae;
        double x, y;
        int index;
        int depth;
        double xOffset = node_width / 2;
        for (int i = 0; i < struct.getElements().size(); i++) {
            gc.setStroke(Color.BLACK);
            ae = (ArrayElement) struct.getElements().get(i);
            index = ae.getIndex()[0];
            depth = this.getDepth(index);
            x = getX(ae);
            y = getY(ae);
            if (index == 0) { //Mark root
                gc.fillOval(x + node_width / 2 - (node_height / 2) / 2, y - node_height / 2, node_height / 2, node_height / 2);
            }
            if (depth == totDepth) {
                return;
            }
            gc.fillText("[" + index + "]", x + node_width + 4, y + node_height / 4);
            x = x + node_width / 2;
            y = y + node_height;
            double childY = getY(depth + 1);
            //Stroke lines to all children
            for (int child = 0; child < K; child++) {
                gc.strokeLine(x, y, getX(getBreadth(K * index + 1 + child, depth + 1), depth + 1) + xOffset, childY);
            }
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

    @Override
    public double getX (Element e){
        int index = ((ArrayElement) e).getIndex()[0];
        double x;
        int breadth, depth;
        if (index == 0) { //Root element
            double p = K_pow(totDepth) / 2;
            x = hspace + (hspace + node_width) * (p) - ((K + 1) % 2) * (node_width + hspace) / 2;
            breadth = 0;
            depth = 0;
        }
        else {
            depth = getDepth(index);
            breadth = getBreadth(index, depth);
            x = getX(breadth, depth);
        }
        return x;
    }

    @Override
    public double getY (Element e){
        int index = ((ArrayElement) e).getIndex()[0];
        double y;
        int depth;
        if (index == 0) { //Root element
            y = vspace;
            depth = 0;
        }
        else {
            depth = getDepth(index);
            y = getY(depth);
        }
        return y;
    }

    @Override
    public void drawAnimatedElement (Element e, double x, double y, Color style){
        drawNode(e.getNumericValue() + "", x, y, e.getColor(), SHARED_ANIMATED);
    }

    @Override
    public void startAnimation (Element e, double x, double y){
        Animation a = new LinearAnimation(this, e, x, y);
        a.start();
    }

    @Override
    public RenderSpinnerVF getOptionsSpinnerValueFaxtory (){
//        System.out.println("\nktree render spinner factory:");
//        System.out.println(rsvf);
        return rsvf;
    }
}