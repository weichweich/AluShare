package edu.kit.tm.pseprak2.alushare.network.packer;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.coding.DataEncoder;
import edu.kit.tm.pseprak2.alushare.network.protocol.MessagingProtocol;
import edu.kit.tm.pseprak2.alushare.network.protocol.NetworkProtocol;
import edu.kit.tm.pseprak2.alushare.network.protocol.ProtocolConstants;

/**
 * This class handles the packing and sending of an data object.
 *
 * @author Albrecht Weiche
 */
public class DataSender {
    private static final String TAG = "DataSender";

    private final DataEncoder dataEncoder;
    private final Data data;
    private final MessagingProtocol messagingProtocol;
    private final NetworkProtocol networkProtocol;
    private final HashMap<Long, ConcurrentLinkedQueue<Packet>> packetQueueMap;
    private final int sequenceCount;

    private AtomicInteger sequenceNumber = new AtomicInteger(0);
    private float progress = 0;
    private boolean sending = false;

    private DataHelper dataHelper;
    private ContactHelper contactHelper;

    /**
     * Creates a new DataSender which sends the given data object through the network protocol.
     *
     * @param data              the data object which should be send
     * @param messagingProtocol the messaging protocol which should be notified about the success or
     *                          failure of the sending process.
     * @param networkProtocol   the network protocol which should be used to send the packets.
     * @param context           the current application context
     */
    public DataSender(Data data, MessagingProtocol messagingProtocol, NetworkProtocol networkProtocol, Context context) {
        this.messagingProtocol = messagingProtocol;
        this.networkProtocol = networkProtocol;
        this.data = data;
        this.dataEncoder = messagingProtocol.getEncoder(data);
        this.packetQueueMap = new HashMap<>();

        if (dataEncoder.available() % ProtocolConstants.PACKET_MAX_DATA_SIZE > 0) {
            this.sequenceCount = dataEncoder.available() / ProtocolConstants.PACKET_MAX_DATA_SIZE + 1;
        } else {
            this.sequenceCount = dataEncoder.available() / ProtocolConstants.PACKET_MAX_DATA_SIZE;
        }

        for (Contact c : data.getReceivers()) {
            if (data.getState(c).getDataStateType() == DataState.Type.NOT_SENT)
                packetQueueMap.put(c.getId(), new ConcurrentLinkedQueue<Packet>());
        }

        dataHelper = HelperFactory.getDataHelper(context);
        contactHelper = HelperFactory.getContacHelper(context);
    }

