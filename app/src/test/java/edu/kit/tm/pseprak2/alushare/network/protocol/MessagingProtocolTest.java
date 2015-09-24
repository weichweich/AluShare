package edu.kit.tm.pseprak2.alushare.network.protocol;

import android.content.Context;
import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.coding.ChatCoder;
import edu.kit.tm.pseprak2.alushare.network.coding.DataEncoder;
import edu.kit.tm.pseprak2.alushare.network.coding.StreamDataEncoder;
import edu.kit.tm.pseprak2.alushare.network.packer.Packet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Albrecht Weiche
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class MessagingProtocolTest {
    Context context = RuntimeEnvironment.application;
    Random rand = new Random();

    DataHelper dataHelper;
    ChatHelper chatHelper;
    ContactHelper contactHelper;
    Contact self;

    MessagingProtocol messagingProtocol;
    MockNetProtocol mockNetProtocol;

    @Before
    public void setup() {
        TestHelper.resetHelperFactory();

        contactHelper = HelperFactory.getContacHelper(context);
        chatHelper = HelperFactory.getChatHelper(context);
        dataHelper = HelperFactory.getDataHelper(context);

        DummyDataSet.createDummyDataSet(context);
        self = contactHelper.getSelf();

        mockNetProtocol = new MockNetProtocol(RuntimeEnvironment.application);
        mockNetProtocol.connect();
        mockNetProtocol.setSafePacketHistory(true);
        messagingProtocol = new MessagingProtocol(RuntimeEnvironment.application, mockNetProtocol);
        TestHelper.blockingSend(true);
    }

    @Test
    public void testSending() {
        List<Data> datas = dataHelper.getDataObjects();
        for (Data data : datas) {
            if (data.received()) {
                continue;
            }
            data.getReceivers().clear();
            data.getReceivers().add(self);

            HashMap<Long, DataState> stateMap = DataState.createStates(data.getReceivers(), DataState.Type.NOT_SENT);
            data.setReceiverStateMap(stateMap);

            dataHelper.update(data);

            assertTrue("Data should be send after update.", data.sendingCompleted());
            assertTrue("MessagingProtocol should have finished sending", messagingProtocol.sendingDataCount() == 0);
        }
    }

    @Test
    public void testReceivingData() {
        List<Data> datas = dataHelper.getDataObjects();
        for (Data data : datas) {
            if (!data.received()) {
                continue;
            }

            DataEncoder dataEncoder = new StreamDataEncoder(data);
            int sequenceCount = (dataEncoder.available() / ProtocolConstants.PACKET_MAX_DATA_SIZE) + 1;
            for (int i = 0; i < sequenceCount; i++) {
                int packetSize = ProtocolConstants.PACKET_MAX_DATA_SIZE;
                int available = dataEncoder.available();
                if (0 < available && available < packetSize) {
                    packetSize = dataEncoder.available();
                } else if (available <= 0) {
                    packetSize = 0;
                }

                byte[] dataBuffer = new byte[packetSize];
                int read = dataEncoder.read(dataBuffer, 0, dataBuffer.length);

                messagingProtocol.packetReceived(new Packet(i, sequenceCount, data.getId(),
                        dataBuffer, MockMessagingProtocol.ownTestNID, data.getSender().getNetworkingId()));


           }
        }
    }

    @Test
    public void testSendCreateChat() {
        List<Chat> chats = DummyDataSet.generateChatList(context, 20);
        mockNetProtocol.setSafePacketHistory(true);
        List<Packet> packets = mockNetProtocol.lastDispatchedPackets;
        for (Chat chat : chats) {
            packets.clear();
            chat.setNetworkChatID(mockNetProtocol.createNewNetworkChatID());

            chatHelper.insert(chat);


            if (chat.getReceivers().size() > 2) {
                assertTrue("Every receiver in the chat, except us, should get a packet.", packets.size() == chat.getReceivers().size() - 1);

                for (Contact receiver : chat.getReceivers()) {
                    Packet createChatPacket = getPacket(receiver.getNetworkingId(), packets);

                    if (receiver.getNetworkingId().equals(contactHelper.getSelf().getNetworkingId())) {
                        assertNull("We should not send a packet to our self.", createChatPacket);

                    } else {
                        assertNotNull("A packet should be dispatched", createChatPacket);
                        assertEquals("Packet should have create chat flag", ProtocolConstants.PACKET_NEW_CHAT, createChatPacket.flag);
                        Chat sendChat = ChatCoder.byteToChat(createChatPacket.data, context);

                        assertNotNull("The decoded chat should not be null", sendChat);
                        assertEquals("Chat title should be equal!", chat.getTitle(), sendChat.getTitle());
                        assertTrue("Chats should have the same receiver!",
                                TestHelper.diffReceiver(chat.getReceivers(), sendChat.getReceivers()).size() == 0);
                        assertEquals("Chats should have the same identifier", chat.getNetworkChatID(), sendChat.getNetworkChatID());
                    }
                }
            } else {
                assertTrue("If only 2 receivers are in a chat, no create chat packet should be send!", packets.size() == 0);
            }
        }
    }

    @Test
    public void testReceiveCreateChat() {
        List<Chat> chats = DummyDataSet.generateChatList(context, 20);

        for (Chat chat : chats) {
            String admin = chat.getNetworkChatID().split(":")[0];
            byte[] data = ChatCoder.chatToByte(chat);
            Packet createChatP = new Packet(ProtocolConstants.PACKET_NEW_CHAT, data,
                    MockMessagingProtocol.ownTestNID, admin);
            messagingProtocol.packetReceived(createChatP);

            Chat insertedChat = chatHelper.getChat(chat.getNetworkChatID());

            assertNotNull("Chat should be inserted", chat);

            assertEquals("Chat title should be equal!", chat.getTitle(), insertedChat.getTitle());
            assertTrue("Chats should have the same receiver!",
                    TestHelper.diffReceiver(chat.getReceivers(), insertedChat.getReceivers()).size() == 0);
            assertEquals("Chats should have the same identifier", chat.getNetworkChatID(), insertedChat.getNetworkChatID());
        }
    }

    @Test
    public void testSendUpdateChat() {
        List<Chat> chats = chatHelper.getChats();

        mockNetProtocol.setSafePacketHistory(true);
        List<Packet> packets = mockNetProtocol.lastDispatchedPackets;

        for (Chat chat : chats) {
            packets.clear();

            if (chat.getReceivers().size() > 2) {
                if (!chat.isAdmin(contactHelper.getSelf())) {
                    continue;
                }
                chat.setTitle("ajdsfkljasödlkfjaldsk");
                int index = 0;
                boolean found = false;
                while (!found && index < chat.getReceivers().size()) {
                    if (!chat.getReceivers().get(index).getNetworkingId().equals(MockMessagingProtocol.ownTestNID)) {
                        found = true;
                        chat.getReceivers().remove(index);
                    }
                    index++;
                }
                chatHelper.update(chat);

                assertEquals("Every receiver in the chat, except us, should get a packet.", chat.getReceivers().size() - 1, packets.size());

                for (Contact receiver : chat.getReceivers()) {
                    Packet updateChatPacket = getPacket(receiver.getNetworkingId(), packets);

                    if (receiver.getNetworkingId().equals(contactHelper.getSelf().getNetworkingId())) {
                        assertNull("We should not send a packet to our self.", updateChatPacket);

                    } else {
                        assertNotNull("A packet should be dispatched", updateChatPacket);
                        assertEquals("Packet should have update chat flag", ProtocolConstants.PACKET_UPDATE_CHAT, updateChatPacket.flag);
                        Chat sendChat = ChatCoder.byteToChat(updateChatPacket.data, context);

                        assertNotNull("The decoded chat should not be null", sendChat);
                        assertEquals("Chat title should be equal!", chat.getTitle(), sendChat.getTitle());
                        assertTrue("Chats should have the same receiver!",
                                TestHelper.diffReceiver(chat.getReceivers(), sendChat.getReceivers()).size() == 0);
                        assertEquals("Chats should have the same identifier", chat.getNetworkChatID(), sendChat.getNetworkChatID());
                    }
                }
            } else {
                assertTrue("If only 2 receivers are in a chat, no update chat packet should be send!", packets.size() == 0);
            }
        }
    }

    @Test
    public void testReceiveUpdateChat() {
        List<Chat> chats = chatHelper.getChats();

        for (Chat chat : chats) {
            String newTitle = "Nice title! title kdjfkasdjfad";
            chat.setTitle(newTitle);

            if (chat.getReceivers().size() > 2) {
                int index = 0;
                int deleted = rand.nextInt(chat.getReceivers().size() - 2);
                while (deleted > 0 && index < chat.getReceivers().size()) {
                    if (!chat.getReceivers().get(index).getNetworkingId().equals(MockMessagingProtocol.ownTestNID)) {
                        deleted--;
                        chat.getReceivers().remove(index);
                    }
                    index++;
                }
            }

            String admin = chat.getNetworkChatID().split(":")[0];
            byte[] data = ChatCoder.chatToByte(chat);
            Packet createChatP = new Packet(ProtocolConstants.PACKET_UPDATE_CHAT, data,
                    MockMessagingProtocol.ownTestNID, admin);

            messagingProtocol.packetReceived(createChatP);

            Chat updatedChat = chatHelper.getChat(chat.getNetworkChatID());

            assertNotNull("Chat should be inserted", chat);

            assertEquals("Chat title should be equal!", chat.getTitle(), updatedChat.getTitle());
            assertTrue("Chats should have the same receiver!",
                    TestHelper.diffReceiver(chat.getReceivers(), updatedChat.getReceivers()).size() == 0);
            assertEquals("Chats should have the same identifier", chat.getNetworkChatID(), updatedChat.getNetworkChatID());
        }
    }

    @Test
    public void testSendDeleteChat() {
        List<Chat> chats = chatHelper.getChats();

        mockNetProtocol.setSafePacketHistory(true);
        List<Packet> packets = mockNetProtocol.lastDispatchedPackets;

        for (Chat chat : chats) {
            packets.clear();

            if (chat.getReceivers().size() > 2) {

                chatHelper.delete(chat);

                assertTrue("Every receiver in the chat, except us, should get a packet.", packets.size() == chat.getReceivers().size() - 1);

                for (Contact receiver : chat.getReceivers()) {
                    Packet deleteChatPacket = getPacket(receiver.getNetworkingId(), packets);

                    if (receiver.getNetworkingId().equals(contactHelper.getSelf().getNetworkingId())) {
                        assertNull("We should not send a packet to our self.", deleteChatPacket);

                    } else {
                        assertNotNull("A packet should be dispatched", deleteChatPacket);
                        assertEquals("Packet should have update chat flag", ProtocolConstants.PACKET_DELETE_CHAT, deleteChatPacket.flag);
                        Chat sendChat = ChatCoder.byteToChat(deleteChatPacket.data, context);

                        assertNotNull("The decoded chat should not be null", sendChat);
                        assertEquals("Chat title should be equal!", chat.getTitle(), sendChat.getTitle());
                        assertTrue("Chats should have the same receiver!",
                                TestHelper.diffReceiver(chat.getReceivers(), sendChat.getReceivers()).size() == 0);
                        assertEquals("Chats should have the same identifier", chat.getNetworkChatID(), sendChat.getNetworkChatID());
                    }
                }
            } else {
                assertTrue("If only 2 receivers are in a chat, no update chat packet should be send!", packets.size() == 0);
            }
        }
    }

    @Test
    public void testReceiveDeleteChat() {
        List<Chat> chats = chatHelper.getChats();

        for (Chat chat : chats) {
            List<Contact> oldReceiver = new ArrayList<>(chat.getReceivers());

            String sender = DummyDataSet.pickRandomContact(chat.getReceivers()).getNetworkingId();
            boolean found = false;
            int index = 0;
            while (!found && index < chat.getReceivers().size()) {
                if (chat.getReceivers().get(index).getNetworkingId().equals(sender)) {
                    chat.getReceivers().remove(index);
                    found = true;
                }
                index++;
            }
            byte[] data = ChatCoder.chatToByte(chat);
            Packet deleteChatP = new Packet(ProtocolConstants.PACKET_DELETE_CHAT, data,
                    MockMessagingProtocol.ownTestNID, sender);

            messagingProtocol.packetReceived(deleteChatP);

            Chat updatedChat = chatHelper.getChat(chat.getNetworkChatID());

            if (chat.isAdmin(sender)) {
                assertNull("If the admin deletes the chat, the local chat should be deleted", updatedChat);
            } else {
                assertNotNull("The chat should still exist.", chat);

                assertEquals("Chat title should be equal!", chat.getTitle(), updatedChat.getTitle());

                List<Contact> diffList = TestHelper.diffReceiver(oldReceiver, updatedChat.getReceivers());
                assertTrue("Only one receiver should be deleted.", diffList.size() == 1);
                assertTrue("Chats should have the same receiver, but without the sender of the delete chat packet!",
                        diffList.get(0).getNetworkingId().equals(sender));
                assertEquals("Chats should have the same identifier", chat.getNetworkChatID(), updatedChat.getNetworkChatID());
            }
        }
    }

    @Test
    public void testReceiveChatInfo() {
        List<Chat> chats = chatHelper.getChats();
        String testOnion = "testtesttest.onion";
        List<Packet> packets = mockNetProtocol.lastDispatchedPackets;

        for (Chat chat : chats) {
            if (chat.isDeleted()) {
                continue;
            }

            messagingProtocol.sendRequestChatInformation(chat.getNetworkChatID(), testOnion);
            assertEquals("Exactly one packet should be send", 1, packets.size());
            Packet lastPacket = packets.get(packets.size() - 1);

            assertNotNull("Packet should not be null", lastPacket);
            assertEquals("Packet should be a request-chat-info-packet.",
                    ProtocolConstants.PACKET_REQUEST_INFO_CHAT, lastPacket.flag);
            Chat sendChat = ChatCoder.byteToChat(lastPacket.data, context);
            assertEquals("Chat id should be equal!", chat.getNetworkChatID(), sendChat.getNetworkChatID());

            packets.clear();
        }
    }

    @After
    public void teardown() {
        TestHelper.resetHelperFactory();
    }

    private Packet getPacket(String receiverNID, List<Packet> packets) {
        for (Packet packet : packets) {
            if (packet.receiverNID.equals(receiverNID)) {
                return packet;
            }
        }
        return null;
    }
}
