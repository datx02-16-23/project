package application.visualization.render2d;

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
        init(); //TODO
    }

    /**
     * Create and render all elements.
     */
    private void init (){
        grid.getChildren().clear();
        for (Element e : struct.getElements()) {
            ArrayElement ae = (ArrayElement) e;
            grid.add(new Label(ae.getIndex() + ""), ae.getIndex()[0], 0);
            grid.add(new GridElement(e), ae.getIndex()[0], 0);
        }
    }

    /**
     * The actual drawing surface for the elements.
     * 
     * @author Richard
     *
     */
    private class GridElement extends Pane {

        private static final String BORDER = "-fx-border-color: black;\n" + "-fx-border-insets: 5;\n" + "-fx-border-width: 3;\n" + "-fx-border-style: dashed;\n";
        private final Element       e;
        private final Label         valueLabel;

        private GridElement (Element e){
            this.e = e;
            this.setStyle(BORDER);
            this.setPrefWidth(GRID_SIZE);
            this.setPrefHeight(GRID_SIZE);
            this.setMaxWidth(GRID_SIZE);
            this.setMaxHeight(GRID_SIZE);
            valueLabel = new Label(e.getValue()+"");
            valueLabel.setPrefWidth(Double.MAX_VALUE);
            valueLabel.setPrefHeight(Double.MIN_VALUE);
        }
        //TODO: Override draw method.
    }
}
