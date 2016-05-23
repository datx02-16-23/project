package gui.dialog;

import java.io.IOException;
import java.util.Arrays;

import assets.Const;
import gui.Controller;
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

    public ExamplesDialog (Stage parent) {
        this.parent = parent;
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/dialog/ExamplesDialog.fxml"));
        fxmlLoader.setController(this);
        this.root = new Stage();
        this.root.getIcons().add(new Image(Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
        this.root.initModality(Modality.APPLICATION_MODAL);
        this.root.setTitle(Const.PROGRAM_NAME + ": Example");
        this.root.initOwner(this.parent);
        GridPane p = null;
        try {
            p = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.root.setOnCloseRequest(event -> {
            event.consume(); // Better to do this now than missing it later.
            this.closeButton();
        });
        fxmlLoader.getNamespace();
        this.input = (TextField) fxmlLoader.getNamespace().get("input");
        this.input.setOnKeyTyped(event -> {
            this.validateInput();
        });
        this.mirror = (TextField) fxmlLoader.getNamespace().get("mirror");
        this.status = (Label) fxmlLoader.getNamespace().get("status");
        this.name = (Label) fxmlLoader.getNamespace().get("name");
        this.run = (Button) fxmlLoader.getNamespace().get("run");
        Scene dialogScene = new Scene(p, p.getPrefWidth() - 5, p.getPrefHeight());
        this.root.setScene(dialogScene);
        this.root.setResizable(false);
    }

    private boolean validateInput () {
        String input = this.input.getText();
        if (input.length() == 0) {
            this.data = EMPTY;
            this.mirror.setText("[]");
            this.status.setText("INPUT VALID");
            this.status.setTextFill(STATUS_OK);
            this.run.setDisable(false);
            return true;
        }
        input = input.replaceAll("\\s+", "");
        input.replaceAll("\\s", "");
        String[] doubles_string;
        if (input.contains(",")) {
            doubles_string = input.split(",");
        } else {
            doubles_string = new String[] { input };
        }
        double[] doubles = new double[doubles_string.length];
        for (int i = 0; i < doubles_string.length; i++) {
            try {
                doubles [i] = Double.parseDouble(doubles_string [i]);
            } catch (Exception e) {
                this.status.setText("INPUT INVALID");
                this.status.setTextFill(STATUS_ERR);
                this.mirror.clear();
                this.data = null;
                this.run.setDisable(true);
                return false;
            }
        }
        this.data = doubles;
        this.mirror.setText(Arrays.toString(doubles));
        this.status.setText("INPUT VALID");
        this.status.setTextFill(STATUS_OK);
        this.run.setDisable(false);
        return true;
    }

    /**
     * Show and wait for user input. Returns a (possibly empty) double[].
     * 
     * @param name
     *            The name of the algoritm.
     * @return An array of doubles.
     */
    public double[] show (String name) {
        this.validateInput();
        this.name.setText(name);
        // Wait for user input.
        this.root.showAndWait();
        if (this.data == null) {
            this.input.clear();
            return null;
        } else {
            return this.data;
        }
    }

    public void closeButton () {
        this.input.clear();
        this.mirror.clear();
        this.data = null;
        this.root.close();
    }

    public void okButton () {
        this.root.close();
    }
}
