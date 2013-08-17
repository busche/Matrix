package de.ismll.messaging;

public interface MessageReciever {

	/**
	 * called if the specified type of message will be broadcasted in the next seconds.
	 * Implementations should return a non-noll String here if the component cannot handle the given type of message at this moment. The String should specify the reason as clearly as possible.
	 * 
	 * @param messageType
	 * @return
	 */
	public String prepareFor(int messageType);

	/**
	 * processes the given message
	 * 
	 * @param messageType
	 * @param m
	 */
	public void process(int messageType, Message m);

}
