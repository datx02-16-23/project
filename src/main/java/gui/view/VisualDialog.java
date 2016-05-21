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
	private final ChoiceBox choice;
	private final Spinner options;
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
		fxmlLoader.getNamespace();
		choice = (ChoiceBox) fxmlLoader.getNamespace().get("choice");
		for(VisualType vt : VisualType.values()){
			if(vt.has_clones == false){
				choice.getItems().add(vt);
			}
		}
		choice.setOnAction(event -> {
			chooseVisual();
		});
		options = (Spinner) fxmlLoader.getNamespace().get("children");
		name = (Label) fxmlLoader.getNamespace().get("name");
		Scene dialogScene = new Scene(p, p.getPrefWidth() - 5, p.getPrefHeight());
		root.setScene(dialogScene);
		root.setResizable(false);
	}

	private void chooseVisual() {
		VisualType vt = (VisualType) choice.getSelectionModel().getSelectedItem();
	}

	public void closeButton() {
		changed = false;
		root.close();
	}

	public void okButton() {
		VisualType vt = (VisualType) choice.getSelectionModel().getSelectedItem();
		if (vt == null) {
			changed = false;
			root.close();
			return;
		}
		if (vt != struct.resolveVisual()) {
			// Visual type changed
			struct.setVisual(vt);
			changed = true;
			root.close();
		}
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
		choice.getSelectionModel().select(struct.resolveVisual());
		root.showAndWait();
		return changed;
	}
}
