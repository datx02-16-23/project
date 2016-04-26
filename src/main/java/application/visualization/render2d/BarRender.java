package application.visualization.render2d;

import java.util.List;

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
import wrapper.datastructures.Array;
import wrapper.datastructures.Array.ArrayElement;

public class BarRender extends Render {

    public static final Color   DEFAULT_COLOR = Color.web("#123456");
    public static final double  DEFAULT_SIZE  = 40;
    private static final double padding       = 35;
    private final Array         struct;
    private short               base          = 10;
    private short               exp_pos       = 0;
    private short               exp_neg       = 0;
    private int                 yAxis_max, yAxis_min;

    /**
     * Create a new BarRender.
     * 
     * @param struct The structure to draw as a Matrix.
     * @param width The width of the nodes.
     * @param height The height per unit.
     * @param hspace The horizontal space between elements.
     * @param padding The padding around the chart.
     */
    public BarRender (DataStructure struct, double width, double height, double hspace){
        super(struct, width, height, hspace, -1);
        this.struct = (Array) struct;
    }

    /**
     * Render only the necessary elements.
     */
    @Override
    public void render (){
        if (struct.repaintAll) {
            System.out.println("repaint all");
            init();
        }
        else {
            System.out.println("repaint modified");
            List<Element> modifiedElements = struct.getModifiedElements();
            List<Element> resetElements = struct.getResetElements();
            ArrayElement ae;
            for (Element e : struct.getElements()) {
                ae = (ArrayElement) e;
                if (modifiedElements.contains(e)) {
                    drawElement(ae.getNumericValue(), ae.getIndex(), e.getColor());
//                    calculateSize();
                }
                else if (resetElements.contains(e)) {
                    drawElement(ae.getNumericValue(), ae.getIndex(), DEFAULT_COLOR);
                }
            }
        }
        struct.elementsDrawn();
    }

    private void checkMax (){
        double new_max = struct.getMax();
        if (yAxis_max == new_max) {
            return;
        }
        boolean negative = new_max < 0;
        yAxis_max = (int) Math.ceil(foo(new_max));
        if (negative) {
            yAxis_max = -yAxis_max;
        }
    }

    private void checkMin (){
        double new_min = struct.getMin();
        if (yAxis_min == new_min) {
            return;
        }
        boolean negative = new_min < 0;
        yAxis_min = (int) Math.ceil(foo(new_min));
        if (negative) {
            yAxis_min = -yAxis_min;
        }
    }

    /**
     * 
     */
    public double foo (double arg){
        return arg;
//        if (arg == 0) {
//            return 0;
//        }
//        int sign = arg < 0 ? -1 : 1;
//        arg = Math.abs(arg);
//        double ans = 0;
//        double base;
//        /*
//         * 
//         */
//        if (arg < 1) {
//            base = 1 / 10;
//            while(arg > base) {
//                base = base * (1 / 10);
//            }
//            
//            while (arg > 0){
//                ans = ans + 
//            }
//        }
//        /*
//         * 
//         */
//        else {
//            base = 10;
//            while(arg < base) {}
//        }
//        return sign * ans;
    }

    /**
     * Create and render all elements.
     */
    private void init (){
        calculateSize();
        GraphicsContext context = local_canvas.getGraphicsContext2D();
        context.clearRect(-5000, -5000, 10000, 10000);
        context.setFill(COLOR_BLACK);
        context.fillText(struct.toString(), 5, 10);
        for (Element e : struct.getElements()) {
            ArrayElement ae = (ArrayElement) e;
            drawElement(ae.getNumericValue(), ae.getIndex(), DEFAULT_COLOR);
        }
        struct.repaintAll = false;
    }

    @Override
    public void calculateSize (){
        checkMax();
        checkMin();
        super.calculateSize();
        WIDTH = padding * 2 + (hspace + node_width) * struct.getElements().size();
        HEIGHT = padding * 2 + node_height * (Math.abs(yAxis_max) + Math.abs(yAxis_min));
        System.out.println("yAxis_min = " + yAxis_min);
        System.out.println("yAxis_max = " + yAxis_max);
        System.out.println(HEIGHT);
        this.setPrefSize(WIDTH, HEIGHT);
        drawAxes();
    }

    private double getX (int column){
        return padding + hspace * 2 + (hspace + node_width) * column;
    }

    private double getY (double value){
        return this.HEIGHT - (node_height) * foo(value) - padding;
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
//        context.clearRect(x - context.getLineWidth(), padding / 2, node_width + 2 * context.getLineWidth(), this.HEIGHT);
        context.fillRect(x, y, node_width, node_height * foo(value));
        //Outline, text
        context.setFill(COLOR_BLACK);
        context.setStroke(COLOR_BLACK);
        //Value
        context.strokeRect(x, y, node_width, node_height * foo(value));
        final Text text = new Text(value + "");
        new Scene(new Group(text));
        text.applyCss();
        double tw = text.getLayoutBounds().getWidth();
        double th = text.getLayoutBounds().getHeight();
        context.fillText(value + "", x + node_width / 2 - tw / 2, y - th);
    }

    private void drawAxes (){
        GraphicsContext context = local_canvas.getGraphicsContext2D();
        context.setFill(COLOR_BLACK);
        context.setStroke(COLOR_BLACK);
        System.out.println("stroke");
        double y = this.HEIGHT - padding;
        double x = this.WIDTH - padding;
        //Y-axis
        int yLength = (Math.abs(yAxis_min) + Math.abs(yAxis_max));
        context.strokeLine(padding, padding / 2, padding, yLength + padding / 2);
        //X-axis
        context.strokeLine(padding / 2, y + 1, x + padding / 2, y + 1);
        int lines = 5 * yLength;
        System.out.println("lines = " + lines);
        for (int i = 0; i < lines; i++) {
            System.out.println("line");
            context.strokeLine(padding - hspace / 4, i * node_height / 5, padding + hspace / 2, i * node_height / 5);
        }
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
        ArrayElement ae = (ArrayElement) e;
        return padding + ae.getIndex()[0] * (hspace + node_width);
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
    public RenderSVF getOptionsSpinnerValueFactory (){
        return null; //No options for BarRender
    }

    @Override
    public void startAnimation (Element e, double start_x, double start_y, double end_x, double end_y){
        Animation a = new LinearAnimation(this, e, end_x, end_y);
        a.start();
    }

    @Override
    public double absX (Element e){
        double bx = this.getTranslateX() + this.getLayoutX();
        return this.getX(e) + bx;
    }

    @Override
    public double absY (Element e){
        double by = this.getTranslateY() + this.getLayoutY();
        return this.getY(e) + by;
    }
}
