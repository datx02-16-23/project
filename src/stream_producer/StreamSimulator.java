package stream_producer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

import com.google.gson.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import manager.LogStreamManager;
import manager.operations.*;

import wrapper.*;

public class StreamSimulator extends Application implements Receiver{

	private final LogStreamManager LSM;
	private final ObservableList<Operation> queuedOperations, sentOperations;
	SimpleStringProperty nbrQueuedString = new SimpleStringProperty();
	SimpleStringProperty nbrSentString = new SimpleStringProperty();
	SimpleStringProperty waitingOperationsList = new SimpleStringProperty();
	SimpleStringProperty sentOperationsList = new SimpleStringProperty();

	private JChannel channel;
	private final int id;
	
	public StreamSimulator(){
		id = (int)(Math.random()*Integer.MAX_VALUE);
		try {
			channel = new JChannel("udp.xml");
			channel.connect("mavser");
			channel.setReceiver(this);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		LSM = new LogStreamManager();
		queuedOperations = FXCollections.observableArrayList();
		sentOperations = FXCollections.observableArrayList();
		updateSent();
		
		try {
			LSM.readLog("C:\\Users\\Richard\\Documents\\datx02-16-23\\git\\src\\stream_producer\\init.json");
			queuedOperations.addAll(LSM.getOperations());
			updateQueued();
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void transmitOperation(Operation op){
		ArrayList<Operation> operationList = new ArrayList<Operation>();
		operationList.add(op);
		Wrapper message = new Wrapper(null, operationList);
		if(transmit(message)){
			sentOperations.add(op);
			updateSent();
		}
	}
	
	private boolean transmit(Wrapper wrapper){
		WrapperMessage wm = new WrapperMessage(wrapper, id);
		try {
			channel.send(new Message().setObject(wm));
		} catch (Exception e) {
			return false;
		}
		return true; //Return true if transmit was successful.
	}
	
	private void transmitAll(){
		while(queuedOperations.isEmpty() == false){
			transmitOperation(queuedOperations.remove(0));
		}
		updateQueued();
	}	
	private void transmitFirst(){
		if(queuedOperations.isEmpty() == false){
			transmitOperation(queuedOperations.remove(0));
		}
		updateQueued();
	}

	private void readOperation(){
		OP_Read op = new OP_Read();
		op.setSource(new Locator("a1", new int[]{1}));
		op.setTarget(new Locator("a1", new int[]{2}));
		op.setValue(new double[]{Math.random()});
		queuedOperations.add(op);
		updateQueued();
	}
	
	private void writeOperation(){
		OP_Write op = new OP_Write();
		op.setSource(new Locator("a2", new int[]{2}));
		op.setTarget(new Locator("a2", new int[]{1}));
		op.setValue(new double[]{Math.random()});
		queuedOperations.add(op);
		updateQueued();
	}
	
	private void initOperation(){
		OP_Init op = new OP_Init();
		op.setTarget(new Locator("a1", new int[]{1}));
		op.setValue(new double[]{
				Math.round(Math.random()*100),Math.round(Math.random()*100),Math.round(Math.random()*100),
				Math.round(Math.random()*100),Math.round(Math.random()*100),Math.round(Math.random()*100)
				});
		queuedOperations.add(op);
		updateQueued();
	}
	
	private void swapOperation(){
		OP_Swap op= new OP_Swap();
		op.setVar1((new Locator("a1", new int[]{0})));
		op.setVar2((new Locator("a2", new int[]{0})));
		op.setValues(null);
		queuedOperations.add(op);
		updateQueued();
	}

	
	private void updateQueued(){
		nbrQueuedString.set("#Queued: " + queuedOperations.size());
		
		if (queuedOperations.isEmpty()){
			waitingOperationsList.set("\tNo operations in queue!");
		} else {
			StringBuilder sb = new StringBuilder();
			for(Operation op : queuedOperations){
				sb.append("\t" + op.toSimpleString() + "\n");
			}
			waitingOperationsList.set(sb.toString());			
		}
	}
	
	private void updateSent(){
		nbrSentString.set("#Sent: " + sentOperations.size());
		
		if (sentOperations.isEmpty()){
			sentOperationsList.set("\tNo operations have been sent!");
		} else {
			StringBuilder sb = new StringBuilder();
			for(Operation op : sentOperations){
				sb.append("\t" + op.toSimpleString() + "\n");
			}
			sentOperationsList.set(sb.toString());			
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	Dialog<String> inspect = new Dialog<String>();
	private void inspectOperation(ListView<Operation> sentList){
		Operation op = sentList.getSelectionModel().getSelectedItem();
		
		if (op == null){
			return;
		}
		
		//Construct "Inspect" dialog
		inspect.setHeaderText(op.toString());
		inspect.setContentText(new GsonBuilder().setPrettyPrinting().create().toJson(op));
    	inspect.showAndWait();
	}
	
	
	
	
	
	
	
	
	
	private Stage primaryStage;
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
        
		StackPane root = new StackPane();
		BorderPane base = new BorderPane();
		
		
		//Construct "Waiting Operations List" list
		ListView<Operation> waitList = new ListView<Operation>();
		waitList.setItems(queuedOperations);
        waitList.setMinWidth(250);
        waitList.setMaxWidth(250);
		waitList.setTooltip(new Tooltip("A list of operations waiting to be sent."));

		//Construct "Waiting Operations List" list
		ListView<Operation> sentList = new ListView<Operation>();
		sentList.setItems(sentOperations);
        sentList.setMinWidth(250);
        sentList.setMaxWidth(250);
		sentList.setTooltip(new Tooltip("A list of operations which have been sent."));
	    
		//Construct Inspect dialog.
		inspect.setTitle("Inspecting Operation");
		inspect.getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonData.CANCEL_CLOSE));
	    
		//Construct "Inspect sent operation" button
		Button inspectSent = new Button();
		inspectSent.setText("Inspect Sent Operation");
		inspectSent.setPrefSize(250, 30);
		inspectSent.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	inspectOperation(sentList);
            }
        });
		
