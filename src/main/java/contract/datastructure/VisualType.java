package contract.datastructure;

public enum VisualType {
    /**
     * Chart type.
     */
    bar("Bar Chart", "bar"), barchart(bar, "bar chart", "barchart"),
    /**
     * Special Graph type.
     */
    tree("K-Tree", "tree"), binaryTree(tree, "binary tree", "binarytree", "bintree"),
    /**
     * Grid type.
     */
    grid("Grid", "grid"), box(grid, "box"), matrix(grid, "matrix"),
    /**
     * Single element type.
     */
    single("Single Element", "single"), element(single, "element", "independentElement");

    public transient final String   pretty;
    public transient final String[] json;
    public transient final boolean  isClone;

    private VisualType (String pretty, String... json) {
        this.pretty = pretty;
        this.json = json;
        isClone = false;
    }

    /**
     * Constructor for enums which dont have their own render.
     *
     * @param original
     *            The original VisualType.
     * @param json
     *            The json string for this VisualType.
     */
    private VisualType (VisualType original, String... json) {
        pretty = original.pretty;
        this.json = json;
        isClone = true;
    }

    /**
     * Returns the VisualType corresponding to the stylish name (VisualType.
     * <type>.name).
     *
     * @param pretty
     *            The name to resolve.
     * @return A VisualType, or null if the stylish name was unknown.
     */
    public static VisualType resolveVisualType (String pretty) {
        for (VisualType vt : values()) {
            if (vt.pretty.equals(pretty)) {
                return vt;
            }
        }
        return null;
    }

    @Override public String toString () {
        return pretty;
    }

    /**
     * Returns the VisualType corresponding to the json name.
     *
     * @param json
     *            The name to resolve.
     * @return A VisualType, or null if the stylish name was unknown.
     */
    public static VisualType fromString (String json) {
        for (VisualType vt : values()) {
            if (vt.json.equals(json)) {
                return vt;
            }
        }
        return null;
    }
}
