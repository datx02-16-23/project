package render;

import assets.Debug;
import assets.Tools;
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

    private final Order              majorOrder;
    private int[]                    dims;

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
    public GridRender (DataStructure struct, Order majorOrder, double width, double height, double hspace,
            double vspace) {
        super(struct, width, height, hspace, vspace);
        this.majorOrder = majorOrder;
        this.setRelativeNodeSize(true, 2);
    }

    @Override public void render () {
        if (this.struct.repaintAll) {
            this.struct.repaintAll = false;
            this.repaintAll();
        }
        super.render();
    }

    @Override public boolean repaintAll () {

        if (super.repaintAll() == false) {
            return false; // Nothing to render.
        }

        /*
         * Max 2 dimensions.
         */
        IndexedElement ae = (IndexedElement) this.struct.getElements().get(0);
        int dimensions = ae.getIndex().length;
        if (dimensions != 2 && dimensions != 1) {
            Main.console.force("WARNING: Structure " + this.struct + " has declared " + dimensions
                    + " dimensions. MatrixRender supports only one or two dimensions.");
        }
        return true;
    }

    @Override public double getX (Element e) {
        if (e == null || e instanceof IndexedElement == false) {
            return -1;
        }
        int[] index = ((IndexedElement) e).getIndex();
        double x = this.hSpace;
        if (this.majorOrder == Order.ROW_MAJOR) {
            x = this.getX(index [0]);
        } else {
            if (index.length == 2) {
                x = this.getY(index [1]);
            }
        }
        return x + Tools.getAdjustedX(this, e);
    }

    private double getX (int column) {
        return this.hSpace + (this.hSpace + this.nodeWidth) * column;
    }

    @Override public double getY (Element e) {
        if (e == null || e instanceof IndexedElement == false) {
            return -1;
        }

        int[] index = ((IndexedElement) e).getIndex();
        double y = this.vSpace;
        if (this.majorOrder == Order.ROW_MAJOR) {
            if (index.length == 2) {
                y = this.getY(index [1]);
            }
        } else {
            y = this.getX(index [0]);
        }
        return y + Tools.getAdjustedY(this, e);
    }

    private double getY (int row) {
        return this.vSpace + (this.vSpace + this.nodeHeight) * row;
    }

    @Override public void calculateSize () {

        this.ensureDimensionsSet();

        /*
         * Row Major
         */
        if (this.majorOrder == Order.ROW_MAJOR) {
            this.renderWidth = this.vSpace + (this.vSpace + this.nodeWidth) * this.dims [0];
            this.renderHeight = this.hSpace + (this.hSpace + this.nodeHeight) * this.dims [1];

            /*
             * Column Major
             */
        } else {
            this.renderHeight = this.hSpace + (this.hSpace + this.nodeHeight) * this.dims [0];
            this.renderWidth = 2 + this.vSpace + (this.vSpace + this.nodeWidth) * this.dims [1];
        }
        this.setRestricedSize(this.renderWidth, this.renderHeight);
    }

    private void ensureDimensionsSet () {
        int[] backup = new int[] { this.struct.getElements().size(), 1 };
        Array array = (Array) this.struct;

        this.dims = array.getCapacity();

        if (this.dims == null || this.dims.length == 0) {
            this.dims = backup;
            if (Debug.ERR) {
                System.err.println("Size was null or empty for \"" + this.struct + "\"!");
            }
        } else if (this.dims.length == 1) {
            int[] newSize = { this.dims [0], 1 };
            this.dims = newSize; // Add 2nd which is used in size calculation.
        }

        // Else assume dims are okay.
    }

    public static enum Order {
        ROW_MAJOR("Row Major", "The first index will indicate row.", 0), COLUMN_MAJOR("Column Major",
                "The first index will indicate column.", 1);

        public final String name;
        public final String description;
        public final int    optionNbr;

        private Order (String name, String description, int optionNbr) {
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
        public static Order resolve (int optionNbr) {
            for (Order o : values()) {
                if (o.optionNbr == optionNbr) {
                    return o;
                }
            }
            return ROW_MAJOR;
        }
    }

    @Override protected AVElement createVisualElement (Element e) {
        this.elementStyle = this.elementStyle == null ? DEFAULT_ELEMENT_STYLE : this.elementStyle;

        AVElement re = AVElementFactory.shape(this.elementStyle, e, this.nodeWidth, this.nodeHeight);
        re.setInfoPos(Pos.BOTTOM_CENTER);
        re.setInfoArray(((IndexedElement) e).getIndex());
        return re;
    }

    @Override protected AVElement createVisualElement (double value, Color color) {
        this.elementStyle = this.elementStyle == null ? DEFAULT_ELEMENT_STYLE : this.elementStyle;

        AVElement re = AVElementFactory.shape(this.elementStyle, value, color, this.nodeWidth, this.nodeHeight);
        re.setInfoPos(null);
        return re;
    }

    @Override protected void bellsAndWhistles (Element e, AVElement ve) {
        // System.out.println("grid: baw shape = " + ve.getShape());
    }
}
