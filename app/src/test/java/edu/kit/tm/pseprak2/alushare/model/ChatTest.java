package edu.kit.tm.pseprak2.alushare.model;


import android.content.Context;

import org.junit.After;
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
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ChatTest {
    private Context mContext = RuntimeEnvironment.application;
    private String expectedTitle;
    private List<Data> expectedDataList;
    private List<Contact> expectedReceiverList;
    private String expectedNetworkChatId;
    private Chat expectedChat;
    private ChatHelper chatHelper;
    private boolean expectedDeleted;

    @Before
    public void setUp() throws Exception {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");

        chatHelper = HelperFactory.getChatHelper(mContext);
        Chat chat = chatHelper.getChats().get(0);

        expectedNetworkChatId = chat.getNetworkChatID();
        expectedTitle = chat.getTitle();
        expectedDataList = chat.getDataObjects();
        expectedReceiverList = chat.getReceivers();
        expectedDeleted = false;

        expectedChat = new Chat(expectedNetworkChatId, expectedTitle, expectedReceiverList, expectedDeleted);
        expectedChat.addData(expectedDataList);
    }

    @Test
    public void testConstructorWithNetworkChatIdAndTitleAndReceiverList() {
        assertEquals(expectedTitle, expectedChat.getTitle());
        assertEquals(expectedNetworkChatId, expectedChat.getNetworkChatID());
        assertEquals(expectedDataList, expectedChat.getDataObjects());
        assertEquals(expectedReceiverList, expectedChat.getReceivers());

        //Should log or in future throw an exception
        List<Contact> receiver = new ArrayList<>();
        receiver.add(new Contact("asdasd"));
        expectedChat = new Chat(expectedNetworkChatId, expectedTitle, receiver);
    }

    @Test
    public void testGetTitle() {
        assertEquals(expectedTitle, expectedChat.getTitle(mContext));
    }

    @Test
    public void testGetTitleShouldReturnNameOfContact() {
        List<Contact> receiver = new ArrayList<>();
        receiver.add(new Contact(1, "weeeeerwtrgsdfgt", "juergen"));
        Chat newChat = new Chat("asewrgasdvhtra", "12345", receiver);
        assertEquals("juergen", newChat.getTitle(mContext));
    }

    @Test
    public void testGetLastDataShouldReturnNullIfDataListIsEmpty() {
        expectedChat = new Chat(expectedNetworkChatId, expectedTitle, expectedReceiverList);
        assertEquals(null, expectedChat.getLastData());
    }

    @Test
    public void testAddDataAtPosition() {
        Data tmp = expectedDataList.get(expectedDataList.size() - 1);
        expectedChat.addData(0, tmp);
        assertEquals(tmp, expectedChat.getDataObjects().get(0));
    }

    @Test
    public void testRemoveReceiver() {
        expectedChat.removeReceiver(expectedReceiverList);
        assertEquals(0, expectedChat.getReceivers().size());
    }

    @Test
    public void testIsGroupChat() {
        if (expectedReceiverList.size() > 2) {
            assertTrue(expectedChat.isGroupChat());
        } else {
            assertFalse(expectedChat.isGroupChat());
        }
    }

    @Test
    public void testGetLastData() {
        Data tmp = expectedDataList.get(expectedDataList.size() - 1);
        assertEquals(tmp, expectedChat.getLastData());
    }

    @Test
    public void testIsAdmin() {
        Contact self = DummyDataSet.initSelf(mContext);
        assertFalse(expectedChat.isAdmin(self));

        //Should log
        expectedChat = new Chat("sssssssssss", expectedTitle, expectedReceiverList);
        expectedChat.isAdmin("wert");
    }

    @Test
    public void testAddReceiver() {
        Contact newReceiver = new Contact("friend.onion");
        expectedChat.addReceiver(newReceiver);
        assertEquals(expectedReceiverList.size() + 1, expectedChat.getReceivers().size());
    }

    @Test
    public void testIsDeleted() {
        assertFalse(expectedChat.isDeleted());
    }

    @After
    public void tearDown() throws Exception{
        TestHelper.resetHelperFactory();
    }
}
