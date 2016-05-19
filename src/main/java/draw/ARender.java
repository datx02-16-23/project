package draw;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import contract.datastructure.DataStructure;
import contract.datastructure.Element;
import contract.datastructure.Array.IndexedElement;
import contract.operation.OP_Remove;
import contract.operation.OperationCounter;
import draw.GridRender.Order;
import draw.element.VisualElement;
import gui.Main;
import gui.Controller;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.StringConverter;

public abstract class ARender extends Pane {

	/**
	 * The value for renders which prefer a fixed width.
	 */
	public static final int DEFAULT_RENDER_WIDTH = 400;
	
	/**
	 * The value for renders which prefer a fixed height.
	 */
	public static final int DEFAULT_RENDER_HEIGHT = 250;
	
	/*
	 * Shared stuff.
	 */
	private static final Background ARRAY_BACKGROUND = createArrayBg();
	private static final Background ORPHAN_BACKGROUND = createOrphanBg();
	private static final Background TREE_BACKGROUND = createTreeBg();
	private static final Border BORDER_MOUSEOVER = getMOBorder();
	private static final String url = "/render/RenderBase.fxml";

	/**
	 * Default node width.
	 */
	public static final double DEFAULT_NODE_WIDTH = 60;
	/**
	 * Default node height.
	 */
	public static final double DEFAULT_NODE_HEIGHT = 40;
	/**
	 * Default horizontal space between nodes.
	 */
	public static final double DEFAULT_NODE_HSPACE = 0;
	/**
	 * Default vertical space between nodes.
	 */
	public static final double DEFAULT_NODE_VSPACE = 0;

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
	protected double totWidth;
	/**
	 * The height of the render.
	 */
	protected double totHeight;

	/**
	 * A mapping of actual Elements to VisualElements.
	 */
	// protected final HashMap<Element, VisualElement> visualElementsMapping =
	// new HashMap<Element, VisualElement>();
	protected final HashMap<String, VisualElement> visualElementsMapping = new HashMap<String, VisualElement>();

	/**
	 * Pane for rendering of visual element nodes.
	 */
	protected final Pane nodes = new Pane();
	/**
	 * The content pane for the render. By default, a Pane for {@link #nodes}
	 * will be added, but renders can add their own as well.
	 */
	protected Pane content;

	/**
	 * The pane used when drawing animated elements.
	 */
	protected Pane animation_pane;
	/**
	 * The root for the FXML Render.
	 */
	private GridPane root;
	/**
	 * Name label.
	 */
	private Label name;
	/**
	 * Header bar.
	 */
	private Node header;
	/**
	 * Info labels.
	 */
	private Label xposLabel, yposLabel, scaleLabel;

