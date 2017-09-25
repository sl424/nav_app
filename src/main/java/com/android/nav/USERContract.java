package com.android.nav;
import android.os.Bundle;

import android.provider.BaseColumns;

public class USERContract {
	private USERContract(){};

	public final class userTable implements BaseColumns {
		//public static final String DB_NAME = "location_db";
		public static final String TABLENAME = "user_table";
		public static final String USERNAME = "user_name";
		public static final String PASSWORD = "password_str";
		public static final int DB_VERSION = 1;

		public static final String USER_CREATE_TABLE = "CREATE TABLE " +
			userTable.TABLENAME + "(" + 
			userTable._ID + " INTEGER PRIMARY KEY NOT NULL," +
			userTable.USERNAME + " VARCHAR(255)," +
			userTable.PASSWORD + " VARCHAR(255)" +
			//LocTable.COL_LAT_STRING + " VARCHAR(255)," +
			//LocTable.COL_INPUT_STRING + " VARCHAR(255)" + 
			");";
		//DemoTable.COLUMN_NAME_DEMO_INT + " INTEGER);";

		public static final String USER_DROP_TABLE = 
			"DROP TABLE IF EXISTS " + userTable.TABLENAME;
	}
}
