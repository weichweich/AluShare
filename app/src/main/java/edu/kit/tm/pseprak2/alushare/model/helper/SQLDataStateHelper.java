package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;

import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;

import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_CONTACT_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_DATA_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_DATA_PROGRESS;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_DATA_STATE;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.TABLE_DATA_STATE;

/**
 * Created by dominik on 23.08.15.
 */
public class SQLDataStateHelper extends DataStateHelper{
    private Context mContext;
    private SQLDatabaseHelper dbHelper;

    public SQLDataStateHelper(Context context) {
        dbHelper = new SQLDatabaseHelper(context);
        this.mContext = context;
    }

    @Override
    public HashMap<Long, DataState> getStateByDataID(long dataID) {
        String selectQuery = "SELECT * FROM " + TABLE_DATA_STATE +
                " WHERE " + KEY_DATA_ID + "=" + dataID + ";";

        return doReadDataStateQuery(selectQuery);
    }

    @Override
    public void unsafeInsert(DataState dataState, Data data) {
        if (dataState != null && data != null) {
            if (data.getId() != -1 && dataState.getReceiver().getId() != -1) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.insert(TABLE_DATA_STATE, null, dataStateToContentValue(dataState, data));
            }
        }
    }

    @Override
    public void unsafeUpdate(DataState dataState, Data data) {
        if (dataState != null && data != null) {
            if (data.getId() != -1 && dataState.getReceiver().getId() != -1) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.update(TABLE_DATA_STATE, dataStateToContentValue(dataState, data), KEY_DATA_ID + " = " + data.getId() + " AND " + KEY_CONTACT_ID + " = " + dataState.getReceiver().getId(), null);
            }
        }
    }

    @Override
    public void unsafeDelete(DataState dataState, Data data) {
        if (dataState != null && data != null) {
            if (data.getId() != -1 && dataState.getReceiver().getId() != -1) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete(TABLE_DATA_STATE, KEY_DATA_ID + " = " + data.getId() + " AND " + KEY_CONTACT_ID + " = " + dataState.getReceiver().getId(), null);
            }
        }
    }

    private HashMap<Long, DataState> doReadDataStateQuery(String query) {
        HashMap<Long, DataState> receiverStateMap = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            do {
                DataState curState = curserToDataState(c);
                receiverStateMap.put(curState.getReceiver().getId(), curState);
            } while (c.moveToNext());
        }

        c.close();
        return receiverStateMap;
    }

    private DataState curserToDataState(Cursor c) {
        int progress = c.getInt(c.getColumnIndex(KEY_DATA_PROGRESS));
        DataState.Type type = sStateToDataStateType(c.getString(c.getColumnIndex(KEY_DATA_STATE)));
        long receiverID = c.getLong(c.getColumnIndex(KEY_CONTACT_ID));

        Contact receiver = HelperFactory.getContacHelper(mContext).getContactByID(receiverID);
        return new DataState(receiver, type, progress);
    }

    private ContentValues dataStateToContentValue(DataState dataState, Data data) {
        ContentValues values = new ContentValues();
        values.put(KEY_DATA_PROGRESS, dataState.getProgress());
        values.put(KEY_DATA_STATE, dataState.getDataStateType().name());
        values.put(KEY_CONTACT_ID, dataState.getReceiver().getId());
        values.put(KEY_DATA_ID, data.getId());
        return values;
    }

    private DataState.Type sStateToDataStateType(String sState) {
        DataState.Type rStateType = null;
        for (DataState.Type state : DataState.Type.values()) {
            if (sState.equalsIgnoreCase(state.name())) {
                rStateType = state;
                break;
            }
        }
        return rStateType;
    }
}
