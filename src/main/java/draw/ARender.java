package draw;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import assets.DasConstants;
import contract.datastructure.DataStructure;
import contract.datastructure.Element;
import contract.datastructure.Array.IndexedElement;
import contract.operation.OperationCounter;
import contract.operation.OperationCounter.OperationCounterHaver;
import draw.ARenderAnimation.AnimationOption;
import draw.element.ElementShape;
import draw.element.VisualElement;
import gui.Main;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

public abstract class ARender extends Pane {
	
	
	/**
	 * The DataStructure this render represents.
	 */
	protected final DataStructure struct;

	/**
	 * Width of individual elements bounding boxes.
	 */
	protected final double nodeWidth;
	/**
	 * Height of individual elements bounding boxes.
	 */
	protected final double nodeHeight;

	/**
	 * Horizontal space between elements.
	 */
	protected final double hSpace;
	/**
	 * Vertical space between elements.
	 */
	protected final double vSpace;
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
	// protected final HashMap<Element, VisualElement> visualMap =
	// new HashMap<Element, VisualElement>();
	protected final HashMap<String, VisualElement> visualMap = new HashMap<String, VisualElement>();

	/**
	 * Pane for rendering of visual element nodes. Added to {@link contentPane}
	 * automatically.
	 */
	protected final Pane defaultNodePane = new Pane();
	/**
	 * The content pane for the render. By default, a Pane for nodes (
	 * {@link #defaultNodePane}) will be added, but renders can add their own
	 * panes to {@code contentPane} if need be.
	 */
	protected Pane contentPane;

	/**
	 * The pane used when drawing animated elements.
	 */
	protected final Pane animPane;
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
	 * The element style to use.
	 */
	protected ElementShape elementStyle;

	/**
	 * Default constructor. Will use default values: <br>
	 * Element width: {@link DasConstants#ELEMENT_HSPACE}<br>
	 * Element height: {@link DasConstants#ELEMENT_HEIGHT}<br>
	 * Element horizontal space: {@link DasConstants#ELEMENT_HSPACE}<br>
	 * Element vertical space: {@link DasConstants#ELEMENT_VSPACE}<br>
	 * 
	 * @param struct
	 *            The DataStructure this Render will draw.
	 */
	public ARender(DataStructure struct) {
		this(struct, DasConstants.ELEMENT_WIDTH, DasConstants.ELEMENT_HEIGHT,
				DasConstants.ELEMENT_HSPACE, DasConstants.ELEMENT_VSPACE);
	}

	/**
	 * Creates a new Render.
	 * 
	 * @param struct
	 *            The structure to render.
	 * @param nodeWidth
	 *            The width of the elements in this Render.
	 * @param nodeHeight
	 *            The height of the elements in this Render.
	 * @param hSpace
	 *            The horizontal space between elements in this Render.
	 * @param vSpace
	 *            The vertical space between elements in this Render.
	 */
	public ARender(DataStructure struct, double nodeWidth, double nodeHeight, double hSpace, double vSpace) {
		this.struct = struct;

		this.nodeWidth = nodeWidth;
		this.nodeHeight = nodeHeight;
		this.hSpace = hSpace;
		this.vSpace = vSpace;

		this.animPane = new Pane();

		// Add stacked canvases
		loadBase();
		initDragAndZoom();

		// this.setMinSize(150, 20);
		// this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		expand();
	}

