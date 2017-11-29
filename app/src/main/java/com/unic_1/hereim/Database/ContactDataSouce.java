package com.unic_1.hereim.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.unic_1.hereim.Model.ContactModel;

import java.util.ArrayList;

/**
 * Created by unic-1 on 26/11/17.
 */

public class ContactDataSouce {

    private SQLiteDatabase mDatabase;
    private DatabaseHelper mDBHelper;

    public void getDBHelper(Context context) {
        mDBHelper = new DatabaseHelper(context);
    }

    public void openDatabase() {
        mDatabase = mDBHelper.getWritableDatabase();
    }

    public void closeDatabase() {
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    public long insertData(String number, String name) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NUMBER, number);
        values.put(DatabaseHelper.COLUMN_NAME, name);
        return mDatabase.insert(DatabaseHelper.TABLE
                , null
                , values
        );
    }

    public void deleteData(String number) {
        mDatabase.delete(DatabaseHelper.TABLE, DatabaseHelper.COLUMN_NUMBER +"="+number, null);
    }

    public ArrayList<ContactModel> getData() {
        System.out.println("Getting data...");
        ArrayList<ContactModel> contact = new ArrayList<>();
        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE, new String[]{DatabaseHelper.COLUMN_NUMBER, DatabaseHelper.COLUMN_NAME}, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            String number = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NUMBER));
            System.out.println(name+": "+number);
            contact.add(new ContactModel(name,number));
            cursor.moveToNext();
        }

        cursor.close();
        return contact;
    }
}
