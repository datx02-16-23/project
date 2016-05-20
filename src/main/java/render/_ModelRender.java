package render;

import java.util.HashMap;

import assets.Const;
import assets.Tools;
import assets.Debug;
import contract.Locator;
import contract.Operation;
import contract.datastructure.*;
import contract.operation.OP_ReadWrite;
import contract.operation.OP_Remove;
import contract.operation.OP_Swap;
import gui.Controller;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import model.Model;

/**
 * Handler class for rendering a model.
 * 
 * @author Richard Sundqvist
 *
 */
public class _ModelRender extends StackPane {

	/**
	 * Pane for drawing of animated elements.
	 */
	private final Pane animationPane = new Pane();

	/**
	 * Animation time in milliseconds.
	 */
	private long millis;
	/**
	 * Determines whether operations are animated on the animated_nodes canvas.
	 */
	private boolean useAnimation;
	/**
	 * The model to visualise.
	 */
	private final Model model;
	/**
	 * A list of render managers for the data structures.
	 */
	private final Pane managerPane = new Pane();
	/**
	 * A mapping of renders and their managers.
	 */
	private final HashMap<String, ARenderManager> managerMap = new HashMap<String, ARenderManager>();

	/**
	 * Create a new ModelRender.
	 * @param model The Model to render.
	 */
	public _ModelRender(Model model) {
		this.model = model;
		
		// Shared animation space
		animationPane.setMouseTransparent(true);
		animationPane.maxWidth(Double.MAX_VALUE);
		animationPane.maxHeight(Double.MAX_VALUE);
		useAnimation = true;

		// Add stacked canvases
		this.getChildren().addAll(Tools.HINT_PANE, managerPane, animationPane);
	}

	/**
	 * Clear the visualization.
	 */
	public void clear() {
		managerMap.clear();
		managerPane.getChildren().clear();
		animationPane.getChildren().clear();
		Tools.HINT_PANE.setVisible(true);
	}

	public void clearAndCreateVisuals() {
		clear();
		for (DataStructure struct : model.getStructures().values()) {
			ARenderManager manager = new ARenderManager(struct, animationPane);
			managerPane.getChildren().add(manager);
			managerMap.put(struct.identifier, manager);
		}
		// overlay.expandAll();
		placeVisuals();
		Tools.HINT_PANE.setVisible(managerPane.getChildren().isEmpty());
	}

	/**
	 * Should be called whenever model is updated.
	 * 
	 * @param op
	 *            An operation to animate.
	 */
	public void render(Operation op) {
		for (Object rm : managerPane.getChildren()) {
			((ARenderManager) rm).getRender().render();
		}
		if (useAnimation && op != null) {
			animate(op);
		}
	}

	/**
	 * Force Render initialisation.
	 */
	public void init() {
		for (Object rm : managerPane.getChildren()) {
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
		this.millis = (long) (millis * 0.60000);
	}

	/**
	 * Toggles animation on and off.
	 * 
	 * @param value
	 *            The new animation option.
	 */
	public void setAnimate(boolean value) {
		useAnimation = value;
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
		Locator tar = remove.getTarget();
		Element e;
		
		/**
		 * Var1 params
		 */
		for (DataStructure struct : model.getStructures().values()) {
			e = struct.getElement(tar);
			if (e != null) {
				ARender render = this.managerMap.get(struct.identifier).getRender();
				render.animateRemove(e, millis);
				if(Debug.ERR){
					System.err.println("\nVisualization.animateRemove():");					
				}
				return;
			}
		}
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
		 * Source paraeters
		 */
		for (DataStructure struct : model.getStructures().values()) {
			src_e = struct.getElement(source);
			if (src_e != null) {
				src_render = this.managerMap.get(struct.identifier).getRender();
				break; //Source found
			}
		}
		/**
		 * Target paraeters
		 */
		for (DataStructure struct : model.getStructures().values()) {
			tar_e = struct.getElement(target);
			if (tar_e != null) {
				tar_render = this.managerMap.get(struct.identifier).getRender();
				break; //Target found
			}
		}

		if(Debug.ERR){
			System.err.println("\nVisualization.animateReadWrite():");
			System.err.println("Has target: " + (tar_e == null ? "false" : tar_render.getDataStructure()));
			System.err.println("Has source: " + (src_e == null ? "false" : src_render.getDataStructure()));			
		}
		
		/**
		 * Start animations
		 */
		if (src_e != null && tar_e != null) {
			// Render data transfer between two known structures
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
				v1_render = this.managerMap.get(struct.identifier).getRender();
				break;
			}
		}
		/**
		 * Var2 params
		 */
		for (DataStructure struct : model.getStructures().values()) {
			v2_e = struct.getElement(var2);
			if (v2_e != null) {
				v2_render = this.managerMap.get(struct.identifier).getRender();
				break;
			}
		}
		/**
		 * Start animations
		 */

		if(Debug.ERR){
			System.err.println("\nVisualization.animateSwap():");			
		}
		
		v1_render.animateSwap(v1_e, v1_render, v2_e, v2_render, millis);
		v2_render.animateSwap(v2_e, v2_render, v1_e, v1_render, millis);
	}

