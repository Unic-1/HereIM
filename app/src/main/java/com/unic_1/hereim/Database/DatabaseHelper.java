package com.unic_1.hereim.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by unic-1 on 26/11/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public final static String DATABASE_NAME = "contacts.db";
    public final static String TABLE = "contact_table";
    public final static String COLUMN_NAME = "name";
    public final static String COLUMN_NUMBER = "number";
    public final static int VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE
                + "(" + COLUMN_NUMBER + " text(10),"
                + COLUMN_NAME + " text(20),"
                + "Primary key (" + COLUMN_NUMBER + ")"
                + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
}
