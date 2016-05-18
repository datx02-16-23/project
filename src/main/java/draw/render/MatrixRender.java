package draw.render;

import contract.datastructures.Array;
import contract.datastructures.DataStructure;
import contract.datastructures.Element;
import contract.datastructures.Array.IndexedElement;
import draw.render.elements.ElementShape;
import draw.render.elements.VisElemFact;
import draw.render.elements.VisualElement;
import gui.Main;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;

public class MatrixRender extends Render {

	private final Order mo;
	private int[] size;

	/**
	 * 
	 * @param struct
	 * @param width
	 * @param height
	 * @param hspace
	 * @param vspace
	 */
	public MatrixRender(DataStructure struct, Order mo, double width, double height, double hspace, double vspace) {
		super(struct, width, height, hspace, vspace);
		this.mo = mo;
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
		int[] index = ((IndexedElement) e).getIndex();
		double x = hspace;
		if (mo == Order.ROW_MAJOR) {
			x = getX(index[0]);
		} else {
			if (index.length == 2) {
				x = getY(index[1]);
			}
		}
		return x;
	}

	private double getX(int column) {
		return hspace + (hspace + node_width) * column;
	}

	@Override
	public double getY(Element e) {
		int[] index = ((IndexedElement) e).getIndex();
		double y = vspace;
		if (mo == Order.ROW_MAJOR) {
			if (index.length == 2) {
				y = getY(index[1]);
			}
		} else {
			y = getX(index[0]);
		}
		return y;
	}

	private double getY(int row) {
		return vspace + (vspace + node_height) * row;
	}

	@Override
	public void calculateSize() {
		Array array = (Array) struct;
		size = array.getCapacity() == null ? new int[] { struct.getElements().size(), 1 } : array.getCapacity();

		if (mo == Order.ROW_MAJOR) {
			width = vspace + (vspace + node_width) * size[0];
			if (size.length == 2) {
				height = hspace + (hspace + node_height) * size[1];
			} else {
				height = hspace + (hspace + node_height) * 1;
			}
		} else {
			height = hspace + (hspace + node_height) * size[0];
			if (size.length == 2) {
				width = 2 + vspace + (vspace + node_width) * size[1];
			} else {
				width = 2 + vspace + (vspace + node_width) * 1;
			}
		}
		setSize(width, height);
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
	protected VisualElement createVisualElement(Element e) {
		VisualElement re = VisElemFact.create(ElementShape.RECTANGLE, e, node_width, node_height);
		re.setInfoPos(Pos.BOTTOM_CENTER);
		re.setInfoArray(((IndexedElement) e).getIndex());
		return re;
	}

	@Override
	protected VisualElement createVisualElement(double value, Color color) {
		VisualElement re = VisElemFact.create(ElementShape.RECTANGLE, value, color, node_width, node_height);
		re.setInfoPos(null);
		return re;
	}

	@Override
	protected void bellsAndWhistles(Element e, VisualElement ve) {
	}
}
