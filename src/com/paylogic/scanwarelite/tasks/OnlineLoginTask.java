package com.paylogic.scanwarelite.tasks;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.paylogic.scanwarelite.APIFacade;
import com.paylogic.scanwarelite.ApiResponse;
import com.paylogic.scanwarelite.ScanwareLiteApplication;
import com.paylogic.scanwarelite.TaskListener;
import com.paylogic.scanwarelite.activities.EventsActivity;
import com.paylogic.scanwarelite.dialogs.login.InvalidCredentialsDialog;
import com.paylogic.scanwarelite.dialogs.login.LoginDialog;
import com.paylogic.scanwarelite.helpers.PreferenceHelper;
import com.paylogic.scanwarelite.models.User;
import com.paylogic.scanwarelite.security.BCrypt;

public class OnlineLoginTask extends AsyncTask<String, Void, Void> {

	private int response;

	private String passwordHash;
	private String usernameHash;

	private ProgressDialog progressDialog;
	private AlertDialog alertDialog;
	private Context context;

	private SharedPreferences settings;
	private SharedPreferences.Editor editor;

	private String username;
	private String password;

	private ScanwareLiteApplication app;
	private User user;

	private APIFacade apiFacade;

	// Used for tests
	private TaskListener listener;

	public OnlineLoginTask(Context context, APIFacade apiFacade) {
		this.context = context;
		this.app = (ScanwareLiteApplication) ((Activity) context)
				.getApplication();
		this.settings = context.getSharedPreferences(
				PreferenceHelper.PREFS_FILE, Context.MODE_PRIVATE);

		this.apiFacade = apiFacade;
	}

	public void setListener(TaskListener listener) {
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new LoginDialog(context);
		progressDialog.show();
	}

	@Override
	protected Void doInBackground(String... params) {
		username = params[0];
		password = params[1];
		Document response = apiFacade.login(username, password);

		if (response != null) {
			parseResponse(response);
		} else {
			// handle null in APIFacade
		}
		return null;

	}

	private void parseResponse(Document doc) {
		Element root = doc.getDocumentElement();
		if (root.getNodeName().equals("errorList")) {
			Node errorNode = root.getElementsByTagName("error").item(0);
			if (errorNode.getNodeType() == Node.ELEMENT_NODE) {
				Element errorElement = (Element) errorNode;
				response = Integer.parseInt(errorElement.getAttribute("code"));
			}
		} else if (root.getNodeName().equals("sparq")) {
			Node userIdNode = root.getElementsByTagName("userid").item(0);
			if (userIdNode.getNodeType() == Node.ELEMENT_NODE) {
				Element userIdElement = (Element) userIdNode;
				int userId = Integer.parseInt(userIdElement.getAttribute("id"));

				String salt = BCrypt.gensalt();

				usernameHash = BCrypt.hashpw(username, salt);
				passwordHash = BCrypt.hashpw(password, salt);

				user = new User(userId, username, password);
//				 usernameHash = "asdasd";
//				 passwordHash = "asdasd";
				response = ApiResponse.OK;
			}
		}

	}

	@Override
	protected void onPostExecute(Void result) {
		progressDialog.dismiss();
		if (response == ApiResponse.OK) {
			Intent intent = new Intent(context, EventsActivity.class);

			String writeString = user.getUserId() + " " + usernameHash + " "
					+ passwordHash;
			FileOutputStream userFileStream;

			String userFile = settings.getString(
					PreferenceHelper.KEY_USER_FILE, null);
			try {
				userFileStream = context.openFileOutput(userFile,
						Context.MODE_PRIVATE);
				userFileStream.write(writeString.getBytes());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			app.setUser(user);

			editor = settings.edit();
			editor.putBoolean(PreferenceHelper.KEY_SHOW_ALL, false);
			editor.commit();

			context.startActivity(intent);
		} else if (response == ApiResponse.INVALID_LOGIN) {
			alertDialog = new InvalidCredentialsDialog(context);
			alertDialog.show();
		}
		
		if (listener != null) {
			listener.executionDone();
		}
	}

}
