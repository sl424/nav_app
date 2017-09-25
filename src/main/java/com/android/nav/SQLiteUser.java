package com.android.nav;

import android.os.Bundle;
import android.widget.TextView;
import android.view.View;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

public class SQLiteUser extends SQLiteOpenHelper {

    public SQLiteUser(Context context) {
        super(context, USERContract.userTable.TABLENAME, null, USERContract.userTable.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( USERContract.userTable.USER_CREATE_TABLE);
		 ContentValues testValues = new ContentValues();
        testValues.put(USERContract.userTable.USERNAME, "admin");
        testValues.put(USERContract.userTable.PASSWORD, "admin");
        db.insert(USERContract.userTable.TABLENAME,null,testValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL( USERContract.userTable.USER_DROP_TABLE);
        onCreate(db);
    }
}

