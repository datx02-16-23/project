package application.visualization;

import application.gui.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class Overlay {

	private SplitPane root;
	private final VBox container;
	private final TitledPane summaryPane;
	private final Label label;

	public Overlay() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/VisualOverlay.fxml"));
		fxmlLoader.setController(null);
		try {
			root = fxmlLoader.load();
		} catch (Exception e) {
			Main.console.err("Failed to load Overlay from fxml: " + e);
		}
		container = (VBox) fxmlLoader.getNamespace().get("container");
		label = (Label) container.getChildren().get(0);
		summaryPane = (TitledPane) container.getChildren().get(1);
	}

	/**
	 * Add a Node to the Overlay.
	 * 
	 * @param tp
	 *            The Node to add.
	 */
	public void addNode(Node tp) {
		container.getChildren().add(tp);
	}

	/**
	 * Remove all titled panes except the summary pane.
	 */
	public void clear() {
		container.getChildren().clear();
		container.getChildren().add(label);
		container.getChildren().add(summaryPane);
	}

	/**
	 * Return the root object for this Overlay, which can be added to a Scene.
	 * 
	 * @return The root object for this Overlay.
	 */
	public Node getNode() {
		return root;
	}

	/**
	 * Expand all panes.
	 */
	public void expandAll() {
		for (Node tp : container.getChildren()) {
			if (tp instanceof TitledPane) {
				((TitledPane) tp).setExpanded(true);
			}
			// tp.setStyle("-fx-background-color: green");
			System.out.println("tp = " + tp + ", style = " + tp.getStyle());
		}
	}
}
