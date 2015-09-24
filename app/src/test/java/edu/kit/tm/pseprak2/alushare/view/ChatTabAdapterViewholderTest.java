package edu.kit.tm.pseprak2.alushare.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.widget.ImageView;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.viewholder.ChatTabRecyclerItemViewHolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by niklas on 29.08.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ChatTabAdapterViewholderTest {
    TestFragment fragment;
    ChatTabRecyclerAdapter adapter;
    Context context = RuntimeEnvironment.application;
    ChatHelper chatHelper;
    ContactHelper contactHelper;
    DataHelper dataHelper;
    List<Chat> chats;
    LinearLayoutManager manager;
    RecyclerView recyclerView;

    @Before
    public void setUp() {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");
        contactHelper = HelperFactory.getContacHelper(context);
        chatHelper = HelperFactory.getChatHelper(context);
        dataHelper = HelperFactory.getDataHelper(context);
        chats = chatHelper.getChats();

        fragment = TestFragment.createInstance(TestFragment.TYPE_CHAT_TAB);
        SupportFragmentTestUtil.startVisibleFragment(fragment);

        adapter = (ChatTabRecyclerAdapter) fragment.getAdapter();
        manager = fragment.getLayoutManager();
        recyclerView = fragment.getRecyclerView();
    }

    @Test
    public void testCreateViewHolder() {
        adapter.updateDataSet(chats);

        adapter.setOwnContact(contactHelper.getSelf());

        ChatTabRecyclerItemViewHolder viewHolder = (ChatTabRecyclerItemViewHolder) adapter.onCreateViewHolder(recyclerView, 0);
        Chat chat;
        for (int i = 0; i < chats.size(); i++) {
            chat = chats.get(i);

            adapter.onBindViewHolder(viewHolder, i);
            viewHolder.isGroup();
            viewHolder.setUnread();
            viewHolder.setDate("04:04");
            if(i%2 == 0) {
                viewHolder.isAdmin();
            }
            assertNotNull(viewHolder);

            TextView textView = (TextView) viewHolder.itemView.findViewById(R.id.text_title);
            assertTrue(textView.getText().equals(chats.get(i).getTitle()));

            textView = (TextView) viewHolder.itemView.findViewById(R.id.text_lastMessage);
            Data data = chat.getLastData();
            if (data == null) {
                assertTrue(textView.getText().equals(fragment.getString(R.string.no_message_received)));
            } else if (data.getText() != null) {
                String t = textView.getText().toString();
                String b = data.getText();
                assertEquals(t,b);
            } else if (data.getFile() != null) {
                assertTrue(textView.getText().equals(data.getFile().getName()));
            }

            ImageView imageView = (ImageView) viewHolder.itemView.findViewById(R.id.person_photo);
            assertNotNull(imageView);


            textView = (TextView) viewHolder.itemView.findViewById(R.id.chat_item_last_date);
            assertEquals("04:04", textView.getText());

        }

    }

    @Test(expected = NullPointerException.class)
    public void testClickViewHolder() {
        adapter.updateDataSet(chats);
        adapter.setOwnContact(contactHelper.getSelf());
        ChatTabRecyclerItemViewHolder viewHolder = null;

        for (int i = 0; i < chats.size(); i++) {
            viewHolder = (ChatTabRecyclerItemViewHolder) adapter.onCreateViewHolder(recyclerView, i);
            adapter.onBindViewHolder(viewHolder, i);

            viewHolder.itemView.performClick();
            assertTrue(fragment.wasClicked());

            viewHolder.itemView.performLongClick();
            assertTrue(chats.get((int) fragment.getClickID()).getNetworkChatID() == adapter.getId());
        }
    }

    @Test
    public void testGetChatIdent() {
        adapter.updateDataSet(chats);
        String i = adapter.getChatIdentByPos(0);
        assertEquals(chats.get(0).getNetworkChatID(), i);
    }

    @Test
    public void testUpdateChat() {
        adapter.updateDataSet(chats);
        Chat c = chats.get(1);
        String id = c.getNetworkChatID();
        Data d = chats.get(chats.size() -1).getLastData();

        adapter.updateChat(c.getNetworkChatID(), d);

        assertTrue(adapter.getList().get(0).getNetworkChatID().equals(id));
    }

    @Test
    public void testUpdateData() {
        adapter.updateDataSet(chats);
        Contact own = DummyDataSet.initSelf(context);
        Chat chat = chats.get(0);
        adapter.updateData(chat.getNetworkChatID(), chat);

        adapter.updateData("asd", chats.get(0));
    }

    @Test
    public void testUpdateChatData() {
        adapter.updateDataSet(chats);
        Chat c = chats.get(0);
        Data d = c.getLastData();
        d.setText("neuerText");
        dataHelper.update(d);
        adapter.updateChat(d);
        assertTrue(adapter.getList().get(0).getLastData().getText().equals("neuerText"));

    }
    @Test
    public void testSetBitmap() {
        adapter.updateDataSet(chats);
        ChatTabRecyclerItemViewHolder viewHolder = (ChatTabRecyclerItemViewHolder) adapter.onCreateViewHolder(recyclerView, 0);
        viewHolder.setItemImage(null);
        adapter.onBindViewHolder(viewHolder, 0);

        ImageView imageView = (ImageView) viewHolder.itemView.findViewById(R.id.person_photo);
        assertNotNull(imageView);
    }
    @After
    public void tearDown() {
        TestHelper.resetHelperFactory();
    }
}
