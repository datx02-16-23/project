package draw;

import java.util.HashMap;

import assets.DasConstants;
import contract.datastructure.DataStructure;
import contract.datastructure.VisualType;
import contract.datastructure.DataStructure.VisualListener;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * Class for maintaining visualizations for a structure.
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
	private final Pane animation_pane;

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

	/**
	 * Create a new thingy.
	 * 
	 * @param struct
	 *            The data structure being visualized.
	 * @param animation_container
	 *            Container for animation.
	 */
	public ARenderManager(DataStructure struct, Pane animation_container) {
		this.struct = struct;
		this.animation_pane = animation_container;
		this.setPickOnBounds(false); //Mouse fix.
		
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
			curRender = resolveRender(struct);
			renders.put(struct.resolveVisual(), curRender);
		}

		struct.setListener(this);

		initRender();
		setCenter(curRender);
		if(type == VisualType.single){
			this.toFront(); //Single element renders are small.
		}
	}

	private void initRender() {
		if (prevRender != null) {
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
		
		curRender.init();
		curRender.updateInfoLabels();
		animation_pane.getChildren().add(curRender.getAnimationPane());
		prevRender = curRender;
	}

	/**
	 * Determines and creates a Render for use by this DataStructure.
	 * 
	 * @param struct
	 *            The DataStructure to create a Render for.
	 */
	public ARender resolveRender(DataStructure struct) {
		VisualType visual = struct.resolveVisual();
		switch (visual) {
		case bar:
			curRender = new BarchartRender(struct, 30, DasConstants.DEFAULT_RENDER_HEIGHT, 10, 10);
			break;
		case box:
			curRender = new GridRender(struct, GridRender.Order.resolve(struct.visualOption), 40, 40, 3, 3);
			break;
		case tree:
			curRender = new KTreeRender(struct, struct.visualOption, 40, 40, 5, 5);
			break;
		case single:
			curRender = new SingleElementRender(struct, 80, 40);
			break;
		}
//		render.setAnimationPane(animation_pane == null ? render.getNodes() : animation_pane);
		return curRender;
	}

	@Override
	public void visualChanged(VisualType newVisual) {
		this.setRender(newVisual);
	}
	
	/**
	 * Force the current Render to initialise.
	 */
	public void init(){
		curRender.init();
	}

	/**
	 * Returns the current Render for the structure.
	 * 
	 * @return The current Render for the structure.
	 */
	public ARender getRender() {
		return curRender;
	}
	
	public String toString(){
		return struct.identifier + ": " + renders.values();
	}
	
	/**
	 * The data structure this thingy is responsible for.
	 * @return A DataStructure.
	 */
	public DataStructure getStructure(){
		return struct;
	}
}
