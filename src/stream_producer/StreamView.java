package stream_producer;

import java.io.File;

import com.google.gson.GsonBuilder;

import javafx.application.Application;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import manager.datastructures.DataStructure;
import wrapper.Operation;

public class StreamView extends Application {
	private StreamSimulator sm;
	private Dialog<String> inspectDialog, memberDialog;
	private BorderPane variablesView = new BorderPane();
	private BorderPane baseView = new BorderPane();
	private StackPane root;
	
	ObservableList<DataStructure> knowVariablesList;
	@Override
	public void start(Stage primaryStage) throws Exception {
		sm = new StreamSimulator();
		inspectDialog = new Dialog<String>();
		memberDialog = new Dialog<String>();
		root = new StackPane();
	    
		//Construct "Inspect" dialog.
		inspectDialog.setTitle("Inspecting Operation");
		inspectDialog.getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonData.CANCEL_CLOSE));
	    
		//Construct "Members" dialog.
		memberDialog.setTitle("Channel members");
		memberDialog.getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonData.CANCEL_CLOSE));
	    
		//Known Variables view
		Button closeKnownVariablesPane = buildCloseKnownVariablesButton();
		buildKnownVariablesView(closeKnownVariablesPane);
		Button knownVariables = buildKnownVariablesViewButton();
		
		//Labels
		Label waitingOperations = buildWaitingOperationsLabel();
		Label sentOperations = buildSentOperationsLabel();
		
		//Construct lists
		ListView<Operation> waitList = buildWaitList();
		ListView<Operation> sentList = buildSentList();
		
		//Buttons
		Button inspectSent = buildInspectSentOperationButton(sentList);
		Button inspectQueued = buildInspectQueuedOperationButton(waitList);
		Button transmit = buildTransmitButton();
		Button transmitAll = buildTransmitAllButton();
		Button read = buildReadButton();
		Button write = buildWriteButton();
		Button swap = buildSwapButton();
		Button init = buildInitButton();
		Button message = buildMessageButton();
		Button interpret = buildInterpretButton();
		ToggleButton continuousTransmit = buildContinousTransmitButton();
		Button clearLists = buildClearListsButton();
		Button importList = buildImportButton(primaryStage);
		Button exportSent = buildExportSentButton(primaryStage);
		Button exportQueued = buildExportQueuedButton(primaryStage);
		Button showMembers = buildShowMembersButton();

		//Construct stage
        	//Add text fields
        	FlowPane textPane = new FlowPane();
        	textPane.setBorder(null);
	        baseView.setTop(textPane);
	        textPane.getChildren().add(waitingOperations);
	        textPane.getChildren().add(sentOperations);
        
		    //Add operation buttons
	        VBox operationButtons = new VBox();
	        baseView.setLeft(operationButtons);

	        Separator separator1 = new Separator();
	        separator1.setPrefHeight(10);
	        Separator separator2 = new Separator();
	        separator2.setPrefHeight(10);

	        operationButtons.getChildren().addAll(knownVariables,separator1,read,write,swap,
	        										init,message,separator2,interpret);
	        
		    //Add operation buttons
	        VBox controlButtons = new VBox();
	        baseView.setRight(controlButtons);
	        
	        Separator separator3 = new Separator();
	        separator3.setPrefHeight(10);
	        Separator separator4 = new Separator();
	        separator4.setPrefHeight(10);
	        
	        controlButtons.getChildren().addAll(transmit,transmitAll,continuousTransmit,separator3,
	        								clearLists,importList,exportQueued,exportSent,separator4,
	        								showMembers);
	        
	        //Add lists
	        GridPane listPane = new GridPane();
	        baseView.setCenter(listPane);
	        listPane.add(waitingOperations, 0, 0);
	        listPane.add(sentOperations, 1, 0);
	        listPane.add(waitList, 0, 1);
	        listPane.add(sentList, 1, 1);
	        listPane.add(inspectSent, 1, 2);
	        listPane.add(inspectQueued, 0, 2);
        
	        
	    primaryStage.setTitle("id =" + sm.getId() + ", channel = " + sm.getChannelName());
		root.getChildren().add(baseView);
	    Scene scene = new Scene(root, 700, 400);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.show();
	}

	private Button buildExportQueuedButton(Stage primaryStage) {
		Button exportQueued = new Button();
		exportQueued.setText("Export Queued");
		exportQueued.setPrefSize(100,30);
		exportQueued.setTooltip(new Tooltip("Export queued/received operations on a JSON format."));
		exportQueued.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	File f = chooseDirectory(primaryStage);
            	if (f == null){
            		return;
            	}
            	sm.exportQueued(f);
            }
        });
		return exportQueued;
	}

	private Button buildExportSentButton(Stage primaryStage) {
		Button exportSent = new Button();
		exportSent.setText("Export Sent");
		exportSent.setPrefSize(100,30);
		exportSent.setTooltip(new Tooltip("Export sent operations on a JSON format."));
		exportSent.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	File f = chooseDirectory(primaryStage);
            	if (f == null){
            		return;
            	}
            	sm.exportSent(f);
            }
        });
		return exportSent;
	}

	private Button buildImportButton(Stage primaryStage) {
		//Construct "Import "button.
		Button importList = new Button();
		importList.setText("Import");
		importList.setPrefSize(100,30);
		importList.setTooltip(new Tooltip("Import a log file and append it to queued/received operations."));
		importList.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	File f = chooseFile(primaryStage);
            	if (f == null){
            		return;
            	}
            	sm.importList(f);
            }
        });
		return importList;
	}

	private Button buildClearListsButton() {
		//Construct "Continuous Transmit "button.
		Button clearLists = new Button();
		clearLists.setText("Clear");
		clearLists.setPrefSize(100,30);
		clearLists.setTooltip(new Tooltip("Clear lists, without transmitting anything."));
		clearLists.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	sm.clearLists();
            }
        });
		return clearLists;
	}

	private ToggleButton buildContinousTransmitButton() {
		ToggleButton continuousTransmit = new ToggleButton();
		continuousTransmit.setText("Continous\nTransmit");
		continuousTransmit.setPrefSize(100,50);
		continuousTransmit.setTooltip(new Tooltip("Trasmit all operations automatically. Creates new random operations if neccessary."));
		continuousTransmit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	sm.continousTransmit();
            }
        });
		return continuousTransmit;
	}

	private Button buildMessageButton() {
		//Construct "Message" button.
		Button message = new Button();
		message.setText("Message");
		message.setTooltip(new Tooltip("Create a new Message operation and add it to the queue."));
		message.setPrefSize(100,30);
		message.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	sm.messageOperation();
            }
        });
		return message;
	}

	private Button buildInitButton() {
		//Construct "Init" button.
		Button init = new Button();
		init.setText("Init");
		init.setTooltip(new Tooltip("Create a new Init operation and add it to the queue."));
		init.setPrefSize(100,30);
		init.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	sm.initOperation();
            }
        });
		return init;
	}

	private Button buildSwapButton() {
		//Construct "Swap" button.
		Button swap = new Button();
		swap.setText("Swap");
		swap.setTooltip(new Tooltip("Create a new Swap operation and add it to the queue."));
		swap.setPrefSize(100,30);
		swap.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	sm.swapOperation();
            }
        });
		return swap;
	}

	private Button buildWriteButton() {
		//Construct "Write" button.
		Button write = new Button();
		write.setText("Write");
		write.setTooltip(new Tooltip("Create a new Write operation and add it to the queue."));
		write.setPrefSize(100,30);
		write.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	sm.writeOperation();
            }
        });
		return write;
	}

	private Button buildReadButton() {
		//Construct "Read" button.
		Button read = new Button();
		read.setText("Read");
		read.setTooltip(new Tooltip("Create a new Read operation and add it to the queue."));
		read.setPrefSize(100,30);
		read.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	sm.readOperation();
            }
        });
		return read;
	}

	private Button buildTransmitAllButton() {
		//Construct "Transmit All" button.
		Button transmitAll = new Button();
		transmitAll.setText("Transmit All");
		transmitAll.setTooltip(new Tooltip("Transmit all waiting operations."));
		transmitAll.setPrefSize(100,30);
		transmitAll.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	sm.transmitAll();
            }
        });
		return transmitAll;
	}

	private Button buildTransmitButton() {
		//Construct "Transmit" button
		Button transmit = new Button();
		transmit.setText("Transmit");
		transmit.setTooltip(new Tooltip("Transmit the first waiting operation."));
		transmit.setPrefSize(100,30);
		transmit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	sm.transmitFirst();
            }
        });
		return transmit;
	}

	private Label buildSentOperationsLabel() {
		//Construct "sent Operations" label
		Label sentOperations = new Label();
		sentOperations.setPadding(new Insets(0, 5, 0, 5));
		sentOperations.setTooltip(new Tooltip("The number of operations that have been sent so far."));
		sentOperations.textProperty().bind(sm.getNbrSentString());
		return sentOperations;
	}

	private Label buildWaitingOperationsLabel() {
		Label waitingOperations = new Label();
		waitingOperations.setPadding(new Insets(0, 5, 0, 5));
		waitingOperations.setTooltip(new Tooltip("The number of operations waiting to be sent."));
//		waitingOperations.setText("#Waiting: " + getNumberWaitingOperations());
		waitingOperations.textProperty().bind(sm.getNbrQueuedString());
		return waitingOperations;
	}

	private Button buildInspectQueuedOperationButton(ListView<Operation> waitList) {
		Button inspectQueued = new Button();
		inspectQueued.setText("Inspect Queued Operation");
		inspectQueued.setPrefSize(250, 30);
		inspectQueued.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	inspectOperation(waitList);
            }
        });
		return inspectQueued;
	}

	private Button buildInspectSentOperationButton(ListView<Operation> sentList) {
		Button inspectSent = new Button();
		inspectSent.setText("Inspect Sent Operation");
		inspectSent.setPrefSize(250, 30);
		inspectSent.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	inspectOperation(sentList);
            }
        });
		return inspectSent;
	}
	
	private Button buildShowMembersButton() {
		Button showMembers = new Button();
		showMembers.setText("Members");
		showMembers.setPrefSize(100, 30);
		showMembers.setTooltip(new Tooltip("Request a list of the members connected the channel."));
		showMembers.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	showMembers();
            }
        });
		return showMembers;
	}
	
	private void showMembers(){
		memberDialog.showAndWait();
	}

	private Button buildKnownVariablesViewButton() {
		//Construct "Known Variables view" button
		Button knownVariables = new Button();
		knownVariables.setText("Known\nVariables");
		knownVariables.setPrefSize(100, 50);
		knownVariables.setTooltip(new Tooltip("Open the Known Variables view."));
		knownVariables.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	knownVariablesView();
            }
        });
		return knownVariables;
	}

	private void buildKnownVariablesView(Button closeKnownVariablesPane) {
		ListView<DataStructure> knownVariablesListView = new ListView<DataStructure>();
		knownVariablesListView.setPrefWidth(500);
		knownVariablesListView.setPrefHeight(300);
		knowVariablesList = FXCollections.observableArrayList();
		knownVariablesListView.setItems(knowVariablesList);
		variablesView.setLeft(knownVariablesListView);
		variablesView.setTop(new Label("Known Variables"));
		variablesView.setBottom(closeKnownVariablesPane);
	}

	private Button buildCloseKnownVariablesButton() {
		//Contruct "Close" button for knowVariablesPane
		Button closeKnownVariablesPane = new Button();
		closeKnownVariablesPane.setText("Close");
		closeKnownVariablesPane.setPrefSize(100, 50);
		closeKnownVariablesPane.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	root.getChildren().remove(variablesView);
            	root.getChildren().add(baseView);
            }
        });
		return closeKnownVariablesPane;
	}

	private ListView<Operation> buildSentList() {
		//Construct "Sent Operations" list
		ListView<Operation> sentList = new ListView<Operation>();
		sentList.setItems(sm.getSentOperations());
        sentList.setMinWidth(250);
        sentList.setMaxWidth(250);
		sentList.setTooltip(new Tooltip("A list of operations which have been sent."));
		return sentList;
	}

	private ListView<Operation> buildWaitList() {
		//Construct "Waiting Operations" list
		ListView<Operation> waitList = new ListView<Operation>();
		waitList.setItems(sm.getQueuedOperations());
        waitList.setMinWidth(250);
        waitList.setMaxWidth(250);
		waitList.setTooltip(new Tooltip("A list of operations waiting to be sent."));
		return waitList;
	}

	private Button buildInterpretButton() {
		Button interpret = new Button();
		interpret.setText("Interpret");
		interpret.setTooltip(new Tooltip("Attempt to consolidate all queued/received operations."));
		interpret.setPrefSize(100,30);
		interpret.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	sm.interpret();
            }
        });
		return interpret;
	}
	
	private void knownVariablesView(){
		//Construct "Known Variables" dialog
		knowVariablesList.addAll(sm.getKnownVariables());
		root.getChildren().add(variablesView);
		root.getChildren().remove(baseView);
	}
	
	private void inspectOperation(ListView<Operation> sentList){
		Operation op = sentList.getSelectionModel().getSelectedItem();
		
		if (op == null){
			return;
		}
		
		//Construct "Inspect" dialog
		inspectDialog.setHeaderText(op.toString());
		inspectDialog.setContentText(new GsonBuilder().setPrettyPrinting().create().toJson(op));
    	inspectDialog.showAndWait();
	}
	
	public static void main(String[] args)  {
		System.out.println("Launch: main()");
        launch(args);
	}
	
	private File chooseDirectory(Stage stage){
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("Choose Output Directory");
		return dc.showDialog(stage);
		
	}
	
	private File chooseFile(Stage stage){
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
		fileChooser.setTitle("Select Log File");
		return fileChooser.showOpenDialog(stage);
	}
}
