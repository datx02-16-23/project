package manager;

import java.util.ArrayList;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import com.google.gson.Gson;

import assets.Strings;
import wrapper.Wrapper;

/**
 * Interprocess communication implementation using the JGroups library.
 * @author Richard
 *
 */
public class JGroupCommunicator extends ReceiverAdapter implements Communicator{
	
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
	private boolean transmitOnly;
	
	private final List<Wrapper> incomingQueue;
	private final CommunicatorListener listener;
	
	private JChannel jChannel;
	
	private int senderMode;
	
	Gson gson;
	
	
	/**
	 * Create a new JGroupCommunicator with a random transmitter id. Connects to the default channel.
	 * @param listener The listener for this JGroupCommunicator.
	 */
	public JGroupCommunicator(CommunicatorListener listener){
		this((int)(Math.random()*Integer.MAX_VALUE), Strings.DEFAULT_CHANNEL, listener, false);
	}
	
	/**
	 * Create a new JGroupCommunicator with a random transmitter id. Connects to the default channel.
	 * @param listener The listener for this JGroupCommunicator.
	 * @param transmitOnly If true, most incoming messages will be ignored.
	 */
	public JGroupCommunicator(CommunicatorListener listener, boolean transmitOnly){
		this((int)(Math.random()*Integer.MAX_VALUE), Strings.DEFAULT_CHANNEL, listener, transmitOnly);
	}
	
	/**
	 * Create a new JGroupCommunicator with the given transmitter id. Connects to the given channel.
	 * @param transmitterId The transmitter id for this JGroupCommunicator.
	 * @param channel The channel to connect to.
	 * @param listener The listener for this JGroupCommunicator.
	 * @param transmitOnly If true, most incoming messages will be ignored.
	 */
	public JGroupCommunicator (int transmitterId, String channel, CommunicatorListener listener, boolean transmitOnly){
		super();
		this.transmitterId = transmitterId;
		this.channel = channel;
		this.listener = listener;
		this.transmitOnly = transmitOnly;
		setNativeSenderMode();
		
		gson = new Gson();
		incomingQueue = new ArrayList<Wrapper>();
		
		try {
			jChannel = new JChannel("udp.xml");
			jChannel.connect(this.channel);
			jChannel.setReceiver(this);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Returns the name of the channel this JGroupCommunicator is connected to.
	 * @return The name of the channel this JGroupCommunicator is connected to.
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Change channel this JGroupCommunicator will use to communicate.
	 * @param channel The name of the channel this JGroupCommunicator will use.
	 */
	public void setChannel(String channel) {
		if (channel == null || channel.equals("")){
			throw new IllegalArgumentException("(channel == null || channel.equals(\"\")) != false");
		}
		
		this.channel = channel;
			try {
				jChannel.disconnect();
				jChannel.connect(this.channel);
			} catch (Exception e) {
				e.printStackTrace();
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

	@Override
	public void receive(Message incoming){
		
		Object messageObject = incoming.getObject();
		
		if (messageObject instanceof MavserMessage == false){
			System.err.println("Invalid message type: " + messageObject);
			return;
		}
		
		MavserMessage message = (MavserMessage) messageObject;
			
		if(message.senderId == transmitterId){
			return;  //Don't process our own messages.
		}
		
		switch(message.messageType){
			case MavserMessage.WRAPPER:
				if(transmitOnly){
					return;
				}
				addAndFireEvent((Wrapper) message.payload);
			break;
				
			case MavserMessage.JSON:
				if(transmitOnly){
					return;
				}
				try{
					addAndFireEvent(gson.fromJson((String) message.payload, Wrapper.class));
				} catch (Exception e){
					System.err.println("JSON String malformed: " + message.payload);
				}
			break;
			
			case MavserMessage.REQUEST_FOR_MEMBER_INFO:
				String myListener = listener == null ? "null" :  listener.getClass().getSimpleName();
				Message messageReply = new Message();
				messageReply.setObject(new MavserMessage("JGroupCommunicator [" + myListener +"], (id = " + transmitterId + ")", transmitterId, MavserMessage.MEMBER_INFO));
			try {
				jChannel.send(messageReply);
			} catch (Exception e) {
				System.err.println("Failed to send member information.");
			}
			break;
			
			case MavserMessage.MEMBER_INFO:
				if (listenForMemeberInfo){
					memberStrings.add((String) message.payload);
					listener.messageReceived(MavserMessage.MEMBER_INFO);
				}
			break;
			
			default:
				System.err.println("Invalid message type: " + message.messageType);
				return;
		}

	}
	
	private boolean listenForMemeberInfo = false;
	public void listenForMemberInfo(boolean value){
		listenForMemeberInfo = value;
		if (listenForMemeberInfo == false){
			memberStrings.clear();
		}
	}
	
	private final List<String> memberStrings = new ArrayList<String>();
	public List<String> getMemberStrings(){
		return memberStrings;
	}
	
	private void addAndFireEvent(Wrapper w){
		incomingQueue.add(w);
		listener.messageReceived(MavserMessage.WRAPPER);
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
		if (incomingQueue.isEmpty() == false){
			allQueuedMessages.addAll(incomingQueue);
		}
		incomingQueue.clear();
		return allQueuedMessages;
	}
	/**
	 * Send the given Wrapper to all everyone listening on the current channel.
	 * @param outgoing The Wrapper to send.
	 */
	public boolean sendWrapper(Wrapper outgoing){
		Message outMessage = new Message();
		
		if (senderMode == SENDER_MODE_NATIVE){
			outMessage.setObject(new MavserMessage(outgoing, this.transmitterId, MavserMessage.WRAPPER));			
		} else if (senderMode == SENDER_MODE_JSON){
			outMessage.setObject(new MavserMessage(gson.toJson(outgoing), this.transmitterId, MavserMessage.JSON));			
		} else {
			System.err.println("Message could not be sent: Sender mode invalid.");
			return false;
		}
		
		
		try {
			jChannel.send(outMessage);
		} catch (Exception e) {
			System.err.println("Message could not be sent: " + e);
			System.err.println(outgoing);
			return false;
		}
		return true;
	}
	
	@Override
	public void sendAll(List<Wrapper> outgoing) {
		for(Wrapper w : outgoing){
			sendWrapper(w);
		}
	}

	/**
	 * Destroy channel and free resources. Should be called before exiting to prevent resource leaks.
	 */
	public void close(){
		jChannel.close();
	}

}
