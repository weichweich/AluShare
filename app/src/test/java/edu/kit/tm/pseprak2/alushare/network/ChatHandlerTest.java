package edu.kit.tm.pseprak2.alushare.network;

import android.content.Context;
import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.MockMessagingProtocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * @author Albrecht Weiche
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class ChatHandlerTest {

    private MockMessagingProtocol mockMessProto;
    private ChatHelper chatHelper;
    private Context context = RuntimeEnvironment.application;
    private Contact self;
    private ChatHandler chatHandler;

    @Before
    public void setup() {
        TestHelper.resetHelperFactory();
        self = DummyDataSet.initSelf(context);
        chatHelper = HelperFactory.getChatHelper(context);

        mockMessProto = new MockMessagingProtocol(context);
        chatHandler = (ChatHandler) mockMessProto.getChatChangeListener();
    }

    @Test
    public void testNewChat() {
        List<Chat> chats = DummyDataSet.generateChatList(context, 10);
        for (Chat chat : chats) {
            chatHelper.insert(chat);
            if (chat.isAdmin(self) && chat.isGroupChat()) {
                assertNotNull("Created chat object should not be null", mockMessProto.lastCreateChat);
                assertEquals("Chat IDs should be equal", chat.getNetworkChatID(), mockMessProto.lastCreateChat.getNetworkChatID());
            }
            mockMessProto.lastCreateChat = null;
        }
    }

    @Test
    public void testDeleteChat() {
        List<Chat> chats = DummyDataSet.generateChatList(context, 10);
        for (Chat chat : chats) {
            chatHelper.insert(chat);

            if (chat.isAdmin(self) && chat.isGroupChat()) {
                assertNotNull("Created chat object should not be null", mockMessProto.lastCreateChat);
                assertEquals("Chat IDs should be equal", chat.getNetworkChatID(), mockMessProto.lastCreateChat.getNetworkChatID());
            }

            chatHelper.delete(chat);

            assertNotNull("Deleted chat object should not be null", mockMessProto.lastDeleteChat);
            assertEquals("Chat IDs should be equal", chat.getNetworkChatID(), mockMessProto.lastDeleteChat.getNetworkChatID());

            mockMessProto.lastCreateChat = null;
            mockMessProto.lastDeleteChat = null;
        }
    }

    @Test
    public void testUpdateChat() {
        List<Chat> chats = DummyDataSet.generateChatList(context, 10);
        for (Chat chat : chats) {
            chatHelper.insert(chat);

            if (chat.isAdmin(self) && chat.isGroupChat()) {
                assertNotNull("Created chat object should not be null", mockMessProto.lastCreateChat);
                assertEquals("Chat IDs should be equal", chat.getNetworkChatID(), mockMessProto.lastCreateChat.getNetworkChatID());
            }

            chat.setTitle(DummyDataSet.randomString());
            chatHelper.update(chat);

            if (chat.isAdmin(self)) {
                assertNotNull("Chat object should not be null", mockMessProto.lastUpdateChat);
                assertEquals("Chat IDs should be equal", chat.getNetworkChatID(), mockMessProto.lastUpdateChat.getNetworkChatID());
            }

            mockMessProto.lastCreateChat = null;
            mockMessProto.lastUpdateChat = null;
        }
    }

    @Test
    public void testReceiveNewChat() {
        List<Chat> chats = DummyDataSet.generateChatList(context, 10);
        for (Chat chat : chats) {
            if (!chat.isAdmin(self) && chat.isGroupChat()) {
                chatHandler.receivedInsert(chat);

                Chat insertedChat = chatHelper.getChat(chat.getNetworkChatID());
                assertNotNull("Created chat object should not be null", insertedChat);
                assertEquals("Chat IDs should be equal", chat.getNetworkChatID(), insertedChat.getNetworkChatID());
                assertTrue("Receiver should be equal",
                        TestHelper.diffReceiver(chat.getReceivers(), insertedChat.getReceivers()).size() == 0);
            }
        }
    }

    @Test
    public void testReceiveDeleteChat() {
        List<Chat> chats = DummyDataSet.generateChatList(context, 10);
        for (Chat chat : chats) {
            chatHelper.insert(chat);

            Contact contact = DummyDataSet.pickRandomContact(chat.getReceivers());
            chatHandler.receivedDelete(chat, contact.getNetworkingId());

            if (chat.isAdmin(contact) && chat.isGroupChat()) {
                Chat deletedChat = chatHelper.getChat(chat.getNetworkChatID());
                assertNull("Chat should be deleted", deletedChat);

            } else if (!self.getNetworkingId().equals(contact.getNetworkingId())) {

                Chat deletedChat = chatHelper.getChat(chat.getNetworkChatID());
                assertNotNull("Only admin should be able to delete chat.", deletedChat);
                List<Contact> diffList = TestHelper.diffReceiver(deletedChat.getReceivers(), chat.getReceivers());
                assertEquals("Only one contact should be removed", 1, diffList.size());
                assertEquals("The sender should be removed", contact.getNetworkingId(), diffList.get(0).getNetworkingId());
            }
        }
    }

    @Test
    public void testReceiveUpdateChat() {
        DummyDataSet.createDummyDataSet(context);

        List<Chat> chats = chatHelper.getChats();
        for (Chat chat : chats) {

            chat.setTitle(DummyDataSet.randomString());
            chatHandler.receivedUpdate(chat);

            if (chat.isGroupChat() && containsSelf(chat)) {
                Chat updated = chatHelper.getChat(chat.getNetworkChatID());
                assertEquals("Title should be equal", chat.getTitle(), updated.getTitle());
            }
        }
    }

    @After
    public void teardown() {
        TestHelper.resetHelperFactory();
        mockMessProto = null;
    }

    private boolean containsSelf(Chat chat) {
        for (Contact c: chat.getReceivers()) {
            if (c.getNetworkingId().equals(self.getNetworkingId())) {
                return true;
            }
        }
        return false;
    }
}
