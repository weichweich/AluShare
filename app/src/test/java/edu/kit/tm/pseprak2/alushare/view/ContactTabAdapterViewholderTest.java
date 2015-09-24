package edu.kit.tm.pseprak2.alushare.view;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;
import org.robolectric.util.FragmentTestUtil;

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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
/**
 * Created by niklas on 31.08.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ContactTabAdapterViewholderTest {
    TestFragment fragment;
    ContactTabRecyclerAdapter adapter;
    Context context = RuntimeEnvironment.application;
    ChatHelper chatHelper;
    ContactHelper contactHelper;
    List<Contact> contacts;
    LinearLayoutManager manager;
    RecyclerView recyclerView;

    @Before
    public void setUp() {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");
        chatHelper = HelperFactory.getChatHelper(context);
        contactHelper = HelperFactory.getContacHelper(context);
        contacts = contactHelper.getContacts();

        fragment = TestFragment.createInstance(TestFragment.TYPE_CONTACT_TAB);
        SupportFragmentTestUtil.startVisibleFragment(fragment);
        adapter = (ContactTabRecyclerAdapter) fragment.getAdapter();
        manager = fragment.getLayoutManager();
        recyclerView = fragment.getRecyclerView();
    }

    @Test
    public void testCreateViewholder() {
        adapter.updateDataSet(contacts);
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        assertNotNull(viewHolder);
        for(int i = 0; i < contacts.size(); i++) {
            adapter.onBindViewHolder(viewHolder,i);

            Contact c = contacts.get(i);
            String cName = c.getName(context);
            TextView v = (TextView) viewHolder.itemView.findViewById(R.id.contact_name);
            String vText = v.getText().toString();
            assertEquals(cName, vText);

            ImageView imageView = (ImageView) viewHolder.itemView.findViewById(R.id.contact_photo);
            assertTrue(imageView.getDrawable() != null);
        }
    }

    @Test (expected = NullPointerException.class)
    public void testClickViewholder() {
        adapter.updateDataSet(contacts);
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        assertNotNull(viewHolder);
        for(int i = 0; i < contacts.size(); i++) {
            adapter.onBindViewHolder(viewHolder,0);

            viewHolder.itemView.performClick();
            assertTrue(fragment.wasClicked());

            viewHolder.itemView.performLongClick();
            assertTrue(adapter.getId() == fragment.getClickID());
        }
    }

    @After
    public void tearDown() {
        TestHelper.resetHelperFactory();
    }
}
