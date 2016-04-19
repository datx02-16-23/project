package application.gui.views;

import java.io.IOException;
import java.util.Arrays;
import application.assets.Strings;
import application.gui.GUI_Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ExamplesDialog {

    private static final double[] EMPTY      = new double[] {};
    private static final Color    STATUS_OK  = Color.web("#00c8ff");
    private static final Color    STATUS_ERR = Color.web("#ff0000");
    private final TextField       input, mirror;
    private final Label           status, name;
    private final Stage           parent, root;
    private final Button          run;
    private double[]              data;

    public ExamplesDialog (Stage parent){
        this.parent = parent;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ExamplesDialog.fxml"));
        fxmlLoader.setController(this);
        root = new Stage();
        root.getIcons().add(new Image(GUI_Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
        root.initModality(Modality.APPLICATION_MODAL);
        root.setTitle(Strings.PROJECT_NAME + ": Example");
        root.initOwner(this.parent);
        GridPane p = null;
        try {
            p = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        root.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            closeButton();
        });
        fxmlLoader.getNamespace();
        input = (TextField) fxmlLoader.getNamespace().get("input");
        input.setOnKeyTyped(event -> {
            validateInput();
        });
        mirror = (TextField) fxmlLoader.getNamespace().get("mirror");
        status = (Label) fxmlLoader.getNamespace().get("status");
        name = (Label) fxmlLoader.getNamespace().get("name");
        run = (Button) fxmlLoader.getNamespace().get("run");
        Scene dialogScene = new Scene(p, p.getPrefWidth() - 5, p.getPrefHeight());
        root.setScene(dialogScene);
        root.setResizable(false);
    }

    private boolean validateInput (){
        String input = this.input.getText();
        if (input.length() == 0) {
            data = EMPTY;
            mirror.setText("[]");
            status.setText("INPUT VALID");
            status.setTextFill(STATUS_OK);
            run.setDisable(false);
            return true;
        }
        input = input.replaceAll("\\s+", "");
        input.replaceAll("\\s", "");
        String[] doubles_string;
        if (input.contains(",")) {
            doubles_string = input.split(",");
        }
        else {
            doubles_string = new String[] {input};
        }
        double[] doubles = new double[doubles_string.length];
        for (int i = 0; i < doubles_string.length; i++) {
            try {
                doubles[i] = Double.parseDouble(doubles_string[i]);
            } catch (Exception e) {
                status.setText("INPUT INVALID");
                status.setTextFill(STATUS_ERR);
                mirror.clear();
                data = null;
                run.setDisable(true);
                return false;
            }
        }
        data = doubles;
        mirror.setText(Arrays.toString(doubles));
        status.setText("INPUT VALID");
        status.setTextFill(STATUS_OK);
        run.setDisable(false);
        return true;
    }

    /**
     * Show and wait for user input. Returns a (possibly empty) double[].
     * 
     * @param name The name of the algoritm.
     * @return An array of doubles.
     */
    public double[] show (String name){
        validateInput();
        this.name.setText(name);
        //Wait for user input.
        root.showAndWait();
        if (data == null) {
            input.clear();
            return null;
        }
        else {
            return data;
        }
    }

    public void closeButton (){
        input.clear();
        mirror.clear();
        data = null;
        root.close();
    }

    public void okButton (){
        root.close();
    }
}
