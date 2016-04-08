package manager;

import java.io.Serializable;
import java.util.List;

import wrapper.Wrapper;

public interface Communicator {

	/**
	 * Returns the first received Wrapper in queue. Returns null if the queue is empty.
	 * @return The first received Wrapper in queue.
	 */
	public Wrapper popQueuedMessage();
	
	/**
	 * Returns the all received Wrappers in queue.
	 * @return The all received Wrappers in queue.
	 */
	public List<Wrapper> getAllQueuedMessages();
	
	/**
	/**
	 * Send the given Wrapper to all everyone listening on the current channel.
	 * @param outgoing The Wrapper to send.
	 * @return True if message was sent, false otherwise.
	 */
	public boolean sendWrapper(Wrapper outgoing);
	
	/**
	 * Send all Wrappers to all everyone listening on the current channel.
	 * @param outgoing The list of Wrappers to send.
	 */
	public void sendAll(List<Wrapper> outgoing);
	
	@SuppressWarnings("serial")
	public class MavserMessage implements Serializable{
		
		/**
		 * Message containing a Wrapper for variables and operations.
		 */
		public static final short WRAPPER = 0; 
		/**
		 * Message containing a String on the JSON format, which may be deserialized into a Wrapper.
		 */
		public static final short JSON = 1; 
		/**
		 * Request for info about connected channel members.
		 */
		public static final short REQUEST_FOR_MEMBER_INFO = 11;
		/**
		 * Info about the sending member contained as a String in payload.
		 */
		public static final short MEMBER_INFO = 12;
		
		
		/**
		 * They payload for this message. May be null.
		 */
		public final Object payload;
		/**
		 * The sender id for this message.
		 */
		public final int senderId;
		/**
		 * The message type for this message.
		 */
		public final short messageType;

		/**
		 * Construct a new MavserMessage.
		 * @param payload They payload for this message. May be null.
		 * @param senderId The sender id for this message.
		 * @param messageType The message type for this message.
		 */
		public MavserMessage(Object payload, int senderId, short messageType){
			this.payload = payload;
			this.senderId = senderId;
			this.messageType = messageType;
		}
	}
}
