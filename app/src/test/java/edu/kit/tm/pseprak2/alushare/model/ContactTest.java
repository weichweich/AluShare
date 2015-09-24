package edu.kit.tm.pseprak2.alushare.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.R;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ContactTest {
    private Context mContext = RuntimeEnvironment.application;
    private long expectedId = 0;
    private String expectedNetworkingId = "torid.onion";
    private String expectedLookUpKey = "rnfodsafpqepo1ejn0i3";
    private boolean expectedSelected = false;
    private Contact expectedContact = new Contact(expectedId, expectedLookUpKey, expectedNetworkingId);

    @Test
    public void testConstructorWithIdAndLookUpKeyAndNetworkingId() {
        assertEquals(expectedId, expectedContact.getId());
        assertEquals(expectedLookUpKey, expectedContact.getLookUpKey());
        assertEquals(expectedNetworkingId, expectedContact.getNetworkingId());
        assertEquals(false, expectedContact.isSelected());
    }

    @Test
    public void testConstructorWithLookUpKeyAndNetworkingId() {
        expectedContact = new Contact(expectedLookUpKey, expectedNetworkingId);

        assertEquals(-1, expectedContact.getId());
        assertEquals(expectedLookUpKey, expectedContact.getLookUpKey());
        assertEquals(expectedNetworkingId, expectedContact.getNetworkingId());
        assertEquals(false, expectedContact.isSelected());
    }

    @Test
    public void testConstructorWithNetworkingId() {
        expectedContact = new Contact(expectedNetworkingId);

        assertEquals(-1, expectedContact.getId());
        assertEquals("", expectedContact.getLookUpKey());
        assertEquals(expectedNetworkingId, expectedContact.getNetworkingId());
        assertEquals(false, expectedContact.isSelected());
    }

    @Test
    public void testSetSelectedAndIsSelected() {
        expectedContact.setSelected(true);
        assertEquals(true, expectedContact.isSelected());
    }

    @Test
    public void testGetNameWithNoEntryInSystemContactDatabaseAndWrongLookUpKey() {
        assertEquals(expectedNetworkingId, expectedContact.getName(mContext));
    }

    @Test
    public void testGetNameShouldReturnNIDWhenLookUpKeyDoesntExist() {
        expectedContact.setLookUpKey("");
        assertEquals(expectedNetworkingId, expectedContact.getName(mContext));
    }

    @Test
    public void testGetSystemContactIdWithNoEntryInSystemContactDatabaseAndWrongLookUpKey() {
        assertEquals(-1, expectedContact.getSystemContactId(mContext));
    }

    @Test
    public void testGetSystemContactIdWithNoLookUpKey() {
        expectedContact.setLookUpKey("");
        assertEquals(-1, expectedContact.getSystemContactId(mContext));
    }

    @Test
    public void testGetPicturedWithNoSystemContactId() {
        assertEquals(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher), expectedContact.getPicture(mContext));
    }

    @Test
    public void testEqualsWithCorrecIdAndNetworkingId() {
        Contact compareThisContact = new Contact(expectedId, expectedLookUpKey, expectedNetworkingId);
        assertTrue(compareThisContact.equals(expectedContact));
    }

    @Test
    public void testEqualsWithWrongId() {
        Contact compareThisContact = new Contact(20, expectedLookUpKey, expectedNetworkingId);
        assertFalse(compareThisContact.equals(expectedContact));
    }

    @Test
    public void testEqualsWithWrongNetworkingId() {
        Contact compareThisContact = new Contact(expectedId, expectedLookUpKey, "werner");
        assertFalse(compareThisContact.equals(expectedContact));
    }
}
