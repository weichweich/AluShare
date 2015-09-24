package edu.kit.tm.pseprak2.alushare.network.protocol;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.packer.Packet;

/**
 * This class notifies a listener aboput the connection progress, checks if the network is online and
 * handles when to send packets.
 *
 * @author Albrecht Weiche
 */
public abstract class NetworkProtocol {
    private static final String TAG = "NetworkProtocol";
    private PacketListener packetListener;
    private Queue<Packet> unhandledPackets = new LinkedList<>();
    private WeakReference<ProtocolListener> wrProtocolListener;
    private final ContactHelper contactHelper;

    /**
     * The time the last test packet was received.
     */
    protected long lastTestPacket = -1;

    /**
     * This interface provides methods to notify about the
     * success or the failure of the dispatch process.
     */
    public interface SendCallback {
        /**
         * Called whenever the sending was successful.
         */
        void sendSuccess();

        /**
         * Called whenever the sending failed.
         */
        void sendFailed();
    }

    /**
     * An interface for listening for new packets.
     */
    public interface PacketListener {

        /**
         * This method is called whenever a new packet is received.
         * @param packet the new received packet.
         */
        void packetReceived(Packet packet);
    }

    /**
     * the current application context.
     */
    protected final Context context;

    /**
     * Initialises a new NetworkProtocol.
     * @param context the current application context
     * @param protocolListener the protocol listener
     */
    public NetworkProtocol(Context context, ProtocolListener protocolListener) {
        this.context = context;
        this.wrProtocolListener = new WeakReference<ProtocolListener>(protocolListener);
        this.contactHelper = HelperFactory.getContacHelper(context);
    }

    /**
     * This method informs the caller if the protocol is connected to the network or not.
     * This method may return true also there is no internet connection.
     *
     * @return true if it is possible to start sending, otherwise false.
     */
    public abstract boolean isConnected();

    /**
     * This method is called if the device has no active network connection.
     * The protocol stops sending.
     */
    public void networkConnectionLost() {
        this.disconnect();
    }

    /**
     * Sends a testpacket. If the packet was successfully send the protocol is connected.
     */
    protected void checkConnected() {
        if (getNetworkID() != null) {
            Packet testPacket = new Packet(ProtocolConstants.PACKET_CONNECTION_TEST,
                    getNetworkID(), getNetworkID());
            dispatchPacket(testPacket, new SendCallback() {
                @Override
                public void sendSuccess() {
                    notifyConnected();
                }

                @Override
                public void sendFailed() {
                    notifyDisconnected();
                }
            });
        }
    }

    /**
     * Connects to the network if possible.
     */
    public abstract void connect();

    /**
     * Disconnects from the network.
     */
    public abstract void disconnect();

    /**
     * Stops the protocol and all processes used by the protocol.
     */
    public abstract void stop();

    /**
     * This method returns the current identifier with which the device is reachable
     * from the network.
     *
     * @return a String object representing the networking identifier.
     */
    public abstract String getNetworkID();

    /**
     * Creates new network chat identifier based on the network identifier and a random string.
     *
     * @return the newly created network chat identifier
     */
    public String createNewNetworkChatID() {
        String randString = Long.toHexString(Double.doubleToLongBits(Math.random()));
        return getNetworkID() + ":" + randString;
    }

    /**
     * Dispatches the given packet. If the packet was successfuly dispatched, the sendSuccess()
     * method of the callback is called, otherwise the sendFailed() method is called.
     *
     * @param packet the packet which should be dispatched
     * @param callback the callback which which notifies about the success or failure of the
     *                 dispatch process
     */
    public void dispatchPacket(Packet packet, final SendCallback callback) {

        packet.sendTries++;
        if (!packet.senderNID.equals(contactHelper.getSelf().getNetworkingId())) {
            Log.e(TAG, "Packet has INVALID SENDER! " + packet.senderNID
                    + " should be " + contactHelper.getSelf().getNetworkingId());
            callback.sendFailed();

        } else if (packet.sendTries > ProtocolConstants.PACKET_MAX_SEND_TRIES) {
            Log.w(TAG, "Packet could NOT be SEND! Tried to OFTEN!");
            callback.sendFailed();

        } else if (!isOnline() || !isConnected()) {
            Log.w(TAG, "Packet could NOT be SEND! NetworkProtocol offline!");
            callback.sendFailed();
            if (isOnline()) {
                this.connect();
            }
        } else {
            this.dispatch(Packet.packetToByte(packet), packet.receiverNID, callback);
        }
    }

