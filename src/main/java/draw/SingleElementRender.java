package draw;

import contract.datastructure.DataStructure;
import contract.datastructure.Element;
import draw.element.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SingleElementRender extends _Render {

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
		this.totWidth = 150;
		this.totHeight = 150;
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
		this.totWidth = 150;
		this.totHeight = node_height;
		setSize(totWidth, totHeight);
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
		return totWidth/2;
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
		VisualElement re = VisualElementFactory.shape(ElemShape.RECTANGLE, e, node_width, node_height);
		return re;
	}

	@Override
	protected VisualElement createVisualElement(double value, Color color) {
		VisualElement re = VisualElementFactory.shape(ElemShape.RECTANGLE, value, color, node_width, node_height);
		re.setInfoPos(null);
		return re;
	}

	@Override
	protected void bellsAndWhistles(Element e, VisualElement ve) {
		System.out.println("single: bells shape = " + ve.getShape());
		if(ve.getShape() == null){
			return;
		}
		((Rectangle) ve.getShape()).setArcWidth(node_width/4);
		((Rectangle) ve.getShape()).setArcHeight(node_height/4);
	}
}