	private void loadBase() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(DasConstants.RENDER_FXML_URL));
		fxmlLoader.setController(this);

		try {
			root = (GridPane) fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		root.setMinSize(150, 20);

		// Content pane
		contentPane = (Pane) fxmlLoader.getNamespace().get("content");
		contentPane.getChildren().add(defaultNodePane);
		contentPane.setBackground(getStructBackground(struct));
		setSize(150, 125); // Size of background images

		// Name labels
		name = (Label) fxmlLoader.getNamespace().get("name");
		name.setText(struct.toString());
		Label name_mo = (Label) fxmlLoader.getNamespace().get("name_mo");
		name_mo.textProperty().bind(name.textProperty());

		ToolBar headerButtonBar = (ToolBar) fxmlLoader.getNamespace().get("buttons");
		for (Node node : headerButtonBar.getItems()) {
			if (node instanceof ToggleButton == false) {
				optionalHeaderContent.add(node);
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
	 * Default animation for a Remove operation.
	 * 
	 * @param remove
	 *            The operation to animate.
	 * @param millis
	 *            The time in milliseconds the animation should last.
	 */
	//@formatter:off
	public void animateRemove(Element tar, long millis) {
		double x = absX(tar);
		double y = absY(tar);

		ARenderAnimation.animate(tar,
				x, y, x, y,
				millis * 4, this,	
				AnimationOption.FADE_OUT, AnimationOption.SHRINK, AnimationOption.USE_GHOST);
	}
	//@formatter:on

	/**
	 * Default animations for a read or write.
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
	//@formatter:off
	public void animateReadWrite(Element src, ARender srcRender, Element tar, ARender tarRender, long millis) {
		boolean hasSource = srcRender != null;
		boolean hasTarget = tarRender != null;
		double x1 = -1; double y1 = -1;
		double x2 = -1; double y2 = -1;
		
		if(hasSource){
			x1 = srcRender.absX(src);
			y1 = srcRender.absY(src);
		}
		
		if(hasTarget){
			x2 = tarRender.absX(tar);
			y2 = tarRender.absY(tar);
		}
		
		if(hasSource && hasTarget){
			ARenderAnimation.animate(tar,
					x1, y1, 
					x2, y2,
					millis, this);
		} else if (hasSource) {
			//Source only
			ARenderAnimation.animate(src,
					x1, y1, 
					x1, y1 - DasConstants.ELEMENT_HEIGHT * 2,
					millis, this,
					AnimationOption.FADE_OUT, AnimationOption.SHRINK);
		} else {
			//Target only
			ARenderAnimation.animate(tar,
					x1, y1 - DasConstants.ELEMENT_HEIGHT * 2,
					x1, y1,
					millis, this,
					AnimationOption.FADE_IN, AnimationOption.GROW);
		}
	}
	//@formatter:off

	/**
	 * Default animation of a swap between two elements.
	 * 
	 * @param var1
	 *            The first element.
	 * @param render1
	 *            The render for the first element.
	 * @param var2
	 *            The second element.
	 * @param render2
	 *            The render for the second element.
	 * @param millis
	 *            The time in milliseconds the animation should last.
	 */
	// formatter:off
	public void animateSwap(Element var1, ARender render1, Element var2, ARender render2, long millis) {
		ARenderAnimation.animate(var2,
				render1.absX(var1), render1.absY(var1),
				render2.absX(var2), render2.absY(var2),
				millis, this,
				AnimationOption.USE_GHOST);
	}
	// formatter:on

	/**
	 * Calls, setPrefSize, setMaxSize, setWidth and setHeight.
	 * 
	 * @param width
	 *            The width of this Render.
	 * @param height
	 *            The height of this Render.
	 */
	protected void setSize(double width, double height) {
		if (contentPane.isVisible()) {
			contentPane.setMinSize(width, height);
			contentPane.setPrefSize(width, height);
			contentPane.setMaxSize(width, height);

			// if (content.isVisible()) {
			height = height + 45; // Space for header bar
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
	 * Returns the absolute x-coordinate of an element. Returns -1 if the
	 * calculation fails.
	 * 
	 * @param e
	 *            An element to resolve coordinates for.
	 * @return The absolute x-coordinate of the element.
	 */
	public abstract double getX(Element e);

	/**
	 * Returns the absolute y-coordinate of an element. Returns -1 if the
	 * calculation fails.
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
			setBorder(DasConstants.BORDER_MOUSEOVER);
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

	/**
	 * Returns the absolute x-coordinate for the element e. Returns -1 if the calculation fails.
	 * 
	 * @param e
	 *            An element owned by this Render.
	 * @return The absolute x-coordinates of e.
	 */
	public double absX(Element e) {
		double bx = this.getTranslateX() + this.getLayoutX();
		return getX(e) + bx;
	}

	/**
	 * Returns the absolute y-coordinate for the element e. Returns -1 if the calculation fails.
	 * 
	 * @param e
	 *            An element owned by this Render.
	 * @return The absolute y-coordinates of e.
	 */
	public double absY(Element e) {
		double by = this.getTranslateY() + this.getLayoutY() + contentPane.getLayoutY();
		return this.getY(e) + by;
	}

	/**
	 * Force the Render to initialise all elements. This method will attempt
	 * create elements and add them to the standard pane. It will clear the
	 * children of all children in {@link #contentPane} before beginning.
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
		for (Node n : contentPane.getChildren()) {
			((Pane) n).getChildren().clear();
		}

		visualMap.clear();
		contentPane.setBackground(null);
		calculateSize();

		// Create nodes
		VisualElement newVis;

		for (Element e : struct.getElements()) {
			newVis = createVisualElement(e);
			newVis.setLayoutX(getX(e));
			newVis.setLayoutY(getY(e));

			defaultNodePane.getChildren().add(newVis);
			visualMap.put(Arrays.toString(((IndexedElement) e).getIndex()), newVis);

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

	private static Background getStructBackground(DataStructure struct) {
		if (struct == null) {
			return null;
		}

		switch (struct.rawType) {
		case array:
			return DasConstants.ARRAY_BACKGROUND;
		case tree:
			return DasConstants.TREE_BACKGROUND;
		case independentElement:
			return DasConstants.ORPHAN_BACKGROUND;
		default:
			return null;
		}
	}
	
	/**
	 * Print statistics for the structure this render carries.
	 */
	public void printStats() {
		Main.console.info("Statistics for \"" + struct + "\":");
		OperationCounterHaver.printStats(struct);
	}

	public void showOptions() {
		//TODO implement options
		System.out.println("options");
	}

	// Center on button when hiding or showing.
	private double conbwhs = 0;
	// Used to hide other buttons when minimising.
	private final ArrayList<Node> optionalHeaderContent = new ArrayList<Node>();

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
		conbwhs = contentPane.getPrefWidth() - 150;
		setTranslateX(getTranslateX() + conbwhs);

		// Show only header
		root.setPrefSize(150, 20);
		root.setMaxSize(150, 20);
		this.setPrefSize(150, 20);
		this.setMaxSize(150, 20);
		unbindHeader();
		for (Node n : optionalHeaderContent) {
			n.setVisible(false);
		}
		contentPane.setVisible(false);
	}

	private void expand() {
		contentPane.setVisible(true);
		setTranslateX(getTranslateX() - conbwhs);
		bindHeader();
		calculateSize(); // Size recalculation is disabled while hidden.

		if (contentPane.getBackground() == null) {
			calculateSize();
		} else {
			setSize(150, 90);
		}
		for (Node n : optionalHeaderContent) {
			n.setVisible(true);
		}
	}

	/**
	 * Returns the Pane used to draw element nodes.
	 * 
	 * @return The Pane used to draw element nodes.
	 */
	public Pane getNodes() {
		return defaultNodePane;
	}

	/**
	 * Returns the Pane used for drawing animated elements.
	 * 
	 * @param animPane
	 *            The used Pane for animation.
	 */
	public Pane getAnimationPane() {
		return this.animPane;
	}

	/**
	 * Update info labels in the header.
	 */
	public void updateInfoLabels() {
		DecimalFormat df = new DecimalFormat("#0.00");
		xposLabel.setText("XPos: " + (int) (getTranslateX() + 0.5));
		yposLabel.setText("| YPos: " + (int) (getTranslateY() + 0.5));
		scaleLabel.setText("| Scale: " + df.format(scale));
	}

	private boolean playFailureSound = true;

	/**
	 * Makes the header red when something goes wrong.
	 */
	public void renderFailure() {
		if (playFailureSound) {
			playFailureSound = false;
			URL resource = getClass().getResource("/assets/sad_trombone.mp3");
			Media media = new Media(resource.toString());
			MediaPlayer mp3 = new MediaPlayer(media);
			mp3.play();

			header.setStyle("-fx-background-color: rgba(255, 0, 0, 0.5);");
			contentPane.setStyle("-fx-background-color: rgba(255, 0, 0, 0.5);");
			Main.console.err("Render Failure in " + this.toString() + ".");
		}
	}

	/**
	 * Returns the visual map for this Render.
	 * 
	 * @return The visual map for this Render.
	 */
	public HashMap<String, VisualElement> getVisualMap() {
		return visualMap;
	}

	public String toString() {
		return this.getClass().getSimpleName() + " (" + this.struct + ")";
	}
	
	/**
	 * Set the currently used element style.
	 * @param newStyle The new Style to use.
	 */
	public void setElementStyle(ElementShape newStyle) {
		if(newStyle != this.elementStyle){
			this.elementStyle = newStyle;
			init();
		}
	}
}