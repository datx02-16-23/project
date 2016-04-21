package application.visualization;

import java.util.ArrayList;

import application.gui.Main;
import application.model.Model;
import application.visualization.render2d.*;
import application.visualization.render2d.MatrixRender.Order;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import wrapper.Locator;
import wrapper.Operation;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;
import wrapper.operations.OP_ReadWrite;
import wrapper.operations.OP_Swap;
import wrapper.operations.OperationType;

public class Visualization extends StackPane {

    private final Model          model;
    private static Visualization INSTANCE;
    private final GridPane       RENDERS  = new GridPane();
    public final Canvas          ANIMATED = new Canvas();

    public static Visualization instance (){
        if (INSTANCE == null) {
            INSTANCE = new Visualization();
        }
        return INSTANCE;
    }

    private Visualization (){
        this.model = Model.instance();
        //Build Canvas
        ANIMATED.setMouseTransparent(true);
        ANIMATED.widthProperty().bind(this.widthProperty());
        ANIMATED.heightProperty().bind(this.heightProperty());
        ANIMATED.maxWidth(Double.MAX_VALUE);
        ANIMATED.maxHeight(Double.MAX_VALUE);
        ANIMATE = true;
        //Add stacked canvases
        this.getChildren().add(RENDERS);
        this.getChildren().add(ANIMATED);
    }

    public void clear (){
        RENDERS.getChildren().clear();
        ANIMATED.getGraphicsContext2D().clearRect(0, 0, this.getWidth(), this.getHeight());
    }

    public void clearAndCreateVisuals (){
        System.out.println("create!");
        clear();
        int loc = 0;
        System.out.println("RENDERS.getChildren().size() = " + RENDERS.getChildren().size());
        System.out.println("model.getStructures().values() = " + model.getStructures().values());
        for (DataStructure struct : model.getStructures().values()) {
            if (struct.rawType == "independentElement") {
//                Render render = new IndependentElementRender(struct);
//                root.add(render, loc++, 0);
//                RENDERS.add(render);
                continue;
            }
            Render render = resolveRender(struct);
            if (render != null) {
                render.setPrefWidth(this.getWidth());
                render.setPrefHeight(this.getHeight());
                RENDERS.add(render, loc++, 0);
                continue;
            }
        }
    }

    /**
     * Determines the model to use for this DataStructure. Will iteratively
     * 
     * @param struct The DataStructure to assign a Render to.
     */
    private Render resolveRender (DataStructure struct){
        Render render = null;
        String visual = struct.visual == null ? "NULL" : struct.visual;
        outer: for (int attempt = 1; attempt < 3; attempt++) {
            switch (visual) {
                case "bar":
                    render = new BarRender(struct, 50, 1, 5, 25);
                    break outer;
                case "box":
                    render = new MatrixRender(struct, Order.COLUMN_MAJOR, 50, 50, 50, 0);
                    break outer;
                case "tree":
                    render = new KTreeRender(struct, struct.visualOptions, 50, 50, 20, 10);
                    break outer;
                default:
                    /*
                     * Visual null or unknown.
                     */
                    switch (attempt) {
                        case 0: //Fallback 1
                            visual = struct.getAbstractVisual();
                            break;
                        case 1: //Fallback 2
                            visual = struct.getRawVisual();
                            break;
                        default:
                            Main.console.err("Unable to determine Visual style for: " + struct);
                            return null;
                    }
                    break;
            }
        }
        return render;
    }

    /**
     * Should be called whenever model is updated, does a complete rerender of the structures.
     */
    public void render (Operation op){
        Render render;
        for (Object node : RENDERS.getChildren()) {
            render = (Render) node;
            render.render();
        }
        if (ANIMATE && op != null) {
            animate(op);
        }
    }

    public void animate (Operation op){
        if (op.operation == OperationType.read & op.operation == OperationType.write) {
            animateReadWrite((OP_ReadWrite) op);
        }
        else if (op.operation == OperationType.swap) {
            animateSwap((OP_Swap) op);
        }
    }

    public void animateReadWrite (OP_ReadWrite rw){
        Locator source = rw.getSource();
        if (source == null) {
            return;
        }
        Element e;
        for (DataStructure struct : model.getStructures().values()) {
            e = struct.getElement(source);
            if (e != null) {
                struct.getAnimatedElements().add(e);
                break;
            }
        }
    }

    public void animateSwap (OP_Swap swap){
        Locator var1 = swap.getVar1();
        Locator var2 = swap.getVar2();
        Element e;
        for (DataStructure struct : model.getStructures().values()) {
            e = struct.getElement(var1);
            if (e != null) {
                struct.getAnimatedElements().add(e);
            }
            e = struct.getElement(var2);
            if (e != null) {
                struct.getAnimatedElements().add(e);
            }
        }
    }

    public void clean (){
        ANIMATED.getGraphicsContext2D().clearRect(0, 0, Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public boolean ANIMATE;

    public void setAnimate (boolean value){
        if (value == ANIMATE) {
            return;
        }
        ANIMATE = value;
        //TODO
    }
}