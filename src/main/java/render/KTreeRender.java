package render;

import java.util.Arrays;

import assets.Debug;
import assets.Tools;
import contract.datastructure.Array.IndexedElement;
import contract.datastructure.DataStructure;
import contract.datastructure.Element;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import render.element.AVElement;
import render.element.AVElementFactory;
import render.element.ElementShape;

/**
 * A Render for Arrays with abstract type Tree. Can draw any K-ary tree for K >=
 * 2, where K is the number of children a node has (at most). All nodes except
 * root have one parent. All the elements of the supplied DataStruture must have
 * an index[] of length 1. No checking of index length is performed. Behaviour
 * is undefined for K < 2 and index.length != 1. Implementation based on
 * {@link https://en.wikipedia.org/wiki/K-ary_tree}.
 * 
 * @author Richard Sundqvist
 *
 */
public class KTreeRender extends ARender {

    public static final ElementShape DEFAULT_ELEMENT_STYLE = ElementShape.RECTANGLE;
    
    // ============================================================= //
    /*
     * 
     * Field Variables
     * 
     */
    // ============================================================= //

    /**
     * Container for connector lines.
     */
    private final Pane nodeConnectorLines = new Pane();

    /**
     * Number of children per node.
     */
    private final int K;
    /**
     * //TODO How to write javadoc for each without splitting them up?<br>
     * Number of levels (excluding root); <br>
     * number of elements at the bottom;<br>
     * total capacity for a tree with this depth.
     */
    private int totDepth, totBreadth, completedSize;
    
    // ============================================================= //
    /*
     * 
     * Constructors
     * 
     */
    // ============================================================= //

    /**
     * Create a new KTreeRender with K children and one parent. Will set K = 2
     * for any K < 2.
     * 
     * @param struct
     *            The structure to draw as an K-ary tree.
     * @param K
     *            The number of children each node has.
     * @param width
     *            The width of the visual_nodes.
     * @param height
     *            The height of the visual_nodes.
     * @param hspace
     *            The horizontal space between elements.
     * @param vspace
     *            The vertical space between elements.
     */
    public KTreeRender(DataStructure struct, int K, double width, double height, double hspace, double vspace) {
	super(struct, width, height, hspace, vspace);
	this.K = K < 2 ? 2 : K;
	contentPane.getChildren().add(nodeConnectorLines);
	nodeConnectorLines.toBack();
    }
    
    // ============================================================= //
    /*
     * 
     * Controls
     * 
     */
    // ============================================================= //

    public void render() {
	if (struct.repaintAll) {
	    struct.repaintAll = false;
	    repaintAll();
	}
	super.render();
    }

    @Override
    public boolean repaintAll() {

	if (super.repaintAll() == false) {
	    return false; // Nothing to render.
	}

	createGhosts(defaultNodePane.getChildren().size());
	return true;
    }
    
    // ============================================================= //
    /*
     * 
     * Interface methods
     * 
     */
    // ============================================================= //

    /**
     * Connect a node to its parent.
     * 
     * @param ae
     *            The child node.
     * @param childVis
     *            The child node visual.
     */
    @Override
    protected void bellsAndWhistles(Element ae, AVElement childVis) {
	// System.out.println("ktree: baw shape = " + childVis.getShape());

	IndexedElement parentClone = new IndexedElement(0, new int[] { (((IndexedElement) ae).getIndex()[0] - 1) / K });

	// VisualElement parentVis = visualElementsMapping.get(parent_clone);
	AVElement parentVis = visualMap
		.get(Arrays.toString(new int[] { (((IndexedElement) ae).getIndex()[0] - 1) / K }));

	double dx = nodeWidth / 2;
	double dy = nodeHeight / 2;

	// Connect child to parent
	if (parentVis != null) {
	    Line line = new Line();

	    // Bind start to child..
	    line.setStartX(childVis.getLayoutX());
	    line.setStartY(childVis.getLayoutY());
//	    line.startXProperty().bind(childVis.layoutXProperty());
//	    line.startYProperty().bind(childVis.layoutYProperty());
	    
	    // ..and end to parent.
	    line.setEndX(parentVis.getLayoutX());
	    line.setEndY(parentVis.getLayoutY());
//	    line.endXProperty().bind(parentVis.layoutXProperty());
//	    line.endYProperty().bind(parentVis.layoutYProperty());

	    line.setOpacity(0.5);

	    line.setTranslateX(dx);
	    line.setTranslateY(dy);

	    nodeConnectorLines.getChildren().add(line);
	}
    }

