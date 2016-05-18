package draw;

import java.util.HashMap;

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
public class Manager extends BorderPane implements VisualListener {
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
	private final HashMap<VisualType, _Render> renders = new HashMap<VisualType, _Render>();

	/**
	 * The current render for the structure.
	 */
	private _Render render;

	// Used to maintain settings when changing renders.
	private double scaleX = 1;
	private double scaleY = 1;
	private double translateX = 0;
	private double translateY = 0;
	private double layoutX = 0;
	private double layoutY = 0;
	private _Render oldRender;

	/**
	 * Create a new thingy.
	 * 
	 * @param struct
	 *            The data structure being visualized.
	 * @param animation_pane
	 *            The Pane used for drawing animations. Will use own canvas if
	 *            null.
	 */
	public Manager(DataStructure struct, Pane animation_pane) {
		this.struct = struct;
		this.animation_pane = animation_pane;
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
		render = renders.get(type);

		if (render == null) { // Create new render for the structure.
			render = resolveRender(struct);
			renders.put(struct.resolveVisual(), render);
		}

		struct.setListener(this);

		copyTransform();
		setCenter(render);
		if(type == VisualType.single){
			this.toFront(); //Single element renders are small
		}
	}

	private void copyTransform() {
		if (oldRender != null) {
			scaleX = oldRender.getScaleX();
			scaleY = oldRender.getScaleY();
			translateX = oldRender.getTranslateX();
			translateY = oldRender.getTranslateY();
			layoutX = oldRender.getLayoutX();
			layoutY = oldRender.getLayoutY();

			render.setScaleX(scaleX);
			render.setScaleX(scaleY);
			render.setTranslateX(translateX);
			render.setTranslateY(translateY);
			render.setLayoutX(layoutX);
			render.setLayoutY(layoutY);

			oldRender = render;
			render.init();
		}
	}

	/**
	 * Determines and creates a Render for use by this DataStructure.
	 * 
	 * @param struct
	 *            The DataStructure to create a Render for.
	 */
	public _Render resolveRender(DataStructure struct) {
		VisualType visual = struct.resolveVisual();
		switch (visual) {
		case bar:
			render = new BarchartRender(struct, 40, 300, 30, 10);
			break;
		case box:
			render = new GridRender(struct, GridRender.Order.resolve(struct.visualOption), 40, 40, 3, 3);
			break;
		case tree:
			render = new KTreeRender(struct, struct.visualOption, 40, 40, 5, 5);
			break;
		case single:
			render = new SingleElementRender(struct, 80, 40);
			break;
		}
		render.setAnimationPane(animation_pane == null ? render.getNodes() : animation_pane);
		return render;
	}

	@Override
	public void visualChanged(VisualType newVisual) {
		this.setRender(newVisual);
	}
	
	/**
	 * Force the current Render to initialise.
	 */
	public void init(){
		render.init();
	}

	/**
	 * Returns the current Render for the structure.
	 * 
	 * @return The current Render for the structure.
	 */
	public _Render getRender() {
		return render;
	}
	
	public String toString(){
		return struct.identifier + ": " + renders.values();
	}
}
