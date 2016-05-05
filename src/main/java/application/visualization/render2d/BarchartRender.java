package application.visualization.render2d;

import java.util.List;

import application.visualization.animation.*;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;
import wrapper.datastructures.Array;
import wrapper.datastructures.Array.ArrayElement;

public class BarchartRender extends Render {

    public static final Color   DEFAULT_COLOR = Color.web("#123456");
    public static final double  DEFAULT_SIZE  = 40;
    private static final double padding       = 35;
    private final Array         struct;

    /**
     * Create a new BarRender.
     * 
     * @param struct The structure to draw as a Matrix.
     * @param width The width of the nodes.
     * @param height The height per unit.
     * @param hspace The horizontal space between elements.
     * @param padding The padding around the chart.
     */
    public BarchartRender (DataStructure struct, double width, double height, double hspace){
        super(struct, width, height, hspace, -1);
        this.struct = (Array) struct;
    }

    /**
     * Render only the necessary elements.
     */
    @Override
    public void render (){
        if (struct.repaintAll) {
            init();
        }
        else {
            List<Element> modifiedElements = struct.getModifiedElements();
            List<Element> resetElements = struct.getResetElements();
            ArrayElement ae;
            for (Element e : struct.getElements()) {
                ae = (ArrayElement) e;
                if (modifiedElements.contains(e)) {
                    drawElement(ae.getNumericValue(), ae.getIndex(), e.getColor());
                }
                else if (resetElements.contains(e)) {
                    drawElement(ae.getNumericValue(), ae.getIndex(), DEFAULT_COLOR);
                }
            }
        }
        drawAxes();
        struct.elementsDrawn();
    }

    /**
     * Create and render all elements.
     */
    private void init (){
        GraphicsContext context = local_canvas.getGraphicsContext2D();
        context.clearRect(-5000, -5000, 10000, 10000);
        calculateSize();
        context.setFill(COLOR_BLACK);
        context.fillText(struct.toString(), 0, 10);
        for (Element e : struct.getElements()) {
            ArrayElement ae = (ArrayElement) e;
            drawElement(ae.getNumericValue(), ae.getIndex(), DEFAULT_COLOR);
        }
        struct.repaintAll = false;
    }

    @Override
    public void calculateSize (){
        WIDTH = padding * 3 + (hspace + node_width) * struct.getElements().size();
        HEIGHT = padding * 2 + node_height * (Math.abs(struct.getMax()) + Math.abs(struct.getMin()));
        this.setMinSize(WIDTH, HEIGHT);
        this.setPrefSize(WIDTH, HEIGHT);
        this.setMaxSize(WIDTH, HEIGHT);
    }

    private double getX (int column){
        return padding + hspace * 2 + (hspace + node_width) * column;
    }

    private double getY (double value){
        return this.HEIGHT - (node_height) * value - padding;
    }

    /**
     * Draw an element.
     * 
     * @param value The value to print in this node.
     * @param index The index of this element.
     * @param style The style for this element.
     */
    private void drawElement (double value, int[] index, Color style){
        double x = getX(index[0]);
        double y = getY(value);
        //Dispatch
        drawNode(value, x, y, style, index[0], local_canvas);
    }

    /*
     * Actual drawing.
     */
    /**
     * Draw a node.
     * 
     * @param value The value to print in this node.
     * @param x The x value of this node.
     * @param y The y value of this node
     * @param fill The fill color of this node.
     * @param lastRow If {@code true}, no child connection lines are drawn.
     */
    private void drawNode (double value, double x, double y, Color fill, int index, Canvas canvas){
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.setFill(fill);
        context.clearRect(x - context.getLineWidth(), 0, node_width + 2 * context.getLineWidth(), this.HEIGHT);
        context.fillRect(x, y, node_width, node_height * value);
        //Outline, text
        context.setFill(COLOR_BLACK);
        context.setStroke(COLOR_BLACK);
        //Value
        context.strokeRect(x, y, node_width, node_height * value);
        final Text text = new Text(value + "");
        new Scene(new Group(text));
        text.applyCss();
        double tw = text.getLayoutBounds().getWidth();
//        double th = text.getLayoutBounds().getHeight();
        context.fillText(value + "", x + node_width / 2 - tw / 2, y - 5);
    }

    private void drawAxes (){
        GraphicsContext context = local_canvas.getGraphicsContext2D();
        context.setFill(COLOR_BLACK);
        context.setStroke(COLOR_BLACK);
        double y = this.HEIGHT - padding;
        double x = this.WIDTH - padding;
        //Y-axis
        double yLength = (Math.abs(struct.getMax()) + Math.abs(struct.getMin())) * (node_height);
        context.strokeLine(padding, padding / 2, padding, yLength + 1.5 * padding);
        //X-axis
        context.strokeLine(padding / 2, y + 1, x + padding / 2, y + 1);
//        int lines = 5 * yLength;
//        for (int i = 0; i < lines; i++) {
//            context.strokeLine(padding - hspace / 4, i * node_height / 5, padding + hspace / 2, i * node_height / 5);
//        }
    }

    public enum GrowthDirection{
        UP("Up", "Positive values will expand above the x-axis.", 0), DOWN("Down", "Positive values will expand below the x-axis.", 1);

        public final String name;
        public final String description;
        public final int    optionNbr;

        private GrowthDirection (String name, String description, int optionNbr){
            this.name = name;
            this.description = description;
            this.optionNbr = optionNbr;
        }
    }

    @Override
    public double getX (Element e){
        return getX(((ArrayElement) e).getIndex()[0]);
    }

    @Override
    public double getY (Element e){
        return this.HEIGHT - padding;
    }

    @Override
    public void drawAnimatedElement (Element e, double x, double y, Color style){
        drawNode(e.getNumericValue(), x, y, style, ((ArrayElement) e).getIndex()[0], SHARED_ANIMATED);
    }

    @Override
    public void startAnimation (Element e, double start_x, double start_y, double end_x, double end_y){
        Animation a = new LinearAnimation(this, e, end_x, end_y);
        a.start();
    }

    @Override
    public void clearAnimatedElement (Element e, double x, double y){
        SHARED_ANIMATED.getGraphicsContext2D().clearRect(x - 2, y - 15, node_width + 2 * 2, this.HEIGHT+15);
    }
}