    /**
     * Starts to pack the data into packets and hand them over to the network protocol.
     * For every receiver only one packet at a time is dispatched. After the packet was send
     * successfully the next packet will be send.
     *
     * @param blocking true if the method should block until all threads finished, otherwise false.
     */
    public void startSending(boolean blocking) {
        if (!sending) {
            sending = true;
            nextPacket();
            List<Thread> threadList = new ArrayList<>(packetQueueMap.size());
            for (Contact c : data.getReceivers()) {
                final Contact curContact = c;
                if (!data.getState(c).wasSendSuccessful()) {
                    data.getState(curContact).sendingStarted();
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendNext(curContact);
                        }
                    });
                    threadList.add(thread);
                    if (blocking) {
                        thread.run();
                    } else {
                        thread.start();
                    }
                }
            }
            dataHelper.update(data);
            if (blocking) {
                sending = false;
            }
        }
    }

    /**
     * This method gives information about the progress of the sending process.
     *
     * @return an integer between 0 and 100. 0 -> nothing send 100 -> finished sending
     */
    public int getProgress() {
        return (int) progress;
    }

    /**
     * The data object which is currently send.
     *
     * @return a data object
     */
    public Data getData() {
        return data;
    }

    /**
     * Stops sending and sets the state to send failed if the data was not already successfully send.
     */
    public void stopSending() {
        Log.i(TAG, "Stop sending data " + data.getId());
        packetQueueMap.clear();
        data.stopSending();
        dataHelper.update(data);
    }

    /**
     * Stops sending the data object to the given contact. If the data object was already send or is
     * not send to the specified contact, this method does nothing.
     *
     * @param contact the contact which should not receive the message.
     */
    public void stopSending(final Contact contact) {
        Log.i(TAG, "Stop sending data " + data.getId() + " to " + contact.getNetworkingId());
        if (packetQueueMap.containsKey(contact.getId())
                && !data.getState(contact).wasSendSuccessful()) {
            // remove the packet queue. This will stop the thread currently sending packets to the contact
            packetQueueMap.remove(contact.getId());
            // set state for the contact
            data.getState(contact).sendingFailed();
            dataHelper.update(data);
        }
    }

    /**
     * Sends all packets to the given receiver.
     *
     * @param contact the receiver.
     */
    private void sendNext(final Contact contact) {
        final Queue<Packet> packetQueue = packetQueueMap.get(contact.getId());
        if (packetQueue != null) {
            final Semaphore sendSema = new Semaphore(0);
            while (!packetQueue.isEmpty() || nextPacket()) {
                try {
                    final Packet packet = packetQueue.element();
                    networkProtocol.dispatchPacket(packet,
                            new NetworkProtocol.SendCallback() {
                                @Override
                                public void sendSuccess() {
                                    progress += 100 / ((sequenceCount) * data.getReceivers().size());
                                    packetQueue.remove();
                                    sendSema.release();
                                }

                                @Override
                                public void sendFailed() {
                                    Log.e(TAG, "Packet could not be send!!!");
                                    packetQueueMap.remove(contact);
                                    data.getState(contact).sendingFailed();
                                    dataHelper.update(data);
                                    DataSender.this.checkFinish();
                                }
                            });
                    if (sending) {
                        sendSema.acquire();
                    } else {
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }
            data.getState(contact).sendingFinished();
            dataHelper.update(data);
            checkFinish();
        }
    }

    /**
     * puts a new packet in to the packet queues.
     *
     * @return false when no packet was enqueued, otherwise true.
     */
    private boolean nextPacket() {
        int lastSequenceNum;
        if ((lastSequenceNum = sequenceNumber.getAndAdd(1)) >= sequenceCount) {
            if (dataEncoder.available() > 0) {
                Log.e(TAG, "max sequence count reached, but not all data was send!!");
            }
            return false;
        } else if (packetQueueMap.size() == 0) {
            Log.w(TAG, "No packet queues available.");
            return false;
        }

        int packetSize = ProtocolConstants.PACKET_MAX_DATA_SIZE;
        int available = dataEncoder.available();
        if (0 < available && available < packetSize) {
            packetSize = dataEncoder.available();
        } else if (available <= 0) {
            Log.w(TAG, "Sending EMPTY packet!!");
            packetSize = 0;
        }

        byte[] dataBuffer = new byte[packetSize];
        int read = dataEncoder.read(dataBuffer, 0, dataBuffer.length);
        if (read != dataBuffer.length) {
            Log.e(TAG, "Less read than buffer size!");

        }
        for (Contact c : data.getReceivers()) {
            Queue<Packet> packetQueue = packetQueueMap.get(c.getId());
            if (packetQueue != null) {
                packetQueue.add(new Packet(lastSequenceNum, sequenceCount, data.getId(),
                        dataBuffer, c.getNetworkingId(), data.getSender().getNetworkingId()));
            }
        }
        return true;
    }

    /**
     * Checks if the sender has finished. If the data sender has finished the messaging protocol is
     * informed about the ending of the sending process.
     */
    private void checkFinish() {
        boolean finished = true;
        synchronized (data) {
            for (Contact c : data.getReceivers()) {
                finished &= data.getState(c).wasSendSuccessful() || data.getState(c).wasFailedToSend();
            }
        }
        if (finished) {
            sending = false;
            messagingProtocol.senderFinished(data.getId());
        }
    }

    /**
     * Returns whether or not the sender is still sending.
     * @return true if the sender is not sending otherwise false.
     */
    public boolean hasFinished() {
        return !sending;
    }
}
