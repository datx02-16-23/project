package application.gui.views;

import java.io.IOException;

import application.assets.Strings;
import application.gui.GUI_Controller;
import application.gui.Main;
import application.visualization.VisualType;
import application.visualization.Visualization;
import application.visualization.render2d.Render.RenderSVF;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import wrapper.datastructures.*;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CreateStructureDialog {

	private final Stage parent, root;
	private final ChoiceBox visType, rawType, absType;
	private final ObservableList<RawType.AbstractType> absTypeItems;
	private final Spinner visOption;
	private final Label name;
	// Volatile
	private RawType raw;
	private RawType.AbstractType abs;
	private VisualType vis;
	private DataStructure struct;
	private String identifier;
	private boolean changed;

	public CreateStructureDialog(Stage parent) {
		this.parent = parent;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/CreateStructure.fxml"));
		fxmlLoader.setController(this);
		root = new Stage();
		root.getIcons().add(new Image(GUI_Controller.class.getResourceAsStream("/assets/icon_interpreter.png")));
		root.initModality(Modality.APPLICATION_MODAL);
		root.setTitle(Strings.PROJECT_NAME + ": Create Data Structure");
		root.initOwner(this.parent);
		GridPane p = null;
		try {
			p = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		root.setOnCloseRequest(event -> {
			event.consume();
			closeButton();
		});
		/*
		 * Raw Type
		 */
		rawType = (ChoiceBox) fxmlLoader.getNamespace().get("rawType");
		rawType.setItems(FXCollections.observableArrayList(RawType.values()));
		rawType.getSelectionModel().select(RawType.independentElement);
		rawType.setOnAction(event -> {
			chooseRawType();
		});
		/*
		 * Abstract Type
		 */
		absType = (ChoiceBox) fxmlLoader.getNamespace().get("absType");
		absTypeItems = FXCollections.observableArrayList();
		absType.setItems(absTypeItems);
		absType.setOnAction(event -> {
			chooseAbsType();
		});
		/*
		 * Visual options
		 */
		visType = (ChoiceBox) fxmlLoader.getNamespace().get("visType");
		visType.setItems(FXCollections.observableArrayList(VisualType.values()));
		visType.setOnAction(event -> {
			chooseVisual();
		});
		visOption = (Spinner) fxmlLoader.getNamespace().get("visOption");
		/*
		 * Build.
		 */
		name = (Label) fxmlLoader.getNamespace().get("name");
		Scene dialogScene = new Scene(p, p.getPrefWidth() - 5, p.getPrefHeight());
		root.setScene(dialogScene);
		root.setResizable(false);
	}

	private void chooseRawType() {
		raw = (RawType) rawType.getSelectionModel().getSelectedItem();
		if (raw.absTypes.length > 0) {
			absTypeItems.setAll(raw.absTypes);
			absTypeItems.add(null);
			absType.setDisable(false);
		} else {
			abs = null;
			absTypeItems.clear();
			absType.setDisable(true);
		}
	}

	private void chooseAbsType() {
		abs = (RawType.AbstractType) absType.getSelectionModel().getSelectedItem();
	}

	private void chooseVisual() {
		VisualType vt = (VisualType) visType.getSelectionModel().getSelectedItem();
		setSpinner(vt);
	}

	public void closeButton() {
		changed = false;
		root.close();
	}

	public void okButton() {
		resolveVisual();
		createStruct();
		root.close();
	}

	private void createStruct() {
		raw = (RawType) rawType.getSelectionModel().getSelectedItem();
		switch (raw) {
		case array:
			struct = new Array(identifier, abs, vis, null);
			break;
		case tree:
			struct = null;
			Main.console.err("Not supported yet.");
			break;
		case independentElement:
			struct = new IndependentElement(identifier, abs, vis, null);
			break;
		}
	}

	private void resolveVisual() {
		VisualType vt = (VisualType) visType.getSelectionModel().getSelectedItem();
		if (vt == null) {
			changed = false;
			root.close();
			return;
		}
		if (vt != struct.visual) {
			// Visual type changed
			if (vt.has_options) {
				struct.visualOption = (Integer) visOption.getValue();
			}
			vis = vt;
			changed = true;
		} else {
			// Visual type has not changed
			if (vt.has_options) {
				changed = struct.visualOption != (Integer) visOption.getValue();
				if (changed) {
					struct.visualOption = (Integer) visOption.getValue();
				}
			} else {
				changed = false;
			}
		}
	}

	/**
	 * Show the DataStructure creation dialog.
	 * 
	 * @param identifier
	 *            The name of the new structure.
	 * @return A new DataStructure. Returns {@code null} if the user cancelled.
	 */
	public DataStructure show(String identifier) {
		this.identifier = identifier;
		struct = null;
		name.setText("Create Variable: \"" + identifier + "\"");
		root.showAndWait();
		return struct;
	}

	private void setSpinner(VisualType vt) {
		if (vt.has_options) {
			RenderSVF rsvf = Visualization.getRender(vt).getOptionsSpinnerValueFactory();
			if (rsvf == null) {
				visOption.setDisable(true); // Failed to fetch options.
			} else {
				visOption.setDisable(false);
				visOption.setValueFactory(rsvf);
			}
		} else {
			visOption.setDisable(true);
		}
	}

	public void ignoreStructure() {
		// TODO: implement
	}
}
