package stream_producer;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import manager.LogStreamManager;
import manager.operations.OP_Read;
import manager.operations.OP_Swap;
import manager.operations.OP_Write;
import wrapper.Locator;
import wrapper.Operation;

public class StreamSimulator extends Application{

	private final LogStreamManager LSM;
	private final ArrayList<Operation> queuedOperations;
	private final Gson GSON;
	private int transmittedOperations = 0;
	SimpleStringProperty nbrQueuedString = new SimpleStringProperty();
	SimpleStringProperty nbrSentString = new SimpleStringProperty();
	SimpleStringProperty nextOperationString = new SimpleStringProperty();
	
	public StreamSimulator(){
		LSM = new LogStreamManager();
		GSON = new Gson();
		queuedOperations = new ArrayList<Operation>();
		updateSent();
		
		try {
			LSM.readLog("C:\\Users\\Richard\\Documents\\datx02-16-23\\git\\src\\stream_producer\\init.json");
			queuedOperations.addAll(LSM.getOperations());
			updateQueued();
			updatePreview();
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	private void transmitOperation(Operation op){
		String message = GSON.toJson(op);
		if(transmit(message)){
			transmittedOperations++;
			updateSent();
		}
	}
	
	private boolean transmit(String message){
		System.out.println("Transmitting: " + message);
		return true; //Return true if transmit was successful.
	}
	
	private int getNbrTransmittedOperations(){
		return transmittedOperations;
	}
	
	private void transmitAll(){
		int total = queuedOperations.size();
		int soFar = 0;
		while(queuedOperations.isEmpty() == false){
			soFar++;
			System.out.println("Transmitting operation " + soFar + "/" + total +".");
			transmitOperation(queuedOperations.remove(0));
			updatePreview();
			updateQueued();
		}
	}	
	private void transmitFirst(){
		if(queuedOperations.isEmpty() == false){
			transmitOperation(queuedOperations.remove(0));
			System.out.println("Transmitting operation.");
		}
		updateQueued();
		updatePreview();
	}
	
	private int getNumberWaitingOperations(){
		return queuedOperations.size();
	}
	
	private boolean hasWaitingOperations(){
		return !queuedOperations.isEmpty();
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
	
	private void swapOperation(){
		OP_Swap op= new OP_Swap();
		op.setVar1((new Locator("a1", new int[]{0})));
		op.setVar2((new Locator("a2", new int[]{0})));
		op.setValues(null);
		queuedOperations.add(op);
		updateQueued();
	}
	
	private void updatePreview(){
		if (queuedOperations.isEmpty()){
			nextOperationString.set("Next operation:\n" + "No operations in queue!");
		} else {
			nextOperationString.set("Next operation:\n" + queuedOperations.get(0).toSimpleString());			
		}
	}
	
	private void updateQueued(){
		nbrQueuedString.set("#Queued: " + queuedOperations.size());
		if (queuedOperations.size() == 1){
			updatePreview();
		}
	}
	
	private void updateSent(){
		nbrSentString.set("#Sent: " + transmittedOperations);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		//Construct "Operation Preview" label
		Label operationPreview = new Label();
		operationPreview.textProperty().bind(nextOperationString);
		operationPreview.setTooltip(new Tooltip("A preview of the next operation to be transmitted."));
		
		//Construct "Waiting Operations" label
		Label waitingOperations = new Label();
		waitingOperations.setPadding(new Insets(0, 5, 0, 5));
		waitingOperations.setTooltip(new Tooltip("The number of operations waiting to be transmitted."));
//		waitingOperations.setText("#Waiting: " + getNumberWaitingOperations());
		waitingOperations.textProperty().bind(nbrQueuedString);

		//Construct "Transmitted Operations" label
		Label transmittedOperations = new Label();
		transmittedOperations.setPadding(new Insets(0, 5, 0, 5));
		transmittedOperations.setTooltip(new Tooltip("The number of operations that have been transmitted so far."));
//		transmittedOperations.setText("#Transmitted: " + getNbrTransmittedOperations());
		transmittedOperations.textProperty().bind(nbrSentString);
		
		//Construct "Transmit" button
		Button transmit = new Button();
		transmit.setText("Transmit");
		transmit.setTooltip(new Tooltip("Transmit the first waiting operation."));
		transmit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	transmitFirst();
            }
        });
		
		//Construct "Transmit All" button.
		Button transmitAll = new Button();
		transmitAll.setText("Transmit All");
		transmitAll.setTooltip(new Tooltip("Transmit all waiting operations."));
		transmitAll.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	transmitAll();
            }
        });
		
		//Construct "Read" button.
		Button read = new Button();
		read.setText("Read");
		read.setTooltip(new Tooltip("Create a new Read operation and add it to the queue."));
		read.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	readOperation();
            }
        });
		
		//Construct "Write" button.
		Button write = new Button();
		write.setText("Write");
		write.setTooltip(new Tooltip("Create a new Write operation and add it to the queue."));
		write.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	writeOperation();
            }
        });
		
		//Construct "Swap" button.
		Button swap = new Button();
		swap.setText("Swap");
		swap.setTooltip(new Tooltip("Create a new Swap operation and add it to the queue."));
		swap.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	swapOperation();
            }
        });
		
		//Construct stage
        FlowPane root = new FlowPane();
        
        	//Add text fields
	        root.getChildren().add(waitingOperations);
	        root.getChildren().add(transmittedOperations);
        
		    //Add buttons
	        root.getChildren().add(transmit);
	        root.getChildren().add(transmitAll);
	        root.getChildren().add(read);
	        root.getChildren().add(write);
	        root.getChildren().add(swap);
	        root.getChildren().add(operationPreview);
        

	    primaryStage.setTitle("Stream Producer");
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
	}	
	
	public static void main(String[] args)  {
		System.out.println("Launch: main().");
        launch(args);
	}
}
