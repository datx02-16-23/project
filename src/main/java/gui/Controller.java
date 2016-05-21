package gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import assets.*;
import assets.example.*;
import assets.example.Examples.Algorithm;
import contract.*;
import contract.datastructure.DataStructure;
import contract.operation.Key;
import gui.panel.*;
import gui.view.*;
import io.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.util.Duration;
import model.Model;
import multiset.MultisetController;
import render.ModelRender;

/**
 * GUI controller class.
 */
public class Controller implements CommunicatorListener {

	private ModelRender modelRender;
	private Stage window;
	private final LogStreamManager lsm;
	private final Model model;
	// Controls
	private Menu visualMenu;
	private MenuButton streamBehaviourMenuButton;
	//Stream behaviour
	private boolean streamAlwaysShowLastOperation = true;
	private boolean streamStartAutoplay = false;
	// Autoplay
	private boolean isPlaying = false;
	private int stepDelaySpeedupFactor = 1;
	private long stepDelayBase = 1500;
	private long stepDelay = stepDelayBase / stepDelaySpeedupFactor;
	// Settings dialog stuff
	private Stage settingsView;
	// Views, panels, dialogs
	private final ConnectedView connectedView;
	private final InterpreterView interpreterView;
	private final SourcePanel sourceViewer;
	private final OperationPanel operationPanel;
	private final ExamplesDialog examplesDialog;
	private final VisualDialog visualDialog;
	private final CreateStructureDialog createStructureDialog;
	private final IdentifierCollisionDialog icd;
	private HelpView helpView;
	// Buttons
	private Button backwardButton, forwardButton, playPauseButton;
	private Button restartButton, clearButton, speedButton;

	public Controller(Stage window, LogStreamManager lsm, SourcePanel sourceViewer, ModelRender visualization) {
		this.modelRender = visualization;
		visualization.setAnimationTime(stepDelay);
		this.window = window;
		model = Model.instance();
		this.lsm = lsm;
		this.lsm.PRETTY_PRINTING = true;
		this.lsm.setListener(this);
		this.sourceViewer = sourceViewer;
		this.operationPanel = new OperationPanel(this);
		this.examplesDialog = new ExamplesDialog(window);
		this.visualDialog = new VisualDialog(window);
		this.createStructureDialog = new CreateStructureDialog(window);
		this.connectedView = new ConnectedView(window, (JGroupCommunicator) lsm.getCommunicator());
		this.icd = new IdentifierCollisionDialog(window);
		this.helpView = new HelpView(window);
		initSettingsPane();
		interpreterView = new InterpreterView(window);
		loadProperties();
	}

	public void showSettings() {
		// Playback speed
		perSecField.setText(df.format(1000.0 / stepDelayBase));
		timeBetweenField.setText(df.format(stepDelayBase));
		toggleAutorunStream.setSelected(streamAlwaysShowLastOperation);
		// Size and show
		settingsView.setWidth(this.window.getWidth() * 0.75);
		settingsView.setHeight(this.window.getHeight() * 0.75);
		settingsView.show();
	}

	public void showMultiset() {
		new MultisetController(window);
	}

	private CheckBox toggleAutorunStream;

	public void toggleAutorunStream() {
		streamAlwaysShowLastOperation = toggleAutorunStream.isSelected();
		unsavedChanged();
	}

	public void jumpToEndClicked(Event e) {
		streamBehaviourMenuButton.setText(">>");
		Main.console.info("Model will always display the latest operation streamed operation.");
		streamAlwaysShowLastOperation = true;
		streamStartAutoplay = false;
	}

	public void continueClicked(Event e) {
		streamBehaviourMenuButton.setText(">");
		Main.console.info("Autoplay will start when a streamed operation has been received.");
		streamAlwaysShowLastOperation = false;
		streamStartAutoplay = true;
	}

