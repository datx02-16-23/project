package io;

public interface CommunicatorListener {
	/**
	 * Called when a Cummunicator receives a message.
	 * @param messageType The type of message received.
	 */
	public void messageReceived(short messageType);
	
	/**
	 * Returns any parent CommunicatorListener attached to this CommunicatorListener, if applicable.
	 * @return A CommunicatorListener if applicable, null otherwise.
	 */
	public CommunicatorListener getListener();
}
