package contract.datastructure;

public enum VisualType {
	/*
	 * Chart type.
	 */
	bar("Bar Chart", "bar"),
	/*
	 * Graph type.
	 */
	tree("KTree", "tree", true),
	/*
	 * Grid type.
	 */
	grid("Grid", "grid", true),
	box(grid, "box"),
	/*
	 * Single element render.
	 */
	single("Single Element", "single");

	public transient final String pretty;
	public transient final String json;
	public transient final boolean has_options;
	public transient final boolean has_clones;

	private VisualType(String pretty, String json, boolean has_options) {
		this.pretty = pretty;
		this.json = json;
		this.has_options = has_options;
		has_clones = false;
	}
	
	/**
	 * Constructor for enums which dont have their own render.
	 * @param original The original VisualType.
	 * @param json The json string for this VisualType.
	 */
	private VisualType(VisualType original, String json){
		this.pretty = original.pretty;
		this.json = original.json;
		this.has_options = original.has_options;
		has_clones = true;
	}

	private VisualType(String pretty, String json) {
		this(pretty, json, false);
	}

	/**
	 * Returns the VisualType corresponding to the stylish name (VisualType.
	 * <type>.name).
	 * 
	 * @param pretty
	 *            The name to resolve.
	 * @return A VisualType, or null if the stylish name was unknown.
	 */
	public static VisualType resolveVisualType(String pretty) {
		for (VisualType vt : values()) {
			if (vt.pretty.equals(pretty)) {
				return vt;
			}
		}
		return null;
	}

	public String toString() {
		return this.pretty;
	}

	/**
	 * Returns the VisualType corresponding to the json name.
	 * 
	 * @param json
	 *            The name to resolve.
	 * @return A VisualType, or null if the stylish name was unknown.
	 */
	public static VisualType fromString(String json) {
		for (VisualType vt : values()) {
			if (vt.json.equals(json)) {
				return vt;
			}
		}
		return null;
	}
}