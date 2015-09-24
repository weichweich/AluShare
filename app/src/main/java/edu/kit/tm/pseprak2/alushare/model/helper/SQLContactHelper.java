package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Contact;

import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_CHAT_NETWORK_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_CONTACT_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_DATA_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_NETWORKING_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_RAW_CONTACT_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.LIMIT;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.OFFSET;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.TABLE_CONTACT;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.TABLE_CONTACT_CHAT;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.TABLE_DATA_STATE;

/**
 * Uses a SQLite database as the data source for inserting/deleting/updating contacts.
 */
public class SQLContactHelper extends ContactHelper {
    private static final String TAG = "SQLContactHelper";
    private Context mContext;
    private SQLDatabaseHelper dbHelper;
    private String ownNID;
    private static final String CONTACT_FILENAME = "own_name";

    /**
     * Creates an instance with a given context.
     * @param context context of the application.
     */
    public SQLContactHelper(Context context) {
        dbHelper = new SQLDatabaseHelper(context);
        this.mContext = context;

        try {
            FileInputStream fis = new FileInputStream(new File(context.getFilesDir() + "/" + CONTACT_FILENAME));
            ObjectInputStream ois = new ObjectInputStream(fis);
            ownNID = (String) ois.readObject();
            ois.close();
        } catch (Exception ex) {
            Log.i(TAG, "Own network identifier not found.");
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert(Contact contact) {
        if (contact != null) {
            if (contact.getId() != -1) {
                update(contact);
            } else {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long id = db.insert(TABLE_CONTACT, null, contactToContentValue(contact));
                if (id > -1) {
                    contact.setId(id);
                    notifyInserted(contact);
                }
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void update(Contact contact) {
        if (contact != null) {
            if (contact.getId() > -1) {
                if (exist(contact)) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.update(TABLE_CONTACT, contactToContentValue(contact), KEY_CONTACT_ID + " = " + contact.getId(), null);

                    notifyUpdated(contact);
                }
            } else {
                insert(contact);
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete(Contact contact) {
        if (contact != null) {
            if (contact.getId() > -1) {
                if (exist(contact)) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete(TABLE_CONTACT, KEY_CONTACT_ID + " = " + contact.getId(), null);

                    notifyRemoved(contact);
                }
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean exist(Contact contact) {
        String selectQuery = "SELECT 1 FROM " + TABLE_CONTACT +
                " WHERE " + KEY_CONTACT_ID + " = " + contact.getId();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        boolean exists = c.moveToFirst();
        c.close();

        return exists;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Contact> getContacts() {
        return getContacts(-1, -1);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Contact> getContacts(int limit, int offset) {
        limit = (limit > 0) ? limit : LIMIT;
        offset = (offset >= 0) ? offset : OFFSET;

        String selectQuery = "SELECT * FROM " + TABLE_CONTACT;
        if (ownNID != null) {
            selectQuery += " WHERE " + KEY_NETWORKING_ID + " NOT LIKE \"" + ownNID + "\"";
        }
        selectQuery += " LIMIT " + limit
                + " OFFSET " + offset + ";"; // TODO Sortierung?

        return doReadQuery(selectQuery);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Contact> getContactsByNetworkChatID(String networkChatID) {
        String selectQuery = "SELECT * FROM " + TABLE_CONTACT + " NATURAL JOIN " + TABLE_CONTACT_CHAT +
                " WHERE " + KEY_CHAT_NETWORK_ID + " = '" + networkChatID + "';";

        return doReadQuery(selectQuery);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Contact getSelf() {
        return getContactByNetworkingID(ownNID);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setOwnNID(String newOwnNID) {
        if (newOwnNID != null) {
            if (!newOwnNID.equals(ownNID)) {

                Contact curSelf = getSelf();
                this.ownNID = newOwnNID;
                if (curSelf == null) {
                    insert(new Contact(newOwnNID));
                } else {
                    curSelf.setNetworkingId(newOwnNID);
                }

                try {
                    FileOutputStream fis = new FileOutputStream(new File(mContext.getFilesDir() + "/" + CONTACT_FILENAME));
                    ObjectOutputStream oos = new ObjectOutputStream(fis);
                    oos.writeObject(ownNID);
                    oos.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean isContactInAnyChat(long contactID) {
        String selectQuery = "SELECT 1 FROM " + TABLE_CONTACT_CHAT +
                " WHERE " + KEY_CONTACT_ID + " = " + contactID;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        boolean exists = c.moveToFirst();
        c.close();

        return exists;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Contact> getContactByData(long dataID) {
        String selectQuery = "SELECT * FROM " + TABLE_CONTACT + " NATURAL JOIN " + TABLE_DATA_STATE +
                " WHERE " + KEY_DATA_ID + " = '" + dataID + "';";

        return doReadQuery(selectQuery);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Contact getContactByID(long contactID) {
        String selectQuery = "SELECT * FROM " + TABLE_CONTACT +
                " WHERE " + KEY_CONTACT_ID + " = " + contactID;

        List<Contact> contacts = doReadQuery(selectQuery);
        return (contacts.size() > 0) ? contacts.get(0) : null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Contact getContactByNetworkingID(String networkingID) {
        String selectQuery = "SELECT * FROM " + TABLE_CONTACT +
                " WHERE " + KEY_NETWORKING_ID + " = '" + networkingID + "';";

        List<Contact> contacts = doReadQuery(selectQuery);
        return (contacts.size() > 0) ? contacts.get(0) : null;
    }

    private List<Contact> doReadQuery(String query) {
        List<Contact> contacts = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            do {
                contacts.add(cursorToContact(c));
            } while (c.moveToNext());
        }

        c.close();
        return contacts;
    }

    private Contact cursorToContact(Cursor c) {
        long id = c.getLong(c.getColumnIndex(KEY_CONTACT_ID));
        String networking_Id = c.getString(c.getColumnIndex(KEY_NETWORKING_ID));
        String raw_contact_Id = c.getString(c.getColumnIndex(KEY_RAW_CONTACT_ID));

        return new Contact(id, raw_contact_Id, networking_Id);
    }

    private ContentValues contactToContentValue(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(KEY_NETWORKING_ID, contact.getNetworkingId());
        values.put(KEY_RAW_CONTACT_ID, contact.getLookUpKey());
        return values;
    }
}