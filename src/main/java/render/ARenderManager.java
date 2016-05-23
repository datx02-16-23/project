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
    /**
     * The data structure this thingy is responsible for.
     */
    private final DataStructure struct;

    /**
     * The pane used for animation.
     */
    private final Pane animPane;

    /**
     * Mapping of renders for the structure.
     */
    private final HashMap<VisualType, ARender> renders = new HashMap<VisualType, ARender>();

    /**
     * The current render for the structure.
     */
    private ARender curRender;

    // Used to maintain settings when changing renders.
    private double scaleX = 1;
    private double scaleY = 1;
    private double translateX = 0;
    private double translateY = 0;
    private double layoutX = 0;
    private double layoutY = 0;
    private ARender prevRender;

    public boolean translateOnVisualTypeChange = true;

    /**
     * Create a new thingy.
     * 
     * @param struct
     *            The data structure being visualized.
     * @param animation_container
     *            Container for animation.
     */
    public ARenderManager(DataStructure struct, Pane animation_container) {
	struct.resolveVisual();

	this.struct = struct;
	this.animPane = animation_container;
	this.setPickOnBounds(false); // Mouse fix.

	setRender(struct.visual);
    }

    /**
     * Set the visual type to use for this Structure.
     * 
     * @param type
     *            The type to use.
     */
    public void setRender(VisualType type) {
	curRender = renders.get(type);

	if (curRender == null) { // Create new render for the structure.
	    // @formatter:off
	    curRender = ARenderFactory.resolveRender(struct, Const.DEFAULT_ELEMENT_WIDTH, Const.DEFAULT_ELEMENT_HEIGHT,
		    Const.DEFAULT_RENDER_WIDTH, Const.DEFAULT_RENDER_HEIGHT);
	    // @formatter:on
	    renders.put(struct.resolveVisual(), curRender);
	}

	struct.setListener(this);

	initRender();
	setCenter(curRender);
	if (type == VisualType.single) {
	    this.toFront(); // Single element renders are small.
	}
    }

    private void initRender() {
	if (translateOnVisualTypeChange && prevRender != null) {
	    scaleX = prevRender.getScaleX();
	    scaleY = prevRender.getScaleY();
	    translateX = prevRender.getTranslateX();
	    translateY = prevRender.getTranslateY();
	    layoutX = prevRender.getLayoutX();
	    layoutY = prevRender.getLayoutY();

	    curRender.setScaleX(scaleX);
	    curRender.setScaleX(scaleY);
	    curRender.setTranslateX(translateX);
	    curRender.setTranslateY(translateY);
	    curRender.setLayoutX(layoutX);
	    curRender.setLayoutY(layoutY);
	}

	curRender.repaintAll();
	curRender.updateInfoLabels();
	animPane.getChildren().remove(curRender.getAnimationPane());
	animPane.getChildren().add(curRender.getAnimationPane());
	prevRender = curRender;
    }

    @Override
    public void visualChanged(VisualType newVisual) {
	setRender(newVisual);
    }

    /**
     * Force the current Render to initialise.
     */
    public void init() {
	curRender.repaintAll();
    }

    /**
     * Returns the current Render for the structure.
     * 
     * @return The current Render for the structure.
     */
    public ARender getRender() {
	if(curRender == null){
	    setRender(struct.resolveVisual());
	}
	return curRender;
    }

    public String toString() {
	return struct.identifier + ": " + renders.values();
    }

    /**
     * The data structure this thingy is responsible for.
     * 
     * @return A DataStructure.
     */
    public DataStructure getStructure() {
	return struct;
    }

    /**
     * Reset the renders held by this manager.
     */
    public void reset() {
	for (ARender r : renders.values()) {
	    r.reset();
	}
    }
}
