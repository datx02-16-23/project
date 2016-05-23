package render;

import java.util.HashMap;

import assets.Const;
import contract.datastructure.DataStructure;
import contract.datastructure.DataStructure.VisualListener;
import contract.datastructure.VisualType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * Class maintaining visualisations for a structure.
 *
 * @author Richard Sundqvist
 *
 */
public class ARenderManager extends BorderPane implements VisualListener {

    // ============================================================= //
    /*
     *
     * Field variables
     *
     */
    // ============================================================= //

    /**
     * The data structure this thingy is responsible for.
     */
    private final DataStructure                struct;

    /**
     * The pane used for animation.
     */
    private final Pane                         animPane;

    /**
     * Mapping of renders for the structure.
     */
    private final HashMap<VisualType, ARender> renders                     = new HashMap<VisualType, ARender>();

    /**
     * The current render for the structure.
     */
    private ARender                            curRender;

    // Used to maintain settings when changing renders.
    private double                             scaleX                      = 1;
    private double                             scaleY                      = 1;
    private double                             translateX                  = 0;
    private double                             translateY                  = 0;
    private double                             layoutX                     = 0;
    private double                             layoutY                     = 0;
    private ARender                            prevRender;

    public boolean                             translateOnVisualTypeChange = true;

    // ============================================================= //
    /*
     *
     * Constructors
     *
     */
    // ============================================================= //

    /**
     * Create a new thingy.
     *
     * @param struct
     *            The data structure being visualized.
     * @param animation_container
     *            Container for animation.
     */
    public ARenderManager (DataStructure struct, Pane animation_container) {
        struct.resolveVisual();

        this.struct = struct;
        this.animPane = animation_container;
        this.setPickOnBounds(false); // Mouse fix.

        this.setRender(struct.visual);
    }

    // ============================================================= //
    /*
     *
     * Setters and Getters
     *
     */
    // ============================================================= //

    /**
     * Set the visual type to use for this Structure.
     *
     * @param type
     *            The type to use.
     */
    public void setRender (VisualType type) {
        this.curRender = this.renders.get(type);

        if (this.curRender == null) { // Create new render for the structure.
        // @formatter:off
	    this.curRender = ARenderFactory.resolveRender(this.struct, Const.DEFAULT_ELEMENT_WIDTH, Const.DEFAULT_ELEMENT_HEIGHT,
		    Const.DEFAULT_RENDER_WIDTH, Const.DEFAULT_RENDER_HEIGHT);
	    // @formatter:on
            this.renders.put(this.struct.resolveVisual(), this.curRender);
        }

        this.struct.setListener(this);

        this.initRender();
        this.setCenter(this.curRender);
        if (type == VisualType.single) {
            this.toFront(); // Single element renders are small.
        }
    }

    /**
     * Returns the current Render for the structure.
     *
     * @return The current Render for the structure.
     */
    public ARender getRender () {
        if (this.curRender == null) {
            this.setRender(this.struct.resolveVisual());
        }
        return this.curRender;
    }

    /**
     * The data structure this thingy is responsible for.
     *
     * @return A DataStructure.
     */
    public DataStructure getStructure () {
        return this.struct;
    }

    @Override
    public String toString () {
        return this.struct.identifier + ": " + this.renders.values();
    }

    // ============================================================= //
    /*
     *
     * Controls
     *
     */
    // ============================================================= //

    private void initRender () {
        if (this.translateOnVisualTypeChange && this.prevRender != null) {
            this.scaleX = this.prevRender.getScaleX();
            this.scaleY = this.prevRender.getScaleY();
            this.translateX = this.prevRender.getTranslateX();
            this.translateY = this.prevRender.getTranslateY();
            this.layoutX = this.prevRender.getLayoutX();
            this.layoutY = this.prevRender.getLayoutY();

            this.curRender.setScaleX(this.scaleX);
            this.curRender.setScaleX(this.scaleY);
            this.curRender.setTranslateX(this.translateX);
            this.curRender.setTranslateY(this.translateY);
            this.curRender.setLayoutX(this.layoutX);
            this.curRender.setLayoutY(this.layoutY);
        }

        this.curRender.repaintAll();
        this.curRender.updateInfoLabels();
        this.animPane.getChildren().remove(this.curRender.getAnimationPane());
        this.animPane.getChildren().add(this.curRender.getAnimationPane());
        this.prevRender = this.curRender;
    }

    /**
     * Force the current Render to initialise.
     */
    public void init () {
        this.curRender.repaintAll();
    }

    /**
     * Reset the renders held by this manager.
     */
    public void reset () {
        for (ARender r : this.renders.values()) {
            r.reset();
        }
    }

    // ============================================================= //
    /*
     *
     * Interface methods
     *
     */
    // ============================================================= //

    @Override
    public void visualChanged (VisualType newVisual) {
        this.setRender(newVisual);
    }
}