		//Construct "Inspect queued operation" button
		Button inspectQueued = new Button();
		inspectQueued.setText("Inspect Queued Operation");
		inspectQueued.setPrefSize(250, 30);
		inspectQueued.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	inspectOperation(waitList);
            }
        });
		
		//Construct "Waiting Operations" label
		Label waitingOperations = new Label();
		waitingOperations.setPadding(new Insets(0, 5, 0, 5));
		waitingOperations.setTooltip(new Tooltip("The number of operations waiting to be sent."));
//		waitingOperations.setText("#Waiting: " + getNumberWaitingOperations());
		waitingOperations.textProperty().bind(nbrQueuedString);

		//Construct "sent Operations" label
		Label sentOperations = new Label();
		sentOperations.setPadding(new Insets(0, 5, 0, 5));
		sentOperations.setTooltip(new Tooltip("The number of operations that have been sent so far."));
//		sentOperations.setText("#sent: " + getNbrsentOperations());
		sentOperations.textProperty().bind(nbrSentString);
		
		//Construct "Transmit" button
		Button transmit = new Button();
		transmit.setText("Transmit");
		transmit.setTooltip(new Tooltip("Transmit the first waiting operation."));
		transmit.setPrefSize(100,30);
		transmit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	transmitFirst();
            }
        });
		
		//Construct "Transmit All" button.
		Button transmitAll = new Button();
		transmitAll.setText("Transmit All");
		transmitAll.setTooltip(new Tooltip("Transmit all waiting operations."));
		transmitAll.setPrefSize(100,30);
		transmitAll.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	transmitAll();
            }
        });
		
		//Construct "Read" button.
		Button read = new Button();
		read.setText("Read");
		read.setTooltip(new Tooltip("Create a new Read operation and add it to the queue."));
		read.setPrefSize(100,30);
		read.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	readOperation();
            }
        });
		
		//Construct "Write" button.
		Button write = new Button();
		write.setText("Write");
		write.setTooltip(new Tooltip("Create a new Write operation and add it to the queue."));
		write.setPrefSize(100,30);
		write.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	writeOperation();
            }
        });
		
		//Construct "Swap" button.
		Button swap = new Button();
		swap.setText("Swap");
		swap.setTooltip(new Tooltip("Create a new Swap operation and add it to the queue."));
		swap.setPrefSize(100,30);
		swap.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	swapOperation();
            }
        });

		//Construct "Init" button.
		Button init = new Button();
		init.setText("Init");
		init.setTooltip(new Tooltip("Create a new Init operation and add it to the queue."));
		init.setPrefSize(100,30);
		init.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	initOperation();
            }
        });
		
		//Construct stage
        
        	//Add text fields
        	FlowPane textPane = new FlowPane();
        	textPane.setBorder(null);
	        base.setTop(textPane);
	        textPane.getChildren().add(waitingOperations);
	        textPane.getChildren().add(sentOperations);
        
		    //Add operation buttons
	        VBox operationButtons = new VBox();
	        base.setLeft(operationButtons);
	        operationButtons.getChildren().add(read);
	        operationButtons.getChildren().add(write);
	        operationButtons.getChildren().add(swap);
	        operationButtons.getChildren().add(init);
	        
		    //Add operation buttons
	        VBox controlButtons = new VBox();
	        base.setRight(controlButtons);
	        controlButtons.getChildren().add(transmit);
	        controlButtons.getChildren().add(transmitAll);
	        
	        //Add lists
	        GridPane listPane = new GridPane();
	        base.setCenter(listPane);
	        listPane.add(waitingOperations, 0, 0);
	        listPane.add(sentOperations, 1, 0);
	        listPane.add(waitList, 0, 1);
	        listPane.add(sentList, 1, 1);
	        listPane.add(inspectSent, 1, 2);
	        listPane.add(inspectQueued, 0, 2);
        

	    primaryStage.setTitle("Stream Simulator: (id =" + id + ", channel = " + channel.getClusterName() + ")");
		root.getChildren().add(base);
	    Scene scene = new Scene(root, 700, 400);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.show();
	}
	
	public static void main(String[] args)  {
		System.out.println("Launch: main().");
        launch(args);
	}

	
	@Override
	public void receive(Message msg) {
		WrapperMessage wm = (WrapperMessage) msg.getObject();
		
		if (wm.senderId == id){
			return; //Don't receive messages from self.
		}
		
		Platform.runLater(new Runnable(){
			public void run() {
				queuedOperations.add(wm.wrapper.body.get(0));
				updateQueued();
			}
		});
	}

	@Override
	public void getState(OutputStream output) throws Exception {
		System.out.println("getState(OutputStream output)");
	}

	@Override
	public void setState(InputStream input) throws Exception {
		System.out.println("setState(InputStream input)");
	}

	@Override
	public void viewAccepted(View new_view) {}

	@Override
	public void suspect(Address suspected_mbr) {
		System.out.println("uspect(Address suspected_mbr)");
	}

	@Override
	public void block() {
		System.out.println("block()");
	}

	@Override
	public void unblock() {
		System.out.println("unblock()");
	}

}
