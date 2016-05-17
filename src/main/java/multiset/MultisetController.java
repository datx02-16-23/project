package multiset;

import java.io.IOException;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import multiset.filter.Filter;
import multiset.filter.iFilter;
import multiset.model.Model;
import multiset.model.iModel;
import multiset.view.View;
import multiset.view.iView;

/**
 * Created by Smith on 26/04/16.
 */
public class MultisetController {

	private final FXMLLoader fxmlLoader;
	private TextField range, cond, input, output;
	private iModel model;
	private iView view;
	private final Stage window;

	public MultisetController(Stage window) {
		this.window = window;
		fxmlLoader = new FXMLLoader(getClass().getResource("/MultisetView.fxml"));
		fxmlLoader.setController(this);
		VBox p = null;
		try {
			p = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadNamespaceItems(fxmlLoader.getNamespace());
		window.setScene(new Scene(p));
	}

	public void goBackPressed() {

	}

	/**
	 * Called when the "Go!" button is pressed.
	 */
	public void run() {
		Canvas ballCanvas = (Canvas) fxmlLoader.getNamespace().get("ballCanvas");
		iFilter filter = new Filter(input.getText(), output.getText(), cond.getText());
		model = new Model(ballCanvas.getWidth(), ballCanvas.getHeight(), filter, 0, 10);
		view = new View(model, ballCanvas);
		setupContinousUpdates();
		System.out.println("Go button pressed!");
	}

	/**
	 * Load assets from theMultisetController FXML loader namespace.
	 * 
	 * @param namespace
	 *            The namespace of the FXML loader.
	 */
	private void loadNamespaceItems(Map<String, Object> namespace) {
		range = (TextField) namespace.get("range");
		cond = (TextField) namespace.get("cond");
		input = (TextField) namespace.get("input");
		output = (TextField) namespace.get("output");
	}

	private void setupContinousUpdates() {
		final Timeline timeline = new Timeline();
		final int renderTime = 17; // In ms
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(renderTime), actionEvent -> {
			model.tick(renderTime);
			view.render();
		}));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}
}
