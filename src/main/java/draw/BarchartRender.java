package draw;

import contract.datastructure.DataStructure;
import contract.datastructure.Array;
import contract.datastructure.Array.IndexedElement;
import contract.datastructure.Element;
import draw.element.ElemShape;
import draw.element.VisualElement;
import draw.element._VisualElementFactory;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.paint.Color;

public class BarchartRender extends _Render {
	
	//Using instead of default render field names for clarity.
	private double heightPerUnit, padding;
	
	private final CategoryAxis xAxis = new CategoryAxis(); 
	private final NumberAxis yAxis = new NumberAxis();

	/**
	 * Create a new BarchartRender.
	 * 
	 * @param struct
	 *            The structure to render.
	 * @param barWidth
	 *            Width of the bars.
	 * @param heightPerUnit
	 *            Height of the bars per unit.
	 * @param hspace
	 *            Space between bars.
	 */
	public BarchartRender(DataStructure struct, double barWidth, double heightPerUnit, double hspace) {
		super(struct, barWidth, heightPerUnit, hspace, barWidth / 2);
		this.heightPerUnit = heightPerUnit;
		this.padding = barWidth / 2;
	}

	@Override
	public double getX(Element e) {
		return ((IndexedElement) e).getIndex()[0];
	}

	public double getX(int index) {
		return (node_width + hspace) * index + hspace + padding;
	}

	@Override
	public double getY(Element e) {
		return totHeight - getY(e.getNumericValue());
	}

	public double getY(double value) {
		return value * this.node_height + padding;
	}

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
			return false; // Nothing to draw.
		}

		renderAxes();
		return true;
	}

	private void renderAxes() {
		//Use default canvas.
		nodes.getChildren().add(xAxis);
		nodes.getChildren().add(yAxis);
	}

	@Override
	protected VisualElement createVisualElement(Element e) {
		VisualElement ve = _VisualElementFactory.shape(ElemShape.RECTANGLE, e, node_width,
				heightPerUnit * e.getNumericValue());
		// ve.setInfoArray(((IndexedElement) e).getIndex());
		return ve;
	}

	@Override
	protected VisualElement createVisualElement(double value, Color color) {
		VisualElement ve = _VisualElementFactory.shape(ElemShape.RECTANGLE, value, color, node_width,
				heightPerUnit * value);
		return ve;
	}

	@Override
	protected void bellsAndWhistles(Element e, VisualElement ve) {
		System.out.println("bar: baw shape = " + ve.getShape());
	}

}
