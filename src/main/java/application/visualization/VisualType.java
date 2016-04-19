package application.visualization;

public enum VisualType{
    bar("Bar Chart"), box("Boxes"), tree("KTree");

    public final String name;

    private VisualType (String name){
        this.name = name;
    }
}
