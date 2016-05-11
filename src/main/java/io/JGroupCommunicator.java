package io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import com.google.gson.Gson;
import application.assets.Strings;
import application.gui.Main;
import wrapper.Wrapper;

/**
 * Interprocess communication implementation using the JGroups library.
 * 
 * @author Richard Sundqvist
 *
 */
public class JGroupCommunicator extends ReceiverAdapter implements Communicator {

	/**
	 * Send messages in native mode (serialised Wrapper).
	 */
	public static final short SENDER_MODE_NATIVE = 0;
	/**
	 * Send messages in JSON mode (Wrapper serialised as JSON String).
	 */
	public static final short SENDER_MODE_JSON = 1;
	/**
	 * If true, most incoming messages will be ignored. The messageReceived()
	 * method of the listener will be called only if the listener has requested
	 * a head count of group members. Useful when the owner of this
	 * JGroupCommunicator functions exclusively as the sender.
	 */
	public final String hierarchy;
	public boolean suppressIncoming;
	private int senderId;
	private short senderMode;
	private String channel;
	private final List<Wrapper> incomingQueue;
	private final CommunicatorListener listener;
	private final Gson gson;
	private JChannel jChannel;
	private final HashMap<Integer, String> allTransmitters;

	/**
	 * Create a new JGroupCommunicator with a random transmitter id. Connects to
	 * the default channel.
	 * 
	 * @param listener
	 *            The listener for this JGroupCommunicator.
	 */
	public JGroupCommunicator(String hierarchy, CommunicatorListener listener) {
		this(hierarchy, (int) (Math.random() * Integer.MAX_VALUE), Strings.DEFAULT_CHANNEL, listener, false);
	}

	/**
	 * Create a new JGroupCommunicator with a random transmitter id. Connects to
	 * the default channel.
	 * 
	 * @param listener
	 *            The listener for this JGroupCommunicator.
	 * @param suppressIncoming
	 *            If true, most incoming messages will be ignored.
	 */
	public JGroupCommunicator(String hierarchy, CommunicatorListener listener, boolean suppressIncoming) {
		this(hierarchy, (int) (Math.random() * Integer.MAX_VALUE), Strings.DEFAULT_CHANNEL, listener, suppressIncoming);
	}

