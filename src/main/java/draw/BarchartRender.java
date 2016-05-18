package draw;

import contract.datastructure.DataStructure;

import java.util.Arrays;

import contract.datastructure.Array;
import contract.datastructure.Array.BoundaryChangeListener;
import contract.datastructure.Array.IndexedElement;
import contract.datastructure.Element;
import draw.element.BarchartElement;
import draw.element.ElemShape;
import draw.element.VisualElement;
import draw.element.VisualElementFactory;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class BarchartRender extends _Render implements BoundaryChangeListener {

	// Using instead of default render field names for clarity.
	private double renderHeight, padding, barWidth, barMax, unitHeight;
	private final Array array;

	private Pane axes = new Pane();
	private final CategoryAxis xAxis = new CategoryAxis();
	private final NumberAxis yAxis = new NumberAxis();

	/**
	 * Create a new BarchartRender.
	 * 
	 * @param struct
	 *            The structure to render.
	 * @param barWidth
	 *            Width of the bars.
	 * @param renderHeight
	 *            Height of the Render unit.
	 * @param unitHeight
	 *            The height of the bars per unit.
	 * @param hspace
	 *            Space between bars.
	 */
	public BarchartRender(DataStructure struct, double barWidth, double renderHeight, double unitHeight,
			double hspace) {
		super(struct, barWidth, renderHeight, hspace, barWidth / 2);

		// Convenient names
		this.array = (Array) struct;
		this.barWidth = barWidth;
		this.renderHeight = renderHeight;
		this.padding = barWidth / 2;
		this.unitHeight = unitHeight;
		content.getChildren().add(axes);
		axes.setMouseTransparent(true);
		// axes.toBack();
	}

	@Override
	public double getX(Element e) {
		return getX(((IndexedElement) e).getIndex()[0]);
	}

	public double getX(int index) {
		return (barWidth + hspace) * index + hspace + padding;
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
		if (struct.getElements().isEmpty()) {
			return false; // Nothing to draw.
		}
		struct.repaintAll = false;

		/*
		 * Clear the nodes from all content Panes.
		 */
		for (Node n : content.getChildren()) {
			((Pane) n).getChildren().clear();
		}

		visualElementsMapping.clear();
		content.setBackground(null);
		calculateSize();

		// Create nodes
		VisualElement newVis;

		for (Element e : struct.getElements()) {
			newVis = createVisualElement(e);
			newVis.setLayoutX(getX(e));

			nodes.getChildren().add(newVis);
			visualElementsMapping.put(Arrays.toString(((IndexedElement) e).getIndex()), newVis);
			bellsAndWhistles(e, newVis);
		}
		sizeChildren();
		renderAxes();
		return true;
	}

	private void sizeChildren() {
		for (Node node : nodes.getChildren()) {
			sizeChild((BarchartElement) node);
		}
	}
	private void sizeChild(BarchartElement be){
		double barHeight = be.getElement().getNumericValue() * this.unitHeight;

		barHeight = barHeight > this.barMax ? barMax : barHeight;

		be.setBarHeight(barHeight);

		be.setLayoutY(barMax - barHeight);
	}

	/**
	 * Render the axes.
	 */
	private void renderAxes() {
		if (axes.getChildren().isEmpty()) {
			// axes.getChildren().add(xAxis);
			// axes.getChildren().add(yAxis);
			Line x_ax = new Line(padding, renderHeight - padding, totWidth - padding, renderHeight - padding);
			x_ax.setStrokeWidth(2);
			x_ax.setStroke(Color.PINK);
			axes.getChildren().add(x_ax);
			Line y_ax = new Line(padding, padding, padding, renderHeight - padding);
			y_ax.setStrokeWidth(2);
			y_ax.setStroke(Color.HOTPINK);
			axes.getChildren().add(y_ax);

			Line max_marker = new Line(padding, padding, totWidth - padding, padding);
			max_marker.setStrokeWidth(2);
			max_marker.setStroke(Color.HOTPINK);
			max_marker.getStrokeDashArray().addAll(20.0, 10.0);
			axes.getChildren().add(max_marker);
		}
		/*
		 * X-Axeis
		 */
		xAxis.setLabel("Index");
		xAxis.setPrefWidth(this.totWidth);
		xAxis.setLayoutX(0);
		xAxis.setLayoutY(renderHeight);
		/*
		 * X-Axeis
		 */
		yAxis.setLabel("Value");
		yAxis.setPrefWidth(renderHeight);
		yAxis.setLayoutX(0);
	}

	@Override
	public void calculateSize() {
		totWidth = array.getElements().size() * (barWidth + hspace) + padding * 2;
		barMax = renderHeight - padding * 2;
		setSize(totWidth, renderHeight);
	}

	@Override
	protected VisualElement createVisualElement(Element e) {
		VisualElement ve = VisualElementFactory.shape(ElemShape.BAR_ELEMENT, e, barWidth,
				unitHeight * e.getNumericValue());
		ve.setInfoArray(((IndexedElement) e).getIndex());
		return ve;
	}

	@Override
	protected VisualElement createVisualElement(double value, Color color) {
		VisualElement ve = VisualElementFactory.shape(ElemShape.BAR_ELEMENT, value, color, barWidth,
				unitHeight * value);
		return ve;
	}

	@Override
	protected void bellsAndWhistles(Element e, VisualElement ve) {
		System.out.println("bar: baw shape = " + ve.getShape());
	}

	@Override
	public void maxChanged(double newMin, double diff) {
//		init();
		sizeChildren();
	}

	@Override
	public void minChanged(double newMin, double diff) {
//		init();
		sizeChildren();
	}

	/**
	 * Have to override since elements are translated to position them in the
	 * bar. Will always return the value y-value at the x-axis).
	 * 
	 * @param e
	 *            An element owned by this BarcharRender.
	 * @return The absolute y-coordinates of e (always at the x-axis).
	 */
	@Override
	public double absY(Element e) {
		double by = this.getTranslateY() + this.getLayoutY() + content.getLayoutY();
		return by;
//		return barMax + by;
	}
	
	/*
	 * 
	 * 
	 * Animation overrider
	 * 
	 * 
	 */
	
}
