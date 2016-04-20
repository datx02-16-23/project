package application.visualization.render2d;

import java.util.ArrayList;

import application.visualization.render2d.MatrixRender.Order;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

public abstract class Render extends Pane {

    public static final double    DEFAULT_SIZE           = 40;
    protected final double        node_width, node_height;
    protected final double        hspace, vspace;
    protected final Canvas        canvas;
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
        canvas = new Canvas();
        moveAndZoom();
        canvas.widthProperty().bind(this.maxWidthProperty());
        canvas.heightProperty().bind(this.maxHeightProperty());
        canvas.maxWidth(Double.MAX_VALUE);
        canvas.maxHeight(Double.MAX_VALUE);
        //Sizing and spacing
        this.node_width = width;
        this.node_height = height;
        this.hspace = hspace;
        this.vspace = vspace;
        this.getChildren().add(canvas);
        this.setMinSize(200, 100);
        this.setMaxSize(200, 100);
    }

    /**
     * Create listeners to zoom and move.
     */
    private void moveAndZoom (){
        canvas.setOnContextMenuRequested(event -> {
            scale = scale + sign * 0.1;
            if (scale < 0.1) {
                scale = 2;
            }
            else if (scale > 2) {
                scale = 0.25;
            }
            canvas.setScaleX(scale);
            canvas.setScaleY(scale);
        });
        canvas.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle (MouseEvent mouseEvent){
                // record a delta distance for the drag and drop operation.
                transX = canvas.getLayoutX() - mouseEvent.getSceneX();
                transY = canvas.getLayoutY() - mouseEvent.getSceneY();
                canvas.setCursor(Cursor.MOVE);
            }
        });
        canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle (MouseEvent mouseEvent){
                canvas.setCursor(Cursor.HAND);
            }
        });
        canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle (MouseEvent mouseEvent){
                canvas.setLayoutX(mouseEvent.getSceneX() + transX);
                canvas.setLayoutY(mouseEvent.getSceneY() + transY);
            }
        });
        canvas.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle (MouseEvent mouseEvent){
                canvas.setCursor(Cursor.HAND);
            }
        });
    }

    public abstract void render ();

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
