package com.chaoyang805.cloudnote.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chaoyang805 on 2015/10/19.
 */
public class NoteDBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "notes.db";

    public static final String TABLE_NAME_USERS = "users";

    public static final String COLUMN_NAME_ID = "_id";

    public static final String COLUMN_NAME_NAME = "name";

    public static final String COLUMN_NAME_EMAIL = "email";

    public static final String COLUMN_NAME_PASSWORD = "password";

    public static final String TABLE_NAME_POSTS = "posts";

    public static final String COLUMN_NAME_MODIFY_DATE = "modify_date";

    public static final String COLUMN_NAME_POST_AUTHOR = "post_author";

    public static final String COLUMN_NAME_POST_TITLE = "post_title";

    public static final String COLUMN_NAME_POST_CONTENT = "post_content";

    public static final String COLUMN_NAME_PLAIN_CONTENT = "plain_content";

    public static final String COLUMN_NAME_POST_DATE = "post_date";

    public static final String COLUMN_NAME_IS_POST_SYNCED = "is_synced";

    public static final String TABLE_NAME_ATTACHMENT = "attachment";

    public static final String COLUMN_NAME_NOTE_ID = "note_id";

    public static final String COLUMN_NAME_TYPE = "type";

    public static final String COLUMN_NAME_ATTACHEMENT_PATH = "attachement_path";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME_USERS + " (" +
            COLUMN_NAME_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            COLUMN_NAME_NAME + " TEXT NOT NULL DEFAULT \"\"," +
            COLUMN_NAME_EMAIL + " TEXT NOT NULL DEFAULT \"\"," +
            COLUMN_NAME_PASSWORD + " TEXT NOT NULL DEFAULT \"\")";

    private static final String CREATE_TABLE_POSTS = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME_POSTS + " (" +
            COLUMN_NAME_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            COLUMN_NAME_NOTE_ID + " INTEGER NOT NULL," +
            COLUMN_NAME_POST_AUTHOR + " INTTEGER NOT NULL," +
            COLUMN_NAME_PLAIN_CONTENT + " TEXT NOT NULL DEFAULT \"\"," +
            COLUMN_NAME_POST_DATE + " TEXT NOT NULL DEFAULT \"1970-01-01 00:00:00\"," +
            COLUMN_NAME_MODIFY_DATE + " TEXT NOT NULL DEFAULT \"1970-01-01 00:00:00\"," +
            COLUMN_NAME_IS_POST_SYNCED + " BOOLEAN NOT NULL DEFAULT 0," +
            COLUMN_NAME_POST_TITLE + " TEXT NOT NULL DEFAULT \"\"," +
            COLUMN_NAME_POST_CONTENT + " TEXT)";

    private static final String CREATE_TABLE_ATTACHMENT = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME_ATTACHMENT + " (" +
            COLUMN_NAME_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            COLUMN_NAME_TYPE + " INTEGET NOT NULL DEFAULT 1," +
            COLUMN_NAME_IS_POST_SYNCED + " BOOLEAN NOT NULL DEFAULT 0," +
            COLUMN_NAME_POST_AUTHOR + " INTEGER NOT NULL," +
            COLUMN_NAME_NOTE_ID + " INTEGER NOT NULL," +
            COLUMN_NAME_ATTACHEMENT_PATH + " TEXT NOT NULL)";

    private static final String DROP_TABLE_USERS = "DROP TABLE IF EXISTS users";
    private static final String DROP_TABLE_POSTS = "DROP TABLE IF EXISTS posts";
    private static final String DROP_TABLE_ATTACHEMENT = "DROP TABLE IF EXISTS attachement";

    public NoteDBHelper(Context context) {
        super(context, DB_NAME, null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_POSTS);
        db.execSQL(CREATE_TABLE_ATTACHMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME_POSTS + " ADD COLUMN " + COLUMN_NAME_IS_POST_SYNCED + " BOOLEAN NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_NAME_ATTACHMENT + " ADD COLUMN " + COLUMN_NAME_IS_POST_SYNCED + " BOOLEAN NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_NAME_POSTS + " ADD COLUMN " + COLUMN_NAME_PLAIN_CONTENT);
        } else if (oldVersion == 2 && newVersion == 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME_POSTS + " ADD COLUMN " + COLUMN_NAME_MODIFY_DATE + " TEXT NOT NULL DEFAULT \"1970-01-01 00:00:00\"");
        } else {
            db.execSQL(DROP_TABLE_USERS);
            db.execSQL(DROP_TABLE_POSTS);
            db.execSQL(DROP_TABLE_ATTACHEMENT);

            db.execSQL(CREATE_TABLE_USERS);
            db.execSQL(CREATE_TABLE_POSTS);
            db.execSQL(CREATE_TABLE_ATTACHMENT);
        }
    }
}
