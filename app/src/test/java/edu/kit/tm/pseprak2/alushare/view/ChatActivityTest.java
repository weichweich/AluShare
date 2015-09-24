package edu.kit.tm.pseprak2.alushare.view;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toolbar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.MockNetProtocol;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatRecyclerAdapter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 *
 * Created by arthur anselm on 06.09.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ChatActivityTest {

    private Context context = RuntimeEnvironment.application;
    private ChatActivity chatActivity;
    private RecyclerView recyclerView;
    private ChatRecyclerAdapter adapter;
    private Chat chat;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        String chatId = new MockNetProtocol(context).createNewNetworkChatID();
        DummyDataSet.generateChat(chatId, context);
        chat = HelperFactory.getChatHelper(context).getChat(chatId);
        //Starting ChatActivity with chatId used above
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra(context.getString(R.string.CHAT_ID), chatId);
        this.chatActivity = Robolectric.buildActivity(ChatActivity.class)
                .withIntent(chatIntent).create().start().get();
        Field field = ChatActivity.class.getDeclaredField("recyclerView");
        field.setAccessible(true);
        recyclerView = (RecyclerView) field.get(chatActivity);
        Field field2 = ChatActivity.class.getDeclaredField("chatRecyclerAdapter");
        field2.setAccessible(true);
        adapter = (ChatRecyclerAdapter) field2.get(chatActivity);
    }

    @Test (expected=NullPointerException.class)
    public void testOnContextItemSelected(){
        chatActivity.onContextItemSelected(null);
    }

    @Test
    public void testSetUpRecyclerView() throws NoSuchFieldException, IllegalAccessException {
        assertTrue(recyclerView.hasFixedSize());
        assertTrue(recyclerView.getLayoutManager() != null);
        assertTrue(adapter != null);
    }

    @Test
    public void testScrollDown() throws NoSuchFieldException, IllegalAccessException {
        assertTrue(recyclerView.getVerticalScrollbarPosition() == 0);
        chatActivity.scrollDown();
        assertTrue(recyclerView.getVerticalScrollbarPosition() == 0);
    }

    @Test
    public void testShowMessageString(){
        int calculated = adapter.getItemCount() + 1;
        chatActivity.showMessage("Test that ***.");
        int after = adapter.getItemCount();
        assertEquals(calculated, after);
    }

    @Test
    public void testShowMessageFile() throws IOException {
        int calculated = adapter.getItemCount() + 1;
        File file = File.createTempFile("Test that ***.", ".jpeg", context.getFilesDir());
        chatActivity.showMessage(file);
        int after = adapter.getItemCount();
        assertEquals(calculated, after);
    }

    @Test
    public void testShowMessageUri() throws IOException {
        int calculated = adapter.getItemCount() + 1;
        File file = File.createTempFile("Test that ***.", ".jpeg", context.getFilesDir());
        chatActivity.showMessage(Uri.fromFile(file));
        int after = adapter.getItemCount();
        assertEquals(calculated, after);
    }

    @Test
    public void testShowDataList() throws IOException, NoSuchFieldException, IllegalAccessException {
        int before = adapter.getItemCount();
        chatActivity.showDataList(null);
        Field field = ChatRecyclerAdapter.class.getDeclaredField("mDataList");
        field.setAccessible(true);
        List<Data> dataList = (List<Data>) field.get(adapter);
        assertNull(dataList);
        chatActivity.showDataList(chat.getDataObjects());
        dataList = (List<Data>) field.get(adapter);
        assertTrue(dataList != null);
        int after = adapter.getItemCount();
        assertEquals(before, after);
    }

    @Test
    public void testUpdateDataSet() throws IllegalAccessException, NoSuchFieldException {
        chatActivity.onResume();
        chatActivity.showMessage("test");
        assertTrue(recyclerView.hasPendingAdapterUpdates());
        chatActivity.updateDataSet();
        //assertFalse(recyclerView.hasPendingAdapterUpdates());
        chatActivity.onStop();
    }


    @After
    public void tearDown() {
        this.chat = null;
        this.adapter = null;
        this.recyclerView = null;
        this.chatActivity = null;
        TestHelper.resetHelperFactory();
    }
}
