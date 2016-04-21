package application.visualization.render2d;

import java.util.List;

import application.gui.Main;
import application.visualization.Visualization;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;
import wrapper.datastructures.Array;
import wrapper.datastructures.Array.ArrayElement;

public class MatrixRender extends Render {

    public static final double DEFAULT_SIZE           = 40;
    private final Order        mo;
    private int                elementsPreviousRender = 0;
    private int                dimensions;
    private int[]              size;
    private static final int   PADDING                = 40;

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
     * Create a new BoxRender with the default settings.
     * 
     * @param struct The structure to draw as a Matrix.
     */
    public MatrixRender (DataStructure struct){
        this(struct, Order.ROW_MAJOR, DEFAULT_SIZE, DEFAULT_SIZE, 0, 0);
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
            List<Element> animatedElements = struct.getAnimatedElements();
            for (Element e : struct.getElements()) {
                if (animatedElements.contains(e)) {
                    continue; //Animated elements are handled seperately.
                }
                else if (modifiedElements.contains(e)) {
                    drawElement(e, e.getColor());
                }
                else if (resetElements.contains(e)) {
                    drawElement(e, null);
                }
            }
        }
        struct.elementsDrawn();
    }

    /**
     * Create and render all elements.
     */
    private void init (){
        Array a = (Array) struct;
        size = a.getCapacity() == null ? new int[] {struct.getElements().size(), 1} : a.getCapacity();
        calculatePrefSize();
        GraphicsContext context = LOCAL_STATIONARY.getGraphicsContext2D();
        context.clearRect(0, 0, this.WIDTH, this.HEIGHT);
        context.setFill(COLOR_BLACK);
        context.fillText(struct.toString(), hspace, vspace + 10);
        if (struct.getElements().isEmpty() == false) {
            ArrayElement ae = (ArrayElement) struct.getElements().get(0);
            dimensions = ae.getIndex().length;
            if (dimensions != 2 && dimensions != 1) {
                Main.console.force("WARNING: Structure " + struct + " has declared " + dimensions + " dimensions. MatrixRender supports only one or two dimensions.");
            }
        }
        for (Element e : struct.getElements()) {
            drawElement(e, null);
        }
        drawIndicies();
    }

    @Override
    public void calculatePrefSize (){
        super.calculatePrefSize();
        WIDTH = PADDING * 2 + vspace + (vspace + node_width) * size[0];
        HEIGHT = PADDING * 2 + hspace + (hspace + node_height) * size[1];
        this.setPrefSize(WIDTH, HEIGHT);
    }

    private double getX (int column){
        return PADDING + vspace + (vspace + node_width) * column;
    }

    private double getY (int row){
        return PADDING + hspace + (hspace + node_height) * row;
    }

    /**
     * Draw an element.
     * 
     * @param value The value to print in this node.
     * @param index The index of this element.
     * @param style The style for this element.
     */
    private void drawElement (Element e, String style){
        //Dispatch
        drawNode(e.getValue() + "", this.getX(e), this.getY(e), getFillColor(style), ((ArrayElement) e).getIndex(), LOCAL_STATIONARY);
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
    private void drawNode (String value, double x, double y, Color fill, int[] index, Canvas c){
        GraphicsContext context = c.getGraphicsContext2D();
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
        GraphicsContext context = LOCAL_STATIONARY.getGraphicsContext2D();
        context.setFill(COLOR_BLACK);
        //Column numbering
        if (size[0] > 1) {
            for (int i = 0; i < size[0]; i++) {
                context.fillText("[" + i + "]", getX(i), PADDING - 10);
            }
        }
        //Row numbering
        if (size[1] > 1) {
            for (int i = 0; i < size[1]; i++) {
                context.fillText("[" + i + "]", 5, getY(i) - node_height / 2);
            }
        }
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

    @Override
    public void animate (Element e, double x_end, double y_end){
        Array struct = (Array) this.struct;
        int index = struct.getElements().indexOf(e);
        ArrayElement ae = (ArrayElement) struct.getElements().get(index);
        double[][] points = generatePoints(getX(ae.getIndex()[0]), getY(0), x_end, y_end, 100);
        Timeline tl = new Timeline();
        tl.setCycleCount(100);
        step = 0;
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(1500 / 100), new EventHandler<ActionEvent>() {

            @Override
            public void handle (ActionEvent actionEvent){
                drawNode(ae.getValue() + "", points[0][step], points[1][step], getFillColor(ae.getColor()), ae.getIndex(), SHARED_ANIMATED);
                step++;
            }
        }));
        tl.setOnFinished(event -> {
            System.out.println("done!");
        });
        tl.playFromStart();
    }

    int step = 0;

    @Override
    public double getX (Element e){
        int[] index = ((ArrayElement) e).getIndex();
        double x = 0;
        if (mo == Order.COLUMN_MAJOR) {
            x = getX(index[0]);
        }
        else {
            if (dimensions == 2) {
                x = getY(index[1]);
            }
        }
        return x;
    }

    @Override
    public double getY (Element e){
        int[] index = ((ArrayElement) e).getIndex();
        double y = PADDING;
        if (mo == Order.COLUMN_MAJOR) {
            if (dimensions == 2) {
                y = getY(index[1]);
            }
        }
        else {
            y = getX(index[0]);
        }
        return y;
    }
}
