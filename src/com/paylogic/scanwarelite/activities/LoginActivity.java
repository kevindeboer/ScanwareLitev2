package com.paylogic.scanwarelite.activities;

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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.paylogic.scanwarelite.APIFacade;
import com.paylogic.scanwarelite.ApiResponse;
import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.ScanwareLiteApplication;
import com.paylogic.scanwarelite.TaskListener;
import com.paylogic.scanwarelite.dialogs.UnhandledExceptionDialog;
import com.paylogic.scanwarelite.dialogs.login.EmptyInputDialog;
import com.paylogic.scanwarelite.dialogs.login.EmptyPasswordDialog;
import com.paylogic.scanwarelite.dialogs.login.EmptyUsernameDialog;
import com.paylogic.scanwarelite.dialogs.login.InvalidCredentialsDialog;
import com.paylogic.scanwarelite.dialogs.login.LoginDialog;
import com.paylogic.scanwarelite.dialogs.login.NoLocalDataDialog;
import com.paylogic.scanwarelite.helpers.ConnectivityHelper;
import com.paylogic.scanwarelite.helpers.OfflineLoginHelper;
import com.paylogic.scanwarelite.helpers.PreferenceHelper;
import com.paylogic.scanwarelite.models.User;
import com.paylogic.scanwarelite.security.BCrypt;

public class LoginActivity extends Activity {

	private EditText usernameView;
	private EditText passwordView;
	private Button loginButton;

	private AlertDialog alertDialog;
	private ProgressDialog progressDialog;

	private OnlineLoginTask onlineLoginTask;
	private OfflineLoginTask offlineLoginTask;

	private String username;
	private String password;
	private String passwordHash;
	private String usernameHash;
	private User user;

	private ScanwareLiteApplication app;

	private SharedPreferences settings;
	private SharedPreferences.Editor editor;

	private ConnectivityHelper connHelper;

	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		context = LoginActivity.this;
		connHelper = new ConnectivityHelper(context);
		offlineLoginTask = new OfflineLoginTask();
		onlineLoginTask = new OnlineLoginTask();

		settings = getSharedPreferences(PreferenceHelper.PREFS_FILE,
				Context.MODE_PRIVATE);

		app = (ScanwareLiteApplication) ((Activity) context).getApplication();
		loginButton = (Button) findViewById(R.id.button_login);
		usernameView = (EditText) findViewById(R.id.editText_username);
		passwordView = (EditText) findViewById(R.id.editText_password);

		editor = settings.edit();
		editor.putBoolean(PreferenceHelper.KEY_SHOW_ALL, false);
		editor.commit();

