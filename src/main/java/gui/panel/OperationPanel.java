package gui.panel;

import java.io.IOException;
import java.util.Map;

import contract.Operation;
import gui.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.FocusModel;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class OperationPanel extends Pane {

    private final Controller                        controller;
    private final TextField                         currOpTextField;
    private final Label                             totNrOfOpLabel;
    private final ProgressBar                       opProgress;
    private final ObservableList<Operation>         items;
    private final ListView<Operation>               operationHistory;
    private final MultipleSelectionModel<Operation> selectionModel;
    private final FocusModel<Operation>             focusModel;

    @SuppressWarnings("unchecked")
    public OperationPanel (Controller controller) {
        this.controller = controller;
        // Load content
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/panel/OperationListPanel.fxml"));
        fxmlLoader.setController(controller);
        VBox root = null;
        try {
            root = (VBox) fxmlLoader.load();
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        }
        // Content size
        root.prefHeightProperty().bind(this.heightProperty());
        root.prefWidthProperty().bind(this.widthProperty());
        Map<String, Object> namespace = fxmlLoader.getNamespace();
        this.currOpTextField = (TextField) namespace.get("currOpTextField");
        this.currOpTextField.setOnAction(event -> {
            this.textFieldOnAction();
        });
        // Fetch namespace items
        this.totNrOfOpLabel = (Label) namespace.get("totNrOfOpLabel");
        this.opProgress = (ProgressBar) namespace.get("opProgress");
        this.operationHistory = (ListView<Operation>) namespace.get("operationHistory");
        // List stuff
        this.items = FXCollections.observableArrayList();
        this.operationHistory.setItems(this.items);
        this.selectionModel = this.operationHistory.getSelectionModel();
        this.focusModel = this.operationHistory.getFocusModel();
        // Finishing touches and build
        this.opProgress.setProgress(-1); // Make the thingy bounce
        this.getChildren().add(root);
    }

    /**
     * Update the list position, focus and highlight. Update counters and
     * progress bar.
     * 
     * @param index
     *            The index to select.
     * @param jump
     *            if {@code true}, the list will jump to the selected item.
     */
    public void update (int index, boolean jump) {
        // List selection and position
        if (jump) {
            this.selectionModel.select(index);
            this.focusModel.focus(index);
            this.operationHistory.scrollTo(index - 1);
        }
        this.currOpTextField.setText("" + index);
        int totItems = this.items.size();
        this.totNrOfOpLabel.setText("/ " + totItems);
        // Progress bar
        double progress = totItems == 0 ? -1 : (double) index / (double) totItems;
        this.opProgress.setProgress(progress);
    }

    /**
     * Returns the items shown by this OperationPanel.
     * 
     * @return The items shown by this OperationPanel.
     */
    public ObservableList<Operation> getItems () {
        return this.items;
    }

    /**
     * Returns the index of the selected Operation.
     * 
     * @return The index of the selected Operation.
     */
    public int getIndex () {
        return this.selectionModel.getSelectedIndex();
    }

    /**
     * Returns the of the selected Operation.
     * 
     * @return The selected Operation.
     */
    public Operation getOperation () {
        return this.selectionModel.getSelectedItem();
    }

    /**
     * Listener for the current operation box.
     */
    private void textFieldOnAction () {
        int index;
        try {
            this.currOpTextField.setStyle("-fx-control-inner-background: white;");
            index = Integer.parseInt(this.currOpTextField.getText());
        } catch (Exception exc) {
            // NaN
            this.currOpTextField.setStyle("-fx-control-inner-background: #C40000;");
            return;
        }
        this.controller.goToStep(index);
    }

    public void clear () {
        this.items.clear();
        this.totNrOfOpLabel.setText("/ 0");
        this.currOpTextField.setText("0");
        this.opProgress.setProgress(-1);
    }
}
