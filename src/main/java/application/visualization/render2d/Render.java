package application.visualization.render2d;

import java.util.Arrays;

import application.visualization.Visualization;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

public abstract class Render extends Pane {

    public static final double    DEFAULT_SIZE           = 40;
    protected final double        node_width, node_height;
    protected final double        hspace, vspace;
    protected double              WIDTH, HEIGHT;
    protected final DataStructure struct;
    protected int                 elementsPreviousRender = 0;
    protected final Canvas        local_canvas       = new Canvas();
    protected static final Canvas SHARED_ANIMATED        = Visualization.instance().ANIMATED;
    protected static final Color  COLOR_READ             = Color.valueOf(Element.COLOR_READ);
    protected static final Color  COLOR_WRITE            = Color.valueOf(Element.COLOR_WRITE);
    protected static final Color  COLOR_SWAP             = Color.valueOf(Element.COLOR_SWAP);
    protected static final Color  COLOR_INACTIVE         = Color.valueOf(Element.COLOR_INACTIVE);
    protected static final Color  COLOR_WHITE            = Color.WHITE;
    protected static final Color  COLOR_BLACK            = Color.BLACK;

    public Render (DataStructure struct, double width, double height, double hspace, double vspace){
        this.struct = struct;
        //Sizing and spacing
        this.node_width = width;
        this.node_height = height;
        this.hspace = hspace;
        this.vspace = vspace;
//        local.widthProperty().bind(this.widthProperty());
//        local.heightProperty().bind(this.heightProperty());
        local_canvas.setWidth(2000);
        local_canvas.setHeight(2000);
        //Add stacked canvases
        this.getChildren().add(local_canvas);
        initDragAndZoom();
    }

    public abstract void render ();

    /**
     * Draw an element using the animation canvas. Style can be fetched using {@code e.getColor()}, or use {@code null}
     * for the default color.
     * 
     * @param e The element to draw.
     * @param x The absolute x-coordinate.
     * @param y The absolute y-coordinate.
     * @param style The style to use (null = default)
     */
    public abstract void drawAnimatedElement (Element e, double x, double y, String style);

    /**
     * Makes an effort to clears an animated element from the shared animation canvas. <br>
     * It may be necessary to shadow this method if it doesn't work properly.
     * 
     * @param e The element to clear.
     * @param x The x-coordinate to clear.
     * @param y The y-coordinate to clear.
     */
    public void clearAnimatedElement (Element e, double x, double y){
        SHARED_ANIMATED.getGraphicsContext2D().clearRect(x - 4, y - 4, node_width + 8, node_height + 8);
    }

    /**
     * Get the fill color for the center. Returns white for style == null and black as default.
     * 
     * @param style The style to return a color for.
     * @return A color.
     */
    public final Color getFillColor (String style){
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
     * Returns the absolute x-coordinate of an element.
     * 
     * @param e An element to resolve coordinates for.
     * @return The absolute x-coordinate of the element.
     */
    public abstract double getX (Element e);

    /**
     * Returns the absolute y-coordinate of an element.
     * 
     * @param e An element to resolve coordinates for.
     * @return The absolute y-coordinate of the element.
     */
    public abstract double getY (Element e);

    /**
     * Order the Render to calculate it's preferred size. Should be shadowed by inheriting types.
     */
    public void calculatePrefSize (){
        local_canvas.getGraphicsContext2D().clearRect(0, 0, local_canvas.getWidth(), local_canvas.getHeight());
    }

    //Drag and Zoom
    private double transX, transY;
    private double scale = 1;
    private int    sign  = 1;

    /**
     * Create listeners to drag and zoom.
     */
    private void initDragAndZoom (){
        /*
         * Zoom
         */
        local_canvas.setOnScroll(event -> {
            sign = event.getDeltaY() > 0 ? 1 : -1;
            scale = scale + sign * 0.1;
            if (scale < 0.1) {
                scale = 0.1;
                return;
            }
            else if (scale > 2) {
                scale = 2;
                return;
            }
            local_canvas.setScaleX(scale);
            local_canvas.setScaleY(scale);
        });
        /*
         * Drag
         */
        // Record a delta distance for the drag and drop operation.
        local_canvas.setOnMousePressed(event -> {
            transX = local_canvas.getTranslateX() - event.getSceneX();
            transY = local_canvas.getTranslateY() - event.getSceneY();
            local_canvas.setCursor(Cursor.MOVE);
        });
        // Restore cursor
        local_canvas.setOnMouseReleased(event -> {
            local_canvas.setCursor(Cursor.HAND);
        });
        // Translate canvases
        local_canvas.setOnMouseDragged(event -> {
            local_canvas.setTranslateX(event.getSceneX() + transX);
            local_canvas.setTranslateY(event.getSceneY() + transY);
        });
        // Set cursor
        local_canvas.setOnMouseEntered(event -> {
            local_canvas.setCursor(Cursor.HAND);
        });
    }

    /**
     * Return the DataStructure held by this Render.
     * 
     * @return The DataStructure held by this Render.
     */
    public DataStructure getDataStructure (){
        return struct;
    }

    /**
     * Start an animation of an element to a point.
     * 
     * @param e The element to animate.
     * @param x End point x-coordinate.
     * @param y End point y-coordinate.
     */
    public abstract void startAnimation (Element e, double x, double y);

    /**
     * Returns the Canvas for this Render.
     * @return The Canvas for this Render.
     */
    public Canvas getCanvas(){
        return local_canvas;
    }
    /**
     * Returns the absolute x-coordinate for the element e.
     * @param owner The owner of the element.
     * @param e An element in owner.
     * @return The absolute x-coordinates of e.
     */
    public static double getAbsoluteX(Render owner, Element e){
        double bx = owner.getCanvas().getTranslateX() + owner.getLayoutX();
        return owner.getX(e) + bx;
    }
    
    /**
     * Returns the absolute y-coordinate for the element e.
     * @param owner The owner of the element.
     * @param e An element in owner.
     * @return The absolute y-coordinates of e.
     */
    public static double getAbsoluteY(Render owner, Element e){
        double by = owner.getCanvas().getTranslateY() + owner.getLayoutY();
        return owner.getY(e) + by;
    }
}
