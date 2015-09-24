package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "AluShareDataBase.db";

    private static final String ID_SUFFIX = "_id";

    public static final int LIMIT = Integer.MAX_VALUE;
    public static final int OFFSET = 0;

    //Tables
    public static final String TABLE_CONTACT = "contact";
    public static final String TABLE_DATA = "data";
    public static final String TABLE_DATA_STATE = "datastate";
    public static final String TABLE_FILE = "file";
    public static final String TABLE_CHAT = "chat";
    public static final String TABLE_CONTACT_CHAT = TABLE_CONTACT + "_" + TABLE_CHAT;

    //
    public static final String KEY_ID = "id";

    //Contact - Column names
    public static final String KEY_CONTACT_ID = TABLE_CONTACT + ID_SUFFIX;
    public static final String KEY_NETWORKING_ID = "networking" + ID_SUFFIX;
    public static final String KEY_RAW_CONTACT_ID = "raw_contact" + ID_SUFFIX;

    //Data - Column names
    public static final String KEY_DATA_ID = TABLE_DATA + ID_SUFFIX;
    public static final String KEY_DATA_TEXT = "text";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_SENDER_ID = "sender" + ID_SUFFIX;

    //Datastate -Column names
    public static final String KEY_DATA_STATE = "state";
    public static final String KEY_DATA_PROGRESS = "progress";

    //File - Column names#
    public static final String KEY_FILE_ID = TABLE_FILE + ID_SUFFIX;
    public static final String KEY_FILE_NAME = "name";
    public static final String KEY_FILE_PATH = "path";
    public static final String KEY_FILE_RECEIVED = "received";

    //Chat - Column names
    public static final String KEY_CHAT_TITLE = "chat_title";
    public static final String KEY_CHAT_NETWORK_ID = "chat_network" + ID_SUFFIX;
    public static final String KEY_CHAT_DELETED = "deleted";

    //Table Create Statements
    //Contact create table statement
    private static final String CREATE_TABLE_CONTACT = "CREATE TABLE " +
            TABLE_CONTACT + " (" +
            KEY_CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KEY_NETWORKING_ID + " TEXT NOT NULL, " +
            KEY_RAW_CONTACT_ID + " TEXT" + ")";

    //Data create table statement
    private static final String CREATE_TABLE_DATA = "CREATE TABLE " +
            TABLE_DATA + " (" +
            KEY_DATA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KEY_CHAT_NETWORK_ID + " TEXT NOT NULL, " +
            KEY_DATA_TEXT + " TEXT NOT NULL, " +
            KEY_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            KEY_SENDER_ID + " INTEGER NOT NULL, " +
            "FOREIGN KEY("+KEY_SENDER_ID+") REFERENCES "+ TABLE_CONTACT +"("+ KEY_CONTACT_ID +"), " +
            "FOREIGN KEY("+KEY_CHAT_NETWORK_ID+") REFERENCES "+ TABLE_CHAT +"("+ KEY_CHAT_NETWORK_ID +"))";

    //Data state create table statement
    private static final String CREATE_TABLE_DATA_STATE = "CREATE TABLE " +
            TABLE_DATA_STATE + " (" +
            KEY_DATA_STATE + " TEXT NOT NULL, " +
            KEY_DATA_PROGRESS + " INTEGER, " +
            KEY_DATA_ID + " INTEGER, " +
            KEY_CONTACT_ID + " INTEGER, " +
            "PRIMARY KEY (" + KEY_DATA_ID + ", " + KEY_CONTACT_ID + "), " +
            "FOREIGN KEY(" + KEY_DATA_ID + ")" + "REFERENCES " + TABLE_DATA + "(" + KEY_DATA_ID + "), " +
            "FOREIGN KEY(" + KEY_CONTACT_ID + ")" + "REFERENCES " + TABLE_CONTACT + "(" + KEY_CONTACT_ID + "))";


    //Contact_Chat create table statement
    private static final String CREATE_TABLE_CONTACT_CHAT = "CREATE TABLE " +
            TABLE_CONTACT_CHAT + " (" +
            KEY_CONTACT_ID + " INTEGER NOT NULL, " +
            KEY_CHAT_NETWORK_ID + " TEXT NOT NULL, " +
            "PRIMARY KEY (" + KEY_CHAT_NETWORK_ID  + ", " + KEY_CONTACT_ID + "), " +
            "FOREIGN KEY("+KEY_CONTACT_ID+") REFERENCES "+ TABLE_CONTACT +"("+ KEY_CONTACT_ID +"), " +
            "FOREIGN KEY("+KEY_CHAT_NETWORK_ID+") REFERENCES "+ TABLE_CHAT +"("+ KEY_CHAT_NETWORK_ID +"))";


    //File create table statement
    private static final String CREATE_TABLE_FILE = "CREATE TABLE " +
            TABLE_FILE + " (" +
            KEY_FILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KEY_DATA_ID + " INTEGER, " +
            KEY_FILE_NAME + " TEXT NOT NULL, " +
            KEY_FILE_PATH + " TEXT NOT NULL, " +
            KEY_FILE_RECEIVED + " BOOLEAN NOT NULL, " +
            "FOREIGN KEY("+KEY_DATA_ID+") REFERENCES "+ TABLE_DATA +"("+ KEY_DATA_ID +"))";

    //Chat create table statement
    private static final String CREATE_TABLE_CHAT = "CREATE TABLE " +
            TABLE_CHAT + " (" +
            KEY_CHAT_TITLE + " TEXT NOT NULL, " +
            KEY_CHAT_DELETED + " BOOLEAN NOT NULL, " +
            KEY_CHAT_NETWORK_ID + " TEXT PRIMARY KEY" + ")";

    public SQLDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CONTACT);
        db.execSQL(CREATE_TABLE_DATA);
        db.execSQL(CREATE_TABLE_CONTACT_CHAT);
        db.execSQL(CREATE_TABLE_FILE);
        db.execSQL(CREATE_TABLE_CHAT);
        db.execSQL(CREATE_TABLE_DATA_STATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT_CHAT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA_STATE);

        onCreate(db);
    }
}
