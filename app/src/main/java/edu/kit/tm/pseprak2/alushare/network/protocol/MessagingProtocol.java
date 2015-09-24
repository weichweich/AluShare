package edu.kit.tm.pseprak2.alushare.network.protocol;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.ChatHandler;
import edu.kit.tm.pseprak2.alushare.network.DataHandler;
import edu.kit.tm.pseprak2.alushare.network.coding.ChatCoder;
import edu.kit.tm.pseprak2.alushare.network.coding.DataDecoder;
import edu.kit.tm.pseprak2.alushare.network.coding.DataEncoder;
import edu.kit.tm.pseprak2.alushare.network.coding.StreamDataDecoder;
import edu.kit.tm.pseprak2.alushare.network.coding.StreamDataEncoder;
import edu.kit.tm.pseprak2.alushare.network.packer.DataReceiver;
import edu.kit.tm.pseprak2.alushare.network.packer.DataSender;
import edu.kit.tm.pseprak2.alushare.network.packer.Packet;

/**
 * An abstract class which manages the network connection. This class handles disconnects and
 * sends data.
 *
 * @author Albrecht Weiche
 */
public class MessagingProtocol implements NetworkProtocol.PacketListener {
    private static final String TAG = "MessagingProtocol";
    private static boolean sendBlocking = false;

    private static final NetworkProtocol.SendCallback dummyCallback = new NetworkProtocol.SendCallback() {
        @Override
        public void sendSuccess() {
        }

        @Override
        public void sendFailed() {
        }
    };
    /**
     * the current application context.
     */
    protected final Context context;
    /**
     * A hashmap containing the DataSender
     */
    protected final ConcurrentHashMap<Long, DataSender> dataSenderMap;
    /**
     * A hashmap containing the DataReceiver
     */
    protected final ConcurrentHashMap<String, DataReceiver> dataReceiverMap;
    /**
     * a reference to the messaging listener. May be null
     */
    protected MessagingListener messageListener;
    /**
     * the listener for received messages.
     */
    protected ChatChangeListener chatChangeListener;

    private NetworkProtocol networkProtocol;
    private String ownNID;

    /**
     * Initiats a MessagingProtocol
     *
     * @param appContext the current application context
     */
    public MessagingProtocol(Context appContext, NetworkProtocol networkProtocol) {
        this.context = appContext;
        this.networkProtocol = networkProtocol;

        dataSenderMap = new ConcurrentHashMap<>();
        dataReceiverMap = new ConcurrentHashMap<>();

        ownNID = HelperFactory.getContacHelper(context).getSelf().getNetworkingId();

        chatChangeListener = new ChatHandler(appContext, this);

        messageListener = new DataHandler(appContext, this);
    }

    /**
     * The number of data objects which are currently in the sending queue.
     *
     * @return an integer representing the number of messages in the queue.
     */
    public int sendingDataCount() {
        return dataSenderMap.size();
    }

    /**
     * Creates a new DataSender object and starts the sending process.
     *
     * @param data the data object to send
     */
    public void sendMessage(final Data data) {
        final DataSender sender = new DataSender(data, this, networkProtocol, context);
        DataSender oldSender = dataSenderMap.putIfAbsent(data.getId(), sender);
        if (oldSender == null) {
            sender.startSending(sendBlocking);
        } else if (oldSender.hasFinished()) {
            dataSenderMap.put(data.getId(), sender);
            sender.startSending(sendBlocking);
        }
    }

    /**
     * Sends the signal to create a new chat to all receivers in the chat.
     *
     * @param chat the chat which was created and should be broadcast.
     */
    public void sendCreateChat(Chat chat) {
        String ownNID = HelperFactory.getContacHelper(context).getSelf().getNetworkingId();

        for (Contact c : chat.getReceivers()) {
            if (!c.getNetworkingId().equals(ownNID)) {
                Packet createPacket = new Packet(ProtocolConstants.PACKET_NEW_CHAT,
                        ChatCoder.chatToByte(chat), c.getNetworkingId(), ownNID);
                networkProtocol.dispatchPacket(createPacket, dummyCallback);
            }
        }
    }

