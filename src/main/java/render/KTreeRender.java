package render;

import java.util.Arrays;

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

// TODO Draw arrays with index.length > 1 by getting linear index from Array.

/**
 * A Render for Arrays with abstract type Tree. Can draw any K-ary tree for K >=
 * 2, where K is the number of children a node has (at most). All nodes except
 * root have one parent.<br>
 * All the elements of the supplied DataStruture must have an index[] of length
 * 1. No checking of index length is performed. Behaviour is undefined for
 * index.length != 1.
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
    private final Pane               nodeConnectorLines    = new Pane();
    /**
     * Number of children per node.
     */
    private int                      K;

    /**
     * Current number of levels of this tree (excluding root).
     */
    private int                      totDepth;
    /**
     *
     * Number leaf nodes.
     */
    private int                      totBreadth;
    /**
     *
     * Total capacity for a tree with depth {@link #totDepth}.
     */
    private int                      completedSize;

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
    public KTreeRender (DataStructure struct, int K, double width, double height, double hspace, double vspace) {
        super(struct, width, height, hspace, vspace);
        this.K = K < 2 ? 2 : K;
        this.contentPane.getChildren().add(this.nodeConnectorLines);
        this.nodeConnectorLines.toBack();
    }

    // ============================================================= //
    /*
     *
     * Controls
     *
     */
    // ============================================================= //

    @Override
    public void render () {
        if (this.struct.repaintAll) {
            this.struct.repaintAll = false;
            this.repaintAll();
        }
        super.render();
    }

    @Override
    public boolean repaintAll () {

        if (super.repaintAll() == false) {
            return false; // Nothing to render.
        }

        this.createGhosts(this.defaultNodePane.getChildren().size());
        return true;
    }

    /**
     * Set K for the K-Tree, the maximum number of children per node.
     *
     * @param K
     *            The new K-value.
     */
    public void setK (int K) {
        if (this.K != K) {
            this.K = K;
            this.repaintAll();
        }
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
    protected void bellsAndWhistles (Element ae, AVElement childVis) {
        // System.out.println("ktree: baw shape = " + childVis.getShape());

        new IndexedElement(0, new int[] { (((IndexedElement) ae).getIndex() [0] - 1) / this.K });

        // VisualElement parentVis = visualElementsMapping.get(parent_clone);
        AVElement parentVis = this.visualMap
                .get(Arrays.toString(new int[] { (((IndexedElement) ae).getIndex() [0] - 1) / this.K }));

        double dx = this.nodeWidth / 2;
        double dy = this.nodeHeight / 2;

        // Connect child to parent
        if (parentVis != null) {
            Line line = new Line();

            // Bind start to child..
            line.setStartX(childVis.getLayoutX());
            line.setStartY(childVis.getLayoutY());
            // line.startXProperty().bind(childVis.layoutXProperty());
            // line.startYProperty().bind(childVis.layoutYProperty());

            // ..and end to parent.
            line.setEndX(parentVis.getLayoutX());
            line.setEndY(parentVis.getLayoutY());
            // line.endXProperty().bind(parentVis.layoutXProperty());
            // line.endYProperty().bind(parentVis.layoutYProperty());

            line.setOpacity(0.5);

            line.setTranslateX(dx);
            line.setTranslateY(dy);

            this.nodeConnectorLines.getChildren().add(line);
        }
    }

    /**
     * Complete the tree using ghosts.
     *
     * @param index
     *            The index to start from.
     */
    private void createGhosts (int index) {
        AVElement ghostVis = null;
        for (; index < this.completedSize; index++) {
            IndexedElement ghostElem = new IndexedElement(Double.MAX_VALUE, new int[] { index });
            ghostVis = this.createVisualElement(ghostElem);
            ghostVis.setLayoutX(this.getX(ghostElem));
            ghostVis.setLayoutY(this.getY(ghostElem));
            ghostVis.setGhost(true);
            this.bellsAndWhistles(ghostElem, ghostVis);
            ghostVis.setInfoPos(Pos.CENTER);
            this.defaultNodePane.getChildren().add(ghostVis);
        }
    }

    @Override
    public double getX (Element e) {
        if (e == null || e instanceof IndexedElement == false) {
            return -1;
        }

        int index = ((IndexedElement) e).getIndex() [0];
        double x;
        int breadth, depth;
        if (index == 0) { // Root element
            double p = Tools.pow(this.K, this.totDepth) / 2;
            x = this.hSpace + (this.hSpace + this.nodeWidth) * p
                    - (this.K + 1) % 2 * (this.nodeWidth + this.hSpace) / 2;
        } else {
            depth = this.getDepth(index);
            breadth = this.getBreadth(index, depth);
            x = this.getX(breadth, depth);
        }

        return x + this.hSpace + Tools.getAdjustedX(this, e);
    }

    private double getX (int breadth, int depth) {
        // Stepsize at this depth. Farther from root smaller steps
        double L = (double) Tools.pow(this.K, this.totDepth) / (double) Tools.pow(this.K, depth);
        // Apply indentation for every row except the last
        double x = this.hSpace;

        if (depth < this.totDepth) {
            x = x + (this.hSpace + this.nodeWidth) * ((L - 1) / 2);
        }
        // Dont multiply by zero
        if (breadth > 0) {
            return this.hSpace + x + breadth * L * (this.hSpace + this.nodeWidth);
        } else {
            return this.hSpace + x;
        }
    }

    @Override
    public double getY (Element e) {
        if (e == null || e instanceof IndexedElement == false) {
            return -1;
        }

        int index = ((IndexedElement) e).getIndex() [0];

        double y = 0;

        if (index != 0) {
            y = this.getY(this.getDepth(index)); // Should not be used for root.
        }

        return y + Tools.getAdjustedY(this, e);
    }

    private double getY (int depth) {
        return depth * (this.nodeHeight + this.vSpace);
    }

    private int getDepth (int index) {
        int depth = 1;
        // Calculate depth and breadth
        while (Tools.lowerLevelSum(depth, this.K) <= index) {
            depth++;
        }
        return depth - 1;
    }

    private int getBreadth (int index, int depth) {
        return index - Tools.lowerLevelSum(depth, this.K);
    }

    /**
     * Calculate the depth and breath of the tree.
     */

    private void calculateDepthAndBreadth () {
        if (this.K < 2) {
            return;
            // Fixed infinite recursion case caused by
            // superconstructor call before K could be validated by local
            // constructor.
        }

        double structSize = this.struct.getElements().size();
        this.totDepth = 0;

        // Calculate the minimum depth which can hold all elements of the array.
        while (Tools.lowerLevelSum(this.totDepth, this.K) < structSize) {
            this.totDepth++;
        }
        this.totDepth--;
        this.completedSize = Tools.lowerLevelSum(this.totDepth + 1, this.K);
        this.totBreadth = Tools.pow(this.K, this.totDepth);
    }

    @Override
    public void calculateSize () {
        this.calculateDepthAndBreadth();
        this.renderWidth = this.totBreadth * (this.nodeWidth + this.hSpace) + this.hSpace * 2;
        this.renderHeight = (this.totDepth + 1) * (this.nodeHeight + this.vSpace) + this.vSpace;
        this.setRestricedSize(this.renderWidth, this.renderHeight);
    }

    @Override
    protected AVElement createVisualElement (Element e) {
        this.elementStyle = this.elementStyle == null ? DEFAULT_ELEMENT_STYLE : this.elementStyle;
        AVElement ve = AVElementFactory.shape(this.elementStyle, e, this.nodeWidth, this.nodeHeight);
        ve.setInfoPos(Pos.TOP_LEFT);
        ve.setInfoArray(((IndexedElement) e).getIndex());
        return ve;
    }

    @Override
    protected AVElement createVisualElement (double value, Color color) {
        this.elementStyle = this.elementStyle == null ? DEFAULT_ELEMENT_STYLE : this.elementStyle;
        AVElement ve = AVElementFactory.shape(this.elementStyle, value, color, this.nodeWidth, this.nodeHeight);
        ve.setInfoPos(null);
        return ve;
    }
}
