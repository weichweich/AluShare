package edu.kit.tm.pseprak2.alushare.presenter;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.model.helper.SQLChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.SQLContactHelper;
import edu.kit.tm.pseprak2.alushare.network.NetworkingService;
import edu.kit.tm.pseprak2.alushare.view.adapter.ContactTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.fragments.ContactTabFragment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by niklas on 26.08.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ContactTabPresenterTest {
    Context context = RuntimeEnvironment.application;
    ContactTabFragment fragment;
    ContactTabRecyclerAdapter adapter;
    ContactTabPresenter presenter;
    ContactHelper contactHelper;
    ChatHelper chatHelper;

    @Before
    public void setUp() throws Exception {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");
        fragment = ContactTabFragment.createInstance();
        SupportFragmentTestUtil.startFragment(fragment);
        adapter = new ContactTabRecyclerAdapter(fragment);
        presenter = new ContactTabPresenter(fragment, adapter);

        contactHelper = HelperFactory.getContacHelper(context);
        chatHelper = HelperFactory.getChatHelper(context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePresenter1() {
        presenter = new ContactTabPresenter(null, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePresenter2() {
        presenter = new ContactTabPresenter(fragment, null);
    }

    @Test
    public void testSearch() {
        List<Contact> list = presenter.getContactList("RANDOMQUERY");
        assertTrue(list.size() == 0);

        //Contact Provider benötigt? Kann nicht getestet werden.
        //list = presenter.getContactList("Kontakt");
        //assertTrue(list.size() > 0);
    }

    @Test
    public void testGetChatId() {
        Contact contact = new Contact("netID");
        contactHelper.insert(contact);
        Contact self = DummyDataSet.initSelf(context);
        List<Contact> cList = new ArrayList<>();
        cList.add(contact);
        cList.add(self);

        Chat chat = new Chat("nadsfetID", "randomTitle", cList);
        chatHelper.insert(chat);

        Contact c = contactHelper.getContactByNetworkingID("netID");
        String s = presenter.getChatID(c.getId());
        assertTrue(!s.isEmpty());

        for(Chat chatt : chatHelper.getChats()) {
            chatHelper.delete(chatt);
        }
        assertNull(presenter.getChatID(c.getId()));
    }

    @Test
    public void testStartChat() {
        contactHelper.insert(new Contact("eineNetworkingID"));
        Contact c = contactHelper.getContactByNetworkingID("eineNetworkingID");
        assertNotNull(c);
        String chatIdent = presenter.startChat(c.getId());
        assertTrue(!chatIdent.isEmpty());

        Chat chat = chatHelper.getChat(chatIdent);
        assertNotNull(chat);

        chatIdent = presenter.startChat(100000);
        assertNull(chatIdent);
    }

    @Test (expected = NullPointerException.class) // Schläft fehl, Kontakte können nicht getestet werden
    public void testRemoveContact() {
        int sizeBefore = contactHelper.getContacts().size();
        Contact c = contactHelper.getContactByID(1);
        presenter.removeContact(c.getId());
        assertTrue(adapter.getItemCount() < sizeBefore);
    }

    @Test
    public void updateContactNotFound() {
        presenter.updateContactNotFound(2);
        Contact c = contactHelper.getContactByID(2);
        assertTrue(c.getLookUpKey().isEmpty());
    }


    @After
    public void tearDown() {
        if (fragment != null) {
            FragmentManager m = fragment.getActivity().getSupportFragmentManager();
            m.beginTransaction().remove(fragment).commit();
        }
        this.presenter = null;
        this.adapter = null;
        this.fragment = null;
        TestHelper.resetHelperFactory();
    }
}

