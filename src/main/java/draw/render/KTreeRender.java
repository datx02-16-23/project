package draw.render;

import java.util.ArrayList;
import java.util.Arrays;

import contract.datastructures.DataStructure;
import contract.datastructures.Element;
import contract.datastructures.Array.IndexedElement;
import draw.render.elements.ElementShape;
import draw.render.elements.VisElemFact;
import draw.render.elements.VisualElement;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class KTreeRender extends Render {

	/**
	 * Container for connector lines.
	 */
	protected final Pane visual_lines = new Pane();

	protected final int K;
	private final ArrayList<Integer> lowerLevelSums = new ArrayList<Integer>();
	protected int totDepth, totBreadth, completedSize;

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
		lowerLevelSums.add(new Integer(0));
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

		createGhosts(getNodes().getChildren().size());
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
		IndexedElement parent_clone = new IndexedElement(0,
				new int[] { (((IndexedElement) ae).getIndex()[0] - 1) / K });

		// VisualElement parentVis = visualElementsMapping.get(parent_clone);
		VisualElement parentVis = visualElementsMapping
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
			ghostVis.setInfoPos(Pos.CENTER	);
			getNodes().getChildren().add(ghostVis);
		}
	}

	@Override
	public double getX(Element e) {
		int index = ((IndexedElement) e).getIndex()[0];
		double x;
		int breadth, depth;
		if (index == 0) { // Root element
			double p = K_pow(totDepth) / 2;
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
		double L = (double) K_pow(totDepth) / (double) K_pow(depth);
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
		while (lowerLevelSum(depth) <= index) {
			depth++;
		}
		return depth - 1;
	}

	private int getBreadth(int index, int depth) {
		return index - lowerLevelSum(depth);
	}

	/**
	 * Memoized function. Calculates the total number of elements below a given
	 * depth and saves it to higherLevelSums. Once this method returns, total
	 * number of elements on any given depth up to {@code targetDepth} can be
	 * fetched from {@code higherLevelSums}.
	 * 
	 * @param targetDepth
	 *            The greatest depth to calculate for.
	 * @return The total number of elements above {@code targetDepth} for a
	 *         K-ary tree.
	 */
	private int lowerLevelSum(int targetDepth) {
		while (lowerLevelSums.size() <= targetDepth) {
			int sum = lowerLevelSums.get(lowerLevelSums.size() - 1) + K_pow(lowerLevelSums.size() - 1);
			lowerLevelSums.add(sum);
		}
		return lowerLevelSums.get(targetDepth);
	}

	/**
	 * Returns the number nodes at depth d (K^d). Really just a simplified pow
	 * function for ints.
	 * 
	 * @param d
	 *            the depth to calculate #nodes for.
	 * @return The number of nodes at depth d.
	 */
	private int K_pow(int d) {
		int p = 1;
		for (int i = 0; i < d; i++) {
			p = p * K;
		}
		return p;
	}

	/**
	 * Calculate the depth and breath of the tree.
	 */
	private void calculateDepthAndBreadth() {
		double structSize = struct.getElements().size();
		totDepth = 0;
		// Calculate the minimum depth which can hold all elements of the array.
		while (lowerLevelSum(totDepth) < structSize) {
			totDepth++;
		}
		totDepth--;
		this.completedSize = lowerLevelSums.get(totDepth + 1);
		totBreadth = K_pow(totDepth);
	}

	@Override
	public void calculateSize() {
		calculateDepthAndBreadth();
		width = totBreadth * (node_width + hspace) + hspace;
		height = (totDepth + 1) * (node_height + vspace) + vspace * 2;
		setSize(width, height);
	}

	@Override
	protected VisualElement createVisualElement(Element e) {
		VisualElement ve = VisElemFact.create(ElementShape.ELLIPSE, e, node_width, node_height);
		ve.setInfoPos(Pos.TOP_LEFT);
		ve.setInfoArray(((IndexedElement) e).getIndex());
		return ve;
	}

	@Override
	protected VisualElement createVisualElement(double value, Color color) {
		VisualElement ve = VisElemFact.create(ElementShape.ELLIPSE, value, color, node_width, node_height);
		ve.setInfoPos(null);
		return ve;
	}
}