    /**
     * This method send a byte array to the given address. After the
     *
     * @param buffer       the bytes to send
     * @param receiversNID the networking identifier of the receiver
     * @param callback     the callback object
     */
    protected abstract void dispatch(byte[] buffer, String receiversNID, SendCallback callback);

    /**
     * This method is called whenever the NetworkProtocol receives a test packet.
     */
    protected void receivedTestPacket() {
        lastTestPacket = System.nanoTime();
        notifyConnected();
    }

    /**
     * This method handles received bytes
     *
     * @param buffer    the received bytes
     * @param senderNID the networking identifier of the sender
     */
    public void received(byte[] buffer, String senderNID) {
        this.received(buffer, 0, buffer.length, senderNID);
    }

    /**
     * This method handles received bytes
     *
     * @param buffer    the received bytes
     * @param offset    the start of the relevant bytes in the byte buffer
     * @param length    the number of relevant bytes in the byte buffer
     * @param senderNID the networking identifier of the sender
     */
    public void received(byte[] buffer, int offset, int length, String senderNID) {
        Packet packet;
        packet = Packet.byteToPacket(buffer, offset, length, this.getNetworkID(), senderNID);

        if (packet.flag == ProtocolConstants.PACKET_CONNECTION_TEST) {
            receivedTestPacket();
            Log.v(TAG, "RECEIVED test packet!!");
        } else if (packetListener == null) {
            unhandledPackets.add(packet);
            Log.w(TAG, "UNHANDLED packet!!");
        } else {
            packetListener.packetReceived(packet);
        }
    }

    /**
     * Notifies the protocol listener that the protocol is ready to send messages.
     */
    protected void notifyConnected() {
        ProtocolListener protocolListener = wrProtocolListener.get();
        if (protocolListener != null) {
            protocolListener.protocolConnected();
        }
    }

    /**
     * Notifies the protocol listener that the protocol is disconnected and wont be able to send messages.
     */
    protected void notifyDisconnected() {
        ProtocolListener protocolListener = wrProtocolListener.get();
        if (protocolListener != null) {
            protocolListener.protocolDisconnected();
        }
    }

    /**
     * Notifies the protocol listener that the protocol was unable to connect to the network.
     */
    protected void notifyConnectionFailed() {
        ProtocolListener protocolListener = wrProtocolListener.get();
        if (protocolListener != null) {
            protocolListener.protocolConnectionFailed();
        }
    }

    /**
     * Notifies the protocol listener about the progress of the connection task.
     *
     * @param progress the progress. an integer between 0 and 100. 0 -> not started, 100 -> finished.
     */
    protected void notifyConnectionProgress(int progress) {
        ProtocolListener protocolListener = wrProtocolListener.get();
        if (protocolListener != null) {
            protocolListener.protocolConnectionProgress(progress);
        }
    }

    /**
     * sets the current protocol listener.
     *
     * @param listener the new protocol listener.
     */
    public void setProtocolListener(ProtocolListener listener) {
        wrProtocolListener = new WeakReference<ProtocolListener>(listener);
    }

    /**
     * Sets the listener which will be notified when a packet arrives.
     *
     * @param packetListener the new listener
     */
    public void setPacketListener(PacketListener packetListener) {
        this.packetListener = packetListener;
        while (!unhandledPackets.isEmpty()) {
            packetListener.packetReceived(unhandledPackets.remove());
        }
    }


    /**
     * This method determines whether or not the device is connected to the internet.
     * @return true if the device has an active internet connection, otherwise false.
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
