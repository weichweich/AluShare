package edu.kit.tm.pseprak2.alushare.network.packer;

import android.util.Log;

import edu.kit.tm.pseprak2.alushare.network.coding.CodingHelper;
import edu.kit.tm.pseprak2.alushare.network.protocol.ProtocolConstants;

/**
 * A class which represents a packet and provides static methods to convert packets.
 *
 * @author Albrecht Weiche
 */
public class Packet {
    private static final String TAG = "Packet";

    /**
     * The flag specifying the type of the packet.
     */
    public final byte flag;
    private static final int FLAG_SIZE = 1;

    /**
     * the sequence number of the packet.
     */
    public final int sequenceNumber;
    private static final int SEQUENCE_NUMBER_SIZE = 4;

    /**
     * Number of packet required to reconstruct the data object
     */
    public final int packetCount;

    /**
     * the identifier of the data which the packet is part of
     */
    public final long dataIdentifier;
    private static final int DATA_ID_SIZE = 8;

    /**
     * the data which should be send
     */
    public final byte[] data;

    /**
     * the receiver of the packet
     */
    public final String receiverNID;

    /**
     * the sender of the packet
     */
    public final String senderNID;

    /**
     * The number of times the network protocol tries to send the packet.
     */
    public int sendTries = 0;

    /**
     * Creates a new packet with the given information.
     *
     * @param flag the flag of the packet. For possible values see ProtocolConstants
     * @param sequenceNumber the number of the packet in the packet sequence
     * @param packetCount the total number of packets which needs to be send.
     * @param dataIdentifier the identifier of the data which is send through this packet. This field
     *                       is used to group packets.
     * @param data the information which should be send.
     * @param receiverNID the receiver of the packet.
     * @param senderNID the sender of the packet.
     */
    public Packet(byte flag, int sequenceNumber, int packetCount, long dataIdentifier, byte[] data, String receiverNID, String senderNID) {
        this.flag = flag;
        this.sequenceNumber = sequenceNumber;
        this.packetCount = packetCount;

        this.receiverNID = receiverNID;
        this.senderNID = senderNID;

        this.dataIdentifier = dataIdentifier;
        if (data.length > ProtocolConstants.PACKET_MAX_DATA_SIZE) {
            Log.e(TAG, "Packet size is too big!! " + data.length + " > " + ProtocolConstants.PACKET_MAX_DATA_SIZE);
        }
        this.data = data;
    }

    /**
     * Creates a new packet with the required information.
     *
     * @param sequenceNumber the sequenceNumber
     * @param dataIdentifier the identifier of the data the packet is part of
     * @param data           the part of the data object which should be send through this packet.
     *                       The length of data must not be bigger than <code>PACKET_MAX_DATA_SIZE</code>
     * @param receiverNID    the receiver of the packet
     * @param senderNID      the sender of the packet
     */
    public Packet(int sequenceNumber, int packetCount, long dataIdentifier, byte[] data,
                  String receiverNID, String senderNID) throws IllegalArgumentException {
        this.flag = ProtocolConstants.PACKET_DATA;
        this.sequenceNumber = sequenceNumber;
        this.packetCount = packetCount;

        this.receiverNID = receiverNID;
        this.senderNID = senderNID;

        this.dataIdentifier = dataIdentifier;
        if (data.length > ProtocolConstants.PACKET_MAX_DATA_SIZE) {
            throw new IllegalArgumentException("Packet data is to big (Max is:"
                    + ProtocolConstants.PACKET_MAX_DATA_SIZE + ", actual length: " + data.length + ")");
        }
        this.data = data;
    }

    /**
     * Creates a new packet with the given information.
     *
     * @param flag the flag of the packet. For possible values see ProtocolConstants
     * @param data the information which should be send.
     * @param receiverNID the receiver of the packet.
     * @param senderNID the sender of the packet.
     */
    public Packet(byte flag, byte[] data, String receiverNID, String senderNID) {
        this.flag = flag;
        this.sequenceNumber = 0;
        this.packetCount = 1;

        this.receiverNID = receiverNID;
        this.senderNID = senderNID;

        this.dataIdentifier = 0;
        this.data = data;
    }

