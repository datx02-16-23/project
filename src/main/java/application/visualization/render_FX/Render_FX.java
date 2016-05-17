package application.visualization.render_FX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import application.gui.GUI_Controller;
import application.gui.Main;
import application.visualization.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import javafx.util.StringConverter;
import wrapper.datastructures.Array.IndexedElement;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;
import wrapper.operations.OP_Remove;
import wrapper.operations.OperationCounter;

public abstract class Render_FX extends Pane {

	/*
	 * Shared stuff.
	 */
	private static final Background ARRAY_BACKGROUND = createArrayBg();
	private static final Background ORPHAN_BACKGROUND = createOrphanBg();
	private static final Background TREE_BACKGROUND = createTreeBg();
	private static final Border BORDER_MOUSEOVER = getMOBorder();
	private static final String url = "/visualization/RenderBase.fxml";

	/**
	 * Default node width.
	 */
	public static final double DEFAULT_NODE_WIDTH = 60;

	/**
	 * Default node height.
	 */
	public static final double DEFAULT_NODE_HEIGHT = 40;

	/**
	 * The DataStructure this render represents.
	 */
	protected final DataStructure struct;

	/**
	 * Width of individual elements bounding boxes.
	 */
	protected final double node_width;
	/**
	 * Height of individual elements bounding boxes.
	 */
	protected final double node_height;

	/**
	 * Horizontal space between elements.
	 */
	protected final double hspace;
	/**
	 * Vertical space between elements.
	 */
	protected final double vspace;
	/**
	 * The width of the render.
	 */
	protected double width;
	/**
	 * The height of the render.
	 */
	protected double height;

	/**
	 * A mapping of actual Elements to VisualElements.
	 */
	// protected final HashMap<Element, VisualElement> visualElementsMapping =
	// new HashMap<Element, VisualElement>();
	protected final HashMap<String, VisualElement> visualElementsMapping = new HashMap<String, VisualElement>();

	/**
	 * The visual element nodes.
	 */
	protected final Pane nodes = new Pane();
	/**
	 * Content pane.
	 */
	protected Pane content;

	/**
	 * The pane used when drawing animated elements.
	 */
	protected Pane animated_nodes;
	/**
	 * The root for the FXML Render.
	 */
	private GridPane root;
	/**
	 * Name label.
	 */
	private Label name;

	/**
	 * Set the Pane used for drawing animated elements.
	 * 
	 * @param animated_nodes
	 *            A Pane for animation.
	 */
	public void setAnimated(Pane animated_nodes) {
		this.animated_nodes = animated_nodes;
	}

	/**
	 * Creates a new Render.
	 * 
	 * @param struct
	 *            The structure to render.
	 * @param width
	 *            The width of the elements in this Render.
	 * @param height
	 *            The height of the elements in this Render.
	 * @param hspace
	 *            The horizontal space between elements in this Render.
	 * @param vspace
	 *            The vertical space between elements in this Render.
	 */
	public Render_FX(DataStructure struct, double width, double height, double hspace, double vspace) {
		this.struct = struct;

		this.node_width = width;
		this.node_height = height;
		this.hspace = hspace;
		this.vspace = vspace;

		// Add stacked canvases
		loadBase();

		this.setMinSize(150, 20);
		this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.setPrefSize(150, 170);
	}

