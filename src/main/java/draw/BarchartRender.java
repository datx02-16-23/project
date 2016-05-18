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
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class BarchartRender extends ARender implements BoundaryChangeListener {

	// Using instead of default render field names for clarity.
	private double renderHeight, padding, barWidth, barMax, unitHeight;
	private final Array array;

	private Pane axes = new Pane();
//	private final CategoryAxis xAxis = new CategoryAxis();
//	private final NumberAxis yAxis = new NumberAxis();

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
		double barHeight = this.barHeight(be);

		be.setBarHeight(barHeight);

		be.setLayoutY(barMax - barHeight);
	}

	/**
	 * Render the axes.
	 */
	private void renderAxes() {
//		if (axes.getChildren().isEmpty()) {
			/*
			 * X-Axis
			 */
			Line xAxis = new Line(padding/2, renderHeight - padding, totWidth - padding/2, renderHeight - padding);
			xAxis.setStrokeWidth(2);
			xAxis.setStroke(Color.PINK);
			axes.getChildren().add(xAxis);
			
			/*
			 * Y-Axis
			 */
			Line yAxis = new Line(padding, padding/2, padding, renderHeight - padding/2);
			yAxis.setStrokeWidth(2);
			yAxis.setStroke(Color.HOTPINK);
			axes.getChildren().add(yAxis);

			/*
			 * Roof
			 */
			Line roof = new Line(padding, padding, totWidth - padding, padding);
			roof.setStrokeWidth(2);
			roof.setStroke(Color.HOTPINK);
			roof.getStrokeDashArray().addAll(20.0, 10.0);
			axes.getChildren().add(roof);
			
			for(Element e : struct.getElements()){
				createIndexLabel(e);
			}
//		}
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
	}
	
	private void createIndexLabel(Element e){
		((IndexedElement) e).getIndex();
		
		Label info = new Label();
		info.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");
		info.setLayoutY(padding);
		info.setLayoutY(this.getX(e));
		
		info.setMouseTransparent(true);
		
		axes.getChildren().add(info);
	}

	@Override
	public void maxChanged(double newMin, double diff) {
		sizeChildren();
	}

	@Override
	public void minChanged(double newMin, double diff) {
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
		return barMax + by;
	}
	
	private double barHeight(BarchartElement be){
		double height = Math.abs(be.getElement().getNumericValue()) * unitHeight;
		
		height = height > this.barMax ? barMax : height;
		
		//TODO scaling?
		
		return height;
	}
	
	/*
	 * 
	 * 
	 * Animation overrider
	 * 
	 * 
	 */
	
}
