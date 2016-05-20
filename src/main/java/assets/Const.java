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
	public static final long VERSION_NUMBER = -1;

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
	public static final int RENDER_HEIGHT = 250;

	// More suggested values
	public static final double ELEMENT_SIZE = 40;
	public static final double ELEMENT_WIDTH = ELEMENT_SIZE;
	public static final double ELEMENT_HEIGHT = ELEMENT_SIZE;
	public static final double ELEMENT_HSPACE = 0;
	public static final double ELEMENT_VSPACE = 0;

	/**
	 * Suggested minimum margin for renders.
	 */
	public static final double RENDER_PADDING = 10;

	// More render constants
	public static final Background ARRAY_BACKGROUND = Tools.createArrayBg();
	public static final Background ORPHAN_BACKGROUND = Tools.createOrphanBg();
	public static final Background TREE_BACKGROUND = Tools.createTreeBg();
	public static final Border BORDER_MOUSEOVER = Tools.getMOBorder();
	public static final String RENDER_FXML_URL = "/render/RenderBase.fxml";
}
