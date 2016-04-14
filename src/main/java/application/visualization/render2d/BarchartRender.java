package application.visualization.render2d;

import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.Pane;

/**
 * Created by cb on 14/04/16.
 */
public class BarchartRender extends Pane {

    public BarchartRender() {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> bc =
                new BarChart<String,Number>(xAxis,yAxis);
        bc.setTitle("Country Summary");
        xAxis.setLabel("Country");
        yAxis.setLabel("Value");

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("2003");
        series1.getData().add(new XYChart.Data("austria", 25601.34));
        series1.getData().add(new XYChart.Data("brazil", 20148.82));
        series1.getData().add(new XYChart.Data("france", 10000));
        series1.getData().add(new XYChart.Data("italy", 35407.15));
        series1.getData().add(new XYChart.Data("usa", 12000));
        bc.getData().add(series1);


        this.getChildren().add(bc);
    }


    public void render(){

    }


}
