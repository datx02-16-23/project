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

import application.Strings;
import interpreter.Interpreter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import manager.LogStreamManager;
import manager.operations.*;

import wrapper.*;

public class StreamSimulator implements Receiver{

	private final LogStreamManager LSM;
	private final ObservableList<Operation> queuedOperations, sentOperations;
	private final SimpleStringProperty nbrQueuedString, nbrSentString, waitingOperationsList, sentOperationsList;
	private final Interpreter interpreter;
	
	public ObservableList<Operation> getQueuedOperations() {
		return queuedOperations;
	}

	private JChannel channel;
	private final int id;
	
	public StreamSimulator(){
		interpreter = new Interpreter();
		nbrQueuedString = new SimpleStringProperty();
		nbrSentString = new SimpleStringProperty();
		waitingOperationsList = new SimpleStringProperty();
		sentOperationsList = new SimpleStringProperty();
		id = (int)(Math.random()*Integer.MAX_VALUE);
		try {
			channel = new JChannel("udp.xml");
			channel.connect(Strings.DEFAULT_CHANNEL);
			channel.setReceiver(this);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		LSM = new LogStreamManager();
		queuedOperations = FXCollections.observableArrayList();
		sentOperations = FXCollections.observableArrayList();
		updateSent();
		
		try {
			LSM.readLog("C:\\Users\\Richard\\Documents\\datx02-16-23\\git\\src\\stream_producer\\bubble.json");
			queuedOperations.addAll(LSM.getOperations());
			updateQueued();
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void transmitOperation(Operation op){
		ArrayList<Operation> operationList = new ArrayList<Operation>();
		operationList.add(op);
		Wrapper message = new Wrapper(null, operationList);
		if(transmit(message)){
			sentOperations.add(op);
			updateSent();
		}
	}
	
	public void clearLists(){
		sentOperations.clear();
		queuedOperations.clear();
		updateSent();
		updateQueued();
	}
	
	int sleepDur = 1500;
	private boolean continousTransmit = false;
	public void continousTransmit(){
		continousTransmit = !continousTransmit;
		
		if (continousTransmit == false){
			return;
		}

		new Thread()
		{
		    public void run() {
		    	while(continousTransmit){
		    		Platform.runLater(new Runnable(){
		    			public void run() {
		    		    	int operation = (int)(Math.random()*5);
		    		    	if(queuedOperations.size() < 2){
		    		    		switch(operation){
						    		case 0:
						    			swapOperation();
						    			break;
						    		case 1:
						    			readOperation();
						    			break;
						    		case 2:
						    			writeOperation();
						    			break;
						    		case 3:
						    			initOperation();
						    			break;
						    		case 4:
						    			messageOperation();
						    			break;
		    		    		}
		    		    	}
			    		transmitFirst();
		    			}
		    			
		    		});
		    		try {
						sleep((int)(sleepDur/Math.sqrt(queuedOperations.size())));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		    	}
		    }
		}.start();
	}

	
	//Getters and setters
	public ObservableList<Operation> getSentOperations() {
		return sentOperations;
	}

	public SimpleStringProperty getNbrQueuedString() {
		return nbrQueuedString;
	}

	public SimpleStringProperty getNbrSentString() {
		return nbrSentString;
	}

	public SimpleStringProperty getWaitingOperationsList() {
		return waitingOperationsList;
	}

	public SimpleStringProperty getSentOperationsList() {
		return sentOperationsList;
	}
	public JChannel getChannel() {
		return channel;
	}
	public int getId() {
		return id;
	}
	
	//Receiver stuff
	
	public boolean transmit(Wrapper wrapper){
		WrapperMessage wm = new WrapperMessage(wrapper, id);
		try {
			channel.send(new Message().setObject(wm));
		} catch (Exception e) {
			return false;
		}
		return true; //Return true if transmit was successful.
	}
	
	public void transmitAll(){
		while(queuedOperations.isEmpty() == false){
			transmitOperation(queuedOperations.remove(0));
		}
		updateQueued();
	}	
	
	public void transmitFirst(){
		if(queuedOperations.isEmpty() == false){
			transmitOperation(queuedOperations.remove(0));
		}
		updateQueued();
	}

	

	public void messageOperation() {
		OP_Message op = new OP_Message();
		op.setMessage("JavaFX is the future!");
		queuedOperations.add(op);
		updateQueued();
	}
	
	public void readOperation(){
		OP_Read op = new OP_Read();
		op.setSource(new Locator("a1", new int[]{1}));
		op.setTarget(new Locator("a1", new int[]{2}));
		op.setValue(new double[]{Math.random()});
		queuedOperations.add(op);
		updateQueued();
	}
	
	public void writeOperation(){
		OP_Write op = new OP_Write();
		op.setSource(new Locator("a2", new int[]{2}));
		op.setTarget(new Locator("a2", new int[]{1}));
		op.setValue(new double[]{Math.random()});
		queuedOperations.add(op);
		updateQueued();
	}
	
	public void initOperation(){
		OP_Init op = new OP_Init();
		op.setTarget(new Locator("a1", new int[]{1}));
		op.setValue(new double[]{
				Math.round(Math.random()*100),Math.round(Math.random()*100),Math.round(Math.random()*100),
				Math.round(Math.random()*100),Math.round(Math.random()*100)
				});
		queuedOperations.add(op);
		updateQueued();
	}
	
	public void swapOperation(){
		OP_Swap op= new OP_Swap();
		op.setVar1((new Locator("a1", new int[]{0})));
		op.setVar2((new Locator("a2", new int[]{0})));
		op.setValues(null);
		queuedOperations.add(op);
		updateQueued();
	}
	
	public void updateQueued(){
		nbrQueuedString.set("#Queued/Received: " + queuedOperations.size());
		
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
	
	public void updateSent(){
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
		
	public void interpret(){
		ArrayList<Operation> toInterpreter = new ArrayList<Operation>();
		interpreter.clearConsoloidatedOperations();
		toInterpreter.addAll(queuedOperations);
		interpreter.setOperations(toInterpreter);
		queuedOperations.clear();
		queuedOperations.addAll(interpreter.getConsolidatedOperations());
		updateQueued();
	}
	
	@Override
	public void receive(Message msg) {
		WrapperMessage wm = (WrapperMessage) msg.getObject();
		
		if (wm.senderId == id){
			return; //Don't receive messages from self.
		}
		
		Platform.runLater(new Runnable(){
			public void run() {
				queuedOperations.addAll(wm.wrapper.body);
				updateQueued();
			}
		});
	}

	@Override
	public void getState(OutputStream output) throws Exception {}

	@Override
	public void setState(InputStream input) throws Exception {}

	@Override
	public void viewAccepted(View new_view) {}

	@Override
	public void suspect(Address suspected_mbr) {}

	@Override
	public void block() {}

	@Override
	public void unblock() {}

}