    /**
     * Informs all receiver in the chat about the changes which where made to the chat.
     *
     * @param chat the changed chat.
     */
    public void sendUpdateChat(Chat chat) {
        for (Contact c : chat.getReceivers()) {
            if (!c.getNetworkingId().equals(ownNID)) {
                byte updateChatFlag = ProtocolConstants.PACKET_UPDATE_CHAT;
                byte[] chatByte = ChatCoder.chatToByte(chat);
                Packet createPacket = new Packet(updateChatFlag, chatByte, c.getNetworkingId(), ownNID);
                networkProtocol.dispatchPacket(createPacket, dummyCallback);
            }
        }
    }

    /**
     * Informs all receiver in the chat about the changes which where made to the chat.
     *
     * @param chat the changed chat.
     */
    public void sendDeleteChat(Chat chat) {
        for (Contact c : chat.getReceivers()) {
            sendDeleteChat(chat, c);
        }
        for (String key: dataReceiverMap.keySet()) {
            String cnid = dataReceiverMap.get(key).getNetworkChatID();
            if (chat.getNetworkChatID().equals(cnid)) {
                dataReceiverMap.remove(key);
            }
        }
    }

    public void sendDeleteChat(Chat chat, Contact contact) {
        if (!contact.getNetworkingId().equals(ownNID)) {
            byte updateChatFlag = ProtocolConstants.PACKET_DELETE_CHAT;
            byte[] chatByte = ChatCoder.chatToByte(chat);
            Packet createPacket = new Packet(updateChatFlag, chatByte, contact.getNetworkingId(), ownNID);
            networkProtocol.dispatchPacket(createPacket, dummyCallback);
        }

    }

    /**
     * This method sends a packet to the device with the networking identifiert <code>receiverNID</code>
     * which request more information about the chat with the network chat identifiert <code>cNID</code>.
     *
     * @param cNID        the networking identifier of the chat where information are missing.
     * @param receiverNID the network identifier of the device which knows more about the chat.
     */
    public void sendRequestChatInformation(String cNID, String receiverNID) {

        List<Contact> receivers = new ArrayList<>(0);

        Chat chat = new Chat(cNID, "", receivers);

        byte updateChatFlag = ProtocolConstants.PACKET_REQUEST_INFO_CHAT;
        byte[] chatByte = ChatCoder.chatToByte(chat);
        Packet createPacket = new Packet(updateChatFlag, chatByte, receiverNID, ownNID);
        networkProtocol.dispatchPacket(createPacket, dummyCallback);
    }

    /**
     * Notifies the MessagingListener
     *
     * @param data the received data object.
     */
    public void receiveMessage(final Data data, final String key) {
        if (data != null) {
            messageListener.messageReceived(data);
        }
        dataReceiverMap.remove(key);
    }

    /**
     * This method handles a create chat packet.
     *
     * @param createPacket the packet with the new chat information.
     */
    public void receivedCreateChat(Packet createPacket) {
        Chat chat = ChatCoder.byteToChat(createPacket.data, this.context);
        chatChangeListener.receivedInsert(chat);
    }

    /**
     * this method handles a update chat packet.
     *
     * @param updatePacket the packet with the updated chat information.
     */
    public void receivedUpdateChat(Packet updatePacket) {
        Chat chat = ChatCoder.byteToChat(updatePacket.data, this.context);
        chatChangeListener.receivedUpdate(chat);
    }

    /**
     * this method handles a delete chat packet.
     *
     * @param deletePacket the packet with the information about the deleted chat.
     */
    public void receivedDeleteChat(Packet deletePacket) {
        Chat chat = ChatCoder.byteToChat(deletePacket.data, this.context);
        chatChangeListener.receivedDelete(chat, deletePacket.senderNID);
    }