	/**
	 * Attempt to place visuals with minimal overlap. Will return {@code false}
	 * if placement failed. Note that {@code true} does not guarantee that there
	 * is no overlap between renders.
	 * 
	 * @return False if placement failed.
	 */
	public boolean placeVisuals() {
		boolean successful = true;

		ARenderManager arm;
		int margin = 10;
		double xPos = 0;
		double yPos = 0;

		//@formatter:off
		int northWest = 0; int nWExpand = 0; // Default.
		int southWest = 0; int sWExpand = 0; // Bar Chart.
		int northEast = 0; int nEExpand = 0; // Single elements.
		int southEast = 0; int sEExpand = 0; // Not used at the moment.
		//@formatter:on

		for (Node node : managerPane.getChildren()) {
			arm = (ARenderManager) node;

			switch (arm.getStructure().visual) {
			case single:
				yPos = northEast * 120 + margin;
				xPos = getWidth() - (150 + margin) * (nEExpand + 1);
				if (!(checkXPos(xPos) && checkYPos(yPos))) {
					System.out.println("retry");
					northEast = 0;
					nEExpand++;
					yPos = northEast * 120 + margin;
					xPos = getWidth() - 150 * (nEExpand + 1) - margin;
				}
				northEast++;
				break;
			case bar:
				xPos = margin + this.getWidth() * sWExpand;
				yPos = getHeight() - (margin + Const.RENDER_HEIGHT) * (southWest + 1) - margin * 3;
				if (!(checkXPos(xPos) && checkYPos(yPos))) {
					sWExpand++; // TODO
				}
				southWest++;
				break;
			default:
				xPos = margin + this.getWidth() * nWExpand;
				yPos = (margin + Const.RENDER_HEIGHT) * northWest + margin;
				if (!(checkXPos(xPos) & checkYPos(yPos))) {
					nWExpand++; // TODO
				}
				northWest++;
				break;

			}

			// Make sure users can see the render.
			if (checkPositions(xPos, yPos) == false) {
				// Do not remove this printout //RS
				if(Debug.ERR){
					System.err.println("Using default placement for \"" + arm.getStructure() + "\".");					
				}
				yPos = margin;
				xPos = margin;
				successful = false;
			}

			arm.getRender().setTranslateX(xPos);
			arm.getRender().setTranslateY(yPos);
			// arm.getRender().setLayoutX(transX);
			// arm.getRender().setLayoutY(transY);
			arm.getRender().updateInfoLabels();
		}

		return successful;
	}

	private boolean checkPositions(double xPos, double yPos) {
		boolean result = true;

		if (checkXPos(xPos) == false) {
			if(Debug.OUT){
				System.err.println("Bad X-Coordinate: " + xPos + " not in " + xRange() + ".");				
			}
			result = false;
		}
		if (checkYPos(yPos) == false) {
			if(Debug.OUT){
				System.err.println("Bad Y-Coordinate: " + yPos + " not in " + yRange() + ".");				
			}
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
		return Const.RENDER_PADDING;
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
		return Const.RENDER_PADDING;
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
	public static class HintPane extends Pane {

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
		System.out.println("Visualization.showLiveStats() not implemnted yet");
	}
}