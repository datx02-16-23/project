package render;

import contract.datastructure.DataStructure;

import java.util.Arrays;

import assets.Const;
import contract.datastructure.Array;
import contract.datastructure.Array.IndexedElement;
import contract.datastructure.Array.MinMaxListener;
import contract.datastructure.Element;
import render.element.BarchartElement;
import render.element.ElementShape;
import render.ARenderAnimation.AnimationOption;
import render.element.AVElement;
import gui.Main;
import render.element.AVElementFactory;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

public class BarchartRender extends ARender implements MinMaxListener {

    public static final ElementShape ELEMENT_STYLE = ElementShape.BAR_ELEMENT;

    // Using instead of default render field names for clarity.
    private double renderHeight, padding, barWidth, xAxisY, unitHeight;

    private Pane axes = new Pane();
    // private final CategoryAxis xAxis = new CategoryAxis();
    // private final NumberAxis yAxis = new NumberAxis();
    private double rightWallX;

    /**
     * Create a new BarchartRender. If both {@code renderHeight} and
     * {@code unitHeight} are greater than 0, the bars may stretch outside of
     * the render depending on element numeric value.
     * 
     * @param struct
     *            The structure to render.
     * @param barWidth
     *            Width of the bars.
     * @param renderHeight
     *            Height of the Render itself. A value lower than zero indicates
     *            that the BarchartRender should update its height
     *            automatically, ignoring {@code unitHeight}.
     * @param unitHeight
     *            The height of the bars per unit. A value lower than 0 will
     *            scale all elements relative to {@code renderHeight}, ignoring
     *            {@code unitHeight}<b>NOT IMPLEMENTED YET </b>.
     * @param hspace
     *            Space between bars.
     */
    // TODO: Bars relative to renderHeight when unitSize < 0.
    public BarchartRender(DataStructure struct, double barWidth, double renderHeight, double unitHeight,
	    double hspace) {
	super(struct, barWidth, renderHeight, hspace, -1);

	// Convenient names
	this.barWidth = barWidth;
	this.padding = barWidth / 2;
	this.unitHeight = unitHeight;

	// Axes
	axes.setMouseTransparent(true);
	contentPane.getChildren().add(axes);

	if (renderHeight < 0) {
	    if (struct instanceof Array) {
		((Array) struct).setListener(this);
		System.out.println("setting self to lstner");
		setRestricedSize(0, 0); // Become as small as setSize will permit.
	    } else {
		System.out.println("not an array");
		this.renderHeight = Const.RENDER_HEIGHT;
	    }
	} else {
	    System.out.println("fixed render height");
	    this.renderHeight = renderHeight;
	}
    }

    @Override
    public double getX(Element e) {
	if (e == null || e instanceof IndexedElement == false) {
	    return -1;
	}
	int[] index = ((IndexedElement) e).getIndex();
	if (index == null || index.length == 0) {
	    System.err.println("Invalid index for element " + e + " in \"" + struct + "\".");
	    Main.console.err("Invalid index for element " + e + " in \"" + struct + "\".");
	    renderFailure();
	    return -1;
	}
	return getX(index[0]);
    }

    public double getX(int index) {
	return (barWidth + hSpace) * index + hSpace + padding + 5;
    }

    @Override
    public double getY(Element e) {
	return xAxisY + 100; // TODO
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
	    return false; // Nothing to render.
	}
	struct.repaintAll = false;

	/*
	 * Clear the nodes from all content Panes.
	 */
	for (Node n : contentPane.getChildren()) {
	    ((Pane) n).getChildren().clear();
	}

	visualMap.clear();
	contentPane.setBackground(null);
	calculateSize();

	// Create nodes
	BarchartElement newVis;

