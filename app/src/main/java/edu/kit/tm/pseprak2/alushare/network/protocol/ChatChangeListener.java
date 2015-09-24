package edu.kit.tm.pseprak2.alushare.network.protocol;

import edu.kit.tm.pseprak2.alushare.model.Chat;

/**
 * An interface for listening to change notifications for chats. This methods will be called be the
 * messaging protocol whenever the corresponding packet is received.
 *
 * @author Albrecht Weiche
 */
public interface ChatChangeListener {

    /**
     * Called whenever a create chat packet was received.
     *
     * @param chat the received chat which should be created.
     */
    void receivedInsert(Chat chat);

    /**
     * Called whenever an update chat packet was received.
     *
     * @param chat the updated chat.
     */
    void receivedUpdate(Chat chat);

    /**
     * Called whenever a remove chat packet was received.
     *
     * @param chat the chat which should be altered.
     * @param nid the networking identifier of the sender.
     */
    void receivedDelete(Chat chat, String nid);
}
