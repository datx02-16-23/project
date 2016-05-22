package assets;

import javafx.scene.layout.Background;
import javafx.scene.layout.Border;

/**
 * Constant container class.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class Const {
	private Const() {
	} // Not to be instantiated.

	/*
	 * Misc strings and stuff
	 */
	public static final String CONTRACT_NAME = "Lorem Namnsum";
	public static final String PROGRAM_NAME = CONTRACT_NAME + ": JavaFX Desktop Visualisation";
	public static final String PROJECT_SLOGAN = "VAD E VAD FO NOGE?";
	public static final String PROPERTIES_FILE_NAME = "config.properties";
	public static final String DEFAULT_CHANNEL = "mavser_stream";
	// Credits
	public static final String[] DEVELOPER_NAMES = { "Johan GERDIN", "Ivar \"Cannonbait\" JOSEFSSON", "Dennis JONSSON",
			"Simon SMITH", "Richard \"Whisp\" SUNDQVIST" };
	public static final long VERSION_NUMBER = Long.MAX_VALUE;

	/*
	 * 
	 * Render constants
	 * 
	 */

	/**
	 * Suggested value for renders which prefer a fixed width.
	 */
	public static final int RENDER_WIDTH = 400;
	/**
	 * Suggested value for renders which prefer a fixed height.
	 */
//	public static final int RENDER_HEIGHT = 250;
	public static final int RENDER_HEIGHT = 450;

	// More suggested values
	public static final double ELEMENT_SIZE = 50;
	public static final double ELEMENT_WIDTH = ELEMENT_SIZE;
	public static final double ELEMENT_HEIGHT = ELEMENT_SIZE;
	public static final double ELEMENT_HSPACE = 2;
	public static final double ELEMENT_VSPACE = 2;

	public static final double ELEMENT_SIZE_DELTA = 2;
	public static final double ELEMENT_WIDTH_DELTA = ELEMENT_SIZE_DELTA;
	public static final double ELEMENT_HEIGHT_DELTA = ELEMENT_SIZE_DELTA;
	public static final double ELEMENT_HSPACE_DELTA = 0.3;
	public static final double ELEMENT_VSPACE_DELTA = 0.3;

	/**
	 * Suggested minimum margin for renders.
	 */
	public static final double RENDER_PADDING = 10;

	// More render constants
	public static final Background ARRAY_BACKGROUND = Tools.createArrayBg();
	public static final Background ORPHAN_BACKGROUND = Tools.createOrphanBg();
	public static final Background TREE_BACKGROUND = Tools.createTreeBg();
	public static final Border BORDER_MOUSEOVER = Tools.getMOBorder();
}
