package edu.kit.tm.pseprak2.alushare.network.protocol;

import android.content.Context;

import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.network.packer.Packet;

/**
 * @author Albrecht Weiche
 */
public class MockMessagingProtocol extends MessagingProtocol {
    private static final String TAG = "MockMessagingProtocol";

    public Data lastSendingData;
    public Data lastReceivedData;
    public Chat lastCreateChat = null;
    public Chat lastUpdateChat = null;
    public Chat lastDeleteChat = null;
    public long lastFinishedDataID = -1;
    public static final String ownTestNID = "ownTest.onion";

    private int sendingCount = 0;

    public MockMessagingProtocol(Context appContext) {
        super(appContext, new PseudoProtocol(appContext, null));
    }

    @Override
    public void sendMessage(Data data) {
        lastSendingData = data;
        sendingCount++;
    }

    @Override
    public int sendingDataCount() {
        return sendingCount;
    }

    @Override
    public void sendCreateChat(Chat chat) {
        lastCreateChat = chat;
    }

    @Override
    public void sendUpdateChat(Chat chat) {
        lastUpdateChat = chat;
    }

    @Override
    public void sendDeleteChat(Chat chat) {
        lastDeleteChat = chat;
    }

    @Override
    public void sendRequestChatInformation(String cNID, String receiverNID) {
    }

    @Override
    public void receiveMessage(final Data data, final String key) {
        lastReceivedData = data;
        dataReceiverMap.remove(key);
    }

    @Override
    public void receivedCreateChat(Packet createPacket) {
    }

    @Override
    public void receivedUpdateChat(Packet updatePacket) {
    }

    @Override
    public void receivedDeleteChat(Packet deletePacket) {
    }

    @Override
    public void receivedRequestChatInformation(Packet requestPacket) {
    }

    @Override
    public void packetReceived(Packet packet) {
    }

    @Override
    public void senderFinished(long dataID) {
        lastFinishedDataID = dataID;
    }
}
