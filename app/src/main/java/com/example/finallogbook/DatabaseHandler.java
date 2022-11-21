package com.example.finallogbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Logbook";
    private static final String TABLE_IMAGE = "Images";

    private static final String IMAGE_ID = "id";
    private static final String IMAGE_URL = "url";

    private SQLiteDatabase database;

    private static final String TABLE_IMAGE_CREATE = String.format(
            "CREATE TABLE %s (" +
                    "   %s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "   %s TEXT)",
            TABLE_IMAGE, IMAGE_ID, IMAGE_URL);

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_IMAGE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGE);

        Log.v(this.getClass().getName(), TABLE_IMAGE + " database upgrade to version " +
                newVersion + " - old data lost");
        onCreate(db);
    }

    public long insertImage(String url) {
        ContentValues rowValues = new ContentValues();
        rowValues.put(IMAGE_URL , url);

        return database.insertOrThrow(TABLE_IMAGE, null, rowValues);
    }

    public ArrayList<Upload> getImages() {
        Cursor cursor = database.query(TABLE_IMAGE, new String[] {IMAGE_ID, IMAGE_URL},
                null, null, null, null, IMAGE_ID);

        ArrayList<Upload> results = new ArrayList<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(0);
            String url = cursor.getString(1);

            Upload trip = new Upload(url);
            trip.setId(id);
            results.add(trip);
            cursor.moveToNext();
        }
        return results;
    }
}
