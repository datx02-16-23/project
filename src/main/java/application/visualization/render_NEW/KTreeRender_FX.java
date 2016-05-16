package application.visualization.render_NEW;

import java.util.Arrays;
import java.util.HashMap;
import application.visualization.animation.Animation;
import application.visualization.render2d.KTreeRender;
import javafx.animation.TranslateTransition;
import javafx.collections.ListChangeListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;
import wrapper.datastructures.Array.IndexedElement;
import wrapper.operations.OperationType;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

public class KTreeRender_FX extends KTreeRender implements ListChangeListener<Element> {

	private int elemCount = -1;
	private final Pane nodes = new Pane();
	private final Pane lines = new Pane();
	private final HashMap<String, EllipseElement> elements = new HashMap<String, EllipseElement>();

	/**
	 * Create a new KTreeRender with K children and one parent. Will set K = 2
	 * for any K < 2.
	 * 
	 * @param struct
	 *            The structure to draw as an K-ary tree.
	 * @param K
	 *            The number of children each node has.
	 * @param width
	 *            The width of the nodes.
	 * @param height
	 *            The height of the nodes.
	 * @param hspace
	 *            The horizontal space between elements.
	 * @param vspace
	 *            The verital space between elements.
	 */
	public KTreeRender_FX(DataStructure struct, int K, double width, double height, double hspace, double vspace) {
		super(struct, K, width, height, hspace, vspace);
		getChildren().add(lines);
		getChildren().add(nodes);

		struct.getElements().addListener(this);
	}

	@Override
	public void onChanged(Change<? extends Element> c) {
		int newElemCount = struct.getElements().size();
		if (newElemCount != elemCount) {
			elemCount = newElemCount;
			nodes.getChildren().clear();
			lines.getChildren().clear();
			elements.clear();

			setBackground(elemCount == 0 ? getBackground() : null);

			// Create nodes
			calculateSize();
			EllipseElement ghost;

			int i = 0;
			for (Element e : struct.getElements()) {
				i++;
				ghost = new EllipseElement(e, node_width / 2, node_height / 2);
				IndexedElement ae = (IndexedElement) e;
				ghost.setLayoutX(getX(ae));
				ghost.setLayoutY(getY(ae));

				EllipseElement parentVis = elements.get(Arrays.toString(new int[] { (ae.getIndex()[0] - 1) / K }));

				// Connect child to parent
				if (parentVis != null) {
					Line line = new Line();

					// Bind start to child..
					line.startXProperty().bind(ghost.layoutXProperty());
					line.startYProperty().bind(ghost.layoutYProperty());
					// ..and end to parent.
					line.endXProperty().bind(parentVis.layoutXProperty());
					line.endYProperty().bind(parentVis.layoutYProperty());

					line.setTranslateX(node_width/2);
					line.setTranslateY(node_height/2);

					lines.getChildren().add(line);
				}
				nodes.getChildren().add(ghost);
				elements.put(Arrays.toString(ae.getIndex()), ghost);
			}
			
			/*
			 * Add ghosts to complete the tree.
			 */
			for (; i < completedSize; i++) {
				IndexedElement ae = new IndexedElement(0, new int[]{i});
				ghost = new EllipseElement(ae, node_width / 2, node_height / 2);
				ghost.setLayoutX(getX(ae));
				ghost.setLayoutY(getY(ae));
				ghost.setGhost(true);

				EllipseElement parentVis = elements.get(Arrays.toString(new int[] { (ae.getIndex()[0] - 1) / K }));

				// Connect child to parent
				if (parentVis != null) {
					Line line = new Line();

					// Bind start to child..
					line.startXProperty().bind(ghost.layoutXProperty());
					line.startYProperty().bind(ghost.layoutYProperty());
					// ..and end to parent.
					line.endXProperty().bind(parentVis.layoutXProperty());
					line.endYProperty().bind(parentVis.layoutYProperty());

					line.setTranslateX(node_width/2);
					line.setTranslateY(node_height/2);

					line.setStrokeLineCap(StrokeLineCap.ROUND);
					line.setStrokeLineJoin(StrokeLineJoin.BEVEL);
					lines.getChildren().add(line);
				}
				nodes.getChildren().add(ghost);
			}
		}
	}

	@Override
	public void render() {
		for (Element e : struct.getResetElements()) {
			if (struct.getModifiedElements().contains(e) == false) {
				e.setColor(Color.WHITE);
			}
		}
		struct.elementsDrawn();
	}

	@Override
	public double absX(Element e) {
		return getX(e);
	}

	@Override
	public double absY(Element e) {
		return getY(e);
	}

	@Override
	public void startAnimation(Element e, double start_x, double start_y, double end_x, double end_y) {
//		EllipseElement animated = new EllipseElement(e, node_width / 2, node_height / 2);
		EllipseElement animated = new EllipseElement(e.getNumericValue(), e.getColor(), node_width / 2, node_height / 2);
		EllipseElement real = elements.get(Arrays.toString(((IndexedElement) e).getIndex()));
		real.setGhost(true);
		nodes.getChildren().add(animated);

		TranslateTransition transition = new TranslateTransition(Duration.millis(Animation.ANIMATION_TIME), animated);
		transition.setOnFinished(event -> {
			nodes.getChildren().remove(animated);
			real.setGhost(false);
		});

		transition.setFromX(start_x);
		transition.setFromY(start_y);
		transition.setToX(end_x);
		transition.setToY(end_y);
		transition.playFromStart();
	}

	/*
	 * trash
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * (non-Javadoc)
	 * 
	 * @see application.visualization.render2d.KTreeRender#render()
	 */

	@Override
	public void drawAnimatedElement(Element e, double x, double y, Color color) {

	}

	@Override
	public void drawElement(Element e, Color style, Canvas canvas) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearElement(Element e, Canvas canvas) {
		// TODO Auto-generated method stub

	}

}
