package edu.kit.tm.pseprak2.alushare.presenter;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CreateContactPresenterTest {
    Context context = RuntimeEnvironment.application;
    CreateContactPresenter presenter;
    ContactHelper helper;
    String networkAddress = "torId";
    String lookUpKey = "Key";
    String newNetworkAddress = "newTorId";
    String newLookUpKey = "newKey";

    Contact contact;

    @Before
    public void setUp(){

        TestHelper.resetHelperFactory();
        DummyDataSet.initSelf(context);
        helper = HelperFactory.getContacHelper(context);
        contact = new Contact(lookUpKey,networkAddress);
        presenter = new CreateContactPresenter();
        presenter.onTakeView(context);
    }
    @Test(expected = IllegalArgumentException.class)
    public void onTakeViewTest(){
        presenter.onTakeView(null);
    }

    @Test
    public void getContactByNetworkAddress(){
        helper.insert(contact);
        Contact testContact = presenter.getContactByNetworkadress(networkAddress);
        assertEquals(testContact.getNetworkingId(), contact.getNetworkingId());
    }
    @Test
    public void getContactBySystemId(){
        helper.insert(contact);
        Contact testContact = presenter.getContactById(2);
        assertEquals(networkAddress,testContact.getNetworkingId());
        assertEquals(lookUpKey,testContact.getLookUpKey());
    }
    @Test
    public void updateContactTest(){
        helper.insert(contact);
        presenter.updateContact(contact, newNetworkAddress, newLookUpKey);
        Contact testContact = helper.getContactByID(2);
        assertEquals(testContact.getNetworkingId(),newNetworkAddress);
        assertEquals(testContact.getLookUpKey(),newLookUpKey);
    }

    @Test
    public void linkContactTest(){
        helper.insert(contact);
        presenter.linkContact(contact, newLookUpKey);
        Contact testContact = helper.getContactByID(2);
        assertEquals(networkAddress, testContact.getNetworkingId());
        assertEquals(newLookUpKey,testContact.getLookUpKey());
    }

    @Test
    public void contactInDatabaseTest(){
        helper.insert(contact);
        assertTrue(presenter.contactInDatabase(-1));
        assertFalse(presenter.contactInDatabase(5));

    }

    @Test
    public void createContactTest(){
        presenter.createContact(networkAddress,lookUpKey);
        Contact testContact = helper.getContactByID(2);
        assertEquals(networkAddress,testContact.getNetworkingId());
        assertEquals(lookUpKey,testContact.getLookUpKey());
    }

    @After
    public void teardown(){
        this.helper = null;
        this.presenter = null;
        this.contact = null;
        TestHelper.resetHelperFactory();
    }
}