    /**
     * Complete the tree using ghosts.
     * 
     * @param index
     *            The index to start from.
     */
    private void createGhosts(int index) {
	AVElement ghostVis = null;
	for (; index < completedSize; index++) {
	    IndexedElement ghostElem = new IndexedElement(Double.MAX_VALUE, new int[] { index });
	    ghostVis = createVisualElement(ghostElem);
	    ghostVis.setLayoutX(getX(ghostElem));
	    ghostVis.setLayoutY(getY(ghostElem));
	    ghostVis.setGhost(true);
	    bellsAndWhistles(ghostElem, ghostVis);
	    ghostVis.setInfoPos(Pos.CENTER);
	    defaultNodePane.getChildren().add(ghostVis);
	}
    }

    @Override
    public double getX(Element e) {
	if (e == null || e instanceof IndexedElement == false) {
	    return -1;
	}
	
	int index = ((IndexedElement) e).getIndex()[0];
	double x;
	int breadth, depth;
	if (index == 0) { // Root element
	    double p = Tools.pow(K, totDepth) / 2;
	    x = hSpace + (hSpace + nodeWidth) * (p) - ((K + 1) % 2) * (nodeWidth + hSpace) / 2;
	} else {
	    depth = getDepth(index);
	    breadth = getBreadth(index, depth);
	    x = getX(breadth, depth);
	}
	
	return x + hSpace + Tools.getAdjustedX(this, e);
    }

    private double getX(int breadth, int depth) {
	// Stepsize at this depth. Farther from root smaller steps
	double L = (double) Tools.pow(K, totDepth) / (double) Tools.pow(K, depth);
	// Apply indentation for every row except the last
	double x = hSpace;
	
	if (depth < totDepth) {
	    x = x + (hSpace + nodeWidth) * ((L - 1) / 2);
	}
	// Dont multiply by zero
	if (breadth > 0) {
	    return hSpace + x + breadth * L * ((hSpace + nodeWidth));
	} else {
	    return hSpace + x;
	}
    }

    @Override
    public double getY(Element e) {
	if (e == null || e instanceof IndexedElement == false) {
	    return -1;
	}
	
	int index = ((IndexedElement) e).getIndex()[0];
	
	double y = 0;

	if (index != 0) {
	    y = getY(getDepth(index)); // Should not be used for root.
	}
	
	return y + Tools.getAdjustedY(this, e);
    }

    private double getY(int depth) {
	return depth * (nodeHeight + vSpace);
    }

    private int getDepth(int index) {
	int depth = 1;
	// Calculate depth and breadth
	while (Tools.lowerLevelSum(depth, K) <= index) {
	    depth++;
	}
	return depth - 1;
    }

    private int getBreadth(int index, int depth) {
	return index - Tools.lowerLevelSum(depth, K);
    }

    /**
     * Calculate the depth and breath of the tree.
     */

    private void calculateDepthAndBreadth() {
	if (K < 2) {
	    return;
	    // Fixed infinite recursion case caused by
	    // superconstructor call before K could be validated by local
	    // constructor.
	}

	double structSize = struct.getElements().size();
	totDepth = 0;

	// Calculate the minimum depth which can hold all elements of the array.
	while (Tools.lowerLevelSum(totDepth, K) < structSize) {
	    totDepth++;
	}
	totDepth--;
	completedSize = Tools.lowerLevelSum(totDepth + 1, K);
	totBreadth = Tools.pow(K, totDepth);
    }

    @Override
    public void calculateSize() {
	calculateDepthAndBreadth();
	renderWidth = totBreadth * (nodeWidth + hSpace) + hSpace * 2;
	renderHeight = (totDepth + 1) * (nodeHeight + vSpace) + vSpace;
	setRestricedSize(renderWidth, renderHeight);
    }

    @Override
    protected AVElement createVisualElement(Element e) {
	elementStyle = elementStyle == null ? DEFAULT_ELEMENT_STYLE : elementStyle;
	AVElement ve = AVElementFactory.shape(elementStyle, e, nodeWidth, nodeHeight);
	ve.setInfoPos(Pos.TOP_LEFT);
	ve.setInfoArray(((IndexedElement) e).getIndex());
	return ve;
    }

    @Override
    protected AVElement createVisualElement(double value, Color color) {
	elementStyle = elementStyle == null ? DEFAULT_ELEMENT_STYLE : elementStyle;
	AVElement ve = AVElementFactory.shape(elementStyle, value, color, nodeWidth, nodeHeight);
	ve.setInfoPos(null);
	return ve;
    }
}
