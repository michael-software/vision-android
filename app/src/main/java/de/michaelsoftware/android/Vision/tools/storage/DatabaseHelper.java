package de.michaelsoftware.android.Vision.tools.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Michael on 20.12.2015.
 * Manages Database requests for offline data/cache
 */
public class DatabaseHelper {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {"ID", "src", "data"};

    public static int OFFLINE_DATA = 1;
    public static int PARA_CACHE_DATA = 2;

    public DatabaseHelper(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public HashMap<String, Object> createEntry(int pTable,String src, String pData) {
        String data = this.getData(pTable, src);
        if(data == null || data.equals("")) {
            String table;

            if (pTable == PARA_CACHE_DATA) {
                table = "para_cache_data";
            } else {
                table = "offline_data";
            }

            ContentValues values = new ContentValues();
            values.put("src", src);
            values.put("data", pData);

            long insertId = database.insert(table, null, values);

            Cursor cursor = database.query(table, allColumns, "ID = " + insertId, null, null, null, null);
            cursor.moveToFirst();

            return cursorToHashMap(cursor);
        } else {
            String table;

            if (pTable == PARA_CACHE_DATA) {
                table = "para_cache_data";
            } else {
                table = "offline_data";
            }

            ContentValues values = new ContentValues();
            values.put("data", pData);

            long updateId = database.update(table, values, "src=?", new String[]{src});

            Cursor cursor = database.query(table, allColumns, "ID = " + updateId, null, null, null, null);
            cursor.moveToFirst();

            return cursorToHashMap(cursor);
        }
    }

    private HashMap<String, Object> cursorToHashMap(Cursor cursor) {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("src", cursor.getString(1));
        hm.put("data", cursor.getString(2));

        return hm;
    }

    @SuppressWarnings("unused") /* Will be used in the future ( maybe :-) ) */
    public List<String> getAllDatas(int pTable) {
        String table;

        if(pTable == PARA_CACHE_DATA) {
            table = "para_cache_data";
        } else {
            table = "offline_data";
        }

        List<String> dataList = new ArrayList<>();

        Cursor cursor = database.query(table, allColumns, null, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() == 0) return dataList;

        while (!cursor.isAfterLast()) {
            dataList.add(cursor.getString(2));
            cursor.moveToNext();
        }

        cursor.close();

        return dataList;
    }

    public String getData(int pTable, String url) {
        String table;

        if(pTable == PARA_CACHE_DATA) {
            table = "para_cache_data";
        } else {
            table = "offline_data";
        }

        String data = "";
        String [] whereArgs = {url};

        Cursor cursor = database.query(table, allColumns, "src = ?", whereArgs, null, null, null, "1");
        cursor.moveToFirst();

        if(cursor.getCount() == 0) return data;

        while (!cursor.isAfterLast()) {
            data = cursor.getString(2);
            cursor.moveToNext();
        }

        cursor.close();

        if(data != null) return data;
        else return "";
    }

    public void clear() {
        if (!this.database.isOpen()) {
            try {
                this.open();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        dbHelper.clear(database);
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }
}
