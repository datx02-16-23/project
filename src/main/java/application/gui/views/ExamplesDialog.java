package application.gui.views;

import java.io.IOException;
import java.util.Arrays;
import application.assets.Strings;
import application.gui.GUI_Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ExamplesDialog {

    private static final Color STATUS_OK  = Color.web("#00c8ff");
    private static final Color STATUS_ERR = Color.web("#ff0000");
    private final TextField    input;
    private final Label        status;
    private final Label        name;
    private final Stage        parent, root;

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
            root.close();
        });
        fxmlLoader.getNamespace();
        input = (TextField) fxmlLoader.getNamespace().get("input");
        input.setOnKeyTyped(event -> {
            validateInput();
        });
        status = (Label) fxmlLoader.getNamespace().get("status");
        name = (Label) fxmlLoader.getNamespace().get("name");
        Scene dialogScene = new Scene(p, p.getPrefWidth()-5, p.getPrefHeight());
        System.out.println(p.getPrefWidth());
        System.out.println(p.getMaxHeight());
        System.out.println(p.getMinHeight());
        root.setScene(dialogScene);
        root.setResizable(false);
    }

    private double[] data;
    
    private void validateInput (){
        String input = this.input.getText();
        if (input.length() == 0) {
            return;
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
            try{
                doubles[i] = Double.parseDouble(doubles_string[i]);                
            } catch (Exception e){
                status.setText("INPUT INVALID");
                status.setTextFill(STATUS_ERR);
                break;
            }
        }
        status.setText("INPUT VALID");
        status.setTextFill(STATUS_OK);
        System.out.println(Arrays.toString(data));
        System.out.println();
    }

    /**
     * Show and wait for user input. Returns a (possibly empty) double[].
     * 
     * @param name The name of the algoritm.
     * @return An array of doubles.
     */
    public double[] show (String name){
        data = null;
        this.name.setText(name);
        root.showAndWait();
        if (data == null) {
            return new double[] {};
        }
        else {
            return data;
        }
    }

    public void closeButton (){
        data = null;
        root.close();
    }

    public void okButton (){
        root.close();
    }
}
