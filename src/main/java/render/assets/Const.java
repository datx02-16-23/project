package render.assets;

import assets.Tools;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;

public class Const {

    /**
     * Suggested value for renders which prefer a fixed width.
     */
    public static final int        DEFAULT_RENDER_WIDTH         = 400;
    /**
     * Suggested value for renders which prefer a fixed height.
     */
    public static final int        DEFAULT_RENDER_HEIGHT        = 400;
    public static final double     DEFAULT_RENDER_PADDING       = 5;
    public static final long       DEFAULT_ANIMATION_TIME       = 750;
    public static final double     DEFAULT_RELATIVE_NODE_FACTOR = 2;
    public static final double     DEFAULT_ELEMENT_SIZE         = 50;
    public static final double     DEFAULT_ELEMENT_WIDTH        = DEFAULT_ELEMENT_SIZE;
    public static final double     DEFAULT_ELEMENT_HEIGHT       = DEFAULT_ELEMENT_SIZE;
    public static final double     DEFAULT_ELEMENT_HSPACE       = 2;
    public static final double     DEFAULT_ELEMENT_VSPACE       = 2;
    public static final double     DEFAULT_ELEMENT_SIZE_DELTA   = 2;
    public static final double     DEFAULT_ELEMENT_WIDTH_DELTA  = DEFAULT_ELEMENT_SIZE_DELTA;
    public static final double     DEFAULT_ELEMENT_HEIGHT_DELTA = DEFAULT_ELEMENT_SIZE_DELTA;
    public static final double     DEFAULT_ELEMENT_HSPACE_DELTA = 0.3;
    public static final double     DEFAULT_ELEMENT_VSPACE_DELTA = 0.3;
    public static final double     MIN_NODE_HEIGHT              = 0.001;
    public static final double     MIN_NODE_WIDTH               = 5;
    // More render constants
    public static final Background ARRAY_BACKGROUND             = Tools.createArrayBg();
    public static final Background ORPHAN_BACKGROUND            = Tools.createOrphanBg();
    public static final Background TREE_BACKGROUND              = Tools.createTreeBg();
    public static final Border     BORDER_MOUSEOVER             = Tools.getMOBorder();

}
