package gui.view;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import assets.DasConstants;
import contract.operation.OperationType;
import gui.Controller;
import gui.Main;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

public class HelpView {

	private final Stage root = new Stage();
	private final Window owner;

	/**
	 * Create a new HelpView.
	 */
	public HelpView(Window owner) {
		this.owner = owner;
		init();
	}

	private void init() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/HelpView.fxml"));
		fxmlLoader.setController(this);

		root.getIcons().add(new Image(Controller.class.getResourceAsStream("/assets/icon.png")));
		root.initModality(Modality.NONE);
		root.setTitle(DasConstants.PROJECT_NAME + ": Help");
		root.initOwner(owner);
		BorderPane p = null;
		try {
			p = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		root.setOnCloseRequest(event -> {
			event.consume(); // Better to do this now than missing it later.
			stopBoxRotation();
			;
			root.close();
		});

		createCubes(fxmlLoader);

		Scene dialogScene = new Scene(p, owner.getWidth() * 0.75, owner.getHeight() * 0.75);
		root.setScene(dialogScene);
	}

	public void show() {
		startBoxRotation();
		root.show();
	}

	private void createCubes(FXMLLoader fxmlLoader) {

		Label label;

		GridPane boxes = (GridPane) fxmlLoader.getNamespace().get("box");
		GridPane labels = (GridPane) fxmlLoader.getNamespace().get("box_label");
		
		int column = 0;
		for (OperationType ot : OperationType.values()) {
			// Create box
			final Box box = new Box();
			box.setMaterial(new PhongMaterial((Color) ot.color));
			box.setWidth(80);
			box.setHeight(40);
			box.setDepth(20);
			box.setOnMouseClicked(event -> {
				about(ot);
				boxClicked(box);
			});

			// Randomise rotation
			double[] random = new double[3];
			for (int i = 0; i < 2; i++) {
				int sign = Math.random() < 0.5 ? -1 : 1;
				random[i] = Math.random() * sign;
			}
			Point3D axis = new Point3D(random[0], random[1], random[2]);
			box.setRotationAxis(axis);
			box.setRotate(Math.random() * 180);

			// Rotation
			RotateTransition rt = new RotateTransition(Duration.seconds(10), box);
			rt.setByAngle(360);
			rt.setCycleCount(RotateTransition.INDEFINITE);
			rotTransitions.add(rt);

			/*
			 * Create label
			 */
			label = new Label(" " + ot.name().toUpperCase() + " ");
			label.setFont(Font.font("consolas", 15));
			label.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");

			// Add to panels
			labels.add(label, column, 0);
			((BorderPane) boxes.getChildren().get(column)).setCenter(box);
			column++;
		}
	}

	private void boxClicked(final Box box) {
		if (box.getScaleX() == 1) {
			RotateTransition rt = new RotateTransition(Duration.millis(500), box);
			rt.setByAngle(360);
			rt.setCycleCount(3);
			rt.play();

			ScaleTransition st = new ScaleTransition(Duration.millis(3 * 500 / 2), box);
			st.setByX(2);
			st.setByY(2);
			st.setByZ(2);
			st.setAutoReverse(true);
			st.setCycleCount(2);
			st.play();
		}
	}

	// TODO: implment
	private boolean playsound = true;

	private void about(OperationType ot) {
		if (playsound) {
			URL resource = getClass().getResource("/assets/sad_trombone.mp3");
			Media media = new Media(resource.toString());
			MediaPlayer mp3 = new MediaPlayer(media);
			mp3.play();
			Main.console.err("Operation info not implemented yet.");
			playsound = false;
		}

	}
	
	private final ArrayList<RotateTransition> rotTransitions = new ArrayList<RotateTransition>();
	private void stopBoxRotation() {
		for (RotateTransition rt : rotTransitions) {
			rt.stop();
		}
	}

	private void startBoxRotation() {
		for (RotateTransition rt : rotTransitions) {
			rt.play();
		}
	}
	
	public void aboutArray(){
		
	}
	
	public void aboutOrphan(){
		
	}
	
	public void aboutTree(){
		
	}
	
	public void onMouseClicked(Event me){
		
	}
	
	public void onMouseEntered(Event me){
		((Pane) me.getSource()).setBorder(DasConstants.BORDER_MOUSEOVER);
	}
	
	public void onMouseExited(Event me){
		((Pane) me.getSource()).setBorder(null);
	}
}
