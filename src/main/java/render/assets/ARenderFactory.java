package render.assets;

import contract.datastructure.DataStructure;
import render.ARender;
import render.BarchartRender;
import render.GridRender;
import render.KTreeRender;
import render.SingleElementRender;

public abstract class ARenderFactory {

    private ARenderFactory () {
    } // Not to be instantiated.

    /**
     * Determines and creates a Render for use by this DataStructure. Some parameters may be
     * ignored, depending on the render desired by {@code struct}.
     *
     * @param struct
     *            The DataStructure to create a Render for.
     *
     * @param elemWidth
     *            Width for the elements.
     * @param elemHeight
     *            Height for the elements.
     * @param renderWidth
     *            Width for the render.
     * @param renderHeight
     *            Height for the render.
     * @return An ARender attached for {@code struct}.
     */
    // @formatter:off
    public static ARender resolveRender(DataStructure struct, double elemWidth, double elemHeight, double renderWidth,
	    double renderHeight) {
	// @formatter:off

	ARender curRender = null;

	switch (struct.visual) {
	case barchart:
	case bar:
	    curRender = new BarchartRender(struct, elemWidth * 0.9, -1, 20, 3);
	    break;
	case matrix:
	case grid:
	case box:
	    curRender = new GridRender(struct, GridRender.Order.ROW_MAJOR, elemWidth, elemHeight, 3, 3);
	    break;
	case binaryTree:
	case tree:
	    curRender = new KTreeRender(struct, 2, elemWidth, elemHeight, 5, 5);
	    break;
	case element:
	case single:
	    curRender = new SingleElementRender(struct, elemWidth * 2, elemHeight);
	    break;
	}

	return curRender;
    }
}
