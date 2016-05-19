package assets;

import draw.ARender;
import gui.Visualization;
import gui.Visualization.HintPane;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 * Utility class to reduce clutter.
 * 
 * @author Richard Sundqvist
 *
 */
public abstract class DasToolkit {
	// A FXML pane showing user instructions.
	public static final Visualization.HintPane HINT_PANE = new Visualization.HintPane();

	private DasToolkit() {
	} // Not to be instantiated.

	/*
	 * Render stuff
	 */
	public static Background createArrayBg() {
		Image image = new Image(ARender.class.getResourceAsStream("/assets/array.png"));
		BackgroundImage bgi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
		return new Background(bgi);

	}

	public static Background createTreeBg() {
		Image image = new Image(ARender.class.getResourceAsStream("/assets/tree.png"));
		BackgroundImage bgi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
		return new Background(bgi);
	}

	public static Background createOrphanBg() {
		Image image = new Image(ARender.class.getResourceAsStream("/assets/orphan.png"));
		BackgroundImage bgi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
		return new Background(bgi);
	}

	public static Border getMOBorder() {
		return new Border(new BorderStroke(Color.web("#123456"), BorderStrokeStyle.SOLID, new CornerRadii(3),
				new BorderWidths(3), new Insets(-3)));
	}
}
