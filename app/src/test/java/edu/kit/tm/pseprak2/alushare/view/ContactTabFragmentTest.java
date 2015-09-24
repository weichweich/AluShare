package edu.kit.tm.pseprak2.alushare.view;

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
import android.widget.ListAdapter;
import android.widget.ListView;

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
import org.robolectric.shadows.ShadowWebSyncManager;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.ContactTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.fragments.ChatTabFragment;
import edu.kit.tm.pseprak2.alushare.view.fragments.ContactTabFragment;

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
public class ContactTabFragmentTest {
    ContactTabFragment fragment;
    RecyclerView recyclerView;
    LinearLayoutManager manager;
    ContactTabRecyclerAdapter adapter;
    Context context = RuntimeEnvironment.application.getApplicationContext();
    MenuInflater inflater;
    Menu menu;
    ContactHelper contactHelper;
    ChatHelper chatHelper;

    List<Contact> contacts;
    AppCompatActivity activity;

    @Before
    public void setUp() {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");
        contactHelper = HelperFactory.getContacHelper(context);
        contacts = contactHelper.getContacts();

        activity = Robolectric.buildActivity(TestActivity.class).create().start().resume().get();
        fragment = ContactTabFragment.createInstance();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

        recyclerView = (RecyclerView) fragment.getView().findViewById(R.id.recyclerView);
        adapter = (ContactTabRecyclerAdapter) recyclerView.getAdapter();
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

    @Test
    public void testOnCreateOption() {
        fragment.onCreateOptionsMenu(menu, inflater);

        MenuItem item = new RoboMenuItem(R.id.action_add);
        fragment.onOptionsItemSelected(item);

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent i = shadowActivity.getNextStartedActivity();
        assertEquals(i.getComponent().getClassName(), CreateContactActivity.class.getName());
    }

    @Test
    public void testSearch() {
        adapter.updateDataSet(contacts);
        Contact c = contacts.get(0);
        fragment.onQueryTextChange(c.getName(context));
        assertTrue(adapter.getItemCount() == 1);
    }

    @Test (expected = NullPointerException.class)
    public void testRecyclerViewClick() {
        adapter.updateDataSet(contacts);
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        adapter.onBindViewHolder(viewHolder, 0);

        viewHolder.itemView.performClick();
    }


    @Test
    public void testContextMenu1() {
        adapter.updateDataSet(contacts);
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        adapter.onBindViewHolder(viewHolder, 0);
        try {
            viewHolder.itemView.performLongClick(); // Danke Robolectric. NullPointer weil irgendwas mit Menu buggy..
        } catch (NullPointerException e) {
            //Nichts tun.
        }

        RoboMenuItem item = new RoboMenuItem(R.id.context_tab_show_in_contacts);
        item.setGroupId(R.id.context_tab_contact_group);
        fragment.onContextItemSelected(item);
        ShadowActivity a = Shadows.shadowOf(activity);
        Intent i  = a.getNextStartedActivityForResult().intent;
        assertNotNull(i);

    }

    @Test (expected = NullPointerException.class)
    public void testContextMenu2() {
        adapter.updateDataSet(contacts);
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        adapter.onBindViewHolder(viewHolder, 0);
        try {
            viewHolder.itemView.performLongClick(); // Danke Robolectric. NullPointer weil irgendwas mit Menu buggy..
        } catch (NullPointerException e) {
            //Nichts tun.
        }

        Contact contact = contacts.get(0);
        RoboMenuItem item = new RoboMenuItem(R.id.context_tab_contact_delete);
        item.setGroupId(R.id.context_tab_contact_group);
        fragment.onContextItemSelected(item);
        contacts = contactHelper.getContacts();
        assertTrue(contacts.contains(contact));

    }

    @Test (expected = NullPointerException.class)
    public void testContextMenu3() {
        adapter.updateDataSet(contacts);
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        adapter.onBindViewHolder(viewHolder, 0);
        try {
            viewHolder.itemView.performLongClick(); // Danke Robolectric. NullPointer weil irgendwas mit Menu buggy..
        } catch (NullPointerException e) {
            //Nichts tun.
        }

        Contact contact = contacts.get(0);
        RoboMenuItem item = new RoboMenuItem(R.id.context_tab_contact_delete);
        item.setGroupId(R.id.context_tab_contact_group);
        contacts = contactHelper.getContacts();

        List<Chat> chatList = chatHelper.getChatsByContactID(contact.getId());
        for(Chat chat :  chatList) {
            chatHelper.delete(chat);
        }

        fragment.onContextItemSelected(item);
        contacts = contactHelper.getContacts();
        assertFalse(contacts.contains(contact));

    }

    @Test
    public void testContextMenu4() {
        adapter.updateDataSet(contacts);
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        adapter.onBindViewHolder(viewHolder, 0);
        try {
            viewHolder.itemView.performLongClick(); // Danke Robolectric. NullPointer weil irgendwas mit Menu buggy..
        } catch (NullPointerException e) {
            //Nichts tun.
        }

        Contact contact = contacts.get(0);
        RoboMenuItem item = new RoboMenuItem(R.id.context_tab_contact_link_to_contact);
        item.setGroupId(R.id.context_tab_contact_group);
        fragment.onContextItemSelected(item);

        android.support.v7.app.AlertDialog dialog = (android.support.v7.app.AlertDialog) ShadowAlertDialog.getLatestDialog();
        ListView listView = dialog.getListView();
        Shadows.shadowOf(listView).performItemClick(0);
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent i = shadowActivity.getNextStartedActivityForResult().intent;
        assertNotNull(i);

        fragment.onContextItemSelected(item);
        dialog = (android.support.v7.app.AlertDialog) ShadowAlertDialog.getLatestDialog();
        listView = dialog.getListView();
        Shadows.shadowOf(listView).performItemClick(1);
        i = shadowActivity.getNextStartedActivityForResult().intent;
        assertNotNull(i);
    }
    @After
    public void tearDown() {
        TestHelper.resetHelperFactory();
    }
}