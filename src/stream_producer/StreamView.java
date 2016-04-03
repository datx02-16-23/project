package stream_producer;

import com.google.gson.GsonBuilder;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import wrapper.Operation;

public class StreamView extends Application {
	private StreamSimulator sm;
	private Dialog<String> inspect;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		sm = new StreamSimulator();
		inspect = new Dialog<String>();
		
		StackPane root = new StackPane();
		BorderPane base = new BorderPane();
		
		
		//Construct "Waiting Operations List" list
		ListView<Operation> waitList = new ListView<Operation>();
		waitList.setItems(sm.getQueuedOperations());
        waitList.setMinWidth(250);
        waitList.setMaxWidth(250);
		waitList.setTooltip(new Tooltip("A list of operations waiting to be sent."));

		//Construct "Waiting Operations List" list
		ListView<Operation> sentList = new ListView<Operation>();
		sentList.setItems(sm.getSentOperations());
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
		waitingOperations.textProperty().bind(sm.getNbrQueuedString());

		//Construct "sent Operations" label
		Label sentOperations = new Label();
		sentOperations.setPadding(new Insets(0, 5, 0, 5));
		sentOperations.setTooltip(new Tooltip("The number of operations that have been sent so far."));
//		sentOperations.setText("#sent: " + getNbrsentOperations());
		sentOperations.textProperty().bind(sm.getNbrSentString());
		
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
		
		//Construct "Continuous Transmit "button.
		ToggleButton continuousTransmit = new ToggleButton();
		continuousTransmit.setText("Continous\nTransmit");
		continuousTransmit.setPrefSize(100,50);
		continuousTransmit.setTooltip(new Tooltip("Trasmit all operations automatically. Creates new random operations if neccessary."));
		continuousTransmit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
            	sm.continousTransmit();
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
	        operationButtons.getChildren().add(message);
	        
		    //Add operation buttons
	        VBox controlButtons = new VBox();
	        base.setRight(controlButtons);
	        controlButtons.getChildren().add(transmit);
	        controlButtons.getChildren().add(transmitAll);
	        controlButtons.getChildren().add(continuousTransmit);
	        
	        //Add lists
	        GridPane listPane = new GridPane();
	        base.setCenter(listPane);
	        listPane.add(waitingOperations, 0, 0);
	        listPane.add(sentOperations, 1, 0);
	        listPane.add(waitList, 0, 1);
	        listPane.add(sentList, 1, 1);
	        listPane.add(inspectSent, 1, 2);
	        listPane.add(inspectQueued, 0, 2);
        

	    primaryStage.setTitle("JGroupCommunicator Simulator: (id =" + sm.getId() + ", channel = " + sm.getChannel().getClusterName() + ")");
		root.getChildren().add(base);
	    Scene scene = new Scene(root, 700, 400);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.show();
	}
	
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
	
	public static void main(String[] args)  {
		System.out.println("Launch: main().");
        launch(args);
	}
	
	public void stop(){
		sm.getChannel().close();
	}
}
