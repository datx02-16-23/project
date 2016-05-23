package gui.view;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import assets.Const;
import contract.Operation;
import contract.operation.OperationType;
import gui.Controller;
import gui.Main;
import interpreter.Interpreter;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    private final Map<String, Object>       namespace;
    private final Stage                     root;
    private final TextField                 beforeCount, afterCount;
    private final Interpreter               interpreter;
    private final Stage                     parent;
    private boolean                         keep;
    /**
     * Items received from the caller of show ().
     */
    private List<Operation>                 receivedItems;
    private final Button                    interpretButton;
    private final Button                    moveToBeforeButton;
    private final Button                    keepButton;

    @SuppressWarnings("unchecked") public InterpreterView (Stage parent) {
        this.parent = parent;
        this.interpreter = new Interpreter();
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/view/InterpreterView.fxml"));
        fxmlLoader.setController(this);
        this.root = new Stage();
        this.root.getIcons().add(new Image(Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
        this.root.initModality(Modality.APPLICATION_MODAL);
        this.root.setTitle(Const.PROGRAM_NAME + ": Interpreter");
        this.root.initOwner(this.parent);
        GridPane p = null;
        try {
            p = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Buttons
        this.root.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            this.discardInterpreted();
        });
        // High order routine
        this.interpreterRoutineChooser = (ChoiceBox<String>) fxmlLoader.getNamespace().get("routineChooser");
        this.interpreterRoutineChooser.getSelectionModel().selectedItemProperty().addListener(event -> {
            this.interpreterRoutineChooser();
        });
        this.interpreterRoutineChooser.setItems(
                FXCollections.observableArrayList("Discard", "Flush Set", "Keep Set", "Deconstruct", "Abort"));
        this.namespace = fxmlLoader.getNamespace();
        // Lists
        ListView<Operation> interpreterBefore = (ListView<Operation>) this.namespace.get("interpreterBefore");
        ListView<Operation> interpreterAfter = (ListView<Operation>) this.namespace.get("interpreterAfter");
        this.beforeItems = interpreterBefore.getItems();
        this.afterItems = interpreterAfter.getItems();
        this.beforeItems.addListener(this);
        this.afterItems.addListener(this);

        // Counters
        this.beforeCount = (TextField) this.namespace.get("beforeCount");
        this.afterCount = (TextField) this.namespace.get("afterCount");

        // Button enabling
        this.interpretButton = (Button) this.namespace.get("interpretButton");
        this.moveToBeforeButton = (Button) this.namespace.get("moveToBeforeButton");
        this.keepButton = (Button) this.namespace.get("keepButton");
        this.keepButton.disableProperty().bind(this.moveToBeforeButton.disabledProperty());

        // Size and build
        p.setPrefWidth(this.parent.getWidth() * 0.75);
        p.setPrefHeight(this.parent.getHeight() * 0.75);
        Scene dialogScene = new Scene(p, this.parent.getWidth() * 0.75, this.parent.getHeight() * 0.75);
        this.root.setScene(dialogScene);
    }

    /**
     * Show the Interpreter View.
     *
     * @param ops
     *            The list of operations to use.
     * @return True if the interpreted operations should be kept, false
     *         otherwise.
     */
    public boolean show (List<Operation> ops) {
        this.interpretButton.setDisable(ops.isEmpty());
        this.moveToBeforeButton.setDisable(true);
        this.receivedItems = ops;
        this.beforeItems.setAll(this.receivedItems);
        this.interpreterRoutineChooser.getSelectionModel().select(this.translateInterpreterRoutine());
        this.afterItems.clear();
        this.loadTestCases();
        // Set size and show
        this.root.setWidth(this.parent.getWidth() * 0.75);
        this.root.setHeight(this.parent.getHeight() * 0.75);
        this.root.showAndWait();
        return this.keep;
    }

    private void loadTestCases () {
        VBox casesBox = (VBox) this.namespace.get("casesBox");
        casesBox.getChildren().clear();
        List<OperationType> selectedTypes = this.interpreter.getTestCases();
        Insets insets = new Insets(2, 0, 2, 5);
        // Create CheckBoxes for all Consolidate operation types
        for (OperationType type : OperationType.values()) {
            if (!type.consolidable) {
                continue;
            }
            CheckBox cb = new CheckBox(type.toString());
            cb.setOnAction(event -> {
                if (cb.isSelected()) {
                    this.interpreter.addTestCase(type);
                } else {
                    this.interpreter.removeTestCase(type);
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
    public void keepInterpreted () {
        if (this.afterItems.isEmpty() == false) {
            this.receivedItems.clear();
            this.receivedItems.addAll(this.afterItems);
            this.keep = true;
            this.root.close();
        } else {
            this.keep = false;
        }
    }

    /**
     * Stylize routine names.
     *
     * @return A stylized routine name.
     */
    private String translateInterpreterRoutine () {
        switch (this.interpreter.getHighOrderRoutine()) {
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
    public void discardInterpreted () {
        this.keep = false;
        this.root.close();
    }

    private final ChoiceBox<String> interpreterRoutineChooser;
    private int                     newRoutine = -1;

    private void interpreterRoutineChooser () {
        String choice = this.interpreterRoutineChooser.getSelectionModel().getSelectedItem();
        switch (choice) {
        case "Discard":
            this.newRoutine = Interpreter.DISCARD;
            break;
        case "Flush Set":
            this.newRoutine = Interpreter.FLUSH_SET_ADD_HIGH;
            break;
        case "Keep Set":
            this.newRoutine = Interpreter.KEEP_SET_ADD_HIGH;
            break;
        case "Deconstruct":
            this.newRoutine = Interpreter.DECONSTRUCT;
            break;
        case "Abort":
            this.newRoutine = Interpreter.ABORT;
            break;
        }
        if (this.newRoutine != this.interpreter.getHighOrderRoutine()) {
            this.interpreter.setHighOrderRoutine(this.newRoutine);
            // saveProperties();
        }
    }

    /**
     * onAction for the "{@literal<}--" button.
     */
    public void moveToBefore () {
        this.moveToBeforeButton.setDisable(true);
        this.interpretButton.setDisable(false);
        if (this.afterItems.isEmpty() == false) {
            this.beforeItems.setAll(this.afterItems);
            this.afterItems.clear();
        }
    }

    /**
     * onAction for the "Interpret" button.
     */
    public void interpret () {
        this.interpretButton.setDisable(true);
        this.moveToBeforeButton.setDisable(false);
        this.afterItems.clear();
        this.afterItems.addAll(this.beforeItems);
        int n = this.interpreter.consolidate(this.afterItems);
        if (n < 1) {
            Main.console.info("Interpretation did not return any new operations.");
        } else {
            Main.console.info("Interpretation returned " + n + " new operation(s)." + " List size reduced by "
                    + (this.beforeItems.size() - this.afterItems.size()) + ", going from " + this.beforeItems.size()
                    + " to " + this.afterItems.size() + ".");
        }
        this.afterCount.setText("" + this.afterItems.size());
    }

    @Override public void invalidated (Observable o) {
        this.beforeCount.setText("" + this.beforeItems.size());
        this.afterCount.setText("" + this.afterItems.size());
    }

    public void fast (List<Operation> ops) {
        this.interpreter.consolidate(ops);
    }
}