	for (Element e : struct.getElements()) {
	    newVis = createVisualElement(e);
	    newVis.setLayoutX(getX(e));

	    defaultNodePane.getChildren().add(newVis);
	    visualMap.put(Arrays.toString(((IndexedElement) e).getIndex()), newVis);
	    bellsAndWhistles(e, newVis);
	}
	positionBars();
	drawAxes();
	return true;
    }

    private void positionBars() {
	for (Node node : defaultNodePane.getChildren()) {
	    ((BarchartElement) node).setBotY(xAxisY + 5); // TODO fix
	}
    }

    /**
     * Render the axes.
     */
    private void drawAxes() {
	// if (axes.getChildren().isEmpty()) {
	/*
	 * X-Axis
	 */
	Line xAxis = new Line(0, xAxisY, rightWallX + 5, xAxisY);
	xAxis.setStrokeWidth(2);
	axes.getChildren().add(xAxis);

	Polyline xArrow = new Polyline(0, 0, 15, 5, 0, 10);
	xArrow.setLayoutX(totWidth - 15);
	xArrow.setLayoutY(xAxisY - 5);
	xArrow.setStrokeWidth(2);
	axes.getChildren().add(xArrow);

	Label xLabel = new Label("Value");
	xLabel.setLayoutX(padding * 1.5);
	xLabel.setLayoutY(-7);
	axes.getChildren().add(xLabel);

	/*
	 * Y-Axis
	 */
	Line yAxis = new Line(padding, padding / 2, padding, renderHeight);
	yAxis.setStrokeWidth(2);
	axes.getChildren().add(yAxis);
	notches();
	for (Element e : struct.getElements()) {
	    createIndexLabel(e);
	}

	Polyline yArrow = new Polyline(0, 15, 5, 0, 10, 15);
	yArrow.setStrokeWidth(2);
	yArrow.setLayoutX(padding - 5);
	yArrow.setLayoutY(-5);
	axes.getChildren().add(yArrow);

	Label yLabel = new Label("Index");
	yLabel.setLayoutX(rightWallX);
	yLabel.setLayoutY(xAxisY + 2);
	axes.getChildren().add(yLabel);

	// showDeveloperGuides();
    }

    /**
     * Draw developer guides where the bar roof, x-axis, y-axis and rightmost
     * limit should be.
     */
    public void drawDeveloperGuides() {

	Line roof = new Line(padding, padding, rightWallX, padding);
	roof.setStroke(Color.HOTPINK);
	roof.setStrokeWidth(2);
	roof.getStrokeDashArray().addAll(20.0);

	Line floor = new Line(padding, xAxisY, rightWallX, xAxisY);
	floor.setStrokeWidth(2);
	floor.setStroke(Color.HOTPINK);
	floor.getStrokeDashArray().addAll(20.0);

	Line left = new Line(padding, padding, padding, xAxisY);
	left.setStroke(Color.HOTPINK);
	left.setStrokeWidth(2);
	left.getStrokeDashArray().addAll(20.0);

	Line right = new Line(totWidth - padding, padding, totWidth - padding, xAxisY);
	right.setStroke(Color.HOTPINK);
	right.setStrokeWidth(2);
	right.getStrokeDashArray().addAll(20.0);

	Pane guides = new Pane();
	guides.getChildren().addAll(roof, floor, left, right);
	guides.setOpacity(0.9);
	contentPane.getChildren().add(guides);
    }

    private void notches() {
	double lim = padding / 2;
	int i = 1;
	
	for (double y = xAxisY - unitHeight; y >= lim; y = y - unitHeight) {
	    // Notch
	    Line line = new Line(padding - 3, y, padding + 3, y);
	    axes.getChildren().add(line);

	    // Value
	    Label value = new Label();
	    value.setLayoutY(lim);
	    value.setLayoutY(y - 10);

	    value.setText(i++ + "");

	    axes.getChildren().add(value);
	}
    }

    @Override
    public void calculateSize() {
	totWidth = struct.getElements().size() * (barWidth + hSpace) + padding * 3;
	xAxisY = renderHeight - padding;
	rightWallX = totWidth - padding;
	setRestricedSize(totWidth, renderHeight);
    }

    @Override
    protected BarchartElement createVisualElement(Element e) {
	BarchartElement ve = (BarchartElement) AVElementFactory.shape(ELEMENT_STYLE, e, barWidth,
		unitHeight * e.getNumValue());
	return ve;
    }

    @Override
    protected AVElement createVisualElement(double value, Color color) {
	AVElement ve = AVElementFactory.shape(ELEMENT_STYLE, value, color, barWidth, unitHeight * value);
	return ve;
    }

    @Override
    protected void bellsAndWhistles(Element e, AVElement ve) {
	((BarchartElement) ve).updateUnitHeight(unitHeight);
    }

    private void createIndexLabel(Element e) {
	int[] index = ((IndexedElement) e).getIndex();

	Label info = new Label();
	info.setLayoutY(xAxisY);
	info.setLayoutX(this.getX(e) + 5);
	// info.setLayoutX(100);
	info.setText(Arrays.toString(index));

	info.setMouseTransparent(true);

	axes.getChildren().add(info);
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
	double by = this.getTranslateY() + this.getLayoutY() + contentPane.getLayoutY();
	return xAxisY + by;
    }

    /**
     * Custom animation for read operations with only one locator.
     * 
     * @param src
     *            The source element.
     * @param srcRender
     *            The render for the source element.
     * @param tar
     *            The target element.
     * @param tarRender
     *            The render for the target element.
     * @param millis
     *            The time in milliseconds the animation should last.
     */
    public void animateReadWrite(Element src, ARender srcRender, Element tar, ARender tarRender, long millis) {
	if (tar != null || src == null) {
	    super.animateReadWrite(src, srcRender, tar, tarRender, millis);
	    return;
	}

	double x1 = this.absX(src);
	double y1 = this.absY(src);

	double x2 = x1;
	double y2 = y1 - barWidth / 2;
	int[] i = ((IndexedElement) src).getIndex();
	Arrays.copyOf(i, i.length);
	final AVElement orig = visualMap.get(Arrays.toString(i));
	orig.setGhost(true);

	ParallelTransition up = ARenderAnimation.linear(src, x1, y1, x2, y2, millis / 3, this);
	ParallelTransition down = ARenderAnimation.linear(src, x2, y2, x1, y1, millis / 3, this, AnimationOption.GHOST);

	SequentialTransition st = new SequentialTransition();
	st.getChildren().addAll(up, down);
	st.play();
    }

    @Override
    public void maxChanged(double newMax) {
	calculateHeight(newMax);
	System.out.println("maxChanged arraydsadasdjiasdsa");
    }

    @Override
    public void minChanged(double newMin) {
	// TODO
    }

    public void calculateHeight(double v) {
	double oldHeight = renderHeight;
	renderHeight = v * unitHeight + padding * 2 + unitHeight / 2;
	System.out.println(renderHeight);
	calculateSize();
	this.init();
	this.setTranslateY(this.getTranslateY() + (oldHeight - renderHeight));
    }
}
