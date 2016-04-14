package io;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import com.google.gson.Gson;

import application.assets.Strings;
import wrapper.Wrapper;

/**
 * Interprocess communication implementation using the JGroups library.
 * 
 * @author Richard
 *
 */
public class JGroupCommunicator extends ReceiverAdapter implements Communicator {

    /**
     * Send messages in native mode (serialised Wrapper).
     */
    public static final short          SENDER_MODE_NATIVE = 0;
    /**
     * Send messages in JSON mode (Wrapper serialised as JSON String).
     */
    public static final short          SENDER_MODE_JSON   = 1;
    /**
     * If true, most incoming messages will be ignored. The messageReceived() method of the listener will be called only
     * if the listener has requested a head count of group members. Useful when the owner of this JGroupCommunicator
     * functions exclusively as the sender.
     */
    public boolean                     suppressIncoming;
    private int                        senderId;
    private short                      senderMode;
    private String                     channel;
    private final List<Wrapper>        incomingQueue;
    private final CommunicatorListener listener;
    private final Gson                 gson;
    private JChannel                   jChannel;
    private final HashSet<Integer>     allTransmitters;

    /**
     * Create a new JGroupCommunicator with a random transmitter id. Connects to the default channel.
     * 
     * @param listener The listener for this JGroupCommunicator.
     */
    public JGroupCommunicator (CommunicatorListener listener){
        this((int) (Math.random() * Integer.MAX_VALUE), Strings.DEFAULT_CHANNEL, listener, false);
    }

    /**
     * Create a new JGroupCommunicator with a random transmitter id. Connects to the default channel.
     * 
     * @param listener The listener for this JGroupCommunicator.
     * @param suppressIncoming If true, most incoming messages will be ignored.
     */
    public JGroupCommunicator (CommunicatorListener listener, boolean suppressIncoming){
        this((int) (Math.random() * Integer.MAX_VALUE), Strings.DEFAULT_CHANNEL, listener, suppressIncoming);
    }

