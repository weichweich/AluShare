package edu.kit.tm.pseprak2.alushare.view;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.View;

import java.util.ArrayList;

import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.presenter.CreateContactPresenter;


public class ContactManagement {
    private Context context;
    private Uri contactData;
    private CreateContactPresenter presenter;
    private long id = -1;
    private String networkAddress;
    private boolean contactAvailable = false;
    private int rawContactId;
    private long contactId;
    private long dataId;
    private String oldNetworkAddress;

    public ContactManagement(Context context, Uri contactData, String networkAddress) {
        this.context = context;
        this.contactData = contactData;
        this.networkAddress = networkAddress;
        presenter = new CreateContactPresenter();
        presenter.onTakeView(context);

    }

    public ContactManagement(Context context, Uri contactData, long id) {
        this.context = context;
        this.contactData = contactData;
        this.id = id;
        presenter = new CreateContactPresenter();
        presenter.onTakeView(context);
        this.networkAddress = presenter.getContactById(id).getNetworkingId();


    }

    public boolean contact() {
        Cursor c = context.getContentResolver().query(contactData, null, null, null, null);
        if (c.moveToFirst()) {
            String lookUpKey = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            contactId = c.getLong(c.getColumnIndex(ContactsContract.Contacts._ID));
            if (presenter.contactInDatabase(contactId)) {
                return false;
            }
            Cursor rawContactCursor = getRawContactCursor();
            if (rawContactCursor.moveToFirst()) {
                rawContactId = rawContactCursor.getInt(rawContactCursor.getColumnIndex(ContactsContract.RawContacts._ID));

                if (id != -1) {
                    if (hasEntry()) {
                        linkContactUpdateSystemContact(lookUpKey);
                        return true;
                    } else {
                        linkContact(lookUpKey);
                        return true;
                    }
                } else {
                    if (hasEntry()) {
                        this.updateContact(lookUpKey);
                        if (!contactAvailable) {
                            insertContact(lookUpKey);
                        }
                    } else {
                        insertContact(lookUpKey);
                    }

                    c.close();
                    rawContactCursor.close();

                }
            }
        }
        return true;
    }

    private boolean hasEntry() {
        Cursor findContact = getSystemContact();
        if (findContact.moveToFirst()) {
            if (("Alushare").equals(findContact.getString(findContact.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL)))) {
                oldNetworkAddress = findContact.getString(findContact.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                dataId = findContact.getLong(findContact.getColumnIndex(ContactsContract.Data._ID));
                contactAvailable = true;
                findContact.close();
                return true;
            } else {
                while (findContact.moveToNext()) {
                    if (("Alushare").equals(findContact.getString(findContact.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL)))) {
                        oldNetworkAddress = findContact.getString(findContact.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                        dataId = findContact.getLong(findContact.getColumnIndex(ContactsContract.Data._ID));
                        contactAvailable = true;
                        findContact.close();
                        return true;
                    }
                }
            }
        }
        findContact.close();
        return false;
    }

    private Cursor getSystemContact() {
        return context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Email.LABEL, ContactsContract.CommonDataKinds.Email.ADDRESS},
                ContactsContract.Data.CONTACT_ID + "=?" + " AND "
                        + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'",
                new String[]{String.valueOf(contactId)}, null);


    }

    private Cursor getRawContactCursor() {
        String[] projection = new String[]{ContactsContract.RawContacts._ID};
        String selection = ContactsContract.RawContacts.CONTACT_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(contactId)};

        return context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, projection, selection, selectionArgs, null);

    }

    private void insertContact(String lookUpKey) {
        insertSystemContact();
        presenter.createContact(this.networkAddress, lookUpKey);
    }

    private void updateContact(String lookUpKey) {
        Contact contact = presenter.getContactByNetworkadress(oldNetworkAddress);
        if (contact == null) {
            presenter.createContact(this.networkAddress, lookUpKey);
            this.updateSystemContact(this.networkAddress);
        } else {
            presenter.updateContact(contact, this.networkAddress, lookUpKey);
            this.updateSystemContact(this.networkAddress);
        }

    }

    private void updateSystemContact(String networkAddress) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.CommonDataKinds.Email.ADDRESS, networkAddress);
        context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values, ContactsContract.Data._ID + "=?", new String[]{String.valueOf(dataId)});
    }

    private void linkContact(String lookUpKey) {
        Contact contact = presenter.getContactById(id);
        this.networkAddress = contact.getNetworkingId();
        insertSystemContact();
        presenter.linkContact(contact, lookUpKey);
    }

    private void linkContactUpdateSystemContact(String lookUpKey) {
        Contact contact = presenter.getContactById(id);
        this.networkAddress = contact.getNetworkingId();
        updateSystemContact(networkAddress);
        presenter.linkContact(contact, lookUpKey);
    }

    private void insertSystemContact() {
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM)
                .withValue(ContactsContract.CommonDataKinds.Email.LABEL, "Alushare")
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, networkAddress)
                .build());

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {

        } catch (OperationApplicationException e) {

        }
    }


}
