package application.visualization.render2d;

import java.util.List;

import application.gui.Main;

import java.util.Arrays;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import wrapper.datastructures.Array.ArrayElement;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.*;

public class BoxRender extends Render {

    public static final double  GRID_SIZE              = 50;
    private final GridPane      grid;
    private static final String DEFAULT_COLOR          = "white";
    private final DataStructure struct;
    private int                 elementsPreviousRender = 0;

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
        List<Element> structElements = struct.getElements();
        if (structElements.size() != elementsPreviousRender) {
            init();
        }
        else {
            List<Element> modifiedElements = struct.getModifiedElements();
            List<Element> resetElements = struct.getResetElements();
            for (Element e : struct.getElements()) {
                if (modifiedElements.contains(e)) {
                    addElementToGrid(e, e.getColor());
                }
                else if (resetElements.contains(e)) {
                    addElementToGrid(e, null);
                }
            }
            struct.elementsDrawn();
        }
        elementsPreviousRender = structElements.size();
    }

    /**
     * Create and render all elements.
     */
    private void init (){
        System.out.println("\tinit");
        grid.getChildren().clear();
        for (Element e : struct.getElements()) {
            addElementToGrid((ArrayElement) e, null);
        }
        elementsPreviousRender = struct.getElements().size();
        struct.elementsDrawn();
    }

    //Ugly way of doing it, but I cant be bothered checking if the element moved.
    private void addElementToGrid (Element e, String style){
        ArrayElement ae = (ArrayElement) e;
        int[] index = ae.getIndex();
        System.out.println(Arrays.toString(index));
        if(index.length == 1){
            System.out.println("1d");
            grid.add(new Label("  " + Arrays.toString(index)), index[0], 0);
            grid.add(new GridElement(e, style), index[0], 1);            
        } else if (index.length == 2){
            System.out.println("2d");
            grid.add(new Label("  " + Arrays.toString(index)), index[0], index[1] + 0);
            grid.add(new GridElement(e, style), index[0], index[1] + 1);       
        } else {
            Main.console.err("ERROR: BoxRender cannot draw more than 2 dimensions.");
        }
    }

    /**
     * The actual drawing surface for the elements.
     * 
     * @author Richard
     *
     */
    private class GridElement extends GridPane {

        private static final String BASE = "-fx-border-color: black;\n" + "-fx-border-insets: 1;\n" + "-fx-border-width: 2;\n" + "-fx-border-radius: 3;\n" + "-fx-background-radius: 5;";

        private GridElement (Element e, String style){
            if (style == null) {
                this.setStyle(BASE + "\n-fx-background-color: white ;");
            }
            else {
                this.setStyle(BASE + "\n-fx-background-color: " + style + ";");
            }
            this.setPrefWidth(GRID_SIZE);
            this.setPrefHeight(GRID_SIZE);
            this.setMaxWidth(GRID_SIZE);
            this.setMaxHeight(GRID_SIZE);
            this.getChildren().add(new Label(e.getValue() + ""));
        }
    }
}
