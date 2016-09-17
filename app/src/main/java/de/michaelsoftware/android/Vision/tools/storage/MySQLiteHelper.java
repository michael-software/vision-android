package de.michaelsoftware.android.Vision.tools.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.michaelsoftware.android.Vision.tools.Logs;

/**
 * Created by Michael on 20.12.2015.
 * Class for managing MySQL-Databases uses in this project
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "vision.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_OFFLINE_DATA = "CREATE TABLE offline_data (ID integer primary key autoincrement, src TEXT, data TEXT)";
    private static final String CREATE_TABLE_PARA_CACHE_DATA = "CREATE TABLE para_cache_data (ID integer primary key autoincrement, src TEXT, data TEXT)";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_OFFLINE_DATA);
        db.execSQL(CREATE_TABLE_PARA_CACHE_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logs.w(this, "Upgrading database from version" + oldVersion + "to" + newVersion + ", which will destroy all old data.");
        db.execSQL("DROP TABLE IF EXISTS offline_data");
        db.execSQL("DROP TABLE IF EXISTS para_cache_data");
        onCreate(db);
    }

    public void clear(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS offline_data");
        db.execSQL("DROP TABLE IF EXISTS para_cache_data");
        onCreate(db);
    }
}
