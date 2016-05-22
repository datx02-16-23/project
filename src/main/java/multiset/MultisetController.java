package multiset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import multiset.filter.Filter;
import multiset.filter.iFilter;
import multiset.model.Model;
import multiset.model.RangePatterns;
import multiset.model.iModel;
import multiset.view.View;
import multiset.view.iView;

/**
 * Created by Smith on 26/04/16.
 */
public class MultisetController {

	private final FXMLLoader fxmlLoader;
	private TextField range, cond, input, output;
	private ListView<String> history;
	private iModel model;
	private iView view;
	private final Stage window;
	private final Scene previousScene;
	private final Timeline timeline;
	private ObservableList<String> items;

	public MultisetController(Stage window) {
		this.window = window;
		this.previousScene = window.getScene();
		fxmlLoader = new FXMLLoader(getClass().getResource("/view/MultisetView.fxml"));
		fxmlLoader.setController(this);
		VBox p = null;
		try {
			p = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadNamespaceItems(fxmlLoader.getNamespace());

		window.setScene(new Scene(p));
		timeline = new Timeline();
		setupTimeline();
	}

	private void setupTimeline(){
		final int renderTime = 17; // In ms
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(renderTime), actionEvent -> {
			model.tick(renderTime);
			view.render();
		}));
		timeline.setCycleCount(Animation.INDEFINITE);
	}

	/**
	 * Called when the "Back" button is pressed.
	 */
	public void goBackPressed() {
		timeline.stop();
		window.setScene(previousScene);
	}

	/**
	 * Called when the "Go!" button is pressed.
	 * (or enter is pressed...)
	 */
	public void run() {
		timeline.stop();
		Canvas ballCanvas = (Canvas) fxmlLoader.getNamespace().get("ballCanvas");

		iFilter filter = new Filter(input.getText(), output.getText(), cond.getText());
		ArrayList<Double> list = new RangePatterns( range.getText() ).getList();
		model = new Model(ballCanvas.getWidth(), ballCanvas.getHeight(), filter, list, items);
		view = new View(model, ballCanvas);

		timeline.play();
	}

	/**
	 * Called from textfields
	 */
	public void keyListener(KeyEvent event){
		if(event.getCode() == KeyCode.ENTER) {
			run(); // Run animation
			event.consume(); // "You shall not pass! (for safety reasons)"
		}
	}

	/**
	 * Load assets from theMultisetController FXML loader namespace.
	 * 
	 * @param namespace
	 *            The namespace of the FXML loader.
	 */
	private void loadNamespaceItems(Map<String, Object> namespace) {
		input = (TextField) namespace.get("input");
		output = (TextField) namespace.get("output");
		cond = (TextField) namespace.get("cond");
		range = (TextField) namespace.get("range");

		// List stuff
		history = (ListView<String>) namespace.get("collisionHistory");
		items = FXCollections.observableArrayList();
		history.setItems(items);
		items.add("History:");
	}

}
