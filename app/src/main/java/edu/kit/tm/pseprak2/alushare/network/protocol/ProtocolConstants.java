package edu.kit.tm.pseprak2.alushare.network.protocol;

public final class ProtocolConstants {

    /**
     * The Charset used to decode Text to byte and vice versa.
     */
    public static final String CHARSET = "UTF-8";

    /**
     * The number of milliseconds the protocol will try to send a packet.
     */
    public static final long PACKET_SEND_TIMEOUT = 60 * 1000;

    // field header
    /**
     * The size of the header of an field.
     * Used for encoding and decoding data objects and chats.
     */
    public static final int FIELD_HEADER_SIZE = (Byte.SIZE + Integer.SIZE) / Byte.SIZE;

    /**
     * The identifier for the receiver field.
     * Used for encoding and decoding chat objects.
     */
    public static final byte RECEIVER = (byte) 'A';

    /**
     * The identifier for the chat network identifier.
     * Used for encoding and decoding data and chat objects.
     */
    public static final byte CHAT_ID = (byte) 'B';

    /**
     * The identifier for the text field.
     * Used for encoding and decoding data objects.
     */
    public static final byte TEXT = (byte) 'C';

    /**
     * The identifier for the filename field.
     * Used for encoding and decoding data objects.
     */
    public static final byte FILENAME = (byte) 'D';

    /**
     * The identifier for the file field.
     * Used for encoding and decoding data objects.
     */
    public static final byte FILE = (byte) 'E';

    /**
     * The identifier for the field which holds the title of a chat.
     * Used for encoding and decoding chat objects.
     */
    public static final byte CHAT_TITLE = (byte) 'F';

    // Packet flags
    /**
     * The identifier for a test packet.
     * This type of packet is only for testing the internetconnection.
     */
    public static final byte PACKET_CONNECTION_TEST = (byte) 0x0;

    /**
     * The identifier for a data packet.
     * This type of packet holds a part of an encoded data object.
     */
    public static final byte PACKET_DATA = (byte) 0x1;

    /**
     * The identifier for a chat packet.
     * This kind of packet is used to notify the receiver about the creation of a new chat. It holds
     * an encoded chat object.
     */
    public static final byte PACKET_NEW_CHAT = (byte) 0x2;

    /**
     * The identifier for a chat packet.
     * This kind of packet is used to notify the receiver about an updated chat. It holds the new
     * chat information. Only packets from the chat admin are relevant.
     */
    public static final byte PACKET_UPDATE_CHAT = (byte) 0x3;

    /**
     * The identifier for a chat packet.
     * This kind of packet is used to notify the receiver about the deletion of a chat. It hold the
     * deleted information.
     */
    public static final byte PACKET_DELETE_CHAT = (byte) 0x4;

    /**
     * The identifier for a chat packet.
     * This packet request more information about a chat. If someone misses a create, update or
     * delete packet, he sends this packet.
     */
    public static final byte PACKET_REQUEST_INFO_CHAT = (byte) 0x5;

    /**
     * The maximal number of trys to send a packet.
     */
    public static final int PACKET_MAX_SEND_TRIES = 3;
    /**
     * The maximal size of a packet.
     */
    public static final int PACKET_MAX_DATA_SIZE = 400 * 1024;
}
