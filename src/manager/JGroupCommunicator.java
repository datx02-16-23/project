package manager;

import java.util.ArrayList;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import com.google.gson.Gson;

import application.Strings;
import wrapper.Wrapper;
import wrapper.WrapperMessage;
import wrapper.StringMessage;

/**
 * Interprocess communication implementation using the JGroups library.
 * @author Richard
 *
 */
public class JGroupCommunicator extends ReceiverAdapter implements Communicator{
	/**
	 * Set to True to print debug messages.
	 */
	public boolean debug = true;
	
	/**
	 * Send messages in native mode (serialised Wrapper).
	 */
	public static final int SENDER_MODE_NATIVE = 0;
	/**
	 * Send messages in JSON mode (Wrapper serialised as JSON String).
	 */
	public static final int SENDER_MODE_JSON = 1;
	
	private int transmitterId;
	private String channel;
	private int totalReceived, totalRejected, totalAccepted, totalSent;
	
	private final List<Wrapper> incomingQueue;
	private final List<CommunicatorListener> listeners;
	
	private JChannel jChannel;
	
	private int senderMode;
	
	Gson gson;
	
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
		totalSent = 0;
		setNativeSenderMode();
		
		gson = new Gson();
		incomingQueue = new ArrayList<Wrapper>();
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
	 * Set the sender mode of this JGroupCommunicator to Native (serialised Wrapper). 
	 */
	public void setNativeSenderMode(){
		senderMode = SENDER_MODE_NATIVE;
	}
	
	/**
	 * Set the sender mode of this JGroupCommunicator to JSON (Wrapper serialised as JSON String).
	 */
	public void setJSONSenderMode(){
		senderMode = SENDER_MODE_JSON;
	}
	

	/**
	 * Returns the sender mode of this JGroupCommunicator.
	 * @return The sender mode of this JGroupCommunicator.
	 */
	public int getSenderMode(){
		return senderMode;
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
		
		//Handle Wrapper case.
		if (messageObject instanceof WrapperMessage){
			WrapperMessage wm = (WrapperMessage) messageObject;
			
			if(senderIsSelf(wm.senderId)){
				return;  //Don't process our own messages.
			}
			
			addAndFireEvent(wm.wrapper);
			return;
		}
		
		//Handle String (assume gson) case
		if (messageObject instanceof StringMessage){
			StringMessage sm = (StringMessage) messageObject;
			
			if(senderIsSelf(sm.senderId)){
				return;  //Don't process our own messages.
			}
			
			Wrapper wrapper;
			try{
				wrapper = gson.fromJson(sm.gsonString, Wrapper.class);
			} catch (Exception e){
				if (debug){
					System.out.println("Gson failed to parse String: " + sm.gsonString);
				}
				totalRejected++;
				return;
			}
			addAndFireEvent(wrapper);
			return;
		}
		
		//Reject message
		if (debug){
			System.out.println("Invalid message type: " + messageObject);
		}
		totalRejected++;
	}
	
	private boolean senderIsSelf(int senderId){
		if (senderId == transmitterId){
			if (debug){
				System.out.println("Message rejected: sender == receiver.");
			}
			totalRejected++;
			return true;
		}
		return false;
	}
	
	private void addAndFireEvent(Wrapper w){
		totalAccepted++;
		incomingQueue.add(w);
		for(CommunicatorListener cl : listeners){
			cl.communicationReceived();
		}
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
	public boolean send(Wrapper outgoing){
		Message outMessage = new Message();
		
		if (senderMode == SENDER_MODE_NATIVE){
			outMessage.setObject(new WrapperMessage(outgoing, this.transmitterId));			
		} else if (senderMode == SENDER_MODE_JSON){
			outMessage.setObject(new StringMessage(gson.toJson(outgoing), this.transmitterId));			
		} else {
			if(debug){
				System.out.println("Message could not be sent: Sender mode invalid.");
				System.out.println(outgoing);
			}
			return false;
		}
		
		
		try {
			jChannel.send(outMessage);
		} catch (Exception e) {
			if(debug){
				System.out.println("Message could not be sent: " + e);
				System.out.println(outgoing);
			}
			return false;
		}
		totalSent++;
		return true;
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

	public void addListener(CommunicatorListener newListener){
		listeners.add(newListener);
	}

	/**
	 * Destroy channel and free resources. Should be called before exiting to prevent resource leaks.
	 */
	public void close(){
		jChannel.close();
	}

}
