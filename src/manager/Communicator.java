package manager;

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
	public boolean send(Wrapper outgoing);
	
	/**
	 * Send all Wrappers to all everyone listening on the current channel.
	 * @param outgoing The list of Wrappers to send.
	 */
	public void sendAll(List<Wrapper> outgoing);
	
	/**
	 * Add a new CommunicatorListener which will be notified when this Communicator accepts a message.
	 * @param newListeners The new CommunicatorListener.
	 */
	public void addListener(CommunicatorListener newListener);
}
