package draw;

import java.util.Arrays;

import assets.DasToolkit;
import contract.datastructure.DataStructure;
import contract.datastructure.Element;
import contract.datastructure.Array.IndexedElement;
import draw.element.ElemShape;
import draw.element.VisualElementFactory;
import draw.element.VisualElement;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

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

	/**
	 * Container for connector lines.
	 */
	private final Pane visual_lines = new Pane();

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
		content.getChildren().add(visual_lines);
		visual_lines.toBack();
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

		createGhosts(nodes.getChildren().size());
		return true;
	}

	/**
	 * Connect a node to its parent.
	 * 
	 * @param ae
	 *            The child node.
	 * @param childVis
	 *            The child node visual.
	 */
	@Override
	protected void bellsAndWhistles(Element ae, VisualElement childVis) {
		System.out.println("ktree: baw shape = " + childVis.getShape());

		IndexedElement parent_clone = new IndexedElement(0,
				new int[] { (((IndexedElement) ae).getIndex()[0] - 1) / K });

		// VisualElement parentVis = visualElementsMapping.get(parent_clone);
		VisualElement parentVis = visualMap
				.get(Arrays.toString(new int[] { (((IndexedElement) ae).getIndex()[0] - 1) / K }));

		double dx = node_width / 2;
		double dy = node_height / 2;

		// Connect child to parent
		if (parentVis != null) {
			Line line = new Line();

			// Bind start to child..
			line.startXProperty().bind(childVis.layoutXProperty());
			line.startYProperty().bind(childVis.layoutYProperty());
			// ..and end to parent.
			line.endXProperty().bind(parentVis.layoutXProperty());
			line.endYProperty().bind(parentVis.layoutYProperty());

			line.setTranslateX(dx);
			line.setTranslateY(dy);

			visual_lines.getChildren().add(line);
		}
	}

	/**
	 * Complete the tree using ghosts.
	 * 
	 * @param index
	 *            The index to start from.
	 */
	private void createGhosts(int index) {
		VisualElement ghostVis = null;
		for (; index < completedSize; index++) {
			IndexedElement ghostElem = new IndexedElement(0, new int[] { index });
			ghostVis = createVisualElement(ghostElem);
			ghostVis.setLayoutX(getX(ghostElem));
			ghostVis.setLayoutY(getY(ghostElem));
			ghostVis.setGhost(true);
			bellsAndWhistles(ghostElem, ghostVis);
			ghostVis.setInfoPos(Pos.CENTER);
			nodes.getChildren().add(ghostVis);
		}
	}

	@Override
	public double getX(Element e) {
		int index = ((IndexedElement) e).getIndex()[0];
		double x;
		int breadth, depth;
		if (index == 0) { // Root element
			double p = DasToolkit.pow(K, totDepth) / 2;
			x = hspace + (hspace + node_width) * (p) - ((K + 1) % 2) * (node_width + hspace) / 2;
		} else {
			depth = getDepth(index);
			breadth = getBreadth(index, depth);
			x = getX(breadth, depth);
		}
		return x;
	}

	private double getX(int breadth, int depth) {
		// Stepsize at this depth. Farther from root smaller steps
		double L = (double) DasToolkit.pow(K, totDepth) / (double) DasToolkit.pow(K, depth);
		// Apply indentation for every row except the last
		double indentation = 0;
		if (depth < totDepth) {
			indentation = (hspace + node_width) * ((L - 1) / 2);
		}
		// Dont multiply by zero
		if (breadth > 0) {
			return hspace + indentation + breadth * L * ((hspace + node_width));
		} else {
			return hspace + indentation;
		}
	}

	@Override
	public double getY(Element e) {
		int index = ((IndexedElement) e).getIndex()[0];
		double y = 0;

		if (index != 0) {
			y = getY(getDepth(index)); // Should not be used for root.
		}

		return y;
	}

	private double getY(int depth) {
		return depth * (node_height + vspace);
	}

	private int getDepth(int index) {
		int depth = 1;
		// Calculate depth and breadth
		while (DasToolkit.lowerLevelSum(depth, K) <= index) {
			depth++;
		}
		return depth - 1;
	}

	private int getBreadth(int index, int depth) {
		return index - DasToolkit.lowerLevelSum(depth, K);
	}

	/**
	 * Calculate the depth and breath of the tree.
	 */
	private void calculateDepthAndBreadth() {
		double structSize = struct.getElements().size();
		totDepth = 0;
		// Calculate the minimum depth which can hold all elements of the array.
		while (DasToolkit.lowerLevelSum(totDepth, K) < structSize) {
			totDepth++;
		}
		totDepth--;
		// completedSize = lowerLevelSums.get(Double.toString(K) +
		// lolwut).get(totDepth + 1);
		completedSize = DasToolkit.lowerLevelSum(totDepth + 1, K);
		totBreadth = DasToolkit.pow(K, totDepth);
	}

	@Override
	public void calculateSize() {
		calculateDepthAndBreadth();
		totWidth = totBreadth * (node_width + hspace) + hspace;
		totHeight = (totDepth + 1) * (node_height + vspace) + vspace * 2;
		setSize(totWidth, totHeight);
	}

	@Override
	protected VisualElement createVisualElement(Element e) {
		VisualElement ve = VisualElementFactory.shape(ElemShape.ELLIPSE, e, node_width, node_height);
		ve.setInfoPos(Pos.TOP_LEFT);
		ve.setInfoArray(((IndexedElement) e).getIndex());
		return ve;
	}

	@Override
	protected VisualElement createVisualElement(double value, Color color) {
		VisualElement ve = VisualElementFactory.shape(ElemShape.ELLIPSE, value, color, node_width, node_height);
		ve.setInfoPos(null);
		return ve;
	}
}
