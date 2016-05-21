package gui.view;

import java.io.IOException;

import assets.Const;
import contract.datastructure.DataStructure;
import contract.datastructure.VisualType;
import gui.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class VisualDialog {

	private final Stage parent, root;
	private final Label name;
	private DataStructure struct;
	private boolean changed;

	public VisualDialog(Stage parent) {
		this.parent = parent;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/dialog/DataStructureDialog.fxml"));
		fxmlLoader.setController(this);
		root = new Stage();
		root.getIcons().add(new Image(Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
		root.initModality(Modality.APPLICATION_MODAL);
		root.setTitle(Const.PROGRAM_NAME + ": Choose Visualisation");
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
		
		name = (Label) fxmlLoader.getNamespace().get("name");
		Scene dialogScene = new Scene(p, p.getPrefWidth(), p.getPrefHeight());
		root.setScene(dialogScene);
		root.setResizable(false);
	}

	public void closeButton() {
		changed = false;
		root.close();
	}

	public void okButton() {
		root.close();
	}

	/**
	 * Show the visualisation options dialog for a given structure.
	 * 
	 * @param struct
	 *            A DataStructure.
	 * @return True if the visualisation options have changed, false otherwise.
	 */
	public boolean show(DataStructure struct) {
		this.struct = struct;
		name.setText(struct.toString());
		root.showAndWait();
		return changed;
	}
}