    /**
     * Creates a new packet with the given information.
     *
     * @param flag the flag of the packet. For possible values see ProtocolConstants
     * @param receiverNID the receiver of the packet.
     * @param senderNID the sender of the packet.
     */
    public Packet(byte flag, String receiverNID, String senderNID) {
        this.flag = flag;
        this.sequenceNumber = 0;
        this.packetCount = 1;

        this.receiverNID = receiverNID;
        this.senderNID = senderNID;

        this.dataIdentifier = 0;
        this.data = new byte[0];
    }

    /**
     * Converts a packet to a byte array.
     *
     * @param packet the packet which should be encoded.
     * @return the encoded packet in a byte array
     */
    static public byte[] packetToByte(Packet packet) {

        // sequence number + packet count + data identifier + data length
        byte[] packetBytes = new byte[FLAG_SIZE + SEQUENCE_NUMBER_SIZE + SEQUENCE_NUMBER_SIZE + DATA_ID_SIZE + packet.data.length];
        int copyCount = 0;
        byte[] intBuffer = new byte[SEQUENCE_NUMBER_SIZE];

        packetBytes[copyCount++] = packet.flag;

        CodingHelper.intTo4Byte(packet.sequenceNumber, intBuffer, 0);
        System.arraycopy(intBuffer, 0, packetBytes, copyCount, SEQUENCE_NUMBER_SIZE);
        copyCount += SEQUENCE_NUMBER_SIZE;

        CodingHelper.intTo4Byte(packet.packetCount, intBuffer, 0);
        System.arraycopy(intBuffer, 0, packetBytes, copyCount, SEQUENCE_NUMBER_SIZE);
        copyCount += SEQUENCE_NUMBER_SIZE;

        byte[] longBuffer = new byte[DATA_ID_SIZE];
        CodingHelper.longTo8Byte(packet.dataIdentifier, longBuffer, 0);
        System.arraycopy(longBuffer, 0, packetBytes, copyCount, DATA_ID_SIZE);
        copyCount += DATA_ID_SIZE;

        System.arraycopy(packet.data, 0, packetBytes, copyCount, packet.data.length);

        return packetBytes;
    }

    /**
     * Decodes a byte array to a packet.
     *
     * @param packetBuffer the byte array holding the information about the packet.
     * @param receiverNID the receiver of the packet
     * @param senderNID the sender of the packet
     * @return the decoded packet
     */
    static public Packet byteToPacket(byte[] packetBuffer, String receiverNID, String senderNID) {
        return byteToPacket(packetBuffer, 0, packetBuffer.length, receiverNID, senderNID);
    }

    /**
     * Decodes a byte array to a packet.
     *
     * @param packetBuffer the byte array holding the information about the packet.
     * @param offset the start of the relevant bytes.
     * @param length the number of relevant bytes.
     * @param receiverNID the receiver of the packet
     * @param senderNID the sender of the packet
     * @return the decoded packet
     */
    static public Packet byteToPacket(byte[] packetBuffer, int offset, int length,
                                      String receiverNID, String senderNID) {
        int readCounter = offset;

        byte flag = packetBuffer[readCounter++];

        int sequenceNumber = CodingHelper.intFromBuffer(packetBuffer, readCounter);
        readCounter += SEQUENCE_NUMBER_SIZE;

        int sequenceCount = CodingHelper.intFromBuffer(packetBuffer, readCounter);
        readCounter += SEQUENCE_NUMBER_SIZE;

        long dataIdentifier = CodingHelper.longFromBuffer(packetBuffer, readCounter);
        readCounter += DATA_ID_SIZE;

        byte[] dataBuffer = new byte[length - readCounter];
        System.arraycopy(packetBuffer, readCounter, dataBuffer, 0, dataBuffer.length);

        return new Packet(flag, sequenceNumber, sequenceCount, dataIdentifier, dataBuffer, receiverNID, senderNID);
    }
}