	private void loadBase() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(url));
		fxmlLoader.setController(this);

		try {
			root = (GridPane) fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		root.setMinSize(150, 20);

		// Content pane
		content = (Pane) fxmlLoader.getNamespace().get("content");
		content.getChildren().add(this.nodes);
		content.setBackground(getStructBackground(struct));
		setSize(150, 150);

		// Name labels
		name = (Label) fxmlLoader.getNamespace().get("name");
		name.setText(struct.toString());
		Label name_mo = (Label) fxmlLoader.getNamespace().get("name_mo"); //Visible only on mouseover
		name_mo.textProperty().bind(name.textProperty());

		Node header = (Node) fxmlLoader.getNamespace().get("header"); //Visible only on mouseover
		header.visibleProperty().bind(name.visibleProperty().not());

		getChildren().add(root);
		initDragAndZoom(this);
	}

	/*
	 * Animation
	 */

	/**
	 * Default animation for a Remove operation. Does nothing.
	 * 
	 * @param remove
	 *            The operation to animate.
	 */
	public void animateRemove(OP_Remove remove) {
		// Do nothing.
	}

	/**
	 * Default animations for a read or write.
	 * 
	 * @param src
	 *            The source element.
	 * @param src_rndr
	 *            The render for the source element.
	 * @param tar
	 *            The target element.
	 * @param tar_rndr
	 *            The render for the target element.
	 */
	public void animateReadWrite(Element src, Render_FX src_rndr, Element tar, Render_FX tar_rndr) {
		/*
		 * Target is unknown. READ: this -> [x]
		 */
		if (tar == null) {
			src_rndr.fade_option = "fade_out";
			src_rndr.animate(src, src_rndr.absX(src), src_rndr.absY(src), // From
					src_rndr.absX(src) - node_width * 0.5, src_rndr.absY(src) - node_height * 1.5); // To
			/*
			 * Source is unknown. WRITE: [x] -> this
			 */
		} else if (src == null) {
			tar_rndr.fade_option = "fade_in";
			tar_rndr.animate(tar, tar_rndr.absX(tar) + node_width * 0.5, tar_rndr.absY(tar) + node_height * 1.5, // From
					tar_rndr.absX(tar), tar_rndr.absY(tar)); // To
			/*
			 * Source and target are known.
			 */
		} else { // if (src != null && tar != null)
			tar_rndr.fade_option = "off";
			tar_rndr.animate(tar, src_rndr.absX(src), src_rndr.absY(src), // From
					tar_rndr.absX(tar), tar_rndr.absY(tar)); // To
		}
	}

	/**
	 * Default animation of a swap between two elements.
	 * 
	 * @param var1
	 *            The first element.
	 * @param var1_rndr
	 *            The render for the first element.
	 * @param var2
	 *            The second element.
	 * @param var2_rndr
	 *            The render for the second element.
	 */
	public void animateSwap(Element var1, Render_FX var1_rndr, Element var2, Render_FX var2_rndr) {
		var1_rndr.fade_option = "swap";
		var1_rndr.animate(var2, var1_rndr.absX(var1), var1_rndr.absY(var1), var2_rndr.absX(var2), var2_rndr.absY(var2));
	}

	/**
	 * Calls, setPrefSize, setMaxSize, setWidth and setHeight.
	 * 
	 * @param width
	 *            The width of this Render.
	 * @param height
	 *            The height of this Render.
	 */
	protected void setSize(double width, double height) {
		content.setMinSize(width, height);
		content.setPrefSize(width, height);
		content.setMaxSize(width, height);

		height = height + 35; // Space for header bar
		root.setPrefSize(width, height);
		root.setMaxSize(width, height);

		this.setPrefSize(width, height);
		this.setMaxSize(width, height);
		// this.setWidth(width);
		// this.setHeight(height);
	}

	/**
	 * Order the Render_FXto draw the elements of the Data Structure it carries.
	 * The default implementation of this method only does one thing: <br>
	 * <br>
	 * 
	 * <b>{@code struct.elementsDrawn(Color.WHITE);}</b>
	 */
	public void render() {
		struct.elementsDrawn(Color.WHITE);
	}

	/**
	 * Returns the absolute x-coordinate of an element.
	 * 
	 * @param e
	 *            An element to resolve coordinates for.
	 * @return The absolute x-coordinate of the element.
	 */
	public abstract double getX(Element e);

	/**
	 * Returns the absolute y-coordinate of an element.
	 * 
	 * @param e
	 *            An element to resolve coordinates for.
	 * @return The absolute y-coordinate of the element.
	 */
	public abstract double getY(Element e);

	/**
	 * Order the Render_FX to calculate it's size. The default implementation of
	 * this method does nothing.
	 */
	public void calculateSize() {
		// Do nothing.
	}

	// Drag and Zoom
	private double transX, transY;
	private double scale = 1;
	private int sign = 1;

	/**
	 * Create listeners to drag and zoom.
	 */
	private void initDragAndZoom(Node node) {
		/*
		 * Zoom
		 */
		node.setOnScroll(event -> {
			sign = event.getDeltaY() > 0 ? 1 : -1;
			scale = scale + sign * 0.1;
			if (scale < 0.1) {
				scale = 0.1;
				return;
			} else if (scale > 2) {
				scale = 2;
				return;
			}
			this.setScaleX(scale);
			this.setScaleY(scale);
		});
		/*
		 * Drag
		 */
		// Record a delta distance for the drag and drop operation.
		node.setOnMousePressed(event -> {
			transX = this.getTranslateX() - event.getSceneX();
			transY = this.getTranslateY() - event.getSceneY();
			this.getParent().setCursor(Cursor.CLOSED_HAND);
		});
		// Restore cursor
		node.setOnMouseReleased(event -> {
			this.getParent().setCursor(null);
		});
		// Translate canvases
		node.setOnMouseDragged(event -> {
			this.setTranslateX(event.getSceneX() + transX);
			this.setTranslateY(event.getSceneY() + transY);
		});
		// Set cursor
		node.setOnMouseEntered(event -> {
//			this.setCursor(Cursor.OPEN_HAND);
			name.setVisible(false);
			this.setBorder(BORDER_MOUSEOVER);
		});
		node.setOnMouseExited(event -> {
//			this.setCursor(null);
			name.setVisible(true);
			this.setBorder(null);
		});
	}

	/**
	 * Returns the DataStructure held by this Render.
	 * 
	 * @return The DataStructure held by this Render.
	 */
	public DataStructure getDataStructure() {
		return struct;
	}

	String fade_option = "bla";

	/**
	 * Start an animation of an element to a point.
	 * 
	 * @param e
	 *            The element to animate.
	 * @param start_x
	 *            Start point x-coordinate.
	 * @param start_y
	 *            Start point y-coordinate.
	 * @param end_x
	 *            End point x-coordinate.
	 * @param end_y
	 *            End point y-coordinate.
	 */
	public void animate(Element e, double start_x, double start_y, double end_x, double end_y) {
		ParallelTransition trans = new ParallelTransition();

		// VisualElement real = visualElementsMapping.get(e);
		int[] i = ((IndexedElement) e).getIndex();
		Arrays.copyOf(i, i.length);
		VisualElement real = visualElementsMapping.get(Arrays.toString(i));

		VisualElement animated = real.clone();
		animated.unbind();

		animated_nodes.getChildren().add(animated);

		final boolean useGhost;

		/*
		 * Fade
		 */

		switch (fade_option) {
		case "fade_in":
			useGhost = true;
			trans.getChildren().add(fadeIn());
			break;

		case "fade_out":
			useGhost = false;
			trans.getChildren().add(fadeOut());
			break;

		case "swap":
			useGhost = true;
			break;
		default:
			useGhost = false;
			break;
		}

		/*
		 * Move
		 */
		TranslateTransition tt = new TranslateTransition(Duration.millis(Animation.ANIMATION_TIME));
		tt.setOnFinished(event -> {
			animated_nodes.getChildren().remove(animated);
			if (useGhost) {
				real.setGhost(false);
			}
		});

		tt.setFromX(start_x);
		tt.setFromY(start_y);
		tt.setToX(end_x);
		tt.setToY(end_y);

		trans.getChildren().add(tt);

		/*
		 * Showtime!!
		 */
		trans.setNode(animated);
		real.setGhost(useGhost);
		trans.play();
	}

	/**
	 * Returns the absolute x-coordinate for the element e.
	 * 
	 * @param e
	 *            An element owned by this Render.
	 * @return The absolute x-coordinates of e.
	 */
	public double absX(Element e) {
		double bx = this.getTranslateX() + this.getLayoutX() + content.getLayoutX();
		return this.getX(e) + bx;
	}

	/**
	 * Returns the absolute y-coordinate for the element e.
	 * 
	 * @param e
	 *            An element owned by this Render.
	 * @return The absolute y-coordinates of e.
	 */
	public double absY(Element e) {
		double by = this.getTranslateY() + this.getLayoutY() + content.getLayoutY();
		return this.getY(e) + by;
	}

	/**
	 * Force the Render to initialise all elements.
	 */
	public abstract void init();

	/**
	 * Returns the SpinnerValueFactory for this Render, or null if there are no
	 * options. The default implementation of this method returns null.
	 * 
	 * @return A list of options for this Render, or null if there are none.
	 */
	public RenderSVF getOptionsSpinnerValueFactory() {
		return null;
	}

	/**
	 * SpinnerValueFactory for Render_FXimplementations.
	 * 
	 * @author Richard Sundqvist
	 *
	 */
	public static class RenderSVF extends SpinnerValueFactory<Integer> {

		// Mode variable
		private final boolean explicit;
		/**
		 * Used to cycle through explicit values.
		 */
		private final ArrayList<Integer> values = new ArrayList<>();
		/**
		 * Used to cycle through ranges.
		 */
		private final int min;
		private final int max;
		/**
		 * The current spinner value.
		 */
		private int current;

		/**
		 * Creates a new RenderSVF with the specified min and max value.
		 * Increments will occur in steps of one. Rollover is applied, so
		 * {@code max + 1 = min} and vice versa.
		 * 
		 * @param min
		 *            The minimum value.
		 * @param max
		 *            The maximum value.
		 */
		public RenderSVF(int min, int max) {
			this.min = min;
			this.max = max;
			current = min;
			setConverter(new Converter());
			for (int i = min; i <= max; i++) {
				values.add(new Integer(i));
			}
			setValue(current);
			explicit = false;
		}

		/**
		 * Creates a new RenderSVF with the specified values and userValues. The
		 * user values is what will be shown to the user when going through the
		 * options of the spinner.
		 * 
		 * @param values
		 *            The keys for this RenderSpinner.
		 * @param userValues
		 *            Their display values.
		 */
		public RenderSVF(List<Integer> values, List<String> userValues) {
			min = -1;
			max = -1;
			setConverter(new Converter(values, userValues));
			for (int i = 0; i < values.size(); i++) {
				this.values.add(values.get(i));
			}
			current = values.get(0);
			setValue(current);
			explicit = true;
		}

		@Override
		public void decrement(int steps) {
			if (explicit) {
				current = current - steps < 0 ? values.size() - 1 : current - steps;
			} else {
				current = current - steps < min ? max : current - steps;
			}
			setValue(current);
		}

		@Override
		public void increment(int steps) {
			if (explicit) {
				current = current + steps > values.size() - 1 ? 0 : current + steps;
			} else {
				current = current + steps > max ? min : current + steps;
			}
			setValue(current);
		}

		public String toString() {
			if (explicit) {
				return "Explicit. Values =  " + values;
			} else {
				return "Non-explict. Range = [" + min + ", " + max + "]";
			}
		}

		/**
		 * Converter for the SpinnerSVF class.
		 * 
		 * @author Richard Sundqvist
		 *
		 */
		private class Converter extends StringConverter<Integer> {

			private final HashMap<Integer, String> conversion;
			private final boolean explicit;

			public Converter(List<Integer> values, List<String> userValues) {
				conversion = new HashMap<Integer, String>();
				for (int i = 0; i < values.size(); i++) {
					conversion.put(values.get(i), userValues.get(i));
				}
				explicit = true;
			}

			public Converter() {
				conversion = null;
				explicit = false;
			}

			@Override
			public String toString(Integer integer) {
				if (explicit) {
					return conversion.get(integer);
				} else {
					return integer.toString();
				}
			}

			@Override
			public Integer fromString(String string) {
				if (explicit) {
					Integer ans = null;
					for (Integer i : conversion.keySet()) {
						if (conversion.get(i).equals(string)) {
							ans = i;
							break;
						}
					}
					return ans;
				} else {
					return Integer.parseInt(string);
				}
			}
		}
	}

	/*
	 * Boring crap.
	 */
	private static Background createArrayBg() {
		return new Background(new BackgroundImage(
				new Image(GUI_Controller.class.getResourceAsStream("/assets/array.png")), BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
	}

	private static Background createOrphanBg() {
		return new Background(new BackgroundImage(
				new Image(GUI_Controller.class.getResourceAsStream("/assets/orphan2.png")), BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
	}

	private static Background createTreeBg() {
		return new Background(new BackgroundImage(
				new Image(GUI_Controller.class.getResourceAsStream("/assets/tree.png")), BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
	}

	private static Border getMOBorder() {
		return new Border(new BorderStroke(Color.web("#123456"), BorderStrokeStyle.SOLID, new CornerRadii(5),
				new BorderWidths(3), new Insets(-5)));
	}

	private static Background getStructBackground(DataStructure struct) {
		if (struct == null) {
			return null;
		}

		switch (struct.rawType) {
		case array:
			return ARRAY_BACKGROUND;
		case tree:
			return TREE_BACKGROUND;
		case independentElement:
			return ORPHAN_BACKGROUND;
		default:
			return null;
		}
	}

	public static final Transition fadeIn() {
		FadeTransition ft = new FadeTransition(Duration.millis(Animation.ANIMATION_TIME));

		ft.setFromValue(1.0);
		ft.setToValue(0);
		return ft;
	}

	public static final Transition fadeOut() {
		FadeTransition ft = new FadeTransition(Duration.millis(Animation.ANIMATION_TIME));
		ft.setFromValue(1.0);
		ft.setToValue(0);
		return ft;
	}

	// TODO
	public void showStats() {
		OperationCounter oc = struct.getCounter();
		Main.console.info("Statistics for \"" + struct + "\":");
		Main.console.info("\tReads: " + oc.getReads());
		Main.console.info("\tWrites: " + oc.getWrites());
		Main.console.info("\tSwaps: " + oc.getSwap());
	}

	// TODO
	public void showOptions() {
		System.out.println("options");
	}

	// TODO
	public void toggleHidden(Event e) {
		ToggleButton tb = (ToggleButton) e.getSource();

		if (tb.isSelected()) {
			tb.setText("Expand");
		} else {
			tb.setText("Collapse");
		}
	}
}
