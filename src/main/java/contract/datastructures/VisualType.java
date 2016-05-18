package contract.datastructures;

public enum VisualType {
	bar("Bar Chart", "bar"), box("Boxes", "box", true), tree("KTree", "tree", true), single("Single Element", "single");

	public final String pretty;
	public final String json;
	public final boolean has_options;

	private VisualType(String pretty, String json, boolean has_options) {
		this.pretty = pretty;
		this.json = json;
		this.has_options = has_options;
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
