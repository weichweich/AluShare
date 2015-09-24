package edu.kit.tm.pseprak2.alushare.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.fragments.ChatTabFragment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by niklas on 01.09.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ChatTabFragmentTest {
    ChatTabFragment fragment;
    RecyclerView recyclerView;
    LinearLayoutManager manager;
    ChatTabRecyclerAdapter adapter;
    Context context = RuntimeEnvironment.application.getApplicationContext();
    MenuInflater inflater;
    Menu menu;
    ChatHelper helper;
    List<Chat> chats;
    AppCompatActivity activity;
    @Before
    public void setUp() {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");
        helper = HelperFactory.getChatHelper(context);
        chats = helper.getChats();

        activity = Robolectric.buildActivity(TestActivity.class).create().start().resume().get();
        fragment = ChatTabFragment.createInstance();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

        recyclerView = (RecyclerView) fragment.getView().findViewById(R.id.recyclerView);
        adapter = (ChatTabRecyclerAdapter) recyclerView.getAdapter();
        manager = (LinearLayoutManager) recyclerView.getLayoutManager();

        menu = new MenuBuilder(activity);
        inflater = new MenuInflater(activity);
    }

    @Test
    public void testComponents() {
        assertNotNull(recyclerView);
        assertNotNull(adapter);
        assertNotNull(manager);
        assertFalse(fragment.onQueryTextSubmit("asd"));
    }

    @Test  // ActionMenuItem...
    public void testOnCreateOptions() {
        fragment.onCreateOptionsMenu(menu, inflater);

        MenuItem item = new RoboMenuItem(R.id.action_add);
        fragment.onOptionsItemSelected(item);

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent i = shadowActivity.getNextStartedActivity();
        assertEquals(i.getComponent().getClassName(), ChooseContactActivity.class.getName());
    }

    @Test
    public void testSearch() {
        adapter.updateDataSet(chats);
        Chat chat = chats.get(0);
        fragment.onQueryTextChange(chat.getTitle());

        assertTrue(adapter.getItemCount() == 1);
    }

    @Test
    public void testlContextDelete() {
        adapter.updateDataSet(chats);
        String chatID = chats.get(0).getNetworkChatID();
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        adapter.onBindViewHolder(viewHolder, 0);

        try {
            viewHolder.itemView.performLongClick(); // Danke Robolectric. NullPointer weil irgendwas mit Menu buggy..
        } catch (NullPointerException e) {
            //Nichts tun.
        }

        RoboMenuItem item = new RoboMenuItem(R.id.context_tab_chat_delete);
        item.setGroupId(R.id.context_tab_chat_group);

        fragment.onContextItemSelected(item);
        assertNull(helper.getChat(chatID));
    }

    @Test
    public void testContextRename() {
        adapter.updateDataSet(chats);
        String newTitle = "NewTitle";
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        adapter.onBindViewHolder(viewHolder, 0);
        try {
            viewHolder.itemView.performLongClick(); // Danke Robolectric. NullPointer weil irgendwas mit Menu buggy..
        } catch (NullPointerException e) {
            //Nichts tun.
        }

        RoboMenuItem item = new RoboMenuItem(R.id.context_tab_chat_rename);
        item.setGroupId(R.id.context_tab_chat_group);

        fragment.onContextItemSelected(item);
        android.support.v7.app.AlertDialog dialog = (android.support.v7.app.AlertDialog) ShadowAlertDialog.getLatestDialog();

        Button b = dialog.getButton(Dialog.BUTTON_POSITIVE);
        EditText editText = (EditText) dialog.findViewById(R.id.alter_dialog_edit);
        editText.setText(newTitle);
        b.performClick();

        chats = helper.getChats();
        Chat chat = chats.get(0);
        assertEquals(chat.getTitle(), newTitle);

        b = dialog.getButton(Dialog.BUTTON_NEGATIVE);
        b.performClick();
        assertFalse(dialog.isShowing());
    }

    @Test
    public void testViewHolderClick() {
        adapter.updateDataSet(chats);
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        adapter.onBindViewHolder(viewHolder, 0);

        viewHolder.itemView.performClick();
        ShadowActivity a = Shadows.shadowOf(activity);
        Intent i = a.getNextStartedActivity();

        String chatID =  i.getExtras().getString("CHATID");
        Chat chat = chats.get(0);
        assertEquals(chatID, chat.getNetworkChatID());
    }


    @After
    public void TearDown() {
        TestHelper.resetHelperFactory();
    }
}
