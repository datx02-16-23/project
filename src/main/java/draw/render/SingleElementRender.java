package draw.render;

import contract.datastructures.DataStructure;
import contract.datastructures.Element;
import draw.render.elements.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SingleElementRender extends Render {

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
	public SingleElementRender(DataStructure struct, double width, double height) {
		super(struct, width, height, 0, 0);
		this.width = 150;
		this.height = 150;
	}

	@Override
	public void render() {
		if (struct.repaintAll) {
			struct.repaintAll = false;
			init();
		}
		super.render();
	}

	@Override
	public boolean init() {
		if (super.init() == false) {
			return false;
		}
		return true;
	}

	@Override
	public void calculateSize() {
		this.width = 150;
		this.height = node_height;
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
		return width/2;
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
		VisualElement re = VisElemFact.create(ElementShape.RECTANGLE, e, node_width, node_height);
		re.setInfoPos(null);
		return re;
	}

	@Override
	protected VisualElement createVisualElement(double value, Color color) {
		VisualElement re = VisElemFact.create(ElementShape.RECTANGLE, value, color, node_width, node_height);
		re.setInfoPos(null);
		return re;
	}

	@Override
	protected void bellsAndWhistles(Element e, VisualElement ve) {
		System.out.println("bellsAndWhistles shape = " + ve.getShape());
		((Rectangle) ve.getShape()).setArcWidth(node_width/4);
		((Rectangle) ve.getShape()).setArcHeight(node_height/4);
	}
}
