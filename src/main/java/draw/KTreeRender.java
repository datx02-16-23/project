package draw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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

public class KTreeRender extends ARender {

	/**
	 * Memoization for number of nodes.
	 */
	private static final HashMap<Integer, ArrayList<Integer>> lowerLevelSums = new HashMap<Integer, ArrayList<Integer>>();
	/**
	 * Container for connector lines.
	 */
	protected final Pane visual_lines = new Pane();

	/**
	 * Number of children per node.
	 */
	protected final int K;
	/**
	 * //TODO How to write javadoc for each without splitting them up? Number of
	 * levels (excluding root); number of elements at the bottom; total capacity
	 * for a tree with this depth.
	 */
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
			double p = K_pow(totDepth, K) / 2;
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
		double L = (double) K_pow(totDepth, K) / (double) K_pow(depth, K);
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
		while (lowerLevelSum(depth, K) <= index) {
			depth++;
		}
		return depth - 1;
	}

	private int getBreadth(int index, int depth) {
		return index - lowerLevelSum(depth, K);
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
	private static int lowerLevelSum(int targetDepth, int K) {
		System.out.println("lowerLevelSums = " + lowerLevelSums);
		
		if(lowerLevelSums.containsKey(K) == false){
			lowerLevelSums.put(K, new ArrayList<Integer>(new Integer(0)));
			System.out.println("put");
		} else {
			System.out.println("already exists");
		}
		
		while (lowerLevelSums.get(K).size() <= targetDepth) {
			int sum = lowerLevelSums.get(K).get(lowerLevelSums.get(K).size() - 1) + K_pow(lowerLevelSums.get(K).size() - 1, K);
			lowerLevelSums.get(K).add(sum);
		}
		return lowerLevelSums.get(K).get(targetDepth);
	}

	/**
	 * Returns the number nodes at depth d (K^d). Really just a simplified pow
	 * function for ints.
	 * 
	 * @param d
	 *            the depth to calculate #nodes for.
	 * @return The number of nodes at depth d.
	 */
	private static int K_pow(int d, int K) {
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
		while (lowerLevelSum(totDepth, K) < structSize) {
			totDepth++;
		}
		totDepth--;
		this.completedSize = lowerLevelSums.get(K).get(totDepth + 1);
		totBreadth = K_pow(totDepth, K);
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
