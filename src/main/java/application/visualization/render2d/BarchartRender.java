//package application.visualization.render2d;
//
//import java.util.List;
//
//import javafx.collections.ObservableList;
//import javafx.scene.Node;
//import javafx.scene.chart.*;
//import javafx.scene.chart.XYChart.Data;
//import javafx.scene.chart.XYChart.Series;
//import wrapper.datastructures.Array;
//import wrapper.datastructures.Array.ArrayElement;
//import wrapper.datastructures.DataStructure;
//import wrapper.datastructures.Element;
//
///**
// * Created by cb on 14/04/16.
// */
//public class BarchartRender extends Render {
//
//    private static final String                        DEFAULT_COLOR = "#123456";
//    private final DataStructure                        struct;
//    private final BarChart<String, Double>             barChart;
//    private final ObservableList<Data<String, Double>> elemData;
//
//    @SuppressWarnings({"unchecked", "rawtypes"})
//    public BarchartRender (DataStructure struct){
//        this.struct = struct;
//        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
//        final CategoryAxis xAxis = new CategoryAxis();
//        final NumberAxis yAxis = new NumberAxis();
//        barChart = new BarChart(xAxis, yAxis);
//        barChart.setLegendVisible(false);
//        barChart.setTitle(struct.identifier);
//        xAxis.setLabel("Index");
//        yAxis.setLabel("Value");
//        Series<String, Double> elemDataSeries = new XYChart.Series<>();
//        elemData = elemDataSeries.getData();
//        for (Element element : struct.getElements()) {
//            Array.ArrayElement e = (Array.ArrayElement) element;
//            elemData.add(new Data(e.getIndex()[0] + "", e.getValue()));
//        }
//        barChart.getData().add(elemDataSeries);
//        this.getChildren().add(barChart);
//    }
//
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    @Override
//    public void render (){
//        barChart.setMaxSize(this.getWidth(), this.getHeight());
//        List<Element> structElements = struct.getElements();
//        int elementsSize = structElements.size();
//        if (elemData.size() != elementsSize) { //Difference in size, probably because of an init.
//            elemData.clear();
//            ArrayElement ae;
//            for (Object o : struct.getElements()) {
//                ae = (ArrayElement) o;
//                elemData.add(new Data(ae.getIndex()[0] + "", ae.getValue()));
//            }
//            //Set bar colors
//            for (Node n : barChart.lookupAll(".default-color0.chart-bar")) {
//                n.setStyle("-fx-bar-fill: " + DEFAULT_COLOR + ";");
//            }
//        }
//        List<Element> modified = struct.getModifiedElements();
//        List<Element> reset = struct.getResetElements();
//        //Change values of elements
//        ArrayElement ae;
//        Data<String, Double> d;
//        for (int i = 0; i < elementsSize; i++) {
//            ae = (ArrayElement) structElements.get(i);
//            d = elemData.get(i);
//            d.setXValue(ae.getIndex()[0] + "");
//            d.setYValue(ae.getValue());
//            //Manage special color.
//            if (modified.contains(ae)) {
//                d.getNode().setStyle("-fx-bar-fill: " + ae.getColor() + ";");
//            }
//            else if (reset.contains(ae)) {
//                d.getNode().setStyle("-fx-bar-fill: " + DEFAULT_COLOR + ";");
//            }
//        }
//        struct.elementsDrawn();
//    }
//}
