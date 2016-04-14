package application.visualization.render2d;

import java.util.List;

import application.gui.Main;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import wrapper.datastructures.Array;
import wrapper.datastructures.Array.ArrayElement;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

/**
 * Created by cb on 14/04/16.
 */
public class BarchartRender extends Render {

    private final DataStructure                        struct;
    private final BarChart<String, Double>             bc;
    private final ObservableList<Data<String, Double>> elemData;

    public BarchartRender (DataStructure struct){
        this.struct = struct;
        this.setPrefSize(2000, 2000);
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        bc = new BarChart(xAxis, yAxis);
        bc.setTitle(struct.identifier);
        xAxis.setLabel("Index");
        yAxis.setLabel("Value");
        Series<String, Double> elemDataSeries = new XYChart.Series<>();
        elemData = elemDataSeries.getData();
        for (Element element : struct.getElements()) {
            Array.ArrayElement e = (Array.ArrayElement) element;
            elemData.add(new Data(e.getIndex()[0] + "", e.getValue()));
        }
        bc.getData().add(elemDataSeries);
        this.getChildren().add(bc);
    }

    @Override
    public void render (){
        List<Element> structElements = struct.getElements();
        int elementsSize = structElements.size();
        if (elemData.size() != elementsSize) { //Difference in size, probably because of an init.
            elemData.clear();
            ArrayElement ae;
            for (Object o : struct.getElements()) {
                ae = (ArrayElement) o;
                elemData.add(new Data(ae.getIndex()[0] + "", ae.getValue()));
            }
        }
        ArrayElement ae;
        Data<String, Double> d;
        for (int i = 0; i < elementsSize; i++) {
            ae = (ArrayElement) structElements.get(i);
            d = elemData.get(i);
            d.setXValue(ae.getIndex()[0] + "");
            d.setYValue(ae.getValue());
        }
    }
}
