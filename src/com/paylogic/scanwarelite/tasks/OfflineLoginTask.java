package com.paylogic.scanwarelite.tasks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.paylogic.scanwarelite.ScanwareLiteApplication;
import com.paylogic.scanwarelite.activities.EventsActivity;
import com.paylogic.scanwarelite.dialogs.login.InvalidCredentialsDialog;
import com.paylogic.scanwarelite.dialogs.login.LoginDialog;
import com.paylogic.scanwarelite.dialogs.login.NoLocalDataDialog;
import com.paylogic.scanwarelite.helpers.PreferenceHelper;
import com.paylogic.scanwarelite.models.User;
import com.paylogic.scanwarelite.security.BCrypt;

public class OfflineLoginTask extends AsyncTask<String, Void, Void> {

	private static final int VALID_LOCAL_CREDENTIALS = 1;
	private static final int INVALID_LOCAL_CREDENTIALS = 2;
	private static final int NO_LOCAL_DATA = 3;

	private ProgressDialog progressDialog;
	private AlertDialog alertDialog;
	private User user;
	private Context context;
	private ScanwareLiteApplication app;
	private SharedPreferences settings;
	private String username;
	private String password;
	private int result = 0;

	public OfflineLoginTask(Context context) {
		this.context = context;
		this.app = (ScanwareLiteApplication) ((Activity) context)
				.getApplication();
		this.settings = context.getSharedPreferences(
				PreferenceHelper.PREFS_FILE, Context.MODE_PRIVATE);
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new LoginDialog(context);
		progressDialog.show();
	}

	@Override
	protected Void doInBackground(String... params) {
		String line = "";
		String fileContent = "";

		username = params[0];
		password = params[1];

		String userFile = settings.getString(PreferenceHelper.KEY_USER_FILE,
				null);

		if (context.getFileStreamPath(userFile).exists()) {
			try {
				InputStream inputStream = context.openFileInput(userFile);
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				while ((line = bufferedReader.readLine()) != null) {
					fileContent += line;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String[] contents = fileContent.split(" ");

			int userId = Integer.parseInt(contents[0]);
			String usernameHash = contents[1];
			String passwordHash = contents[2];

			boolean match = (BCrypt.checkpw(password, passwordHash) && BCrypt
					.checkpw(username, usernameHash));
			// boolean match = true;
			if (match) {
				result = VALID_LOCAL_CREDENTIALS;
				user = new User(userId, usernameHash, passwordHash);
			} else {
				result = INVALID_LOCAL_CREDENTIALS;
			}
		} else {
			result = NO_LOCAL_DATA;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		progressDialog.dismiss();
		if (this.result == VALID_LOCAL_CREDENTIALS) {
			Intent intent = new Intent(context, EventsActivity.class);

			app.setUser(user);

			context.startActivity(intent);

		} else if (this.result == INVALID_LOCAL_CREDENTIALS) {
			alertDialog = new InvalidCredentialsDialog(context);
			alertDialog.show();

		} else if (this.result == NO_LOCAL_DATA) {
			alertDialog = new NoLocalDataDialog(context);
			alertDialog.show();
		}
	}

}