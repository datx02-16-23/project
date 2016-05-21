package render;

import contract.datastructure.DataStructure;

public abstract class ARenderFactory {

	private ARenderFactory() {
	} // Not to be instantiated.

	/**
	 * Determines and creates a Render for use by this DataStructure. Some
	 * parameters may be ignored, depending on the render desired by
	 * {@code struct}.
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
	//@formatter:off
	public static ARender resolveRender(DataStructure struct,
			double elemWidth, double elemHeight,
			double renderWidth, double renderHeight)
	{
	//@formatter:off
		
		ARender curRender = null;

		switch (struct.visual) {
		case bar:
			curRender = new BarchartRender(struct, elemWidth, renderHeight, 20, 10);
			break;
		case grid:
		case box:
			curRender = new GridRender(struct, GridRender.Order.COLUMN_MAJOR, elemWidth,
					elemHeight, 3, 3);
			break;
		case tree:
			curRender = new KTreeRender(struct, 2, elemWidth, elemHeight, 5, 5);
			break;
		case single:
			curRender = new SingleElementRender(struct, elemWidth, elemHeight);
			break;
		}

		return curRender;
	}
}
