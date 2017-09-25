package com.android.nav;
import android.os.Bundle;

import android.provider.BaseColumns;

public class DBContract {
	private DBContract(){};

	public final class LocTable implements BaseColumns {
		public static final String DB_NAME = "location_db";
//		public static final String TABLE_NAME = "location";
		public static final String COL_INPUT_STRING = "input";
		public static final String COL_LON_STRING = "lon";
		public static final String COL_LAT_STRING = "lat";
		public static final int DB_VERSION = 1;

		/*
		public static final String SQL_CREATE_TABLE = "CREATE TABLE " +
			LocTable.TABLE_NAME + "(" + 
			LocTable._ID + " INTEGER PRIMARY KEY NOT NULL," +
			LocTable.COL_LON_STRING + " VARCHAR(255)," +
			LocTable.COL_LAT_STRING + " VARCHAR(255)," +
			LocTable.COL_INPUT_STRING + " VARCHAR(255)" + ");";
		DemoTable.COLUMN_NAME_DEMO_INT + " INTEGER);";

		public static final String SQL_DROP_TABLE = 
			"DROP TABLE IF EXISTS " + LocTable.TABLE_NAME;
			*/
	}
	/*
	public String tablestr_drop(String user){
		String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + user;
		return SQL_DROP_TABLE;
	}

	public String tablestr(String user){
		String SQL_CREATE_TABLE = "CREATE TABLE " +
			user + "(" + 
			LocTable._ID + " INTEGER PRIMARY KEY NOT NULL," +
			LocTable.COL_LON_STRING + " VARCHAR(255)," +
			LocTable.COL_LAT_STRING + " VARCHAR(255)," +
			LocTable.COL_INPUT_STRING + " VARCHAR(255)" + ");";
		//DemoTable.COLUMN_NAME_DEMO_INT + " INTEGER);";
		return SQL_CREATE_TABLE;
	}
	*/
}
