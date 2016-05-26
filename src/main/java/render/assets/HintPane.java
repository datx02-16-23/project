package render.assets;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

/**
 * Hint pane for the visualizer window.
 *
 * @author Richard Sundqvist
 *
 */
public class HintPane extends Pane {

    public HintPane () {
        //@formatter:off
        setBackground(
                new Background(
                        new BackgroundImage(
                                new Image(getClass().getResourceAsStream("/assets/upload.png")),
                                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO,
                                        false, false, true, false))));
        //@formatter:on

        setVisible(true);
    }
}