	public void doNothingClicked(Event e) {
		streamBehaviourMenuButton.setText("=");
		Main.console.info("Streaming will not force model progression.");
		streamAlwaysShowLastOperation = false;
		streamStartAutoplay = false;
	}

	/**
	 * Clear everything.
	 */
	public void clearButtonClicked() {
		visualMenu.getItems().clear();
		visualMenu.setDisable(true);
		model.hardClear();
		modelRender.clear();
		sourceViewer.clear();
		operationPanel.clear();
		setButtons();
	}

	/**
	 * Starts playing or pause the AV animation.
	 */
	public void playPauseButtonClicked() {
		if (!isPlaying) {
			startAutoPlay();
		} else {
			stopAutoPlay();
		}
	}

	private Timeline autoplayTimeline;

	public void startAutoPlay() {
		playPauseButton.setText("Pause");
		if (autoplayTimeline != null) {
			autoplayTimeline.stop();
		}
		isPlaying = true;
		stepForwardButtonClicked();
		autoplayTimeline = new Timeline();
		autoplayTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(stepDelay), event -> {
			if (stepForwardButtonClicked() == false) {
				stopAutoPlay();
			}
		}));
		autoplayTimeline.setCycleCount(Animation.INDEFINITE);
		autoplayTimeline.play();
	}

	public void stopAutoPlay() {
		if (autoplayTimeline != null) {
			autoplayTimeline.stop();
			playPauseButton.setText("Play");
			isPlaying = false;
		}
	}

	/**
	 * Restart the AV animation.
	 */
	public void restartButtonClicked() {
		stopAutoPlay();
		model.reset();
		updatePanels();
		setButtons();
	}

	/**
	 * Listener for the Forward button.
	 * 
	 * @return The value of stepModelForward().
	 */
	public boolean stepForwardButtonClicked() {
		return stepModelForward();
	}

	/**
	 * Steps the model forward and forces any ongoing animations to cancel.
	 * 
	 * @return True if the model could progress. False otherwise.
	 */
	private boolean stepModelForward() {
		boolean result = model.stepForward();

		modelRender.render(model.getLastOp());
		updatePanels();
		setButtons();

		return result;
	}

	/**
	 * Step the animation backward
	 */
	public void stepBackwardButtonClicked() {
		stopAutoPlay();
		if (model.stepBackward()) {
			modelRender.init();
			modelRender.render(model.getLastOp());
			setButtons();
			updatePanels();
		}
	}

	/**
	 * Change the animation speed
	 */
	public void changeSpeedButtonClicked() {
		boolean isPlaying = this.isPlaying;
		if (isPlaying) {
			stopAutoPlay();
		}
		stepDelaySpeedupFactor = stepDelaySpeedupFactor * 2 % 255;
		speedButton.setText(stepDelaySpeedupFactor + "x");
		stepDelay = stepDelayBase / stepDelaySpeedupFactor;
		modelRender.setAnimationTime(stepDelay);
		if (isPlaying) {
			startAutoPlay();
		}
	}

	public void changeSpeedButtonRightClicked() {
		boolean isPlaying = this.isPlaying;
		if (isPlaying) {
			stopAutoPlay();
		}
		for (int i = 0; i < 7; i++) {
			changeSpeedButtonClicked();
		}
		if (isPlaying) {
			startAutoPlay();
		}
	}

	public void aboutProgram() {
		Main.console.info("Placeholder: A project by ");
		for (String name : Const.DEVELOPER_NAMES) {
			Main.console.info(name + ", ");
		}
	}

	public void openInterpreterView() {
		stopAutoPlay();
		if (interpreterView.show(model.getOperations())) {
			model.reset();
			modelRender.clearAndCreateVisuals();
			operationPanel.getItems().setAll(model.getOperations());
			updatePanels();
		}
	}

	public void interpretOperationHistory() {
		interpreterView.fast(model.getOperations());
		updatePanels();
		modelRender.clearAndCreateVisuals();
		operationPanel.getItems().setAll(lsm.getOperations());
	}

	/**
	 * Update SourcePanel and OperationPanel.
	 */
	private void updatePanels() {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				int index = model.getIndex();
				sourceViewer.show(model.getLastOp());
				operationPanel.update(index, true);
			}
		});
	}

	/*
	 * Operation Panel listeners
	 */
	/**
	 * Jump to the given index. {@code index} less than 0 jumps to start,
	 * {@code index} greater than {@code size} jumps to end.
	 * 
	 * @param index
	 *            The index to jump to.
	 */
	public void goToStep(int index) {
		if (1 < 2) {
			System.err.println("goToStep() is buggy and has been disabled.");
			return;
		}
		model.goToStep(index);
		modelRender.init();
		modelRender.render(model.getLastOp());
		operationPanel.update(model.getIndex(), false);
	}

	public void inspectSelection() {
		Main.console.force("Not implemented.");
	}

	public void gotoSelection() {
		goToStep(operationPanel.getIndex());
	}

	public void doubleClickGoTo() {
		goToStep(operationPanel.getIndex());
	}

	/*
	 * Operation Panel end.
	 */
	private DecimalFormat df;
	private Label settingsSaveState;

	private void initSettingsPane() {
		df = new DecimalFormat("#.####");
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/SettingsView.fxml"));
		fxmlLoader.setController(this);
		settingsView = new Stage();
		settingsView.getIcons().add(new Image(Controller.class.getResourceAsStream("/assets/icon_settings.png")));
		settingsView.initModality(Modality.APPLICATION_MODAL);
		settingsView.setTitle(Const.PROGRAM_NAME + ": Settings and Preferences");
		settingsView.initOwner(this.window);
		GridPane p = null;
		try {
			p = fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		settingsView.setOnCloseRequest(event -> {
			event.consume(); // Better to do this now than missing it later.
			revertSettings();
		});
		// Get namespace items
		// Save state label
		settingsSaveState = (Label) fxmlLoader.getNamespace().get("settingsSaveState");
		// Playpack speed
		timeBetweenField = (TextField) fxmlLoader.getNamespace().get("timeBetweenField");
		perSecField = (TextField) fxmlLoader.getNamespace().get("perSecField");
		toggleAutorunStream = (CheckBox) fxmlLoader.getNamespace().get("toggleAutorunStream");
		p.setPrefWidth(this.window.getWidth() * 0.75);
		p.setPrefHeight(this.window.getHeight() * 0.75);
		Scene dialogScene = new Scene(p, this.window.getWidth() * 0.75, this.window.getHeight() * 0.75);
		settingsView.setScene(dialogScene);
	}

	public void connectedToChannel() {
		connectedView.show();
	}

	/**
	 * Used for closing the GUI properly.
	 */
	public void closeProgram() {
		lsm.close();
		window.close();
	}

	/**
	 * Used for choosing a file to Visualize.
	 */
	public void openFileChooser() {
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File(System.getProperty("user.home")));
		fc.setTitle("Open OI-File");
		fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON-Files", "*.json"),
				new FileChooser.ExtensionFilter("All Files", "*.*"));
		File source = fc.showOpenDialog(window);
		if (source != null) {
			readLog(source);
		}
	}

	/**
	 * Helper function for {@link #openFileChooser() openFileChooser}
	 * 
	 * @param file
	 *            The file to load.
	 */
	public void readLog(File file) {
		lsm.clearData();
		boolean success = lsm.readLog(file);
		if (success) {
			loadFromLSM();
			lsm.clearData();
			Main.console.info("Import successful: " + file);
		} else {
			Main.console.err("Import failed: " + file);
		}
	}

	private boolean always_clear_old = false;
	private boolean always_keep_old = false;

	/**
	 * Load the current data from LSM. Does not clear any data.
	 */
	public void loadFromLSM() {
		// Add operations to model and create Render visuals, then draw them.
		Map<String, DataStructure> oldStructs = model.getStructures();
		Map<String, DataStructure> newStructs = lsm.getDataStructures();
		if (checkCollision(oldStructs, newStructs) == false) {
			return;
		}
		oldStructs.putAll(newStructs);
		visualMenu.getItems().clear();
		visualMenu.setDisable(newStructs.isEmpty());
		model.getOperations().addAll(lsm.getOperations());
		checkOperationIdentifiers(model.getOperations(), model.getStructures());
		sourceViewer.addSources(lsm.getSources());
		modelRender.clearAndCreateVisuals();
		modelRender.render(model.getLastOp());
		// Update operation list
		operationPanel.getItems().addAll(lsm.getOperations());
		loadVisualMenu();
		updatePanels();
		setButtons();
	}

	private void checkOperationIdentifiers(List<Operation> ops, Map<String, DataStructure> structs) {
		HashSet<String> opsIdentifiers = new HashSet<String>();
		/*
		 * Gather all operation identifiers.
		 */
		for (Operation op : ops) {
			String identifier;
			Locator locator;
			DataStructure struct;
			switch (op.operation) {
			case message:
				break;
			case read:
			case write:
				locator = ((Locator) op.operationBody.get(Key.source));
				if (locator != null) {
					struct = structs.get(locator.identifier);
					if (struct == null) {
						opsIdentifiers.add(locator.identifier);
					}
				}
				locator = ((Locator) op.operationBody.get(Key.target));
				if (locator != null) {
					identifier = locator.identifier;
					struct = structs.get(identifier);
					if (struct == null) {
						opsIdentifiers.add(locator.identifier);
					}
				}
				break;
			case swap:
				break;
			case remove:
				identifier = ((Locator) op.operationBody.get(Key.target)).identifier;
				struct = structs.get(identifier);
				if (struct == null) {
					opsIdentifiers.add(identifier);
				}
				break;
			}
		}
		Set<String> keyset = structs.keySet();
		DataStructure newStruct;
		for (String identifier : opsIdentifiers) {
			if (keyset.contains(identifier) == false) {
				newStruct = createStructureDialog.show(identifier);
				if (newStruct != null) {
					structs.put(newStruct.identifier, newStruct);
				}
			}
		}
	}

	private boolean checkCollision(Map<String, DataStructure> oldStructs, Map<String, DataStructure> newStructs) {
		checkCollison: for (String newKey : newStructs.keySet()) {
			for (String oldKey : oldStructs.keySet()) {
				if (oldKey.equals(newKey)) {
					Main.console.force("ERROR: Data Structure identifier collision:");
					Main.console.force("Known structures: " + model.getStructures().values());
					Main.console.force("New structures: " + lsm.getDataStructures().values());
					if (always_clear_old) {
						Main.console.force("Known structures cleared.");
						clearButtonClicked();
						break checkCollison;
					} else if (always_keep_old) {
						Main.console.force("New structures rejected.");
						lsm.clearData();
						return false;
					} else {
						java.awt.Toolkit.getDefaultToolkit().beep();
						short routine = icd.show(oldStructs.values(), oldStructs.values());
						switch (routine) {
						// Clear old structures, import new
						case IdentifierCollisionDialog.ALWAYS_CLEAR_OLD:
							always_clear_old = true;
							clearButtonClicked();
							Main.console.force("Conflicting structures will overrwrite existing for this session.");
							break checkCollison;
						case IdentifierCollisionDialog.CLEAR_OLD:
							clearButtonClicked();
							Main.console.force("Known structures cleared.");
							break checkCollison;
						// Reject new structures
						case IdentifierCollisionDialog.ALWAYS_KEEP_OLD:
							always_keep_old = true;
							Main.console.force("Conflicting structures will be rejected for this session.");
							lsm.clearData();
							return false;
						case IdentifierCollisionDialog.KEEP_OLD:
							Main.console.force("New structures rejected.");
							lsm.clearData();
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private void loadVisualMenu() {
		MenuItem reset = new MenuItem("Reset Positions");
		reset.setOnAction(event -> {
			modelRender.placeVisuals();
		});
		visualMenu.getItems().add(reset);

		MenuItem live = new MenuItem("Show Live Stats");
		live.setOnAction(event -> {
			modelRender.showLiveStats();
		});
		live.setDisable(true);
		visualMenu.getItems().add(live);

		visualMenu.getItems().add(new SeparatorMenuItem());

		MenuItem struct_mi;
		for (DataStructure struct : model.getStructures().values()) {
			struct_mi = new MenuItem();
			struct_mi.setText(struct.identifier + ": " + struct.rawType.toString().toUpperCase());
			struct_mi.setOnAction(event -> {
				openVisualDialog(struct);
			});
			visualMenu.getItems().add(struct_mi);
		}
	}

	public void openVisualDialog(DataStructure struct) {
		if (visualDialog.show(struct)) {
			modelRender.init();
		}
	}

	/**
	 * Method for reception of streamed messages.
	 */
	@Override
	public void messageReceived(short messageType) {
		if (messageType >= 10) {
			JGroupCommunicator jgc = (JGroupCommunicator) lsm.getCommunicator();
			connectedView.update(jgc.getMemberStrings(), jgc.allKnownEntities());
			return;
		}
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				loadFromLSM();
				lsm.clearData();

				if (streamAlwaysShowLastOperation) {
					model.goToEnd();
					stepForwardButtonClicked();
				} else if (streamStartAutoplay) {
					startAutoPlay();
				}

				updatePanels();
			}
		});
	}

	public void openDestinationChooser() {
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File(System.getProperty("user.home")));
		fc.setTitle("Save OI-File");
		DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd_HHmmss");
		Calendar cal = Calendar.getInstance();
		fc.setInitialFileName(dateFormat.format(cal.getTime()));
		fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON-Files", "*.json"),
				new FileChooser.ExtensionFilter("All Files", "*.*"));
		File target = fc.showSaveDialog(this.window);
		if (target == null) {
			return;
		}
		lsm.setOperations(model.getOperations());
		lsm.setDataStructures(model.getStructures());
		lsm.setSources(sourceViewer.getSources());
		boolean old = lsm.PRETTY_PRINTING;
		lsm.PRETTY_PRINTING = model.getOperations().size() > 100;
		lsm.printLog(target);
		lsm.PRETTY_PRINTING = old;
	}

	public void propertiesFailed(Exception exception) {
		if (exception != null) {
			Main.console.err(exception.getMessage());
		}
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/dialog/PropertiesAlertDialog.fxml"));
		Stage stage = new Stage();
		GridPane p = null;
		try {
			p = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Scene dialogScene = new Scene(p);
		stage.setOnCloseRequest(event -> {
			event.consume();
			stage.close();
		});
		Button close = (Button) loader.getNamespace().get("closeAlert");
		close.setOnAction(event -> {
			event.consume();
			stage.close();
		});
		stage.setScene(dialogScene);
		stage.toFront();
		stage.show();
	}

	public void loadMainViewFxID(FXMLLoader mainViewLoader) {
		ObservableMap<String, Object> namespace = mainViewLoader.getNamespace();
		// Load from main view namespace
		playPauseButton = (Button) namespace.get("playPauseButton");
		restartButton = (Button) namespace.get("restartButton");
		backwardButton = (Button) namespace.get("backwardButton");
		forwardButton = (Button) namespace.get("forwardButton");
		clearButton = (Button) namespace.get("clearButton");
		speedButton = (Button) namespace.get("speedButton");
		streamBehaviourMenuButton = (MenuButton) namespace.get("streamBehaviourMenuButton");
		visualMenu = (Menu) namespace.get("visualMenu");
		visualMenu.setDisable(true);
		setButtons();
	}

	/*
	 * SETTINGS PANEL
	 */
	private boolean settingsChanged = false;

	// Commit changes to file.
	public void saveSettings() {
		if (settingsChanged) {
			saveProperties();
			noUnsavedChanges();
		}
		settingsView.close();
	}

	// Reload settings from file.
	public void revertSettings() {
		if (settingsChanged) {
			loadProperties();
			noUnsavedChanges();
		}
		settingsView.close();
	}

	private void noUnsavedChanges() {
		settingsChanged = false;
		settingsSaveState.setText("No unsaved changes.");
		settingsSaveState.setTextFill(Color.web("#00c8ff"));
	}

	private void unsavedChanged() {
		settingsChanged = true;
		settingsSaveState.setText("Unsaved changes.");
		settingsSaveState.setTextFill(Color.web("#ff0000"));
	}

	// Playback speed
	private TextField perSecField;

	public void setPlayBackOpsPerSec(Event e) {
		long newSpeed;
		try {
			perSecField.setStyle("-fx-control-inner-background: white;");
			newSpeed = Long.parseLong(perSecField.getText());
		} catch (Exception exc) {
			// NaN
			perSecField.setStyle("-fx-control-inner-background: #C40000;");
			return;
		}
		if (newSpeed <= 0) {
			perSecField.setText("invalid");
			perSecField.selectAll();
			return;
		}
		// Valid input. Change other button and speed variable.
		perSecField.setText(df.format(newSpeed));// BLA
		timeBetweenField.setText(df.format((1000.0 / newSpeed)));
		stepDelayBase = (1000L / newSpeed);
		stepDelay = stepDelayBase / stepDelaySpeedupFactor;
		unsavedChanged();
	}

	private TextField timeBetweenField;

	public void setPlaybackTimeBetweenOperations(Event e) {
		long newSpeed;
		try {
			perSecField.setStyle("-fx-control-inner-background: white;");
			newSpeed = Long.parseLong(timeBetweenField.getText());
		} catch (Exception exc) {
			// NaN
			perSecField.setStyle("-fx-control-inner-background: #C40000;");
			return;
		}
		if (newSpeed < 0) {
			timeBetweenField.setText("invalid");
			perSecField.selectAll();
			return;
		}
		// Valid input. Change other button and speed variable.
		perSecField.setText(df.format(1000.0 / newSpeed));
		timeBetweenField.setText(df.format(newSpeed));
		stepDelayBase = newSpeed;
		stepDelay = stepDelayBase / stepDelaySpeedupFactor;
		unsavedChanged();
	}

	public Properties tryLoadProperties() {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(Const.PROPERTIES_FILE_NAME);
		if (inputStream == null) {
			Main.console.err("Failed to open properties file.");
			propertiesFailed(null);
			return DefaultProperties.get();
		}
		Properties properties = new Properties();
		try {
			properties.load(inputStream);
			inputStream.close();
			return properties;
		} catch (IOException e) {
			propertiesFailed(e);
			Main.console.err("Property file I/O failed.");
			return DefaultProperties.get();
		}
	}

	// Load settings
	public void loadProperties() {
		Properties properties = tryLoadProperties();
		stepDelayBase = Long.parseLong(properties.getProperty("playbackStepDelay"));
		stepDelay = stepDelayBase; // Speedup factor is 1 at startup.
		streamAlwaysShowLastOperation = Boolean.parseBoolean(properties.getProperty("autoPlayOnIncomingStream"));
	}

	// Save settings
	public void saveProperties() {
		Properties properties = new Properties();
		properties.setProperty("playbackStepDelay", "" + stepDelayBase);
		properties.setProperty("autoPlayOnIncomingStream", "" + streamAlwaysShowLastOperation);
		try {
			URL url = getClass().getClassLoader().getResource(Const.PROPERTIES_FILE_NAME);
			OutputStream outputStream = new FileOutputStream(new File(url.toURI()));
			properties.store(outputStream, Const.PROGRAM_NAME + " user preferences.");
		} catch (Exception e) {
			propertiesFailed(e);
		}
	}
	/*
	 * End settings
	 */

	/*
	 * How to do sound in JavaFX.
	 */
	private boolean oooooOOoooOOOooooOOoooed = false;

	public void oooooOOoooOOOooooOOooo(Event e) {
		// Sound: https://www.youtube.com/watch?v=inli9ukUKIs
		URL resource = getClass().getResource("/assets/oooooOOoooOOOooooOOooo.mp3");
		Media media = new Media(resource.toString());
		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.play();
		Main.console.info("GET SPoooooOOoooOOOooooOOoooKED!");
		if (!oooooOOoooOOOooooOOoooed) {
			Button spooky = (Button) e.getSource();
			spooky.setBlendMode(BlendMode.SRC_OVER);
			// Image:
			// https://pixabay.com/en/ghost-white-spooky-scary-ghostly-157985/
			Image img = new Image(getClass().getResourceAsStream("/assets/oooooOOoooOOOooooOOooo.png"));
			spooky.setGraphic(new ImageView(img));
			oooooOOoooOOOooooOOoooed = true;
			window.setTitle("SpoooooOOoooOOOooooOOoookster!");
		}
	}

	// Fulhack
	public OperationPanel getOperationPanel() {
		return operationPanel;
	}

	/**
	 * Load an example.
	 * 
	 * @param algo
	 *            The algorithm to run.
	 */
	public void loadExample(Algorithm algo) {
		double[] data = examplesDialog.show(algo.name);
		if (data == null) {
			return;
		}
		Main.console.force("Not implemented yet. Sorry :/");
		Main.console.info("Running " + algo.name + " on: " + Arrays.toString(data));
		String json = Examples.getExample(algo, data);
		if (json != null) {
			lsm.clearData();
			lsm.unwrap(json);
			loadFromLSM();
			lsm.clearData();
		}
	}

	/*
	 * Console controls.
	 */
	public void toggleQuietMode(Event e) {
		CheckBox cb = (CheckBox) e.getSource();
		Main.console.setQuiet(cb.isSelected());
	}

	public void toggleInformation(Event e) {
		CheckBox cb = (CheckBox) e.getSource();
		Main.console.setInfo(cb.isSelected());
	}

	public void toggleError(Event e) {
		CheckBox cb = (CheckBox) e.getSource();
		Main.console.setError(cb.isSelected());
	}

	public void toggleDebug(Event e) {
		CheckBox cb = (CheckBox) e.getSource();
		Main.console.setDebug(cb.isSelected());
	}

	public void clearConsole() {
		Main.console.clear();
	}
	/*
	 * Console controls end.
	 */

	public void dragDropped(DragEvent event) {
		Dragboard db = event.getDragboard();
		boolean hasFiles = db.hasFiles();
		if (hasFiles) {
			for (File file : db.getFiles()) {
				readLog(file);
			}
		}
		event.setDropCompleted(hasFiles);
		event.consume();
	}

	public void dragOver(DragEvent event) {
		Dragboard db = event.getDragboard();
		if (db.hasFiles()) {
			event.acceptTransferModes(TransferMode.COPY);
		} else {
			event.consume();
		}
	}

	public void showHelp() {
		helpView.show();
	}

	/**
	 * Set enable/disable on buttons.
	 */
	public void setButtons() {
		// TODO: Use a property in Model instead.
		// Model clear?
		if (model.isHardCleared()) {
			playPauseButton.setDisable(true);
			forwardButton.setDisable(true);
			// backwardButton.setDisable(true);
			restartButton.setDisable(true);
			clearButton.setDisable(true);
			return;
		}
		boolean forward = !model.tryStepForward();
		playPauseButton.setDisable(forward);
		forwardButton.setDisable(forward);
		boolean backward = !model.tryStepBackward();
		// backwardButton.setDisable(backward);
		restartButton.setDisable(backward);
		clearButton.setDisable(false);
	}
}