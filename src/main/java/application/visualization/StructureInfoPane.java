package application.visualization;

import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

/**
 * 
 * @author Richard Sundqvist
 *
 */
public class StructureInfoPane extends TitledPane {

	private GridPane grid = new GridPane();
	private int noChildren;

	public StructureInfoPane(String title) {
		super();
		this.setText(title);
		this.setContent(grid);
		this.setMinSize(0, 0);
		this.setPrefSize(150, 150);
		this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.setCollapsible(true);
	}

	/**
	 * Add a row to this StructurePane.
	 * 
	 * @param label
	 *            The label for this row.
	 * @param stringProperty
	 *            The StringProperty for this row.
	 */
	protected void addRow(String label, StringProperty stringProperty) {
		Label _label = new Label(label);
		_label.setPadding(new Insets(0, 10, 0, 0));
		grid.add(_label, 0, noChildren);
		TextField tf = new TextField();
		tf.textProperty().bind(stringProperty);
		tf.setMinWidth(0);
		tf.prefWidth(0);
		tf.setMaxWidth(Double.MAX_VALUE);
		// tf.setPadding(new Insets(0, 0, 0, 10));
		grid.add(tf, 1, noChildren);
		noChildren++;
	}
}