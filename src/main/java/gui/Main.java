package gui;

import assets.Strings;
import assets.example.Examples;
import assets.example.Examples.Algorithm;
import gui.panel.OperationPanel;
import gui.panel.SourcePanel;
import io.LogStreamManager;
import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.util.Map;

/**
 * Entry class for the GUI.
 */
public class Main extends Application {

	/**
	 * Console for printing system and error messages.
	 */
	public static GUIConsole console;
	/**
	 * Indicates whether the program is being run for the first time.
	 */
	public static boolean firstRun;
	private Controller controller;
	private LogStreamManager lsm;

	@Override
	public void start(Stage primaryStage) throws Exception {
		lsm = new LogStreamManager(Strings.PROJECT_NAME + "_GUI");
		primaryStage.setTitle(Strings.PROJECT_NAME);
		// Create a Group view for the AV.
		Visualization visualization = new Visualization();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/VisualizerView.fxml"));
		SourcePanel sourceViewer = new SourcePanel();
		controller = new Controller(primaryStage, lsm, sourceViewer, visualization);
		OperationPanel operationPanel = controller.getOperationPanel();
		fxmlLoader.setController(controller);
		// Load and get the root layout.
		VBox root;
		try {
			root = fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		// Load console
		Map<String, Object> namespace = fxmlLoader.getNamespace();
		console = new GUIConsole((TextArea) namespace.get("console"));
		// Window size
		Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
		double windowWidth = (screenSize.getWidth() * .9);
		double windowHeight = (screenSize.getHeight() * .9);
		Scene scene = new Scene(root, windowWidth, windowHeight);
		// Extracting some nodes from the fxml:
		SplitPane sP = (SplitPane) namespace.get("splitPane");
		BorderPane operationPanelContainer = (BorderPane) namespace.get("operationPanelContainer");
		operationPanelContainer.setCenter(operationPanel);
		double leftDivider = (((GridPane) namespace.get("buttonsGrid")).getPrefWidth() + 14) / scene.getWidth();
		sP.setDividerPositions(leftDivider * 0.3, 1 - leftDivider);
		// Add examples
		Menu examples = (Menu) namespace.get("examplesMenu");
		for (Algorithm algo : Examples.Algorithm.values()) {
			MenuItem algoButton = new MenuItem(algo.name);
			algoButton.setOnAction(event -> {
				controller.loadExample(algo);
			});
			examples.getItems().add(algoButton);
		}
		// Add SourceViewer
		AnchorPane sourceViewContainer = (AnchorPane) namespace.get("sourceViewContainer");
		sourceViewContainer.getChildren().add(sourceViewer);
		AnchorPane.setTopAnchor(sourceViewer, 0.0);
		AnchorPane.setBottomAnchor(sourceViewer, 0.0);
		AnchorPane.setLeftAnchor(sourceViewer, 0.0);
		AnchorPane.setRightAnchor(sourceViewer, 0.0);
		// Add AV
		GridPane visualizationPane = (GridPane) namespace.get("visualizationPane");
		visualizationPane.add(visualization, 0, 0);
		// Load needed components of from main view in Controller.
		controller.loadMainViewFxID(fxmlLoader);
		// Load main window
		scene.getStylesheets().add(getClass().getResource("/VisualizerStyle.css").toExternalForm());
		primaryStage.setOnCloseRequest(event -> {
			event.consume(); // Better to do this now than missing it later.
			controller.closeProgram();
		});
		primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/assets/icon.png")));
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * Make a file name a bit more fancy. For example: "bubble_sort.json" ->
	 * "Bubble Sort". Specifically, this method will return the file name
	 * without '_' and '.*' and there is always an upper case after '_'.
	 * 
	 * @param original
	 *            The original file name
	 * @return A stylized file name.
	 */
	@SuppressWarnings("unused")
	private String stylizeFileName(String original) {
		StringBuilder sb = new StringBuilder();
		char currentChar;
		boolean nextUpper = false;
		sb.append(Character.toUpperCase(original.charAt(0)));
		for (int i = 1; i < original.length(); i++) {
			currentChar = original.charAt(i);
			if (currentChar == '_') {
				sb.append(" ");
				nextUpper = true;
			} else if (currentChar == '.') {
				return sb.toString();
			} else {
				if (nextUpper) {
					currentChar = Character.toUpperCase(currentChar);
					nextUpper = false;
				}
				sb.append(currentChar);
			}
		}
		return sb.toString(); // Shouldn't get called.
	}

	@Override
	public void stop() {
		if (controller != null) {
			controller.stopAutoPlay(); // Kill autoplay thread.
		}
		lsm.close();
	}

	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Printout of error messages and warnings from the program. Strings only.
	 * Use Object toString to print them.
	 * 
	 * @author Richard Sundqvist
	 *
	 */
	public class GUIConsole {

		private static final String prepend_force = "\n<>\t";
		private static final String prepend_err = "\n>\t";
		private static final String prepend_info = "\n";
		private static final String prepend_debug = "\n";
		public boolean quiet = false;
		public boolean info = true;
		public boolean err = true;
		public boolean debug = false;
		private final TextArea consoleTextArea;

		public GUIConsole(TextArea consoleTextArea) {
			this.consoleTextArea = consoleTextArea;
			consoleTextArea.setEditable(false);
			init();
		}

		/**
		 * Print a regular line to the GUI console.
		 * 
		 * @param info
		 *            The line to prine.
		 */
		public void info(String info) {
			if (quiet || !this.info) {
				return;
			}
			print(prepend_info + info);
		}

		/**
		 * Print an error to the GUI console.
		 * 
		 * @param err
		 *            The error to print.
		 */
		public void err(String err) {
			if (quiet || !this.err) {
				return;
			}
			print(prepend_err + err);
		}

		/**
		 * Print a debug String. Generally DISABLED.
		 * 
		 * @param debug
		 *            A debug String to print.
		 */
		public void debug(String debug) {
			if (quiet || !this.debug) {
				print(prepend_debug + debug);
			}
		}

		/**
		 * Print a line regardless of settings.
		 * 
		 * @param force
		 *            The line to print.
		 */
		public void force(String force) {
			print(prepend_force + force);
		}

		/**
		 * Print the given String. Runs on JavaFX Application thread.
		 * 
		 * @param string
		 *            The string to print to the console.
		 */
		private void print(String string) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					consoleTextArea.appendText(string);
				}
			});
		}

		/**
		 * Enable or disable information printouts.
		 * 
		 * @param value
		 *            The setting to apply.
		 */
		public void setInfo(boolean value) {
			info = value;
			if (!quiet) {
				print(prepend_info + "Information printouts " + (info ? "ENABLED." : "DISABLED."));
			}
		}

		/**
		 * Enable or disable Quiet Mode.
		 * 
		 * @param value
		 *            The setting to apply.
		 */
		public void setQuiet(boolean value) {
			quiet = value;
			force("Quiet Mode " + (quiet ? "ENABLED." : "DISABLED."));
		}

		/**
		 * Enable or disable debug printouts.
		 * 
		 * @param value
		 *            The setting to apply.
		 */
		public void setDebug(boolean value) {
			debug = value;
			if (!quiet) {
				print(prepend_debug + "Debug printouts " + (debug ? "ENABLED." : "DISABLED."));
			}
		}

		/**
		 * Enable or disable error printouts.
		 * 
		 * @param value
		 *            The setting to apply.
		 */
		public void setError(boolean value) {
			err = value;
			if (!quiet) {
				print(prepend_err + "Error printouts " + (err ? "ENABLED." : "DISABLED."));
			}
		}

		private void init() {
			StringBuilder sb = new StringBuilder();
			sb.append(Strings.PROJECT_NAME + " v" + Strings.VERSION_NUMBER);
			sb.append("\nAUTHORS: ");
			for (String s : Strings.DEVELOPER_NAMES) {
				sb.append(s + " | ");
			}
			sb.replace(sb.length() - 2, sb.length(), "\n");
			String initMessage = sb.toString();
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					consoleTextArea.setText(initMessage);
				}
			});
		}

		/**
		 * Clear the console.
		 */
		public void clear() {
			init();
		}
	}
}