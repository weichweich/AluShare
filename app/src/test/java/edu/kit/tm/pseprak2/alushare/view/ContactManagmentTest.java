package edu.kit.tm.pseprak2.alushare.view;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboCursor;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)

public class ContactManagmentTest {
    Context context = RuntimeEnvironment.application;
    String lookUpKey;
    Uri res;
    Uri dataUri;
    ContactManagement contactManagement;
    ContactHelper helper;
    String torId = "Tor-Id";


    @Before
    public void setUp(){
        TestHelper.resetHelperFactory();
        DummyDataSet.initSelf(context);
        RoboCursor cursor = new RoboCursor();
        String[] projection = new String[]{ContactsContract.RawContacts._ID};
        String selection = ContactsContract.RawContacts.CONTACT_ID + "=?";
        String[] selectionArgs = new String[]{};
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, 001);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, "1-800-GOOG-411");
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM);
        values.put(ContactsContract.CommonDataKinds.Phone.LABEL, "Test");
        dataUri = context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);;
        helper = HelperFactory.getContacHelper(context);
        //context.getContentResolver().insert(ContactsContract.Contacts.CONTENT_URI,);
       /* cursor.setQuery(ContactsContract.Contacts.CONTENT_URI,projection,selection,selectionArgs,null);
        if(cursor.moveToFirst()) {
            lookUpKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, this.lookUpKey);
            res = ContactsContract.Contacts.lookupContact(context.getContentResolver(), lookupUri);
            contactManagement = new ContactManagement(context, res, torId);
        }*/


    }
    @Test
    public void createTest(){
        contactManagement = new ContactManagement(context,dataUri,torId);
        assertNotNull(contactManagement);
    }

    @Test
    public void createTest2(){
        contactManagement = new ContactManagement(context,dataUri,1);
        assertNotNull(contactManagement);

    }

    @After
    public void tearDown(){
        TestHelper.resetHelperFactory();
        contactManagement = null;
        dataUri = null;
    }

}
