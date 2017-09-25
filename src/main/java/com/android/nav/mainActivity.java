package com.android.nav;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Toast;

import android.widget.Button;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.os.AsyncTask;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import android.support.annotation.NonNull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.FormBody;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
//import com.google.firebase.auth.GoogleAuthProvider;

import java.io.UnsupportedEncodingException;

public class mainActivity extends AppCompatActivity implements View.OnClickListener
{
	private String user;
	private TextView tv;
	private TextView result;

	private mydb dba;
	public static final int EXTRACT = 9000;
	public static final int EDIT = 1000;

	private String auth_id = "8f55d8c6-08f9-2593-06de-4ea538edb221";
	private String auth_token = "6F0SZXJcEbNn0L7Dajtp";
	private String post_uri = "https://us-extract.api.smartystreets.com";

	public static final MediaType CONTENT_TYPE = MediaType.parse("text/plain; charset=utf-8");
	private String LOG_TAG = "******\nmainActivity******"; 
	private String API_URL = "https://backend-dot-firebase-nav-app.appspot.com/notes";

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;
	private FirebaseUser fbuser;
	private String fbtoken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		tv = (TextView) findViewById(R.id.hello);
		result = (TextView) findViewById(R.id.result);

		Intent i = getIntent();
		user = i.getStringExtra(loginActivity.USER);
		tv.setText("hello " + user);

		findViewById(R.id.input).setOnClickListener(this);
		findViewById(R.id.button_reset).setOnClickListener(this);
		findViewById(R.id.camera).setOnClickListener(this);
		findViewById(R.id.scrub).setOnClickListener(this);
		findViewById(R.id.drive).setOnClickListener(this);
		findViewById(R.id.insert).setOnClickListener(this);
		findViewById(R.id.logoff).setOnClickListener(this);

		dba = new mydb(this, (ListView)findViewById(R.id.sql_list_view), user);
		dba.open();
		dba.populateTable();

