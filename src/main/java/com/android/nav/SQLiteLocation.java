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

public class SQLiteLocation extends SQLiteOpenHelper {
	private String dbstr;

    public SQLiteLocation(Context context, String dbname) {
        super(context, dbname, null, DBContract.LocTable.DB_VERSION);
		dbstr = dbname;
    }

    @Override
	public void onCreate(SQLiteDatabase db) {

		String SQL_CREATE_TABLE = "CREATE TABLE " +
			dbstr + "(" + 
			DBContract.LocTable._ID + " INTEGER PRIMARY KEY NOT NULL," +
			DBContract.LocTable.COL_LON_STRING + " VARCHAR(255)," +
			DBContract.LocTable.COL_LAT_STRING + " VARCHAR(255)," +
			DBContract.LocTable.COL_INPUT_STRING + " VARCHAR(255)" + ");";

		db.execSQL(SQL_CREATE_TABLE);
		ContentValues testValues = new ContentValues();
		testValues.put(DBContract.LocTable.COL_LON_STRING, "Longitude");
		testValues.put(DBContract.LocTable.COL_LAT_STRING, "Latitude");
		testValues.put(DBContract.LocTable.COL_INPUT_STRING, "Address");
		db.insert(dbstr,null,testValues);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + dbstr;
		db.execSQL(SQL_DROP_TABLE);
		onCreate(db);
	}
}

