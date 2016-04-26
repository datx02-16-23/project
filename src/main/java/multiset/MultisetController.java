package multiset;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Smith on 26/04/16.
 */
public class MultisetController {

    Stage window;
    Scene scene;

    public MultisetController(Stage stage){
        window = stage;
        scene = stage.getScene();
    }

    public void goBackPressed(){
        window.setScene(scene);
    }
}
