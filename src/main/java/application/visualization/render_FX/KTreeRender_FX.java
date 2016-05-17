package application.visualization.render_FX;

import java.util.ArrayList;
import java.util.Arrays;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import wrapper.datastructures.Array.IndexedElement;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

public class KTreeRender_FX extends Render_FX {

	/**
	 * Container for connector lines.
	 */
	protected final Pane visual_lines = new Pane();

	private static final RenderSVF rsvf = new RenderSVF(2, 1337); // TODO

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
	public KTreeRender_FX(DataStructure struct, int K, double width, double height, double hspace, double vspace) {
		super(struct, width, height, hspace, vspace);
		this.K = K < 2 ? 2 : K;
		lowerLevelSums.add(new Integer(0));
		getChildren().add(visual_lines);
		visual_lines.toBack();
	}

	public void render() {
		if (struct.repaintAll) {
			struct.repaintAll = false;
			if (struct.getElements().isEmpty()) {
				setBackground(getBackground());
				return; // Nothing to draw.
			}

			visual_nodes.getChildren().clear();
			visual_lines.getChildren().clear();
			visualElementsMapping.clear();

			setBackground(null);

			// Create nodes
			calculateSize();
			VisualElement newVis;

			int i = 0;
			for (Element e : struct.getElements()) {
				i++;
				newVis = new EllipseElement(e, node_width / 2, node_height / 2);
				IndexedElement ae = (IndexedElement) e;
				newVis.setLayoutX(getX(ae));	
				newVis.setLayoutY(getY(ae));
				((EllipseElement) newVis).setIndex(((IndexedElement) e).getIndex());

				IndexedElement parent_clone = new IndexedElement(0, new int[] { (ae.getIndex()[0] - 1) / K });

				// VisualElement parentVis =
				// visualElementsMapping.get(parent_clone);
				EllipseElement parentVis = (EllipseElement) visualElementsMapping
						.get(Arrays.toString(new int[] { (ae.getIndex()[0] - 1) / K }));

				// Connect child to parent
				if (parentVis != null) {
					Line line = new Line();

					// Bind start to child..
					line.startXProperty().bind(newVis.layoutXProperty());
					line.startYProperty().bind(newVis.layoutYProperty());
					// ..and end to parent.
					line.endXProperty().bind(parentVis.layoutXProperty());
					line.endYProperty().bind(parentVis.layoutYProperty());

					line.setTranslateX(node_width / 2);
					line.setTranslateY(node_height / 2);

					visual_lines.getChildren().add(line);
				}
				visual_nodes.getChildren().add(newVis);
				visualElementsMapping.put(Arrays.toString(ae.getIndex()), newVis);
				// visualElementsMapping.put(ae, ghost);
			}

			/*
			 * Add ghosts to complete the tree.
			 */
			for (; i < completedSize; i++) {
				IndexedElement ae = new IndexedElement(0, new int[] { i });
				newVis = new EllipseElement(ae, node_width / 2, node_height / 2);
				newVis.setLayoutX(getX(ae));
				newVis.setLayoutY(getY(ae));
				newVis.setGhost(true);

				IndexedElement parent_clone = new IndexedElement(0, new int[] { (ae.getIndex()[0] - 1) / K });

				EllipseElement parentVis = (EllipseElement) visualElementsMapping
						.get(Arrays.toString(new int[] { (ae.getIndex()[0] - 1) / K }));
						// EllipseElement parentVis = (EllipseElement)
						// visualElementsMapping.get(parent_clone);

				// Connect child to parent
				if (parentVis != null) {
					Line line = new Line();

					// Bind start to child..
					line.startXProperty().bind(newVis.layoutXProperty());
					line.startYProperty().bind(newVis.layoutYProperty());
					// ..and end to parent.
					line.endXProperty().bind(parentVis.layoutXProperty());
					line.endYProperty().bind(parentVis.layoutYProperty());

					line.setTranslateX(node_width / 2);
					line.setTranslateY(node_height / 2);

					line.setStrokeLineCap(StrokeLineCap.ROUND);
					line.setStrokeLineJoin(StrokeLineJoin.ROUND);
					visual_lines.getChildren().add(line);
				}
				visual_nodes.getChildren().add(newVis);
			}
		}
		super.render();
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
		double y;
		int depth;
		if (index == 0) { // Root element
			y = vspace;
			depth = 0;
		} else {
			depth = getDepth(index);
			y = getY(depth);
		}
		return y;
	}

	private double getY(int depth) {
		return depth * node_height * 2 + vspace; // Padding on top
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
		height = totDepth * (node_height + vspace) * 2 + node_height - vspace;
		setSize(width, height);
	}
}
