package com.android.nav;

import android.util.Log;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent; 
import android.widget.Button;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import android.net.Uri;

import android.util.Log;
import android.os.AsyncTask;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.FormBody;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import android.support.annotation.NonNull;


import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;


public class editActivity extends AppCompatActivity 
{
	private EditText addr_edit;
	private EditText msg_edit;
	private EditText lat_edit;
	private EditText lon_edit;
	private String addrstr;
	private String key;
	private Button btn_save;
	private Button btn_drive;
	private Button btn_del;
	private TextView result;

	private String LOG_TAG = "******\neditActivity******"; 
	private String API_URL = "https://backend-dot-firebase-nav-app.appspot.com/notes";
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;
	private FirebaseUser fbuser;
	private String fbtoken;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);

		addr_edit = (EditText)findViewById(R.id.addr_edit);
		msg_edit = (EditText)findViewById(R.id.msg_edit);
		lat_edit = (EditText)findViewById(R.id.lat_edit);
		lon_edit = (EditText)findViewById(R.id.lon_edit);
		result = (TextView)findViewById(R.id.result);

		btn_save = (Button) findViewById(R.id.save);
		btn_drive = (Button) findViewById(R.id.drive);
		btn_del = (Button) findViewById(R.id.delete);

		Intent i = getIntent();
		key = i.getStringExtra("key");
		API_URL = API_URL + "/"+ key;

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


		btn_save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				postEndpointsRequest(fbtoken);
				finish();
			}
		});

		btn_drive.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//register();
				Uri gmmIntentUri = Uri.parse("google.navigation:q="+ Uri.encode(addr_edit.getText().toString()));
				Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
				mapIntent.setPackage("com.google.android.apps.maps");
				startActivity(mapIntent);
			}
		});

		btn_del.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				deleteEndpointsRequest(fbtoken);
				finish();
			}
		});
	}

	private void updateUI(FirebaseUser fbuser) {
		//hideProgressDialog();
		if (fbuser != null) {
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
		final String addObj = String.format("{\"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\"}", 
				"message", (msg_edit.getText().toString()).replaceAll("(\\r|\\n)", " "),
				"latitude", (lat_edit.getText().toString()).replaceAll("(\\r|\\n)", " "),
				"longitude", (lon_edit.getText().toString()).replaceAll("(\\r|\\n)", " "),
				"address", (addr_edit.getText().toString()).replaceAll("(\\r|\\n)", " ")
				);

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
					.patch(body)
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
				finish();
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
						//JSONArray items = userInfo.getJSONArray("items");
						//final String addrstr = items.getJSONObject(0).getString("address");
						//final String msgstr = items.getJSONObject(0).getString("message");
						final String addrstr = userInfo.getString("address");
						final String msgstr = userInfo.getString("message");
						final String latstr = userInfo.getString("latitude");
						final String lonstr = userInfo.getString("longitude");
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								addr_edit.setText(addrstr);
								msg_edit.setText(msgstr);
								lat_edit.setText(latstr);
								lon_edit.setText(lonstr);
							}
						});
					} catch (JSONException e1) {                           
						e1.printStackTrace();                              
					}                                                      
				}                                                          
			}      
		}.execute(accessToken);
	}

	@Override
	protected void onStart() {
		//tv.append("Activity Started\n");
		mAuth.addAuthStateListener(mAuthListener);
		//FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
		super.onStart();
	}

	@Override
	protected void onStop() {
		if (mAuthListener != null) {
			mAuth.removeAuthStateListener(mAuthListener);
		}
		//tv.append("Activity stopped\n");
		super.onStop();
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
				finish();
			}
		}.execute(accessToken);
	}

}
