package edu.kit.tm.pseprak2.alushare.network.packer;

import android.util.Log;

import edu.kit.tm.pseprak2.alushare.network.coding.DataDecoder;
import edu.kit.tm.pseprak2.alushare.network.protocol.MessagingProtocol;

/**
 * This class handles received packets for a specific data object.
 *
 * @author Albrecht Weiche
 */
public class DataReceiver {
    private static final String TAG = "DataReceiver";

    private final DataDecoder dataDecoder;
    private final MessagingProtocol messagingProtocol;

    private final long dataIdentifier;
    private final int expectedPacketCount;
    private int lastSequenceNum;

    /**
     * Creates a new DataReceiver.
     * @param firstPacket the first received packet
     * @param messagingProtocol the messaging protocol which will be notified when the data object
     *                          is complete.
     */
    public DataReceiver(Packet firstPacket, MessagingProtocol messagingProtocol) {
        this.messagingProtocol = messagingProtocol;
        this.expectedPacketCount = firstPacket.packetCount;
        this.lastSequenceNum = -1;
        this.dataDecoder = messagingProtocol.getDecoder(firstPacket.senderNID);
        this.dataIdentifier = firstPacket.dataIdentifier;

        packetReceived(firstPacket);
    }

    /**
     * Returns the network chat identifier. This method may return null if the identifier was not
     * decoded/received jet.
     *
     * @return null or the network chat identifier.
     */
    public String getNetworkChatID() {
        return dataDecoder.getNetworkChatID();
    }

    /**
     * Adds the data of the packet to the data.
     * The packets should be hand over in the right order.
     *
     * @param packet the new packet
     */
    public void packetReceived(Packet packet) {
        if (packet.sequenceNumber >= expectedPacketCount) {
            Log.e(TAG, "Packet out of bounds!");
        } else if (packet.sequenceNumber != lastSequenceNum + 1) {
            Log.e(TAG, "Wrong packet order! Expected: " + (lastSequenceNum + 1)
                    + " got: " + packet.sequenceNumber);


        } else if (packet.dataIdentifier != dataIdentifier) {
            Log.e(TAG, "Wrong data identifier!!");
        } else  {
            dataDecoder.write(packet.data);
            lastSequenceNum++;
        }

        if (packet.sequenceNumber == expectedPacketCount - 1) {
            String key = MessagingProtocol.keyForPacket(packet);
            messagingProtocol.receiveMessage(dataDecoder.getDecodedData(), key);
        }
    }
}
