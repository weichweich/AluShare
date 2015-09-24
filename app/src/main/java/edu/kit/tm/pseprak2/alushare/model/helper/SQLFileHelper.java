package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.ASFile;

import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_DATA_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_FILE_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_FILE_NAME;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_FILE_PATH;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_FILE_RECEIVED;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.LIMIT;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.OFFSET;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.TABLE_FILE;

/**
 * Uses a SQLite database as the data source for inserting/deleting/updating ASFiles.
 */
public class SQLFileHelper extends ASFileHelper {
    private Context mContext;
    private SQLDatabaseHelper dbHelper;

    /**
     * Creates an instance with a given context.
     * @param context context of the application.
     */
    public SQLFileHelper(Context context) {
        dbHelper = new SQLDatabaseHelper(context);
        this.mContext = context;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert(ASFile file) {
        if (file != null) {
            if (file.getId() != -1) {
                update(file);
            } else {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long id = db.insert(TABLE_FILE, null, asFileToContentValue(file));
                if (id > -1) {
                    file.setId(id);

                    notifyInserted(file);
                }
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void update(ASFile file) {
        if (file != null) {
            if (file.getId() > -1) {
                if (exist(file)) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.update(TABLE_FILE, asFileToContentValue(file), KEY_FILE_ID + " = " + file.getId(), null);

                    notifyUpdated(file);
                }
            } else {
                insert(file);
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete(ASFile file) {
        if (file != null) {
            if (file.getId() > -1) {
                if (exist(file)) {
                    file.delete();
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete(TABLE_FILE, KEY_FILE_ID + " = " + file.getId(), null);
                    notifyRemoved(file);
                }
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean exist(ASFile file) {
        String selectQuery = "SELECT 1 FROM " + TABLE_FILE +
                " WHERE " + KEY_FILE_ID + " = " + file.getId();

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
    public List<ASFile> getFiles() {
        return getFiles(-1, -1);
    }

    public List<ASFile> getFiles(int limit, int offset) {
        limit = (limit > 0) ? limit : LIMIT;
        offset = (offset >= 0) ? offset : OFFSET;

        String selectQuery = "SELECT * FROM " + TABLE_FILE +
                            " ORDER BY " + KEY_FILE_ID + " DESC " +
                            " LIMIT " + limit +
                            " OFFSET " + offset + ";"; // TODO Sortierung?

        return doReadQuery(selectQuery);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ASFile> getFilesByName(String name) {
        return getFilesByName(-1, -1, name);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ASFile> getFilesByName(int limit, int offset, String name) {
        String selectQuery = "SELECT * FROM " + TABLE_FILE +
                            " WHERE " + KEY_FILE_NAME + " LIKE '%" + name + "%'" +
                            " ORDER BY " + KEY_FILE_ID + " DESC " +
                            " LIMIT " + limit +
                            " OFFSET " + offset + ";";

        return doReadQuery(selectQuery);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ASFile> getReceivedFiles() {
        return getReceivedFiles(-1, -1);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ASFile> getReceivedFiles(int limit, int offset) {
        String selectQuery = "SELECT * FROM " + TABLE_FILE +
                " WHERE " + KEY_FILE_RECEIVED + " = 1" +
                " ORDER BY " + KEY_FILE_ID + " DESC " +
                " LIMIT " + limit +
                " OFFSET " + offset + ";";

        return doReadQuery(selectQuery);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ASFile> getReceivedFilesByName(String name) {
        return getReceivedFilesByName(-1, -1, name);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ASFile> getReceivedFilesByName(int limit, int offset, String name) {
        String selectQuery = "SELECT * FROM " + TABLE_FILE +
                            " WHERE " + KEY_FILE_NAME + " LIKE '%" + name + "%' AND " + KEY_FILE_RECEIVED + " = 1" +
                            " ORDER BY " + KEY_FILE_ID + " DESC " +
                            " LIMIT " + limit +
                            " OFFSET " + offset + ";";

        return doReadQuery(selectQuery);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ASFile> getSendFiles() {
        return getSendFiles(-1, -1);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ASFile> getSendFiles(int limit, int offset) {
        String selectQuery = "SELECT * FROM " + TABLE_FILE +
                            " WHERE " + KEY_FILE_RECEIVED + " = 0" +
                            " ORDER BY " + KEY_FILE_ID + " DESC " +
                            " LIMIT " + limit +
                            " OFFSET " + offset + ";";

        return doReadQuery(selectQuery);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ASFile> getSendFilesByName(String name) {
        return getSendFilesByName(-1, -1, name);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ASFile> getSendFilesByName(int limit, int offset, String name) {
        String selectQuery = "SELECT * FROM " + TABLE_FILE +
                " WHERE " + KEY_FILE_NAME + " LIKE '%" + name + "%' AND " + KEY_FILE_RECEIVED + " = 0" +
                " ORDER BY " + KEY_FILE_ID + " DESC " +
                " LIMIT " + limit +
                " OFFSET " + offset + ";";

        return doReadQuery(selectQuery);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ASFile getFileByDataID(long dataID) {
        String selectQuery = "SELECT * FROM " + TABLE_FILE +
                " WHERE " + KEY_DATA_ID + " = " + dataID + ";";

        List<ASFile> files = doReadQuery(selectQuery);
        return (files.size() > 0) ? files.get(0) : null;
    }

    public ASFile getFileByID(long fileID) {
        String selectQuery = "SELECT * FROM " + TABLE_FILE +
                " WHERE " + KEY_FILE_ID + " = " + fileID;

        List<ASFile> files = doReadQuery(selectQuery);
        return (files.size() > 0) ? files.get(0) : null;
    }

    private List<ASFile> doReadQuery(String query) {
        List<ASFile> files = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            do {
                files.add(cursorToASFile(c));
            } while (c.moveToNext());
        }

        c.close();
        return files;
    }

    private ASFile cursorToASFile(Cursor c) {
        long id = c.getLong(c.getColumnIndex(KEY_FILE_ID));
        long dataId = c.getLong(c.getColumnIndex(KEY_DATA_ID));
        String path = c.getString(c.getColumnIndex(KEY_FILE_PATH));
        String name = c.getString(c.getColumnIndex(KEY_FILE_NAME));
        Boolean received = c.getInt(c.getColumnIndex(KEY_FILE_RECEIVED))>0;

        return new ASFile(id, dataId, path, name, received);
    }

    private ContentValues asFileToContentValue(ASFile file) {
        ContentValues values = new ContentValues();
        values.put(KEY_FILE_PATH, file.getPath());
        values.put(KEY_FILE_NAME, file.getASName());
        values.put(KEY_DATA_ID, file.getDataId());
        values.put(KEY_FILE_RECEIVED, file.getReceived());
        return values;
    }
}