	/**
	 * Create a new JGroupCommunicator with the given transmitter id. Connects
	 * to the given channel.
	 * 
	 * @param hierarchy
	 *            The user hierarchy for this JGroupCommunicator.
	 * @param senderId
	 *            The transmitter id for this JGroupCommunicator.
	 * @param channel
	 *            The channel to connect to.
	 * @param listener
	 *            The listener for this JGroupCommunicator.
	 * @param suppressIncoming
	 *            If true, most incoming messages will be ignored.
	 */
	public JGroupCommunicator(String hierarchy, int senderId, String channel, CommunicatorListener listener,
			boolean suppressIncoming) {
		super();
		if (listener == null) {
			throw new IllegalArgumentException("Listener may not be null.");
		}
		this.hierarchy = "JGroupCommunicator[" + hierarchy + "], id = " + senderId;
		this.senderId = senderId;
		this.channel = channel;
		this.listener = listener;
		this.suppressIncoming = suppressIncoming;
		setNativeSenderMode();
		gson = new Gson();
		incomingQueue = new ArrayList<Wrapper>();
		allTransmitters = new HashMap<Integer, String>();
		try {
			jChannel = new JChannel("udp.xml");
			jChannel.connect(this.channel);
			jChannel.setReceiver(this);
			// Say hello
			Message hello = new Message();
			hello.setObject(new CommunicatorMessage(null, senderId, CommunicatorMessage.HELLO));
			jChannel.send(hello);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Returns the name of the channel this JGroupCommunicator is connected to.
	 * 
	 * @return The name of the channel this JGroupCommunicator is connected to.
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * Change channel this JGroupCommunicator will use to communicate.
	 * 
	 * @param channel
	 *            The name of the channel this JGroupCommunicator will use.
	 */
	public void setChannel(String channel) {
		if (channel == null || channel.equals("")) {
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
	 * Set the sender mode of this JGroupCommunicator to Native (serialised
	 * Wrapper).
	 */
	public void setNativeSenderMode() {
		senderMode = SENDER_MODE_NATIVE;
	}

	/**
	 * Set the sender mode of this JGroupCommunicator to JSON (Wrapper
	 * serialised as JSON String).
	 */
	public void setJSONSenderMode() {
		senderMode = SENDER_MODE_JSON;
	}

	/**
	 * Returns the sender mode of this JGroupCommunicator.
	 * 
	 * @return The sender mode of this JGroupCommunicator.
	 */
	public int getSenderMode() {
		return senderMode;
	}

	@Override
	public void receive(Message incoming) {
		Object messageObject = incoming.getObject();
		if (messageObject instanceof CommunicatorMessage == false) {
			Main.console.err("Invalid message type: " + messageObject);
			return;
		}
		CommunicatorMessage message = (CommunicatorMessage) messageObject;
		if (message.senderId == senderId) {
			return; // Don't process our own messages.
		}
		if (allTransmitters.keySet().contains(new Integer(message.senderId)) == false) {
			requestMemberInfo(CommunicatorMessage.FIRST_CONTACT);
		}
		switch (message.messageType) {
		case CommunicatorMessage.WRAPPER:
			if (suppressIncoming) {
				return;
			}
			addAndFireEvent((Wrapper) message.payload);
			break;
		case CommunicatorMessage.JSON:
			if (suppressIncoming) {
				return;
			}
			try {
				addAndFireEvent(gson.fromJson((String) message.payload, Wrapper.class));
			} catch (Exception e) {
				Main.console.err("JSON String malformed: " + message.payload);
			}
			break;
		default:
			handleInformationExchange((String) message.payload, message.messageType, message.senderId);
			break;
		}
	}

	private void handleInformationExchange(String member_string, short messageType, int senderId) {
		switch (messageType) {
		case CommunicatorMessage.BROADCAST_CHANNEL_CHECK_IN:
			sendMemberInfo(CommunicatorMessage.CHECKING_IN);
			break;
		case CommunicatorMessage.FIRST_CONTACT:
			sendMemberInfo(CommunicatorMessage.FIRST_CONTACT_ACK);
			break;
		case CommunicatorMessage.CHECKING_IN:
			if (listenForMemeberInfo) {
				currentMemberStrings.add(member_string);
				listener.messageReceived(CommunicatorMessage.CHECKING_IN);
			}
			break;
		case CommunicatorMessage.HELLO:
			requestMemberInfo(CommunicatorMessage.FIRST_CONTACT);
			break;
		case CommunicatorMessage.FIRST_CONTACT_ACK:
			allTransmitters.put(new Integer(senderId), member_string);
			break;
		}
	}

	private void requestMemberInfo(short context) {
		Message memberInfo = new Message();
		memberInfo.setObject(new CommunicatorMessage(null, senderId, context));
		try {
			jChannel.send(memberInfo);
		} catch (Exception e) {
		}
	}

	private void sendMemberInfo(short context) {
		Message memberInfo = new Message();
		memberInfo.setObject(new CommunicatorMessage(hierarchy, senderId, context));
		try {
			jChannel.send(memberInfo);
		} catch (Exception e) {
		}
	}

	private boolean listenForMemeberInfo = false;

	/**
	 * Enable/disable listening for member info. Used to get a head count of of
	 * agents connected to the current channel. Listener will be notified on by
	 * a call to messageReceived(MavserMessage.MEMBER_INFO).
	 * 
	 * @param value
	 *            True to enable listening. False to disable.
	 */
	public void listenForMemberInfo(boolean value) {
		listenForMemeberInfo = value;
		if (listenForMemeberInfo == false) {
			currentMemberStrings.clear();
		} else {
			Message m = new Message();
			m.setObject(new CommunicatorMessage(null, senderId, CommunicatorMessage.BROADCAST_CHANNEL_CHECK_IN));
			currentMemberStrings.add("ME: " + hierarchy);
			listener.messageReceived(CommunicatorMessage.CHECKING_IN);
			try {
				jChannel.send(m);
			} catch (Exception e) {
			}
		}
	}

	private final List<String> currentMemberStrings = new ArrayList<String>();

	/**
	 * Returns a list of agents connected to the channel.
	 * 
	 * @return A list of agents connected to the channel.
	 */
	public List<String> getMemberStrings() {
		return currentMemberStrings;
	}

	private final List<String> allMemberStrings = new ArrayList<String>();

	/**
	 * Returns a list all agents this JGroupCommunicator has been in contact
	 * with.
	 * 
	 * @return A list all agents this JGroupCommunicator has been in contact
	 *         with.
	 */
	public final List<String> getAllMemberStrings() {
		return allMemberStrings;
	}

	/**
	 * Returns the first received Wrapper in queue. Returns null if the queue is
	 * empty.
	 * 
	 * @return The first received Wrapper in queue.
	 */
	@Override
	public Wrapper popQueuedMessage() {
		return incomingQueue.remove(0);
	}

	/**
	 * Returns the all received Wrappers in queue, then clears the queue.
	 * 
	 * @return The all received Wrappers in queue.
	 */
	@Override
	public List<Wrapper> getAllQueuedMessages() {
		ArrayList<Wrapper> allQueuedMessages = new ArrayList<Wrapper>();
		if (incomingQueue.isEmpty() == false) {
			allQueuedMessages.addAll(incomingQueue);
			incomingQueue.clear();
		}
		return allQueuedMessages;
	}

	/**
	 * Send the given Wrapper to all everyone listening on the current channel.
	 * 
	 * @param outgoing
	 *            The Wrapper to send.
	 */
	@Override
	public boolean sendWrapper(Wrapper outgoing) {
		Message outMessage = new Message();
		if (senderMode == SENDER_MODE_NATIVE) {
			outMessage.setObject(new CommunicatorMessage(outgoing, this.senderId, CommunicatorMessage.WRAPPER));
		} else if (senderMode == SENDER_MODE_JSON) {
			outMessage
					.setObject(new CommunicatorMessage(gson.toJson(outgoing), this.senderId, CommunicatorMessage.JSON));
		} else {
			Main.console.err("Message could not be sent: Sender mode invalid.");
			return false;
		}
		try {
			jChannel.send(outMessage);
		} catch (Exception e) {
			Main.console.err("Message could not be sent: " + e);
			return false;
		}
		return true;
	}

	/**
	 * Send the given String to all everyone listening on the current channel.
	 * <br>
	 * <b>NOTE:</b> JSONString must be a valid serialisation of a Wrapper.
	 * 
	 * @param JSONString
	 *            The JSON String to send.
	 * @return True if the String was successfully sent. False otherwise.
	 */
	@Override
	public boolean sendString(String JSONString) {
		Message m = new Message();
		m.setObject(new CommunicatorMessage(JSONString, senderId, CommunicatorMessage.JSON));
		try {
			jChannel.send(m);
		} catch (Exception e) {
			Main.console.err("Message could not be sent: " + e);
			return false;
		}
		return true;
	}

	@Override
	public boolean sendWrappers(List<Wrapper> outgoing) {
		boolean allSuccessful = true;
		for (Wrapper w : outgoing) {
			allSuccessful = allSuccessful && sendWrapper(w);
		}
		return allSuccessful;
	}

	/**
	 * Destroy channel and free resources. Should be called before exiting to
	 * prevent resource leaks.
	 */
	@Override
	public void close() {
		jChannel.close();
	}

	/**
	 * Add a wrapper to the incoming queue and signal listener.
	 * 
	 * @param w
	 *            The wrapper to add the the incoming queue.
	 */
	private void addAndFireEvent(Wrapper w) {
		incomingQueue.add(w);
		listener.messageReceived(CommunicatorMessage.WRAPPER);
	}

	public Collection<String> allKnownEntities() {
		return allTransmitters.values();
	}
}
