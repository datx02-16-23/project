package application.visualization.render2d;

import java.util.List;

import application.gui.Main;
import javafx.collections.ObservableList;
import javafx.scene.Node;
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
    private final BarChart<String, Double>             barChart;
    private final ObservableList<Data<String, Double>> elemData;

    public BarchartRender (DataStructure struct){
        this.struct = struct;
        this.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setTitle(struct.identifier);
        xAxis.setLabel("Index");
        yAxis.setLabel("Value");
        Series<String, Double> elemDataSeries = new XYChart.Series<>();
        elemData = elemDataSeries.getData();
        for (Element element : struct.getElements()) {
            Array.ArrayElement e = (Array.ArrayElement) element;
            elemData.add(new Data(e.getIndex()[0] + "", e.getValue()));
        }
        barChart.getData().add(elemDataSeries);
        this.getChildren().add(barChart);
    }

    @Override
    //TODO: Set colour based on what has happened to the individual elements.
    //http://stackoverflow.com/questions/15233858/how-to-change-color-of-a-single-bar-java-fx
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
            //Set bar colors
            for(Node n:barChart.lookupAll(".default-color0.chart-bar")) {
                      n.setStyle("-fx-bar-fill: #123456;");
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
