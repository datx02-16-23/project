package application.visualization.render2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import application.gui.GUI_Controller;
import application.visualization.Visualization;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.SpinnerValueFactory;
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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

public abstract class Render extends StackPane {

	private static final Background ARRAY_BACKGROUND = getArrayBg();
	private static final Background ORPHAN_BACKGROUND = getOrphanBg();
	private static final Background TREE_BACKGROUND = getTreeBg();
	private static final Border BORDER_MOUSEOVER = new Border(new BorderStroke(Color.web("#123456"),
			BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(3), new Insets(-5)));
	public static final double DEFAULT_SIZE = 40;
	protected final double node_width, node_height;
	protected final double hspace, vspace;
	protected double WIDTH, HEIGHT;
	protected final DataStructure struct;
	protected final Canvas local_canvas = new Canvas();
	protected static final Canvas SHARED_ANIMATED = Visualization.instance().ANIMATED;
	protected static final Color COLOR_WHITE = Color.WHITE;
	protected static final Color COLOR_BLACK = Color.BLACK;

	/**
	 * 
	 * @param struct
	 * @param width
	 * @param height
	 * @param hspace
	 * @param vspace
	 */
	public Render(DataStructure struct, double width, double height, double hspace, double vspace) {
		this.struct = struct;
		// Sizing and spacing
		this.node_width = width;
		this.node_height = height;
		this.hspace = hspace;
		this.vspace = vspace;
		local_canvas.widthProperty().bind(this.widthProperty());
		local_canvas.heightProperty().bind(this.heightProperty());
		// this.setMinSize(150, 150);
		// this.setPrefSize(150, 150);
		// this.setMaxSize(150, 150);
		// this.setWidth(150);
		// this.setHeight(150);
		// this.setSize(150, 150);
		setSize(150, 150);
		this.setBackground(getStructBackground(struct));
		// Add stacked canvases
		this.getChildren().add(local_canvas);
		initDragAndZoom();
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

	/**
	 * Calls setMinSize, setPrefSize, setMaxSize, setWidth and setHeight
	 * 
	 * @param width
	 *            The width of this Render.
	 * @param height
	 *            The height of this Render.
	 */
	protected void setSize(double width, double height) {
		this.setMinSize(width, height);
		this.setPrefSize(width, height);
		this.setMaxSize(width, height);
		this.setWidth(width);
		this.setHeight(height);
	}

	/**
	 * Order the render to draw the elements of the Data Structure it carries.
	 */
	public abstract void render();

	/**
	 * Draw an element using the animation canvas.
	 * 
	 * @param e
	 *            The element to draw.
	 * @param x
	 *            The absolute x-coordinate.
	 * @param y
	 *            The absolute y-coordinate.
	 * @param color
	 *            The style to use (null = default)
	 */
	public abstract void drawAnimatedElement(Element e, double x, double y, Color color);

	/**
	 * Clears an element from the Canvas. It may be necessary to shadow this
	 * method if it doesn't work properly.
	 * 
	 * @param e
	 *            The element to clear.
	 * @param x
	 *            The x-coordinate to clear.
	 * @param y
	 *            The y-coordinate to clear.
	 */
	public void clearAnimatedElement(Element e, double x, double y) {
		SHARED_ANIMATED.getGraphicsContext2D().clearRect(x - 2, y - 2, node_width + 4, node_height + 4);
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
	 * Order the Render to calculate it's size. Should be shadowed by inheriting
	 * types.
	 */
	public void calculateSize() {
		local_canvas.getGraphicsContext2D().clearRect(0, 0, WIDTH, HEIGHT);
	}

	// Drag and Zoom
	private double transX, transY;
	private double scale = 1;
	private int sign = 1;

	/**
	 * Create listeners to drag and zoom.
	 */
	private void initDragAndZoom() {
		/*
		 * Zoom
		 */
		this.setOnScroll(event -> {
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
		this.setOnMousePressed(event -> {
			transX = this.getTranslateX() - event.getSceneX();
			transY = this.getTranslateY() - event.getSceneY();
			this.setCursor(Cursor.MOVE);
		});
		// Restore cursor
		this.setOnMouseReleased(event -> {
			this.setCursor(Cursor.HAND);
		});
		// Translate canvases
		this.setOnMouseDragged(event -> {
			this.setTranslateX(event.getSceneX() + transX);
			this.setTranslateY(event.getSceneY() + transY);
		});
		// Set cursor
		this.setOnMouseEntered(event -> {
			this.setCursor(Cursor.HAND);
			this.setBorder(BORDER_MOUSEOVER);
		});
		this.setOnMouseExited(event -> {
			this.setCursor(null);
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
	public abstract void startAnimation(Element e, double start_x, double start_y, double end_x, double end_y);

	/**
	 * Returns the absolute x-coordinate for the element e.
	 * 
	 * @param owner
	 *            The owner of the element.
	 * @param e
	 *            An element in owner.
	 * @return The absolute x-coordinates of e.
	 */
	public static double absX(Render owner, Element e) {
		return owner.absX(e);
	}

	/**
	 * Returns the absolute x-coordinate for the element e.
	 * 
	 * @param e
	 *            An element owned by this Render.
	 * @return The absolute x-coordinates of e.
	 */
	public double absX(Element e) {
		double bx = this.getTranslateX() + this.getLayoutX();
		return this.getX(e) + bx;
	}

	/**
	 * Returns the absolute y-coordinate for the element e.
	 * 
	 * @param owner
	 *            The owner of the element.
	 * @param e
	 *            An element in owner.
	 * @return The absolute y-coordinates of e.
	 */
	public static double absY(Render owner, Element e) {
		return owner.absY(e);
	}

	/**
	 * Returns the absolute y-coordinate for the element e.
	 * 
	 * @param e
	 *            An element owned by this Render.
	 * @return The absolute y-coordinates of e.
	 */
	public double absY(Element e) {
		double by = this.getTranslateY() + this.getLayoutY();
		return this.getY(e) + by;
	}

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
	 * Draw an element to the canvas.
	 * 
	 * @param e
	 *            The element to draw.
	 * @param style
	 *            The style to use.
	 * @param canvas
	 *            The Canvas to draw on.
	 */
	public abstract void drawElement(Element e, Color style, Canvas canvas);

	/**
	 * Erase an element from the given Canvas.
	 * 
	 * @param e
	 *            The elements to erase.
	 * @param canvas
	 *            The canvas to erase the element from.
	 */
	public abstract void clearElement(Element e, Canvas canvas);

	/**
	 * Indicates that an element has finished animating. The default
	 * implementation of this method does nothing.
	 * 
	 * @param e
	 *            The element which finished animating.
	 */
	public void finishAnimation(Element e) {
		//Do nothing.
	}

	/**
	 * SpinnerValueFactory for Render implementations.
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

	private static Background getArrayBg() {
		return new Background(new BackgroundImage(
				new Image(GUI_Controller.class.getResourceAsStream("/assets/array.png")), BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
	}

	private static Background getOrphanBg() {
		return new Background(new BackgroundImage(
				new Image(GUI_Controller.class.getResourceAsStream("/assets/orphan2.png")), BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
	}

	private static Background getTreeBg() {
		return new Background(new BackgroundImage(
				new Image(GUI_Controller.class.getResourceAsStream("/assets/tree.png")), BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
	}

}
