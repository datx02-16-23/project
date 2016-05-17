package application.visualization.render_FX;

import java.util.Arrays;

import application.gui.Main;
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
			
			visualElementsMapping.clear();
			visual_nodes.getChildren().clear();

			if (struct.getElements().isEmpty()) {
				setBackground(getBackground());
				setSize(150, 150);
				return; // Nothing to drawn.
			}

			setBackground(null);

//			setSize(node_width + 1, node_height);
			
			int i = 0;
			for (Element e : struct.getElements()) {
				SingleElement elem = new SingleElement(e, node_width, node_height);
				visual_nodes.getChildren().add(elem);
				
//				visualElementsMapping.put(e, elem);
				visualElementsMapping.put(Arrays.toString(((IndexedElement) e).getIndex()), elem);
				if (i > 0) {
					Main.console.err("Warning: " + getClass().getSimpleName() + " cannot draw more than one element.");
					Main.console.err("Failed to draw element: " + e);
				}

				i++;
			}
		}
		super.render();
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
