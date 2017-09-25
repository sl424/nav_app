package com.android.nav;
import android.util.Log;

import android.widget.ListView;
import android.widget.TextView;
import android.view.View;

import android.view.LayoutInflater;
import android.support.v7.app.AppCompatActivity;

import android.database.Cursor;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import android.provider.BaseColumns;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;


public class userdb {

	public final SQLiteUser sql;
	public SQLiteDatabase sqlDB;
	private Context context;
	/*
	public Cursor sqlCursor;
	public SimpleCursorAdapter sqlCursorAdapter;
	private LayoutInflater inflater;
	private View v;
	private ListView sqlListView;
	*/

	public userdb(Context c) {
		//sqlListView = lv;
		context = c;
		sql = new SQLiteUser(context);
	}

	public void close()
	{
		sqlDB.close();
	}

	public void open() throws SQLiteException
	{
		try {
			sqlDB = sql.getWritableDatabase();
		} catch (SQLiteException ex) {
			sqlDB = sql.getReadableDatabase();
		}
	}

	public long AddUser(String username, String password)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(USERContract.userTable.USERNAME, username);
		initialValues.put(USERContract.userTable.PASSWORD, password);
		return sqlDB.insert(USERContract.userTable.TABLENAME, null, initialValues);

	}

	public boolean Login(String username, String password) throws SQLiteException
	{
		Cursor mCursor = sqlDB.rawQuery("SELECT * FROM " 
				+ USERContract.userTable.TABLENAME
				+ " WHERE "+USERContract.userTable.USERNAME+"=?"
				+ "AND " +USERContract.userTable.PASSWORD+"=?", 
				new String[]{username,password});
		if (mCursor != null) {
			if(mCursor.getCount() > 0)
			{
				return true;
			}
		}
		return false;
	}
}
