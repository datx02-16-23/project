package gui;

import java.util.HashMap;

import contract.Locator;
import contract.Operation;
import contract.datastructure.*;
import contract.operation.OP_ReadWrite;
import contract.operation.OP_Remove;
import contract.operation.OP_Swap;
import draw.ARenderManager;
import draw.ARender;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import model.Model;

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
	 * Suggested minimum render distance from the edges of the parent.
	 */
	public static final double PADDING = 10;

	/**
	 * Pane for drawing of animated elements.
	 */
	private final Pane animated = new Pane();

	/**
	 * Animation time in milliseconds.
	 */
	private long millis;
	/**
	 * Determines whether operations are animated on the animated_nodes canvas.
	 */
	private boolean animate;
	/**
	 * The model being visualised.
	 */
	private final Model model;
	/**
	 * A list of render managers for the data structures.
	 */
	private final Pane managers = new Pane();
	/**
	 * A mapping of renders and their managers.
	 */
	private final HashMap<String, ARenderManager> struct_manager_mapping = new HashMap<String, ARenderManager>();

	/**
	 * Create a new Visualization.
	 */
	public Visualization() {
		this.model = Model.instance();
		// Shared animation space
		animated.setMouseTransparent(true);
		animated.maxWidth(Double.MAX_VALUE);
		animated.maxHeight(Double.MAX_VALUE);
		animate = true;

		// Add stacked canvases
		this.getChildren().addAll(HINT_PANE, managers, animated);
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
			ARenderManager manager = new ARenderManager(struct, animated);
			managers.getChildren().add(manager);
			struct_manager_mapping.put(struct.identifier, manager);
		}
		// overlay.expandAll();
		placeVisuals();
		HINT_PANE.setVisible(managers.getChildren().isEmpty());
	}

	/**
	 * Should be called whenever model is updated.
	 * 
	 * @param op
	 *            An operation to animate.
	 */
	public void render(Operation op) {
		for (Object rm : managers.getChildren()) {
			((ARenderManager) rm).getRender().render();
		}
		if (animate && op != null) {
			animate(op);
		}
	}

	/**
	 * Force Render initialisation.
	 */
	public void init() {
		for (Object rm : managers.getChildren()) {
			((ARenderManager) rm).getRender().init();
		}

	}

	/**
	 * Set the animation time in milliseconds for all animations. Actual
	 * animation time will be {@code millis * 0.6} to allow rest time after the
	 * animation.
	 * 
	 * @param millis
	 *            The new animation time in milliseconds.
	 */
	public final void setAnimationTime(long millis) {
//		millis = 10000; //TODO
		this.millis = (long) (millis * 0.60000);
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
		ARender src_render = null, tar_render = null;
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
		ARender v1_render = null, v2_render = null;
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
	 * Attempt to place visuals with minimal overlap.
	 */
	public void placeVisuals() {
		ARenderManager arm;
		int padding = 10;
		double xPos = 0;
		double yPos = 0;

		// Most renders
		int northWest = 0;
		int nWRow = 0;

		// Barcharts
		int southWest = 0;
		int sWRow = 0;

		// Single elements
		int northEast = 0;
		int nERow = 0;

		// Not used at the moment.
		int southEast = 0;

		for (Node node : managers.getChildren()) {
			arm = (ARenderManager) node;

			switch (arm.getStructure().visual) {
			case single:
				yPos = northEast * 150 + padding;
				xPos = getWidth() - 150 * (nERow + 1) - padding;
				if (checkXPos(xPos) == false && nERow == 0) {
					northEast = 0;
					nERow++;
					xPos = getWidth() - 150 * (nERow + 1) - padding;
				}
				northEast++;
				break;
			case bar:
				xPos = padding + this.getWidth() * sWRow;
				yPos = getHeight() - (padding + ARender.DEFAULT_RENDER_HEIGHT) * (southWest + 1) - padding * 3;
				if (checkYPos(yPos) == false && sWRow == 0) {
					sWRow++;
				}
				southWest++;
				break;
			default:
				xPos = padding + this.getWidth() * nWRow;
				yPos = (padding + ARender.DEFAULT_RENDER_HEIGHT) * northWest + padding;
				if (checkYPos(yPos) == false && nWRow == 0) {
					nWRow++;
				}
				northWest++;
				break;

			}

			// Make sure users can see the render.
			if (checkPositions(xPos, yPos) == false) {
				// Do not remove this printout //RS
				System.err.println("Using default placement for \"" + arm.getStructure() + "\".");
				yPos = padding;
				xPos = padding;
			}

			arm.getRender().setTranslateX(xPos);
			arm.getRender().setTranslateY(yPos);
			// arm.getRender().setLayoutX(transX);
			// arm.getRender().setLayoutY(transY);
			arm.getRender().updateInfoLabels();
		}
	}

	private boolean checkPositions(double xPos, double yPos) {
		boolean result = true;

		if (checkXPos(xPos) == false) {
			// Do not remove this printout //RS
			System.err.println("Bad X-Coordinate: " + xPos + " not in " + xRange() + ".");
			result = false;
		}
		if (checkYPos(yPos) == false) {
			// Do not remove this printout //RS
			System.err.println("Bad Y-Coordinate: " + yPos + " not in " + yRange() + ".");
			result = false;
		}

		return result;
	}

	/**
	 * Check to see if an X-Coordinate is in the acceptable range.
	 * 
	 * @param xPos
	 *            An x-coordinate.
	 * @return True if the coordinate good, false otherwise.
	 */
	public boolean checkXPos(double xPos) {
		return !(xPos < getXMin() || xPos > getXMax());
	}

	/**
	 * Returns the maximum acceptable X-Coordinate.
	 * 
	 * @return The maximum acceptable X-Coordinate.
	 */
	public double getXMax() {
		return this.getWidth() - 100;
	}

	/**
	 * Returns the minimum acceptable X-Coordinate.
	 * 
	 * @return The minimum acceptable X-Coordinate.
	 */
	public double getXMin() {
		return PADDING;
	}

	/**
	 * Check to see if an Y-Coordinate is in the acceptable range.
	 * 
	 * @param yPos
	 *            An y-coordinate.
	 * @return True if the coordinate good, false otherwise.
	 */

	public boolean checkYPos(double yPos) {
		return !(yPos < getYMin() || yPos > getYMax());
	}

	/**
	 * Returns the maximum acceptable Y-Coordinate.
	 * 
	 * @return The maximum acceptable Y-Coordinate.
	 */
	public double getYMax() {
		return this.getHeight() - 100;
	}

	/**
	 * Returns the minimum acceptable Y-Coordinate.
	 * 
	 * @return The minimum acceptable Y-Coordinate.
	 */
	public double getYMin() {
		return PADDING;
	}

	/**
	 * Returns a String representing the range of acceptable X-Coordinates
	 * 
	 * @return A String representing the range
	 */
	public String xRange() {
		return "[" + getXMin() + ", " + getXMax() + "]";
	}

	/**
	 * Returns a String representing the range of acceptable Y-Coordinates
	 * 
	 * @return A String representing the range
	 */
	public String yRange() {
		return "[" + getYMin() + ", " + getYMax() + "]";
	}

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

	/**
	 * Create a render which shows live updating statistics for the model.
	 */
	public void showLiveStats() {
		// TODO Auto-generated method stub
		System.err.println("showLiveStats() not implemnted yet");
	}
}