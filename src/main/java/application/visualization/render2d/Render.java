package application.visualization.render2d;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

public abstract class Render extends StackPane {

    public static final double    DEFAULT_SIZE           = 40;
    protected final double        node_width, node_height;
    protected final double        hspace, vspace;
    protected final Canvas        stationary, animated;
    protected final DataStructure struct;
    protected int                 elementsPreviousRender = 0;
    protected static final Color  COLOR_READ             = Color.valueOf(Element.COLOR_READ);
    protected static final Color  COLOR_WRITE            = Color.valueOf(Element.COLOR_WRITE);
    protected static final Color  COLOR_SWAP             = Color.valueOf(Element.COLOR_SWAP);
    protected static final Color  COLOR_INACTIVE         = Color.valueOf(Element.COLOR_INACTIVE);
    protected static final Color  COLOR_WHITE            = Color.WHITE;
    protected static final Color  COLOR_BLACK            = Color.BLACK;
    private double                transX, transY;
    private double                scale                  = 1;
    private int                   sign                   = 1;

    public Render (DataStructure struct, double width, double height, double hspace, double vspace){
        this.struct = struct;
        //Build Canvas
        stationary = new Canvas();
        stationary.setMouseTransparent(true);
        animated = new Canvas();
        initDragAndZoom();
        stationary.widthProperty().bind(this.maxWidthProperty());
        stationary.heightProperty().bind(this.maxHeightProperty());
        stationary.maxWidth(Double.MAX_VALUE);
        stationary.maxHeight(Double.MAX_VALUE);
        animated.widthProperty().bind(this.maxWidthProperty());
        animated.heightProperty().bind(this.maxHeightProperty());
        animated.maxWidth(Double.MAX_VALUE);
        animated.maxHeight(Double.MAX_VALUE);
        //Sizing and spacing
        this.node_width = width;
        this.node_height = height;
        this.hspace = hspace;
        this.vspace = vspace;
        this.getChildren().add(stationary);
        this.getChildren().add(animated);
        this.setMinSize(200, 100);
        this.setMaxSize(200, 100);
    }

    /**
     * Create listeners to drag and zoom.
     */
    private void initDragAndZoom (){
        /*
         * Zoom
         */
        stationary.setOnContextMenuRequested(event -> {
            scale = scale + sign * 0.1;
            if (scale < 0.1) {
                scale = 2;
            }
            else if (scale > 2) {
                scale = 0.25;
            }
            stationary.setScaleX(scale);
            stationary.setScaleY(scale);
            animated.setScaleX(scale);
            animated.setScaleY(scale);
        });
        /*
         * Drag
         */
        // Record a delta distance for the drag and drop operation.
        animated.setOnMousePressed(event -> {
            transX = animated.getTranslateX() - event.getSceneX();
            transY = animated.getTranslateY() - event.getSceneY();
            animated.setCursor(Cursor.MOVE);
        });
        // Restore cursor
        animated.setOnMouseReleased(event -> {
            animated.setCursor(Cursor.HAND);
        });
        // Translate canvases
        animated.setOnMouseDragged(event -> {
            stationary.setTranslateX(event.getSceneX() + transX);
            stationary.setTranslateY(event.getSceneY() + transY);
            animated.setTranslateX(event.getSceneX() + transX);
            animated.setTranslateY(event.getSceneY() + transY);
        });
        // Set cursor
        animated.setOnMouseEntered(event -> {
            stationary.setCursor(Cursor.HAND);
        });
    }

    //TODO: abstract
    public void render (){
        GraphicsContext context = this.animated.getGraphicsContext2D();
        context.setFill(Color.AQUAMARINE);
        context.clearRect(0, 0, animated.getWidth(), animated.getWidth());
        context.fillRect(Math.random() * animated.getWidth(), Math.random() * animated.getHeight(), 50, 50);
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
}
