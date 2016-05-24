package gui.view;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import assets.Const;
import contract.interpreter.Interpreter;
import contract.json.Operation;
import contract.operation.OperationType;
import gui.Controller;
import gui.Main;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
        interpreter = new Interpreter();
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/view/InterpreterView.fxml"));
        fxmlLoader.setController(this);
        root = new Stage();
        root.getIcons().add(new Image(Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
        root.initModality(Modality.APPLICATION_MODAL);
        root.setTitle(Const.PROGRAM_NAME + ": Interpreter");
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

        // Button enabling
        interpretButton = (Button) namespace.get("interpretButton");
        moveToBeforeButton = (Button) namespace.get("moveToBeforeButton");
        keepButton = (Button) namespace.get("keepButton");
        keepButton.disableProperty().bind(moveToBeforeButton.disabledProperty());

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
     * @return True if the interpreted operations should be kept, false otherwise.
     */
    public boolean show (List<Operation> ops) {
        interpretButton.setDisable(ops.isEmpty());
        moveToBeforeButton.setDisable(true);
        receivedItems = ops;
        beforeItems.setAll(receivedItems);
        afterItems.clear();
        loadTestCases();
        // Set size and show
        root.setWidth(parent.getWidth() * 0.75);
        root.setHeight(parent.getHeight() * 0.75);
        root.showAndWait();
        return keep;
    }

    private void loadTestCases () {
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
    public void keepInterpreted () {
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
     * Listener for the "Discard" button.
     */
    public void discardInterpreted () {
        keep = false;
        root.close();
    }
    /**
     * onAction for the "{@literal<}--" button.
     */
    public void moveToBefore () {
        moveToBeforeButton.setDisable(true);
        interpretButton.setDisable(false);
        if (afterItems.isEmpty() == false) {
            beforeItems.setAll(afterItems);
            afterItems.clear();
        }
    }

    /**
     * onAction for the "Interpret" button.
     */
    public void interpret () {
        interpretButton.setDisable(true);
        moveToBeforeButton.setDisable(false);
        afterItems.clear();
        afterItems.addAll(beforeItems);
        int n = interpreter.consolidate(afterItems);
        if (n < 1) {
            Main.console.info("Interpretation did not return any new operations.");
        } else {
            Main.console.info("Interpretation returned " + n + " new operation(s)." + " List size reduced by "
                    + (beforeItems.size() - afterItems.size()) + ", going from " + beforeItems.size() + " to "
                    + afterItems.size() + ".");
        }
        afterCount.setText("" + afterItems.size());
    }

    @Override public void invalidated (Observable o) {
        beforeCount.setText("" + beforeItems.size());
        afterCount.setText("" + afterItems.size());
    }

    public void fast (List<Operation> ops) {
        interpreter.consolidate(ops);
    }
}
