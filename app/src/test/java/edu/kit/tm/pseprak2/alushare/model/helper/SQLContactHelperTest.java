package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by dominik on 09.07.15.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class SQLContactHelperTest {
    private Context mContext = RuntimeEnvironment.application;
    private ContactHelper contactHelper;
    private DataHelper dataHelper;
    private ChatHelper chatHelper;


    private Contact newContact;

    @Before
    public void setUp() throws Exception {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");
        contactHelper = HelperFactory.getContacHelper(mContext);
        dataHelper = HelperFactory.getDataHelper(mContext);

        newContact = new Contact("sdferguurgehti.onion");
    }

    @Test
    public void testInsertContactNotAlreadyInDB() {
        contactHelper.insert(newContact);
        assertFalse(newContact.getId() == -1);
    }

    @Test
    public void testInsertContactAlreadyInDB() {
        Contact alreadyInDB = contactHelper.getContactByID(1);
        alreadyInDB.setLookUpKey("asd");
        contactHelper.insert(alreadyInDB);
        Contact tmp = contactHelper.getContactByID(alreadyInDB.getId());
        assertEquals("asd", tmp.getLookUpKey());
    }

    @Test
    public void testUpdateContactNotAlreadyInDB() {
        contactHelper.update(newContact);
        assertFalse(newContact.getId() == -1);
    }

    @Test
    public void testUpdateContactAlreadyInDB() {
        Contact alreadyInDB = contactHelper.getContactByID(1);
        alreadyInDB.setLookUpKey("asd");
        contactHelper.update(alreadyInDB);
        Contact tmp = contactHelper.getContactByID(alreadyInDB.getId());
        assertEquals("asd", tmp.getLookUpKey());
    }

    @Test
    public void testDelete() {
        Contact alreadyInDB = contactHelper.getContactByID(1);
        contactHelper.delete(alreadyInDB);
        Contact tmp = contactHelper.getContactByID(alreadyInDB.getId());
        assertEquals(null, tmp);
    }

    @Test
    public void testExistContactInDB() {
        Contact alreadyInDB = contactHelper.getContactByID(1);
        assertTrue(contactHelper.exist(alreadyInDB));
    }

    @Test
    public void testGetContacts() {
        contactHelper.setOwnNID("ownId.onion");
        assertEquals(15, contactHelper.getContacts().size());
    }

    @Test
    public void testSetOwnNIDAndGetOwnNID() {
        contactHelper.setOwnNID("newId.onion");
        assertEquals("newId.onion", contactHelper.getSelf().getNetworkingId());
    }

    @Test
    public void testOwnNIDFileShouldBeSet() {
        contactHelper.setOwnNID("newOwnId.onion");
        SQLContactHelper newHelper = new SQLContactHelper(mContext);
        assertEquals("newOwnId.onion", newHelper.getSelf().getNetworkingId());
    }

    @Test
    public void testGetContactByData() {
        assertEquals(6, contactHelper.getContactByData(1).size());
    }

    @Test
    public void testGetContactsByNetworkChatID() {
        assertEquals(8, contactHelper.getContactsByNetworkChatID("fq28jalv3mprq0out8ncjr3ij2.onion:6d5t2kbd6k5i5cegfftckrfpe7").size());
    }

    @Test
    public void testGetContactByID() {
        for (int i = 1; i < 17; i++) {
            assertFalse(null == contactHelper.getContactByID(i));
        }
    }

    @Test
    public void testGetContactByNetworkingID() {
        assertNotNull(contactHelper.getContactByNetworkingID("kd4i2qpmfbtkqg9vutd92j7ob5.onion"));
    }

    @Test
    public void testIsContactInAnyChat() {
        assertFalse(contactHelper.isContactInAnyChat(newContact.getId()));
    }

    @After
    public void tearDown() throws Exception {
        TestHelper.resetHelperFactory();
    }

}
