package edu.kit.tm.pseprak2.alushare.network;

/**
 * This interface specifies methods for listening to the connection process if the network service.
 *
 * @author Albrecht Weiche
 */
public interface NetworkingServiceConnectionListener {

    /**
     * This method is called whenever the networking service is ready for sending data objects.
     */
    void connected();

    /**
     * This method is called whenever the networking service has failed to establish a connection to
     * the network.
     */
    void connectionFailed();

    /**
     * This method notifies about the current connection progress.
     * 0 -> not started
     * 100 -> finished
     * @param progress the progress. An integer between 0 and 100.
     */
    void connectionProgress(int progress);

    /**
     * Notifies the receiver about the creation of a network identifier.
     *
     * @param nid the new network identifier.
     */
    void networkingIDCreated(String nid);
}
