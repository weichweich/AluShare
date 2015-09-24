package edu.kit.tm.pseprak2.alushare.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.MockNetProtocol;
import edu.kit.tm.pseprak2.alushare.view.ChatActivity;
import edu.kit.tm.pseprak2.alushare.view.MainActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.apache.tools.ant.util.FileUtils;
import org.robolectric.shadows.ShadowActivity;

/**
 *
 * Created by arthuranselm on 27.08.15.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ChatPresenterTest {

    //private String TAG = "ChatPresenter";
    private ChatActivity chatActivity;
    private ChatPresenter chatPresenter;
    private Context context = RuntimeEnvironment.application;
    private DataHelper dataHelper;
    private ContactHelper contactHelper;
    private ChatHelper chatHelper;
    private Contact sender;
    private Chat chat;
    private Data data;
    private String chatId;
    private int i;

    @Before
    public void setUp() throws Exception {
        this.dataHelper = HelperFactory.getDataHelper(context);
        this.contactHelper = HelperFactory.getContacHelper(context);
        this.chatHelper = HelperFactory.getChatHelper(context);

        //Creating chat to test with
        chatId = new MockNetProtocol(context).createNewNetworkChatID();
        DummyDataSet.generateChat(chatId, context);

        this.sender = contactHelper.getSelf();
        this.chat = chatHelper.getChat(chatId);
        this.data = chat.getLastData();

        //Starting ChatActivity with chatId used above
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra(context.getString(R.string.CHAT_ID), chatId);
        this.chatActivity = Robolectric.buildActivity(ChatActivity.class)
                .withIntent(chatIntent).create().get();

        Collections.reverse(chat.getDataObjects());
        chatActivity.showDataList(chat.getDataObjects());
        this.chatPresenter = new ChatPresenter(chatActivity, chatId);
    }

    @Test(expected=NullPointerException.class)
    public void testCreateChatPresenter1() {
        chatPresenter = new ChatPresenter(null, chatId);
    }

    @Test(expected=NullPointerException.class)
    public void testCreateChatPresenter2() {
        chatPresenter = new ChatPresenter(chatActivity, null);
    }

    @Test
    public void testGetReceivers(){
        List<Contact> receivers = chat.getReceivers();
        int count = 0;
        for(int i = 0; i < receivers.size(); i++){
            if(receivers.get(i).getId() == sender.getId()){
                count++;
            }
        }
        assertTrue(count == 1);
        receivers = chatPresenter.getReceivers();
        count = 0;
        for(int i = 0; i < receivers.size(); i++){
            if(receivers.get(i).getId() == sender.getId()){
                count++;
            }
        }
        assertTrue(count == 0);
    }

    @Test
    public void testUpdateContactNotFound(){
        Contact contact = new Contact("Receiver2");
        contactHelper.insert(contact);
        chatPresenter.updateContactNotFound(contact.getId());
        assertTrue(contact.getLookUpKey().equals(""));
    }

    @Test
    public void testStartMainActivity(){
        chatPresenter.startMainActivity();
        testNextStartedMainActivity();
    }

    public void testNextStartedMainActivity(){
        ShadowActivity shadowActivity = Shadows.shadowOf(chatActivity);
        Intent i = shadowActivity.getNextStartedActivity();
        assertEquals(i.getComponent().getClassName(), MainActivity.class.getName());
    }

    @Test
    public void testResendData() {
        assertTrue(chat.getDataObjects().size() > 0);
        assertTrue(data.needsResend());
        data.sendingStopped();
        chatPresenter.resendData(data.getId());
        assertTrue(!dataHelper.getDataByID(data.getId()).needsResend());
    }

    @Test
    public void testAddDataString(){
        int sizeCalculated = chat.getDataObjects().size() + 1;
        String newMessage = "newMessage";
        chatPresenter.addData(newMessage);

        Chat chat = chatHelper.getChat(chatId);
        assertTrue(chat != null);
        int sizeAfter = chat.getDataObjects().size();
        Data data = chat.getLastData();
        assertTrue(data.getText().equals(newMessage) && sizeCalculated==sizeAfter);
    }

    @Test
    public void testAddDataFile() throws IOException {
        int sizeCalculated = chat.getDataObjects().size() + 1;
        File file = File.createTempFile("testFile", ".jpeg", context.getFilesDir());
        chatPresenter.addData(file);
        Chat chat = chatHelper.getChat(chatId);
        assertTrue(chat != null);
        int sizeAfter = chat.getDataObjects().size();
        Data data = chat.getLastData();
        assertTrue(FileUtils.getFileUtils().contentEquals(data.getFile(), file)
                && sizeCalculated == sizeAfter);
    }

    @Test
    public void testAddDataFileUri() throws IOException {
        int sizeCalculated = chat.getDataObjects().size() + 1;
        File file = File.createTempFile("testFile", ".jpeg", context.getFilesDir());
        chatPresenter.addData(Uri.fromFile(file));
        Chat chat = chatHelper.getChat(chatId);
        assertTrue(chat != null);
        int sizeAfter = chat.getDataObjects().size();
        Data data = chat.getLastData();
        assertTrue(FileUtils.getFileUtils().contentEquals(data.getFile(), file)
                && sizeCalculated == sizeAfter);
    }


    public void testAddDataContentUri() throws IOException {
        int sizeCalculated = chat.getDataObjects().size() + 1;
        File file = File.createTempFile("testFile", ".jpeg", context.getFilesDir());
        Uri contentUri =
                android.provider.MediaStore.Files.getContentUri(file.getAbsolutePath());
        chatPresenter.addData(contentUri);
        Chat chat = chatHelper.getChat(chatId);
        assertTrue(chat != null);
        int sizeAfter = chat.getDataObjects().size();
        Data data = chat.getLastData();
        assertTrue(FileUtils.getFileUtils().contentEquals(data.getFile(), file)
                && sizeCalculated == sizeAfter);
    }

    @Test
    public void testInserted() throws NoSuchFieldException, IllegalAccessException {
        Field field = ChatPresenter.class.getDeclaredField("chat");
        field.setAccessible(true);
        Chat chat = (Chat) field.get(chatPresenter);
        int sizeCalculated = chat.getDataObjects().size() + 1;
        List<Contact> receiverList = chat.getReceivers();
        final HashMap<Long, DataState> receiverStateMap = DataState.createStates(receiverList, DataState.Type.SENDING_FAILED);
        Data data = new Data(sender, receiverList, receiverStateMap, "newInsertedMessage");
        data.setNetworkChatID(chat.getNetworkChatID());
        chatPresenter.inserted(data);

        int sizeAfter = chat.getDataObjects().size();
        assertTrue(sizeCalculated == sizeAfter);
        assertTrue(chat.getDataObjects().get(0) == data);

        data.setNetworkChatID("testChatId");
        chatPresenter.inserted(data);
        sizeAfter = chat.getDataObjects().size();
        assertTrue(sizeCalculated == sizeAfter);
    }

    @Test
    public void testUpdated() throws NoSuchFieldException, IllegalAccessException {
        Field field = ChatPresenter.class.getDeclaredField("chat");
        field.setAccessible(true);
        Chat chat = (Chat) field.get(chatPresenter);
        int sizeBefore = chat.getDataObjects().size();
        assertTrue(sizeBefore > 0);
        List<Contact> receiverList = chat.getReceivers();
        final HashMap<Long, DataState> receiverStateMap = DataState.createStates(receiverList, DataState.Type.SENDING_FAILED);
        Data data = new Data(sender, receiverList, receiverStateMap, "newChangedMessage");
        data.setNetworkChatID(chat.getNetworkChatID());
        data.setId(this.data.getId());
        chatPresenter.updated(data);

        int sizeAfter = chat.getDataObjects().size();
        assertTrue(sizeBefore == sizeAfter);
        for (i = 0; i < chat.getDataObjects().size(); i++) {
            if (chat.getDataObjects().get(i).getId() == data.getId()) {
                assertTrue(chat.getDataObjects().get(i) == data);
                return;
            }
        }
    }

    @Test
    public void testRemoved() throws NoSuchFieldException, IllegalAccessException {
        Field field = ChatPresenter.class.getDeclaredField("chat");
        field.setAccessible(true);
        Chat chat = (Chat) field.get(chatPresenter);
        int sizeCalculated = chat.getDataObjects().size() - 1;
        assertTrue(sizeCalculated >= 0);
        List<Contact> receiverList = chat.getReceivers();
        final HashMap<Long, DataState> receiverStateMap = DataState.createStates(receiverList, DataState.Type.SENDING_FAILED);
        Data data = new Data(sender, receiverList, receiverStateMap, "newChangedMessage");
        data.setNetworkChatID(chat.getNetworkChatID());
        data.setId(this.data.getId());
        chatPresenter.removed(data);

        int sizeAfter = chat.getDataObjects().size();
        assertTrue(sizeCalculated == sizeAfter);
        for (i = 0; i < chat.getDataObjects().size(); i++) {
            assertFalse(chat.getDataObjects().get(i).getId() == data.getId());
        }
    }

    @Test
    public void testRemovedChat(){
        chatPresenter.removed(chat);
        testNextStartedMainActivity();
    }

    @Test
    public void testUpdatedChat(){
        chat.setTitle("newTestTitle");
        chatPresenter.updated(chat);
        assertEquals(chat.getTitle(), chatPresenter.getTitle());
        assertEquals(chat.getTitle(), chatActivity.getTitle());
    }

    @Test
    public void testGetChatTitle(){
        assertEquals(chat.getTitle(), chatPresenter.getTitle());
    }

    @After
    public void tearDown() {
        this.chatActivity = null;
        this.chatHelper = null;
        this.dataHelper = null;
        this.contactHelper = null;
        this.chatHelper = null;
        this.sender = null;
        this.chatId = null;
        TestHelper.resetHelperFactory();
    }
}
