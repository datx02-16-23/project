package application.visualization.render_FX;

import java.util.Arrays;

import wrapper.datastructures.Array.IndexedElement;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

public class SingleElementRender_FX extends Render_FX {

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

	public void init() {

		visualElementsMapping.clear();
		nodes.getChildren().clear();

		if (struct.getElements().isEmpty()) {
			return; // Nothing to draw.
		}

		content.setBackground(null);
		calculateSize();

		//Will probably look like crap with more than one element :D!
		for (Element e : struct.getElements()) {
			SingleElement elem = new SingleElement(e, node_width, node_height);
			nodes.getChildren().add(elem);

			// visualElementsMapping.put(e, elem);
			visualElementsMapping.put(Arrays.toString(((IndexedElement) e).getIndex()), elem);
		}
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

}