		if (getIntent().getBooleanExtra("error", false)) {
			alertDialog = new UnhandledExceptionDialog(context);
			alertDialog.show();
		}

	}

	protected void onResume() {
		super.onResume();
		loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String username = usernameView.getText().toString();
				String password = passwordView.getText().toString();
				boolean isConnected = connHelper.isConnected();
				handleLogin(username, password, isConnected);

			}
		});
	}

	public void handleLogin(String username, String password,
			boolean isConnected) {
		// if both inputs are empty
		if (username.length() == 0 && password.length() == 0) {
			alertDialog = new EmptyInputDialog(context);
			alertDialog.show();
			return;
		}

		// if password is empty
		if (password.length() == 0) {
			alertDialog = new EmptyPasswordDialog(context);
			alertDialog.show();
			return;
		}

		// if username is empty
		if (username.length() == 0) {
			alertDialog = new EmptyUsernameDialog(context);
			alertDialog.show();
			return;
		}

		// if connected to internet
		if (isConnected) {
			onlineLoginTask.execute(username, password);

			// A task can not be executed twice, so a new task needs to be
			// created for the next time you click the button
			onlineLoginTask = new OnlineLoginTask();
		} else {
			offlineLoginTask.execute(username, password);

			// A task can not be executed twice, so a new task needs to be
			// created for the next time you click the button
			offlineLoginTask = new OfflineLoginTask();
		}
	}

	public void setConnectivityHelper(ConnectivityHelper connHelper) {
		this.connHelper = connHelper;
	}

	public void setOnlineLoginTask(OnlineLoginTask onlineLoginTask) {
		this.onlineLoginTask = onlineLoginTask;
	}

	public void setOfflineLoginTask(OfflineLoginTask offlineLoginTask) {
		this.offlineLoginTask = offlineLoginTask;
	}

	public class OfflineLoginTask extends AsyncTask<String, Void, Void> {

		private final int validLocalCredentials = 1;
		private final int invalidLocalCredentials = 2;
		private final int noLocalData = 3;

		private int result = 0;

		private OfflineLoginHelper olHelper;

		// Used for tests
		private TaskListener listener;

		public OfflineLoginTask() {
			this.olHelper = new OfflineLoginHelper(context);
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
			if (olHelper.userFileExists()) {
				String fileContent = olHelper.getUserFileContent();
				String[] contents = fileContent.split(" ");

				int userId = Integer.parseInt(contents[0]);
				usernameHash = contents[1];
				passwordHash = contents[2];
				System.out.println();
				boolean match = (BCrypt.checkpw(password, passwordHash) && BCrypt
						.checkpw(username, usernameHash));
				// boolean match = true;
				if (match) {
					result = validLocalCredentials;
					user = new User(userId, usernameHash, passwordHash);
				} else {
					result = invalidLocalCredentials;
				}
			} else {
				result = noLocalData;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			if (this.result == validLocalCredentials) {
				Intent intent = new Intent(context, EventsActivity.class);

				app.setUser(user);

				startActivity(intent);

			} else if (this.result == invalidLocalCredentials) {
				alertDialog = new InvalidCredentialsDialog(context);
				alertDialog.show();

			} else if (this.result == noLocalData) {
				alertDialog = new NoLocalDataDialog(context);
				alertDialog.show();
			}
			if (listener != null) {
				listener.executionDone();
			}
		}

		public void setOfflineLoginHelper(OfflineLoginHelper olHelper) {
			this.olHelper = olHelper;
		}

		public void setListener(TaskListener listener) {
			this.listener = listener;
		}
	}

	public class OnlineLoginTask extends AsyncTask<String, Void, Void> {

		private int response;

		private APIFacade apiFacade;
		private OfflineLoginHelper olHelper;

		// Used for tests
		private TaskListener listener;

		public OnlineLoginTask() {
			this.apiFacade = new APIFacade();
			this.olHelper = new OfflineLoginHelper(context);
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
					response = Integer.parseInt(errorElement
							.getAttribute("code"));
				}
			} else if (root.getNodeName().equals("sparq")) {
				Node userIdNode = root.getElementsByTagName("userid").item(0);
				if (userIdNode.getNodeType() == Node.ELEMENT_NODE) {
					Element userIdElement = (Element) userIdNode;
					int userId = Integer.parseInt(userIdElement
							.getAttribute("id"));

					String salt = BCrypt.gensalt();

					usernameHash = BCrypt.hashpw(username, salt);
					passwordHash = BCrypt.hashpw(password, salt);

					user = new User(userId, username, password);
					// usernameHash = "asdasd";
					// passwordHash = "asdasd";
					response = ApiResponse.OK;
				}
			}

		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();

			if (response == ApiResponse.OK) {
				Intent intent = new Intent(context, EventsActivity.class);

				String writeString = user.getUserId() + " " + usernameHash
						+ " " + passwordHash;
				FileOutputStream userFileStream;

				try {
					userFileStream = olHelper.openUserFileOutput();
					userFileStream.write(writeString.getBytes());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				app.setUser(user);

				startActivity(intent);
			} else if (response == ApiResponse.INVALID_LOGIN) {
				alertDialog = new InvalidCredentialsDialog(context);
				alertDialog.show();
			}

			if (listener != null) {
				listener.executionDone();
			}
		}

		public void setListener(TaskListener listener) {
			this.listener = listener;
		}

		public void setAPIFacade(APIFacade apiFacade) {
			this.apiFacade = apiFacade;
		}

	}
}
