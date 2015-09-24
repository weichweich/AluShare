package edu.kit.tm.pseprak2.alushare.network.protocol;

/**
 * An interface for listening to the connection state of the protocol.
 *
 * @author Albrecht Weiche
 */
public interface ProtocolListener {

    /**
     * This method is called whenever the protocol establishes a connection to the network.
     */
    void protocolConnected();

    /**
     * This method is called whenever the protocol loses the connection to the network.
     * Notice: it is not guaranteed that the protocol reports disconnects immediately.
     */
    void protocolDisconnected();

    /**
     * This method is called whenever the protocol fails to connect to the network.
     */
    void protocolConnectionFailed();

    /**
     * This method reports the progress of the connection process.
     * @param progress the progress in percentages
     */
    void protocolConnectionProgress(int progress);
}
