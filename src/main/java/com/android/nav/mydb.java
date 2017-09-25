package com.android.nav;
import android.util.Log;

import android.content.Intent; 
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

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


public class mydb {

	public final SQLiteLocation sql;
	public SQLiteDatabase sqlDB;
	public Cursor sqlCursor;
	public SimpleCursorAdapter sqlCursorAdapter;
	private Context context;
	private LayoutInflater inflater;
	private View v;
	private ListView sqlListView;
	private String dbstr;

	public mydb(Context c, ListView lv, String user) {
		sqlListView = lv;
		context = c;
		dbstr = user;
		sql = new SQLiteLocation(context, user);
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

	public void insertTable(String lon, String lat, String input){
				if(sqlDB != null){
					ContentValues vals = new ContentValues();
					vals.put(DBContract.LocTable.COL_LON_STRING, lon);
					vals.put(DBContract.LocTable.COL_LAT_STRING, lat);
					vals.put(DBContract.LocTable.COL_INPUT_STRING, input);

					sqlDB.insert(dbstr,null,vals);
					populateTable();
				} else {
					Log.d("*****\n", "Unable to access database for writing.");
					//tv.append("unable to access database");
				}
	}

	public void update(long id, String s) {
        ContentValues values = new ContentValues();
		//values.put(DBContract.LocTable.COL_LON_STRING, "dummy");
		//values.put(DBContract.LocTable.COL_LAT_STRING, "dummy");
        values.put(DBContract.LocTable.COL_INPUT_STRING, s);
        // updating row
        sqlDB.update(dbstr, values, "_ID = ?",
                new String[] { String.valueOf(id) });
    }

    public String get(long id) {

		Cursor cursor = sqlDB.query(dbstr, 
				new String[]{
					//DBContract.LocTable._ID,
					DBContract.LocTable.COL_LON_STRING,
					DBContract.LocTable.COL_LAT_STRING,
					DBContract.LocTable.COL_INPUT_STRING},
					"_ID =?", new String[] { String.valueOf(id) }, 
					null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

        String addr = cursor.getString(2);
        return addr;
    }

    public void delete(long id) {
        sqlDB.delete(dbstr, "_ID = ?",
                new String[] { String.valueOf(id) });
    }


	public void populateTable(){
		if(sqlDB != null) {
			try {
				sqlCursor = sqlDB.query(dbstr,
						new String[]{
							DBContract.LocTable._ID,
							DBContract.LocTable.COL_LON_STRING,
							DBContract.LocTable.COL_LAT_STRING,
							DBContract.LocTable.COL_INPUT_STRING},
							//DBContract.DemoTable.COLUMN_NAME_DEMO_INT}, 
							null,null, null, null, null);

			sqlCursorAdapter = new SimpleCursorAdapter(context,
					R.layout.sql_item,
					sqlCursor,
					new String[]{DBContract.LocTable.COL_LON_STRING,
						DBContract.LocTable.COL_LAT_STRING,
						DBContract.LocTable.COL_INPUT_STRING},
						new int[]{R.id.sql_lon, R.id.sql_lat, R.id.sql_string},
						0);

			sqlListView.setAdapter(sqlCursorAdapter);
			sqlListView.setOnItemClickListener(new OnItemClickListener()
					{
						@Override
						public void onItemClick(AdapterView<?> adapter, View v, int position, long id) 
						{
							Toast.makeText(context,String.format("clicked %d", id), Toast.LENGTH_LONG).show();
							//delete(id);
							//populateTable();
							String addrstr = get(id);
							Intent launch = new Intent(context, editActivity.class);
							launch.putExtra("addr", addrstr);
							launch.putExtra("id", String.valueOf(id));
							((AppCompatActivity)context).startActivityForResult(launch, mainActivity.EDIT);
							//String value = (String)adapter.getItemAtPosition(position); 
							// assuming string and if you want to get the value on click of list item
							// do what you intend to do on click of listview row
						}
					});
		} catch (Exception e) {
			Log.d("*****\n", "Error loading data from database");
			//tv.append("Error loading data");
		}
	}
}

}
