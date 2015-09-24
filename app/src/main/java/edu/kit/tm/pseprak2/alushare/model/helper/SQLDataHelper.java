package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;

import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_CHAT_NETWORK_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_CONTACT_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_DATA_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_DATA_STATE;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_DATA_TEXT;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_SENDER_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_TIMESTAMP;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.LIMIT;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.OFFSET;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.TABLE_DATA;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.TABLE_DATA_STATE;

/**
 * Uses a SQLite database as the data source for inserting/deleting/updating data objects.
 */
public class SQLDataHelper extends DataHelper {
    private Context mContext;
    private SQLDatabaseHelper dbHelper;
    private DataStateHelper dataStateHelper;
    private ContactHelper contactHelper;
    private ASFileHelper fileHelper;
    private static final String SORT_STATEMENT = " ORDER BY " + KEY_TIMESTAMP + " ASC";

    /**
     * Creates an instance with a given context.
     * @param context context of the application.
     */
    public SQLDataHelper(Context context) {
        dbHelper = new SQLDatabaseHelper(context);
        this.mContext = context;

        dataStateHelper = HelperFactory.getDataStateHelper(context);
        fileHelper = HelperFactory.getFileHelper(context);
        contactHelper = HelperFactory.getContacHelper(context);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void unsafeInsert(Data data) {
        if (data != null) {
            if (data.getId() != -1) {
                unsafeUpdate(data);
            } else {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                data.setTimestamp(new Timestamp(Calendar.getInstance().getTime().getTime()));
                long id = db.insert(TABLE_DATA, null, dataToContentValue(data));

                if (id > -1) {
                    data.setId(id);

                    insertDataState(data);
                    insertDataFile(data);

                    finishedInsertingData(data);
                }
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void unsafeUpdate(Data data) {
        if (data != null) {
            if (data.getId() > -1) {
                if (exist(data)) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.update(TABLE_DATA, dataToContentValue(data), KEY_DATA_ID + " = " + data.getId(), null);

                    updateDataState(data);

                    finishedUpdatingData(data);
                }
            } else {
                unsafeInsert(data);
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void unsafeDelete(Data data) {
        if (data != null) {
            if (data.getId() > -1) {
                if (exist(data)) {
                    silentDelete(data);
                    finishedDeletingData(data);
                }
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean exist(Data data) {
        String selectQuery = "SELECT 1 FROM " + TABLE_DATA +
                " WHERE " + KEY_DATA_ID + " = " + data.getId();

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
    public void deleteByNetworkChatID(String networkChatID) {
        List<Data> dataList = this.getDataObjectsByNetworkChatID(networkChatID);
        for (Data data : dataList) {
            unsafeDelete(data);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Data> getDataObjects() {
        return getDataObjects(-1, -1);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Data> getDataObjects(int limit, int offset) {
        limit = (limit > 0) ? limit : LIMIT;
        offset = (offset >= 0) ? offset : OFFSET;

        String selectQuery = "SELECT * FROM " + TABLE_DATA +
                SORT_STATEMENT +
                " LIMIT " + limit + " OFFSET " + offset + ";";

        return doReadQuery(selectQuery);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Data> getDataObjectsByNetworkChatID(String networkChatID) {
        return getDataObjectsByNetworkChatID(networkChatID, -1, -1);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Data> getDataObjectsByNetworkChatID(String networkChatID, int limit, int offset) {
        limit = (limit > 0) ? limit : LIMIT;
        offset = (offset >= 0) ? offset : OFFSET;

        String selectQuery = "SELECT * FROM " + TABLE_DATA +
                " WHERE " + KEY_CHAT_NETWORK_ID + " = '" + networkChatID + "'" +
                SORT_STATEMENT +
                " LIMIT " + limit + " OFFSET " + offset + ";";

        return doReadQuery(selectQuery);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Data> getDataObjectsByDataState(DataState.Type stateType) {
        return getDataObjectsByDataState(stateType, -1, -1);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Data> getDataObjectsByDataState(DataState.Type stateType, int limit, int offset) {
        limit = (limit > 0) ? limit : LIMIT;
        offset = (offset >= 0) ? offset : OFFSET;

        String selectQuery = "SELECT * FROM " + TABLE_DATA +
                " JOIN " + TABLE_DATA_STATE +
                " ON " + TABLE_DATA + "." + KEY_DATA_ID + " = " + TABLE_DATA_STATE + "." + KEY_DATA_ID +
                " WHERE " + KEY_DATA_STATE + " = '" + stateType + "'" +
                " GROUP BY " + TABLE_DATA + "." + KEY_DATA_ID +
                SORT_STATEMENT +
                " LIMIT " + limit + " OFFSET " + offset + ";";

        return doReadQuery(selectQuery);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Data getDataByID(long id) {
        String selectQuery = "SELECT * FROM " + TABLE_DATA +
                " WHERE " + KEY_DATA_ID + "=" + id + ";";

        List<Data> datas = doReadQuery(selectQuery);
        return (datas.size() > 0) ? datas.get(0) : null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Data> getDataObjectsByDataStateAndContact(DataState.Type type, Contact contact) {
        String selectQuery = "SELECT * FROM " + TABLE_DATA +
                            " JOIN " + TABLE_DATA_STATE +
                            " ON " + TABLE_DATA + "." + KEY_DATA_ID + " = " + TABLE_DATA_STATE + "." + KEY_DATA_ID +
                            " WHERE " + KEY_DATA_STATE + " = '" + type + "' AND " + KEY_CONTACT_ID + " = " + contact.getId() +
                SORT_STATEMENT + ";";

        return doReadQuery(selectQuery);
    }

    private List<Data> doReadQuery(String query) {
        List<Data> datas = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            do {
                datas.add(cursorToData(c));
            } while (c.moveToNext());
        }

        c.close();
        return datas;
    }

    private void silentDelete(Data data) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_DATA, KEY_DATA_ID + " = " + data.getId(), null);
        deleteDataState(data);
        unlinkDataFile(data.getFile());
    }

    private void insertDataState(Data data) {
        for (Contact c: data.getReceivers()) {
            dataStateHelper.unsafeInsert(data.getState(c), data);
        }
    }

    private void updateDataState(Data data) {
        for (Contact c : data.getReceivers()) {
            dataStateHelper.unsafeUpdate(data.getState(c), data);
        }
    }

    private void deleteDataState(Data data) {
        for (Contact c : data.getReceivers()) {
            dataStateHelper.unsafeDelete(data.getState(c), data);
        }
    }

    private void insertDataFile(Data data) {
        ASFile asFile = data.getFile();
        if (asFile != null) {
            asFile.setDataId(data.getId());
            fileHelper.insert(asFile);
        }
    }

    private void unlinkDataFile(ASFile file) {
        if (file != null) {
            file.setDataId(-1);
            fileHelper.update(file);
        }
    }

    private Data cursorToData(Cursor c) {
        long id = c.getLong(c.getColumnIndex(KEY_DATA_ID));
        String chatId = c.getString(c.getColumnIndex(KEY_CHAT_NETWORK_ID));
        String text = c.getString(c.getColumnIndex(KEY_DATA_TEXT));
        long senderID = c.getLong(c.getColumnIndex(KEY_SENDER_ID));
        Timestamp timestamp = new Timestamp(c.getLong(c.getColumnIndex(KEY_TIMESTAMP)));

        Contact sender = contactHelper.getContactByID(senderID);
        ASFile file = fileHelper.getFileByDataID(id);
        HashMap<Long, DataState> receiverStateMap = dataStateHelper.getStateByDataID(id);
        List<Contact> receivers = contactHelper.getContactByData(id);

        Data data;
        if (file == null && !"".equals(text)) {
            data = new Data(id, chatId, sender, receivers, receiverStateMap, timestamp, text);
        } else if (file != null && "".equals(text)) {
            data = new Data(id, chatId, contactHelper.getContactByID(senderID), receivers, receiverStateMap, timestamp, file);
        } else if (file != null && !"".equals(text)) {
            data = new Data(id, chatId, contactHelper.getContactByID(senderID), receivers, receiverStateMap, timestamp, text, file);
        } else {
            throw new RuntimeException("Invalid data in database! (not text and no file)");
        }

        return data;
    }

    private ContentValues dataToContentValue(Data data) {
        ContentValues values = new ContentValues();
        values.put(KEY_DATA_TEXT, data.getText());
        values.put(KEY_CHAT_NETWORK_ID, data.getNetworkChatID());
        values.put(KEY_SENDER_ID, data.getSender().getId());
        values.put(KEY_TIMESTAMP, data.getTimestamp().getTime());
        return values;
    }
}
