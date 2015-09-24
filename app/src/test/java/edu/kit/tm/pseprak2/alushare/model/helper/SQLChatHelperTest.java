package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.Context;

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
import edu.kit.tm.pseprak2.alushare.model.Data;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by dominik on 12.07.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class SQLChatHelperTest{
    private Context mContext = RuntimeEnvironment.application;
    private DataHelper dataHelper;
    private ChatHelper chatHelper;
    private ContactHelper contactHelper;

    private Chat newChat;

    @Before
    public void setUp() throws Exception {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");
        dataHelper = HelperFactory.getDataHelper(mContext);
        chatHelper = HelperFactory.getChatHelper(mContext);
        contactHelper = HelperFactory.getContacHelper(mContext);

        newChat = DummyDataSet.generateChatList(mContext, 1).get(0);
    }

    @Test
    public void testInsertDataNotAlreadyInDB() {
        chatHelper.insert(newChat);
        assertNotNull(chatHelper.getChat(newChat.getNetworkChatID()));
    }

    @Test
    public void testInsertDataAlreadyInDB() {
        String newTitle = "NeuerTitel";

        Chat alreadyInDB = chatHelper.getChats().get(0);
        alreadyInDB.setTitle(newTitle);
        chatHelper.insert(alreadyInDB);
        Chat tmp = chatHelper.getChat(alreadyInDB.getNetworkChatID());

        assertEquals(newTitle, tmp.getTitle());
    }

    @Test
    public void testUpdateDataNotAlreadyInDB() {
        chatHelper.update(newChat);
        assertNotNull(chatHelper.getChat(newChat.getNetworkChatID()));
    }

    @Test
    public void testUpdateDataAlreadyInDB() {
        String newTitle = "NeuerTitel";

        Chat alreadyInDB = chatHelper.getChats().get(0);
        alreadyInDB.setTitle(newTitle);
        chatHelper.update(alreadyInDB);
        Chat tmp = chatHelper.getChat(alreadyInDB.getNetworkChatID());

        assertEquals(newTitle, tmp.getTitle());
    }

    @Test
    public void testUpdateDataHasDeletedFlagg() {
        newChat.setIsDeleted(true);
        chatHelper.insert(newChat);
        newChat.setTitle("Neuer Titel");
        chatHelper.update(newChat);
        assertNull(chatHelper.getChat(newChat.getNetworkChatID()));
    }

    @Test
    public void testDelete() {
        Chat alreadyInDB = chatHelper.getChats().get(0);
        chatHelper.delete(alreadyInDB);
        Chat tmp = chatHelper.getChat(alreadyInDB.getNetworkChatID());
        assertEquals(null, tmp);
    }

    @Test
    public void testExistChatInDB() {
        Chat alreadyInDB = chatHelper.getChats().get(0);
        assertTrue(chatHelper.exist(alreadyInDB));
    }

    @Test
    public void testGetChat() {
        Chat tmp = chatHelper.getChat("fq28jalv3mprq0out8ncjr3ij2.onion:6d5t2kbd6k5i5cegfftckrfpe7");
        assertNotNull(tmp);

    }

    @Test
    public void testGetChats() {
       assertEquals(3, chatHelper.getChats().size());
    }

    @Test
    public void testGetChatsByTitle() {
        assertEquals(1, chatHelper.getChatsByTitle("Chat-Title-0", -1, -1).size());
    }

    @Test
    public void testGetChatsByContactID() {
        assertEquals(0, chatHelper.getChatsByContactID(2).size());
    }

    @Test
    public void testIsContactInChat() {
        Chat chat = chatHelper.getChats().get(0);
        Contact contact = chat.getReceivers().get(0);
        assertTrue(chatHelper.isContactInChat(contact, chat));
    }

    @Test
    public void testIsDeleted() {
        Chat chat = chatHelper.getChats().get(0);
        chat.setIsDeleted(true);
        chatHelper.update(chat);
        assertTrue(chatHelper.isDeleted(chat));
    }

    @Test
    public void testIsDeletedWithId() {
        Chat chat = chatHelper.getChats().get(0);
        chat.setIsDeleted(true);
        chatHelper.update(chat);
        assertTrue(chatHelper.isDeleted(chat.getNetworkChatID()));
    }

    @Test
    public void testRemoveReceiver() {
        Chat chat = chatHelper.getChats().get(0);
        List<Contact> receiverList = chat.getReceivers();
        if (receiverList.size() > 2) {
            Contact contact = receiverList.get(0);
            chatHelper.removeReceiver(chat, contact);
            chat = chatHelper.getChat(chat.getNetworkChatID());
            assertEquals(receiverList.size() - 1, chat.getReceivers().size());
        }
    }

    @Test
    public void testGetChatWithoutData() {
        Chat chat = chatHelper.getChatWithoutData("fq28jalv3mprq0out8ncjr3ij2.onion:6d5t2kbd6k5i5cegfftckrfpe7");
        assertTrue(chat.getDataObjects().isEmpty());
    }

    @Test
    public void testGetChatWithoutDataShouldReturnNull() {
        assertNull(chatHelper.getChatWithoutData("pppasdpaspdergmpm"));
    }

    @Test
    public void testLockChat() {
        chatHelper.lockChat(newChat.getNetworkChatID());
        Thread tA = new Thread(new Runnable() {
            @Override
            public void run() {
                chatHelper.lockChat(newChat.getNetworkChatID());
            }
        });
        tA.start();
        try {
            Thread.sleep(20);
        } catch (Exception e) {

        };
        tA.interrupt();
        assertTrue(true);
        //Es werden nur logs geschrieben. Es sollte keine Excpetion geworfen werden.
    }

    @After
    public void tearDown() {
        TestHelper.resetHelperFactory();
    }
}
