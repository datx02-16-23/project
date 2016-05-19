package assets;

import javafx.scene.layout.Background;
import javafx.scene.layout.Border;

/**
 * Constant container class.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class DasConstants {
	private DasConstants() {
	} // Not to be instantiated.

	/*
	 * Misc strings and stuff
	 */
	public static final String PROJECT_NAME = "Lorem Namnsum";
	public static final String PROJECT_SLOGAN = "VAD E VAD FO NOGE?";
	public static final String PROPERTIES_FILE_NAME = "config.properties";
	public static final String DEFAULT_CHANNEL = "mavser_stream";
	// Credits
	public static final String[] DEVELOPER_NAMES = { "Johan GERDIN", "Ivar \"Cannonbait\" JOSEFSSON", "Dennis JONSSON",
			"Simon SMITH", "Richard \"Whisp\" SUNDQVIST" };
	public static final long VERSION_NUMBER = 2;

	/*
	 * 
	 * Render constants
	 * 
	 */

	/**
	 * The value for renders which prefer a fixed width.
	 */
	public static final int DEFAULT_RENDER_WIDTH = 400;
	/**
	 * The value for renders which prefer a fixed height.
	 */
	public static final int DEFAULT_RENDER_HEIGHT = 250;

	public static final double DEFAULT_ELEMENT_WIDTH = 60;
	public static final double DEFAULT_ELEMENT_HEIGHT = 40;
	public static final double DEFAULT_ELEMENT_HSPACE = 0;
	public static final double DEFAULT_ELEMENT_VSPACE = 0;

	/**
	 * Suggested minimum margin for renders.
	 */
	public static final double RENDER_PADDING = 10;

	// More render constants
	public static final Background ARRAY_BACKGROUND = DasToolkit.createArrayBg();
	public static final Background ORPHAN_BACKGROUND = DasToolkit.createOrphanBg();
	public static final Background TREE_BACKGROUND = DasToolkit.createTreeBg();
	public static final Border BORDER_MOUSEOVER = DasToolkit.getMOBorder();
	public static final String RENDER_FXML_URL = "/render/RenderBase.fxml";
}