    /**
     * This method handels request for chat information.
     *
     * @param requestPacket the packet with the information about the chat which misses some
     *                      information.
     */
    public void receivedRequestChatInformation(Packet requestPacket) {
        Chat reqChat = ChatCoder.byteToChat(requestPacket.data, this.context);
        Chat ownChat = HelperFactory.getChatHelper(context).getChat(reqChat.getNetworkChatID());
        if (ownChat != null) {
            byte updateChatFlag = ProtocolConstants.PACKET_UPDATE_CHAT;
            byte[] chatByte = ChatCoder.chatToByte(ownChat);
            Packet updateChatPacket = new Packet(updateChatFlag, chatByte, requestPacket.senderNID, ownNID);
            networkProtocol.dispatchPacket(updateChatPacket, dummyCallback);
        }
    }

    @Override
    public void packetReceived(Packet packet) {
        switch (packet.flag) {
            case ProtocolConstants.PACKET_DATA:
                Log.v(TAG, "RECEIVED data packet! " + (packet.sequenceNumber + 1) + "/" + packet.packetCount);
                if (packet.sequenceNumber == 0) {
                    if (dataReceiverMap.get(keyForPacket(packet)) != null) {
                        Log.e(TAG, "Resend data! " + keyForPacket(packet));
                    }
                    dataReceiverMap.put(keyForPacket(packet), new DataReceiver(packet, this));
                } else {
                    DataReceiver receiver = dataReceiverMap.get(keyForPacket(packet));
                    if (receiver != null) {
                        receiver.packetReceived(packet);
                    }
                }
                break;
            case ProtocolConstants.PACKET_NEW_CHAT:
                Log.v(TAG, "RECEIVED new chat packet!!");
                receivedCreateChat(packet);
                break;
            case ProtocolConstants.PACKET_UPDATE_CHAT:
                Log.v(TAG, "RECEIVED update chat packet!!");
                receivedUpdateChat(packet);
                break;
            case ProtocolConstants.PACKET_DELETE_CHAT:
                Log.v(TAG, "RECEIVED delete chat packet!!");
                receivedDeleteChat(packet);
                break;
            case ProtocolConstants.PACKET_REQUEST_INFO_CHAT:
                Log.v(TAG, "RECEIVED request chat info packet!!");
                receivedRequestChatInformation(packet);
                break;
            default:
                Log.e(TAG, "Unknown packet typ!");
        }
    }


    /**
     * returns the current messaging listener
     *
     * @return the messaging listener
     */
    public MessagingListener getMessageListener() {
        return messageListener;
    }

    /**
     * returns the current listener for chat changes
     *
     * @return the listener for chat changes
     */
    public ChatChangeListener getChatChangeListener() {
        return chatChangeListener;
    }

    /**
     * This method is called from the DataSender when it has finished to send its data object.
     * Also the sending finished, it is possible that sending failed.
     *
     * @param dataID the data id of the send data object
     */
    public void senderFinished(long dataID) {
        DataSender dataSender = dataSenderMap.remove(dataID);
        if (dataSender != null) {
            Data data = dataSender.getData();
            messageListener.messageSendSuccess(data);
        }
    }

    /**
     * This method creates and returns a new DataEncoder object which will encode the given data object.
     *
     * @param data the data object to encode
     * @return the newly created DataEncoder object
     */
    public DataEncoder getEncoder(Data data) {
        return new StreamDataEncoder(data);
    }

    /**
     * This method creates and returns a new DataDecoder object which can be used to decode bytes.
     *
     * @param senderNID the networking identifier of the sender
     * @return the newly created DataDecoder object.
     */
    public DataDecoder getDecoder(String senderNID) {
        return new StreamDataDecoder(this.context, senderNID);
    }

    /**
     * Calculated an identifier which groups packets to a single data object.
     * All packets with the same key belong to the same data object
     *
     * @param packet the packet for which the key should be calculated.
     * @return the key which groups all packets which holds information about the same data object.
     */
    public static String keyForPacket(Packet packet) {
        return  packet.senderNID + ":" + packet.dataIdentifier;
    }
}