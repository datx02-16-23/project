package draw;

import contract.datastructure.DataStructure;
import contract.datastructure.Element;
import draw.element.*;
import javafx.scene.paint.Color;

public class SingleElementRender extends ARender {
	
	public static final ElementShape DEFAULT_ELEMENT_STYLE = ElementShape.SINGLE;

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
		this.totHeight = nodeHeight;
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
	protected AVElement createVisualElement(Element e) {
		AVElement re = AVElementFactory.shape(DEFAULT_ELEMENT_STYLE, e, nodeWidth, nodeHeight);
		return re;
	}

	@Override
	protected AVElement createVisualElement(double value, Color color) {
		AVElement re = AVElementFactory.shape(DEFAULT_ELEMENT_STYLE, value, color, nodeWidth, nodeHeight);
		re.setInfoPos(null);
		return re;
	}
	
	

	@Override
	protected void bellsAndWhistles(Element e, AVElement ve) {
		System.out.println("single: bells shape = " + ve.getShape());	
	}
}
