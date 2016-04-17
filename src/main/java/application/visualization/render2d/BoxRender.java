package application.visualization.render2d;

import java.util.List;

import application.gui.Main;

import java.util.Arrays;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import wrapper.datastructures.Array.ArrayElement;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.*;

public class BoxRender extends Render {

    private static final double GRID_SIZE              = 50;
    private final GridPane      grid;
    private static final String DEFAULT_COLOR          = "white";
    private final DataStructure struct;
    private int                 elementsPreviousRender = 0;

    public BoxRender (DataStructure struct){
        this.struct = struct;
        grid = new GridPane();
        BorderPane bp = new BorderPane();
        //Struct name
        if (struct.rawType.equals("independentElement")) {
            bp.setTop(new Label("identifier: " + struct.identifier));
        }
        else {
            bp.setTop(new Label("\tidentifier: " + struct.identifier));
        }
        //Build
        bp.setCenter(grid);
        this.getChildren().add(bp);
        init();
        this.setMinSize(200, 100);
        this.setMaxSize(200, 100);
    }

    /**
     * Render only the necessary elements.
     */
    @Override
    public void render (){
        List<Element> structElements = struct.getElements();
        if (structElements.size() != elementsPreviousRender) {
            init();
            elementsPreviousRender = structElements.size();
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
    }

    /**
     * Create and render all elements.
     */
    private void init (){
        grid.getChildren().clear();
        for (Element e : struct.getElements()) {
            addElementToGrid(e, null);
        }
        struct.elementsDrawn();
        calculateSize();
    }

    /**
     * Recalculate size.
     */
    private void calculateSize (){
        int elems = struct.getElements().size();
        double width = GRID_SIZE * elems + 20;
        double height = GRID_SIZE * 2 + 20;
        this.setMinSize(width, height);
        this.setMaxSize(width, height);
    }

    //Ugly way of doing it, but I cant be bothered checking if the element moved.
    private void addElementToGrid (Element e, String style){
        ArrayElement ae = (ArrayElement) e;
        int[] index = ae.getIndex();
        if (index == null) { //Assume IndependentElement
            grid.add(new Label(), 0, 0);
            grid.add(new GridElement(e, style), 0, 1);
        }
        else if (index.length == 1) {
            grid.add(new Label("  " + Arrays.toString(index)), index[0], 0);
            grid.add(new GridElement(e, style), index[0], 1);
        }
        else if (index.length == 2) {
            grid.add(new Label("  " + Arrays.toString(index)), index[0], index[1] + 0);
            grid.add(new GridElement(e, style), index[0], index[1] + 1);
        }
        else {
            Main.console.err("ERROR: BoxRender cannot draw more than 2 dimensions.");
        }
    }

    /**
     * The actual drawing surface for the elements.
     * 
     * @author Richard Sundqvist
     *
     */
    private class GridElement extends StackPane {

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
            Label label = new Label(e.getValue() + "");
            this.getChildren().add(label);
            StackPane.setAlignment(label, Pos.CENTER);
        }
    }
}
