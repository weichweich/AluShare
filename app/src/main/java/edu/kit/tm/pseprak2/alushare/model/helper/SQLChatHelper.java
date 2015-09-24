package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;

import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_CHAT_NETWORK_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_CHAT_TITLE;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_CONTACT_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_TIMESTAMP;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.LIMIT;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.OFFSET;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.TABLE_CHAT;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.TABLE_CONTACT_CHAT;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_CHAT_DELETED;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.TABLE_DATA;

/**
 * Uses a SQLite database as the data source for inserting/deleting/updating chats.
 */
public class SQLChatHelper extends ChatHelper {
    private static final String TAG = "SQLChatHelper";
    private Context mContext;
    private SQLDatabaseHelper dbHelper;

    public SQLChatHelper(Context context) {
        dbHelper = new SQLDatabaseHelper(context);
        this.mContext = context;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void unsafeInsert(Chat chat) {
        if (chat != null) {
            if (exist(chat)) {
                unsafeUpdate(chat);
            } else {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long id = db.insert(TABLE_CHAT, null, chatToContentValue(chat));
                if (id > -1) {
                    insertChatContacts(chat);
                    insertChatData(chat);

                    finishedInsertingChat(chat);

                    releaseChat(chat.getNetworkChatID());
                }
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void unsafeUpdate(Chat chat) {
        if (chat != null) {
            if (exist(chat)) {
                boolean wasDeleted = isDeleted(chat);

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.update(TABLE_CHAT, chatToContentValue(chat), KEY_CHAT_NETWORK_ID + " LIKE '" + chat.getNetworkChatID() + "'", null);

                insertChatData(chat);
                updateMtoNforChatAndContact(chat);

                if (wasDeleted) {
                    finishedInsertingChat(chat);
                } else {
                    finishedUpdatingChat(chat);
                }
            } else {
                unsafeInsert(chat);
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void unsafeDelete(Chat chat) {
        if (chat != null) {
            if (exist(chat)) {
                chat.setIsDeleted(true);

                if (chat.isGroupChat()) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.update(TABLE_CHAT, chatToContentValue(chat), KEY_CHAT_NETWORK_ID + " LIKE '" + chat.getNetworkChatID() + "'", null);
                } else {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete(TABLE_CHAT, KEY_CHAT_NETWORK_ID + " LIKE '" + chat.getNetworkChatID() + "'", null);
                }

                deleteChatData(chat);
                deleteMtoNforChatAndContact(chat);

                finishedDeletingChat(chat);
            } else {
                releaseChat(chat.getNetworkChatID());
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean exist(Chat chat) {
        String selectQuery = "SELECT 1 FROM " + TABLE_CHAT +
                " WHERE " + KEY_CHAT_NETWORK_ID + " LIKE '" + chat.getNetworkChatID() + "'";

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
    public Chat getChat(String chatNetworkID) {
        String selectQuery = "SELECT * FROM " + TABLE_CHAT +
                " WHERE " + KEY_CHAT_NETWORK_ID + " LIKE '" + chatNetworkID + "'" +
                " AND " + KEY_CHAT_DELETED + " = 0;";

        List<Chat> chats = doReadQuery(selectQuery, true);

        if (chats.size() != 1) {
            return null;
        }
        return chats.get(0);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Chat getChatWithoutData(String chatNetworkID) {
        String selectQuery = "SELECT * FROM " + TABLE_CHAT +
                " WHERE " + KEY_CHAT_NETWORK_ID + " LIKE '" + chatNetworkID + "'" +
                " AND " + KEY_CHAT_DELETED + " = 0;";

        List<Chat> chats = doReadQuery(selectQuery, false);

        if (chats.size() != 1) {
            return null;
        }
        return chats.get(0);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Chat> getChats() {
        return getChats(-1, -1);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Chat> getChats(int limit, int offset) {
        limit = (limit > 0) ? limit : LIMIT;
        offset = (offset >= 0) ? offset : OFFSET;

        String selectQuery = "SELECT * FROM " + TABLE_CHAT +
                " LEFT OUTER JOIN " + TABLE_DATA + " USING(" + KEY_CHAT_NETWORK_ID + ") " +
                " WHERE " + KEY_CHAT_DELETED + " = 0 " +
                " GROUP BY " + KEY_CHAT_NETWORK_ID + " ORDER BY " + KEY_TIMESTAMP + " DESC " +
                " LIMIT " + limit +
                " OFFSET " + offset + ";"; // TODO Sortierung?
        return doReadQuery(selectQuery, true);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Chat> getChatsByTitle(String title, int limit, int offset) {
        limit = (limit > 0) ? limit : LIMIT;
        offset = (offset >= 0) ? offset : OFFSET;

        String selectQuery = "SELECT * FROM " + TABLE_CHAT +
                " LEFT OUTER JOIN " + TABLE_DATA + " USING(" + KEY_CHAT_NETWORK_ID + ") " +
                " WHERE " + KEY_CHAT_TITLE + " LIKE '%" + title + "%'" +
                " AND " + KEY_CHAT_DELETED + " = 0 " +
                " GROUP BY " + KEY_CHAT_NETWORK_ID + " ORDER BY " + KEY_TIMESTAMP + " DESC " +
                " LIMIT " + limit +
                " OFFSET " + offset + ";"; // TODO Sortierung?

        return doReadQuery(selectQuery, true);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Chat> getChatsByContactID(long contactID) {
        return getChatsByContactID(contactID, -1, -1);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Chat> getChatsByContactID(long contactID, int limit, int offset) {
        limit = (limit > 0) ? limit : LIMIT;
        offset = (offset >= 0) ? offset : OFFSET;

        String selectQuery = "SELECT * FROM " + TABLE_CHAT +
                " JOIN " + TABLE_CONTACT_CHAT +
                " ON " + TABLE_CONTACT_CHAT + "." + KEY_CHAT_NETWORK_ID + " = " + TABLE_CHAT + "." + KEY_CHAT_NETWORK_ID +
                " WHERE " + TABLE_CONTACT_CHAT + "." + KEY_CHAT_NETWORK_ID + " IN (" +
                    " SELECT a." + KEY_CHAT_NETWORK_ID +
                    " FROM " + TABLE_CONTACT_CHAT + " a, " + TABLE_CONTACT_CHAT + " b" +
                    " WHERE a." + KEY_CONTACT_ID + " = " + contactID +
                    " AND b." + KEY_CONTACT_ID + " = " + HelperFactory.getContacHelper(mContext).getSelf().getId() +
                    " AND a." + KEY_CHAT_NETWORK_ID + " = b." + KEY_CHAT_NETWORK_ID +
                    ") GROUP BY " + TABLE_CONTACT_CHAT + "." + KEY_CHAT_NETWORK_ID +
                    " HAVING COUNT(" + KEY_CONTACT_ID + ") = 2" +
                " AND " + KEY_CHAT_DELETED + " = 0 " +
                " LIMIT " + limit +
                " OFFSET " + offset + ";"; // TODO Sortierung?

        return doReadQuery(selectQuery, true);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isContactInChat(Contact contact, Chat chat) {
        String selectQuery = "SELECT 1 FROM " + TABLE_CONTACT_CHAT +
                " WHERE " + KEY_CHAT_NETWORK_ID + " LIKE '" + chat.getNetworkChatID() + "'" +
                " AND " + KEY_CONTACT_ID + " = " + contact.getId();

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
    public boolean isDeleted(Chat chat) {
        String selectQuery = "SELECT 1 FROM " + TABLE_CHAT +
                " WHERE " + KEY_CHAT_NETWORK_ID + " LIKE '" + chat.getNetworkChatID() + "'" +
                " AND " + KEY_CHAT_DELETED + " = 1;";

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
    public boolean isDeleted(String networkId) {
        String selectQuery = "SELECT 1 FROM " + TABLE_CHAT +
                " WHERE " + KEY_CHAT_NETWORK_ID + " LIKE '" + networkId + "'" +
                " AND " + KEY_CHAT_DELETED + " = 1;";

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
    public void removeReceiver(Chat chat, Contact contact) {
        if (chat != null && contact != null) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(TABLE_CONTACT_CHAT, KEY_CHAT_NETWORK_ID + " = '" + chat.getNetworkChatID() + "' AND " + KEY_CONTACT_ID + " = '" + contact.getId() + "'", null);
        }
    }

    private List<Chat> doReadQuery(String query, boolean withData) {
        List<Chat> chats = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            do {
                chats.add(cursorToChat(c, withData));
            } while (c.moveToNext());
        }

        c.close();
        return chats;
    }

    private Chat cursorToChat(Cursor c, boolean withData) {
        ContactHelper contactHelper = HelperFactory.getContacHelper(mContext);

        String title = c.getString(c.getColumnIndex(KEY_CHAT_TITLE));
        String chatID = c.getString(c.getColumnIndex(KEY_CHAT_NETWORK_ID));
        boolean isDeleted = (c.getInt(c.getColumnIndex(KEY_CHAT_DELETED))>0);

        List<Contact> receivers = contactHelper.getContactsByNetworkChatID(chatID);

        Chat chat = new Chat(chatID, title, receivers, isDeleted);

        if (withData) {
            DataHelper dataHelper = HelperFactory.getDataHelper(mContext);
            chat.addData(dataHelper.getDataObjectsByNetworkChatID(chatID));
        }

        return chat;
    }

    private void deleteChatData(Chat chat) {
        DataHelper dataHelper = HelperFactory.getDataHelper(mContext);
        dataHelper.deleteByNetworkChatID(chat.getNetworkChatID());
    }

    private void insertChatData(Chat chat) {
        DataHelper dataHelper = HelperFactory.getDataHelper(mContext);
        for (Data data : chat.getDataObjects()) {
            data.setNetworkChatID(chat.getNetworkChatID());
            dataHelper.unsafeInsert(data);
        }
    }

    private void insertChatContacts(Chat chat) {
        ContactHelper contactHelper = HelperFactory.getContacHelper(mContext);
        for (Contact contact : chat.getReceivers()) {
            contactHelper.insert(contact);
        }
        insertMtoNforChatAndContact(chat);
    }

    private void insertMtoNforChatAndContact(Chat chat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (Contact contact : chat.getReceivers()) {
            db.insert(TABLE_CONTACT_CHAT, null, chatAndContactToContentValue(chat, contact));
        }
        db.close();
    }

    private void updateMtoNforChatAndContact(Chat chat) {
        deleteMtoNforChatAndContact(chat);
        insertMtoNforChatAndContact(chat);
    }

    private void deleteMtoNforChatAndContact(Chat chat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_CONTACT_CHAT, KEY_CHAT_NETWORK_ID + " = '" + chat.getNetworkChatID() + "'", null);
        db.close();
    }

    private ContentValues chatToContentValue(Chat chat) {
        ContentValues values = new ContentValues();
        values.put(KEY_CHAT_TITLE, chat.getTitle());
        values.put(KEY_CHAT_NETWORK_ID, chat.getNetworkChatID());
        values.put(KEY_CHAT_DELETED, chat.isDeleted());
        return values;
    }

    private ContentValues chatAndContactToContentValue(Chat chat, Contact contact) {
        ContentValues values = new ContentValues();
        values.put(KEY_CHAT_NETWORK_ID, chat.getNetworkChatID());
        values.put(KEY_CONTACT_ID, contact.getId());
        return values;
    }
}
