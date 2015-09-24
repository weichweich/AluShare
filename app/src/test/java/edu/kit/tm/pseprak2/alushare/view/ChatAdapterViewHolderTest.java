package edu.kit.tm.pseprak2.alushare.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

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
import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.MockNetProtocol;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.viewholder.ChatRecyclerItemViewHolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by arthur on 06.09.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ChatAdapterViewHolderTest {

    private Context context = RuntimeEnvironment.application;
    private ChatActivity chatActivity;
    private RecyclerView recyclerView;
    private ChatRecyclerAdapter adapter;
    private Chat chat;
    private static final String FILES_DIR = "src/test/resources/";
    private static final int TEST_FILE_COUNT = 4;

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

    @Test
    public void testCreateViewHolder() throws NoSuchFieldException, IllegalAccessException, IOException {
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        assertTrue(getDataID(viewHolder) == -1);
        assertTrue(getFileID(viewHolder) == -1);
        Data data;
        chatActivity.showMessage("hello");
        chatActivity.showMessage(File.createTempFile("testFile", ".jpeg", context.getFilesDir()));
        for(int i = 1; i <= TEST_FILE_COUNT; i++) {
            chatActivity.showMessage(new File(FILES_DIR + "Datei-" + ((i % TEST_FILE_COUNT) + 1)));
        }
        chat = HelperFactory.getChatHelper(context).getChat(chat.getNetworkChatID());
        adapter.setDataList(chat.getDataObjects());
        if(chat.getDataObjects() != null) {
            for (int i = 0; i < chat.getDataObjects().size(); i++) {
                data = chat.getDataObjects().get(i);
                adapter.onBindViewHolder(viewHolder, i);
                assertNotNull(viewHolder);
                assertEquals(data.getId(), getDataID(viewHolder));
                if(data.getFile() == null) {
                    assertTrue(getFileID(viewHolder) == -1);
                } else {
                    assertTrue(getFileID(viewHolder) == data.getFile().getId());
                }
            }
        }
    }

    public long getFileID(RecyclerView.ViewHolder holder) throws NoSuchFieldException, IllegalAccessException {
        Field field = ChatRecyclerItemViewHolder.class.getDeclaredField("fileID");
        field.setAccessible(true);
        return (long) field.get(holder);
    }

    public long getDataID(RecyclerView.ViewHolder holder) throws NoSuchFieldException, IllegalAccessException {
        Field field = ChatRecyclerItemViewHolder.class.getDeclaredField("dataID");
        field.setAccessible(true);
        return (long) field.get(holder);
    }

    @Test
    public void testGetItemCount(){
        assertTrue(chat.getDataObjects().size() == adapter.getItemCount());
    }

    @Test
    public void testSetDataList() throws NoSuchFieldException, IllegalAccessException {
        adapter.setDataList(null);
        Field field = ChatRecyclerAdapter.class.getDeclaredField("mDataList");
        field.setAccessible(true);
        List<Data> dataList =  (List<Data>) field.get(adapter);
        assertEquals(dataList, null);
        adapter.setDataList(chat.getDataObjects());
        dataList =  (List<Data>) field.get(adapter);
        assertEquals(dataList, chat.getDataObjects());
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
