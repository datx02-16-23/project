package application.visualization.render2d;

import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.GridPane;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

public class BoxRender extends Render {

    private final GridPane      grid;
    private static final String DEFAULT_COLOR = "#123456";
    private final DataStructure struct;

    public BoxRender (DataStructure struct){
        grid = new GridPane();
        this.struct = struct;
        init();
    }

    /**
     * Render only the necessary elements.
     */
    @Override
    public void render (){
        GridElement ge;
        for(Element e : struct.getElements()){
            
        }
    }
    
    /**
     * Create and render elements.
     */
    private void init (){
        grid.getChildren().clear();
        
    }
    
    /**
     * The actual drawing surface for the elements.
     * @author Richard
     *
     */
    private class GridElement{
        
    }
}
