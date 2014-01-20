package com.paylogic.scanwarelite.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.paylogic.scanwarelite.APIFacade;
import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.UnhandledExceptionDialog;
import com.paylogic.scanwarelite.dialogs.login.EmptyInputDialog;
import com.paylogic.scanwarelite.dialogs.login.EmptyPasswordDialog;
import com.paylogic.scanwarelite.dialogs.login.EmptyUsernameDialog;
import com.paylogic.scanwarelite.helpers.ConnectivityHelper;
import com.paylogic.scanwarelite.helpers.PreferenceHelper;
import com.paylogic.scanwarelite.tasks.OfflineLoginTask;
import com.paylogic.scanwarelite.tasks.OnlineLoginTask;

public class LoginActivity extends Activity {

	private EditText usernameView;
	private EditText passwordView;
	private Button loginButton;
	private AlertDialog alertDialog;

	private ConnectivityHelper connHelper;
	private OnlineLoginTask onlineLoginTask;
	private OfflineLoginTask offlineLoginTask;

	private String userFile;

	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private APIFacade apiFacade;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		setConnectivityHelper(new ConnectivityHelper());

		settings = getSharedPreferences(PreferenceHelper.PREFS_FILE,
				Context.MODE_PRIVATE);

		loginButton = (Button) findViewById(R.id.button_login);
		usernameView = (EditText) findViewById(R.id.editText_username);
		passwordView = (EditText) findViewById(R.id.editText_password);
		userFile = "user";

		editor = settings.edit();
		editor.putString(PreferenceHelper.KEY_USER_FILE, userFile);
		editor.commit();

		if (getIntent().getBooleanExtra("error", false)) {
			alertDialog = new UnhandledExceptionDialog(LoginActivity.this);
			alertDialog.show();
		}
		
		apiFacade = new APIFacade();
		
		setOnlineLoginTask(new OnlineLoginTask(LoginActivity.this, apiFacade));
		setOfflineLoginTask(new OfflineLoginTask(LoginActivity.this));
	}

	protected void onResume() {
		super.onResume();
		loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String username = usernameView.getText().toString();
				String password = passwordView.getText().toString();
				boolean isConnected = connHelper.isConnected(
						LoginActivity.this);
				handleLogin(username, password, isConnected);
				
			}
		});
	}

	public void handleLogin(String username, String password,
			boolean isConnected) {
		// if both inputs are empty
		if (username.length() == 0 && password.length() == 0) {
			alertDialog = new EmptyInputDialog(LoginActivity.this);
			alertDialog.show();
			return;
		}

		// if password is empty
		if (password.length() == 0) {
			alertDialog = new EmptyPasswordDialog(LoginActivity.this);
			alertDialog.show();
			return;
		}

		// if username is empty
		if (username.length() == 0) {
			alertDialog = new EmptyUsernameDialog(LoginActivity.this);
			alertDialog.show();
			return;
		}


		// if connected to internet
		if (isConnected) {
			onlineLoginTask.execute(username, password);
		} else {
			offlineLoginTask.execute(username, password);
		}
	}

	public void setConnectivityHelper(ConnectivityHelper connHelper) {
		this.connHelper = connHelper;
	}
	
	public void setOnlineLoginTask(OnlineLoginTask onlineLoginTask){
		this.onlineLoginTask = onlineLoginTask;
	}
	
	public void setOfflineLoginTask(OfflineLoginTask offlineLoginTask){
		this.offlineLoginTask = offlineLoginTask;
	}

}
