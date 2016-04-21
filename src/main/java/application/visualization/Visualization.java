package application.visualization;

import java.util.HashMap;

import application.gui.Main;
import application.model.Model;
import application.visualization.animation.Animation;
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

/**
 * Handler class for visualisations and animations. The ANIMATED Canvas should only be used for moving objects as it is
 * cleared every iteration.
 * 
 * @author Richard Sundqvist
 *
 */
public class Visualization extends StackPane {

    private boolean                       animate;
    private final Model                   model;
    private static Visualization          INSTANCE;
    private final GridPane                RENDERS               = new GridPane();
    public final Canvas                   ANIMATED              = new Canvas();
    private final HashMap<String, Render> struct_render_mapping = new HashMap<String, Render>();

    /**
     * Returns the static instance of Visualization.
     * 
     * @return The static Visualization instance.
     */
    public static Visualization instance (){
        if (INSTANCE == null) {
            INSTANCE = new Visualization();
        }
        return INSTANCE;
    }

    /**
     * Create a new Visualization.
     */
    public Visualization (){
        this.model = Model.instance();
        //Build Canvas
        ANIMATED.setMouseTransparent(true);
        ANIMATED.widthProperty().bind(this.widthProperty());
        ANIMATED.heightProperty().bind(this.heightProperty());
        ANIMATED.maxWidth(Double.MAX_VALUE);
        ANIMATED.maxHeight(Double.MAX_VALUE);
        animate = true;
        //Add stacked canvases
        this.getChildren().add(RENDERS);
        this.getChildren().add(ANIMATED);
    }

    public void clear (){
        struct_render_mapping.clear();
        RENDERS.getChildren().clear();
        ANIMATED.getGraphicsContext2D().clearRect(0, 0, this.getWidth(), this.getHeight());
    }

    public void clearAndCreateVisuals (){
        clear();
        int reg = 0;
        int small = 0;
        for (DataStructure struct : model.getStructures().values()) {
            Render render = resolveRender(struct);
            if (struct.rawType == "independentElement") {
                render = new MatrixRender(struct);
                RENDERS.add(render, 1, small++);
            }
            else {
                render.setPrefWidth(this.getWidth());
                render.setPrefHeight(this.getHeight());
                RENDERS.add(render, 0, reg++);
            }
            struct_render_mapping.put(struct.identifier, render);
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
        if (animate && op != null) {
            Animation.finishAll();
            cleanAnimatedCanvas();
            animate(op);
        }
    }
    
    /**
     * Set the animation time in millisections for <b>ALL</b> animations.
     * 
     * @param millis The new animation time in milliseconds.
     */
    public static final void setAnimationTime (long millis){
        Animation.setAnimationTime(millis);
    }

    public void animate (Operation op){
        if (op.operation == OperationType.read || op.operation == OperationType.write) {
            animateReadWrite((OP_ReadWrite) op);
        }
        else if (op.operation == OperationType.swap) {
            animateSwap((OP_Swap) op);
        }
    }

    public void animateReadWrite (OP_ReadWrite rw){
        Locator source = rw.getSource();
        Locator target = rw.getTarget();
        if (source == null || target == null) {
            return;
        }
        Element src_e = null, tar_e = null;
        Render src_render = null, tar_render = null;
        /**
         * Source params
         */
        for (DataStructure struct : model.getStructures().values()) {
            src_e = struct.getElement(source);
            if (src_e != null) {
                src_render = this.struct_render_mapping.get(struct.identifier);
                struct.getAnimatedElements().add(src_e);
                break;
            }
        }
        /**
         * Target params
         */
        for (DataStructure struct : model.getStructures().values()) {
            tar_e = struct.getElement(target);
            if (tar_e != null) {
                tar_render = this.struct_render_mapping.get(struct.identifier);
                break;
            }
        }
        /**
         * Start animations
         */
        src_render.startAnimation(src_e, tar_render.getX(tar_e), tar_render.getY(tar_e));
    }

    /**
     * Trigger an animation of a swap.
     * 
     * @param swap The swap to animate.
     */
    public void animateSwap (OP_Swap swap){
        Locator var1 = swap.getVar1();
        Locator var2 = swap.getVar2();
        if (var1 == null || var2 == null) {
            return;
        }
        Element v1_e = null, v2_e = null;
        Render v1_render = null, v2_render = null;
        /**
         * Var1 params
         */
        for (DataStructure struct : model.getStructures().values()) {
            v1_e = struct.getElement(var1);
            if (v1_e != null) {
                v1_render = this.struct_render_mapping.get(struct.identifier);
                struct.getAnimatedElements().add(v1_e);
                break;
            }
        }
        /**
         * Var2 params
         */
        for (DataStructure struct : model.getStructures().values()) {
            v2_e = struct.getElement(var2);
            if (v2_e != null) {
                v2_render = this.struct_render_mapping.get(struct.identifier);
                struct.getAnimatedElements().add(v2_e);
                break;
            }
        }
        /**
         * Start animations
         */
        v1_render.startAnimation(v1_e, v2_render.getX(v2_e), v2_render.getY(v2_e));
        v2_render.startAnimation(v2_e, v2_render.getX(v1_e), v2_render.getY(v1_e));
    } //End animate swap

    public void cleanAnimatedCanvas (){
        ANIMATED.getGraphicsContext2D().clearRect(0, 0, 5000, 5000);
    }

    /**
     * Toggles animation on and off.
     * 
     * @param value The new animation option.
     */
    public void setAnimate (boolean value){
        if (value == animate) {
            return;
        }
//        ANIMATE = value;
        //TODO
    }
}