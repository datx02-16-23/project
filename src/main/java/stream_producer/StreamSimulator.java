package stream_producer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import interpreter.Interpreter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import manager.CommunicatorListener;
import manager.JGroupCommunicator;
import manager.LogStreamManager;
import manager.datastructures.DataStructure;
import manager.operations.*;

import wrapper.*;

public class StreamSimulator implements CommunicatorListener{

	private final LogStreamManager LSM;
	private final ObservableList<Operation> queuedOperations, sentOperations;
	private final ObservableMap<String, DataStructure> knownVariables;
	private final SimpleStringProperty nbrQueuedString, nbrSentString, waitingOperationsList, sentOperationsList;
	private final Interpreter interpreter;
	
	public ObservableList<Operation> getQueuedOperations() {
		return queuedOperations;
	}

	private final int id;
	
	public StreamSimulator(){
		interpreter = new Interpreter();
		nbrQueuedString = new SimpleStringProperty();
		nbrSentString = new SimpleStringProperty();
		waitingOperationsList = new SimpleStringProperty();
		sentOperationsList = new SimpleStringProperty();
		
		id = (int)(Math.random()*Integer.MAX_VALUE);
		
		LSM = new LogStreamManager();
		LSM.PRETTY_PRINTING = true;
		LSM.setListener(this);
		
		queuedOperations = FXCollections.observableArrayList();
		sentOperations = FXCollections.observableArrayList();
		knownVariables = FXCollections.observableHashMap();
		
		updateSent();
		updateQueued();
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
						sleep((int)(sleepDur/Math.sqrt(1+queuedOperations.size())));
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
	
	public int getId() {
		return id;
	}
	
	//Receiver stuff
	
	public boolean transmit(Wrapper wrapper){
		try {
			LSM.streamWrapper(wrapper);
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
		interpreter.consolidate(queuedOperations);
		updateQueued();
	}
	
	public void importList(File jsonFile){
		LSM.clearData();
		
		LSM.readLog(jsonFile);
		
		queuedOperations.addAll(LSM.getOperations());
		knownVariables.putAll(LSM.getKnownVariables());
		
		updateQueued();
	}
	
	public void exportSent(File targetDir){
		exportList(targetDir, sentOperations);
		
	}
	
	public void exportQueued(File targetDir){
		exportList(targetDir, queuedOperations);
	}
	
	private void exportList(File targetDir, List<Operation> list){
		LSM.setKnownVariables(knownVariables);
		LSM.setOperations(list);
		LSM.printLog(targetDir);
	}

	@Override
	public void messageReceived(short messageType) {
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				//Operations
				queuedOperations.addAll(LSM.getOperations());
				LSM.clearOperations();
				updateQueued();
				
				//Variables
				knownVariables.putAll(LSM.getKnownVariables());
				LSM.clearKnownVariables();
			}
		});
	}
	
	public List<DataStructure> getKnownVariables() {
		ArrayList<DataStructure> ans = new ArrayList<DataStructure>();
		ans.addAll(knownVariables.values());
		return ans;
	}

	public String getChannelName() {
		return ((JGroupCommunicator) LSM.getCommunicator()).getChannel();
	}

	public void stop() {
		LSM.getCommunicator().close();
	}

	@Override
	public CommunicatorListener getListener() {
		return null; //StreamSimulator doesn't have any listeners.
	}

}
