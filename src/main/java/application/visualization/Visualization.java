package application.visualization;

import java.util.HashMap;

import application.gui.GUI_Controller;
import application.model.Model;
import application.visualization.animation.Animation;
import application.visualization.render2d.*;
import application.visualization.render_NEW.KTreeRender_FX;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import wrapper.Locator;
import wrapper.Operation;
import wrapper.datastructures.*;
import wrapper.operations.OP_ReadWrite;
import wrapper.operations.OP_Remove;
import wrapper.operations.OP_Swap;

/**
 * Handler class for visualisations and animations. The ANIMATED Canvas should
 * only be used for moving objects as it is cleared every iteration.
 * 
 * @author Richard Sundqvist
 *
 */
public class Visualization extends StackPane {

	private static final HintPane hintPane = new HintPane();
	private boolean animate;
	private final Model model;
	private static Visualization INSTANCE;
	private final StackPane renders = new StackPane();
	private final Overlay overlay;
	private final HashMap<String, Render> struct_render_mapping = new HashMap<String, Render>();
	public final Canvas ANIMATED = new Canvas();

	/**
	 * Returns the static instance of Visualization.
	 * 
	 * @return The static Visualization instance.
	 */
	public static Visualization instance() {
		if (INSTANCE == null) {
			INSTANCE = new Visualization();
		}
		return INSTANCE;
	}

	/**
	 * Create a new Visualization.
	 */
	public Visualization() {
		this.model = Model.instance();
		// Build Canvas
		ANIMATED.setMouseTransparent(true);
		ANIMATED.widthProperty().bind(this.widthProperty());
		ANIMATED.heightProperty().bind(this.heightProperty());
		ANIMATED.maxWidth(Double.MAX_VALUE);
		ANIMATED.maxHeight(Double.MAX_VALUE);
		animate = true;
		// Add stacked canvases
		this.getChildren().add(renders);
		this.getChildren().add(ANIMATED);
		this.getChildren().add(hintPane);
		overlay = new Overlay();
		// this.getChildren().add(overlay.getNode());
	}

	public void clear() {
		struct_render_mapping.clear();
		renders.getChildren().clear();
		ANIMATED.getGraphicsContext2D().clearRect(0, 0, ANIMATED.getWidth(), ANIMATED.getHeight());
		// overlay.clear();
		hintPane.setVisible(true);
	}

	public void clearAndCreateVisuals() {
		clear();
		overlay.clear();
		for (DataStructure struct : model.getStructures().values()) {
			Render render = resolveRender(struct);
			renders.getChildren().add(render);
			// overlay.addNode(new ArrayInfoPane((Array) struct));
			struct_render_mapping.put(struct.identifier, render);
		}
		// overlay.expandAll();
		hintPane.setVisible(renders.getChildren().isEmpty());
	}

	/**
	 * Determines the model to use for this DataStructure.
	 * 
	 * @param struct
	 *            The DataStructure to assign a Render to.
	 */
	public static Render resolveRender(DataStructure struct) {
		Render render = null;
		VisualType visual = struct.resolveVisual();
		switch (visual) {
		case bar:
			// render = new BarchartRender(struct, 40, 5, 5);
			render = new BarchartRender_OLD(struct);
			break;
		case box:
			render = new MatrixRender(struct, struct.visualOption, 40, 40, 0, 0);
			break;
		case tree:
			// render = new KTreeRender(struct, struct.visualOption, 40, 40, 0,
			// 10);
			render = new KTreeRender_FX(struct, 4, 50, 40, 5, 5);
			break;
		}
		return render;
	}

	/**
	 * TODO: remove this method
	 * 
	 * @param vt
	 * @return
	 */
	public static Render getRender(VisualType vt) {
		Render render = null;
		switch (vt) {
		case bar:
			render = new BarchartRender(null, 40, 1, 5);
			break;
		case box:
			render = new MatrixRender(null, -1, -1, -1, -1, -1);
			break;
		case tree:
			render = new KTreeRender(null, -1, -1, -1, -1, -1);
			break;
		}
		return render;
	}

	/**
	 * Should be called whenever model is updated, does a complete rerender of
	 * the structures.
	 */
	public void render(Operation op) {
		if (op == null) {
			return;
		}
		Render render;
		for (Object node : renders.getChildren()) {
			render = (Render) node;
			render.render();
		}
		if (animate) {
			cleanAnimatedCanvas();
			animate(op);
		}
	}

	/**
	 * Set the animation time in millisections for <b>ALL</b> animations.
	 * 
	 * @param millis
	 *            The new animation time in milliseconds.
	 */
	public static final void setAnimationTime(long millis) {
		Animation.setAnimationTime(millis);
	}

	public void animate(Operation op) {
		switch (op.operation) {
		case read:
		case write:
			animateReadWrite((OP_ReadWrite) op);
			break;
		case remove:
			animateRemove((OP_Remove) op);
			break;
		case swap:
			animateSwap((OP_Swap) op);
			break;
		default:
			// Do nothing.
			break;
		}
	}

	private void animateRemove(OP_Remove remove) {

	}

	public void animateReadWrite(OP_ReadWrite rw) {
		Locator source = rw.getSource();
		Locator target = rw.getTarget();
		if (source == null && target == null) {
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
		if (src_e != null && tar_e != null) {
			// Render data transfer between two known structures
			src_render.animateReadWrite(src_e, src_render, tar_e, tar_render);
			tar_render.animateReadWrite(src_e, src_render, tar_e, tar_render);
		} else if (tar_e == null && src_render != null) {
			// Render read without target
			src_render.animateReadWrite(src_e, src_render, null, null);
		} else if (src_e == null && tar_render != null) {
			// Render write without source
			tar_render.animateReadWrite(null, null, tar_e, tar_render);
		}
	}

	/**
	 * Trigger an animation of a swap.
	 * 
	 * @param swap
	 *            The swap to animate.
	 */
	public void animateSwap(OP_Swap swap) {
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
				break;
			}
		}
		/**
		 * Start animations
		 */

		v1_render.animateSwap(v1_e, v1_render, v2_e, v2_render);
		v2_render.animateSwap(v2_e, v2_render, v1_e, v1_render);
	} // End animate swap

	public void cleanAnimatedCanvas() {
		ANIMATED.getGraphicsContext2D().clearRect(-5000, -5000, 10000, 10000);
	}

	/**
	 * Toggles animation on and off.
	 * 
	 * @param value
	 *            The new animation option.
	 */
	public void setAnimate(boolean value) {
		animate = value;
	}

	/**
	 * Hint pane for the visualiser window.
	 * 
	 * @author Richard Sundqvist
	 *
	 */
	private static class HintPane extends Pane {

		public HintPane() {
			Image image = new Image(GUI_Controller.class.getResourceAsStream("/assets/upload.png"));
			this.setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
					BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
					new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false))));
			this.setVisible(true);
		}
	}
}