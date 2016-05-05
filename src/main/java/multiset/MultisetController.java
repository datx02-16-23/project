package multiset;

import java.util.Map;

import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by Smith on 26/04/16.
 */
public class MultisetController {

    private final Stage     window;
    private final Scene     scene;
    private TextField range, cond, input, output;

    public MultisetController (Stage stage){
        window = stage;
        scene = stage.getScene();
    }

    public void goBackPressed (){
        window.setScene(scene);
    }

    /**
     * Called when the "Go!" button is pressed.
     */
    public void run (){
        System.out.println("Go button pressed!");
        System.out.println("range = " + range.getText());
        System.out.println("cond = " + cond.getText());
        System.out.println("input = " + input.getText());
        System.out.println("output = " + output.getText());
    }
    
    /**
     * Load assets from the FXML loader namespace.
     * @param namespace The namespace of the FXML loader.
     */
    public void loadNamespaceItems(Map<String, Object> namespace){
        range = (TextField) namespace.get("range");
        cond = (TextField) namespace.get("cond");
        input = (TextField) namespace.get("input");
        output = (TextField) namespace.get("output");
    }
}
