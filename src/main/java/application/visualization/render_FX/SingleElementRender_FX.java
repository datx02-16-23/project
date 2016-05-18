package application.visualization.render_FX;

import application.visualization.render_FX.elements.SingleElement;
import application.visualization.render_FX.elements.VisualElement;
import javafx.scene.paint.Color;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

public class SingleElementRender_FX extends Render {

	/**
	 * Create a new SingleElementRender.
	 * 
	 * @param struct
	 *            The structure to draw as a single element.
	 * @param width
	 *            The width of the nodes.
	 * @param height
	 *            The height of the nodes.
	 */
	public SingleElementRender_FX(DataStructure struct, double width, double height) {
		super(struct, width, height, 0, 0);
	}

	@Override
	public void render() {
		if (struct.repaintAll) {
			struct.repaintAll = false;
			init();
		}
		super.render();
	}

	public boolean init() {
		if (super.init() == false) {
			return false;
		}
		return true;
	}

	@Override
	public void calculateSize() {
		width = node_width;
		height = node_height;
		setSize(width, height);
	}

	/**
	 * This method always returns 0.
	 * 
	 * @param e
	 *            An element.
	 * @return 0 regardless of e.
	 */
	@Override
	public double getX(Element e) {
		return 0; // Always 0.
	}

	/**
	 * This method always returns 0.
	 * 
	 * @param e
	 *            An element.
	 * @return 0 regardless of e.
	 */
	@Override
	public double getY(Element e) {
		return 0; // Always 0.
	}

	@Override
	protected VisualElement createVisualElement(Element e) {
		SingleElement se = new SingleElement(e, node_height, node_height);
		se.setIndex(null);
		return se;
	}

	@Override
	protected VisualElement createVisualElement(double value, Color color) {
		SingleElement se = new SingleElement(value, color, node_width, node_height);
		se.setIndex(null);
		return se;
	}

	@Override
	protected void bellsAndWhistles(Element e, VisualElement ve) {

	}
}
