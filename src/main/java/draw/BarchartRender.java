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
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

public class BarchartRender extends ARender implements BoundaryChangeListener {

	// Using instead of default render field names for clarity.
	private double renderHeight, padding, barWidth, barMax, unitSize;
	private final Array array;

	private Pane axes = new Pane();
	// private final CategoryAxis xAxis = new CategoryAxis();
	// private final NumberAxis yAxis = new NumberAxis();

	/**
	 * Create a new BarchartRender.
	 * 
	 * @param struct
	 *            The structure to render.
	 * @param barWidth
	 *            Width of the bars.
	 * @param totHeight
	 *            Height of the Render itself.
	 * @param unitSize
	 *            The height of the bars per unit.
	 * @param hspace
	 *            Space between bars.
	 */
	public BarchartRender(DataStructure struct, double barWidth, double totHeight, double unitSize, double hspace) {
		super(struct, barWidth, totHeight, hspace, -1);

		// Convenient names
		this.array = (Array) struct;
		this.barWidth = barWidth;
		this.renderHeight = totHeight;
		this.padding = barWidth / 2;
		this.unitSize = unitSize;

		// Axes
		axes.setMouseTransparent(true);
		content.getChildren().add(axes);
	}

	@Override
	public double getX(Element e) {
		int[] index = ((IndexedElement) e).getIndex();
		if (index == null || index.length == 0) {
			System.err.println("Invalid index for element " + e + " in \"" + struct + "\".");
			renderFailure();
			return -1;
		}
		return getX(index[0]);
	}

	public double getX(int index) {
		return (barWidth + hspace) * index + hspace + padding;
	}

	@Override
	public double getY(Element e) {
		return this.barMax;
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
		BarchartElement newVis;

		for (Element e : struct.getElements()) {
			newVis = createVisualElement(e);
			newVis.setLayoutX(getX(e));

			nodes.getChildren().add(newVis);
			visualElementsMapping.put(Arrays.toString(((IndexedElement) e).getIndex()), newVis);
			bellsAndWhistles(e, newVis);
		}
		fixChildren();
		renderAxes();
		return true;
	}

	private void fixChildren() {
		for (Node node : nodes.getChildren()) {
			((BarchartElement) node).setBotY(barMax - 10);
		}
	}

	/**
	 * Render the axes.
	 */
	private void renderAxes() {
		// if (axes.getChildren().isEmpty()) {
		/*
		 * X-Axis
		 */
		Line xAxis = new Line(0, renderHeight - padding, totWidth - padding / 2, renderHeight - padding);
		xAxis.setStrokeWidth(2);
		axes.getChildren().add(xAxis);

		Polyline xArrow = new Polyline(0, 0, 15, 5, 0, 10);
		xArrow.setLayoutX(totWidth - 10);
		xArrow.setLayoutY(renderHeight - padding - 5);
		xArrow.setStrokeWidth(2);
		axes.getChildren().add(xArrow);
		
		Label xLabel = new Label("Value");
		xLabel.setLayoutX(padding * 1.5);
		xLabel.setLayoutY(-5);
		axes.getChildren().add(xLabel);

		/*
		 * Y-Axis
		 */
		Line yAxis = new Line(padding, padding / 2, padding, renderHeight);
		yAxis.setStrokeWidth(2);
		axes.getChildren().add(yAxis);
		notches();

		Polyline yArrow = new Polyline(0, 15, 5, 0, 10, 15);
		yArrow.setStrokeWidth(2);
		yArrow.setLayoutX(padding - 5);
		yArrow.setLayoutY(-5);
		axes.getChildren().add(yArrow);
		
		Label yLabel = new Label("Index");
		yLabel.setLayoutX(totWidth - padding);
		yLabel.setLayoutY(renderHeight - padding * 0.8);
		axes.getChildren().add(yLabel);

		/*
		 * Roof
		 */
		Line roof = new Line(padding, padding, totWidth - padding, padding);
		roof.setStrokeWidth(2);
		roof.setStroke(Color.HOTPINK);
		roof.getStrokeDashArray().addAll(20.0, 10.0);
		axes.getChildren().add(roof);

		for (Element e : struct.getElements()) {
			createIndexLabel(e);
		}
		// }
	}

	private void notches() {
		double x = padding / 2;
		int i = 1;
		for (double y = barMax; y >= padding; y = y - unitSize) {
			// Notch
			Line line = new Line(padding - 3, y, padding + 3, y);
			axes.getChildren().add(line);

			// Value
			Label value = new Label();
			value.setLayoutY(x);
			value.setLayoutY(y - 10);

			value.setText(i++ + "");

			axes.getChildren().add(value);
		}
	}

	@Override
	public void calculateSize() {
		totWidth = array.getElements().size() * (barWidth + hspace) + padding * 3;
		barMax = renderHeight - padding * 2;
		setSize(totWidth, renderHeight);
	}

	@Override
	protected BarchartElement createVisualElement(Element e) {
		BarchartElement ve = (BarchartElement) VisualElementFactory.shape(ElemShape.BAR_ELEMENT, e, barWidth,
				unitSize * e.getNumericValue());
		return ve;
	}

	@Override
	protected VisualElement createVisualElement(double value, Color color) {
		VisualElement ve = VisualElementFactory.shape(ElemShape.BAR_ELEMENT, value, color, barWidth, unitSize * value);
		return ve;
	}

	@Override
	protected void bellsAndWhistles(Element e, VisualElement ve) {
		((BarchartElement) ve).updateUnitHeight(unitSize);
	}

	private void createIndexLabel(Element e) {
		int[] index = ((IndexedElement) e).getIndex();

		Label info = new Label();
		info.setStyle("-fx-background-color: white;");
		info.setLayoutY(barMax + 12);
		info.setLayoutX(this.getX(e) + 5);
		// info.setLayoutX(100);
		info.setText(Arrays.toString(index));

		info.setMouseTransparent(true);

		axes.getChildren().add(info);
	}

	@Override
	public void maxChanged(double newMin, double diff) {
		fixChildren();
	}

	@Override
	public void minChanged(double newMin, double diff) {
		fixChildren();
	}

	/**
	 * Have to override since elements are translated to position them in the
	 * bar.
	 * 
	 * @param e
	 *            An element owned by this BarcharRender.
	 * @return The absolute y-coordinates of e.
	 */
	@Override
	public double absY(Element e) {
		double by = this.getTranslateY() + this.getLayoutY() + content.getLayoutY();
		return barMax - 10 + by;
	}

	/*
	 * 
	 * 
	 * Animation overrider
	 * 
	 * 
	 */

}