	/**
	 * Default constructor. Will use default values: <br>
	 * Element width: {@link #DEFAULT_NODE_HSPACE}<br>
	 * Element height: {@link #DEFAULT_NODE_HEIGHT}<br>
	 * Element horizontal space: {@link #DEFAULT_NODE_HSPACE}<br>
	 * Element vertical space: {@link #DEFAULT_NODE_VSPACE}<br>
	 * 
	 * @param struct
	 *            The DataStructure this Render will draw.
	 */
	public ARender(DataStructure struct) {
		this(struct, DEFAULT_NODE_WIDTH, DEFAULT_NODE_HEIGHT, DEFAULT_NODE_HSPACE, DEFAULT_NODE_VSPACE);
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
	public ARender(DataStructure struct, double width, double height, double hspace, double vspace) {
		this.struct = struct;

		this.node_width = width;
		this.node_height = height;
		this.hspace = hspace;
		this.vspace = vspace;

		// Add stacked canvases
		loadBase();
		initDragAndZoom();

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
		content.getChildren().add(nodes);
		content.setBackground(getStructBackground(struct));
		setSize(150, 150);

		// Name labels
		name = (Label) fxmlLoader.getNamespace().get("name");
		name.setText(struct.toString());
		Label name_mo = (Label) fxmlLoader.getNamespace().get("name_mo");
		name_mo.textProperty().bind(name.textProperty());

		ToolBar headerButtonBar = (ToolBar) fxmlLoader.getNamespace().get("buttons");
		for (Node node : headerButtonBar.getItems()) {
			if (node instanceof ToggleButton == false) {
				nonHideButtons.add(node);
			}
		}
		header = (Node) fxmlLoader.getNamespace().get("header");
		bindHeader();

		// Info labels
		name = (Label) fxmlLoader.getNamespace().get("name");
		name.setText(struct.toString());
		xposLabel = (Label) fxmlLoader.getNamespace().get("xpos");
		yposLabel = (Label) fxmlLoader.getNamespace().get("ypos");
		scaleLabel = (Label) fxmlLoader.getNamespace().get("scale");

		getChildren().add(root);
	}

	// Make header visible only on mousever.
	private void bindHeader() {
		header.visibleProperty().bind(name.visibleProperty().not());
	}

	// Make header visible and enable manual setting of visiblity
	private void unbindHeader() {
		name.setVisible(false);
		header.visibleProperty().unbind();
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
	 * @param millis
	 *            The time in milliseconds the animation should last.
	 */
	public void animateReadWrite(Element src, ARender src_rndr, Element tar, ARender tar_rndr, long millis) {
		/*
		 * Target is unknown. READ: this -> [x]
		 */
		if (tar == null) {
			fade_option = "fade_out";
			animate(src, absX(src), absY(src), // From
					absX(src) - node_width * 0.5, absY(src) - node_height * 1.5, millis); // To
			/*
			 * Source is unknown. WRITE: [x] -> this
			 */
		} else if (src == null) {
			fade_option = "fade_in";
			animate(tar, absX(tar) + node_width * 0.5, absY(tar) + node_height * 1.5, // From
					absX(tar), absY(tar), millis); // To
			/*
			 * Source and target are known.
			 */
		} else { // if (src != null && tar != null)
			tar_rndr.fade_option = "off";
			tar_rndr.animate(tar, src_rndr.absX(src), src_rndr.absY(src), // From
					tar_rndr.absX(tar), tar_rndr.absY(tar), millis); // To
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
	 * @param millis
	 *            The time in milliseconds the animation should last.
	 */
	public void animateSwap(Element var1, ARender var1_rndr, Element var2, ARender var2_rndr, long millis) {
		var1_rndr.fade_option = "swap";
		var1_rndr.animate(var2, var1_rndr.absX(var1), var1_rndr.absY(var1), var2_rndr.absX(var2), var2_rndr.absY(var2),
				millis);
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
		if (content.isVisible()) {
			content.setMinSize(width, height);
			content.setPrefSize(width, height);
			content.setMaxSize(width, height);

			// if (content.isVisible()) {
			height = height + 35; // Space for header bar
			root.setPrefSize(width, height);
			root.setMaxSize(width, height);

			this.setPrefSize(width, height);
			this.setMaxSize(width, height);
		}
	}

	/**
	 * Order the Render_FX to draw the elements of the Data Structure it
	 * carries. <br>
	 * The default implementation only calls:
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
	 * Order the render to calculate it's size.
	 */
	public abstract void calculateSize();

	// Drag and Zoom
	private double transX, transY;
	private double scale = 1;
	private int sign = 1;

	/**
	 * Create listeners to drag and zoom.
	 * 
	 * @param tParent
	 *            The parent to apply transformation to.
	 */
	private void initDragAndZoom() {
		/*
		 * Zoom
		 */
		setOnScroll(event -> {
			sign = event.getDeltaY() > 0 ? 1 : -1;
			
			scale = scale + sign * 0.100000000;
			if (scale < 0.1) {
				scale = 0.1;
				return;
			} else if (scale > 4) {
				scale = 4;
				return;
			}
			setScaleX(scale);
			setScaleY(scale);
			updateInfoLabels();
		});

		// this.setStyle("-fx-background-color: red;");
		// content.setStyle("-fx-background-color: pink;");
		// getParent().setStyle("-fx-background-color: orange;");
		/*
		 * Drag
		 */
		// Record a delta distance for the drag and drop operation.
		setOnMousePressed(event -> {
			transX = getTranslateX() - event.getSceneX();
			transY = getTranslateY() - event.getSceneY();
			setCursor(Cursor.CLOSED_HAND);
		});
		// Restore cursor
		setOnMouseReleased(event -> {
			setCursor(null);
		});
		// Translate canvases
		setOnMouseDragged(event -> {
			setTranslateX(event.getSceneX() + transX);
			setTranslateY(event.getSceneY() + transY);
			updateInfoLabels();
		});
		// Set cursor
		setOnMouseEntered(event -> {
			// this.setCursor(Cursor.OPEN_HAND);
			if (header.visibleProperty().isBound()) {
				name.setVisible(false);
			}
			setBorder(BORDER_MOUSEOVER);
		});
		setOnMouseExited(event -> {
			// this.setCursor(null);
			if (header.visibleProperty().isBound()) {
				name.setVisible(true);
			}
			setBorder(null);
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
	 * @param millis
	 *            The time in milliseconds the animation should last.
	 */
	public void animate(Element e, double start_x, double start_y, double end_x, double end_y, long millis) {
		if (content.isVisible() == false) {
			return;
		}
		ParallelTransition trans = new ParallelTransition();

		// VisualElement real = visualElementsMapping.get(e);
		int[] i = ((IndexedElement) e).getIndex();
		Arrays.copyOf(i, i.length);

		final VisualElement real = visualElementsMapping.get(Arrays.toString(i));
		if (real == null) {
			System.err.println("Failed to resolve visual for: " + struct);
			return;
		}

		VisualElement animated = real.clone();

		animated.unbind();

		animation_pane.getChildren().add(animated);

		final boolean useGhost;

		/*
		 * Fade
		 */

		switch (fade_option) {
		case "fade_in":
			useGhost = true;
			trans.getChildren().add(fadeIn(millis));
			break;

		case "fade_out":
			useGhost = false;
			trans.getChildren().add(fadeOut(millis));
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
		TranslateTransition tt = new TranslateTransition(Duration.millis(millis));
		tt.setOnFinished(event -> {
			animation_pane.getChildren().remove(animated);
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
		return getX(e) + bx;
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
	 * Force the Render to initialise all elements. This method will attempt
	 * create elements and add them to the standard pane. It will clear the
	 * children of all children in {@link #content} before beginning.
	 * {@link #bellsAndWhistles} will be called on every element.
	 * 
	 * 
	 * @return True if there was anything to draw.
	 */
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
			newVis.setLayoutY(getY(e));

			nodes.getChildren().add(newVis);
			visualElementsMapping.put(Arrays.toString(((IndexedElement) e).getIndex()), newVis);

			bellsAndWhistles(e, newVis);
		}

		return true;
	}

	/**
	 * Create a bound node element in whatever style the Render prefers to use.
	 * 
	 * @param e
	 *            The element to bind.
	 * @return A new bound VisualElement.
	 */
	protected abstract VisualElement createVisualElement(Element e);

	/**
	 * Create an unbound node element in whatever style the Render prefers to
	 * use.
	 * 
	 * @param value
	 *            The value of the element.
	 * @param color
	 *            The colour of the element.
	 * @return A new unbound VisualElement.
	 */
	protected abstract VisualElement createVisualElement(double value, Color color);

	/**
	 * Decorator method used to attach bells and whistles to the current
	 * element. {@link #init} will called this method on every element.
	 * 
	 * @param e
	 *            The element to attach a whistle to.
	 * @param ve
	 *            The VisualElement to attach a bell to.
	 */
	protected abstract void bellsAndWhistles(Element e, VisualElement ve);

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
		public static class Converter extends StringConverter<Integer> {

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

		public static RenderSVF resolve(DataStructure struct) {
			RenderSVF svf = null;

			switch (struct.resolveVisual()) {
			case bar:
				svf = null;
				break;

			case box:
				ArrayList<Integer> values = new ArrayList<Integer>();
				values.add(Order.ROW_MAJOR.optionNbr);
				values.add(Order.COLUMN_MAJOR.optionNbr);
				ArrayList<String> userValues = new ArrayList<String>();
				userValues.add(Order.ROW_MAJOR.name);
				userValues.add(Order.COLUMN_MAJOR.name);
				svf = new RenderSVF(values, userValues);
				break;
			case single:
				svf = null;
				break;

			case tree:
				svf = new RenderSVF(2, 1337);
				break;

			default:
				break;

			}
			return svf;
		}
	}

	/*
	 * Boring crap.
	 */
	private static Background createArrayBg() {
		return new Background(new BackgroundImage(new Image(Controller.class.getResourceAsStream("/assets/array.png")),
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				BackgroundSize.DEFAULT));
	}

	private static Background createOrphanBg() {
		return new Background(new BackgroundImage(
				new Image(Controller.class.getResourceAsStream("/assets/orphan2.png")), BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
	}

	private static Background createTreeBg() {
		return new Background(new BackgroundImage(new Image(Controller.class.getResourceAsStream("/assets/tree.png")),
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				BackgroundSize.DEFAULT));
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

	public static final Transition fadeIn(long millis) {
		FadeTransition ft = new FadeTransition(Duration.millis(millis));

		ft.setFromValue(1.0);
		ft.setToValue(0);
		return ft;
	}

	public static final Transition fadeOut(long millis) {
		FadeTransition ft = new FadeTransition(Duration.millis(millis));
		ft.setFromValue(1.0);
		ft.setToValue(0);
		return ft;
	}

	public void showStats() {
		OperationCounter oc = struct.getCounter();
		Main.console.info("Statistics for \"" + struct + "\":");
		Main.console.info("\tReads: " + oc.getReads());
		Main.console.info("\tWrites: " + oc.getWrites());
		Main.console.info("\tSwaps: " + oc.getSwap());

		// TODO: Live update pop up in addition to the printout.
	}

	// TODO
	public void showOptions() {
		System.out.println("options");
	}

	// Center on button when hiding or showing
	private double conbwhs = 0;
	// Used to hide other buttons when minimzing
	private final ArrayList<Node> nonHideButtons = new ArrayList<Node>();

	public void toggleHidden(Event e) {
		ToggleButton tb = (ToggleButton) e.getSource();

		if (tb.isSelected()) {
			tb.setText("Show");
			collapse();
		} else {
			tb.setText("Hide");
			expand();
		}
	}

	private void collapse() {
		// Make the render expand and collapse and NE corner.
		conbwhs = content.getPrefWidth() - 150;
		setTranslateX(getTranslateX() + conbwhs);

		// Show only header
		root.setPrefSize(150, 20);
		root.setMaxSize(150, 20);
		this.setPrefSize(150, 20);
		this.setMaxSize(150, 20);
		unbindHeader();
		for (Node n : nonHideButtons) {
			n.setVisible(false);
		}
		content.setVisible(false);
	}

	private void expand() {
		content.setVisible(true);
		setTranslateX(getTranslateX() - conbwhs);
		bindHeader();
		if (content.getBackground() == null) {
			calculateSize();
		} else {
			setSize(150, 115);
		}
		for (Node n : nonHideButtons) {
			n.setVisible(true);
		}
	}

	/**
	 * Returns the Pane used to draw element nodes.
	 * 
	 * @return The Pane used to draw element nodes.
	 */
	public Pane getNodes() {
		return nodes;
	}

	/**
	 * Set the Pane used for drawing animated elements.
	 * 
	 * @param animation_pane
	 *            A Pane for animation.
	 */
	public void setAnimationPane(Pane animation_pane) {
		this.animation_pane = animation_pane;
	}
	
	/**
	 * Update info labels in the header.
	 */
	public void updateInfoLabels(){
		DecimalFormat df = new DecimalFormat("#0.00");
		xposLabel.setText("XPos: " + (int) (getTranslateX() + 0.5));
		yposLabel.setText("| YPos: " + (int) (getTranslateY() + 0.5));
		scaleLabel.setText("| Scale: " + df.format(scale));
	}
}
