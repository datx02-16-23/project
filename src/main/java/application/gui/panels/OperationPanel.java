package application.gui.panels;

import java.io.IOException;
import java.util.Map;

import application.gui.GUI_Controller;
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
import wrapper.Operation;

public class OperationPanel extends Pane {

    private final GUI_Controller                    controller;
    private final TextField                         currOpTextField;
    private final Label                             totNrOfOpLabel;
    private final ProgressBar                       opProgress;
    private final ObservableList<Operation>         items;
    private final ListView<Operation>               operationHistory;
    private final MultipleSelectionModel<Operation> selectionModel;
    private final FocusModel<Operation>             focusModel;

    @SuppressWarnings("unchecked")
    public OperationPanel (GUI_Controller controller){
        this.controller = controller;
        //Load content
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/OperationListPanel.fxml"));
        fxmlLoader.setController(controller);
        VBox root = null;
        try {
            root = (VBox) fxmlLoader.load();
        } catch (IOException e) {
            System.err.println(e);
        }
        //Content size
        root.prefHeightProperty().bind(this.heightProperty());
        root.prefWidthProperty().bind(this.widthProperty());
        Map<String, Object> namespace = fxmlLoader.getNamespace();
        currOpTextField = (TextField) namespace.get("currOpTextField");
        currOpTextField.setOnAction(event -> {
            textFieldOnAction();
        });
        //Fetch namespace items
        totNrOfOpLabel = (Label) namespace.get("totNrOfOpLabel");
        opProgress = (ProgressBar) namespace.get("opProgress");
        operationHistory = (ListView<Operation>) namespace.get("operationHistory");
        //List stuff
        items = FXCollections.observableArrayList();
        operationHistory.setItems(items);
        selectionModel = operationHistory.getSelectionModel();
        focusModel = operationHistory.getFocusModel();
        //Finishing touches and build
        opProgress.setProgress(-1); //Make the thingy bounce
        this.getChildren().add(root);
    }

    /**
     * Update the list position, focus and highlight. Update counters and progress bar.
     * 
     * @param The item to select.
     * @param jump if {@code true}, the list will jump to the selected item.
     */
    public void update (int index, boolean jump){
        //List selection and position
        if (jump) {
            selectionModel.select(index);
            focusModel.focus(index);
            operationHistory.scrollTo(index - 1);
        }
        currOpTextField.setText("" + (index));
        int totItems = items.size();
        totNrOfOpLabel.setText("/ " + totItems);
        //Progress bar 
        double progress = totItems == 0 ? -1 : (double) index / (double) totItems;
        opProgress.setProgress(progress);
    }

    /**
     * Returns the items shown by this OperationPanel.
     * 
     * @return The items shown by this OperationPanel.
     */
    public ObservableList<Operation> getItems (){
        return items;
    }

    /**
     * Returns the index of the selected Operation.
     * 
     * @return The index of the selected Operation.
     */
    public int getIndex (){
        return selectionModel.getSelectedIndex();
    }

    /**
     * Returns the of the selected Operation.
     * 
     * @return The selected Operation.
     */
    public Operation getOperation (){
        return selectionModel.getSelectedItem();
    }

    /**
     * Listener for the current operation box.
     */
    private void textFieldOnAction (){
        int index;
        try {
            currOpTextField.setStyle("-fx-control-inner-background: white;");
            index = Integer.parseInt(currOpTextField.getText());
        } catch (Exception exc) {
            // NaN
            currOpTextField.setStyle("-fx-control-inner-background: #C40000;");
            return;
        }
        controller.goToStep(index);
    }
}
