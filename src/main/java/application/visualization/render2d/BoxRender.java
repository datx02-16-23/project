package application.visualization.render2d;

import java.util.List;
import java.util.Arrays;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import wrapper.datastructures.Array.ArrayElement;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.*;

public class BoxRender extends Render {

    public static final double  GRID_SIZE     = 50;
    private final GridPane      grid;
    private static final String DEFAULT_COLOR = "white";
    private final DataStructure struct;

    public BoxRender (DataStructure struct){
        grid = new GridPane();
        this.getChildren().add(grid);
        this.struct = struct;
        this.setVisible(true);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        this.setPrefWidth(Double.MAX_VALUE);
        this.setPrefHeight(Double.MAX_VALUE);
        this.setStyle("-fx-background-color: #2f4f4f;");
        init();
    }

    /**
     * Render only the necessary elements.
     */
    @Override
    public void render (){
        System.out.println("box render()");
        List<Element> structElements = struct.getElements();
        if (structElements.size()*2 != grid.getChildren().size()) { //x2 because grid also has labels.
            System.out.println("init");
            init();
        }
        else {
            System.out.println("no init");
            List<Element> modifiedElements = struct.getModifiedElements();
            List<Element> resetElements = struct.getResetElements();
            System.out.println(modifiedElements);
            System.out.println(resetElements);
            for (Element e : struct.getElements()) {
                if(modifiedElements.contains(e)){
                    addElementToGrid(e, e.getColor());                    
                } else if (resetElements.contains(e)){
                    addElementToGrid(e, null);
                }
            }
            struct.elementsDrawn();
        }
    }

    //Ugly way of doing it, but I cant be bothered checking if the element moved.
    private void addElementToGrid (Element e, String style){
        ArrayElement ae = (ArrayElement) e;
        grid.add(new Label("  " + Arrays.toString(ae.getIndex())), ae.getIndex()[0], 0);
        grid.add(new GridElement(e, style), ae.getIndex()[0], 1);
    }

    /**
     * Create and render all elements.
     */
    private void init (){
        grid.getChildren().clear();
        for (Element e : struct.getElements()) {
            addElementToGrid((ArrayElement) e, null);
        }
    }

    /**
     * The actual drawing surface for the elements.
     * 
     * @author Richard
     *
     */
    private class GridElement extends GridPane {

        private static final String BORDER = "-fx-border-color: black;\n" + "-fx-border-insets: 1;\n" + "-fx-border-width: 2;\n" + " -fx-border-radius: 3;\n";

        private GridElement (Element e, String style){
            if (style == null) {
                System.out.println("no color");
                this.setStyle(BORDER);
            } else {
                System.out.println("color");
                this.setStyle(BORDER + "\n-fx-background-color: " + style + ";");                
            }
            this.setPrefWidth(GRID_SIZE);
            this.setPrefHeight(GRID_SIZE);
            this.setMaxWidth(GRID_SIZE);
            this.setMaxHeight(GRID_SIZE);
            this.getChildren().add(new Label(e.getValue() + ""));
        }
    }
}
