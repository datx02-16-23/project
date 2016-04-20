package application.visualization.render2d;

import java.util.Arrays;

import application.model.Model;
import javafx.animation.Timeline;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import wrapper.Locator;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;
import wrapper.operations.OP_ReadWrite;
import wrapper.operations.OP_Swap;

public abstract class ARender extends StackPane {

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

    public ARender (DataStructure struct, double width, double height, double hspace, double vspace){
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
        animated.setOnScroll(event -> {
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

    public abstract void render ();

    public abstract void animate (Element e, int targetX, int targetT);

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

    public double[][] generatePoints (double x1, double y1, double x2, double y2, int points){
        double[][] ans = new double[2][points];
        System.out.println("x1 = " + x1);
        System.out.println("x2 = " + x2);
        double xstep = (x2 - x1) / points;
        double k = (y2 - y1) / (x2 - x1);
        double m = y1 - k * x1;
        double x = xstep;
        for (int i = 0; i < points; i++) {
            x += xstep;
            ans[0][i] = x;
            ans[1][i] = k * i + m;
        }
        System.out.println(Arrays.toString(ans[0]));
        System.out.println(Arrays.toString(ans[1]));
        return ans;
    }

    public static class AnimatedOperation {

        public static final short FRAMES   = 100;
        private short             frame    = 0;
        private final Timeline    timeline = new Timeline();

        public static AnimatedOperation getAnimatedOperation(OP_ReadWrite op){
            Model.instance().getStructures().values();
            Locator source = op.getSource();
            if(source == null){
                return null;
            }
            Element e;
            for(DataStructure struct : Model.instance().getStructures().values()){
                e = struct.getElement(source);
                if(e != null){
                    break;
                }
            }
            
            
            
            AnimatedOperation ae;
            
            return null;
        }
        
        private AnimatedOperation (Element e, double x_end, double y_end){
            
        }
    }
}
