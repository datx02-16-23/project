package application.gui.views;

import java.io.IOException;
import java.util.Collection;

import application.assets.Strings;
import application.gui.GUI_Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import wrapper.datastructures.DataStructure;

public class IdentifierCollisionDialog {

	public static final short KEEP_OLD = 0;
	public static final short ALWAYS_KEEP_OLD = 1;
	public static final short CLEAR_OLD = 3;
	public static final short ALWAYS_CLEAR_OLD = 4;
	private short answer;
	private final TextField oldStructs, newStructs;
	private final CheckBox memory;
	private final Stage parent, root;

	public IdentifierCollisionDialog(Stage parent) {
		this.parent = parent;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/IdentifierCollisionDialog.fxml"));
		fxmlLoader.setController(this);
		root = new Stage();
		root.getIcons().add(new Image(GUI_Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
		root.initModality(Modality.APPLICATION_MODAL);
		root.setTitle(Strings.PROJECT_NAME + ": Identifier Collision");
		root.initOwner(this.parent);
		GridPane p = null;
		try {
			p = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		root.setOnCloseRequest(event -> {
			event.consume(); // Better to do this now than missing it later.
			answer = KEEP_OLD;
			root.close();
		});
		fxmlLoader.getNamespace();
		oldStructs = (TextField) fxmlLoader.getNamespace().get("oldStructs");
		newStructs = (TextField) fxmlLoader.getNamespace().get("newStructs");
		memory = (CheckBox) fxmlLoader.getNamespace().get("memory");
		Scene dialogScene = new Scene(p, p.getPrefWidth() - 5, p.getPrefHeight());
		root.setScene(dialogScene);
		root.setResizable(false);
	}

	/**
	 * 
	 * @param oldStructs
	 * @param newStructs
	 * @return
	 */
	public short show(Collection<DataStructure> oldStructs, Collection<DataStructure> newStructs) {
		this.oldStructs.setText(oldStructs.toString());
		this.newStructs.setText(newStructs.toString());
		root.showAndWait();
		return answer;
	}

	public void reject_old() {
		if (memory.isSelected()) {
			answer = ALWAYS_CLEAR_OLD;
		} else {
			answer = CLEAR_OLD;
		}
		root.close();
	}

	public void keep_old() {
		if (memory.isSelected()) {
			answer = ALWAYS_KEEP_OLD;
		} else {
			answer = KEEP_OLD;
		}
		root.close();
	}
}
