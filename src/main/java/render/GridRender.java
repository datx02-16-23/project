package render;

import assets.Debug;
import contract.datastructure.Array;
import contract.datastructure.Array.IndexedElement;
import contract.datastructure.DataStructure;
import contract.datastructure.Element;
import gui.Main;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import render.element.AVElement;
import render.element.AVElementFactory;
import render.element.ElementShape;

/**
 * Render drawing data structures with their elements in a grid.
 * 
 * @author Richard Sundqvist
 *
 */
public class GridRender extends ARender {
	
	public static final ElementShape DEFAULT_ELEMENT_STYLE = ElementShape.RECTANGLE;
	
	private final Order majorOrder;
	private int[] dims;

	/**
	 * Creates a new GridRender.
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
	public GridRender(DataStructure struct, Order major_order, double width, double height, double hspace,
			double vspace) {
		super(struct, width, height, hspace, vspace);
		this.majorOrder = major_order;
	}

	public void render() {
		if (struct.repaintAll) {
			struct.repaintAll = false;
			repaintAll();
		}
		super.render();
	}

	@Override
	public boolean repaintAll() {

		if (super.repaintAll() == false) {
			return false; // Nothing to render.
		}

		/*
		 * Max 2 dimensions.
		 */
		IndexedElement ae = (IndexedElement) struct.getElements().get(0);
		int dimensions = ae.getIndex().length;
		if (dimensions != 2 && dimensions != 1) {
			Main.console.force("WARNING: Structure " + struct + " has declared " + dimensions
					+ " dimensions. MatrixRender supports only one or two dimensions.");
		}
		return true;
	}

	@Override
	public double getX(Element e) {
		if (e == null || e instanceof IndexedElement == false) {
			return -1;
		}
		int[] index = ((IndexedElement) e).getIndex();
		double x = hSpace;
		if (majorOrder == Order.ROW_MAJOR) {
			x = getX(index[0]);
		} else {
			if (index.length == 2) {
				x = getY(index[1]);
			}
		}
		return x;
	}

	private double getX(int column) {
		return hSpace + (hSpace + nodeWidth) * column;
	}

	@Override
	public double getY(Element e) {
		if (e == null || e instanceof IndexedElement == false) {
			return -1;
		}

		int[] index = ((IndexedElement) e).getIndex();
		double y = vSpace;
		if (majorOrder == Order.ROW_MAJOR) {
			if (index.length == 2) {
				y = getY(index[1]);
			}
		} else {
			y = getX(index[0]);
		}
		return y;
	}

	private double getY(int row) {
		return vSpace + (vSpace + nodeHeight) * row;
	}

	@Override
	public void calculateSize() {

		ensureDimensionsSet();

		/*
		 * Row Major
		 */
		if (majorOrder == Order.ROW_MAJOR) {
			totWidth = vSpace + (vSpace + nodeWidth) * dims[0];
			totHeight = hSpace + (hSpace + nodeHeight) * dims[1];

			/*
			 * Column Major
			 */
		} else {
			totHeight = hSpace + (hSpace + nodeHeight) * dims[0];
			totWidth = 2 + vSpace + (vSpace + nodeWidth) * dims[1];
		}
		setRestricedSize(totWidth, totHeight);
	}

	private void ensureDimensionsSet() {
		int[] backup = new int[] { struct.getElements().size(), 1 };
		Array array = (Array) struct;

		dims = array.getCapacity();

		if (dims == null || dims.length == 0) {
			dims = backup;
			if(Debug.TRACING){
				System.err.println("Size was null or empty for \"" + struct + "\"!");				
			}
		} else if (dims.length == 1) {
			int[] newSize = { dims[0], 1 };
			dims = newSize; // Add 2nd which is used in size calculation.
		}

		// Else assume dims are okay.
	}

	public static enum Order {
		ROW_MAJOR("Row Major", "The first index will indicate row.", 0), COLUMN_MAJOR("Column Major",
				"The first index will indicate column.", 1);

		public final String name;
		public final String description;
		public final int optionNbr;

		private Order(String name, String description, int optionNbr) {
			this.name = name;
			this.description = description;
			this.optionNbr = optionNbr;
		}

		/**
		 * Returns the Order corresponding to the given option number. Defaults
		 * to ROW_MAJOR for unknown option numbers.
		 * 
		 * @param optionNbr
		 *            The option to resolve an order for.
		 * @return An Order.
		 */
		public static Order resolve(int optionNbr) {
			for (Order o : values()) {
				if (o.optionNbr == optionNbr) {
					return o;
				}
			}
			return ROW_MAJOR;
		}
	}

	@Override
	protected AVElement createVisualElement(Element e) {
		elementStyle = elementStyle == null ? DEFAULT_ELEMENT_STYLE : elementStyle;
		
		AVElement re = AVElementFactory.shape(elementStyle, e, nodeWidth, nodeHeight);
		re.setInfoPos(Pos.BOTTOM_CENTER);
		re.setInfoArray(((IndexedElement) e).getIndex());
		return re;
	}

	@Override
	protected AVElement createVisualElement(double value, Color color) {
		elementStyle = elementStyle == null ? DEFAULT_ELEMENT_STYLE : elementStyle;
		
		AVElement re = AVElementFactory.shape(elementStyle, value, color, nodeWidth, nodeHeight);
		re.setInfoPos(null);
		return re;
	}

	@Override
	protected void bellsAndWhistles(Element e, AVElement ve) {
//		System.out.println("grid: baw shape = " + ve.getShape());
	}
}