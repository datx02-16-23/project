package application.visualization.render2d;

import java.util.List;

import application.gui.Main;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;
import wrapper.datastructures.Array.ArrayElement;

public class MatrixRender extends Render {

    public static final double DEFAULT_SIZE           = 40;
    private final Order        mo;
    private int                elementsPreviousRender = 0;
    private int                dimensions;

    /**
     * Create a new BoxRender.
     * 
     * @param struct The structure to draw as a Matrix.
     * @param mo Indicates whether this MatrixRender is COLUMN or ROW major.
     * @param width The width of the nodes.
     * @param height The height of the nodes.
     * @param hspace The horizontal space between elements.
     * @param vspace The vertical space between elements.
     * @throws IllegalArgumentException If K < 2.
     */
    public MatrixRender (DataStructure struct, Order mo, double width, double height, double hspace, double vspace){
        super(struct, width, height, hspace, vspace);
        this.mo = mo;
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
                    drawElement(ae.getValue() + "", ae.getIndex(), e.getColor());
                }
                else if (resetElements.contains(e)) {
                    drawElement(ae.getValue() + "", ae.getIndex(), null);
                }
            }
        }
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
        if (struct.getElements().isEmpty() == false) {
            ArrayElement ae = (ArrayElement) struct.getElements().get(0);
            dimensions = ae.getIndex().length;
            if (dimensions != 2 || dimensions != 2) {
                Main.console.force("WARNING: Structure " + struct + " has declared " + dimensions + " dimensions. MatrixRender supports only one or two dimensions.");
            }
        }
        for (Element e : struct.getElements()) {
            ArrayElement ae = (ArrayElement) e;
            drawElement(ae.getValue() + "", ae.getIndex(), null);
        }
    }

    private void calculateSize (){
        //TODO
        double width = 500;
        double height = 500;
        this.setMinSize(width, height);
        this.setMaxSize(width, height);
    }

    private double getX (int column){
        return vspace + (vspace + node_width) * column;
    }

    private double getY (int row){
        return hspace + (hspace + node_height) * row;
    }

    /**
     * Draw an element.
     * 
     * @param value The value to print in this node.
     * @param index The index of this element.
     * @param style The style for this element.
     */
    private void drawElement (String value, int[] index, String style){
        double x = 0;
        double y = 0;
        if (mo == Order.COLUMN_MAJOR) {
            x = getX(index[0]);
            if (dimensions == 2) {
                y = getY(index[1]);
            }
        }
        else {
            y = getX(index[0]);
            if (dimensions == 2) {
                x = getY(index[1]);
            }
        }
        //Dispatch
        drawNode(value, x, y, getFillColor(style), index);
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
    private void drawNode (String value, double x, double y, Color fill, int[] index){
        GraphicsContext context = stationary.getGraphicsContext2D();
        context.setFill(fill);
        context.fillRect(x, y, node_width, node_height);
        //Outline, text, children
        context.setFill(COLOR_BLACK);
        context.setStroke(COLOR_BLACK);
        //Value
        context.strokeRect(x, y, node_width, node_height);
        final Text text = new Text(value);
        new Scene(new Group(text));
        text.applyCss();
        double tw = text.getLayoutBounds().getWidth();
//        double th = text.getLayoutBounds().getHeight();
        context.fillText(value, x + node_width / 2 - tw / 2, y + node_height / 2);
    }

    private void drawIndicies (){
        GraphicsContext context = stationary.getGraphicsContext2D();
        context.setFill(COLOR_BLACK);
//        context.strokeOval(x, y, node_width, node_height);
//        context.fillText("[" + index + "]", x + node_width + 4, y + node_height / 4);
    }

    public enum Order{
        ROW_MAJOR("Row Major", "The first index will indicate row.", 0), COLUMN_MAJOR("Column Major", "The first index will indicate column.", 1);

        public final String name;
        public final String description;
        public final int    optionNbr;

        private Order (String name, String description, int optionNbr){
            this.name = name;
            this.description = description;
            this.optionNbr = optionNbr;
        }
    }
}
