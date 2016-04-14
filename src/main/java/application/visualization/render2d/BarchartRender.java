package application.visualization.render2d;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.Pane;
import wrapper.datastructures.Array;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

/**
 * Created by cb on 14/04/16.
 */
public class BarchartRender extends Pane {
    private final DataStructure struct;
    private final BarChart<String, Number> bc;
    private final XYChart.Series elemData;

    public BarchartRender(DataStructure struct) {

        this.struct = struct;
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        bc = new BarChart<>(xAxis,yAxis);
        bc.setTitle(struct.identifier);
        xAxis.setLabel("Index");
        yAxis.setLabel("Value");

        elemData = new XYChart.Series();
        ObservableList data = elemData.getData();
        for (Element element:struct.getElements()){
            Array.ArrayElement e = (Array.ArrayElement)element;
            data.add(new XYChart.Data(e.getIndex()[0] + "", e.getValue()));
        }

        bc.getData().add(elemData);


        this.getChildren().add(bc);
    }


    public void render(){
    }


}
