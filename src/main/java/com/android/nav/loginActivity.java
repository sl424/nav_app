package com.android.nav;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent; 
import android.widget.Button;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class loginActivity extends AppCompatActivity 
{
	public static final String USER = "com.android.nav.USER";
	private static final String TAG = "EmailPassword";

	private String username;
	private String password;
	private String uid;

	private EditText u;
	private EditText p;

	private TextView tv;
	private TextView tv_detail;

	private userdb db;

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;


	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		u = (EditText)findViewById(R.id.user);
		p = (EditText)findViewById(R.id.password);
		u.setText("linsh@oregonstate.edu");
		p.setText("fc9m9apk");

		
		tv = (TextView) findViewById(R.id.status);
		tv_detail = (TextView) findViewById(R.id.detail);


		//createDB(username);
		db = new userdb(this);

		Button btn = (Button) findViewById(R.id.login);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				signIn(u.getText().toString(), p.getText().toString());
			}
		});

		Button btn2 = (Button) findViewById(R.id.register);
		btn2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				createAccount(u.getText().toString(), p.getText().toString());
				//register();
			}
		});

		Button btn3 = (Button) findViewById(R.id.logout);
		btn3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				signOut();
			}
		});

		//OAuth
		mAuth = FirebaseAuth.getInstance();
		mAuthListener = new FirebaseAuth.AuthStateListener() 
		{
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();
				if (user != null) {
					Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
				} else {
					Log.d(TAG, "onAuthStateChanged:signed_out");
				}
				updateUI(user);
			}
		};

	}

	@Override
	protected void onStart() {
		super.onStart();
		tv.append("Activity Started\n");
		db.open();
		mAuth.addAuthStateListener(mAuthListener);
	}

	@Override
	protected void onStop() {
		super.onStop();
		db.close();
		tv.append("Activity stopped\n");
		if (mAuthListener != null) {
			mAuth.removeAuthStateListener(mAuthListener);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		db.close();
		tv.append("Activity paused\n");
	}

	@Override
	protected void onResume() {
		super.onResume();
		tv.append("Activity Resumed\n");
		db.open();
	}

	private void start()
	{
			Intent launch = new Intent(this, mainActivity.class);
			launch.putExtra(USER, uid);
			startActivity(launch);
		/*
		username = u.getText().toString();
		password = p.getText().toString();

		if(db.Login(username, password))
		{
			u.setText("");
			p.setText("");
			Intent launch = new Intent(this, mainActivity.class);
			launch.putExtra(USER, username);
			startActivity(launch);
		}else{
			Toast.makeText(this,"Invalid Username/Password", Toast.LENGTH_LONG).show();
		}
		*/
	}

	private void register()
	{
		username = u.getText().toString();
		password = p.getText().toString();
		if(db.Login(username, password))
			Toast.makeText(this,"duplicate username/password", Toast.LENGTH_LONG).show();
		else
			db.AddUser(username, password);
		start();
	}

	private void updateUI(FirebaseUser user) {
		if (user != null) {
			tv.setText(getString(R.string.emailpassword_status_fmt,
						user.getEmail(), user.isEmailVerified()));
			tv_detail.setText(getString(R.string.firebase_status_fmt, user.getUid()));
			String email = user.getEmail();
			uid = email.substring(0,email.indexOf('@'));

			findViewById(R.id.user).setVisibility(View.GONE);
			findViewById(R.id.password).setVisibility(View.GONE);
			findViewById(R.id.login).setVisibility(View.GONE);
			findViewById(R.id.logout).setVisibility(View.VISIBLE);

			//findViewById(R.id.verify_email_button).setEnabled(!user.isEmailVerified());
		} else {
			tv.setText(R.string.signed_out);
			tv_detail.setText(null);

			findViewById(R.id.user).setVisibility(View.VISIBLE);
			findViewById(R.id.password).setVisibility(View.VISIBLE);
			findViewById(R.id.login).setVisibility(View.VISIBLE);
			findViewById(R.id.logout).setVisibility(View.GONE);
		}
	}
	private boolean validateForm() {
		boolean valid = true;

		String email = u.getText().toString();
		if (TextUtils.isEmpty(email)) {
			u.setError("Required.");
			valid = false;
		} else {
			u.setError(null);
		}

		String password = p.getText().toString();
		if (TextUtils.isEmpty(password)) {
			p.setError("Required.");
			valid = false;
		} else {
			p.setError(null);
		}

		return valid;
	}
	private void createAccount(String email, String password) {
		Log.d(TAG, "createAccount:" + email);
		if (!validateForm()) {
			return;
		}


		// [START create_user_with_email]
		mAuth.createUserWithEmailAndPassword(email, password)
			.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

					// If sign in fails, display a message to the user. If sign in succeeds
					// the auth state listener will be notified and logic to handle the
					// signed in user can be handled in the listener.
					if (!task.isSuccessful()) {
						Toast.makeText(loginActivity.this, R.string.auth_failed,
								Toast.LENGTH_SHORT).show();
					}

					// [START_EXCLUDE]
					// [END_EXCLUDE]
				}
			});
		// [END create_user_with_email]
	}

	private void signIn(String email, String password) {
		Log.d(TAG, "signIn:" + email);
		if (!validateForm()) {
			return;
		}


		// [START sign_in_with_email]
		mAuth.signInWithEmailAndPassword(email, password)
			.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

					if (task.isSuccessful()) {
						start();
					}

					// If sign in fails, display a message to the user. If sign in succeeds
					// the auth state listener will be notified and logic to handle the
					// signed in user can be handled in the listener.
					if (!task.isSuccessful()) {
						Log.w(TAG, "signInWithEmail:failed", task.getException());
						Toast.makeText(loginActivity.this, R.string.auth_failed,
								Toast.LENGTH_SHORT).show();
					}

					// [START_EXCLUDE]
					if (!task.isSuccessful()) {
						tv.setText(R.string.auth_failed);
					}
					// [END_EXCLUDE]
				}
			});
		// [END sign_in_with_email]
	}

	private void signOut() {
		mAuth.signOut();
		updateUI(null);
	}
}