    /**
     * Create a new JGroupCommunicator with the given transmitter id. Connects to the given channel.
     * 
     * @param senderId The transmitter id for this JGroupCommunicator.
     * @param channel The channel to connect to.
     * @param listener The listener for this JGroupCommunicator.
     * @param suppressIncoming If true, most incoming messages will be ignored.
     */
    public JGroupCommunicator (int senderId, String channel, CommunicatorListener listener, boolean suppressIncoming){
        super();
        if (listener == null) {
            throw new IllegalArgumentException("Listener may not be null.");
        }
        this.senderId = senderId;
        this.channel = channel;
        this.listener = listener;
        this.suppressIncoming = suppressIncoming;
        setNativeSenderMode();
        gson = new Gson();
        incomingQueue = new ArrayList<Wrapper>();
        allTransmitters = new HashSet<Integer>();
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
     * 
     * @return The name of the channel this JGroupCommunicator is connected to.
     */
    public String getChannel (){
        return channel;
    }

    /**
     * Change channel this JGroupCommunicator will use to communicate.
     * 
     * @param channel The name of the channel this JGroupCommunicator will use.
     */
    public void setChannel (String channel){
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
     * Set the sender mode of this JGroupCommunicator to Native (serialised Wrapper).
     */
    public void setNativeSenderMode (){
        senderMode = SENDER_MODE_NATIVE;
    }

    /**
     * Set the sender mode of this JGroupCommunicator to JSON (Wrapper serialised as JSON String).
     */
    public void setJSONSenderMode (){
        senderMode = SENDER_MODE_JSON;
    }

    /**
     * Returns the sender mode of this JGroupCommunicator.
     * 
     * @return The sender mode of this JGroupCommunicator.
     */
    public int getSenderMode (){
        return senderMode;
    }

    @Override
    public void receive (Message incoming){
        Object messageObject = incoming.getObject();
        if (messageObject instanceof MavserMessage == false) {
            System.err.println("Invalid message type: " + messageObject);
            return;
        }
        MavserMessage message = (MavserMessage) messageObject;
        if (message.senderId == senderId) {
            return; //Don't process our own messages.
        }
        if (allTransmitters.add(new Integer(message.senderId))) {
            String s = "" + message.senderId;
            allMemberStrings.add(s);
        }
        switch (message.messageType) {
            case MavserMessage.WRAPPER:
                if (suppressIncoming) {
                    return;
                }
                addAndFireEvent((Wrapper) message.payload);
                break;
            case MavserMessage.JSON:
                if (suppressIncoming) {
                    return;
                }
                try {
                    addAndFireEvent(gson.fromJson((String) message.payload, Wrapper.class));
                } catch (Exception e) {
                    System.err.println("JSON String malformed: " + message.payload);
                }
                break;
            case MavserMessage.REQUEST_FOR_MEMBER_INFO:
                Message memberInfo = new Message();
                memberInfo.setObject(new MavserMessage("JGroupCommunicator [ " + getListenerHierarchy() + " ], (id = " + senderId + ")", senderId, MavserMessage.MEMBER_INFO));
                try {
                    jChannel.send(memberInfo);
                } catch (Exception e) {
                    System.err.println("Failed to send member information.");
                }
                break;
            case MavserMessage.MEMBER_INFO:
                if (listenForMemeberInfo) {
                    currentMemberStrings.add((String) message.payload);
                    listener.messageReceived(MavserMessage.MEMBER_INFO);
                }
                break;
            default:
                System.err.println("Invalid message type: " + message.messageType);
                return;
        }
    }

    private boolean listenForMemeberInfo = false;

    /**
     * Enable/disable listening for member info. Used to get a head count of of agents connected to the current channel.
     * Listener will be notified on by a call to messageReceived(MavserMessage.MEMBER_INFO).
     * 
     * @param value True to enable listening. False to disable.
     */
    public void listenForMemberInfo (boolean value){
        listenForMemeberInfo = value;
        if (listenForMemeberInfo == false) {
            currentMemberStrings.clear();
        }
        else {
            Message m = new Message();
            m.setObject(new MavserMessage(null, senderId, MavserMessage.REQUEST_FOR_MEMBER_INFO));
            currentMemberStrings.add("ME: JGroupCommunicator [ " + getListenerHierarchy() + " ], (id = " + senderId + ")");
            listener.messageReceived(MavserMessage.MEMBER_INFO);
            try {
                jChannel.send(m);
            } catch (Exception e) {
                System.err.println("Failed to send REQUEST_FOR_MEMBER_INFO message.");
            }
        }
    }

    private String getListenerHierarchy (){
        if (listener == null) {
            return "null";
        }
        CommunicatorListener theListner = listener;
        StringBuilder sb = new StringBuilder();
        while(theListner != null) {
            sb.append(theListner.getClass().getSimpleName() + "/");
            theListner = theListner.getListener();
        }
        sb.append("null/");
        return sb.toString();
    }

    /**
     * Returns a list of agents connected to the channel.
     * 
     * @return A list of agents connected to the channel.
     */
    private final List<String> currentMemberStrings = new ArrayList<String>();

    public List<String> getMemberStrings (){
        return currentMemberStrings;
    }

    /**
     * Returns a list all agents this JGroupCommunicator has been in contact with.
     * 
     * @return A list all agents this JGroupCommunicator has been in contact with.
     */
    private final List<String> allMemberStrings = new ArrayList<String>();

    public final List<String> getAllMemberStrings (){
        return allMemberStrings;
    }

    /**
     * Returns the first received Wrapper in queue. Returns null if the queue is empty.
     * 
     * @return The first received Wrapper in queue.
     */
    public Wrapper popQueuedMessage (){
        return incomingQueue.remove(0);
    }

    /**
     * Returns the all received Wrappers in queue.
     * 
     * @return The all received Wrappers in queue.
     */
    public List<Wrapper> getAllQueuedMessages (){
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
     * @param outgoing The Wrapper to send.
     */
    public boolean sendWrapper (Wrapper outgoing){
        Message outMessage = new Message();
        if (senderMode == SENDER_MODE_NATIVE) {
            outMessage.setObject(new MavserMessage(outgoing, this.senderId, MavserMessage.WRAPPER));
        }
        else if (senderMode == SENDER_MODE_JSON) {
            outMessage.setObject(new MavserMessage(gson.toJson(outgoing), this.senderId, MavserMessage.JSON));
        }
        else {
            System.err.println("Message could not be sent: Sender mode invalid.");
            return false;
        }
        try {
            jChannel.send(outMessage);
        } catch (Exception e) {
            System.err.println("Message could not be sent: " + e);
            return false;
        }
        return true;
    }

    /**
     * Send the given String to all everyone listening on the current channel. <br>
     * <b>NOTE:</b> JSONString must be a valid serialisation of a Wrapper.
     * 
     * @param JSONString The JSON String to send.
     * @return True if the String was successfully sent. False otherwise.
     */
    public boolean sendString (String JSONString){
        Message m = new Message();
        m.setObject(new MavserMessage(JSONString, senderId, MavserMessage.JSON));
        try {
            jChannel.send(m);
        } catch (Exception e) {
            System.err.println("Message could not be sent: " + e);
            return false;
        }
        return true;
    }

    public boolean sendWrappers (List<Wrapper> outgoing){
        boolean allSuccessful = true;
        for (Wrapper w : outgoing) {
            allSuccessful = allSuccessful && sendWrapper(w);
        }
        return allSuccessful;
    }

    /**
     * Destroy channel and free resources. Should be called before exiting to prevent resource leaks.
     */
    public void close (){
        jChannel.close();
    }

    /**
     * Add a wrapper to the incoming queue and signal listener.
     * 
     * @param w The wrapper to add the the incoming queue.
     */
    private void addAndFireEvent (Wrapper w){
        incomingQueue.add(w);
        listener.messageReceived(MavserMessage.WRAPPER);
    }
}
