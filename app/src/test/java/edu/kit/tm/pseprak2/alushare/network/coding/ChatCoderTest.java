package edu.kit.tm.pseprak2.alushare.network.coding;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Albrecht Weiche
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ChatCoderTest {
    Context context;
    DataHelper dataHelper;
    ChatHelper chatHelper;
    ContactHelper contactHelper;

    @Before
    public void setup() {
        TestHelper.resetHelperFactory();
        context = RuntimeEnvironment.application;

        contactHelper = HelperFactory.getContacHelper(context);
        chatHelper = HelperFactory.getChatHelper(context);
        dataHelper = HelperFactory.getDataHelper(context);

        DummyDataSet.createDummyDataSet(context);
    }

    @Test
    public void testEncodeDecode() {
        List<Chat> chats = chatHelper.getChats();
        for (Chat chat:chats) {
            byte[] chatBytes = ChatCoder.chatToByte(chat);
            Chat decodedChat = ChatCoder.byteToChat(chatBytes, context);

            assertEquals("Network chat identifier should be equal!",
                    chat.getNetworkChatID(), decodedChat.getNetworkChatID());
            assertEquals("Chat title should be equal!", chat.getTitle(), decodedChat.getTitle());
            List<Contact> decodedContacts = new ArrayList<>(decodedChat.getReceivers());
            for (Contact oldContact:chat.getReceivers()) {
                int removeIndex = -1;
                for (Contact decodedContact: decodedContacts) {
                    if (decodedContact.getNetworkingId().equals(oldContact.getNetworkingId())) {
                        removeIndex = decodedContacts.indexOf(decodedContact);
                        break;
                    }
                }
                assertNotEquals("The receiver list should be equal!", -1, removeIndex);
                decodedContacts.remove(removeIndex);
            }
        }
    }

}
