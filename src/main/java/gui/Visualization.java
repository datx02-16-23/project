package gui;

import java.util.HashMap;

import contract.Locator;
import contract.Operation;
import contract.datastructure.*;
import contract.operation.OP_ReadWrite;
import contract.operation.OP_Remove;
import contract.operation.OP_Swap;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import model.Model;
import render.AbsRender;
import render.RenderManager;

/**
 * Handler class for visualisations and animations.
 * 
 * @author Richard Sundqvist
 *
 */
public class Visualization extends StackPane {

	// A FXML pane showing user instructions.
	private static final HintPane HINT_PANE = new HintPane();
	
	/**
	 * Pane for drawing of animated elements.
	 */
	private final Pane animated_nodes = new Pane();

	/**
	 * Animation time in milliseconds.
	 */
	private long millis;
	/**
	 * Determines whether operations are animated on the animated_nodes canvas.
	 */
	private boolean animate;
	/**
	 * The model being visualized.
	 */
	private final Model model;
	/**
	 * A list of render managers for the data structures.
	 */
	private final Pane managers = new Pane();
	/**
	 * A mapping of renders and their managers.
	 */
	private final HashMap<String, RenderManager> struct_manager_mapping = new HashMap<String, RenderManager>();

	/**
	 * Create a new Visualization.
	 */
	public Visualization() {
		this.model = Model.instance();
		// Shared animation space
		animated_nodes.setMouseTransparent(true);
		animated_nodes.maxWidth(Double.MAX_VALUE);
		animated_nodes.maxHeight(Double.MAX_VALUE);
		animate = true;
		// Add stacked canvases
		this.getChildren().addAll(HINT_PANE, managers, animated_nodes);
	}

	/**
	 * Clear the visualization.
	 */
	public void clear() {
		struct_manager_mapping.clear();
		managers.getChildren().clear();
		HINT_PANE.setVisible(true);
	}

	public void clearAndCreateVisuals() {
		clear();
		for (DataStructure struct : model.getStructures().values()) {
			RenderManager manager = new RenderManager(struct, animated_nodes);
			managers.getChildren().add(manager);
			struct_manager_mapping.put(struct.identifier, manager);
		}
		// overlay.expandAll();
		HINT_PANE.setVisible(managers.getChildren().isEmpty());
	}

	/**
	 * Should be called whenever model is updated.
	 * @param op An operation to animate.
	 */
	public void render(Operation op) {
		for (Object rm : managers.getChildren()) {
			((RenderManager) rm).getRender().render();
		}
		if (animate && op != null) {
			animate(op);
		}
	}

	/**
	 * Set the animation time in milliseconds for all animations.
	 * 
	 * @param millis
	 *            The new animation time in milliseconds.
	 */
	public final void setAnimationTime(long millis) {
		this.millis = millis;
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
	 * Animate an operation.
	 * 
	 * @param op
	 *            The operation to animate.
	 */
	public void animate(Operation op) {
		if (op == null) {
			return;
		}
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
		AbsRender src_render = null, tar_render = null;
		/**
		 * Source params
		 */
		for (DataStructure struct : model.getStructures().values()) {
			src_e = struct.getElement(source);
			if (src_e != null) {
				src_render = this.struct_manager_mapping.get(struct.identifier).getRender();
				break;
			}
		}
		/**
		 * Target params
		 */
		for (DataStructure struct : model.getStructures().values()) {
			tar_e = struct.getElement(target);
			if (tar_e != null) {
				tar_render = this.struct_manager_mapping.get(struct.identifier).getRender();
				break;
			}
		}
		/**
		 * Start animations
		 */
		if (src_e != null && tar_e != null) {
			// Render data transfer between two known structures
			src_render.animateReadWrite(src_e, src_render, tar_e, tar_render, millis);
			tar_render.animateReadWrite(src_e, src_render, tar_e, tar_render, millis);
		} else if (tar_e == null && src_render != null) {
			// Render read without target
			src_render.animateReadWrite(src_e, src_render, null, null, millis);
		} else if (src_e == null && tar_render != null) {
			// Render write without source
			tar_render.animateReadWrite(null, null, tar_e, tar_render, millis);
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
		AbsRender v1_render = null, v2_render = null;
		/**
		 * Var1 params
		 */
		for (DataStructure struct : model.getStructures().values()) {
			v1_e = struct.getElement(var1);
			if (v1_e != null) {
				v1_render = this.struct_manager_mapping.get(struct.identifier).getRender();
				break;
			}
		}
		/**
		 * Var2 params
		 */
		for (DataStructure struct : model.getStructures().values()) {
			v2_e = struct.getElement(var2);
			if (v2_e != null) {
				v2_render = this.struct_manager_mapping.get(struct.identifier).getRender();
				break;
			}
		}
		/**
		 * Start animations
		 */

		v1_render.animateSwap(v1_e, v1_render, v2_e, v2_render, millis);
		v2_render.animateSwap(v2_e, v2_render, v1_e, v1_render, millis);
	} // End animate swap

	/**
	 * Hint pane for the visualiser window.
	 * 
	 * @author Richard Sundqvist
	 *
	 */
	private static class HintPane extends Pane {

		public HintPane() {
			Image image = new Image(Controller.class.getResourceAsStream("/assets/upload.png"));
			this.setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
					BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
					new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false))));
			this.setVisible(true);
		}
	}
}