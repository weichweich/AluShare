package edu.kit.tm.pseprak2.alushare.network.protocol;

import edu.kit.tm.pseprak2.alushare.model.Data;

/**
 * This interface is used to handle message events.
 * 
 * @author Albrecht Weiche
 */
public interface MessagingListener {

	/**
	 * This method is called whenever a message was send successfully.
	 *
	 * @param data the data which was send
	 */
	void messageSendSuccess(Data data);
	
	/**
	 * This method is called whenever a message could not be send.
	 *
	 * @param data the data which to send the protocol failed
	 */
	void messageSendFailed(Data data);
	
	/**
	 * This method is called whenever a message was received
	 *
	 * @param data the received data
	 */
	void messageReceived(Data data);
}
