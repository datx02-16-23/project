package contract.io;

import java.io.Serializable;
import java.util.List;

import contract.Root;

public interface Communicator {
    /**
     * Returns the first received Wrapper in queue. Returns null if the queue is empty.
     *
     * @return The first received Wrapper in queue.
     */
    public Root popQueuedMessage ();

    /**
     * Returns the all received Wrappers in queue.
     *
     * @return The all received Wrappers in queue.
     */
    public List<Root> getAllQueuedMessages ();

    /**
     * /** Send the given Wrapper to all everyone listening on the current channel.
     *
     * @param outgoing
     *            The Wrapper to send.
     * @return True if message was sent, false otherwise.
     */
    public boolean sendWrapper (Root outgoing);

    /**
     * Send all Wrappers to everyone listening on the current channel.
     *
     * @param outgoing
     *            The list of Wrappers to send.
     * @return True if all wrappers were successfully sent.
     */
    public boolean sendWrappers (List<Root> outgoing);

    /**
     * Close any communications that can cause leaks.
     */
    public void close ();

    /**
     * Send the given String to all everyone listening on the current channel. <br>
     * <b>NOTE:</b> JSONString must be a valid serialisation of a Wrapper.
     *
     * @param JSONString
     *            The JSON String to send.
     * @return True if the String was successfully sent. False otherwise.
     */
    public boolean sendString (String JSONString);

    /*
     * Internal class
     */
    /**
     * Wrapper for messages sent and recevied by implementations of the Communicator interface.
     *
     * @author Richard Sundqvist
     *
     */
    @SuppressWarnings("serial")
    public class CommunicatorMessage implements Serializable {

        /**
         * Message containing a Wrapper for variables and operations.
         */
        public static final short WRAPPER                    = 0;
        /**
         * Message containing a String on the JSON format, which may be deserialized into a Wrapper.
         */
        public static final short JSON                       = 1;
        /**
         * Sent when the Communicator starts to announce it's presence, triggering information
         * requests.
         */
        public static final short HELLO                      = 10;
        /**
         * Request for info about connected channel members.
         */
        public static final short BROADCAST_CHANNEL_CHECK_IN = 11;
        /**
         * Info about the sending member contained as a String in payload.
         */
        public static final short CHECKING_IN                = 12;
        /**
         * Request for info about connected channel members.
         */
        public static final short FIRST_CONTACT              = 13;
        /**
         * Info about the sending member contained as a String in payload.
         */
        public static final short FIRST_CONTACT_ACK          = 14;
        /**
         * They payload for this message. May be null.
         */
        public final Object       payload;
        /**
         * The sender id for this message.
         */
        public final int          senderId;
        /**
         * The message type for this message.
         */
        public final short        messageType;

        /**
         * Construct a new MavserMessage.
         *
         * @param payload
         *            They payload for this message. May be null.
         * @param senderId
         *            The sender id for this message.
         * @param messageType
         *            The message type for this message.
         */
        public CommunicatorMessage (Object payload, int senderId, short messageType) {
            this.payload = payload;
            this.senderId = senderId;
            this.messageType = messageType;
        }
    }
}
