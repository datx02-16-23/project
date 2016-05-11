package application.visualization.render2d;

import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.paint.Color;
import wrapper.datastructures.Array;
import wrapper.datastructures.Array.ArrayElement;
import wrapper.datastructures.DataStructure;
import wrapper.datastructures.Element;

public class BarchartRender_OLD extends Render {

	private static final String DEFAULT_COLOR = "#123456";
	private final DataStructure struct;
	private final BarChart<String, Double> barChart;
	private final ObservableList<Data<String, Double>> elemData;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public BarchartRender_OLD(DataStructure struct) {
		super(struct, 0, 0, 0, 0);
		this.struct = struct;
		this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
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
			elemData.add(new Data(e.getIndex()[0] + "", e.getNumericValue()));
		}
		barChart.getData().add(elemDataSeries);
		this.getChildren().add(barChart);
		this.setBackground(null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void render() {
		barChart.setMaxSize(this.getWidth(), this.getHeight());
		List<Element> structElements = struct.getElements();
		int elementsSize = structElements.size();
		if (elemData.size() != elementsSize) { // Difference in size, probably
												// because of an init.
			elemData.clear();
			ArrayElement ae;
			for (Object o : struct.getElements()) {
				ae = (ArrayElement) o;
				elemData.add(new Data(ae.getIndex()[0] + "", ae.getNumericValue()));
			}
			// Set bar colors
			for (Node n : barChart.lookupAll(".default-color0.chart-bar")) {
				n.setStyle("-fx-bar-fill: " + DEFAULT_COLOR + ";");
			}
		}
		List<Element> modified = struct.getModifiedElements();
		List<Element> reset = struct.getResetElements();
		// Change values of elements
		ArrayElement ae;
		Data<String, Double> d;
		for (int i = 0; i < elementsSize; i++) {
			ae = (ArrayElement) structElements.get(i);
			d = elemData.get(i);
			d.setXValue(ae.getIndex()[0] + "");
			d.setYValue(ae.getNumericValue());
			// Manage special color.
			if (modified.contains(ae)) {
				String color = "#" + ae.getColor().toString().substring(2);
				d.getNode().setStyle("-fx-bar-fill: " + color + ";");
			} else if (reset.contains(ae)) {
				d.getNode().setStyle("-fx-bar-fill: " + DEFAULT_COLOR + ";");
			}
		}
		struct.elementsDrawn();
	}

	@Override
	public void drawAnimatedElement(Element e, double x, double y, Color style) {
		// TODO Auto-generated method stub
	}

	@Override
	public double getX(Element e) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getY(Element e) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RenderSVF getOptionsSpinnerValueFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startAnimation(Element e, double start_x, double start_y, double end_x, double end_y) {
		// TODO Auto-generated method stub
	}

	@Override
	public double absX(Element e) {
		double bx = this.getTranslateX() + this.getLayoutX();
		return this.getX(e) + bx;
	}

	@Override
	public double absY(Element e) {
		double by = this.getTranslateY() + this.getLayoutY();
		return this.getY(e) + by;
	}
}