		/* firebase */
		mAuth = FirebaseAuth.getInstance();
		mAuthListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				fbuser = firebaseAuth.getCurrentUser();
				if (fbuser != null) {
					Log.d(LOG_TAG, "onAuthStateChanged:signed_in:" + fbuser.getUid());
				} else {
					Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
				}
				updateUI(fbuser);
			}
		};

	}

	private void updateUI(FirebaseUser fbuser) {
		//hideProgressDialog();
		if (fbuser != null) {
			tv.setText(getString(R.string.emailpassword_status_fmt,
						fbuser.getEmail(), fbuser.isEmailVerified()));
			//mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
			fbuser.getToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
				@Override
				public void onSuccess(GetTokenResult getTokenResult) {
					fbtoken = getTokenResult.getToken();
					makeEndpointsRequest(getTokenResult.getToken());
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					result.setText("Failed to get token from Firebase.");
				}
			});
		} else {
			result.setText("failed to get current user");
		}
	}

	private void postEndpointsRequest(String accessToken) {
		/*
		final String addObj = String.format("{\"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\"}", 
				"message", " ", 
				"latitude", " ", 
				"longitude", " ", 
				"address", (result.getText().toString()).replaceAll("(\\r|\\n)", " "));
				*/

		String addr_data = (result.getText().toString()).replaceAll("(\\r|\\n)", " ");
		String tmp = String.format(
				"{'message':'', 'latitude':'', 'longitude':'', 'address':'%s'}", addr_data);
		final String addObj = tmp.replaceAll("'", "\\\"");
		result.setText(addObj);
		//result.setText("");

		new AsyncTask<String, Void, JSONObject>() {
			@Override
			protected JSONObject doInBackground(String... tokens) {
				OkHttpClient client = new OkHttpClient();
				//String json_data = js;
				//tv.append(json_data);
				RequestBody body = RequestBody.create(JSON, addObj);
				HttpUrl reqUrl = HttpUrl.parse(API_URL);

				reqUrl = reqUrl.newBuilder().build();

				Request request = new Request.Builder()
					.url(reqUrl) //
					.addHeader("Content-Type","application/json")
					.addHeader("Authorization", String.format("Bearer %s", tokens[0]))
					.post(body)
					.build();

				try {
					Response response = client.newCall(request).execute();
					//String jsonBody = response.body().string();
					String jsonBody = response.toString();
					Log.i(LOG_TAG, String.format("User Info Response %s", jsonBody));
					//return new JSONObject(jsonBody);
					return new JSONObject("{}");
				} catch (Exception exception) {
					Log.w(LOG_TAG, exception);
				}
				return null;
			}

			@Override
			protected void onPostExecute(JSONObject userInfo){
				Log.w(LOG_TAG, "ok");
			}
		}.execute(accessToken);
	}

	private void makeEndpointsRequest(String accessToken) {
		new AsyncTask<String, Void, JSONObject>() {
			@Override
			protected JSONObject doInBackground(String... tokens) {
				OkHttpClient client = new OkHttpClient();
				//RequestBody body = RequestBody.create(CONTENT_TYPE, str);
				HttpUrl reqUrl = HttpUrl.parse(API_URL);
				reqUrl = reqUrl.newBuilder().build();

				Request request = new Request.Builder()
					.url(reqUrl) //
					.addHeader("Accept","application/json")
					.addHeader("Content-Type","application/json")
					.addHeader("Authorization", String.format("Bearer %s", tokens[0]))
					//.addHeader("Authorization", String.format("Bearer %s", tokens[0]))
					//.post(body)
					.build();

				try {
					Response response = client.newCall(request).execute();
					String jsonBody = response.body().string();
					Log.i(LOG_TAG, String.format("User Info Response %s", jsonBody));
					return new JSONObject("{ 'items':"+jsonBody+"}");
				} catch (Exception exception) {
					Log.w(LOG_TAG, exception);
				}
				return null;
			}

			@Override                                                      
			protected void onPostExecute(JSONObject userInfo) {            
				if (userInfo != null) {                                    
					try {                                                  

						JSONArray items = userInfo.getJSONArray("items");
						List<Map<String,String>> posts = new ArrayList<Map<String,String>>();
						for(int i = 0; i < items.length(); i++){
							HashMap<String, String> m = new HashMap<String, String>();
							m.put("key", items.getJSONObject(i).getString("id"));
							m.put("address", items.getJSONObject(i).getString("address"));
							m.put("message",items.getJSONObject(i).getString("message"));
							posts.add(m);
						}
						final SimpleAdapter addrAdapter = new SimpleAdapter(
								mainActivity.this,
								posts,
								R.layout.address_item,
								new String[]{"key", "address", "message"},
								new int[]{R.id.key, R.id.addr, R.id.msg});

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								ListView lv = (ListView)findViewById(R.id.list_view);
								lv.setAdapter(addrAdapter);
								lv.setOnItemClickListener(new OnItemClickListener() {
									@Override
									public void onItemClick(AdapterView<?> adapter, View v, int position, long id) 
									{
										Toast.makeText(mainActivity.this,String.format("clicked %d", id), Toast.LENGTH_LONG).show();
										String key = ((TextView)v.findViewById(R.id.key)).getText().toString();
										Intent launch = new Intent(mainActivity.this, editActivity.class);
										launch.putExtra("key", key);
										startActivityForResult(launch, mainActivity.EDIT);
										//String value = (String)adapter.getItemAtPosition(position); 
										// assuming string and if you want to get the value on click of list item
										// do what you intend to do on click of listview row
									}
								});
							}
						});

					} catch (JSONException e1) {                           
						e1.printStackTrace();                              
					}                                                      
				}                                                          
			}      
		}.execute(accessToken);
	}

	private void alert(String message) {
		result.setText(message);
	}

	private void drive(){
		//result.setText("5732 Lincoln Drive Minneapolis MN");
		Uri gmmIntentUri = Uri.parse("google.navigation:q="+ Uri.encode(result.getText().toString()));
		Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
		mapIntent.setPackage("com.google.android.apps.maps");
		startActivity(mapIntent);
	}

	private void scrub(){
		// uncomment follow for test string
		//result.setText("Meet me at 5732 Lincoln Drive Minneapolis MN");
		final String str = result.getText().toString();
		result.setText("waiting...");
		new AsyncTask<String, Void, JSONObject>() {
			@Override
			protected JSONObject doInBackground(String... tokens) {
				OkHttpClient client = new OkHttpClient();
				RequestBody body = RequestBody.create(CONTENT_TYPE, str);
				HttpUrl reqUrl = HttpUrl.parse(post_uri);

				reqUrl = reqUrl.newBuilder()
					.addQueryParameter("auth-id", auth_id)
					.addQueryParameter("auth-token", auth_token)
					.build();

				Request request = new Request.Builder()
					.url(reqUrl) //
					.addHeader("Accept","application/json")
					.addHeader("Content-Type","text/plain")
					//.addHeader("Authorization", String.format("Bearer %s", tokens[0]))
					.post(body)
					.build();

				try {
					Response response = client.newCall(request).execute();
					String jsonBody = response.body().string();
					Log.i(LOG_TAG, String.format("User Info Response %s", jsonBody));
					//return new JSONObject(jsonBody);
					return new JSONObject(jsonBody);
				} catch (Exception exception) {
					Log.w(LOG_TAG, exception);
				}
				return null;
			}

			@Override                                                      
			protected void onPostExecute(JSONObject userInfo) {            
				if (userInfo != null) {                                    
					try {                                                  
						JSONArray items = userInfo.getJSONArray("addresses");  
						final String scrub =  items.getJSONObject(0).getString("text");
						Log.i(LOG_TAG, String.format("Final address: %s", scrub));
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								result.setText(scrub);
								//((ListView)findViewById(R.id.list_view)).setAdapter(postAdapter);
							}
						});
					} catch (JSONException e1) {                           
						e1.printStackTrace();                              
					}                                                      
				}                                                          
			}      
		}.execute();
	}

	private void deleteEndpointsRequest(String accessToken) {
		new AsyncTask<String, Void, JSONObject>() {
			@Override
			protected JSONObject doInBackground(String... tokens) {
				OkHttpClient client = new OkHttpClient();
				HttpUrl reqUrl = HttpUrl.parse(API_URL);
				reqUrl = reqUrl.newBuilder().build();
				Request request = new Request.Builder()
					.url(reqUrl) //
					.addHeader("Content-Type","application/json")
					.addHeader("Authorization", String.format("Bearer %s", tokens[0]))
					.delete()
					.build();

				try {
					Response response = client.newCall(request).execute();
					//String jsonBody = response.body().string();
					String jsonBody = response.toString();
					Log.i(LOG_TAG, String.format("User Info Response %s", jsonBody));
					//return new JSONObject(jsonBody);
					return new JSONObject("{}");
				} catch (Exception exception) {
					Log.w(LOG_TAG, exception);
				}
				return null;
			}

			@Override
			protected void onPostExecute(JSONObject userInfo){
				Log.w(LOG_TAG, "ok");
			}
		}.execute(accessToken);
	}

	@Override
	protected void onActivityResult(int requestCode,
			int resultCode, Intent data) 
	{
		if (requestCode == EXTRACT && resultCode == RESULT_OK) {
			String s = data.getExtras().getString("address");
			result.setText(s);
			//scrub();
		}
		if (requestCode == EDIT && resultCode == RESULT_OK) {
			String s = data.getExtras().getString("address");
			String id = data.getExtras().getString("id");
			//result.setText(s+" " + id);
			dba.open();
			dba.update(Long.parseLong(id), s);
			dba.populateTable();
		}
	}

	private void extract(){
		Intent launch = new Intent(this, extractActivity.class);
		startActivityForResult(launch, EXTRACT);
	}

	private void signOut() {
		mAuth.signOut();
		updateUI(null);
	}


	private void save() {
		dba.insertTable(
				"Longitude", 
				"Latitude",
				//mLonText.getText().toString(),
				//mLatText.getText().toString(),
				((TextView)findViewById(R.id.result)).getText().toString());
		((TextView)findViewById(R.id.result)).setText("");
	}

	@Override
	protected void onStart() {
		//tv.append("Activity Started\n");
		dba.open();
		mAuth.addAuthStateListener(mAuthListener);
		//FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
		super.onStart();
	}

	@Override
	protected void onPause() {
		dba.close();
		//tv.append("Activity paused\n");
		super.onPause();
	}

	@Override
	protected void onResume() {
		//tv.append("Activity Resumed\n");
		dba.open();
		super.onResume();
	}

	@Override
	protected void onStop() {
		dba.close();
		if (mAuthListener != null) {
			mAuth.removeAuthStateListener(mAuthListener);
		}
		//tv.append("Activity stopped\n");
		super.onStop();
	}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
			case R.id.input:
				String s = ((TextView)findViewById(R.id.input)).getText().toString();
				result.setText(s); 
				((EditText)findViewById(R.id.input)).setText("");
                break;

			case R.id.button_reset:
				deleteEndpointsRequest(fbtoken);
				makeEndpointsRequest(fbtoken);
				//dba.sqlDB.execSQL("DELETE FROM "+ user);
				//dba.populateTable();
                break;
			case R.id.camera:
				extract();
                break;
			case R.id.scrub:
				scrub();
                break;
			case R.id.drive:
				drive();
                break;
			case R.id.insert:
				postEndpointsRequest(fbtoken);
				makeEndpointsRequest(fbtoken);
                break;
			case R.id.logoff:
				signOut();
				finish();
                break;
        }
    }

}
