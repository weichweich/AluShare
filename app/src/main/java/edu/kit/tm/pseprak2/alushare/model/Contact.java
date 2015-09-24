package edu.kit.tm.pseprak2.alushare.model;


import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.ByteArrayInputStream;

import edu.kit.tm.pseprak2.alushare.R;

/**
 * Holds all necessary information about a contact that is currently
 * generated or got loaded from a data source.
 */
public class Contact {

    /**
     * Stores a long to Identifiy the Contact in the Database
     */
    private long id = -1;
    /**
     * Stores the Identifier given by the used network
     */
    private String networkingId;

    /**
     * Stores a key to find the Contact in the systems database
     */
    private String lookUpKey = "";
    private boolean selected = false;

    /**
     * Contstructor to create a Contact
     *
     * @param id           Id to identiy the Contact in the database
     * @param lookUpKey    Key to find the Contact in the systems database
     * @param networkingId Identifier given by the used network
     */
    public Contact(long id, String lookUpKey, String networkingId) {
        this(lookUpKey, networkingId);
        setId(id);
    }

    /**
     * Contstructor to create a Contact
     *
     * @param lookUpKey    Key to find the Contact in the systems database
     * @param networkingId Identifier given by the used network
     */
    public Contact(String lookUpKey, String networkingId) {
        this(networkingId);
        setLookUpKey(lookUpKey);
    }

    /**
     * Contstructor to create a Contact
     * <p/>
     * Key to find the Contact in the systems contact database
     *
     * @param networkingId Identifier given by the used network
     */
    public Contact(String networkingId) {
        setNetworkingId(networkingId);
    }

    /**
     * Method to return if contact is currently selected.
     * @return true if contact is selected, otherwise false.
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * Sets boolean selected value.
     * @param selected selected value to set.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    /**
     * Method to return the name of the system contact, specified by the LookUpKey
     *
     * @param context Context of the application
     * @return returns the name of the contact or the network address if the LookUpKey is empty
     */
    public String getName(Context context) {
        if (!this.lookUpKey.equals("")) {
            try {
                String name = "";
                Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, this.lookUpKey);
                Uri res = ContactsContract.Contacts.lookupContact(context.getContentResolver(), lookupUri);
                Cursor c = context.getContentResolver().query(res, null, null, null, null);
                if (c.moveToFirst()) {
                    name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                }
                c.close();
                return name;
            } catch (Exception e) {
                return networkingId;
            }

        } else {
            return networkingId;
        }
    }

    /**
     * Method to return the identifier of this contact in the system contact database.
     * <p/>
     * @param context Context of the application.
     * @return  -1 if this contact is not connected with a contact in the database of the system contact database, otherwise
     * the system contact database identifier.
     */
    public long getSystemContactId(Context context) {
        if (!this.lookUpKey.equals("")) {
            try {
                long id = -1;
                Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, this.lookUpKey);
                Uri res = ContactsContract.Contacts.lookupContact(context.getContentResolver(), lookupUri);
                Cursor c = context.getContentResolver().query(res, null, null, null, null);
                if (c.moveToFirst()) {
                    id = c.getLong(c.getColumnIndex(ContactsContract.Contacts._ID));
                }
                c.close();
                return id;
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Returns a bitmap of this contact.
     * <p/>
     * Can be a picture of the contact in the system contact database or a default icon
     * if this contact has no entry in the system contact database.
     * @param context Context of the application.
     * @return Icon that is set in the systems contact database, otherwise a default icon.
     */
    public Bitmap getPicture(Context context) {
        long id = this.getSystemContactId(context);
        if (id != -1) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
            Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            Cursor cursor = context.getContentResolver().query(photoUri,
                    new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
            if (cursor == null) {
                return null;
            }
            try {
                if (cursor.moveToFirst()) {
                    byte[] data = cursor.getBlob(0);
                    if (data != null) {
                        return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
    }


    /**
     * Returns the contacts identifier.
     * @return return the id
     */
    public long getId() {
        return this.id;
    }

    /**
     * Method checks if the give id is bigger than 0 and changes the contacts id
     *
     * @param id new id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the identifier that is used by the networking protocol.
     * @return the identifier that is used by the networking protocol.
     */
    public String getNetworkingId() {
        return this.networkingId;
    }

    /**
     * Method changes the contacts networking protocol identifier.
     *
     * @param networkingId the identifier for the networking protocol.
     */
    public void setNetworkingId(String networkingId) {
        this.networkingId = networkingId;
    }

    /**
     * Returns the identifier, that's used by the systems contact database.
     * @return the identifer of the systems contact database.
     */
    public String getLookUpKey() {
        return this.lookUpKey;
    }

    /**
     * Method changes the contacts lookUpKey
     *
     * @param lookUpKey new lookUpKey
     */
    public void setLookUpKey(String lookUpKey) {
        this.lookUpKey = lookUpKey;
    }

    @Override
    public boolean equals(Object other) {
        if (((Contact) other).getId() != this.id) {
            return false;
        } else if (!((Contact) other).getNetworkingId().equals(this.getNetworkingId())) {
            return false;
        }
        return true;
    }
}
