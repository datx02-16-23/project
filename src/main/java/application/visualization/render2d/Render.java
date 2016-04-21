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

    public abstract void animate (Element e, double end_x, double end_y);

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

    public abstract double getX (Element e);

    public abstract double getY (Element e);

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
}
