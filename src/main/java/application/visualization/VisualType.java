package application.visualization;

public enum VisualType{
    bar("Bar Chart"), box("Boxes", true), tree("KTree", true);

    public final String  name;
    public final boolean has_options;

    private VisualType (String name, boolean has_options){
        this.name = name;
        this.has_options = has_options;
    }

    private VisualType (String name){
        this(name, false);
    }

    /**
     * Returns the VisualType corresponding to the stylish name (VisualType.<type>.name).
     * 
     * @param stylish_name The name to resolve.
     * @return A VisualType, or null if the stylish name was unknown.
     */
    public static VisualType resolveVisualType (String stylish_name){
        for (VisualType vt : values()) {
            if (vt.name.equals(stylish_name)) {
                return vt;
            }
        }
        return null;
    }

    public String toString (){
        return this.name;
    }
}
