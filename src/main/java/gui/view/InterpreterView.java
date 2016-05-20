package gui.view;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import assets.Const;
import contract.Operation;
import contract.operation.OperationType;
import gui.Main;
import gui.Controller;
import interpreter.Interpreter;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller and model for the Interpreter view.
 * 
 * @author Richard Sundqvist
 *
 */
public class InterpreterView implements InvalidationListener {

	private final ObservableList<Operation> beforeItems, afterItems;
	private Map<String, Object> namespace;
	private Stage root;
	private TextField beforeCount, afterCount;
	private final Interpreter interpreter;
	private final Stage parent;
	private boolean keep;
	/**
	 * Items received from the caller of show ().
	 */
	private List<Operation> receivedItems;

	@SuppressWarnings("unchecked")
	public InterpreterView(Stage parent) {
		this.parent = parent;
		interpreter = new Interpreter();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/InterpreterView.fxml"));
		fxmlLoader.setController(this);
		root = new Stage();
		root.getIcons().add(new Image(Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
		root.initModality(Modality.APPLICATION_MODAL);
		root.setTitle(Const.PROJECT_NAME + ": Interpreter");
		root.initOwner(this.parent);
		GridPane p = null;
		try {
			p = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Buttons
		root.setOnCloseRequest(event -> {
			event.consume(); // Better to do this now than missing it later.
			discardInterpreted();
		});
		// High order routine
		interpreterRoutineChooser = (ChoiceBox<String>) fxmlLoader.getNamespace().get("routineChooser");
		interpreterRoutineChooser.getSelectionModel().selectedItemProperty().addListener(event -> {
			interpreterRoutineChooser();
		});
		interpreterRoutineChooser.setItems(
				FXCollections.observableArrayList("Discard", "Flush Set", "Keep Set", "Deconstruct", "Abort"));
		namespace = fxmlLoader.getNamespace();
		// Lists
		ListView<Operation> interpreterBefore = (ListView<Operation>) namespace.get("interpreterBefore");
		ListView<Operation> interpreterAfter = (ListView<Operation>) namespace.get("interpreterAfter");
		beforeItems = interpreterBefore.getItems();
		afterItems = interpreterAfter.getItems();
		beforeItems.addListener(this);
		afterItems.addListener(this);
		// Counters
		beforeCount = (TextField) namespace.get("beforeCount");
		afterCount = (TextField) namespace.get("afterCount");
		// Size and build
		p.setPrefWidth(this.parent.getWidth() * 0.75);
		p.setPrefHeight(this.parent.getHeight() * 0.75);
		Scene dialogScene = new Scene(p, this.parent.getWidth() * 0.75, this.parent.getHeight() * 0.75);
		root.setScene(dialogScene);
	}

	/**
	 * Show the Interpreter View.
	 * 
	 * @param ops
	 *            The list of operations to use.
	 * @return True if the interpreted operations should be kept, false
	 *         otherwise.
	 */
	public boolean show(List<Operation> ops) {
		receivedItems = ops;
		beforeItems.setAll(receivedItems);
		interpreterRoutineChooser.getSelectionModel().select(translateInterpreterRoutine());
		afterItems.clear();
		loadTestCases();
		// Set size and show
		root.setWidth(this.parent.getWidth() * 0.75);
		root.setHeight(this.parent.getHeight() * 0.75);
		root.showAndWait();
		return keep;
	}

	private void loadTestCases() {
		VBox casesBox = (VBox) namespace.get("casesBox");
		casesBox.getChildren().clear();
		List<OperationType> selectedTypes = interpreter.getTestCases();
		Insets insets = new Insets(2, 0, 2, 5);
		// Create CheckBoxes for all Consolidate operation types
		for (OperationType type : OperationType.values()) {
			if (!type.consolidable) {
				continue;
			}
			CheckBox cb = new CheckBox(type.toString());
			cb.setOnAction(event -> {
				if (cb.isSelected()) {
					interpreter.addTestCase(type);
				} else {
					interpreter.removeTestCase(type);
				}
			});
			if (selectedTypes.contains(type)) {
				cb.setSelected(true);
			} else {
				cb.setSelected(false);
			}
			cb.setPadding(insets);
			casesBox.getChildren().add(cb);
		}
	}

	/**
	 * Listener for the "Keep" button.
	 */
	public void keepInterpreted() {
		if (afterItems.isEmpty() == false) {
			receivedItems.clear();
			receivedItems.addAll(afterItems);
			keep = true;
			root.close();
		} else {
			keep = false;
		}
	}

	/**
	 * Stylize routine names.
	 * 
	 * @return A stylized routine name.
	 */
	private String translateInterpreterRoutine() {
		switch (interpreter.getHighOrderRoutine()) {
		case Interpreter.DISCARD:
			return "Discard";
		case Interpreter.FLUSH_SET_ADD_HIGH:
			return "Flush Set";
		case Interpreter.KEEP_SET_ADD_HIGH:
			return "Keep Set";
		case Interpreter.DECONSTRUCT:
			return "Deconstruct";
		case Interpreter.ABORT:
			return "Abort";
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Listener for the "Discard" button.
	 */
	public void discardInterpreted() {
		keep = false;
		root.close();
	}

	private ChoiceBox<String> interpreterRoutineChooser;
	private int newRoutine = -1;

	private void interpreterRoutineChooser() {
		String choice = interpreterRoutineChooser.getSelectionModel().getSelectedItem();
		switch (choice) {
		case "Discard":
			newRoutine = Interpreter.DISCARD;
			break;
		case "Flush Set":
			newRoutine = Interpreter.FLUSH_SET_ADD_HIGH;
			break;
		case "Keep Set":
			newRoutine = Interpreter.KEEP_SET_ADD_HIGH;
			break;
		case "Deconstruct":
			newRoutine = Interpreter.DECONSTRUCT;
			break;
		case "Abort":
			newRoutine = Interpreter.ABORT;
			break;
		}
		if (newRoutine != interpreter.getHighOrderRoutine()) {
			interpreter.setHighOrderRoutine(newRoutine);
			// saveProperties();
		}
	}

	/**
	 * onAction for the "{@literal<}--" button.
	 */
	public void moveToBefore() {
		if (afterItems.isEmpty() == false) {
			beforeItems.setAll(afterItems);
			afterItems.clear();
		}
	}

	/**
	 * onAction for the "Interpret" button.
	 */
	public void interpret() {
		afterItems.clear();
		afterItems.addAll(beforeItems);
		int n = interpreter.consolidate(afterItems);
		Main.console.info("Interpretation returned " + n + " new operation(s).");
		afterCount.setText("" + afterItems.size());
	}

	@Override
	public void invalidated(Observable o) {
		beforeCount.setText("" + beforeItems.size());
		afterCount.setText("" + afterItems.size());
	}

	public void fast(List<Operation> ops) {
		interpreter.consolidate(ops);
	}
}
