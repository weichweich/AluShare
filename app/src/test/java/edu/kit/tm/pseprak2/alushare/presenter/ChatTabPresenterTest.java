package edu.kit.tm.pseprak2.alushare.presenter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.Log;

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
import edu.kit.tm.pseprak2.alushare.model.helper.SQLChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.SQLContactHelper;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.fragments.ChatTabFragment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Niklas Sänger
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ChatTabPresenterTest {
    Context context = RuntimeEnvironment.application;
    ChatTabPresenter presenter;
    ChatTabRecyclerAdapter adapter;
    ChatTabFragment fragment;
    ChatHelper helper;
    ContactHelper cHelper;
    @Before
    public void setUp() throws Exception {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");
        helper = new SQLChatHelper(context);
        cHelper = new SQLContactHelper(context);
        fragment = ChatTabFragment.createInstance();
        SupportFragmentTestUtil.startFragment(fragment);
        adapter = new ChatTabRecyclerAdapter(fragment);
        presenter = new ChatTabPresenter(fragment, adapter);



        Contact c1 = new Contact("kontakt1");
        Contact c2 = new Contact("kontakt2");
        List<Contact> cList = new ArrayList<>();
        cList.add(c1);
        cList.add(c2);
        Chat chat = new Chat("TestChat", "1", cList);
        helper.insert(chat);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testCreatePresenter() {
        presenter = new ChatTabPresenter(null, adapter);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreatePresenter2() {
        presenter = new ChatTabPresenter(fragment, null);
    }

    @Test
    public void testGetChats() {
        assertTrue(presenter.getChatList().size() == helper.getChats().size());
    }

    @Test
    public void testSearch1() {
        List<Chat> searched = presenter.getChatList("Chat-Title-0");
        assertTrue(searched.size() > 0);
    }

    @Test
    public void testSearch2() {
        List<Chat> searched = presenter.getChatList("falschername");
        assertTrue(searched.size() == 0);
    }

    @Test(expected = NullPointerException.class)
    public void testRenameChat() {
        Chat chatBefore = presenter.getChatList().get(0);
        String beforeNet = chatBefore.getNetworkChatID();
        String beforeTitle = chatBefore.getTitle();
        presenter.renameChat(chatBefore.getNetworkChatID(), "NeuerTitel"); //NullPointerException weil RoboLectric nicht im Ui Thread laufen kann.
    }

    @Test
    public void removeChat() {
        List<Chat> chats = presenter.getChatList();
        assertTrue(chats.size() > 0);
        Chat toRemove = chats.get(0);
        int sizeBefore = chats.size();
        presenter.removeChat(toRemove.getNetworkChatID());

        chats = presenter.getChatList();
        int sizeAfter = chats.size();
        assertTrue(sizeBefore == (sizeAfter + 1));
    }


    @After
    public void tearDown() {
        if (fragment != null) {
            FragmentManager m = fragment.getActivity().getSupportFragmentManager();
            m.beginTransaction().remove(fragment).commit();
        }
        this.helper = null;
        this.presenter = null;
        this.adapter = null;
        this.fragment = null;
        TestHelper.resetHelperFactory();
    }
}