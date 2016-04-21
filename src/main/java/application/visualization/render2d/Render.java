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
    protected final Canvas        LOCAL_STATIONARY       = new Canvas();
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
        LOCAL_STATIONARY.setWidth(2000);
        LOCAL_STATIONARY.setHeight(2000);
        //Add stacked canvases
        this.getChildren().add(LOCAL_STATIONARY);
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
     * Clears an animated element from the shared animation canvas. <br>
     * Assumed a border thickness of 1. It may be necessary to shadow this method if it doesn't work properly.
     * 
     * @param e The element to clear.
     * @param x The x-coordinate to clear.
     * @param y The y-coordinate to clear.
     */
    public void clearAnimatedElement (Element e, double x, double y){
        SHARED_ANIMATED.getGraphicsContext2D().clearRect(x - 1, y - 1, node_width + 2, node_height + 2);
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
        LOCAL_STATIONARY.getGraphicsContext2D().clearRect(0, 0, LOCAL_STATIONARY.getWidth(), LOCAL_STATIONARY.getHeight());
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
        LOCAL_STATIONARY.setOnScroll(event -> {
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
            LOCAL_STATIONARY.setScaleX(scale);
            LOCAL_STATIONARY.setScaleY(scale);
        });
        /*
         * Drag
         */
        // Record a delta distance for the drag and drop operation.
        LOCAL_STATIONARY.setOnMousePressed(event -> {
            transX = LOCAL_STATIONARY.getTranslateX() - event.getSceneX();
            transY = LOCAL_STATIONARY.getTranslateY() - event.getSceneY();
            LOCAL_STATIONARY.setCursor(Cursor.MOVE);
        });
        // Restore cursor
        LOCAL_STATIONARY.setOnMouseReleased(event -> {
            LOCAL_STATIONARY.setCursor(Cursor.HAND);
        });
        // Translate canvases
        LOCAL_STATIONARY.setOnMouseDragged(event -> {
            LOCAL_STATIONARY.setTranslateX(event.getSceneX() + transX);
            LOCAL_STATIONARY.setTranslateY(event.getSceneY() + transY);
        });
        // Set cursor
        LOCAL_STATIONARY.setOnMouseEntered(event -> {
            LOCAL_STATIONARY.setCursor(Cursor.HAND);
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
     * Called by Animation when an animation is finished.
     * 
     * @param e The element which has finished animating.
     */
    public void animationComplete (Element e){
        struct.getAnimatedElements().remove(e);
    }
    
    /**
     * Returns the Canvas for this Render.
     * @return The Canvas for this Render.
     */
    public Canvas getCanvas(){
        return LOCAL_STATIONARY;
    }
}
