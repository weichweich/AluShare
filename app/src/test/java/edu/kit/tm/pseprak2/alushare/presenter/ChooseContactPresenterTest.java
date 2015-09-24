package edu.kit.tm.pseprak2.alushare.presenter;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.ChooseContactActivity;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChooseContactAdapter;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)

public class ChooseContactPresenterTest {
    ChooseContactActivity activity;
    ContactHelper contactHelper;
    ChooseContactAdapter adapter;
    ChooseContactPresenter presenter;
    Context context = RuntimeEnvironment.application;
    ChatHelper chatHelper;
    @Before
    public void setUp(){
        Contact c1 = new Contact("Contact1");
        Contact c2 = new Contact("Contact2");
        Contact c3 = new Contact("Contact3");
        Contact c4 = new Contact("Contact4");
        c1.setSelected(true);
        c2.setSelected(true);
        c3.setSelected(false);
        c4.setSelected(true);
        activity = Robolectric.buildActivity(ChooseContactActivity.class).create().get();
        chatHelper = HelperFactory.getChatHelper(context);
        contactHelper = HelperFactory.getContacHelper(context);
        contactHelper.insert(c1);
        contactHelper.insert(c2);
        contactHelper.insert(c3);
        contactHelper.insert(c4);
        adapter = new ChooseContactAdapter(activity);
        presenter = new ChooseContactPresenter(activity, adapter);


    }

    @Test (expected = IllegalArgumentException.class)
    public void createChooseContactPresenterTest(){
        presenter = new ChooseContactPresenter(null, adapter);
    }

    @Test (expected = IllegalArgumentException.class)
    public void createChooseContactPresenterTest2(){
        presenter = new ChooseContactPresenter(activity, null);
    }

    @Test
    public void getContactListTest(){
        List<Contact> list = presenter.getContactList();
        assertEquals(4,list.size());
    }

    @Test
    public void getChosenContactsTest(){
        List<Contact> list =  presenter.getChoosenContacts();

        assertEquals(0, list.size());

    }

    @Test
    public void startChatTest(){
        presenter.startChat("newChat");
        contactHelper.setOwnNID("Tor");
        assertEquals(1,chatHelper.getChats().size());
    }

    @After
    public void teardown(){
        this.activity = null;
        this.contactHelper = null;
        this.adapter = null;
        this.presenter = null;
        TestHelper.resetHelperFactory();
    }
}
