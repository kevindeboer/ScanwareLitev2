package com.paylogic.scanwarelite.activities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.paylogic.scanwarelite.ApiResponse;
import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.ScanwareLiteApplication;
import com.paylogic.scanwarelite.dialogs.UnhandledExceptionDialog;
import com.paylogic.scanwarelite.dialogs.login.EmptyInputDialog;
import com.paylogic.scanwarelite.dialogs.login.EmptyPasswordDialog;
import com.paylogic.scanwarelite.dialogs.login.EmptyUsernameDialog;
import com.paylogic.scanwarelite.dialogs.login.InvalidCredentialsDialog;
import com.paylogic.scanwarelite.dialogs.login.LoginDialog;
import com.paylogic.scanwarelite.dialogs.login.NoLocalDataDialog;
import com.paylogic.scanwarelite.helpers.ConnectivityHelper;
import com.paylogic.scanwarelite.helpers.PreferenceHelper;
import com.paylogic.scanwarelite.security.BCrypt;

public class LoginActivity extends Activity {

	private ScanwareLiteApplication app;

	private EditText usernameView;
	private EditText passwordView;
	private Button loginButton;
	private AlertDialog alertDialog;
	private ProgressDialog progressDialog;

	private OnlineLoginTask onlineLoginTask;
	private OfflineLoginTask offlineLoginTask;

	private int userId;
	private String username;
	private String password;

	private String userFile;

	private SharedPreferences settings;
	private SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		app = (ScanwareLiteApplication) getApplication();

		settings = getSharedPreferences(PreferenceHelper.PREFS_FILE,
				Context.MODE_PRIVATE);

		loginButton = (Button) findViewById(R.id.button_login);
		usernameView = (EditText) findViewById(R.id.editText_username);
		passwordView = (EditText) findViewById(R.id.editText_password);
		userFile = "user";

		editor = settings.edit();
		editor.putString(PreferenceHelper.KEY_USER_FILE, userFile);
		editor.commit();

		Intent intent = getIntent();
		if (intent.getBooleanExtra("error", false)) {
			alertDialog = new UnhandledExceptionDialog(LoginActivity.this)
					.create();
			alertDialog.show();
		}
	}

	protected void onResume() {
		super.onResume();
		loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				username = usernameView.getText().toString();
				password = passwordView.getText().toString();

				// if both inputs are empty
				if (username.length() == 0 && password.length() == 0) {
					alertDialog = new EmptyInputDialog(LoginActivity.this)
							.create();
					alertDialog.show();
					return;
				}

				// if password is empty
				if (password.length() == 0) {
					alertDialog = new EmptyPasswordDialog(LoginActivity.this)
							.create();
					alertDialog.show();
					return;
				}

				// if username is empty
				if (username.length() == 0) {
					alertDialog = new EmptyUsernameDialog(LoginActivity.this)
							.create();
					alertDialog.show();
					return;
				}
				// if connected to internet
				if (ConnectivityHelper.isConnected(LoginActivity.this)) {
					onlineLoginTask = new OnlineLoginTask();
					onlineLoginTask.execute();
				} else {
					offlineLoginTask = new OfflineLoginTask();
					offlineLoginTask.execute();
				}
			}
		});
	}

	private class OnlineLoginTask extends AsyncTask<Void, Void, Void> {
		private String url = "https://api.paylogic.nl/API/?command=";

		private HttpURLConnection conn;
		private int response;

		private String passwordHash;
		private String usernameHash;

		@Override
		protected void onPreExecute() {
			progressDialog = new LoginDialog(LoginActivity.this);
			progressDialog.show();
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			if (response == ApiResponse.OK) {
				Intent intent = new Intent(LoginActivity.this,
						EventsActivity.class);

				String writeString = userId + " " + usernameHash + " "
						+ passwordHash;
				FileOutputStream userFileStream;

				try {
					userFileStream = openFileOutput(userFile,
							Context.MODE_PRIVATE);
					userFileStream.write(writeString.getBytes());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				app.setUserId(userId);
				app.setUsername(username);
				app.setPassword(password);

				editor = settings.edit();
				editor.putBoolean(PreferenceHelper.KEY_SHOW_ALL, false);
				editor.commit();

				startActivity(intent);
			} else if (response == ApiResponse.INVALID_LOGIN) {
				alertDialog = new InvalidCredentialsDialog(LoginActivity.this)
						.create();
				alertDialog.show();
			}
		}

		@Override
		protected Void doInBackground(Void... args) {
			String command = "sparqLogin";
			String urlParams = "&username=" + username + "&password="
					+ password;

			String loginUrl = url + command + urlParams;

			try {
				URL url = new URL(loginUrl);

				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");

				parseResponse(conn);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;

		}

		private void parseResponse(HttpURLConnection conn) {
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(conn.getInputStream());

				Element root = doc.getDocumentElement();
				if (root.getNodeName().equals("errorList")) {
					Node errorNode = root.getElementsByTagName("error").item(0);
					if (errorNode.getNodeType() == Node.ELEMENT_NODE) {
						Element errorElement = (Element) errorNode;
						response = Integer.parseInt(errorElement
								.getAttribute("code"));
					}
				} else if (root.getNodeName().equals("sparq")) {
					Node userIdNode = root.getElementsByTagName("userid").item(
							0);
					if (userIdNode.getNodeType() == Node.ELEMENT_NODE) {
						Element userIdElement = (Element) userIdNode;
						userId = Integer.parseInt(userIdElement
								.getAttribute("id"));

						String salt = BCrypt.gensalt();

						usernameHash = BCrypt.hashpw(username, salt);
						passwordHash = BCrypt.hashpw(password, salt);
						// usernameHash = "asdasd";
						// passwordHash = "asdasd";
						response = ApiResponse.OK;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
	}

	private class OfflineLoginTask extends AsyncTask<Void, Void, Integer> {

		private static final int VALID_LOCAL_CREDENTIALS = 1;
		private static final int INVALID_LOCAL_CREDENTIALS = 2;
		private static final int NO_LOCAL_DATA = 3;

		private String passwordHash;
		private String usernameHash;

		@Override
		protected void onPreExecute() {
			progressDialog = new LoginDialog(LoginActivity.this);
			progressDialog.show();
		}

		@Override
		protected void onPostExecute(Integer result) {
			progressDialog.dismiss();
			if (result == VALID_LOCAL_CREDENTIALS) {
				Intent intent = new Intent(LoginActivity.this,
						EventsActivity.class);

				app.setUserId(userId);
				app.setUsername(username);
				app.setPassword(password);

				startActivity(intent);

			} else if (result == INVALID_LOCAL_CREDENTIALS) {
				alertDialog = new InvalidCredentialsDialog(LoginActivity.this)
						.create();
				alertDialog.show();

			} else if (result == NO_LOCAL_DATA) {
				alertDialog = new NoLocalDataDialog(LoginActivity.this)
						.create();
				alertDialog.show();
			}
		}

		@Override
		protected Integer doInBackground(Void... params) {
			String line = "";
			String fileContent = "";
			if (getFileStreamPath(userFile).exists()) {
				try {
					InputStream inputStream = openFileInput(userFile);
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
				userId = Integer.parseInt(contents[0]);
				usernameHash = contents[1];
				passwordHash = contents[2];

				boolean match = (BCrypt.checkpw(password, passwordHash) && BCrypt
						.checkpw(username, usernameHash));
				// boolean match = true;
				if (match) {
					return VALID_LOCAL_CREDENTIALS;
				} else {
					return INVALID_LOCAL_CREDENTIALS;
				}
			} else {
				return NO_LOCAL_DATA;
			}
		}

	}

}
