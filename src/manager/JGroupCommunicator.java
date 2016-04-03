package manager;

import java.util.ArrayList;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import application.Strings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import wrapper.Wrapper;
import wrapper.WrapperMessage;

/**
 * Interprocess communication implementation using the JGroups library.
 * @author Richard
 *
 */
public class JGroupCommunicator extends ReceiverAdapter implements ListChangeListener<Wrapper>, Communicator{
	/**
	 * Set to True to print debug messages.
	 */
	public boolean debug = false;
	
	private int transmitterId;
	private String channel;
	private int totalReceived, totalRejected, totalAccepted, totalSent;
	
	private final ObservableList<Wrapper> incomingQueue;
	private final List<CommunicatorListener> listeners;
	
	private JChannel jChannel;
	
	/**
	 * Create a new JGroupCommunicator with the given transmitter id. Connects to the given channel.
	 * @param transmitterId The transmitter id for this JGroupCommunicator.
	 * @param channel The channel to connect to.
	 */
	public JGroupCommunicator (int transmitterId, String channel){
		super();
		this.transmitterId = transmitterId;
		this.channel = channel;
		totalReceived = 0;
		totalRejected = 0;
		totalAccepted = 0;
		
		incomingQueue = FXCollections.observableArrayList();
		listeners = new ArrayList<CommunicatorListener>();
		
		try {
			jChannel = new JChannel("udp.xml");
			jChannel.connect(this.channel);
			jChannel.setReceiver(this);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Create a new JGroupCommunicator with a random transmitter id. Connects to the default channel.
	 */
	public JGroupCommunicator(){
		this((int)(Math.random()*Integer.MAX_VALUE), Strings.DEFAULT_CHANNEL);
	}
	
	@Override
	public void receive(Message incoming){
		totalReceived++;
		
		Object messageObject = incoming.getObject();
		if (messageObject instanceof WrapperMessage == false){
			if (debug){
				System.out.println("Invalig message type: " + messageObject);
			}
			totalRejected++;
			return;
		}
		
		WrapperMessage iWM = (WrapperMessage) messageObject;
		
		if (iWM.senderId == transmitterId){
			if (debug){
				System.out.println("Message rejected: sender == receiver.");
				totalRejected++;
			}
			return; //Don't process our own messages.
		}
		
		totalAccepted++;
		incomingQueue.add(iWM.wrapper);
	}
	
	/**
	 * Returns the first received Wrapper in queue. Returns null if the queue is empty.
	 * @return The first received Wrapper in queue.
	 */
	public Wrapper popQueuedMessage(){
		return incomingQueue.remove(0);
	}
	
	/**
	 * Returns the all received Wrappers in queue.
	 * @return The all received Wrappers in queue.
	 */
	public List<Wrapper> getAllQueuedMessages(){
		ArrayList<Wrapper> allQueuedMessages = new ArrayList<Wrapper>();
		while (incomingQueue.isEmpty() == false){
			allQueuedMessages.add(incomingQueue.remove(0));
		}
		return allQueuedMessages;
	}
	/**
	 * Send the given Wrapper to all everyone listening on the current channel.
	 * @param outgoing The Wrapper to send.
	 */
	public void send(Wrapper outgoing){
		Message outMessage = new Message();
		outMessage.setObject(new WrapperMessage(outgoing, this.transmitterId));
		try {
			jChannel.send(outMessage);
		} catch (Exception e) {
			if(debug){
				System.out.println("Message could not be sent: " + e);
				System.out.println(outgoing);
			}
			return;
		}
		totalSent++;
	}
	
	@Override
	public void sendAll(List<Wrapper> outgoing) {
		for(Wrapper w : outgoing){
			send(w);
		}
	}

	/**
	 * Returns the total number of messages received.
	 * @return Total number of messages received.
	 */
	public int getTotalReceived() {
		return totalReceived;
	}

	/**
	 * Returns the total number of messages rejected.
	 * @return Total number of messages rejected.
	 */
	public int getTotalRejected() {
		return totalRejected;
	}

	/**
	 * Returns the total number of messages accepted.
	 * @return Total number of messages accepted.
	 */
	public int getTotalAccepted() {
		return totalAccepted;
	}

	/**
	 * Returns the total number of messages sent.
	 * @return Total number of messages sent.
	 */
	public int getTotalSent() {
		return totalSent;
	}

	@Override
	public void onChanged(ListChangeListener.Change<? extends Wrapper> c) {
		for(CommunicatorListener cl : listeners){
			cl.communicationReceived();
		}
	}

	public void addListner(CommunicatorListener newListener){
		listeners.add(newListener);
	}


}
