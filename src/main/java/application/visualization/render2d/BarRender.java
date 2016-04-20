package application.visualization.render2d;

import java.util.List;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;
import wrapper.datastructures.Array.ArrayElement;

public class BarRender extends Render {

    public static final double DEFAULT_SIZE           = 40;
    private final double       padding;
    private int                elementsPreviousRender = 0;

    /**
     * Create a new BoxRender.
     * 
     * @param struct The structure to draw as a Matrix.
     * @param width The width of the nodes.
     * @param height The height per unit.
     * @param hspace The horizontal space between elements.
     * @param padding The padding around the chart.
     * @throws IllegalArgumentException If K < 2.
     */
    public BarRender (DataStructure struct, double width, double height, double hspace, double padding){
        super(struct, width, height, hspace, -1);
        this.padding = padding;
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
//                if (animatedElements.contains(e)) {
//                    continue; //Animated elements are handled seperately.
//                }
                ae = (ArrayElement) e;
                if (modifiedElements.contains(e)) {
                    drawElement(ae.getValue(), ae.getIndex(), e.getColor());
                }
                else if (resetElements.contains(e)) {
                    drawElement(ae.getValue(), ae.getIndex(), null);
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
        calculateSize();
        GraphicsContext context = stationary.getGraphicsContext2D();
        context.setFill(COLOR_WHITE);
        context.fillRect(0, 0, this.getMaxWidth(), this.getMaxHeight());
        context.setFill(COLOR_BLACK);
        context.fillText(struct.toString(), hspace, vspace + 10);
        for (Element e : struct.getElements()) {
            ArrayElement ae = (ArrayElement) e;
            drawElement(ae.getValue(), ae.getIndex(), null);
        }
    }

    private void calculateSize (){
        //TODO
        double width = padding * 2 + (hspace + node_width) * struct.getElements().size();
        double height = 1200;
        this.setMinSize(width, height);
        this.setMaxSize(width, height);
    }

    private double getX (int column){
        System.out.println();
        System.out.println(column);
        System.out.println(padding + hspace + (hspace + node_width) * column);
        return padding + hspace + (hspace + node_width) * column;
    }

    private double getY (double value){
        return this.getMinHeight() - (node_height) * value - padding;
    }

    /**
     * Draw an element.
     * 
     * @param value The value to print in this node.
     * @param index The index of this element.
     * @param style The style for this element.
     */
    private void drawElement (double value, int[] index, String style){
        double x = getX(index[0]);
        double y = getY(value);
        //Dispatch
        drawNode(value, x, y, getFillColor(style), index[0]);
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
    private void drawNode (double value, double x, double y, Color fill, int index){
        GraphicsContext context = stationary.getGraphicsContext2D();
        context.setFill(fill);
        context.clearRect(x-1, padding, node_width+2, this.getMaxHeight()+1);
        context.fillRect(x, y, node_width, node_height * value);
        //Outline, text, children
        context.setFill(COLOR_BLACK);
        context.setStroke(COLOR_BLACK);
        //Value
        context.strokeRect(x, y, node_width, node_height * value);
        final Text text = new Text(value + "");
        new Scene(new Group(text));
        text.applyCss();
        double tw = text.getLayoutBounds().getWidth();
        double th = text.getLayoutBounds().getHeight();
        context.fillText(value + "", x + node_width / 2 - tw / 2, y - th);
    }

    private void drawAxes (){
        GraphicsContext context = stationary.getGraphicsContext2D();
        context.setFill(COLOR_BLACK);
        double y = this.getMinHeight() - padding;
        double x = this.getMinWidth() - padding;
        //Y-axis
        context.strokeLine(padding , padding / 2,
                           padding , y + padding / 2);
        //X-axis
        context.strokeLine(padding / 2    , y+1,
                           x + padding / 2, y+1);
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
}
